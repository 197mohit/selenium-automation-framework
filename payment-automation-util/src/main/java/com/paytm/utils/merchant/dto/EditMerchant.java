package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by deepakkumar on 23/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditMerchant {

    @JsonProperty("ACTION")
    private String action;
    @JsonProperty("MID")
    private String mid;
    @JsonProperty("CREATED_BY")
    private String createdBy;
    @JsonProperty("MERCHANT_DETAILS")
    private MerchantDetails merchantDetails;
    @JsonProperty("URL_DETAILS")
    private List<UrlDetails> urlDetails;
    @JsonProperty("DOCS_DETAILS")
    private DocsDetails docsDetails;

    public String getAction() {
        return action;
    }

    public EditMerchant setAction(String action) {
        this.action = action;
        return this;
    }

    public String getMid() {
        return mid;
    }

    public EditMerchant setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public EditMerchant setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
        return this;
    }

    public List<UrlDetails> getUrlDetails() {
        return urlDetails;
    }

    public EditMerchant setUrlDetails(List<UrlDetails> urlDetails) {
        this.urlDetails = urlDetails;
        return this;
    }

    public DocsDetails getDocsDetails() {
        return docsDetails;
    }

    public EditMerchant setDocsDetails(DocsDetails docsDetails) {
        this.docsDetails = docsDetails;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public EditMerchant setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }
}
