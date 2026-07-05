package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultInfo",
        "bankForm",
        "deepLinkInfo",
        "txnInfo",
        "callBackUrl",
        "upiLiteResponseData",
})
public class Body {

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("bankForm")
    private BankForm bankForm;
    @JsonProperty("deepLinkInfo")
    private DeepLinkInfo deepLinkInfo;
    @JsonProperty("txnInfo")
    private TxnInfo txnInfo;
    @JsonProperty("callBackUrl")
    private String callBackUrl;
    @JsonProperty("upiLiteResponseData")
    private UpiLiteResponseData upiLiteResponseData;

    @JsonProperty("txnInfo")
    public TxnInfo getTxnInfo() {
        return txnInfo;
    }

    @JsonProperty("txnInfo")
    public void setTxnInfo(TxnInfo txnInfo) {
        this.txnInfo = txnInfo;
    }

    @JsonProperty("callBackUrl")
    public String getCallBackUrl() {
        return callBackUrl;
    }

    @JsonProperty("callBackUrl")
    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("deepLinkInfo")
    public DeepLinkInfo getDeepLinkInfo() {
        return deepLinkInfo;
    }

    @JsonProperty("deepLinkInfo")
    public void setDeepLinkInfo(DeepLinkInfo deepLinkInfo) {
        this.deepLinkInfo = deepLinkInfo;
    }

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @JsonProperty("bankForm")
    public BankForm getBankForm() {
        return bankForm;
    }

    @JsonProperty("bankForm")
    public void setBankForm(BankForm bankForm) {
        this.bankForm = bankForm;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("upiLiteResponseData")
    public UpiLiteResponseData getUpiLiteResponseData() {
        return upiLiteResponseData;
    }

    @JsonProperty("upiLiteResponseData")
    public void setUpiLiteResponseData(UpiLiteResponseData upiLiteResponseData) {
        this.upiLiteResponseData = upiLiteResponseData;
    }

}