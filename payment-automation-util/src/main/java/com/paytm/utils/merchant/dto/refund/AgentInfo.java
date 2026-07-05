package com.paytm.utils.merchant.dto.refund;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentInfo {
    @JsonProperty("employeeId")
    private String employeeId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phoneNo")
    private String phoneNo;

    @JsonProperty("email")
    private String email;

    public String getEmployeeId() {
        return employeeId;
    }

    public AgentInfo setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AgentInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public AgentInfo setPhoneNo(String phoneNo) {
        this.phoneNo= phoneNo;
        return this;
    }
    public String getEmail() {
        return email;
    }

    public AgentInfo setEmail(String email) {
        this.email = email;
        return this;
    }
}
