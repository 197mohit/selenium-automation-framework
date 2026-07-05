package com.paytm.apphelpers;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import java.util.Date;

public class CorporateHelpers {

    public static boolean isBinCorporate(String bin)
    {
        String request = "{\"bin\":\""+bin+"\", \"fetchBlocked\":\"false\"}";
        BinQueryApi api = new BinQueryApi(bin);
        Response response = api.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("cardBinInfo.cardBin")).isEqualTo(bin);
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");
        return Boolean.valueOf(response.jsonPath().getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD"));

    }


    public static void assertCorporateCardCC(String mid)
    {
        String preference = "CORPORATE_CARD_CC";
        PGPHelpers.validate_MerchantPreference(mid, preference, "Y");
    }

    public static void assertCorporateCardDC(String mid)
    {
        String preference = "CORPORATE_CARD_DC";
        PGPHelpers.validate_MerchantPreference(mid, preference, "Y");
    }

    public static void validateSuccessResponse(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType, String payMode, String bankName)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateBankName(bankName)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }


    public static void validateFailureResponse(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType, String payMode, String bankName)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankName(bankName)
                .validateCheckSum(merchantType.getKey()).assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }

    public static void validatePendingResponse(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType, String payMode, String bankName)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankName(bankName)
                .validateCheckSum(merchantType.getKey()).assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }


    public static TxnStatus validateSuccessTxnStatusCorporate(OrderDTO orderDTO, String payMode, String bankName)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateBankName(bankName)
                .validatePaymentMode(payMode)
                .AssertAll();

        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        //Corporate Card
        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatus(OrderDTO orderDTO, String payMode, String bankName)
    {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(bankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        //Corporate Card
        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");
        return txnStatus;

    }


    public static TxnStatus validateFailureTxnStatusCorporate(OrderDTO orderDTO, String payMode, String bankName)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(bankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());

        //Corporate Card
        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");
        return txnStatus;

    }


    public static TxnStatus validateSuccessTxnStatusNonCorporate(OrderDTO orderDTO, String payMode, String bankName)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateBankName(bankName)
                .validatePaymentMode(payMode)
                .AssertAll();

        //Corporate Card
        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("FEERATEFACTORS.CORPORATECARD")).isEqualTo(null);
        return txnStatus;

    }


    public static JsonPath validateSuccessPaymentStatusAPI(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType, String payMode, String bankName)
    {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(payMode);
        Assertions.assertThat(getPaymentStatus.getString("body.bankName")).contains(bankName);
        Assertions.assertThat(getPaymentStatus.getString("body.feeRateFactors.corporateCard")).isEqualToIgnoringCase("TRUE");

        return  getPaymentStatus;
    }

    public static void validateSuccessPeonCorporate(OrderDTO orderDTO, String payMode, String bankName, String gateway)
    {
         Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());

        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().contains("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","PAYMENTMODE", "CUSTID", "MID","feeRateFactors", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().contains(bankName),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gateway),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.feeRateFactors().equals("{\"corporateCard\":\"TRUE\"}"),
                peon.isChecksumValid());

        sAssert.eval();
    }

    public static void validateSuccessPeonNonCorporate(OrderDTO orderDTO, String payMode, String bankName, String gateway)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().contains(bankName),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gateway),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid());

        sAssert.eval();
    }

    public static void validateFailurePeonCorporate(OrderDTO orderDTO, String payMode, String bankName, String gateway)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().contains(bankName),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gateway),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("227"),
                peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }



}

