package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.PayMethodType;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Tarun")
@Feature("PGP-19760")
public class HDFCUPICollect extends PGPBaseTest {

    //For enhancedwap theme, enhancedweb theme is getting passed TODO
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String postConvFlag = "";
    private final Constants.MerchantType hdfcUPICollect = Constants.MerchantType.HDFC_UPI_COLLECT;
    private final Constants.MerchantType hdfcUPICollect_PG2_RTDD = Constants.MerchantType.HDFC_UPI_COLLECT_PG2_RTDD;

    //As per PGP-24185, ff4j dependency has been removed
    @BeforeClass
    public void enableReturnUserVPAIn() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID='RETURN_USER_VPA_IN_RESPONSE'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_RETURN_USER_VPA_IN_RESPONSE");

    }



    private void validateVPAInResponse(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "RETURN_USER_VPA_IN_RESPONSE", "Y");
        }
    }

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    @Parameters({"theme"})
    @Test(description = "To test successful PGOnly HDFC UPI collect txn when RETURN_USER_VPA_IN_RESPONSE = Y " +
                            "for enhanced flows and validate success peon for ONUS merchant")
    public void validateSuccessfulHDFCUPITxn(@Optional("enhancedweb")String theme)
    {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateVPAInResponse(hdfcUPICollect_PG2_RTDD);
        OrderDTO orderDTO = new OrderFactory.PGOnly(hdfcUPICollect_PG2_RTDD, theme)
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateVPA(new PaymentDTO().getVpa())
                .validateCheckSum(hdfcUPICollect_PG2_RTDD.getKey())
                .assertAll();

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
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateVPA(new PaymentDTO().getVpa())
                .validateStatusAPIParameters()
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","VPA","PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(""),
                peon.vpa().equals(new PaymentDTO().getVpa()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.vpa().equals(paymentDTO.getVpa()),
                peon.isChecksumValid());

        sAssert.eval();

    }

    @Parameters({"theme"})
    @Test(description = "To test failure PGOnly HDFC UPI collect txn when RETURN_USER_VPA_IN_RESPONSE = Y " +
                            "for enhanced flows and validate success peon ONUS merchant")
    public void validateFailureHDFCUPITxn(@Optional("enhancedweb") String theme)
    {
        PaymentDTO paymentDTO  = new PaymentDTO();
        validateVPAInResponse(hdfcUPICollect_PG2_RTDD);
        OrderDTO orderDTO = new OrderFactory.PGOnly(hdfcUPICollect_PG2_RTDD, theme)
                .setTXN_AMOUNT("99.41")
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
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateVPA(new PaymentDTO().getVpa())
                .validateCheckSum(hdfcUPICollect_PG2_RTDD.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateVPA(new PaymentDTO().getVpa())
                .validateStatusAPIParameters()
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE","VPA", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.vpa().equals(new PaymentDTO().getVpa()),
                peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
                peon.bankName().equals(""),
                peon.payMode().equals("UPI"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("227"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.vpa().equals(paymentDTO.getVpa()),
                peon.isChecksumValid());
        sAssert.eval();
    }

    @Parameters({"theme"})
    @Test(description = "To test successful refund for PGOnly HDFC UPI collect txn for enhanced flows")
    public void refundHDFCUPI(@Optional("enhancedweb") String theme)
    {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(hdfcUPICollect.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(hdfcUPICollect, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateGatewayName("HDFC")
                .validatePaymentMode("UPI")
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            cashierPage.pause(45);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMID(hdfcUPICollect.getId(),0)
                    .validateREFUNDAMOUNT(orderDTO.getTXN_AMOUNT(),0)
                    .validateTOTALREFUNDAMT(orderDTO.getTXN_AMOUNT(),0)
                    .validatePAYMENTMODE("UPI",0)
                   .validateGATEWAY("HDFC",0)
                    .validateOrderId(orderDTO.getORDER_ID(),0)
                    .validateREFUNDTYPE("MERC_TO_BANK",0)
                    .assertAll();
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund for PGOnly HDFCUPI Collect")
    public void successfulPartialRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(hdfcUPICollect_PG2_RTDD.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(hdfcUPICollect_PG2_RTDD, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validatePaymentMode("UPI")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateGatewayName("HDFC")
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "1.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            cashierPage.pause(45);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateTOTALREFUNDAMT("1.00", 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .validateGATEWAY("HDFC",0)
                    .validatePAYMENTMODE("UPI",0)
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT(),0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To test successful hybrid HDFC UPI collect txn for enhanced flow when RETURN_USER_VPA_IN_RESPONSE = Y")
    public void validateSuccesfulHybridHDFCUPITxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(hdfcUPICollect_PG2_RTDD, theme, user)
                .build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

    }

    @Parameters({"theme"})
    @Test(description = "To validate txn failing by CC should get passed by HDFC UPI Collect if retry is configured on merchant")
    public void validateRetryTxnForHDFCUPI(@Optional("enhancedweb") String theme)
    {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFC_UPI_COLLECT_RETRY_PG2_RTDD, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber("4718650100030136");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.UPI);
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
            //    .validateBankName(Constants.Bank.HDFC.toString()) Bankname does not comes in Txn Status for UPI only Txn.
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateVPA(paymentDTO.getVpa())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//---------------------Native Txn ------------------

    @Parameters({"isNativePlus"})
    @Test(description = "To verify normal success UPI transaction using native/native+ flow HDFC UPI when " +
            "RETURN_USER_VPA_IN_RESPONSE = Y for ONUS Merchant")
    public void verifyNormalUPINative(@Optional("true") Boolean isNativePlus)
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hdfcUPICollect_PG2_RTDD).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hdfcUPICollect_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateVPA("test@paytm")
                .validateCheckSum(hdfcUPICollect_PG2_RTDD.getKey())
                .assertAll();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateVPA("test@paytm")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
           Peon peon = peons.getAt(orderDTO.getORDER_ID());
                SoftAssertion sAssert = new SoftAssertion();
                sAssert.apply(peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","VPA","PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                        peon.currency().equals("INR"),
                        peon.custId().equals("").not(),
                        peon.gatewayName().equals("HDFC"),
                        peon.mercUnqRef().equals(""),
                        peon.mId().equals(orderDTO.getMID()),
                        peon.orderId().equals(orderDTO.getORDER_ID()),
                        peon.payMode().equals("UPI"),
                        peon.respCode().equals("01"),
                        peon.status().equals("TXN_SUCCESS"),
                        peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                        peon.txnDate().equals("").not(),
                        peon.txnDateTime().equals("").not(),
                        peon.txnId().equals("").not(),
                        peon.vpa().equals("test@paytm"),
                        peon.checksumHash().equals("").not()
                );
                sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify normal failure UPI transaction using native/native+ flow HDFC UPI" +
            "RETURN_USER_VPA_IN_RESPONSE = Y for ONUS Merchant")
    public void verifyNormalFailureUPINative(@Optional("true") Boolean isNativePlus)
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hdfcUPICollect_PG2_RTDD)
                .setTxnValue("99.41")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hdfcUPICollect_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setTXN_AMOUNT("99.41")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("HDFC")
                .validateVPA("test@paytm")
                .validateCheckSum(hdfcUPICollect_PG2_RTDD.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateVPA("test@paytm")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify hybrid UPI success transaction using native/native+ flow HDFC UPI when when RETURN_USER_VPA_IN_RESPONSE = Y")
    public void verifyUPINativeHybrid(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        double amountToBeRetainedInWallet = Double.valueOf(txnAmount) - 1.00;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), hdfcUPICollect_PG2_RTDD)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("HYBRID");

        OrderDTO orderDTO = new OrderFactory.Native(hdfcUPICollect_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount(new PaymentDTO().getVpa())
                .setPaymentFlow("HYBRID")
                .setTXN_AMOUNT(txnAmount)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

    }
}