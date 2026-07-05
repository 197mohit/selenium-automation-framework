package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rahulkumar on Apr,2018
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigureMbidAndInstrument {

    @JsonProperty("PAY_MODES")
    private List<PAY_MODES> payModes;

    public List<PAY_MODES> getPayModes()
    {
        return this.payModes;
    }


    public ConfigureMbidAndInstrument setPayModes(List<PAY_MODES> payModes)
    {
        this.payModes = payModes;
        return this;
    }

    public ConfigureMbidAndInstrument setPayModes(PAY_MODES... payModes)
    {
        this.payModes = Arrays.asList(payModes);
        return this;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [payModes = "+ payModes +"]";
    }
}
