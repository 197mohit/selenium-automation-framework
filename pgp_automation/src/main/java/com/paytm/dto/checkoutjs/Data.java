package com.paytm.dto.checkoutjs;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "orderId",
        "amount",
        "token",
        "tokenType",
        "userDetail"
})
public class Data implements Serializable
{

    @JsonProperty("orderId")
    public String orderId;
    @JsonProperty("amount")
    public String amount;
    @JsonProperty("token")
    public String token;
    @JsonProperty("tokenType")
    public String tokenType;
    @JsonProperty("userDetail")
    public UserDetail userDetail;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 93932577880748056L;

    public Data setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public Data setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Data setToken(String token) {
        this.token = token;
        return this;
    }

    public Data setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public Data setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Data setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
