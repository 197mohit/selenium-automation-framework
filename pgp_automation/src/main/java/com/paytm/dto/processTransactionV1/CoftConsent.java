package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoftConsent {
  @JsonProperty("userConsent")
  public String userConsent;
  @JsonProperty("createdAt")
  public String createdAt;
  @JsonProperty("userConsentId")
  public String userConsentId;
}
