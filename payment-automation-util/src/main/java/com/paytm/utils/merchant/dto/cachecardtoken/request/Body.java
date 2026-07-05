package com.paytm.utils.merchant.dto.cachecardtoken.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Body {

    private String mid;
    private String requestId;
    private  String vpa;
    private  String accountNumber;
    private String ifscCode;
    @JsonIgnore
    private String mobileNo;
    @JsonIgnore
    private Name name;


public Body(){
    super();
}

    public String getMid() {
        return mid;
    }

    public Body setMid(String mid){
        this.mid=mid;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    public Body setAccountNumber(String accountNumber)
    {
        this.accountNumber=accountNumber;
        return this;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public Body setIfscCode(String ifscCode)
    {
        this.ifscCode=ifscCode;
        return this;
    }
    public String getMobileNo()
    {
        return mobileNo;
    }
    public Body setMobileNo(String mobileNo){
        this.mobileNo=mobileNo;
        return this;
    }

    public String getVpa() {
        return vpa;
    }

    public Body setVpa(String vpa){
        this.vpa=vpa;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }
    public Body setRequestId(String requestId){
        this.requestId=requestId;
        return this;
    }

    public Name getName() {
        return name;
    }
    public Body setName(Name name)
    {
        this.name=name;
        return this;
    }

}
