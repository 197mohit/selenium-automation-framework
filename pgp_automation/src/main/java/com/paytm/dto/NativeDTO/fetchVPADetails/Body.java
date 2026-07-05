package com.paytm.dto.NativeDTO.fetchVPADetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Body {

    @JsonInclude(JsonInclude.Include.NON_NULL)

    @JsonProperty
    private String text = "{}";
}
