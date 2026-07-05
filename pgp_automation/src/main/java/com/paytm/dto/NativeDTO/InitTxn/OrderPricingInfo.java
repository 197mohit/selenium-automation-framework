package com.paytm.dto.NativeDTO.InitTxn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "orderTotalAmount",
        "amountInfoList"
})
public class OrderPricingInfo implements Serializable
{

    @JsonProperty("orderTotalAmount")
    private OrderTotalAmount orderTotalAmount;
    @JsonProperty("amountInfoList")
    private List<AmountInfoList> amountInfoList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -2533932341091463199L;

    @JsonProperty("orderTotalAmount")
    public OrderTotalAmount getOrderTotalAmount() {
        return orderTotalAmount;
    }

    @JsonProperty("orderTotalAmount")
    public void setOrderTotalAmount(OrderTotalAmount orderTotalAmount) {
        this.orderTotalAmount = orderTotalAmount;
    }

    @JsonProperty("amountInfoList")
    public List<AmountInfoList> getAmountInfoList() {
        return amountInfoList;
    }

    @JsonProperty("amountInfoList")
    public void setAmountInfoList(List<AmountInfoList> amountInfoList) {
        this.amountInfoList = amountInfoList;
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