
package com.paytm.dto.NativeDTO.FetchCardDetailsDTO;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class FetchCardDetailsDTO implements Serializable
{

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -8799005937213284111L;

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    public FetchCardDetailsDTO(FetchCardDetailsDTO.Builder builder)
    {
        this.head = new Head()
                .setVersion(builder.version)
                .setRequestTimestamp(builder.requestTimestamp)
                .setRequestId(builder.requestId)
                .setChannelId(builder.channelId)
                .setTokenType(builder.tokenType)
                .setToken(builder.token);

        this.body = new Body()
                .setMid(builder.mid)
                .setCardNumber(builder.cardNumber)
                .setSavedCardId(builder.savedCardId)
                .setIsEightDigitBinRequired(builder.isEightDigitBinRequired);

    }

    public static class Builder {
        private String version = "v1";
        private String requestTimestamp = "TIME";
        private String requestId ="test123";
        private String channelId ="WAP";
        private String tokenType;
        private String token;
        private String mid;
        private String cardNumber;
        private String savedCardId;
        private boolean isEightDigitBinRequired=true;

        public FetchCardDetailsDTO.Builder setVersion(String version)
        {
            this.version=version;
            return this;
        }

        public FetchCardDetailsDTO.Builder setRequestTimeStamp(String requestTimeStamp)
        {
            this.requestTimestamp=requestTimeStamp;
            return this;
        }

        public FetchCardDetailsDTO.Builder setRequestId(String requestId)
        {
            this.requestId=requestId;
            return this;
        }

        public FetchCardDetailsDTO.Builder setChannelId(String channelId)
        {
            this.channelId=channelId;
            return this;
        }

        public FetchCardDetailsDTO.Builder setTokenType(String tokenType)
        {
            this.tokenType=tokenType;
            return this;
        }

        public FetchCardDetailsDTO.Builder setToken(String token)
        {
            this.token=token;
            return this;
        }

        public FetchCardDetailsDTO.Builder setMID(String mid)
        {
            this.mid=mid;
            return this;
        }

        public FetchCardDetailsDTO.Builder setCardNumber(String cardNumber)
        {
            this.cardNumber=cardNumber;
            return this;
        }

        public FetchCardDetailsDTO.Builder setSavedCardId(String savedCardId)
        {
            this.savedCardId=savedCardId;
            return this;
        }


        public FetchCardDetailsDTO.Builder setIsEightBinRequired(Boolean isEightDigitBinRequired)
        {
            this.isEightDigitBinRequired = isEightDigitBinRequired;
            return this;
        }


        public FetchCardDetailsDTO build() {
            return new FetchCardDetailsDTO(this);
        }

    }

}
