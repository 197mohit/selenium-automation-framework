package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pageType",
        "redirectForm"
})
public class BankForm {

    @JsonProperty("pageType")
    private String pageType;
    @JsonProperty("redirectForm")
    private RedirectForm redirectForm;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("pageType")
    public String getPageType() {
        return pageType;
    }

    @JsonProperty("pageType")
    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    @JsonProperty("redirectForm")
    public RedirectForm getRedirectForm() {
        return redirectForm;
    }

    @JsonProperty("redirectForm")
    public void setRedirectForm(RedirectForm redirectForm) {
        this.redirectForm = redirectForm;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}