package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.masterRefund.MasterRefundBody;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import org.apache.commons.lang.RandomStringUtils;

import static com.paytm.appconstants.Constants.PGPAPIResourcePath.MASTERREFUND;

/**
 * Created by anjukumari on 24/01/19
 */
public class MasterRefund extends BaseApi {

    public MasterRefund(MasterRefundBody body) {
        this.setMethod(BaseApi.MethodType.POST);
        this.getRequestSpecBuilder().setContentType(ContentType.JSON);
        this.getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        this.getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        this.getRequestSpecBuilder().setBasePath(MASTERREFUND);
        this.getRequestSpecBuilder().setBody(body);

    }

    String body  = "{\n" +
            "    \"MID\": \"{MID}\",\n" +
            "    \"TXNID\": \"{TXNID}\",\n" +
            "    \"ORDERID\": \"{orderId}\",\n" +
            "    \"REFID\": \"{REFID}\",\n" +
            "    \"REFUNDAMOUNT\": \"{TXN_AMOUNT}\",\n" +
            "    \"TXNTYPE\": \"REFUND\",\n" +
            "    \"riskExtendInfo\":{\"verticalId\":\"76\",\"categoryId\":\"262072\",\"subscriberId\":\"bgty\",\"manualRefundFlag\":\"false\",\"udf1\":\"asc\",\"udf2\":\"234\",\"udf3\":\"ghi\",\"udf4\":\"fgh\",\"udf5\":\"123\"},\"envInfo\":{\"clientIp\":\"abc\",\"osType\":\"def\",\"osVersion\":\"os1.2.3\",\"latitude\":\"99.2\",\"longitude\":\"73.2\",\"deviceModel\":\"fghi\",\"deviceIMEI\":\"1234efrd\",\"deviceId\":\"12345\",\"deviceManufacturer\":\"asdf\"},\"agentInfo\":{\"employeeId\":\"emp123\",\"name\":\"abcde\",\"phoneNo\":\"1234567\",\"email\":\"abc.gmail.com\"}\n" +
            "\n" +
            "}";

    public String getRequest()
    {
        return body;
    }


    public  void setRequest(Constants.MerchantType mid, String txnId,String orderId, String txnAmount) {
        String refid = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        body = body.replace("{MID}", mid.getId()).replace("{TXNID}", txnId).replace("{orderId}", orderId).replace("{TXN_AMOUNT}", txnAmount).replace("{REFID}", refid);;

    }

    public MasterRefund(Constants.MerchantType mid, String txnId,String orderId, String txnAmount )
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBasePath(MASTERREFUND);
        setRequest(mid,txnId,orderId,txnAmount);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
