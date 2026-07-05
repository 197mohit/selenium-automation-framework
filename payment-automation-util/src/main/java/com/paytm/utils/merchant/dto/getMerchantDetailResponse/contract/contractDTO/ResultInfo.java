package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

public class ResultInfo {

    private String resultCode;
    private String resultCodeId;
    private String resultMsg;
    private String resultStatus;

    public String getResultCode() {
        return resultCode;
    }

    public ResultInfo setResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public ResultInfo setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
        return this;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public ResultInfo setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
        return this;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public ResultInfo setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
        return this;
    }
}
