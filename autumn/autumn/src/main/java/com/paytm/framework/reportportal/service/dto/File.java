package com.paytm.framework.reportportal.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "content",
        "contentType"
})
public class File {

    @JsonProperty("name")
    private String name;
    @JsonProperty("content")
    private byte[] content;
    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("content")
    public byte[] getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(byte[] content) {
        this.content = content;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
