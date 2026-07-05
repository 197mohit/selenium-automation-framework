package com.paytm.dto.instaproxy.upipayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// AI-Generated: 2026-04-10 - DTO: Instaproxy UPI payment response — resultInfo
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiPaymentResponseResultInfo {

    @JsonProperty("resultStatus")
    private String resultStatus;
    @JsonProperty("resultCodeId")
    private String resultCodeId;
    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultMsg")
    private String resultMsg;

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
