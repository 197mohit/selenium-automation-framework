package com.paytm.api.refund;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.IsEqual;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.Refund.ASYNC_REFUND;
import static com.paytm.appconstants.Constants.Refund.SYNC_REFUND;
import static org.hamcrest.Matchers.*;

public class SyncRefund extends BaseApi {

    String OrderId = "";
    String mid = "";

    public String request = "{\n" +
            "   \"body\": {\n" +
            "      \"mid\": \"{MID}\",\n" +
            "      \"txnType\": \"REFUND\",\n" +
            "      \"orderId\": \"{ORDER_ID}\",\n" +
            "      \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "      \"refId\": \"{ref_id}\",\n" +
            "      \"refundAmount\": \"{TRANSACTION_AMOUNT}\"\n" +
            "   },\n" +
            "   \"head\": {\n" +
            "      \"signature\": \"{SIGNATURE}\"\n" +
            "   }\n" +
            "}";

    public String body = "{\n" +
            "      \"mid\": \"{MID}\",\n" +
            "      \"txnType\": \"REFUND\",\n" +
            "      \"orderId\": \"{ORDER_ID}\",\n" +
            "      \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "      \"refId\": \"{ref_id}\",\n" +
            "      \"refundAmount\": \"{TRANSACTION_AMOUNT}\"\n" +
            "   }";

    public String requestWithJWT = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"client123\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"token\": \"{JWT}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"refId\": \"{ref_id}\",\n" +
            "        \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "        \"refundAmount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"txnType\": \"REFUND\",\n" +
            "        \"disableMerchantDebitRetry\": true,\n" +
            "        \"comments\": \"InitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiate1235\",\n" +
            "        \"agentInfo\": {\n" +
            "            \"employeeId\": \"ASQERDFDSFDFDSFDSFDFDASQERDFDSFDFDSFDSFDFDF\",\n" +
            "            \"name\": \"1234567890\",\n" +
            "            \"phoneNo\": \"87653578996213214353252\",\n" +
            "            \"email\": \"xyz\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public String bodyForRefundRefactoring = "{\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"refId\": \"{ref_id}\",\n" +
            "        \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "        \"refundAmount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"txnType\": \"REFUND\",\n" +
            "        \"disableMerchantDebitRetry\": true,\n" +
            "        \"comments\": \"InitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiateRefundInitiateInitiate1235\",\n" +
            "        \"agentInfo\": {\n" +
            "            \"employeeId\": \"ASQERDFDSFDFDSFDSFDFDASQERDFDSFDFDSFDSFDFDF\",\n" +
            "            \"name\": \"1234567890\",\n" +
            "            \"phoneNo\": \"87653578996213214353252\",\n" +
            "            \"email\": \"xyz\"\n" +
            "        }\n" +
            "    }";

    public String requestRefundInitiator = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"vtyagi41094\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"signature\": \"{SIGNATURE}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"refId\": \"{ref_id}\",\n" +
            "        \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "        \"refundAmount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"txnType\": \"REFUND\",\n" +
            "        \"comments\": \"InitiateRefund\",\n" +
            "        \"agentInfo\": {\n" +
            "        \"name\": \"Vaibhav Tyagi\",\n" +
            "        \"employeeId\": \"29628\",\n" +
            "        \"phoneNo\": \"8373967470\",\n" +
            "        \"email\": \"vaibhav3.tyagi@paytm.com\"\n" +
            "    }\n" +
            "    }\n" +
            "}";

    public String agentInfoBody = "{\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"refId\": \"{ref_id}\",\n" +
            "        \"txnId\": \"{TRANSACTION_ID}\",\n" +
            "        \"refundAmount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"txnType\": \"REFUND\",\n" +
            "        \"comments\": \"InitiateRefund\",\n" +
            "        \"agentInfo\": {\n" +
            "        \"name\": \"Vaibhav Tyagi\",\n" +
            "        \"employeeId\": \"29628\",\n" +
            "        \"phoneNo\": \"8373967470\",\n" +
            "        \"email\": \"vaibhav3.tyagi@paytm.com\"\n" +
            "    }\n" +
            "    }";


    public String requestRiskInfo = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"signature\": \"{SIGNATURE}\"\n" +
            "    },\n" +
            "    \"body\": {\"mid\":\"{MID}\",\"orderId\":\"{ORDER_ID}\",\"refId\":\"{ref_id}\",\"txnId\":\"{TRANSACTION_ID}\",\"refundAmount\":\"{TRANSACTION_AMOUNT}\",\"txnType\":\"REFUND\",\"comments\":\"InitiateRefund\",\"riskExtendInfo\":{\"verticalId\":\"76\",\"categoryId\":\"262072\",\"subscriberId\":\"bgty\",\"manualRefundFlag\":\"false\",\"udf1\":\"asc\",\"udf2\":\"234\",\"udf3\":\"ghi\",\"udf4\":\"fgh\",\"udf5\":\"123\"},\"envInfo\":{\"clientIp\":\"abc\",\"osType\":\"def\",\"osVersion\":\"os1.2.3\",\"latitude\":\"99.2\",\"longitude\":\"73.2\",\"deviceModel\":\"fghi\",\"deviceIMEI\":\"1234efrd\",\"deviceId\":\"12345\",\"deviceManufacturer\":\"asdf\"},\"agentInfo\":{\"employeeId\":\"emp123\",\"name\":\"abcde\",\"phoneNo\":\"1234567\",\"email\":\"abc.gmail.com\"}}\n" +
            "}";

    public String bodyRiskInfo = "{\"mid\":\"{MID}\",\"orderId\":\"{ORDER_ID}\",\"refId\":\"{ref_id}\",\"txnId\":\"{TRANSACTION_ID}\",\"refundAmount\":\"{TRANSACTION_AMOUNT}\",\"txnType\":\"REFUND\",\"comments\":\"InitiateRefund\",\"riskExtendInfo\":{\"verticalId\":\"76\",\"categoryId\":\"262072\",\"subscriberId\":\"bgty\",\"manualRefundFlag\":\"false\",\"udf1\":\"asc\",\"udf2\":\"234\",\"udf3\":\"ghi\",\"udf4\":\"fgh\",\"udf5\":\"123\"},\"envInfo\":{\"clientIp\":\"abc\",\"osType\":\"def\",\"osVersion\":\"os1.2.3\",\"latitude\":\"99.2\",\"longitude\":\"73.2\",\"deviceModel\":\"fghi\",\"deviceIMEI\":\"1234efrd\",\"deviceId\":\"12345\",\"deviceManufacturer\":\"asdf\"},\"agentInfo\":{\"employeeId\":\"emp123\",\"name\":\"abcde\",\"phoneNo\":\"1234567\",\"email\":\"abc.gmail.com\"}}";
    public static String requestWithItems = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"client123\",\n" +
            "        \"requestTimestamp\": \"1709620041967\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"signature\": \"{SIGNATURE}\"\n" +
            "    },\n" +
            "    \"body\": {\"mid\":\"{MID}\",\"orderId\":\"{ORDER_ID}\",\"refId\":\"{ref_id}\",\"txnId\":\"{TRANSACTION_ID}\",\"refundAmount\":\"{TRANSACTION_AMOUNT}\",\"txnType\":\"REFUND\",\"comments\":\"InitiateRefund\",\"refundItems\":{refund_items}}\n" +
            "}";
    public static String bodyWithItems = "{\"mid\":\"{MID}\",\"orderId\":\"{ORDER_ID}\",\"refId\":\"{ref_id}\",\"txnId\":\"{TRANSACTION_ID}\",\"refundAmount\":\"{TRANSACTION_AMOUNT}\",\"txnType\":\"REFUND\",\"comments\":\"InitiateRefund\",\"refundItems\":{refund_items}}";

    public String checksum = "";

    /**
     * Full JSON for {@code POST /refund/api/v1/async/refund} when the body includes {@code refundItems} (item / product
     * lines). Head matches manual/curl style: empty {@code clientId}, {@code version}, {@code requestTimestamp: Time},
     * checksum signature. Body field order matches common client payloads: {@code refundItems}, {@code orderId},
     * {@code mid}, {@code txnType}, {@code refId}, {@code refundAmount}, {@code txnId}.
     */
    private String requestItemLines = "";

    /**
     * @param refundItemsJsonArray JSON array string, e.g. {@code [{"itemId":"0","productId":"SKU","itemRefundAmount":"200"}]}
     */
    public SyncRefund setRequestWithRefundItemLines(
            Constants.MerchantType mid,
            String orderId,
            String txnId,
            String refId,
            String refundAmount,
            String refundItemsJsonArray) {
        this.checksum = "";
        this.OrderId = orderId;
        this.mid = mid.getId();
        if (StringUtils.isBlank(refId)) {
            refId = "IN" + RandomStringUtils.randomNumeric(13);
        }
        String bodyForSign =
                "{\"refundItems\":"
                        + refundItemsJsonArray
                        + ",\"orderId\":\""
                        + orderId
                        + "\",\"mid\":\""
                        + mid.getId()
                        + "\",\"txnType\":\"REFUND\",\"refId\":\""
                        + refId
                        + "\",\"refundAmount\":\""
                        + refundAmount
                        + "\",\"txnId\":\""
                        + txnId
                        + "\"}";
        String signature = createChecksum(mid.getKey(), bodyForSign);
        requestItemLines =
                "{\n"
                        + "    \"head\": {\n"
                        + "        \"clientId\": \"\",\n"
                        + "        \"version\": \"v1\",\n"
                        + "        \"requestTimestamp\": \"Time\",\n"
                        + "        \"signature\": \""
                        + signature
                        + "\"\n"
                        + "    },\n"
                        + "    \"body\": "
                        + bodyForSign
                        + "\n"
                        + "}";
        return this;
    }

    public String getRequestItemLines() {
        return requestItemLines;
    }

    public RequestSpecification reqSpecAsyncRefundWithRefundItemLines(
            Constants.MerchantType mid,
            String orderId,
            String txnId,
            String refId,
            String refundAmount,
            String refundItemsJsonArray) {
        setRequestWithRefundItemLines(mid, orderId, txnId, refId, refundAmount, refundItemsJsonArray);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBody(getRequestItemLines())
                .setBasePath(ASYNC_REFUND)
                .build();
    }

    private String createChecksum(String merchantKey, String body) {
        try {
            if (StringUtils.isEmpty(checksum))
                checksum= PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getRequest() {
        return request;
    }

    public String getRequestWithJWT() {
        return requestWithJWT;
    }
    public String getRequestWithItems() {
        return requestWithItems;
    }

    public SyncRefund setRequest(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        body = body.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        String Signature = createChecksum(mid.getKey(), body);
        request = request.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{SIGNATURE}", Signature).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        return this;
    }
    public SyncRefund setRequestwithItems(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId, String RefundItems) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        bodyWithItems = bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId).replace("{refund_items}",RefundItems);
        String Signature = createChecksum(mid.getKey(), bodyWithItems);
        requestWithItems = requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{SIGNATURE}", Signature).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId).replace("{refund_items}",RefundItems);
        return this;
    }


    public SyncRefund setRequestForRefundRefactoring(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        bodyForRefundRefactoring = bodyForRefundRefactoring.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", mid.getId());
        tokenMap.put("refId",RefId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        requestWithJWT = requestWithJWT.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId).replace("{JWT}",jwtToken);
        return this;
    }


    public RequestSpecification reqSpec(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId) {
        setRequest(mid, refundAmount, OrderId, txnId);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(SYNC_REFUND)
                .build();

    }

    public RequestSpecification reqSpecAsyncRefund(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId) {
        setRequest(mid, refundAmount, OrderId, txnId);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(ASYNC_REFUND)
                .build();

    }
    public RequestSpecification reqSpecAsyncRefundwithItems(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId, String refundItems) {
        setRequestwithItems(mid, refundAmount, OrderId, txnId, refundItems);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequestWithItems())
                .setBasePath(ASYNC_REFUND)
                .build();
    }
    public SyncRefund(String body){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.ANY);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ASYNC_REFUND);
        getRequestSpecBuilder().setBody(body);
    }
    public SyncRefund(){
        //
    }

    public RequestSpecification reqSpecAsyncRefundtxnType(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId, String txnType) {
        setRequesttxnType(mid, refundAmount, OrderId, txnId, txnType);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(ASYNC_REFUND)
                .build();

    }

    public RequestSpecification reqSpecAsyncRiskInfo(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId) {
        setRequestRiskInfo(mid, refundAmount, OrderId, txnId);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(ASYNC_REFUND)
                .build();

    }

    public RequestSpecification reqSpecAsyncRefundAgentInfo(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId) {
        setRequestagentInfo(mid, refundAmount, OrderId, txnId);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(ASYNC_REFUND)
                .build();

    }

    public SyncRefund setRequesttxnType(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId, String txnType) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        body = body.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId).replace("REFUND", txnType);
        String Signature = createChecksum(mid.getKey(), body);
        request = request.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{SIGNATURE}", Signature).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId).replace("REFUND", txnType);
        return this;
    }

    public SyncRefund setRequestRiskInfo(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        bodyRiskInfo = bodyRiskInfo.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        String Signature = createChecksum(mid.getKey(), bodyRiskInfo);
        request = requestRiskInfo.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{SIGNATURE}", Signature).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        return this;
    }

    public SyncRefund setRequestagentInfo(Constants.MerchantType mid, String txnAmount, String OrderId, String TxnId) {
        this.OrderId = OrderId;
        this.mid = mid.getId();
        String RefId = "refundNewAou" + RandomStringUtils.randomNumeric(13);
        agentInfoBody = agentInfoBody.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        String Signature = createChecksum(mid.getKey(), agentInfoBody);
        request = requestRefundInitiator.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{SIGNATURE}", Signature).replace("{TRANSACTION_ID}", TxnId).replace("{ref_id}", RefId);
        return this;
    }

    public RequestSpecification reqSpecForRefundRefactoring(Constants.MerchantType mid, String refundAmount, String OrderId, String txnId) {
        setRequestForRefundRefactoring(mid, refundAmount, OrderId, txnId);
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequestWithJWT())
                .setBasePath(SYNC_REFUND)
                .build();

    }

    public ResponseSpecification resultSchema(){
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", IsEqual.equalTo("TXN_SUCCESS"))
                .expectBody("resultCode", IsEqual.equalTo("10"))
                .expectBody("resultMsg", IsEqual.equalTo("Refund Successfull"))
                .build();
    }


    public ResponseSpecification refundDetailSchema() {
        return new ResponseSpecBuilder()
                .rootPath("body")
                .expectBody("orderId", equalTo(this.OrderId))
                .expectBody("userCreditInitiateStatus", equalTo("SUCCESS"))
                .expectBody("mid", equalTo(this.mid))
                .expectBody("refundDetailInfoList.refundType", hasItem("TO_SOURCE"))
                .expectBody("refundDetailInfoList.issuingBankName", hasItem(allOf(isA(String.class), notNullValue())))
                .expectBody("refundDetailInfoList.cardScheme", hasItem(allOf(isA(String.class), notNullValue())))
                .expectBody("refundDetailInfoList.payMethod", hasItem(allOf(isA(String.class), notNullValue())))
                .expectBody("refundDetailInfoList.userCreditExpectedDate", hasItem(allOf(isA(String.class), notNullValue())))
                .expectBody("refundDetailInfoList.rrn", hasItem(allOf(isA(String.class), notNullValue())))
                .expectBody("refundDetailInfoList.refundAmount", hasItem(allOf(isA(String.class), notNullValue())))
                .build();
    }
}
