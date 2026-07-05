package com.paytm.utils.merchant.dto.alipay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.utils.merchant.dto.alipay.request.Request;
import com.paytm.utils.merchant.dto.alipay.request.SearchCondition;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "request",
        "signature"
})
public class BizOrderSearchReqDTO {

    @JsonProperty("request")
    private Request request;
    @JsonProperty("signature")
    private String signature = "signature string";
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public BizOrderSearchReqDTO(String orderId) {

        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSearchKey("MERCHANT_TRANS_ID");
        searchCondition.setSearchValue(orderId);
        List<SearchCondition> list = new ArrayList<>();
        list.add(searchCondition);

        this.request = new Request();
        request.getBody().setSearchConditions(list);
    }

    public BizOrderSearchReqDTO(String searchKey, String searchValue) {
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSearchKey(searchKey);
        searchCondition.setSearchValue(searchValue);
        List<SearchCondition> list = new ArrayList<>();
        list.add(searchCondition);

        this.request = new Request();
        request.getBody().setSearchConditions(list);
    }

    @JsonProperty("request")
    public Request getRequest() {
        return request;
    }

    @JsonProperty("request")
    public void setRequest(Request request) {
        this.request = request;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public void setSignature(String signature) {
        this.signature = signature;
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