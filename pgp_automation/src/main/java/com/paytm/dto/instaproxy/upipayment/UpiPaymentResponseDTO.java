package com.paytm.dto.instaproxy.upipayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

// AI-Generated: 2026-04-10 - DTO: Instaproxy UPI payment request.htm response body
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiPaymentResponseDTO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @JsonProperty("version")
    private String version;
    @JsonProperty("function")
    private String function;
    @JsonProperty("appId")
    private String appId;
    @JsonProperty("respTime")
    private String respTime;
    @JsonProperty("extSerialNo")
    private String extSerialNo;
    @JsonProperty("mbid")
    private String mbid;
    @JsonProperty("payeeVpa")
    private String payeeVpa;
    @JsonProperty("resultInfo")
    private UpiPaymentResponseResultInfo resultInfo;
    @JsonProperty("custId")
    private String custId;

    public static UpiPaymentResponseDTO fromJson(String json) throws IOException {
        return MAPPER.readValue(json, UpiPaymentResponseDTO.class);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRespTime() {
        return respTime;
    }

    public void setRespTime(String respTime) {
        this.respTime = respTime;
    }

    public String getExtSerialNo() {
        return extSerialNo;
    }

    public void setExtSerialNo(String extSerialNo) {
        this.extSerialNo = extSerialNo;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getPayeeVpa() {
        return payeeVpa;
    }

    public void setPayeeVpa(String payeeVpa) {
        this.payeeVpa = payeeVpa;
    }

    public UpiPaymentResponseResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(UpiPaymentResponseResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }
}
