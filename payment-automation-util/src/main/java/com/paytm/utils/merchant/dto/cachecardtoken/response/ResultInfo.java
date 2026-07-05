package com.paytm.utils.merchant.dto.cachecardtoken.response;

public class ResultInfo {

    private String resultStatus;
    private String resultCode;
    private String resultMsg;


    public String getResultCode() {
        return resultCode;
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

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
