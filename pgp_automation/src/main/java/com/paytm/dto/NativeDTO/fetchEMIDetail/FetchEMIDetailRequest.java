package com.paytm.dto.NativeDTO.fetchEMIDetail;

import com.paytm.dto.NativeDTO.Body;
import com.paytm.dto.NativeDTO.Head;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 30/10/18
 */
public class FetchEMIDetailRequest {

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

    @Override
    public String toString() {
        return "ClassPojo [body = " + body.toString() + ", Head = " + head.toString() + "]";
    }

    public FetchEMIDetailRequest(String token, String channelCode){
        Map<String, Object> map = new HashMap<>();
        map.put("channelCode",channelCode );
        this.body = new Body(map);
        this.head = new Head(token);
    }

    public FetchEMIDetailRequest(String tokenType, String ssoToken, String channelCode,String mid)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("mid",mid);
        map.put("channelCode",channelCode);
        this.body = new Body(map);
        this.head = new Head(tokenType,ssoToken);

    }

    //Overloaded method, to be merged with the above
    public FetchEMIDetailRequest(String tokenType, String ssoToken, String channelCode,String mid,String emiType)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("mid",mid);
        map.put("channelCode",channelCode);
        map.put("emiType",emiType);
        this.body = new Body(map);
        this.head = new Head(tokenType,ssoToken);

    }

}
