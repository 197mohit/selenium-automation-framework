package com.paytm.dto.theia;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchPublicKeyRequestDTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public FetchPublicKeyRequestDTO setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public FetchPublicKeyRequestDTO setBody(Body body) {
        this.body = body;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Head {

        @JsonProperty("tokenType")
        private String tokenType = "ACCESS";
        @JsonProperty("token")
        private String token;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonProperty("tokenType")
        public String getTokenType() {
            return tokenType;
        }

        @JsonProperty("tokenType")
        public Head setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        @JsonProperty("token")
        public String getToken() {
            return token;
        }

        @JsonProperty("token")
        public Head setToken(String token) {
            this.token = token;
            return this;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            additionalProperties.put(name, value);
        }
    }

    /**
     * Body for fetchPublicKey. {@link MidBodyMode} matches prior JSON: string {@code mid},
     * {@code "mid":null}, or omitted {@code mid}.
     */
    @JsonSerialize(using = Body.Serializer.class)
    public static class Body {

        public enum MidBodyMode {
            STRING,
            JSON_NULL,
            OMIT
        }

        private MidBodyMode midBodyMode = MidBodyMode.STRING;
        private String mid;
        private String referenceId;
        private boolean includeReferenceId = true;

        public MidBodyMode getMidBodyMode() {
            return midBodyMode;
        }

        public Body setMidBodyMode(MidBodyMode midBodyMode) {
            this.midBodyMode = midBodyMode;
            return this;
        }

        public String getMid() {
            return mid;
        }

        public Body setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public Body setReferenceId(String referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public boolean isIncludeReferenceId() {
            return includeReferenceId;
        }

        public Body setIncludeReferenceId(boolean includeReferenceId) {
            this.includeReferenceId = includeReferenceId;
            return this;
        }

        public static final class Serializer extends JsonSerializer<Body> {

            @Override
            public void serialize(Body v, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                switch (v.getMidBodyMode()) {
                    case STRING:
                        gen.writeStringField("mid", v.getMid() != null ? v.getMid() : "");
                        break;
                    case JSON_NULL:
                        gen.writeNullField("mid");
                        break;
                    case OMIT:
                    default:
                        break;
                }
                if (v.isIncludeReferenceId()) {
                    gen.writeStringField("referenceId", v.getReferenceId() != null ? v.getReferenceId() : "");
                }
                gen.writeEndObject();
            }
        }
    }
}
