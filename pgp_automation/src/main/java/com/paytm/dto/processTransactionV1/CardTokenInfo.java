
package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cardToken",
    "tokenExpiry",
    "TAVV",
    "tokenType",
    "cardSuffix",
    "panUniqueReference"
})
@Data
public class CardTokenInfo {

    @JsonProperty("cardToken")
    public String cardToken;
    @JsonProperty("tokenExpiry")
    public String tokenExpiry;
    @JsonProperty("TAVV")
    public String tavv;
    @JsonProperty("cardSuffix")
    public String cardSuffix;
    @JsonProperty("panUniqueReference")
    public String panUniqueReference;
    @JsonProperty("merchantTokenRequestorId")
    public String merchantTokenRequestorId;
    @JsonProperty String tokenUniqueReference;
    @JsonProperty("tokenType")
    public String tokenType;

}
