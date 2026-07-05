package com.paytm.dto.NativeDTO.getEMIDetails.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "resultStatus",
            "resultCode",
            "resultMsg"
    })
    public class ResultInfo {

        @JsonProperty("resultStatus")
        private String resultStatus;
        @JsonProperty("resultCode")
        private String resultCode;
        @JsonProperty("resultMsg")
        private String resultMsg;

        @JsonProperty("resultStatus")
        public String getResultStatus() {
            return resultStatus;
        }

        @JsonProperty("resultStatus")
        public void setResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
        }

        public ResultInfo withResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
            return this;
        }

        @JsonProperty("resultCode")
        public String getResultCode() {
            return resultCode;
        }

        @JsonProperty("resultCode")
        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public ResultInfo withResultCode(String resultCode) {
            this.resultCode = resultCode;
            return this;
        }

        @JsonProperty("resultMsg")
        public String getResultMsg() {
            return resultMsg;
        }

        @JsonProperty("resultMsg")
        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public ResultInfo withResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
            return this;
        }

    }

