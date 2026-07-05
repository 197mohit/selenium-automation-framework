package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class withdrawStatus extends BaseApi {

    String appId;
    String amount;
    String mbid;
    String extSerialNo;

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"version\": \"1.1.2\",\n" +
            "            \"function\": \"alipayplus.fluxnet.paytm.withdraw.query.request\",\n" +
            "            \"appId\": \"{APPID}\",\n" +
            "            \"reqTime\": \"{DATE}T18:44:14+05:30\",\n" +
            "            \"reqMsgId\": \"d438516e-7be0-4047-9b37-221ca90f33eb\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"extSerialNo\": \"{EXTSERIALNO}\",\n" +
            "            \"bankAbbr\": \"PPBI\",\n" +
            "            \"referenceNo\": \"\",\n" +
            "            \"mbid\": \"{MBID}\",\n" +
            "            \"exchangeCurrency\": \"INR\",\n" +
            "            \"exchangeAmount\": \"{AMOUNT}\",\n" +
            "            \"traceNo\": \"NODALV3\",\n" +
            "            \"createdTime\": \"{DATE}T16:33:18+05:30\",\n" +
            "            \"payTime\": \"{DATE}T16:33:18+05:30\",\n" +
            "            \"notifyPlatformPlus\": \"y\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"7abac1de9cae84118a43629b06f6e39c8e324a050aea19403ded4372b30421a5\"\n" +
            "}";


    public void setRequest(String mbid,String amount, String extno, String appId) {
        this.request = request
                .replace("{APPID}", appId)
                .replace("{EXTSERIALNO}", extno)
                .replace("{AMOUNT}", amount)
                .replace("{MBID}", mbid)
                .replace("{DATE}", CommonHelpers.getDate().toString());
    }


    public withdrawStatus(Builder builder) {
        this.amount = builder.amount;
        this.appId = builder.appId;
        this.extSerialNo = builder.extSerialNo;
        this.mbid = builder.mbid;
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.InstaProxyService.STATUS_QUERY);
        setRequest(mbid,amount, extSerialNo, appId);
        getRequestSpecBuilder().setBody(request);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }


    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMid(String mbid) {
        this.mbid = mbid;
    }

    public String getExtSerialNo() {
        return extSerialNo;
    }

    public void setExtSerialNo(String extSerialNo) {
        this.extSerialNo = extSerialNo;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }


    public static class Builder {

        String appId = "";
        String amount = "100";
        String extSerialNo = "";
        String mbid = "";


        public Builder(String mbid,withdrawRequest withrawRequest) {
            this.mbid=mbid;
            this.appId = withrawRequest.appId;
            this.amount = withrawRequest.amount;
            this.extSerialNo = withrawRequest.extSerialNo;

        }

        public String getAppId() {
            return appId;
        }

        public withdrawStatus.Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public String getAmount() {
            return amount;
        }

        public withdrawStatus.Builder setAmount(String amount) {
            amount= Integer.parseInt(amount)*100 +"";
            this.amount = amount;
            return this;
        }

        public String getExtSerialNo() {
            return extSerialNo;
        }

        public withdrawStatus.Builder setExtSerialNo(String extSerialNo) {
            this.extSerialNo = extSerialNo;
            return this;
        }

        public withdrawStatus build() {
            return new withdrawStatus(this);
        }


    }

    public static class StatusFromMock extends BaseApi {
        public StatusFromMock(String extNo) {
            setMethod(MethodType.GET);
            getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
            getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
            getRequestSpecBuilder().setBasePath("mockbank/get/ppbi/funds-transfer/"+extNo);
        }
    }
}
