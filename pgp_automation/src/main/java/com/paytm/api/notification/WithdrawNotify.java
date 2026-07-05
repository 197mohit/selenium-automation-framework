package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

public class WithdrawNotify extends BaseApi {
    String request = "{\n" +
            "  \"request\": {\n" +
            "    \"head\": {\n" +
            "      \"version\": \"1.1.4\",\n" +
            "      \"function\": \"alipayplus.fund.merchant.withdrawNotify\",\n" +
            "      \"clientId\": \"2016030715243903536806\",\n" +
            "      \"reqTime\": \"2022-01-18T16:48:13+05:30\",\n" +
            "      \"reqMsgId\": \"9dce72ad-21eb-4972-b286-83d59fb34010\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "      \"bankAccountNo\": \"{bankAccountNo}\",\n" +
            "      \"bizType\": \"{OPERATION}\",\n" +
            "      \"bankName\": \"{bankName}\",\n" +
            "      \"createdTime\": \"2022-01-18T16:48:12+05:30\",\n" +
            "      \"extendInfo\": \"{\\\"sourceId\\\":\\\"2022011820121482010100168020372610578\\\",\\\"executeNum\\\":\\\"8\\\",\\\"originalTransId\\\":\\\"2022011820121482010100168020372546001\\\",\\\"bankResultCode\\\":\\\"{RESULT_CODE}\\\",\\\"retryAttempt\\\":\\\"8\\\",\\\"finalExecution\\\":\\\"true\\\"}\",\n" +
            "      \"fundAmount\": {\n" +
            "        \"currency\": \"INR\",\n" +
            "        \"value\": \"{FUNDAMOUNT}\"\n" +
            "      },\n" +
            "      \"fundOrderId\": \"2022011820121482010100168020372610693\",\n" +
            "      \"ifscCode\": \"WALL012345667\",\n" +
            "      \"merchantId\": \"{MERCHANT_TYPE}\",\n" +
            "      \"occurTime\": \"2022-01-18T16:48:13+05:30\",\n" +
            "      \"paymentView\": {\n" +
            "        \"cashierRequestId\": \"16425046921392022011820121482010100168020372610693030201\",\n" +
            "        \"extendInfo\": \"{\\\"topupAndPay\\\":\\\"false\\\",\\\"riskResultInfo\\\":\\\"{\\\\\\\"eventLinkId\\\\\\\":\\\\\\\"47c21e3b2d35ab3da130e6246b25e40e\\\\\\\"}\\\",\\\"paymentStatus\\\":\\\"REFUND\\\"}\",\n" +
            "        \"paidTime\": \"2022-01-18T16:48:12+05:30\",\n" +
            "        \"payOptionInfos\": [\n" +
            "          {\n" +
            "            \"chargeAmount\": {\n" +
            "              \"currency\": \"INR\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            \"extendInfo\": \"{\\\"instId\\\":\\\"PAYTW3IN\\\",\\\"referenceNo\\\":\\\"{ORDER_ID}\\\",\\\"payOption\\\":\\\"BALANCE\\\"}\",\n" +
            "            \"payAmount\": {\n" +
            "              \"currency\": \"INR\",\n" +
            "              \"value\": \"5000\"\n" +
            "            },\n" +
            "            \"payMethod\": \"BALANCE\",\n" +
            "            \"payOptionBillExtendInfo\": \"{}\",\n" +
            "            \"transAmount\": {\n" +
            "              \"currency\": \"INR\",\n" +
            "              \"value\": \"5000\"\n" +
            "            }\n" +
            "          }\n" +
            "        ],\n" +
            "        \"payRequestExtendInfo\": \"{\\\"sourceId\\\":\\\"2022011820121482010100168020372610578\\\",\\\"executeNum\\\":\\\"8\\\",\\\"originalTransId\\\":\\\"2022011820121482010100168020372546001\\\",\\\"retryAttempt\\\":\\\"8\\\",\\\"finalExecution\\\":\\\"true\\\"}\"\n" +
            "      },\n" +
            "      \"requestId\": \"T2022011820121482010100168020372610578\",\n" +
            "      \"withdrawResult\": {\n" +
            "        \"resultCode\": \"PROCESS_FAIL\",\n" +
            "        \"resultCodeId\": \"12007212\",\n" +
            "        \"resultMsg\": \"process fail\",\n" +
            "        \"resultStatus\": \"F\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"signature\": \"a75de9656735cfb84bf372456d6aa71daf4637e4ca270a6ec4f6f7c2686f7e9d\"\n" +
            "}";


    public WithdrawNotify(Builder builder)
    {
        this.fundAmount=builder.fundAmount;
        this.merchantId = builder.merchantId;
        this.instErrorCode = builder.instErrorCode;
        this.operation = builder.operation;
        this.orderId = builder.orderId;
        this.accountNo = builder.accountNo;
        this.bankName = builder.bankName;

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.WITHDRAW_NOTIFY);

        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(merchantId);
        String alipayId = dbResult.get("alipay_merchant_id").toString();

        setRequest(merchantId,orderId,operation,instErrorCode,accountNo,bankName);
        getRequestSpecBuilder().setBody(request);

    }


    public String getFundAmount()
    {
        return fundAmount;

    }

    public void setFundAmount(String fundAmount)
    {
        this.fundAmount = fundAmount;
    }


    public String getMerchant()
    {
        return merchantId;

    }

    public void setMerchant(String merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getOrderId()
    {
        return orderId;

    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getOperation()
    {
        return operation;

    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }


    public String getInstErrorCode()
    {
        return instErrorCode;

    }

    public void setInstErrorCode(String instErrorCode)
    {
        this.instErrorCode = instErrorCode;
    }


    public String getBankAccountNo()
    {
        return accountNo;

    }

    public void setBankAccountNo(String bankAccountNo)
    {
        this.accountNo = bankAccountNo;
    }

    public String getBankName()
    {
        return bankName;

    }

    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }


    public String getRequest()
    {
        return request;
    }

    public void setRequest(String alipayMerchant,String orderId, String bizType,String bankResultCode,String accountNo,String bankName)
    {
        this.request = request
                .replace("{FUNDAMOUNT}",fundAmount)
                .replace("{MERCHANT_TYPE}",alipayMerchant)
                .replace("{ORDER_ID}",orderId)
                .replace("{OPERATION}",bizType)
                .replace("{RESULT_CODE}",bankResultCode)
                .replace("{bankAccountNo}",accountNo)
                .replace("{bankName}",bankName);
    }

    private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    public static class Builder{

        String fundAmount = "10000";
        String merchantId = "";
        String orderId = "";
        String operation = "MERCHANT_WITHDRAW";
        String instErrorCode = "FGW_ACCOUNT_CLOSED";
        String accountNo = "123456";
        String bankName = "ICICI bank";

        public Builder(String merchantId,String orderId)
        {
            this.merchantId = merchantId;
            this.orderId = orderId;
        }

        public String getFundAmount()
        {
            return fundAmount;

        }

        public Builder setFundAmount(String fundAmount)
        {
            this.fundAmount = fundAmount;
            return this;
        }


        public String getMerchant()
        {
            return merchantId;

        }

        public Builder setMerchant(String merchantId)
        {
            this.merchantId = merchantId;
            return this;
        }

        public String getOrderId()
        {
            return orderId;

        }

        public Builder setOrderId(String orderId)
        {
            this.orderId = orderId;
            return this;
        }

        public String getOperation()
        {
            return operation;

        }

        public Builder setOperation(String operation)
        {
            this.operation = operation;
            return this;
        }


        public String getInstErrorCode()
        {
            return instErrorCode;

        }

        public Builder setInstErrorCode(String instErrorCode)
        {
            this.instErrorCode = instErrorCode;
            return this;
        }


        public String getAccount()
        {
            return accountNo;

        }

        public Builder setAccount(String accountNo)
        {
            this.accountNo = accountNo;
            return this;
        }

        public String getBankName()
        {
            return bankName;

        }

        public Builder setBankName(String bankName)
        {
            this.bankName = bankName;
            return this;
        }

        public WithdrawNotify build() {
            return new WithdrawNotify(this);
        }

    }


    String fundAmount;
    String merchantId;
    String orderId;
    String operation;
    String instErrorCode;
    String accountNo;
    String bankName;


}

