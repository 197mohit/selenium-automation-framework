package com.paytm.dto.EnrollmentStatusDTO;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class EnrollmentStatusDTO {

        @JsonProperty("head")
        private Head head;
        @JsonProperty("body")
        private Body body;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public EnrollmentStatusDTO() {

        }

        @JsonProperty("head")
        public Head getHead() {
                return head;
        }

        @JsonProperty("head")
        public void setHeadetHead(Head head) {
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


        public EnrollmentStatusDTO(Builder builder) {
this.body= new Body().setAppId(builder.appId).setCustId(builder.custId).setAccountDataList(builder.accountDataLists);
this.head= new Head().setRequestId(builder.requestId).setTokenType(builder.tokenType).setToken(builder.token);
        }

        public static class Builder {
                private String requestId;
                private String token;
                private String tokenType;
                private String appId;
                private String custId;
                private List <AccountDataList> accountDataLists;
                private String deviceId;
                private String cardAlias;
                private String bin;

                public Builder(String token, String tokenType) {
                        this.requestId = "123";
                        this.token = token;
                        this.tokenType = tokenType;
                }


                public Builder() {

                }
                public List getAccountDataList(){
                        return accountDataLists;
                }

                public Builder setAccountDataList(List<AccountDataList> accountDataList) {
                        this.accountDataLists = accountDataList;
                        return this;
                }

                public String getRequestId() {
                        return requestId;
                }

                public Builder setRequestId(String requestId) {
                        this.requestId = requestId;
                        return this;
                }
                public String getTokenType() {
                        return tokenType;
                }

                public Builder setTokenType(String tokenType) {
                        this.tokenType = tokenType;
                        return this;
                }
                public String getToken() {
                        return token;
                }

                public Builder setToken(String token) {
                        this.token = token;
                        return this;
                }
                public String getAppId() {
                        return appId;
                }

                public Builder setAppId(String appId) {
                        this.appId = appId;
                        return this;
                }

                public String getCustId() {
                        return custId;
                }

                public Builder setCustId(String custId) {
                        this.custId = custId;
                        return this;
                }
                public String getDeviceId( String deviceId) {
                        return deviceId;
                }

                public Builder setDeviceId(String deviceId) {
                        this.deviceId = deviceId;
                        return this;
                }
                public String getCardAlias( String cardAlias) {
                        return cardAlias;
                }

                public Builder setCardAlias(String cardAlias) {
                        this.cardAlias = cardAlias;
                        return this;
                }
                public String getBin( String bin) {
                        return bin;
                }

                public Builder setBin(String bin) {
                        this.bin = bin;
                        return this;
                }
                public EnrollmentStatusDTO build(){
                        return new EnrollmentStatusDTO(this);
                }
        }
}


