package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deepakkumar on 17/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Velocities {

    @JsonProperty("VELOCITY_TYPE")
    private String velocityType;
    @JsonProperty("MID")
    private String mid;
    @JsonProperty("VELOCITY_DETAILS")
    private VelocityDetails velocityDetails;

    public String getVelocityType() {
        return velocityType;
    }

    public Velocities setVelocityType(String velocityType) {
        this.velocityType = velocityType;
        return this;
    }

    public String getMid() {
        return mid;
    }

    public Velocities setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public VelocityDetails getVelocityDetails() {
        return velocityDetails;
    }

    public Velocities setVelocityDetails(VelocityDetails velocityDetails) {
        this.velocityDetails = velocityDetails;
        return this;
    }

}

