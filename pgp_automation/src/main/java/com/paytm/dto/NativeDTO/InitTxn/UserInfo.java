
package com.paytm.dto.NativeDTO.InitTxn;

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
        "custId",
        "mobile",
        "email",
        "firstName",
        "lastName",
        "address",
        "pincode",
        "city",
        "state",
        "countryName",
        "countryCode",
        "pan",
        "dob",
        "bankAccount",
        "ieCode",
        "ssoToken",
        "mobileNumber"
})
public class UserInfo {

    @JsonProperty("ssoToken")
    private String ssoToken;
    @JsonProperty("custId")
    private String custId;
    @JsonProperty("mobile")
    private String mobile;
    @JsonProperty("email")
    private String email;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("address")
    private String address;
    @JsonProperty("pincode")
    private String pincode;
    @JsonProperty("city")
    private String city;
    @JsonProperty("state")
    private String state;
    @JsonProperty("countryName")
    private String countryName;
    @JsonProperty("countryCode")
    private String countryCode;
    @JsonProperty("pan")
    private String pan;
    @JsonProperty("dob")
    private String dob;
    @JsonProperty("bankAccount")
    private BankAccount bankAccount;
    @JsonProperty("ieCode")
    private String ieCode;
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ssoToken")
    public String getSsoToken() {
        return ssoToken;
    }

    @JsonProperty("ssoToken")
    public UserInfo setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
        return this;
    }
    @JsonProperty("custId")
    public String getCustId() {
        return custId;
    }

    @JsonProperty("custId")
    public void setCustId(String custId) {
        this.custId = custId;
    }

    @JsonProperty("mobile")
    public String getMobile() {
        return mobile;
    }

    @JsonProperty("mobile")
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("pincode")
    public String getPincode() {
        return pincode;
    }

    @JsonProperty("pincode")
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("countryName")
    public String getCountryName() {
        return countryName;
    }

    @JsonProperty("countryName")
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @JsonProperty("countryCode")
    public String getCountryCode() {
        return countryCode;
    }

    @JsonProperty("countryCode")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonProperty("pan")
    public String getPan() {
        return pan;
    }

    @JsonProperty("pan")
    public void setPan(String pan) {
        this.pan = pan;
    }

    @JsonProperty("dob")
    public String getDob() {
        return dob;
    }

    @JsonProperty("dob")
    public void setDob(String dob) {
        this.dob = dob;
    }

    @JsonProperty("bankAccount")
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    @JsonProperty("bankAccount")
    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @JsonProperty("ieCode")
    public String getIeCode() {
        return ieCode;
    }

    @JsonProperty("ieCode")
    public void setIeCode(String ieCode) {
        this.ieCode = ieCode;
    }

    @JsonProperty("mobileNumber")
    public String getMobileNumber() {
        return mobileNumber;
    }

    @JsonProperty("mobileNumber")
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public UserInfo(String custId, String lastName, String email, String firstName, String mobile) {
        this.custId = custId;
        this.lastName = lastName;
        this.email = email;
        this.firstName = firstName;
        this.mobile = mobile;
    }

    public UserInfo() {
        this.custId = "custId";
        this.lastName = "lastName";
        this.email = "email";
        this.firstName = "firstName";
        this.mobile = "mobile";
    }
    public UserInfo(String ssoToken, String mobileNumber) {
        this.ssoToken = ssoToken;
        this.mobileNumber = mobileNumber;
    }

}
