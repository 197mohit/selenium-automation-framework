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

public class WithdrawNotifyWalletSettlement extends BaseApi {


    String fundAmount;
    String merchantId;
    String orderId;
    String operation;
    String instErrorCode;
    String accountNo;
    String bankName;
    String ifscCode;
    String resultCode;
    String resultId;
    String resultMsg;
    String resultStatus;

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"function\": \"alipayplus.fund.merchant.withdrawNotify\",\n" +
            "            \"clientId\": \"2016030715243903536806\",\n" +
            "            \"reqTime\": \"2021-02-26T11:10:56+05:30\",\n" +
            "            \"reqMsgId\": \"525a361d-53e5-4a00-9b3f-a7ed99c0424f\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"extendInfo\": \"{\\\"merchantAccount\\\":\\\"null\\\",\\\"merchantName\\\":\\\"rAvi\\\",\\\"bankResultCode\\\":\\\"DEFAULT\\\"}\",\n" +
            "            \"fundAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"{FUNDAMOUNT}\"\n" +
            "            },\n" +
            "            \"rrnCode\": \"23423\",\n" +
            "            \"referenceNo\": \"{ORDER_ID}\",\n" +
            "            \"bizType\": \"{OPERATION}\",\n" +
            "            \"createdTime\": \"2019-02-26T11:10:53+05:30\",\n" +
            "            \"bankAccountNo\": \"{bankAccountNo}\",\n" +
            "            \"bankName\": \"{bankName}\",\n" +
            "            \"ifscCode\": \"{ifscCode}\",\n" +
            "            \"fundOrderId\": \"2019080520121482010100168531300515019\",\n" +
            "            \"merchantId\": \"{MERCHANT_TYPE}\",\n" +
            "            \"occurTime\": \"2021-08-05T14:38:09+05:30\",\n" +
            "            \"paymentView\": {\n" +
            "                \"cashierRequestId\": \"15511596532992019022620121482010100168819311479892938101\",\n" +
            "                \"extendInfo\": \"{\\\"topupAndPay\\\":\\\"false\\\",\\\"bankReferenceNo\\\":\\\"905711642201\\\",\\\"paymentStatus\\\":\\\"SUCCESS\\\"}\",\n" +
            "                \"paidTime\": \"2019-02-26T11:10:53+05:30\",\n" +
            "                \"payOptionInfos\": [\n" +
            "                    {\n" +
            "                        \"chargeAmount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"0\"\n" +
            "                        },\n" +
            "                        \"extendInfo\": \"{\\\"referenceNo\\\":\\\"2019022611121482021688155950942\\\"}\",\n" +
            "                        \"payAmount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"56300\"\n" +
            "                        },\n" +
            "                        \"payMethod\": \"BALANCE\",\n" +
            "                        \"payOptionBillExtendInfo\": \"{}\",\n" +
            "                        \"transAmount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"56300\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"payRequestExtendInfo\": \"{\\\"merchantAccount\\\":\\\"null\\\",\\\"merchantName\\\":\\\"rAvi\\\"}\"\n" +
            "            },\n" +
            "            \"requestId\": \"UMP-M2B-432584146-38096545-20190226111053271\",\n" +
            "             \"withdrawResult\": {\n" +
            "                \"resultCode\": \"{RESULT_CODE}\",\n" +
            "                \"resultCodeId\": \"{CODE_ID}\",\n" +
            "                \"resultMsg\": \"{RESULT_MSG}\",\n" +
            "                \"resultStatus\": \"{RESULT_STATUS}\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"e3c98ac68bcf25ff83d8a1735fd32e614eb8a3287bbc69c18c97a8457db775f2\"\n" +
            "}";


    public WithdrawNotifyWalletSettlement(Builder builder)
    {
        this.fundAmount=builder.fundAmount;
        this.merchantId = builder.merchantId;
        this.instErrorCode = builder.instErrorCode;
        this.operation = builder.operation;
        this.orderId = builder.orderId;
        this.accountNo = builder.accountNo;
        this.bankName = builder.bankName;
        this.ifscCode = builder.ifscCode;
        this.resultCode = builder.resultCode;
        this.resultId = builder.resultId;
        this.resultMsg = builder.resultMsg;
        this.resultStatus = builder.resultStatus;

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.WITHDRAW_NOTIFY);

        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(merchantId);
        String alipayId = dbResult.get("alipay_merchant_id").toString();

        setRequest(merchantId,orderId,operation,accountNo,bankName);
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

    public void setRequest(String alipayMerchant,String orderId, String bizType,String accountNo,String bankName)
    {
        this.request = request
                .replace("{FUNDAMOUNT}",fundAmount)
                .replace("{MERCHANT_TYPE}",alipayMerchant)
                .replace("{ORDER_ID}",orderId)
                .replace("{OPERATION}",bizType)
                .replace("{RESULT_CODE}",resultCode)
                .replace("{bankAccountNo}",accountNo)
                .replace("{bankName}",bankName)
                .replace("{ifscCode}",ifscCode)
                .replace("{CODE_ID}",resultId)
                .replace("{RESULT_MSG}",resultMsg)
                .replace("{RESULT_STATUS}",resultStatus);
    }

    private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    public static class Builder {

        String fundAmount = "10000";
        String merchantId = "";
        String orderId = "";
        String operation = "MERCHANT_WITHDRAW";
        String instErrorCode = "FGW_ACCOUNT_CLOSED";
        String accountNo = "123456";
        String bankName = "ICICI bank";
        String ifscCode = "";
        String resultCode="";
        String resultId="";
        String resultMsg="";
        String resultStatus="";

        public Builder(String merchantId, String orderId) {
            this.merchantId = merchantId;
            this.orderId = orderId;
        }

        public String getFundAmount() {
            return fundAmount;

        }

        public WithdrawNotifyWalletSettlement.Builder setFundAmount(String fundAmount) {
            this.fundAmount = fundAmount;
            return this;
        }


        public String getMerchant() {
            return merchantId;

        }

        public WithdrawNotifyWalletSettlement.Builder setMerchant(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public String getOrderId() {
            return orderId;

        }

        public WithdrawNotifyWalletSettlement.Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public String getOperation() {
            return operation;

        }

        public WithdrawNotifyWalletSettlement.Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }


        public String getInstErrorCode() {
            return instErrorCode;

        }

        public WithdrawNotifyWalletSettlement.Builder setInstErrorCode(String instErrorCode) {
            this.instErrorCode = instErrorCode;
            return this;
        }


        public String getAccount() {
            return accountNo;

        }

        public WithdrawNotifyWalletSettlement.Builder setAccount(String accountNo) {
            this.accountNo = accountNo;
            return this;
        }

        public String getBankName() {
            return bankName;

        }

        public WithdrawNotifyWalletSettlement.Builder setBankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        public String getIfscCode() {
            return ifscCode;

        }

        public WithdrawNotifyWalletSettlement.Builder setIfscCode(String IfscCode) {
            this.ifscCode = IfscCode;
            return this;
        }

        public String getResultCode() {
            return resultCode;

        }

        public WithdrawNotifyWalletSettlement.Builder setResultCode(String resultCode) {
            this.resultCode = resultCode;
            return this;
        }
        public String getResultId() {
            return resultId;

        }

        public WithdrawNotifyWalletSettlement.Builder setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
            return this;
        }
        public String getResultMsg() {
            return resultMsg;

        }

        public WithdrawNotifyWalletSettlement.Builder setResultId(String resultId) {
            this.resultId = resultId;
            return this;
        }


        public String getResultStatus() {
            return resultStatus;

        }

        public WithdrawNotifyWalletSettlement.Builder setResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
            return this;
        }

        public WithdrawNotifyWalletSettlement build() {
            return new WithdrawNotifyWalletSettlement(this);
        }

    }
}
