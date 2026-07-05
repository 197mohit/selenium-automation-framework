package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 23/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasLowSuccessRate {
    private String status;
    private String msg;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
