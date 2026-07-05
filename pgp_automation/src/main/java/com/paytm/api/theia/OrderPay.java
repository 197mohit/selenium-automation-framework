package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class OrderPay extends BaseApi
{
    String body="{\n" +
            "    \"header\": {\n" +
            "        \"clientId\": \"PAYTM\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": 1616654782342,\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"signature\": \"{signature}\"\n" +
            "    },\n" +
            "   \"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"mid\":\"{mid}\",\"orderId\":\"{orderId}\",\"custId\":\"par123\",\"txnAmount\":\"{txnAmount}\",\"industryType\":\"Retail\",\"website\":\"retail\",\"paymentMode\":\"{paymentMode}\",\"bankCode\":\"{bankCode}\",\"mobileNo\":\"8249910917\",\"UDF_1\":\"812321267130470400\",\"additionalInfo\":\"{\\\\\\\"merchantUniqueReference\\\\\\\":\\\\\\\"\\\\\\\"}\",\"callBackURL\":\"https://qa-aoainsta-in t.paytm.com/instaproxy/aggregator/response/PAYTMPG/RESP/\"}\n" +
            "   }";

    String bodyforchecksum="{\"requestType\":\"SEAMLESS_3D_FORM\",\"mid\":\"{mid}\",\"orderId\":\"{orderId}\",\"custId\":\"par123\",\"txnAmount\":\"{txnAmount}\",\"industryType\":\"Retail\",\"website\":\"retail\",\"paymentMode\":\"{paymentMode}\",\"bankCode\":\"{bankCode}\",\"mobileNo\":\"8249910917\",\"UDF_1\":\"812321267130470400\",\"additionalInfo\":\"{\\\\\\\"merchantUniqueReference\\\\\\\":\\\\\\\"\\\\\\\"}\",\"callBackURL\":\"https://qa-aoainsta-in t.paytm.com/instaproxy/aggregator/response/PAYTMPG/RESP/\"}";

    public String setChecksum(Constants.MerchantType mid, String orderId , String txnAmount , String paymentMode , String bankCode )
    {
        bodyforchecksum=bodyforchecksum.replace("{mid}", mid.getId()).replace("{orderId}",orderId).replace("{txnAmount}",txnAmount).replace("{paymentMode}",paymentMode).replace("{bankCode}",bankCode);
        return bodyforchecksum;
    }


    public void setRequest(Constants.MerchantType mid, String orderId ,String txnAmount , String paymentMode , String bankCode,String signature)
    {
        body = body.replace("{mid}", mid.getId()).replace("{orderId}",orderId).replace("{txnAmount}",txnAmount).replace("{paymentMode}",paymentMode).replace("{bankCode}",bankCode).replace("{signature}",signature);
    }
    public OrderPay(Constants.MerchantType mid,String orderId ,String txnAmount , String paymentMode , String bankCode)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().addQueryParam("MID",mid.getId());
        getRequestSpecBuilder().addQueryParam("ORDER_ID",orderId);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_ORDER_PAY);
        String bodychecksum=setChecksum(mid,orderId,txnAmount,paymentMode,bankCode);
        String signature= PGPUtil.getChecksum(mid.getKey(),bodychecksum);
        setRequest(mid,orderId,txnAmount,paymentMode,bankCode,signature);
        getRequestSpecBuilder().setBody(getRequest());
    }
    public String getRequest()
    {
        return body;
    }


}
