package com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.extendedInfoDTO;

public class ResultInfo {
    private String resultCode;
    private String resultStatus;
    private String messaage;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public String getMessaage() {
        return messaage;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public void setMessaage(String messaage) {
        this.messaage = messaage;
    }
}
