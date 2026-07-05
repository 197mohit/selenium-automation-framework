package com.paytm.dto.NativeDTO.InitTxn;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
public class ChecksumInitTxnApi extends BaseApi
{
    public String checksum(String key,String Mid, String AggrMid,String orderId)
    {
        String bodyForChecksum = "{\"requestType\":\"NATIVE_MF\",\"mid\":\""+Mid+"\",\"validateAccountNumber\":\"false\",\"allowUnverifiedAccount\":\"true\",\"aggMid\":\""+AggrMid+"\",\"orderId\":\""+orderId+"\",\"websiteName\":\"retail\",\"emiOption\":\"\",\"txnAmount\":{\"value\":\"1\",\"currency\":\"INR\"},\"userInfo\":{\"custId\":\"CUD318\",\"mobile\":\"9999161601\",\"email\":\"test@paytm.com\",\"firstName\":\"test\",\"lastName\":\"test\"},\"promoCode\":\"\",\"paytmSsoToken\":\"\",\"callbackUrl\":\"\",\"additionalInfo\":{\"ref1\":\"abc\",\"ref2\":\"qwe\",\"ref3\":\"asdf\",\"ref4\":\"wed\",\"ref5\":\"wed\",\"ref6\":\"wed\",\"ref7\":\"wed\",\"ref8\":\"wed\"}}" ;
        String checksum=PGPUtil.getChecksum(key,bodyForChecksum);
        return checksum;
    }

    public ChecksumInitTxnApi(String key, String mid, String aggeratorMid,String orderID)
    {
        String request = "{\n" +
                "    \"head\": {\n" +
                "        \"clientId\": \"C11\",\n" +
                "        \"version\": \"v1\",\n" +
                "        \"requestTimestamp\": \"Time\",\n" +
                "        \"channelId\": \"WEB\",\n" +
                "        \"signature\": \""+checksum(key,mid,aggeratorMid,orderID)+"\"\n" +
                "    },\n" +
                "    \"body\": {\"requestType\":\"NATIVE_MF\",\"mid\":\""+mid+"\",\"validateAccountNumber\":\"false\",\"allowUnverifiedAccount\":\"true\",\"aggMid\":\""+aggeratorMid+"\",\"orderId\":\""+orderID+"\",\"websiteName\":\"retail\",\"emiOption\":\"\",\"txnAmount\":{\"value\":\"1\",\"currency\":\"INR\"},\"userInfo\":{\"custId\":\"CUD318\",\"mobile\":\"9999161601\",\"email\":\"test@paytm.com\",\"firstName\":\"test\",\"lastName\":\"test\"},\"promoCode\":\"\",\"paytmSsoToken\":\"\",\"callbackUrl\":\"\",\"additionalInfo\":{\"ref1\":\"abc\",\"ref2\":\"qwe\",\"ref3\":\"asdf\",\"ref4\":\"wed\",\"ref5\":\"wed\",\"ref6\":\"wed\",\"ref7\":\"wed\",\"ref8\":\"wed\"}}\n" +
                "}";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderID);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
}
