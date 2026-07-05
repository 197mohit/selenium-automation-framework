package com.paytm.utils.merchant.dto.refund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.util.PGPUtil;

/**
 * Created by ankuragarwal on 13/12/18
 */
public class AsyncRefundDTO {
    private Body body;
    private Head head;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public AsyncRefundDTO(String merchantKey, Body body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String checksum = PGPUtil.getChecksum(merchantKey, mapper.writeValueAsString(body));
        Head head = new Head()
                .setClientId("")
                .setRequestTimestamp("TIME")
                .setVersion("v1")
                .setSignature(checksum);
        this.setHead(head);
        this.setBody(body);
    }


    public AsyncRefundDTO(String jwtToken,String tokenType, Body body, String client)
    {
        Head head = new Head()
                .setClientId(client)
                .setVersion("v1")
                .setRequestTimestamp("Time")
                .setTokenType(tokenType)
                .setToken(jwtToken);
        this.setHead(head);
        this.setBody(body);
    }

}
