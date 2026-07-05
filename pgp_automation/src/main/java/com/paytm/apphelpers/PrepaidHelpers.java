package com.paytm.apphelpers;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
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

public class PrepaidHelpers{

    //to be removed
    public static boolean isBinPrepaidOnAlipay(String bin)
    {
        BinQueryApi api = new BinQueryApi(bin);
        Response response = api.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("cardBinInfo.cardBin")).isEqualTo(bin);
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");
        return Boolean.valueOf(response.jsonPath().getString("cardBinInfo.binConfigAttributes.PREPAID_CARD"));

    }

    public static Response cardBinQuery(String bin)
    {
        BinQueryApi api = new BinQueryApi(bin);
        Response response = api.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("cardBinInfo.cardBin")).isEqualTo(bin);
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");
        return response;
    }

    public static boolean isBinPrepaidOnPaytm(String bin) throws Exception {
        return Boolean.valueOf(PGPHelpers.getBinDetails(bin).getPrepaidCard());
    }

    //Specific to HDFC Prepaid Card as we have HDFC  mock

    public static void validateSuccessResponsePrepaidTxn(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType)
    {
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
                .validateCheckSum(merchantType.getKey())
                .validatePrepaidCard("true")
                .assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }

    //Specific to HDFC Prepaid Card as we have HDFC  mock

    public static void validateSuccessResponseSCPrepaidTxn(OrderDTO orderDTO, com.paytm.appconstants.Constants.MerchantType merchantType)
    {
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
                .validateBankName(com.paytm.appconstants.Constants.Bank.HDFCSC.toString())
                .validateCheckSum(merchantType.getKey())
                .validatePrepaidCard("true")
                .assertAll();
        orderDTO.setMerchantKey(merchantType.getKey());
    }

    public static TxnStatus validateSuccessTxnStatusPrepaidCard(OrderDTO orderDTO)
    {
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
                .validatePrepaidCard("TRUE")
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateSuccessTxnStatusSCPrepaidCard(OrderDTO orderDTO)
    {
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
                .validateBankName(Constants.Bank.HDFCSC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validatePrepaidCard("TRUE")
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static void validateSuccessPeonPrepaidCard(OrderDTO orderDTO)
    {
         Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().contains("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","PAYMENTMODE", "CUSTID", "MID","prepaidCard", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().contains("HDFC"),
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
                peon.isChecksumValid());

        sAssert.eval();
    }


    public static void validateSuccessRefundPrepaidCard(OrderDTO orderDTO) {
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), orderDTO.getTxnId(), "");
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMID(orderDTO.getMID(), 0)
                    .validatePAYMENTMODE("DC", 0)
                    .validateGATEWAY("HDFC", 0)
                    .validateOrderId(orderDTO.getORDER_ID(), 0)
                    .validateREFUNDTYPE("MERC_TO_BANK", 0)
                    .assertAll();
        }

    }



    public static void Validate_BinDetail(String txnToken, InitTxnDTO initTxnDTO, OrderDTO orderDTO, String binNum) {
        Reporter.report.info("Validating binDetails API  with txn token" + orderDTO.getBANK_CODE());
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNum).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }









}

