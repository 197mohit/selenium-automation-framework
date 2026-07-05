package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sureshgupta on 15/11/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigVelocity {

    @JsonProperty("VELOCITIES")
    private List<Velocities> velocities;

    public List<Velocities> getVelocities() {
        return velocities;
    }

    public ConfigVelocity setVelocities(Velocities[] velocities) {
        this.velocities = Arrays.asList(velocities);
        return this;
    }

    public ConfigVelocity setVelocities(List<Velocities> velocities) {
        this.velocities = velocities;
        return this;
    }
}
