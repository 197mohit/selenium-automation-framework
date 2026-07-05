package com.paytm.api.qr;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class GenerateQR extends BaseApi {

    String request = "{\n" +
            "\"request\": {\n" +
            "\"createRequest\": {\n" +
            "\"agentPhoneNo\":\"9958579496\",\n" +
            "\"agentEmailId\":\"\",\n" +
            "\"businessType\":\"{QRTYPE}\",\n" +
            "\"batchSize\": null,\n" +
            "\"batchCount\":null\n" +
            "},\n" +
            "\"mapRequest\": {\n" +
            "\"stickerId\": \"\",\n" +
            "\"phoneNo\": \"\",\n" +
            "\"emailId\": \"\",\n" +
            "\"displayName\": \"ankit34708655987864\",\n" +
            "\"category\": \"Food\",\n" +
            "\"amount\":,\n" +
            "\"subCategory\": null,\n" +
            "\"tagLine\": null,\n" +
            "\"secondaryPhoneNumber\": \"{mobile_number}\",\n" +
            "\"mappingType\": \"MERCHANT\",\n" +
            "\"merchantGuid\": \"\",\n" +
            "\"typeOfQrCode\": \"{QRTYPE}\",\n" +
            "\"sourceId\": \"\",\n" +
            "\"merchantMid\": \"{MERCHANT_ID}\",\n" +
            "\"sourceName\":\"\",\n" +
            "\"posId\": \"{POS_ID}\",\n" +
            "\"mappedBy\" : \"\",\n" +
            "\"qrCodeId\" : \"\",\n" +
            "\"latitude\" : \"\",\n" +
            "\"longitude\" : \"\",\n" +
            "\"allowedDistanceLimit\":\"\",\n" +
            "\"isDistanceLimitMandatory\": \"\",\n" +
            "\"deepLink\":\"\",\n" +
            "            \"additionalInfo\": {\n" +
            "                \"shopId\": \"7894165529674487414\",\n" +
            "                \"kybId\": \"6776562783592985660\"\n" +
            "            },\n" +
            "\"status\":1\n" +
            "},\n" +
            "\"operationType\" :[\"CREATE\",\"MAP\"]\n" +
            "\n" +
            "\n" +
            "},\n" +
            "\"ipAddress\": \"127.0.0.1\",\n" +
            "\"platformName\": \"PayTM\",\n" +
            "\"operationType\": \"QR_CODE\"\n" +
            "}";


    public String getRequest()
    {
        return  request;
    }

    public GenerateQR setRequest(String merchantId,String mobileNumber) {
        request = request.replace("{MERCHANT_ID}",merchantId).replace("{mobile_number}",mobileNumber).replace("{QRTYPE}","UPI_QR_CODE").replace("{POS_ID}","");
        return this;
    }

    public GenerateQR setRequest(String merchantId,String mobileNumber,String qrType) {
        request = request.replace("{MERCHANT_ID}",merchantId).replace("{mobile_number}",mobileNumber).replace("{QRTYPE}",qrType).replace("{POS_ID}","");
        return this;
    }

    public GenerateQR setRequest(String merchantId,String mobileNumber, int posID) {
        String posValue = String.valueOf(posID);
        request = request.replace("{MERCHANT_ID}",merchantId).replace("{mobile_number}",mobileNumber).replace("{QRTYPE}","UPI_QR_CODE").replace("{POS_ID}",posValue);
        return this;
    }



    public GenerateQR(String merchantId,String mobileNumberQR) {
        RestAssured.useRelaxedHTTPSValidation();
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.QR_HOST);
        getRequestSpecBuilder().addHeader("clientid","a5516f104428408fb6051f833c9bb9e0");
        getRequestSpecBuilder().addHeader("hash","d67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94");
       // getRequestSpecBuilder().addHeader("Postman-Token","ca5f63f1-df26-48d3-b9a8-1332ab2ab0e9");
        //getRequestSpecBuilder().addHeader("cache-control:","no-cache,no-cache,no-cache");
        setRequest(merchantId,mobileNumberQR);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.QR.GENERATE_QR);

    }

    public GenerateQR(String merchantId,String mobileNumberQR, int posId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.QR_HOST);
        getRequestSpecBuilder().addHeader("clientid","a5516f104428408fb6051f833c9bb9e0");
        getRequestSpecBuilder().addHeader("hash","d67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94");
        // getRequestSpecBuilder().addHeader("Postman-Token","ca5f63f1-df26-48d3-b9a8-1332ab2ab0e9");
        //getRequestSpecBuilder().addHeader("cache-control:","no-cache,no-cache,no-cache");
        setRequest(merchantId,mobileNumberQR, posId);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.QR.GENERATE_QR);

    }

    /**
     * This is deprecated as whenever API is executed with qrType=UPI_QR_CODE new static QR is generated \n
     * which is not the exact situation like production
     * Hardcode STATIC_QR_CODE_ID in merchant.yaml and fetch from {@link Constants.MerchantType}
     *
     * @param merchantId
     * @param mobileNumberQR
     * @param qrType
     */
    @Deprecated
    public GenerateQR(String merchantId,String mobileNumberQR,String qrType) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.QR_HOST);
        getRequestSpecBuilder().addHeader("clientid","a5516f104428408fb6051f833c9bb9e0");
        getRequestSpecBuilder().addHeader("hash","d67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94");
        // getRequestSpecBuilder().addHeader("Postman-Token","ca5f63f1-df26-48d3-b9a8-1332ab2ab0e9");
        //getRequestSpecBuilder().addHeader("cache-control:","no-cache,no-cache,no-cache");
        setRequest(merchantId,mobileNumberQR,qrType);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.QR.GENERATE_QR);

    }
}
