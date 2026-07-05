package scripts.Native;

import com.paytm.ServerConfigProvider;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.SolidityCheck.ValidateMerchantConfig;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;
import static com.paytm.appconstants.Constants.Owner.TARUN;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.dto.PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER;

@Owner("Deepak")
public class FetchPaymentPage extends PGPBaseTest {

    private static final String BAJAJ_FINSERV_DROPDOWN_BANK_NAME = "Bajaj Finserv Ltd.";
    private static final String VALID_OTP = "123456";
    private static final String APP_INVOKE_PHASE2 = FF4JFeatures.APP_INVOKE_PHASE2;
    private static final String APP_INVOKE_PHASE3 = FF4JFeatures.APP_INVOKE_PHASE3;

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final ProcessTransactionTests processTransactionTests = new ProcessTransactionTests();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    private final BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();

    @Parameters({"theme"})
    @Test(description="CC App Invoke Transaction")
    public void FPP_CCAppInvokeTC001(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.HDFC.toString(),
                Constants.Bank.HDFC.toString(),"CC");
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description="PPI App Invoke transaction")
    public void FPP_PPIAppInvokeTC002(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WalletOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.WalletOnly,initTxnDTO.getBody().getOrderId(),txnToken).build();
        processTransactionTests.Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,"WALLET","WALLET","PPI");
        WalletHelpers.validateBalance(user,0.0);
    }

    @Parameters({"theme"})
    @Test(description="Saved Card App Invoke Transaction")
    public void FPP_SavedCCAppInvokeTC003(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly,initTxnDTO.getBody().getOrderId(),txnToken).build();
        processTransactionTests.Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        processTransactionTests.Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.HDFC.toString(),
                Constants.Bank.HDFCSC.toString(),"CC");
    }

    @Parameters({"theme"})
    @Test(description="HDFO App Invoke Transaction")
    public void FPP_HDFOAppInvokeTC004(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HDFO).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.NATIVE_HDFO,initTxnDTO.getBody().getOrderId(),txnToken).build();
        processTransactionTests.Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        processTransactionTests.Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.HDFO.toString(),
                Constants.Bank.HDFC.toString(),"CC");
    }

    @Issue("PGP-13865")
    @Parameters({"theme"})
    @Test(description="EMI transaction with BAJAJ FN App Invoke")
    public void FPP_BajajFinAppInvokeTC005(@Optional("enhancedwap") String theme) throws Exception{
        {
            PGPHelpers.validateEmiAvail(Constants.MerchantType.Hybrid.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth(null)
                .setExpYear(null)
                .setCvvNumber(null)
                .setMonth(6);
        //SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.Hybrid,
                initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.BAJAJFN.toString(),
                Constants.Gateway.BAJAJFN.toString(),"EMI");
    }

    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate risk extended info URL parameters under theia facade logs for appInvoke")
    public void FPP_validateRiskExtendedInfoUrlParams(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
            String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                    "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).contains("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).contains("callbackURL");
    }



    @Feature("PGP-29616")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate additional param UDF in app Invoke Flow")
    public void FPP_validateAdditionalParamUDFInAppInvokeFlow(@Optional("enhancedweb") String theme) throws Exception {
        String UDF_2 = "default_udf2";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setExtendInfo(new ExtendInfo().setUdf2(UDF_2))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
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
                .validateUDF(UDF_2)
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .assertAll();
    }


    @Feature("PGP-29575")
    @Owner(TARUN)
    @Parameters({"theme"})
    @Test(description = "Validate NB Success txn with PCF flow when app is not invoked & user is not logged in")
    public void appInvokePCFNBNotLoggedIn(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.FLAT_PCF;
    //    Removing Flag controls from TCs -> Making it Mid Based from FF4j Panel
    //    FF4JFlags.enableMidBased(APP_INVOKE_PHASE2,merchantType.getId());
    //    FF4JFlags.enableMidBased(APP_INVOKE_PHASE3,merchantType.getId());

        PGPHelpers.validate_MerchantPreference(merchantType.getId(),"AUTO_APP_INVOKE_ALLOWED","Y");
        PGPHelpers.validate_MerchantPreference(merchantType.getId(),"appInvokeAllowed","Y");
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NB","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateChargeAmount(format.format(chargeAmount))
                .assertAll();

    }

    @Feature("PGP-29575")
    @Owner(TARUN)
    @Parameters({"theme"})
    @Test(description = "Validate NB Success txn with PCF flow when app is not invoked & user is logged in")
    public void appInvokePCFNBLoggedIn(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.FLAT_PCF;
    //    Removing Flag controls from TCs -> Making it Mid Based from FF4j Panel
    //    FF4JFlags.enableMidBased(APP_INVOKE_PHASE2,merchantType.getId());
    //    FF4JFlags.enableMidBased(APP_INVOKE_PHASE3,merchantType.getId());
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        PGPHelpers.validate_MerchantPreference(merchantType.getId(),"AUTO_APP_INVOKE_ALLOWED","Y");
        PGPHelpers.validate_MerchantPreference(merchantType.getId(),"appInvokeAllowed","Y");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NB","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");


        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateChargeAmount(format.format(chargeAmount))
                .validateRespMsg("Txn Success")
                .assertAll();

    }



    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description="App Invoke retry on failure Transaction for NB TO success CC")
    public void FPP_validateSuccessRetryAppInvokeNBTransaction(@Optional("enhancedweb") String theme) throws Exception{

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Retry)
                .setOrderId(CommonHelpers.generateOrderId() + "RETRY").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly_Retry,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.HDFC.toString(),
                Constants.Bank.HDFC.toString(),"CC");
    }


    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description="App Invoke retry on failure Transaction for NB for merchant without retry")
    public void FPP_validateFailureRetryNBTransaction(@Optional("enhancedweb") String theme) throws Exception{

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setOrderId(CommonHelpers.generateOrderId() + "RETRY").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnDate(new Date())
                .assertAll();

    }

    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description="App Invoke retry on failure Transaction for NB when Retry Exhausted")
    public void FPP_validateFailureRetryNBWhenRetryExhaust(@Optional("enhancedweb") String theme) throws Exception{

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_Retry)
                .setTxnValue("99.99").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        ValidateMerchantConfig validateMerchantConfig = new ValidateMerchantConfig();
        validateMerchantConfig.fetchMerchantFromAlipay(Constants.MerchantType.PGOnly_Retry, 2);
        int retryCount = Integer.parseInt(validateMerchantConfig.merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly_Retry,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        for(int i=0 ; i <=retryCount; i++) {
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
            cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnDate(new Date())
                .assertAll();
    }


    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description="App Invoke retry on failure Transaction for HYBRID Merchant")
    public void FPP_validateSuccessRetryNBForHybridTransaction(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("2")
                .setOrderId(CommonHelpers.generateOrderId() + "RETRY").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        ValidateMerchantConfig validateMerchantConfig = new ValidateMerchantConfig();
        validateMerchantConfig.fetchMerchantFromAlipay(Constants.MerchantType.Hybrid_Retry, 2);
        int retryCount = Integer.parseInt(validateMerchantConfig.merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.Hybrid_Retry,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .assertAll();
    }




    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description="App Invoke retry on Successful Transaction for NB for ADDNPAY Merchant")
    public void FPP_validateSuccessRetryNBForADDNPAYTransaction(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay_Retry)
                .setTxnValue("2")
                .setOrderId(CommonHelpers.generateOrderId() + "RETRY").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        ValidateMerchantConfig validateMerchantConfig = new ValidateMerchantConfig();
        validateMerchantConfig.fetchMerchantFromAlipay(Constants.MerchantType.AddnPay_Retry, 2);
        int retryCount = Integer.parseInt(validateMerchantConfig.merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AddnPay_Retry,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-27607")
    @Parameters({"theme"})
    @Test(description = "Verify isAddNPay flag for AppInvoke AddNPay through UPI")
    public void VerifyisAddNPayFlagForAppInvokeAddNPayThroughUPI(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_PPI;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.SUBSCRIPTION_PPI,initTxnDTO.orderFromBody(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .assertAll();
        String tsnId = responsePage.textTxnID().getText();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        String grepcmd = "grep \"" + tsnId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"extendInfo\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        String [] ar = theiaFacadeLogs.split("passThroughExtendInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("\"isAddNPay\":\"true\"");
    }


}

