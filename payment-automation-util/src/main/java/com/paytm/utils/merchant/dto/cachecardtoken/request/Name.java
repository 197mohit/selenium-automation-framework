package com.paytm.utils.merchant.dto.cachecardtoken.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Name {


    private String firstName;
    private String lastName;


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Name setFirstName(String firstName)
    {
        this.firstName=firstName;
        return  this;
    }

    public Name setLastName(String lastName)
    {
        this.lastName=lastName;
        return  this;
    }
}
