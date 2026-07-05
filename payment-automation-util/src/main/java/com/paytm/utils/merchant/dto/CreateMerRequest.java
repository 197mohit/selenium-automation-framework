package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Created by deepakkumar on 18/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateMerRequest {

    @JsonProperty("CREATED_BY")
    private String createdBy;
    @JsonProperty("ACTION")
    private String action;
    @JsonProperty("MERCHANT_DETAILS")
    private MerchantDetails merchantDetails;
    @JsonProperty("URL_DETAILS")
    private List<UrlDetails> urlDetails = null;
    @JsonProperty("DOCS_DETAILS")
    private DocsDetails docsDetails;

    public String getCreatedBy() {
        return createdBy;
    }

    public CreateMerRequest setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getAction() {
        return action;
    }

    public CreateMerRequest setAction(String action) {
        this.action = action;
        return this;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public CreateMerRequest setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
        return this;
    }

    public List<UrlDetails> getUrlDetails() {
        return urlDetails;
    }

    public CreateMerRequest setUrlDetails(UrlDetails[] urlDetails) {
        this.urlDetails = Arrays.asList(urlDetails);
        return this;
    }

    public CreateMerRequest setUrlDetails(List<UrlDetails> urlDetails) {
        this.urlDetails = urlDetails;
        return this;
    }

    public DocsDetails getDocsDetails() {
        return docsDetails;
    }

    public CreateMerRequest setDocsDetails(DocsDetails docsDetails) {
        this.docsDetails = docsDetails;
        return this;
    }
}
