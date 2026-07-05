package com.paytm.apphelpers;

import com.paytm.RefundSucessNotifyPeon;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.MappingService.GetMerchantPreferenceInfoExt;
import com.paytm.api.SMSPrimary;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import java.util.Date;

public class PWPHelpers {

    public static void getPWPEnabledPayMode(String preference,String mid,String... payMode)
    {
        GetMerchantPreferenceInfoExt getMerchantPreferenceInfoExt =
                new GetMerchantPreferenceInfoExt(mid, preference);
        Response response = getMerchantPreferenceInfoExt.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("resultResp.merchantPreferenceInfos[0].prefValue")).contains(payMode);

    }


    public static void validateSuccessResponsePWPTxn(OrderDTO orderDTO, Constants.MerchantType merchantType)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateCheckSum(merchantType.getKey()).assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }

    public static void validateFailureResponsePWPTxn(OrderDTO orderDTO, Constants.MerchantType merchantType)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(Constants.Gateway.PAYTM.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateCheckSum(merchantType.getKey()).assertAll();
    }

    public static TxnStatus validateSuccessTxnStatusPWPTxn(OrderDTO orderDTO)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(Constants.Gateway.PAYTM.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateFailureTxnStatusPWPTxn(OrderDTO orderDTO)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(Constants.Gateway.PAYTM.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatusPWPTxn(OrderDTO orderDTO)
    {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(Constants.Gateway.PAYTM.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        return txnStatus;

    }

    public static TxnStatus validateFailureNativeTxnStatusPWPTxn(OrderDTO orderDTO)
    {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(Constants.Gateway.PAYTM.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        return txnStatus;

    }

    public static JsonPath validateSuccessPaymentStatusAPIPWPTxn(OrderDTO orderDTO, Constants.MerchantType merchantType)
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

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.bankName")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());

        return  getPaymentStatus;
    }

    public static JsonPath validateFailurePaymentStatusAPIPWPTxn(OrderDTO orderDTO, Constants.MerchantType merchantType)
    {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.bankName")).isEqualToIgnoringCase(Constants.Gateway.PAYTM.toString());

        return  getPaymentStatus;
    }


    public static void validateSuccessPeonPWPTxn(OrderDTO orderDTO)
    {
         Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(Constants.Gateway.PAYTM.toString()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(Constants.Gateway.PAYTM.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(Constants.Gateway.PAYTM.toString()),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    //Failure Response Code and Response Message changing
    public static void validateFailurePeonPWPTxn(OrderDTO orderDTO)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("PAYTM"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(Constants.Gateway.PAYTM.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(Constants.Gateway.PAYTM.toString()),
                peon.respCode().equals("501"),
                peon.respMsg().equals("System Error"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }


    public static void validateSuccessRefundPWPTxn(OrderDTO orderDTO)
    {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String postConvFlag="";
        PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), orderDTO.getTxnId(), postConvFlag);

        try {
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMID(orderDTO.getMID(), 0)
                    .validatePAYMENTMODE(Constants.Gateway.PAYTM.toString(), 0)
                    .validateGATEWAY(Constants.Gateway.PAYTM.toString(), 0)
                    .validateOrderId(orderDTO.getORDER_ID(), 0)
                    .validateREFUNDTYPE("MERC_TO_BANK", 0)
                    .assertAll();
        }
        catch (NullPointerException e)
        {
            Reporter.report.error("Refund is in pending state, orderId: " + orderDTO.getORDER_ID());
            throw new RuntimeException("Refund is in pending state, orderId: " + orderDTO.getORDER_ID());
        }

    }

    public static void validateSuccessRefundNotifyPeonPWPTxn(OrderDTO orderDTO, Constants.MerchantType merchantType)
    {
        RefundSucessNotifyPeon refundSucessNotifyPeon = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(),orderDTO.getMID());
        refundSucessNotifyPeon.validateBasicDetails(merchantType,orderDTO);
    }

    public static void validateSuccessSMSViaPaytm(OrderDTO orderDTO)
    {
        SMSPrimary smsPrimary = new SMSPrimary(orderDTO.getORDER_ID());
        String smsToMerchant = smsPrimary.execute().jsonPath().getString("message");
        Assertions.assertThat(smsToMerchant).as("Success SMS message is incorrect").contains("via Paytm").contains(orderDTO.getORDER_ID().substring(5));

    }

    public static void validateFailureSMSViaPaytm(OrderDTO orderDTO)
    {
        SMSPrimary smsPrimary = new SMSPrimary(orderDTO.getORDER_ID());
        String smsToMerchant = smsPrimary.execute().jsonPath().getString("message");
        Assertions.assertThat(smsToMerchant).as("Failure SMS message is incorrect").contains("has failed").contains(orderDTO.getORDER_ID().substring(5));

    }

}

