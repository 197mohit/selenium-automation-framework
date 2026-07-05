package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.Date;

@Owner("Tarun")
@Epic("Prepaid Card")
@Feature("PGP-20506")
public class PrepaidCard extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    MerchantType prepaidCardMerchant = MerchantType.MASKED_MOBILE_ENABLED;
    MerchantType prepaidCardMerchantPg2Rtdd = MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD;

    Double txnAmount = 2.00;

    @Test
    public void successTxnOfHigherAmountToIncreaseMPABalanceOfUser() {

        String txnAmount = "80000.00";//To increase Merchant's MPA balance so that merchant have balance to give back refund to user
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    private void assertPrepaidEnabledPref(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "PREPAID_CARD", "Y");
        }
    }


    private void assertPrepaidDisabledPref(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "PREPAID_CARD", "N");
        }
    }


    private void assertPrepaidPeonEnabledPref(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "PREPAID_CARD_PEON", "Y");
        }
    }


    private void assertPrepaidPeonDisabledPref(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "PREPAID_CARD_PEON", "N");
        }
    }

    public JsonPath isPrepaidCardSupportedFPO(String txnToken, InitTxnDTO initTxnDTO, String payMethod, boolean prepaidCardSupported) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.param("paymentMode", payMethod).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.paymentMode == paymentMode}.prepaidCardSupported"))
                .contains(prepaidCardSupported);


        return fetchPaymentOptionsJson;
    }


    //-------------------Test cases -------------------------------//
    //----------Positive cases -----------------//

//    @Parameters({"theme"})
//    @Test(description = "Enhanced : Verify a successful prepaid card  transaction with new card , verify txnStatus, peon,refund when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",enabled=false,groups = "P0")
    public void successPrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        assertPrepaidEnabledPref(prepaidCardMerchantPg2Rtdd);
        assertPrepaidPeonEnabledPref(prepaidCardMerchantPg2Rtdd);
        PGPHelpers.validateRefundAllowedWithChecksum(prepaidCardMerchantPg2Rtdd.getId());

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "Native : Verify a successful prepaid card native transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P0")
    public void successPrepaidNativeTxn(@Optional("false") Boolean isNativePlus) throws Exception {
      //  FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchantPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchantPg2Rtdd, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        isPrepaidCardSupportedFPO(txnToken, initTxnDTO, "DEBIT_CARD", true);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "App Invoke : Verify a successful prepaid card transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P0")
    public void appInvokeSuccessPrepaidTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //  FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchantPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(prepaidCardMerchantPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "AddNPay Enhanced : Verify a failed prepaid card transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant but default txn should work fine",groups = "P0")
    public void addPayFailedPrepaidTxnEnhancedButDefaultSuccess(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //  FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType addNPayMerchant = MerchantType.FOOD_MERCHANT_ADDNPAY;
        assertPrepaidEnabledPref(addNPayMerchant);
        assertPrepaidPeonEnabledPref(addNPayMerchant);
        String txnAmount = "5.00";
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchant, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount) - 1);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);

        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();

        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("VISA Prepaid card is not allowed for this payment. Please try paying using other cards/options.");

        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,addNPayMerchant);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(orderDTO);


    }

    //Failing for Native Plus
    @Issue("PGP-27301")
    @Parameters({"isNativePlus"})
    @Test(description = "AddNPay Native : Verify a successful prepaid card transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = Group.Status.BUG )
    public void addPaySuccessPrepaidTxnNative(@Optional("false") Boolean isNativePlus) throws Exception {
    //    FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        MerchantType addNPayMerchant = MerchantType.FOOD_MERCHANT_ADDNPAY;
        assertPrepaidEnabledPref(addNPayMerchant);
        assertPrepaidPeonEnabledPref(addNPayMerchant);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addNPayMerchant)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        JsonPath jsonPath =  isPrepaidCardSupportedFPO(txnToken,initTxnDTO,"DEBIT_CARD",true);
        Assertions.assertThat(jsonPath.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        OrderDTO orderDTO = new OrderFactory.Native(addNPayMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO,PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount.toString())
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validatePrepaidCard("true")
                .assertAll();

        InitTxnDTO nonAddPayInit = new InitTxnDTO.Builder(user.ssoToken(), addNPayMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        String nonAddPaytoken = NativeHelpers.Validate_InitTxn(nonAddPayInit);

        OrderDTO nonAddPay = new OrderFactory.Native(addNPayMerchant, nonAddPayInit.orderFromBody(), nonAddPaytoken, paymentDTO,PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount.toString())
                .setPaymentFlow("NONE")
                .build();

        checkoutPage.createNativeOrder(nonAddPay, isNativePlus);
        PrepaidHelpers.validateSuccessResponsePrepaidTxn(nonAddPay,addNPayMerchant);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(nonAddPay);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(nonAddPay);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(nonAddPay);

    }

    @Parameters({"theme"})
    @Test(description = "Saved Card : Verify a successful prepaid saved card (user id) transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P0")
    public void savedCardPrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
   //     FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        assertPrepaidEnabledPref(prepaidCardMerchantPg2Rtdd);
        assertPrepaidPeonEnabledPref(prepaidCardMerchantPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC_WITH_SAVECARD,paymentDTO);
        
        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);

        OrderDTO savedCard = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(savedCard);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD,paymentDTO);

        PrepaidHelpers.validateSuccessResponseSCPrepaidTxn(savedCard,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusSCPrepaidCard(savedCard);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(savedCard);
        PrepaidHelpers.validateSuccessRefundPrepaidCard(savedCard);

    }

    @Issue("PGP-27433")
    @Parameters({"theme"})
    @Test(description = "Offline : Verify a successful prepaid saved card (user id) transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = Group.Status.BUG)
    public void offlinePrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //  FF4JFlags.enable("prepaidCard");
        String DCPaymentDetails = "4766413897814514|882|052026";
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(prepaidCardMerchant, theme, user)
                .setPAYMENT_DETAILS(DCPaymentDetails)
                .setPAYMENT_TYPE_ID("DC")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchant);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        PrepaidHelpers.validateSuccessPeonPrepaidCard(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Retry : Verify a successful prepaid transaction after non prepaid txn gets failed when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P0")
    public void retryPrepaid(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //  FF4JFlags.enable("prepaidCard");
        MerchantType merchantType = MerchantType.PGOnly_Retry_PG2_RTDD;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD).setCreditCardNumber("4718650100030136");
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        assertPrepaidEnabledPref(merchantType);
        assertPrepaidPeonEnabledPref(merchantType);

        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.waitUntilLoads();

        if ("enhancedwap_revamp".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.ErrorRetryButton().click();
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO); //Prepaid Card

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,merchantType);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "PCF : Verify a successful prepaid card transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P0")
    public void pcfPrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) {
    //    FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType merchantType = MerchantType.PGOnly_Pcf;
        assertPrepaidEnabledPref(merchantType);
        assertPrepaidPeonEnabledPref(merchantType);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType.getId(),merchantType.getKey(), theme)
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.pause(1);
        PcfHelpher pcfHelpher = new PcfHelpher();
        double flatCommission = 1.12;
        pcfHelpher.validateCommision(cashierPage, orderDTO, 0.0, flatCommission);
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,merchantType);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);

        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY","CHARGEAMOUNT", "GATEWAYNAME", "RESPMSG", "BANKNAME","PAYMENTMODE", "CUSTID", "MID","prepaidCard", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(com.paytm.appconstants.Constants.Bank.HDFC.toString()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.prepaidCard().equals("true")
        );

        sAssert.eval();
    }


    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native Initiate txn -> FPO -> Fetch Bin -> Process txn: Verify a successful prepaid card native transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant")
    public void successPrepaidNativeTxnInitiateFPOFetchBinPTC(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
      //  FF4JFlags.enable("prepaidCard");
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchantPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        //FPO
        isPrepaidCardSupportedFPO(txnToken, initTxnDTO, "DEBIT_CARD", true);

        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchantPg2Rtdd, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //FETCHBIN
        PrepaidHelpers.Validate_BinDetail(txnToken,initTxnDTO,orderDTO,bin);

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);

    }

    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native Initiate txn -> Fetch Bin -> Process txn: Verify a successful prepaid card native transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant")
    public void successPrepaidNativeTxnFetchBinInitiateFetchBinProcess(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
     //   FF4JFlags.enable("prepaidCard");
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchantPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchantPg2Rtdd, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //FETCHBIN
        PrepaidHelpers.Validate_BinDetail(txnToken,initTxnDTO,orderDTO,bin);

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);

    }



    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native Initiate txn -> Process txn: Verify a successful prepaid card native transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant")
    public void successPrepaidNativeTxnFetchBinInitiateProcess(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
    //    FF4JFlags.enable("prepaidCard");
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchantPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchantPg2Rtdd, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,prepaidCardMerchantPg2Rtdd);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
    }


    ///////////////////--------Negative TC--------///////////////////


    //FF4J (Prepaid feature is ON)
    //Bin - IS_PREPAID - FALSE
    //PREPAID_CARD - true
    //PREPAID_CARD_PEON - true


    @Parameters({"theme"})
    @Test(description = "Enhanced : Verify a successful prepaid card  transaction with new card , verify txnStatus, peon,refund when ff4j flag is off and card is prepaid and merchant is prepaidCardMerchant")
    public void successPrepaidWithPeonFlagFalse(@Optional("enhancedweb_revamp") String theme) throws Exception {
       PaymentDTO paymentDTO = new PaymentDTO();
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        //Fetch Bin (isPrepaid)is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(false);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(false);

        // Both Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchantPg2Rtdd);
        assertPrepaidPeonEnabledPref(prepaidCardMerchantPg2Rtdd);
        PGPHelpers.validateRefundAllowedWithChecksum(prepaidCardMerchantPg2Rtdd.getId());

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchantPg2Rtdd.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    //FF4J (Prepaid feature is OFF)
    //Bin - IS_PREPAID - TRUE
    //PREPAID_CARD - true
    //PREPAID_CARD_PEON - true
 //   @Parameters({"theme"})
 //   @Test(enabled = false, priority = 1,description = "Enhanced : Verify a successful non prepaid card  transaction with new card , verify txnStatus, peon,refund when ff4j flag is off and card is prepaid and merchant is prepaidCardMerchant", groups = "P1")
    public void FF4jOffNormalTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //TODO Disabled this TC’s as we have prepaidCard ff4j flag permanently true on production (As confirmed with DEV)
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is off
//        FF4JFlags.disable("prepaidCard");

        //Bin is Prepaid
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        //Both Pref Enabled
        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonEnabledPref(prepaidCardMerchant);
        PGPHelpers.validateRefundAllowedWithChecksum(prepaidCardMerchant.getId());

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    //FF4J (Prepaid feature is OFF)
    //Bin - IS_PREPAID - FALSE
    //PREPAID_CARD - true
    //PREPAID_CARD_PEON - true
//    @Parameters({"theme"})
//    @Test(enabled = false, priority = 1,description = "Enhanced : Verify a successful non prepaid txn with bin and flag off")
    public void successPrepaidWithPeonFlagFalseBinFalse(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is off
    //    FF4JFlags.disable("prepaidCard");

        //Fetch Bin (isPrepaid )is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(false);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(false);

        // Both Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonEnabledPref(prepaidCardMerchant);
        PGPHelpers.validateRefundAllowedWithChecksum(prepaidCardMerchant.getId());

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    //FF4J (Prepaid feature is ON)
    //Bin - IS_PREPAID - FALSE
    //PREPAID_CARD - true
    //PREPAID_CARD_PEON - false
    @Parameters({"theme"})
    @Test(description = "Enhanced : Verify a successful prepaid card  transaction with new card , verify txnstatus,responePage validations when prepaid_card_peon preference is N")
    public void peonPrefDisabledPrepaidCard(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType prepaidCardMerchant = MerchantType.PGOnly_PG2_RTDD;
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
       // FF4JFlags.enable("prepaidCard");

        //Fetch Bin (isPrepaid) is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        // One Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonDisabledPref(prepaidCardMerchant);

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Parameters({"theme"})
    @Test(description = "EMI CC : Verify a successful prepaid saved card (user id) transaction with new card when ff4j flag is on and card is not prepaid and merchant is prepaidCardMerchant",groups = "P1")
    public void emiCCPrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
    //    FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String bin = paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER.substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(false);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(false);

        assertPrepaidEnabledPref(prepaidCardMerchantPg2Rtdd);
        assertPrepaidPeonEnabledPref(prepaidCardMerchantPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchantPg2Rtdd.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Parameters({"theme"})
    @Test(description = "EMI DC : Verify a successful prepaid saved card (user id) transaction with new card when ff4j flag is on and card is prepaid and merchant is prepaidCardMerchant",groups = "P1")
    public void emiDCPrepaidEnhancedTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
    //    FF4JFlags.enable("prepaidCard");
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD).setMonth(4);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);
        assertPrepaidEnabledPref(prepaidCardMerchantPg2Rtdd);
        assertPrepaidPeonEnabledPref(prepaidCardMerchantPg2Rtdd);
        User user = userManager.getForWrite(Label.EMIDC);
    //    SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber()).setBankName("ICICI Bank Debit Card").setMonth(4);
        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidCardMerchantPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText(paymentDTO.getBankName());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.PREPAID_CARD);
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("VISA Prepaid card is not allowed for EMI payment. Please try paying using other cards/options.");

    }

    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Initiate txn -> FPO with txn token -> Fetch Bin -> Process txn Verify a successful prepaid card  transaction with new card , verify txnstatus,responePage validations when prepaid_card_peon preference is N")
    public void peonPrefDisabledPrepaidCardNativeInitiateFPOFetchBinPTC(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType prepaidCardMerchant = MerchantType.PGOnly_PG2_RTDD;
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        //Fetch Bin (isPrepaid) is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        // One Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonDisabledPref(prepaidCardMerchant);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPrepaidCardSupportedFPO(txnToken, initTxnDTO, "DEBIT_CARD", true);

        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //FETCHBIN
        PrepaidHelpers.Validate_BinDetail(txnToken,initTxnDTO,orderDTO,bin);


        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Initiate txn -> Fetch Bin -> Process txn Verify a successful prepaid card  transaction with new card , verify txnstatus,responePage validations when prepaid_card_peon preference is N")
    public void peonPrefDisabledPrepaidCardNativeFetchBinPTC(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType prepaidCardMerchant = MerchantType.PGOnly_PG2_RTDD;
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        //Fetch Bin (isPrepaid) is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        // One Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonDisabledPref(prepaidCardMerchant);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //FETCHBIN
        PrepaidHelpers.Validate_BinDetail(txnToken,initTxnDTO,orderDTO,bin);


        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Owner("Gagandeep")
    @Feature("PGP-24245")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Initiate txn -> Process txn Verify a successful prepaid card  transaction with new card , verify txnstatus,responePage validations when prepaid_card_peon preference is N")
    public void peonPrefDisabledPrepaidCardNativeInitiatePTC(@Optional("true") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        MerchantType prepaidCardMerchant = MerchantType.PGOnly_PG2_RTDD;
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        //Fetch Bin (isPrepaid) is false

        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        // One Pref Enabled

        assertPrepaidEnabledPref(prepaidCardMerchant);
        assertPrepaidPeonDisabledPref(prepaidCardMerchant);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, prepaidCardMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        OrderDTO orderDTO = new OrderFactory.Native(prepaidCardMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(prepaidCardMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(com.paytm.appconstants.Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }


    //Prepaid Corporate Card

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"isNativePlus"})
    @Test(description = "To test successful prepaid corporate DC txn for Corporate Prepaid Merchant")
    public void prepaidCorporateCard(@Optional("false") Boolean isNativePlus) {

        MerchantType corporateCardPrepaid = MerchantType.CORPORATE_CARD_PREPAID_PG2_RTDD;
        assertPrepaidEnabledPref(corporateCardPrepaid);
        assertPrepaidPeonEnabledPref(corporateCardPrepaid);
        CorporateHelpers.assertCorporateCardDC(corporateCardPrepaid.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        // isPrepaid & isCorporate should be true

        JsonPath binResponse = PrepaidHelpers.cardBinQuery(bin).jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("true");


        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateCardPrepaid)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardPrepaid, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO,corporateCardPrepaid);

        TxnStatus txnStatus = PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        JsonPath txnStatusResponse = txnStatus.getApiResponse().jsonPath();

        Assertions.assertThat(txnStatusResponse.getString("FEERATEFACTORS.PREPAIDCARD")).isEqualTo("TRUE");
        Assertions.assertThat(txnStatusResponse.getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");

        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","feeRateFactors","PAYMENTMODE", "CUSTID", "MID","prepaidCard", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().contains(com.paytm.appconstants.Constants.Bank.HDFC.toString()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.prepaidCard().equals("true"),
                peon.feeRateFactors().equals("{\"corporateCard\":\"TRUE\",\"prepaidCard\":\"TRUE\"}"),
                peon.isChecksumValid());

        sAssert.eval();


    }


    //Prepaid Corporate International Card

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : To test successful prepaid corporate international DC txn for Corporate Prepaid International Merchant")
    public void prepaidCorporateInternationalCard(@Optional("false") Boolean isNativePlus) {

        MerchantType corporateCardPrepaidInternational = MerchantType.ALLPAYMODE;
        assertPrepaidEnabledPref(corporateCardPrepaidInternational);
        assertPrepaidPeonEnabledPref(corporateCardPrepaidInternational);
        CorporateHelpers.assertCorporateCardDC(corporateCardPrepaidInternational.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        //FF4j Flag is on
    //    FF4JFlags.enable("prepaidCard");

        // isPrepaid & isCorporate should be true

        JsonPath binResponse = PrepaidHelpers.cardBinQuery(bin).jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).isEqualTo("false");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateCardPrepaidInternational)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardPrepaidInternational, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.IHDF.toString())
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateCheckSum(corporateCardPrepaidInternational.getKey())
                .validatePrepaidCard("true")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.IHDF.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validatePrepaidCard("TRUE")
                .AssertAll();

        JsonPath txnStatusResponse = txnStatus.getApiResponse().jsonPath();
        Assertions.assertThat(txnStatusResponse.getString("FEERATEFACTORS.PREPAIDCARD")).isEqualTo("TRUE");
        Assertions.assertThat(txnStatusResponse.getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");
        Assertions.assertThat(txnStatusResponse.getString("FEERATEFACTORS.INTERNATIONALCARDPAYMENT")).isEqualTo("TRUE");

        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","feeRateFactors","PAYMENTMODE", "CUSTID", "MID","prepaidCard", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(com.paytm.appconstants.Constants.Bank.HDFC.toString()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("IHDF"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.prepaidCard().equals("true"),
                peon.feeRateFactors().equals("{\"internationalCardPayment\":\"TRUE\",\"corporateCard\":\"TRUE\",\"prepaidCard\":\"TRUE\"}"),
                peon.isChecksumValid());

        sAssert.eval();


    }




}
