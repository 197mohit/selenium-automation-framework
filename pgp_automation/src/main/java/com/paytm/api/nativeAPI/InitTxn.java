package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;

public class InitTxn extends BaseApi {

    private static final String LINK_BASED_PAYMENT = "LINK_BASED_PAYMENT";
    private static final String LINK_BASED_PAYMENT_INVOICE = "LINK_BASED_PAYMENT_INVOICE";
    private static final String NATIVE_MF = "NATIVE_MF";
    private static final String NATIVE_ST = "NATIVE_ST";
    private InitTxnDTO initTxnDTO;

    public static BaseApi LinkBasedPaymentNativeMF(String mId, String mKey, String orderId, String linkId, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT, NATIVE_MF, linkId, null, accountNumber, validateAccountNumber, allowUnverifiedAccount);
    }

    public static BaseApi LinkBasedPaymentNativeMF(String mId, String mKey, String orderId, String linkId, String accountNumber) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT, NATIVE_MF, linkId, null, accountNumber, "true", "true");
    }

    public static BaseApi LinkBasedPaymentNativeST(String mId, String mKey, String orderId, String linkId, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT, NATIVE_ST, linkId, null, accountNumber, validateAccountNumber, allowUnverifiedAccount);
    }

    public static BaseApi LinkBasedPaymentNativeST(String mId, String mKey, String orderId, String linkId, String accountNumber) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT, NATIVE_ST, linkId, null, accountNumber, "true", "true");
    }

    public static BaseApi LinkBasedPaymentInvoiceNativeMF(String mId, String mKey, String orderId, String invoiceId, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT_INVOICE, NATIVE_MF, null, invoiceId, accountNumber, validateAccountNumber, allowUnverifiedAccount);
    }

    public static BaseApi LinkBasedPaymentInvoiceNativeMF(String mId, String mKey, String orderId, String invoiceId, String accountNumber) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT_INVOICE, NATIVE_MF, null, invoiceId, accountNumber, "true", "true");
    }

    public static BaseApi LinkBasedPaymentInvoiceNativeST(String mId, String mKey, String orderId, String invoiceId, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT_INVOICE, NATIVE_ST, null, invoiceId, accountNumber, validateAccountNumber, allowUnverifiedAccount);
    }

    public static BaseApi LinkBasedPaymentInvoiceNativeST(String mId, String mKey, String orderId, String invoiceId, String accountNumber) {
        return LinkPayment(mId, mKey, orderId, LINK_BASED_PAYMENT_INVOICE, NATIVE_ST, null, invoiceId, accountNumber, "true", "true");
    }

    public static BaseApi LinkBasedPaymentNativeST(String mId,String mKey, String orderId, String requestType,String accountNumber, String amount) {
        return LinkPayment(requestType, mId, mKey, orderId, NATIVE_ST,accountNumber, "true", "true");
    }
    private static BaseApi LinkPayment(String requestType,String mId,String mKey, String orderId,String subRequestType, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount){

        InitTxnDTO dto = new InitTxnDTO();
        Body body = new Body();
        body.setMid(mId);
        body.setOrderId(orderId);
        body.setWebsiteName("retail");
        body.setTxnAmount(new TxnAmount("1"));
        body.setUserInfo(new UserInfo());
        body.setRequestType(requestType);
        body.setAccountNumber(accountNumber);
        body.setDisablePaymentMode(null);
        body.setEnablePaymentMode(null);
        body.setAdditionalInfo(new AdditionalInfo());


        LinkDetails linkDetails = new LinkDetails();

        linkDetails.setAmount(1);
        linkDetails.setLinkDescription("Link10DEPLNK1598877077769");
        linkDetails.setLinkNotes("null");
        linkDetails.setPaymentFormId("5");

        LinkPaymentRiskInfo linkPaymentRiskInfo = new LinkPaymentRiskInfo();
        linkPaymentRiskInfo.setLinkName("LinkName10DEPLNK1598877077769");
        linkPaymentRiskInfo.setLinkId("155924583");
        linkPaymentRiskInfo.setLinkDescription("Link10DEPLNK1598877077769");
        linkPaymentRiskInfo.setLinkAmount("100");
        linkPaymentRiskInfo.setLinkType("FIXED");
        linkPaymentRiskInfo.setMerchantLimit(4);
        linkPaymentRiskInfo.setLinkCreationTime(1598877078);
        linkPaymentRiskInfo.setLinkOpenTime("2020-08-31 18:30:31");
        linkPaymentRiskInfo.setLinkPaymentRequest(true);
        linkPaymentRiskInfo.setRequestType("LinkType");
        linkDetails.setLinkPaymentRiskInfo(linkPaymentRiskInfo);

        PaymentFormDetails paymentFormDetails = new PaymentFormDetails();
        paymentFormDetails.setCustomerName("Akshaya Bangar");
        paymentFormDetails.setMobileNo("08082722954");
        paymentFormDetails.setEmailId("akshayabangar@gmail.com");
        paymentFormDetails.setTxnAmount(null);
        paymentFormDetails.setSkipLoginEnabled(false);

        linkDetails.setPaymentFormDetails(paymentFormDetails);

        linkDetails.setSubRequestType("LINK_BASED_PAYMENT");
        linkDetails.setLinkId("155924583");
        linkDetails.setLongUrl("long url");
        linkDetails.setShortUrl("short url");
        linkDetails.setLinkName("link name");
        linkDetails.setResellerId("null");
        linkDetails.setResellerName("null");


        body.setLinkDetails(linkDetails);

        Head head = new Head();
        head.setVersion("v1");
        head.setChannelId("WEB");
        head.setRequestTimestamp("");
        head.setClientId("C11");
        head.setSignature(PGPHelpers.getNativeChecksum(mKey, dto.getBody()));

        dto.setHead(head);
        dto.setBody(body);

        return new InitTxn(dto);
    }

    private static BaseApi LinkPayment(String mId, String mKey, String orderId, String requestType, String subRequestType, String linkId, String invoiceId, String accountNumber, String validateAccountNumber, String allowUnverifiedAccount) {
        InitTxnDTO dto = new InitTxnDTO();

        Body body = new Body();
        body.setMid(mId);
        body.setOrderId(orderId);
        body.setWebsiteName("retail");
        body.setTxnAmount(new TxnAmount("2"));
        body.setUserInfo(new UserInfo());
        body.setRequestType(requestType);
        body.setBankAccountNumbers(Collections.singletonList("123123123123"));
        body.setAccountNumber(accountNumber);
        body.setValidateAccountNumber(validateAccountNumber);
        body.setAllowUnverifiedAccount(allowUnverifiedAccount);
        body.setAdditionalInfo(new AdditionalInfo());

        LinkDetails linkDetails = new LinkDetails();
        linkDetails.setLinkId(linkId);
        linkDetails.setLinkName("qwerty");
        linkDetails.setLinkDescription("qwerty");
        linkDetails.setShortUrl("123");
        linkDetails.setLongUrl("123");
        linkDetails.setSubRequestType(subRequestType);
        linkDetails.setAmount(2);
        linkDetails.setInvoiceId(invoiceId);

        body.setLinkDetails(linkDetails);

        dto.setBody(body);

        Head head = new Head();
        head.setVersion("v1");
        head.setChannelId("WEB");
        head.setSignature(PGPHelpers.getNativeChecksum(mKey, dto.getBody()));

        dto.setHead(head);

        return new InitTxn(dto);
    }

    public InitTxn(String mId, String mKey, String orderId, double txnAmt, String ssoToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
        getRequestSpecBuilder().addQueryParam("mid", mId);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        String body = "{\"head\":{\"signature\":\"?\"},\"body\":{\"requestType\":\"NATIVE\",\"mid\":\"" + mId + "\",\"orderId\":\"" + orderId + "\",\"websiteName\":\"retail\",\"txnAmount\":{\"currency\":\"INR\",\"value\":\"" + txnAmt + "\"},\"userInfo\":{\"custId\":\"" + UUID.randomUUID() + "\"},\"paytmSsoToken\":\"" + ssoToken + "\"}}";
        Map map = (Map) new JsonSlurper().parseText(body);
        ((Map) map.get("head")).put("signature", PGPUtil.getChecksum(mKey, JsonOutput.toJson(map.get("body"))));
        getRequestSpecBuilder().setBody(map);
    }

    public InitTxn(String mid, String orderId, InitTxnDTO initTxnDTO) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(initTxnDTO);
        this.initTxnDTO=initTxnDTO;
    }

    public InitTxn(InitTxnDTO initTxnDTO) {
        this(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
    }

    public static InitTxnResponseDTO executeInitTxn(String mid, String orderId, InitTxnDTO initTxnDTO) {
        Response response = new InitTxn(mid, orderId, initTxnDTO).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: " + statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        InitTxnResponseDTO initTxnResponseDTO = null;
        try {
            initTxnResponseDTO = mapper.readValue(jsonObject.toJSONString(), InitTxnResponseDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in InitTxnResponse Json", e);
        }
        return initTxnResponseDTO;
    }

    public static InitTxnResponseDTO executeInitTxn(InitTxnDTO initTxnDTO) {
        Response response = new InitTxn(initTxnDTO).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: " + statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        InitTxnResponseDTO initTxnResponseDTO = null;
        try {
            initTxnResponseDTO = mapper.readValue(jsonObject.toJSONString(), InitTxnResponseDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in InitTxnResponse Json", e);
        }
        return initTxnResponseDTO;
    }


    public Response execute(Boolean generateNewChecksum) {
        ObjectMapper objectMapper= new ObjectMapper();
        Map<String, Object> map= objectMapper.convertValue(getRequestBody(), Map.class);

        try {
            JsonPath jsonPath= new JsonPath(objectMapper.writeValueAsString(getRequestBody()));
            String midValue= jsonPath.get("body.mid");

            Constants.MerchantType enumvalue= Constants.MerchantType.getByMid(midValue);
            String mkey= enumvalue.getKey();

            String checksum = PGPHelpers.getNativeChecksum(mkey, map.get("body"));
            setContext("head.signature", checksum);


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return super.execute();
    }
}

