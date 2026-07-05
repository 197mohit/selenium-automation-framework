package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

public class withdrawRequest extends BaseApi {

    String appId;
    String bankLineNo;
    String amount;
    String mid;
    String extSerialNo;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getBankLineNo() {
        return bankLineNo;
    }

    public void setBankLineNo(String bankLineNo) {
        this.bankLineNo = bankLineNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
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

    String request = "{\n" +
        "    \"request\": {\n" +
        "        \"head\": {\n" +
        "            \"version\": \"1.1.2\",\n" +
        "            \"function\": \"alipayplus.fluxnet.paytm.withdraw.request\",\n" +
        "            \"appId\": \"{APPID}\",\n" +
        "            \"reqTime\": \"{DATE}T20:23:52+05:30\",\n" +
        "            \"reqMsgId\": \"666e7bba-a30c-4245-a510-2bd029323314\"\n" +
        "        },\n" +
        "        \"body\": {\n" +
        "            \"transId\": \"20200611201214820101101685251210113\",\n" +
        "            \"bankAbbr\": \"\",\n" +
        "            \"bankName\": \"\",\n" +
        "            \"bankLineNo\": \"{BANKIFSC}\",\n" +
        "            \"bankAccountNo\": \"  \",\n" +
        "            \"mobileNo\": \"\",\n" +
        "            \"terminalType\": \"WAP\",\n" +
        "            \"exchangeCurrency\": \"INR\",\n" +
        "            \"exchangeAmount\": \"{AMOUNT}\",\n" +
        "            \"cashierWithdrawTime\": \"{DATE}T19:22:52+05:30\",\n" +
        "            \"payoutId\": \"null\",\n" +
        "            \"pmid\": \"{MID}\",\n" +
        "              \"extendInfo\": \"{\\\"transferType\\\":\\\"m2b\\\"}\",\n" +
        "            \"extSerialNo\": \"{EXTSERIALNO}\",\n" +
        "            \"virtualPaymentAddress\": \"null\",\n" +
        "            \"merchantTransId\": \"null\",\n" +
        "            \"merchantName\": \"null\",\n" +
        "            \"holderFirstName\": \"Gagan\",\n" +
        "            \"createdTime\": \"{DATE}T18:25:52+05:30\"\n" +
        "        }\n" +
        "    },\n" +
        "    \"signature\": \"38da63a7fd7bd8569483cfa8ac1a6ac7df309a0e0ede4014bd8919eb7dc911e1\"\n" +
        "}";



    public void setRequest(String alipayMerchant,String amount, String extno,String bankLineNo,String appId)
    {
        this.request = request
                .replace("{APPID}",appId)
                .replace("{MID}",alipayMerchant)
                .replace("{EXTSERIALNO}",extno)
                .replace("{BANKIFSC}",bankLineNo)
                .replace("{AMOUNT}",amount)
                .replace("{DATE}",CommonHelpers.getDate().toString());
    }


    public withdrawRequest(Builder builder){
        this.amount=builder.amount;
        this.appId=builder.appId;
        this.bankLineNo=builder.bankLineNo;
        this.extSerialNo=builder.extSerialNo;
        this.mid=builder.mid;

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.InstaProxyService.WITHDRAW_REQUEST);
        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(mid);
        String alipayId = dbResult.get("alipay_merchant_id").toString();
        setRequest(alipayId,amount,extSerialNo,bankLineNo,appId);
        getRequestSpecBuilder().setBody(request);
    }

    private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    public static class Builder {

        String appId = "PPBIC1IN06";
        String bankLineNo="WALL0123456";
        String amount="100";
        String mid="";
        String extSerialNo="";


        public Builder(String mid, String extSerialNo) {
            this.mid = mid;
            this.extSerialNo = extSerialNo;
        }



        public String getAppId() {
            return appId;
        }

        public withdrawRequest.Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public String getBankLineNo() {
            return bankLineNo;
        }

        public withdrawRequest.Builder setBankLineNo(String bankLineNo) {
            this.bankLineNo = bankLineNo;
            return this;
        }

        public String getAmount() {
            return amount;
        }

        public withdrawRequest.Builder setAmount(String amount) {
            amount= Integer.parseInt(amount)*100 +"";        //since amount is in Paisa therefore multiplying 100
            this.amount = amount;
            return this;
        }

        public String getMid() {
            return mid;
        }

        public withdrawRequest.Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getExtSerialNo() {
            return extSerialNo;
        }

        public withdrawRequest.Builder setExtSerialNo(String extSerialNo) {
            this.extSerialNo = extSerialNo;
            return this;
        }
        public withdrawRequest build()
        {return new withdrawRequest(this);}


    }






}
