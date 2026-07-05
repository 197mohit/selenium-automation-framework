package com.paytm.dto.checkoutjs;
import java.io.Serializable;
import java.util.Arrays;
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
        "strategy",
        "customerId",
        "referenceId",
        "subventionAmount",
        "items",
})
public class emiSubvention implements Serializable
{

    @JsonProperty("strategy")
    public String strategy;
    @JsonProperty("customerId")
    public String customerId;
    @JsonProperty("referenceId")
    public String referenceId;
    @JsonProperty("subventionAmount")
    public String subventionAmount;
    @JsonProperty("items")
    public List<Item> items;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -1711313455493626908L;


    public emiSubvention setstrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    public emiSubvention setcustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public emiSubvention setreferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public emiSubvention setsubventionAmount(String subventionAmount) {
        this.subventionAmount = subventionAmount;
        return this;
    }

    public emiSubvention setitems(List<Item> items) {
        this.items = Arrays.asList(new Item());
        return this;
    }

    public emiSubvention setitemsNullforAmountBasedTxn() {
        this.items = null;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public emiSubvention setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
