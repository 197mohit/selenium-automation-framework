package com.paytm.dto.CCBillPayments.FetchBin;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchBin {

    Head HeadObject;
    Body BodyObject;


    // Getter Methods

    public Head getHead() {
        return HeadObject;
    }

    public Body getBody() {
        return BodyObject;
    }

    // Setter Methods

    public void setHead(Head headObject) {
        this.HeadObject = headObject;
    }

    public void setBody(Body bodyObject) {
        this.BodyObject = bodyObject;
    }
 }