package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
public class DeductionNotify extends BaseApi {
    String dnRequest="{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"clientId\": \"notification-adapter\",\n" +
            "            \"function\": \"oldpg.acquiring.order.deductionNotify\",\n" +
            "            \"reqTime\": \"2023-05-30T16:29:43+05:30\",\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"reqMsgId\": \"202305300103700008703423342639472642023-05-30T16:29:43+05:30\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"deductionDetailId\": \"20230530010368703423344731832327264\",\n" +
            "            \"notificationUrl\": \"https://api.lending.paytm.com/bwrepayment/order/deduction-settlement\",\n" +
            "            \"merchantId\": \"qa8mid79586673996814\",\n" +
            "            \"orderId\": \"QA8_DNTEST1\",\n" +
            "            \"clientName\": \"lending\",\n" +
            "            \"deductionStatus\": \"INIT\",\n" +
            "            \"deductedAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"92.00\"\n" +
            "            },\n" +
            "            \"paymentId\": \"20230530010860000870342334708692441\",\n" +
            "            \"successTime\": \"2023-05-30T16:29:43+05:30\",\n" +
            "            \"deductionId\": \"20230530010370000870342334263947264\",\n" +
            "            \"payeeId\": \"rrRTGq00083021955026\",\n" +
            "            \"pendingAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"315.00\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"b174fc6927b9ac73d24db8ad9c685a6f9249d74205c70c4eb95d32fcbc3645de\"\n" +
            "}";

    public DeductionNotify setPaymentId(String paymentId){
        setContext("request.body.paymentId",paymentId);
        return this;
    }
    public DeductionNotify setDeductiondetailID(String deductiondetailID){
        setContext("request.body.deductionDetailId",deductiondetailID);
        return this;
    }
    public DeductionNotify setMID(String mid){
        setContext("request.body.merchantId",mid);
        return this;
    }
    public DeductionNotify setOrderID(String orderID){
        setContext("request.body.orderId",orderID);
        return this;
    }
    public DeductionNotify setDeductionStatus(String deductionStatus){
        setContext("request.body.deductionStatus",deductionStatus);
        return this;
    }

    public DeductionNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getDeductionNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.DEDUCTION_NOTIFY);
    }
    public String getDeductionNotifyRequest() {return dnRequest;}
}
