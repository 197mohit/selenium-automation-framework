package com.paytm.utils.merchant.dto.cachecardtoken.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.util.PGPUtil;

public class CacheCardDTO {

    private Body body;
    private Head head;

    public  CacheCardDTO()
    {
        super();
    }
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

    public CacheCardDTO(String merchantKey, Body body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String checksum = PGPUtil.getChecksum(merchantKey, mapper.writeValueAsString(body));
        Head head = new Head().setSignature(checksum).setChannelId("WAP").setClientId("").setRequestTimestamp("").setVersion("");
        this.setBody(body);
        this.setHead(head);
    }




}
