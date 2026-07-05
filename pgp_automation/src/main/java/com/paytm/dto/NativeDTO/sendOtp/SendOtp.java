package com.paytm.dto.NativeDTO.sendOtp;

import com.paytm.dto.NativeDTO.Body;
import com.paytm.dto.NativeDTO.Head;

/**
 * Created by anjukumari on 22/10/18
 */
public class SendOtp {
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

    public SendOtp(String txnToken, String mobileNumber) {
        body = new Body("mobileNumber", mobileNumber);
        head = new Head(txnToken);
    }

    @Override
    public String toString() {
        return "ClassPojo [body = " + body.toString() + ", Head = " + head.toString() + "]";
    }


}
