package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class SubscriptionDetail {
        private String subsType;
        private String subsId;
        private String renewMessage;
        private String saveCardMandatoryAddNPay;
        private String nonSpecificPayMode;
        private String amount;
        private String infoMessage;
        private String showDetails;
        private String infoMessageList;
        private String isEnabled;
        private Boolean isAutoRefund;

        public String getSubsType() {
            return subsType;
        }

    public void setSubsType(String subsType) {
        this.subsType = subsType;
    }

    public String getSubsId() {
        return subsId;
    }

    public void setSubsId(String subsId) {
        this.subsId = subsId;
    }

    public String getRenewMessage() {
        return renewMessage;
    }

    public void setRenewMessage(String renewMessage) {
        this.renewMessage = renewMessage;
    }

    public String getSaveCardMandatoryAddNPay() {
        return saveCardMandatoryAddNPay;
    }

    public void setSaveCardMandatoryAddNPay(String saveCardMandatoryAddNPay) {
        this.saveCardMandatoryAddNPay = saveCardMandatoryAddNPay;
    }
    public String getNonSpecificPayMode() {
        return nonSpecificPayMode;
    }

    public void setNonSpecificPayMode(String nonSpecificPayMode) {
        this.nonSpecificPayMode = nonSpecificPayMode;
    }
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }
    public String getShowDetails() {
        return showDetails;
    }

    public void setShowDetails(String showDetails) {
        this.showDetails = showDetails;
    }
    public String getInfoMessageList() {
        return infoMessageList;
    }

    public void setInfoMessageList(String infoMessageList) {
        this.infoMessageList = infoMessageList;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }


    public Boolean getIsAutoRefund() {
        return isAutoRefund;
    }

    public void setIsAutoRefund(Boolean isAutoRefund) {
        this.isAutoRefund = isAutoRefund;
    }
}
