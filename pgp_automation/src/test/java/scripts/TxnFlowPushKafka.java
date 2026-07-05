package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.NATIVE_HYBRID;
import static com.paytm.appconstants.Constants.MerchantType.PGOnly;
import static com.paytm.appconstants.Constants.Owner.*;

public class TxnFlowPushKafka extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate the successful transaction with DC paymode And validate all txn data push over kafka")
    public void validateFlowforCheckoutJsDCTxnAndtopicPushInKafka(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.orderFromBody(), "CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=DEBIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate the successful transaction with NB paymode And validate all txn data push over kafka")
    public void validateFlowforCheckoutJsNBTxnAndtopicPushInKafka(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.orderFromBody(), "CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=NET_BANKING");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate the successful transaction with CC paymode And validate all txn data push over kafka")
    public void validateFlowforCheckoutJsCCTxnAndtopicPushInKafka(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.orderFromBody(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=CREDIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate the successful transaction with UPI paymode And validate all txn data push over kafka")
    public void validateFlowforCheckoutJsUPITxnAndtopicPushInKafka(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.orderFromBody(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=UPI");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for CC and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutCCTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=CREDIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for DC and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutDCTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=DEBIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for NB and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutNBTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=NET_BANKING");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for UPI and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutUPITransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=UPI");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for DC and validate all txn data push over kafka")
    public void validateSucessfullJsOnRedirectionDCTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_UPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_ON_REDIRECTION");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=DEBIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for CC and validate all txn data push over kafka")
    public void validateSucessfullJsOnRedirectionCCTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_UPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_ON_REDIRECTION");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=CREDIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for UPI and validate all txn data push over kafka")
    public void validateSucessfullJsOnRedirectionUPITransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_UPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_ON_REDIRECTION");
        Assertions.assertThat(logs).contains("isAppInvoke=false");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=UPI");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
        Assertions.assertThat(logs).contains("orderStatus=TXN_SUCCESS");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for DC and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutAppInvokeDCTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=true");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=DEBIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for CC and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutAppInvokeCCTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=true");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=CREDIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for CC and validate all txn data push over kafka")
    public void validateSucessfullStandardCheckoutAppInvokeUPITransaction(@Optional("enhancedweb_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=STANDARD_CHECKOUT");
        Assertions.assertThat(logs).contains("isAppInvoke=true");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=UPI");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for DC and validate all txn data push over kafka")
    public void validateSucessfullJsOnRedirectionAppInvokeDCTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.PG2_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_ON_REDIRECTION");
        Assertions.assertThat(logs).contains("isAppInvoke=true");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=DEBIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Feature("PGP-48406")
    @Test(description = "Validate Successful transaction for CC and validate all txn data push over kafka")
    public void validateSucessfullJsOnRedirectionAppInvokeCCTransaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.PG2_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("checkoutFlow=JS_ON_REDIRECTION");
        Assertions.assertThat(logs).contains("isAppInvoke=true");
        Assertions.assertThat(logs).contains("CheckoutOrderAndErrorData");
        Assertions.assertThat(logs).contains("paymentMode=CREDIT_CARD");
        Assertions.assertThat(logs).contains("orderStatus=PROCESSING");
    }

}
