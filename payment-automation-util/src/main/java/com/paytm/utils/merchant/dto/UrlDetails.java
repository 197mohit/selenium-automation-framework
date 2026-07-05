package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deepakkumar on 17/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlDetails {
    @JsonProperty("WEBSITE_NAME")
    private String websiteName;
    @JsonProperty("REQUEST_URL")
    private String requestUrl;
    @JsonProperty("RESPONSE_URL")
    private String responseUrl;
    @JsonProperty("PEON_URL")
    private String peonUrl;
    @JsonProperty("IMAGE_NAME")
    private String imageName;

    public String getWebsiteName() {
        return websiteName;
    }

    public UrlDetails setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
        return this;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public UrlDetails setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public UrlDetails setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
        return this;
    }

    public String getPeonUrl() {
        return peonUrl;
    }

    public UrlDetails setPeonUrl(String peonUrl) {
        this.peonUrl = peonUrl;
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    public UrlDetails setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }
}
