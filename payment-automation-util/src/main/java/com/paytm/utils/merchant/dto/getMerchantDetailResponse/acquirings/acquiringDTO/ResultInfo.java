package com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.acquiringDTO;

public class ResultInfo {
    private String resultCode;
    private String resultCodeId;
    private String resultMsg;
    private String resultStatus;

    public String getResultCode() {
        return resultCode;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
