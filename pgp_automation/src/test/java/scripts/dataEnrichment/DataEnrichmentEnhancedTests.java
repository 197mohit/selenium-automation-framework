package scripts.dataEnrichment;

import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.ARSH;


/**
 * Created by Arsh Gupta on 20/01/21.
 */
@Owner(ARSH)
public class DataEnrichmentEnhancedTests extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String COPPAYRequest;

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify Risk Parameters going in Enhanced native flow")
    public void verifyRiskParametersInEnhancedNative(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.getAmexCardNumber()).setCvvNumber("1111");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateEnhancedNativeRiskExtendInfoParameters(jsonPath, theme);

        DataEnrichmentValidations.validatePaymentTypeFieldInPaymentBizInfoInEnhancedNative(jsonPath);//Phase 1.1 fields validation

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify risk parameters in case of hybrid transaction")
    public void verifyHybridScenario(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateEnhancedNativeRiskExtendInfoParameters(jsonPath, theme);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentFlow")).isEqualTo("HYBRID_PAY").as("Incorrect paymentFlow");//Phase 1.1 parameter validation

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validatePaymentMode("HYBRID")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify risk parameters in case of MP add money transaction")
    public void verifyAddMoneyMPScenario(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(Constants.MerchantType.AddMoneyMP, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateEnhancedNativeRiskExtendInfoParameters(jsonPath, theme);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify risk parameters in case of PCF transaction")
    public void verifyRiskParametersForPCFMerchant(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.FLAT_PCF, theme)
                .setTXN_AMOUNT("9").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateEnhancedNativeRiskExtendInfoParameters(jsonPath, theme);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify that when clientIp is not sent from client, theia derives the same and send in PAY/COP request")
    public void verifyClientIpFieldDerivedByTheia(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.getAmexCardNumber()).setCvvNumber("1111");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isNotNull().as("clientIp cannot be null");
        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);
        Assertions.assertThat(jsonPath1.getString("clientIp")).isNotNull().as("clientIp cannot be null");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS");
    }

    /////////////////////////////////osType, browserType, deviceModel, deviceManufacturer fields validation test cases///////////////////////////////////////

    @Owner(ARSH)
    @Parameters({"theme", "browser"})
    @Test(description = "Verify that osType, browserType, deviceModel, deviceManufacturer fields are going in WEB and mWeb Enhanced Native transaction")
    public void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsWEBAndMWeb(@Optional("enhancedweb_revamp") String theme, @Optional("chrome") String browser) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.getAmexCardNumber()).setCvvNumber("1111");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        if (theme.contains("web")) {
            DataEnrichmentValidations.validateOsTypeBrowserTypeDeviceModelDeviceManufacturerParametersWEB(jsonPath, browser);
        } else {
            DataEnrichmentValidations.validateOsTypeBrowserTypeDeviceModelDeviceManufacturerParametersMobileWEB(jsonPath);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify that osType, browserType, deviceModel, deviceManufacturer fields are going in IOS mWeb Enhanced Native transaction")
    public void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsIOSmWeb(String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.getAmexCardNumber()).setCvvNumber("1111");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsIOSMobileWeb(jsonPath);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }



    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify value of deviceType field in Tablet device transaction")
    public void validateDeviceTypeFieldInTabletDevice(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateDeviceTypeFieldInTabletDevice(jsonPath);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters("theme")
    @Test(description = "Verify paymentBizInfo parameters for International card")
    public void validatePaymentBizInfoForInternationalCard(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ALLPAYMODE, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isEqualTo("true").as("Card is not international");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify paymentBizInfo parameters for PPBL_NB")
    public void VerifyPaymentBizInfoParametersForPPBL_NB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.tabPPBL().click();
        cashierPage.pause(3);
        cashierPage.payBy(Constants.PayMode.PPBL);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("passcodeOnPGPage").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("PPBL").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails")).as("List is not empty").isEmpty();//as per the current handling

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
}