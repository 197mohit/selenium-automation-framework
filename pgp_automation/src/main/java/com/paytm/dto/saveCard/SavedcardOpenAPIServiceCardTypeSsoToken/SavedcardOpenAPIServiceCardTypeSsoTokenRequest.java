package com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class SavedcardOpenAPIServiceCardTypeSsoTokenRequest {

    @JsonProperty("head")
    private Head head=new Head();
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public SavedcardOpenAPIServiceCardTypeSsoTokenRequest() {

    }

    /**
     *
     * @param body
     * @param head
     */
    public SavedcardOpenAPIServiceCardTypeSsoTokenRequest(Head head, Body body) {
        super();
        this.head = head;
        this.body = body;
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    public SavedcardOpenAPIServiceCardTypeSsoTokenRequest withHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    public SavedcardOpenAPIServiceCardTypeSsoTokenRequest withBody(Body body) {
        this.body = body;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SavedcardOpenAPIServiceCardTypeSsoTokenRequest withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}