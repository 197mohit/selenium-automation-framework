package com.paytm.dto.NativeDTO.validateOtp;

import com.paytm.dto.NativeDTO.Body;
import com.paytm.dto.NativeDTO.Head;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 22/10/18
 */
public class ValidateOtp {
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

    public ValidateOtp(String txnToken, String otp) {
        body = new Body("otp", otp);
        head = new Head(txnToken);
    }

    public ValidateOtp(Map<String, Object> map ) {
        body = new Body(map);
        head = new Head((String) map.get("txnToken"));
    }

    @Override
    public String toString() {
        return "ClassPojo [body = " + body.toString() + ", Head = " + head.toString() + "]";
    }

}
