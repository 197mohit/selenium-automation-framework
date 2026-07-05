package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.autorefund.AutoPeonResponse;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class AutoRefundPeon extends BaseApi {
    private SoftAssertions softAssert = new SoftAssertions();
    private Response response;

    public AutoRefundPeon(String orderId) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.AUTO_REFUND_PEON);
        getRequestSpecBuilder().addParam("orderId", orderId);
    }

    public void executeUntilGetResponse(int timeOutInSeconds, int pollingTimeInSeconds){
        Response response = null;
        try {
            response = execute();
            response = Awaitility.with().pollInSameThread()
                    .await()
                    .pollInterval(new Duration(pollingTimeInSeconds, TimeUnit.SECONDS))
                    .atMost(new Duration(timeOutInSeconds, TimeUnit.SECONDS))
                    .until(responseCallable(), r -> r.statusCode()==200);
        } catch (Exception e){
            Assertions.assertThat(response.getStatusCode()).as("Status Code").isEqualTo(200);
        }
    }

    private Callable<Response> responseCallable(){
        return new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return execute();
            }
        };
    }

    public void executeUntilGetResponse(){
      this.executeUntilGetResponse(120,5);
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

    public AutoRefundPeon validateTxnTimeStamp(String date)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<String> list= Arrays.asList(date.split("T"));
        softAssert.assertThat(timestamp.toString().contains(list.get(0))).as("Timestamp is incorrect");
        return this;
    }

    public void validatePWPTxn(OrderDTO orderDTO,Double reversalAmount)
    {
        String orderId = orderDTO.getORDER_ID();
        this.executeUntilGetResponse();
        AutoPeonResponse autoPeonResponse = getPeonData(orderId);
        DecimalFormat df = new DecimalFormat("0.00");
        validateTxnTimeStamp(autoPeonResponse.getBody().getTxnTimestamp());

        softAssert.assertThat(autoPeonResponse.getBody().getReversalAmount())
                .as("Reversal amount mismatch")
                .isEqualTo(df.format(reversalAmount));

        softAssert.assertThat(autoPeonResponse.getBody().getOrderId())
                .as("Order Id mismatch")
                .isEqualTo(orderDTO.getORDER_ID());

        softAssert.assertThat(autoPeonResponse.getBody().getReversalId())
                .as("Reversal Id is empty")
                .isNotEmpty();

        softAssert.assertThat(autoPeonResponse.getBody().getMid())
                .as("MID mismatch")
                .isEqualTo(orderDTO.getMID());

        softAssert.assertThat(autoPeonResponse.getBody().getTxnAmount())
                .as("Txn amount mismatch")
                .isEqualTo(df.format(Double.valueOf(orderDTO.getTXN_AMOUNT())));

        softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getReversalAmount())
                .as("Reversal amount mismatch in reversalDetailInfoList")
                .isEqualTo(new DecimalFormat("0.00").format(reversalAmount));

        softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getPayMethod())
                .as("Pay method is different")
                .isEqualTo(Constants.Gateway.PAYTM.toString());
        softAssert.assertAll();
    }

    public void validateFundBackOrRevokeTxn(OrderDTO orderDTO, Double reversalAmount, String bankName, String payMethod)
    {
        String orderId = orderDTO.getORDER_ID();
        this.executeUntilGetResponse();
        AutoPeonResponse autoPeonResponse = getPeonData(orderId);
        DecimalFormat df = new DecimalFormat("0.00");
        validateTxnTimeStamp(autoPeonResponse.getBody().getTxnTimestamp());
        softAssert.assertThat(autoPeonResponse.getBody().getReversalAmount())
                .as("Reversal amount mismatch")
                .isEqualTo(df.format(reversalAmount));

        softAssert.assertThat(autoPeonResponse.getBody().getOrderId())
                .as("Order Id mismatch")
                .isEqualTo(orderDTO.getORDER_ID());

        softAssert.assertThat(autoPeonResponse.getBody().getReversalId())
                .as("Reversal Id is empty")
                .isNotEmpty();

        softAssert.assertThat(autoPeonResponse.getBody().getMid())
                  .as("MID mismatch")
                  .isEqualTo(orderDTO.getMID());

        softAssert.assertThat(autoPeonResponse.getBody().getTxnAmount())
                .as("Txn amount mismatch")
                .isEqualTo(df.format(Double.valueOf(orderDTO.getTXN_AMOUNT())));

        softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getReversalAmount())
                .as("Reversal amount mismatch in reversalDetailInfoList")
                .isEqualTo(new DecimalFormat("0.00").format(reversalAmount));

        softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getPayMethod())
                .as("Pay method is different")
                .isEqualTo(payMethod);

        if(!payMethod.equals("PAYTM_DIGITAL_CREDIT") && !payMethod.equals("UPI")) {
            softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getIssuingBankName())
                    .as("Bank name mismatch")
                    .isEqualTo(bankName);

            if(payMethod.equals("CREDIT_CARD") || payMethod.equals("DEBIT_CARD"))
            softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getMaskedCardNumber())
                    .as("Card number not masked")
                    .contains("******");

        }

        if(payMethod.equals("UPI"))
        {
            softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getMaskedVpa()).as("VPA is not masked").contains("****");
        }

        softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(0).getUserMobileNo()).as("Mobile number is not masked").contains("******");

        softAssert.assertAll();
    }


    public void validateFundBackHybridTxn(OrderDTO orderDTO, Double reversalAmount, String bankName, String payMethod,Double amountToBeRetainedInWallet)
    {
        String orderId = orderDTO.getORDER_ID();
        this.executeUntilGetResponse();
        AutoPeonResponse autoPeonResponse = getPeonData(orderId);
        DecimalFormat df = new DecimalFormat("0.00");
        validateTxnTimeStamp(autoPeonResponse.getBody().getTxnTimestamp());
        softAssert.assertThat(autoPeonResponse.getBody().getReversalAmount())
                .as("Reversal amount mismatch")
                .isEqualTo(df.format(reversalAmount));

        softAssert.assertThat(autoPeonResponse.getBody().getOrderId())
                .as("Order Id mismatch")
                .isEqualTo(orderDTO.getORDER_ID());

        softAssert.assertThat(autoPeonResponse.getBody().getReversalId())
                .as("Reversal Id is empty")
                .isNotEmpty();

        softAssert.assertThat(autoPeonResponse.getBody().getMid())
                .as("MID mismatch")
                .isEqualTo(orderDTO.getMID());

        softAssert.assertThat(autoPeonResponse.getBody().getTxnAmount())
                .as("Txn amount mismatch")
                .isEqualTo(df.format(Double.valueOf(orderDTO.getTXN_AMOUNT())));

        for(int i =0 ; i<2;i++)
        {


         if (autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getPayMethod().equals("BALANCE")) {
                softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getReversalAmount())
                        .as("Reversal amount mismatch in reversalDetailInfoList")
                        .isEqualTo(df.format(amountToBeRetainedInWallet));

                softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getUserMobileNo())
                         .as("Mobile number is not masked")
                        .contains("******");

            }
         else  {
                 softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getReversalAmount())
                         .as("Reversal amount mismatch in reversalDetailInfoList")
                         .isEqualTo(df.format(reversalAmount-amountToBeRetainedInWallet));

                 softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getIssuingBankName())
                         .as("Bank name mismatch")
                         .isEqualTo(bankName);
                 softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getMaskedCardNumber())
                         .as("Card number not masked")
                         .contains("********");

                 softAssert.assertThat(autoPeonResponse.getBody().getReversalDetailInfoList().get(i).getUserMobileNo())
                         .as("Mobile number is not masked")
                         .contains("******");

             }
         }

        softAssert.assertAll();

    }


    public AutoRefundPeon assertAll() {
        this.softAssert.assertAll();
        return this;
    }

}
