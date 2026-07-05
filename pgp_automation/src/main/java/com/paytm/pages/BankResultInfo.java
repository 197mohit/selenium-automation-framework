package com.paytm.pages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "bankResultCode",
            "bankResultMsg"
    })
    public class BankResultInfo {

        @JsonProperty("bankResultCode")
        private String bankResultCode;
        @JsonProperty("bankResultMsg")
        private String bankResultMsg;

        @JsonProperty("bankResultCode")
        public String getBankResultCode() {
            return bankResultCode;
        }

        @JsonProperty("bankResultCode")
        public void setBankResultCode(String bankResultCode) {
            this.bankResultCode = bankResultCode;
        }

        @JsonProperty("bankResultMsg")
        public String getBankResultMsg() {
            return bankResultMsg;
        }

        @JsonProperty("bankResultMsg")
        public void setBankResultMsg(String bankResultMsg) {
            this.bankResultMsg = bankResultMsg;
        }
}
