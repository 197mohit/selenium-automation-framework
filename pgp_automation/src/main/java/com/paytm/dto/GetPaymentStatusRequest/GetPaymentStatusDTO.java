package com.paytm.dto.GetPaymentStatusRequest;

import com.fasterxml.jackson.annotation.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.utils.merchant.merchant.util.Merchant;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class GetPaymentStatusDTO {


    public GetPaymentStatusDTO(Builder builder){
        this.body=new Body(builder.OrderId,builder.AggMid,builder.Mid);
        this.head=new Head(builder.checksum);
    }


    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }


    public static class Builder {

        private String OrderId;
        private String AggMid;
        private String Mid;
        private String checksum;
        private String AggMidKey;

        public Builder(String orderId, Constants.MerchantType m){
            this.OrderId = orderId;
            this.Mid = m.getId();
            GetPaymentStatusDTO merchantStatus = new GetPaymentStatusDTO(this);
            this.checksum=PGPHelpers.getNativeChecksum(m.getKey(), merchantStatus.getBody());
        }

        public Builder(String orderId, Merchant m){
            this.OrderId = orderId;
            this.Mid = m.getId();
            GetPaymentStatusDTO merchantStatus = new GetPaymentStatusDTO(this);
            this.checksum=PGPHelpers.getNativeChecksum(m.getKey(), merchantStatus.getBody());
        }

        public Builder(String orderId, String mId, String mKey){
            this.OrderId = orderId;
            this.Mid = mId;
            GetPaymentStatusDTO merchantStatus = new GetPaymentStatusDTO(this);
            this.checksum=PGPHelpers.getNativeChecksum(mKey, merchantStatus.getBody());
        }

        public Builder(String OrderId, String AggMid, String AggMidKey, String Mid) {
            this.OrderId = OrderId;
            this.AggMid = AggMid;
            this.AggMidKey=AggMidKey;
            this.Mid = Mid;
            GetPaymentStatusDTO merchantStatus = new GetPaymentStatusDTO(this);
            this.checksum=PGPHelpers.getNativeChecksum(this.AggMidKey, merchantStatus.getBody());
        }


        public Builder(String OrderId, String AggMid, String AggMidKey, String Mid,String Checksum) {
            this.OrderId = OrderId;
            this.AggMid = AggMid;
            this.AggMidKey=AggMidKey;
            this.Mid = Mid;
            this.checksum=Checksum;
        }



        public GetPaymentStatusDTO build() {
            GetPaymentStatusDTO merchantStatus = new GetPaymentStatusDTO(this);
            merchantStatus.getHead().setSignature(checksum);
            return merchantStatus;
        }


    }


}


