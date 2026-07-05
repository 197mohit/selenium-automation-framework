package com.paytm;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.autorefund.AutoPeonResponse;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class RefundSucessNotifyPeon extends BaseApi {
    private SoftAssertions softAssert = new SoftAssertions();
    private Response response;

    public RefundSucessNotifyPeon(String orderId,String mid) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.REFUND_SUCCESS_PEON);
        getRequestSpecBuilder().addParam("orderId", orderId);
        getRequestSpecBuilder().addParam("mid", mid);
    }

    public Response executeUntilGetResponse(int timeOutInSeconds, int pollingTimeInSeconds){
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
        return response;
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

    public AutoPeonResponse getPeonData(String orderId){
        Response response = this.execute();
        AutoPeonResponse autoPeonResponse;
        autoPeonResponse = response.as(AutoPeonResponse.class);
        return autoPeonResponse;
    }

    public RefundSucessNotifyPeon validateTxnTimeStamp(String date)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<String> list= Arrays.asList(date.split("T"));
        softAssert.assertThat(timestamp.toString().contains(list.get(0))).as("Timestamp is incorrect");
        return this;
    }


    public RefundSucessNotifyPeon validateBasicDetails(Constants.MerchantType merchantType, OrderDTO orderDTO)
    {
        Response response = this.executeUntilGetResponse(190,30);
        softAssert.assertThat(response.jsonPath().getString("head.signature")).as("Signature is empty/null").isNotEmpty().isNotNull();
        softAssert.assertThat(response.jsonPath().getString("body.mid")).as("MID is incorrect in refund success notify").isEqualTo(merchantType.getId());
        softAssert.assertThat(response.jsonPath().getString("body.orderId")).as("Orderid is incorrect").isEqualTo(orderDTO.getORDER_ID());
        softAssert.assertThat(response.jsonPath().getString("body.txnId")).as("Txn Id is empty/null").isNotEmpty().isNotNull();
        softAssert.assertThat(response.jsonPath().getString("body.refundId")).as("Refund Id is empty/null").isNotEmpty().isNotNull();
        this.assertAll();
        return this;
    }

    public RefundSucessNotifyPeon validateBasicDetails(String mid, String orderId)
    {
        Response response = this.executeUntilGetResponse(190,30);
        softAssert.assertThat(response.jsonPath().getString("head.signature")).as("Signature is empty/null").isNotEmpty().isNotNull();
        softAssert.assertThat(response.jsonPath().getString("body.mid")).as("MID is incorrect in refund success notify").isEqualTo(mid);
        softAssert.assertThat(response.jsonPath().getString("body.orderId")).as("Orderid is incorrect").isEqualTo(orderId);
        softAssert.assertThat(response.jsonPath().getString("body.txnId")).as("Txn Id is empty/null").isNotEmpty().isNotNull();
        softAssert.assertThat(response.jsonPath().getString("body.refundId")).as("Refund Id is empty/null").isNotEmpty().isNotNull();
        this.assertAll();
        return this;
    }

    public RefundSucessNotifyPeon assertAll() {
        this.softAssert.assertAll();
        return this;
    }

}
