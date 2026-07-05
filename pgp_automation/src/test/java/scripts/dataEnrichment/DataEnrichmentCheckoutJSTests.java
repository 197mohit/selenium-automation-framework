package scripts.dataEnrichment;

import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.paytm.appconstants.Constants.Owner.ARSH;

/**
 * Created by Arsh Gupta on 20/01/21.
 */
@Owner(ARSH)
public class DataEnrichmentCheckoutJSTests extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutJsCheckoutPage = new CheckoutJsCheckoutPage();
    private String COPPAYRequest;


    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify Risk Parameters going in Checkout JS flow")
    public void verifyRiskParametersInCheckoutJs(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateCheckoutJsRiskExtendInfoParameters(jsonPath, theme);

        DataEnrichmentValidations.validatePaymentTypeFieldInPaymentBizInfoInCheckoutJS(jsonPath);//Phase 1.1 fields validation

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify risk parameters in case of add and pay transaction")
    public void verifyAddAndPayScenario(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .setTxnValue("2")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateCheckoutJsRiskExtendInfoParameters(jsonPath, theme);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentFlow")).isEqualTo("ADD_N_PAY").as("Incorrect paymentFlow");//Phase 1.1 parameter validation

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify risk parameters in case of MDR+PCF transaction")
    public void verifyRiskParametersForMDRPCFMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.MDR_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("9")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateCheckoutJsRiskExtendInfoParameters(jsonPath, theme);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme", "browser"})
    @Test(description = "Verify that osType, browserType, deviceModel, deviceManufacturer fields are going in WEB and mWeb Checkout JS transaction")
    public void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsWEBAndMWebInCheckoutJs(@Optional("checkoutjs_web_revamp") String theme, @Optional("chrome") String browser) throws IOException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
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
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify that osType, browserType, deviceModel, deviceManufacturer fields are going in IOS mWeb Checkout JS transaction")
    public void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsIOSmWebInCheckoutJs(String theme) throws IOException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsIOSMobileWeb(jsonPath);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify paymentBizInfo parameters for Saved card")
    public void validatePaymentBizInfoForSavedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.VISA_CREDIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isEqualTo("true").as("card is not saved");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(ARSH)
    @Parameters({"theme"})
    @Test(description = "Verify paymentBizInfo parameters for NB")
    public void verifyPaymentBizInfoParametersForNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("redirectToBank").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("NET_BANKING").as("payMethod mismatch");
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