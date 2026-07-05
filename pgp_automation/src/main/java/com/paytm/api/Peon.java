package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.asserts.SoftAssert;

import java.text.DecimalFormat;

public class Peon extends BaseApi {
    private SoftAssertions softly = new SoftAssertions();
    private Response response;



    public Peon(String orderId) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath("/mockbank/peon");
        getRequestSpecBuilder().addParam("orderId", orderId);
    }

    public void executeUntilGetResponse(int timeOutInSeconds, int pollingTimeInSeconds){
        Response response = null;
        int pollCount=0;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeOutInSeconds * 1000) {
            pollCount++;
            response = this.execute();
            this.response = response;
            if (response.getStatusCode() == 200) {
                break;
            }
            try {
                if(pollCount>2){
                    Thread.sleep(10 * 1000);

                }else {
                    Thread.sleep(pollingTimeInSeconds * 1000);
                }
            } catch (InterruptedException e) {
            }
        }
        Assertions.assertThat(response.getStatusCode()).as("Status Code").isEqualTo(200);
    }

    public void executeUntilGetResponse(){
      this.executeUntilGetResponse(190,30);
    }

    public void executeToGetNoResponse() {
        Response response = null;
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response = this.execute();
            this.response = response;

        Assertions.assertThat(response.getStatusCode()).as("Status Code").isEqualTo(204);

    }

    public Response response() {
        return this.response;
    }

    public PeonResponse getPeonData(String orderId){
        Response response = this.execute();
        PeonResponse peonResponse = new PeonResponse();
        peonResponse = response.as(PeonResponse.class);
        return peonResponse;
    }

    public void validatePeon_CC(OrderDTO orderDTO, String bankName, String gateway, String status, String txnAmount) {
       // OrderDTO orderDTO = new OrderDTO();
        String orderId = orderDTO.getORDER_ID();
        SoftAssertions softAssert = new SoftAssertions();
        Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertThat(peonResponse.getBANKNAME().equalsIgnoreCase(bankName)).withFailMessage("Incorrect Bank Name");
        softAssert.assertThat(peonResponse.getGATEWAYNAME().equalsIgnoreCase(gateway)).withFailMessage("Incorrect Gateway Name");
        softAssert.assertThat(peonResponse.getBANKTXNID()).isNotNull().withFailMessage("Bank TxnID is Null");
        softAssert.assertThat(peonResponse.getMID()).isNotNull().withFailMessage("MID is Null");
        softAssert.assertThat(peonResponse.getPAYMENTMODE().equalsIgnoreCase("CC")).withFailMessage("PaymentMode is incorrect");
        softAssert.assertThat(peonResponse.getSTATUS().equalsIgnoreCase(status)).withFailMessage("Incorrect Status");
        softAssert.assertThat(peonResponse.getTXNDATETIME()).isNotNull().withFailMessage("TxnDateTime is Null");
        softAssert.assertThat(peonResponse.getTXNDATE()).isNotNull().withFailMessage("TxnDate is Null");
        softAssert.assertThat(peonResponse.getTXNAMOUNT().equalsIgnoreCase(txnAmount)).withFailMessage("Icorrect TxnAmount.");
        softAssert.assertThat(peonResponse.getRESULTCODE().equals("FGW_OTP_VALIDATION_FAILED"));
        softAssert.assertAll();
    }

    public void validatePeon_Promo(OrderDTO orderDTO, String bankName, String gateway, String status){
        String respCode;
        if(status.equalsIgnoreCase("TXN_SUCCESS")){
            respCode = "700";
        }else{
            respCode = "701";
        }
        String orderId = orderDTO.getORDER_ID();
        SoftAssert softAssert = new SoftAssert();
        Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getBANKNAME(),bankName);
        softAssert.assertEquals(peonResponse.getGATEWAYNAME(), gateway);
        softAssert.assertNotNull(peonResponse.getBANKTXNID());
        softAssert.assertNotNull(peonResponse.getMID());
        softAssert.assertEquals(peonResponse.getPAYMENTMODE(),"CC");
        softAssert.assertEquals(peonResponse.getSTATUS(), status);
        softAssert.assertNotNull(peonResponse.getTXNDATETIME());
        softAssert.assertNotNull(peonResponse.getTXNDATE());
        softAssert.assertEquals(orderDTO.getTXN_AMOUNT(), peonResponse.getTXNAMOUNT());
        softAssert.assertEquals("PROMO_SUCCESS", peonResponse.getPROMO_STATUS());
        softAssert.assertEquals(orderDTO.getPROMO_CAMP_ID(), peonResponse.getPROMO_CAMP_ID());
        softAssert.assertEquals("PROMO_SUCCESS", peonResponse.getPROMO_STATUS());
        softAssert.assertEquals("CCPROMOAUTO", peonResponse.getPROMO_CAMP_ID());
        softAssert.assertEquals(respCode, peonResponse.getPROMO_RESPCODE());
        softAssert.assertAll();
    }



    public void validatePeon_CardIndexToken(OrderDTO orderDTO, String bankName, String gateway, String status){
        String orderId = orderDTO.getORDER_ID();
        SoftAssert softAssert = new SoftAssert();
        Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getBANKNAME(),bankName);
        softAssert.assertEquals(peonResponse.getGATEWAYNAME(), gateway);
        softAssert.assertNotNull(peonResponse.getBANKTXNID());
        softAssert.assertNotNull(peonResponse.getMID());
        softAssert.assertEquals(peonResponse.getPAYMENTMODE(),"CC");
        softAssert.assertEquals(peonResponse.getSTATUS(), status);
        softAssert.assertNotNull(peonResponse.getTXNDATETIME());
        softAssert.assertNotNull(peonResponse.getTXNDATE());
        softAssert.assertNotNull(peonResponse.getCardIndexNo());
        softAssert.assertEquals(orderDTO.getTXN_AMOUNT(), peonResponse.getTXNAMOUNT());
        softAssert.assertAll();
    }


    public void validatePeon_Hybrid(OrderDTO orderDTO, String bankName, String gateway, String status, String txnAmount) {
        String orderId = orderDTO.getORDER_ID();
        SoftAssert softAssert = new SoftAssert();
        Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getBANKNAME(),bankName);
        softAssert.assertEquals(peonResponse.getGATEWAYNAME(), gateway);
        softAssert.assertNotNull(peonResponse.getBANKTXNID());
        softAssert.assertNotNull(peonResponse.getMID());
        softAssert.assertEquals(peonResponse.getPAYMENTMODE(),"HYBRID");
        softAssert.assertEquals(peonResponse.getSTATUS(), status);
        softAssert.assertNotNull(peonResponse.getTXNDATETIME());
        softAssert.assertNotNull(peonResponse.getTXNDATE());
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), txnAmount);
        softAssert.assertNotNull(peonResponse.getChildTxnString());
        softAssert.assertAll();
        Assertions.assertThat(peonResponse.getChildTxnString().contains("GATEWAYNAME\\\":\\\"HDFC"));
        Assertions.assertThat(peonResponse.getChildTxnString().contains("GATEWAYNAME\\\":\\\"WALLET"));
        Assertions.assertThat(peonResponse.getChildTxnString().contains("PAYMENTMODE\\\":\\\"PPI"));
    }

    public void validatePeon_EMIIndexToken(OrderDTO orderDTO, String bankName, String gateway, String status){
        String respCode;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setMaximumFractionDigits(2);
        String orderId = orderDTO.getORDER_ID();
        SoftAssert softAssert = new SoftAssert();
        Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getBANKNAME(),bankName);
        softAssert.assertEquals(peonResponse.getGATEWAYNAME(), gateway);
        softAssert.assertNotNull(peonResponse.getBANKTXNID());
        softAssert.assertNotNull(peonResponse.getMID());
        softAssert.assertEquals(peonResponse.getPAYMENTMODE(),"EMI");
        softAssert.assertEquals(peonResponse.getSTATUS(), status);
        softAssert.assertNotNull(peonResponse.getTXNDATETIME());
        softAssert.assertNotNull(peonResponse.getTXNDATE());
        softAssert.assertEquals(decimalFormat.format(Float.parseFloat(orderDTO.getTXN_AMOUNT())), peonResponse.getTXNAMOUNT());
        softAssert.assertAll();
    }


}
