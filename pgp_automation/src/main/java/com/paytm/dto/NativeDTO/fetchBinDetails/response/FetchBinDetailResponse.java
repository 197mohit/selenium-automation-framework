package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 23/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchBinDetailResponse {
    private Head head;
    private Body body;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}
