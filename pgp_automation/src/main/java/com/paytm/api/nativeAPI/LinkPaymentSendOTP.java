package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class LinkPaymentSendOTP extends BaseApi{

    public LinkPaymentSendOTP(String mid, String mobNo, String merchantName, String txnAmount, String LinkId){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.Link_Payment_SendOTP);
        getRequestSpecBuilder().setBody("{\n" +
                "    \"mobileNumber\": \"" + mobNo + "\",\n" +
                "    \"resendCount\": 0,\n" +
                "    \"txnAmount\": \"" + txnAmount + "\",\n" +
                "    \"txnDiscription\": \"\",\n" +
                "    \"mid\": \"" + mid + "\",\n" +
                "    \"merchantName\": \"" + merchantName + "\",\n" +
                "    \"uniqueId\": \"\",\n" +
                "    \"linkId\": \"" + LinkId + "\"\n" +
                "}");
    }
}
