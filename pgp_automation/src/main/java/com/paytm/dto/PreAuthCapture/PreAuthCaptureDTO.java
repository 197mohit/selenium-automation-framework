package com.paytm.dto.PreAuthCapture;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.appconstants.Constants;
import com.paytm.dto.OrderDTO;
import com.paytm.utils.merchant.util.PGPUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "OrderId",
        "PREAUTH_ID",
        "ReqType",
        "MID",
        "AppIP",
        "TxnAmount",
        "Currency",
        "DeviceId",
        "SSOToken",
        "PaymentMode",
        "CustId",
        "IndustryType",
        "Channel",
        "AuthMode",
        "CheckSum"
})
public class PreAuthCaptureDTO {

    @JsonProperty("OrderId")
    private String orderId;
    @JsonProperty("PREAUTH_ID")
    private String pREAUTH_ID;
    @JsonProperty("ReqType")
    private String reqType;
    @JsonProperty("MID")
    private String mID;
    @JsonProperty("AppIP")
    private String appIP;
    @JsonProperty("TxnAmount")
    private String txnAmount;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("DeviceId")
    private String deviceId;
    @JsonProperty("SSOToken")
    private String sSOToken;
    @JsonProperty("PaymentMode")
    private String paymentMode;
    @JsonProperty("CustId")
    private String custId;
    @JsonProperty("IndustryType")
    private String industryType;
    @JsonProperty("Channel")
    private String channel;
    @JsonProperty("AuthMode")
    private String authMode;
    @JsonProperty("CheckSum")
    private String checkSum;

    public PreAuthCaptureDTO(String orderId, String pREAUTH_ID, Constants.MerchantType merchant, String txnAmount, String sSOToken){
        TreeMap<String, String> treeMap = new TreeMap<>();
        this.reqType = "CAPTURE";
        treeMap.put("ReqType",this.reqType);
        this.appIP = "127.0.0.1";
        treeMap.put("AppIP",this.appIP);
        this.currency = "INR";
        treeMap.put("Currency",this.currency);
        this.deviceId = "7404186250";
        treeMap.put("DeviceId", this.deviceId);
        this.paymentMode = "PPI";
        treeMap.put("PaymentMode",this.paymentMode);
        this.custId = UUID.randomUUID().toString();
        treeMap.put("CustId", this.custId);
        this.industryType = "Retail1";
        treeMap.put("IndustryType",this.industryType);
        this.channel = "WEB";
        treeMap.put("Channel",this.channel);
        this.authMode = "USRPWD";
        treeMap.put("AuthMode",this.authMode);
        this.orderId = orderId;
        treeMap.put("OrderId",this.orderId);
        this.pREAUTH_ID = pREAUTH_ID;
        treeMap.put("PREAUTH_ID",this.pREAUTH_ID);
        this.mID = merchant.getId();
        treeMap.put("MID", this.mID);
        this.txnAmount = txnAmount;
        treeMap.put("TxnAmount", this.txnAmount);
        this.sSOToken = sSOToken;
        treeMap.put("SSOToken", this.sSOToken);
        this.checkSum = PGPUtil.getChecksum(merchant.getKey(), treeMap);
    }

    public PreAuthCaptureDTO(OrderDTO orderDTO, String pREAUTH_ID, Constants.MerchantType merchant, String sSOToken){
        this(orderDTO.getORDER_ID(), pREAUTH_ID, merchant, orderDTO.getTXN_AMOUNT(), sSOToken);
    }

    @JsonProperty("OrderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("OrderId")
    public PreAuthCaptureDTO setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("PREAUTH_ID")
    public String getPREAUTH_ID() {
        return pREAUTH_ID;
    }

    @JsonProperty("PREAUTH_ID")
    public PreAuthCaptureDTO setPREAUTH_ID(String pREAUTH_ID) {
        this.pREAUTH_ID = pREAUTH_ID;
        return this;
    }

    @JsonProperty("ReqType")
    public String getReqType() {
        return reqType;
    }

    @JsonProperty("ReqType")
    public PreAuthCaptureDTO setReqType(String reqType) {
        this.reqType = reqType;
        return this;
    }

    @JsonProperty("MID")
    public String getMID() {
        return mID;
    }

    @JsonProperty("MID")
    public PreAuthCaptureDTO setMID(String mID) {
        this.mID = mID;
        return this;
    }

    @JsonProperty("AppIP")
    public String getAppIP() {
        return appIP;
    }

    @JsonProperty("AppIP")
    public PreAuthCaptureDTO setAppIP(String appIP) {
        this.appIP = appIP;
        return this;
    }

    @JsonProperty("TxnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("TxnAmount")
    public PreAuthCaptureDTO setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonProperty("Currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("Currency")
    public PreAuthCaptureDTO setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @JsonProperty("DeviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("DeviceId")
    public PreAuthCaptureDTO setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    @JsonProperty("SSOToken")
    public String getSSOToken() {
        return sSOToken;
    }

    @JsonProperty("SSOToken")
    public PreAuthCaptureDTO setSSOToken(String sSOToken) {
        this.sSOToken = sSOToken;
        return this;
    }

    @JsonProperty("PaymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("PaymentMode")
    public PreAuthCaptureDTO setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
        return this;
    }

    @JsonProperty("CustId")
    public String getCustId() {
        return custId;
    }

    @JsonProperty("CustId")
    public PreAuthCaptureDTO setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    @JsonProperty("IndustryType")
    public String getIndustryType() {
        return industryType;
    }

    @JsonProperty("IndustryType")
    public PreAuthCaptureDTO setIndustryType(String industryType) {
        this.industryType = industryType;
        return this;
    }

    @JsonProperty("Channel")
    public String getChannel() {
        return channel;
    }

    @JsonProperty("Channel")
    public PreAuthCaptureDTO setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    @JsonProperty("AuthMode")
    public String getAuthMode() {
        return authMode;
    }

    @JsonProperty("AuthMode")
    public PreAuthCaptureDTO setAuthMode(String authMode) {
        this.authMode = authMode;
        return this;
    }

    @JsonProperty("CheckSum")
    public String getCheckSum() {
        return checkSum;
    }

    @JsonProperty("CheckSum")
    public PreAuthCaptureDTO setCheckSum(String checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    // --- Payment Services JSON (PS_PRE_AUTH / PS_CAPTURE); extend via @JsonAnySetter on nested types ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsPreAuthUserInfoDTO {

        @JsonProperty("custId")
        private String custId;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("custId")
        public String getCustId() {
            return custId;
        }

        @JsonProperty("custId")
        public PsPreAuthUserInfoDTO setCustId(String custId) {
            this.custId = custId;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsPreAuthHeadDTO {

        @JsonProperty("requestTimestamp")
        private String requestTimestamp;
        @JsonProperty("clientId")
        private String clientId;
        @JsonProperty("version")
        private String version;
        @JsonProperty("channelId")
        private String channelId;
        @JsonProperty("signature")
        private String signature;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("requestTimestamp")
        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        @JsonProperty("requestTimestamp")
        public PsPreAuthHeadDTO setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        @JsonProperty("clientId")
        public String getClientId() {
            return clientId;
        }

        @JsonProperty("clientId")
        public PsPreAuthHeadDTO setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        @JsonProperty("version")
        public String getVersion() {
            return version;
        }

        @JsonProperty("version")
        public PsPreAuthHeadDTO setVersion(String version) {
            this.version = version;
            return this;
        }

        @JsonProperty("channelId")
        public String getChannelId() {
            return channelId;
        }

        @JsonProperty("channelId")
        public PsPreAuthHeadDTO setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        @JsonProperty("signature")
        public String getSignature() {
            return signature;
        }

        @JsonProperty("signature")
        public PsPreAuthHeadDTO setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsPreAuthBodyDTO {

        @JsonProperty("mid")
        private String mid;
        @JsonProperty("orderId")
        private String orderId;
        @JsonProperty("txnAmount")
        private Integer txnAmount;
        @JsonProperty("paytmSsoToken")
        private String paytmSsoToken;
        @JsonProperty("preAuthBlockSeconds")
        private Integer preAuthBlockSeconds;
        @JsonProperty("cardPreAuthType")
        private String cardPreAuthType;
        @JsonProperty("paymentMode")
        private String paymentMode;
        @JsonProperty("multiCaptureAllowed")
        private Boolean multiCaptureAllowed;
        @JsonProperty("websiteName")
        private String websiteName;
        @JsonProperty("txnTokenRequired")
        private Boolean txnTokenRequired;
        @JsonProperty("callbackUrl")
        private String callbackUrl;
        @JsonProperty("peonUrl")
        private String peonUrl;
        @JsonProperty("userInfo")
        private PsPreAuthUserInfoDTO userInfo;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("mid")
        public String getMid() {
            return mid;
        }

        @JsonProperty("mid")
        public PsPreAuthBodyDTO setMid(String mid) {
            this.mid = mid;
            return this;
        }

        @JsonProperty("orderId")
        public String getOrderId() {
            return orderId;
        }

        @JsonProperty("orderId")
        public PsPreAuthBodyDTO setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        @JsonProperty("txnAmount")
        public Integer getTxnAmount() {
            return txnAmount;
        }

        @JsonProperty("txnAmount")
        public PsPreAuthBodyDTO setTxnAmount(Integer txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        @JsonProperty("paytmSsoToken")
        public String getPaytmSsoToken() {
            return paytmSsoToken;
        }

        @JsonProperty("paytmSsoToken")
        public PsPreAuthBodyDTO setPaytmSsoToken(String paytmSsoToken) {
            this.paytmSsoToken = paytmSsoToken;
            return this;
        }

        @JsonProperty("preAuthBlockSeconds")
        public Integer getPreAuthBlockSeconds() {
            return preAuthBlockSeconds;
        }

        @JsonProperty("preAuthBlockSeconds")
        public PsPreAuthBodyDTO setPreAuthBlockSeconds(Integer preAuthBlockSeconds) {
            this.preAuthBlockSeconds = preAuthBlockSeconds;
            return this;
        }

        @JsonProperty("cardPreAuthType")
        public String getCardPreAuthType() {
            return cardPreAuthType;
        }

        @JsonProperty("cardPreAuthType")
        public PsPreAuthBodyDTO setCardPreAuthType(String cardPreAuthType) {
            this.cardPreAuthType = cardPreAuthType;
            return this;
        }

        @JsonProperty("paymentMode")
        public String getPaymentMode() {
            return paymentMode;
        }

        @JsonProperty("paymentMode")
        public PsPreAuthBodyDTO setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
            return this;
        }

        @JsonProperty("multiCaptureAllowed")
        public Boolean getMultiCaptureAllowed() {
            return multiCaptureAllowed;
        }

        @JsonProperty("multiCaptureAllowed")
        public PsPreAuthBodyDTO setMultiCaptureAllowed(Boolean multiCaptureAllowed) {
            this.multiCaptureAllowed = multiCaptureAllowed;
            return this;
        }

        @JsonProperty("websiteName")
        public String getWebsiteName() {
            return websiteName;
        }

        @JsonProperty("websiteName")
        public PsPreAuthBodyDTO setWebsiteName(String websiteName) {
            this.websiteName = websiteName;
            return this;
        }

        @JsonProperty("txnTokenRequired")
        public Boolean getTxnTokenRequired() {
            return txnTokenRequired;
        }

        @JsonProperty("txnTokenRequired")
        public PsPreAuthBodyDTO setTxnTokenRequired(Boolean txnTokenRequired) {
            this.txnTokenRequired = txnTokenRequired;
            return this;
        }

        @JsonProperty("callbackUrl")
        public String getCallbackUrl() {
            return callbackUrl;
        }

        @JsonProperty("callbackUrl")
        public PsPreAuthBodyDTO setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        @JsonProperty("peonUrl")
        public String getPeonUrl() {
            return peonUrl;
        }

        @JsonProperty("peonUrl")
        public PsPreAuthBodyDTO setPeonUrl(String peonUrl) {
            this.peonUrl = peonUrl;
            return this;
        }

        @JsonProperty("userInfo")
        public PsPreAuthUserInfoDTO getUserInfo() {
            return userInfo;
        }

        @JsonProperty("userInfo")
        public PsPreAuthBodyDTO setUserInfo(PsPreAuthUserInfoDTO userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsPreAuthRequestDTO {

        @JsonProperty("head")
        private PsPreAuthHeadDTO head;
        @JsonProperty("body")
        private PsPreAuthBodyDTO body;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        public static PsPreAuthRequestDTO withDefaults() {
            PsPreAuthRequestDTO dto = new PsPreAuthRequestDTO();
            PsPreAuthHeadDTO head = new PsPreAuthHeadDTO()
                    .setRequestTimestamp("1539601338741")
                    .setClientId("C11")
                    .setVersion("v2")
                    .setChannelId("WEB")
                    .setSignature("KVQ5YrYS/pcQtZ0gghKLWc=");
            PsPreAuthUserInfoDTO userInfo = new PsPreAuthUserInfoDTO().setCustId("1000121170");
            PsPreAuthBodyDTO body = new PsPreAuthBodyDTO()
                    .setMid("qa8cba23134289988250")
                    .setOrderId("T2781211211111122121112111")
                    .setTxnAmount(40)
                    .setPaytmSsoToken("")
                    .setPreAuthBlockSeconds(600)
                    .setCardPreAuthType("STANDARD_AUTH")
                    .setPaymentMode("CREDIT_CARD")
                    .setMultiCaptureAllowed(true)
                    .setWebsiteName("retail")
                    .setTxnTokenRequired(true)
                    .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                    .setPeonUrl("https://automation-pg-ext.paytm.in/mockbank/peon")
                    .setUserInfo(userInfo);
            dto.setHead(head);
            dto.setBody(body);
            return dto;
        }

        @JsonProperty("head")
        public PsPreAuthHeadDTO getHead() {
            return head;
        }

        @JsonProperty("head")
        public PsPreAuthRequestDTO setHead(PsPreAuthHeadDTO head) {
            this.head = head;
            return this;
        }

        @JsonProperty("body")
        public PsPreAuthBodyDTO getBody() {
            return body;
        }

        @JsonProperty("body")
        public PsPreAuthRequestDTO setBody(PsPreAuthBodyDTO body) {
            this.body = body;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsCaptureHeadDTO {

        @JsonProperty("signature")
        private String signature;
        @JsonProperty("clientId")
        private String clientId;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("signature")
        public String getSignature() {
            return signature;
        }

        @JsonProperty("signature")
        public PsCaptureHeadDTO setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        @JsonProperty("clientId")
        public String getClientId() {
            return clientId;
        }

        @JsonProperty("clientId")
        public PsCaptureHeadDTO setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsCaptureBodyDTO {

        @JsonProperty("mid")
        private String mid;
        @JsonProperty("preAuthId")
        private String preAuthId;
        @JsonProperty("txnAmount")
        private String txnAmount;
        @JsonProperty("orderId")
        private String orderId;
        @JsonProperty("payMode")
        private String payMode;
        @JsonProperty("terminalCapture")
        private String terminalCapture;
        @JsonProperty("mercUnqRef")
        private String mercUnqRef;
        @JsonProperty("planId")
        private String planId;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("mid")
        public String getMid() {
            return mid;
        }

        @JsonProperty("mid")
        public PsCaptureBodyDTO setMid(String mid) {
            this.mid = mid;
            return this;
        }

        @JsonProperty("preAuthId")
        public String getPreAuthId() {
            return preAuthId;
        }

        @JsonProperty("preAuthId")
        public PsCaptureBodyDTO setPreAuthId(String preAuthId) {
            this.preAuthId = preAuthId;
            return this;
        }

        @JsonProperty("txnAmount")
        public String getTxnAmount() {
            return txnAmount;
        }

        @JsonProperty("txnAmount")
        public PsCaptureBodyDTO setTxnAmount(String txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        @JsonProperty("orderId")
        public String getOrderId() {
            return orderId;
        }

        @JsonProperty("orderId")
        public PsCaptureBodyDTO setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        @JsonProperty("payMode")
        public String getPayMode() {
            return payMode;
        }

        @JsonProperty("payMode")
        public PsCaptureBodyDTO setPayMode(String payMode) {
            this.payMode = payMode;
            return this;
        }

        @JsonProperty("terminalCapture")
        public String getTerminalCapture() {
            return terminalCapture;
        }

        @JsonProperty("terminalCapture")
        public PsCaptureBodyDTO setTerminalCapture(String terminalCapture) {
            this.terminalCapture = terminalCapture;
            return this;
        }

        @JsonProperty("mercUnqRef")
        public String getMercUnqRef() {
            return mercUnqRef;
        }

        @JsonProperty("mercUnqRef")
        public PsCaptureBodyDTO setMercUnqRef(String mercUnqRef) {
            this.mercUnqRef = mercUnqRef;
            return this;
        }

        @JsonProperty("planId")
        public String getPlanId() {
            return planId;
        }

        @JsonProperty("planId")
        public PsCaptureBodyDTO setPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsCaptureRequestDTO {

        @JsonProperty("body")
        private PsCaptureBodyDTO body;
        @JsonProperty("head")
        private PsCaptureHeadDTO head;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        public static PsCaptureRequestDTO withDefaults() {
            PsCaptureRequestDTO dto = new PsCaptureRequestDTO();
            PsCaptureBodyDTO body = new PsCaptureBodyDTO()
                    .setMid("qa8cba23134289988250")
                    .setPreAuthId("20251006011790000182062885976136475")
                    .setTxnAmount("10")
                    .setOrderId("T278121121111112212")
                    .setPayMode("CREDIT_CARD")
                    .setTerminalCapture("true")
                    .setMercUnqRef("online");
            PsCaptureHeadDTO head = new PsCaptureHeadDTO()
                    .setSignature("BH2BqdbQhxYZQ5kW3AGJdo4KgyJVOuZMA/asdadasd7aCeFeKRbXZwMkFHFcs47AX+HNw3Gzs/4z8gg7BNnGmLtu33QueB/zgw9xZwqb9jOauM=")
                    .setClientId("C11");
            dto.setBody(body);
            dto.setHead(head);
            return dto;
        }

        @JsonProperty("body")
        public PsCaptureBodyDTO getBody() {
            return body;
        }

        @JsonProperty("body")
        public PsCaptureRequestDTO setBody(PsCaptureBodyDTO body) {
            this.body = body;
            return this;
        }

        @JsonProperty("head")
        public PsCaptureHeadDTO getHead() {
            return head;
        }

        @JsonProperty("head")
        public PsCaptureRequestDTO setHead(PsCaptureHeadDTO head) {
            this.head = head;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

}