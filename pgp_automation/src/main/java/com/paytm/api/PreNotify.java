package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public class PreNotify extends BaseApi {


    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"timestamp\": \"\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"clientId\": \"1234\"\n" +
            "    },\n" +
            "    \"body\": {BODY}}";

    String body = "{\n" +
            "        \"subsId\": \"{SUBSID}\",\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"txnAmount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"txnDate\": \"{DATE}\",\n" +
            "        \"txnMessage\": \"subscription for postpaid mobile bill\",\n" +
            "        \"referenceId\": \"{REFERID}\"\n" +
            "    }";


    public PreNotify(Constants.MerchantType mid, String txnAmount, String SubsId) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/subscription/preNotify");
        setRequest(mid, txnAmount, SubsId, "", "");
        getRequestSpecBuilder().setBody(getRequest());
    }


    public PreNotify(Constants.MerchantType mid, String txnAmount, String SubsId, String txndate) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/subscription/preNotify");
        setRequest(mid, txnAmount, SubsId, txndate, "");
        getRequestSpecBuilder().setBody(getRequest());
    }

    public PreNotify(Constants.MerchantType mid, String txnAmount, String SubsId, String txndate, String referenceId) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/subscription/preNotify");
        setRequest(mid, txnAmount, SubsId, txndate, referenceId);
        getRequestSpecBuilder().setBody(getRequest());
    }

    private static String createChecksum(String merchantKey, String body) {
        String checksum = "";

        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getRequest() {
        return request;
    }

    public PreNotify setRequest(Constants.MerchantType mid, String txnAmount, String SubsId, String txndate, String referenceId ) {
      //  String RefId = CommonHelpers.generateOrderId();
        String RefId;
        if(referenceId.equals(""))
        {
            RefId = CommonHelpers.generateOrderId();
        }
        else
            RefId = referenceId;
        String date = CommonUtils.getdate("dd-MM-yyyy");
        if (!StringUtils.isBlank(txndate)){
            body = body.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount)
                    .replace("{SUBSID}", SubsId)
                    .replace("{DATE}", txndate+ " 00:00")
                    .replace("{REFERID}", RefId);
            System.out.println("Body : " + body);
        }
        else {
            body = body.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount)
                    .replace("{SUBSID}", SubsId)
                    .replace("{DATE}", CommonHelpers.addDays(date, "dd-MM-yyyy", 2) + " 00:00")
                    .replace("{REFERID}", RefId);
            System.out.println("Body : " + body);
        }
        String Signature = createChecksum(mid.getKey(), body);
        System.out.println("Signature : " + Signature);
        this.request = request.replace("{SIGNATURE}", Signature).replace("{BODY}", body);
        System.out.println("Request Body  : " + request);
        return this;


    }


}
