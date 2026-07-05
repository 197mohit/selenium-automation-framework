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
        "operationOrigin",
        "operationType",
        "rentMonthYear",
        "rentPerMonth",
        "userMerchant",
        "isRentalsPayment",
        "paytmMerchantId",
        "IFSC",
        "panCard",
        "selfAccount",
        "panNameMatchFlag",
        "bankAccountNameMatchFlag",
        "isHighRiskBankAccount",
        "cpId",
        "cpFirstName",
        "cpMiddleName",
        "cpLastName",
        "cpName",
        "cpEmail",
        "cpMobile",
        "cpIdentityType",
        "cpIdentityNo",
        "cpCountry",
        "cpState",
        "cpCity",
        "cpArea",
        "cpPostalCode",
        "cpStreet1",
        "cpStreet2",
        "cpAddress",
        "cpPaytmUserId",
        "cpAccountNo",
        "cpIFSC",
        "cpglobalCardIndex",
        "cpVPA",
        "cpVpaName",
        "purposeCode",
        "initiationMode",
        "isVerifiedMerchant",
        "payeeMccCode",
        "payeeVpa",
        "payerIfsc",
        "payerName",
        "payerAccountType",
        "businessType",
        "merchantGenre",
        "payerMccCode",
        "payerVpa",
        "payerType",
        "payeeType",
        "payeeIfsc",
        "payeeName",
        "payeeAccountType",
        "txnType",
        "txnType1",
        "amount"
})
public class RiskExtendInfo {

    @JsonProperty("operationOrigin")
    private String operationOrigin;
    @JsonProperty("operationType")
    private String operationType;
    @JsonProperty("rentMonthYear")
    private String rentMonthYear;
    @JsonProperty("rentPerMonth")
    private String rentPerMonth;
    @JsonProperty("userMerchant")
    private String userMerchant;
    @JsonProperty("isRentalsPayment")
    private String isRentalsPayment;
    @JsonProperty("paytmMerchantId")
    private String paytmMerchantId;
    @JsonProperty("IFSC")
    private String iFSC;
    @JsonProperty("panCard")
    private String panCard;
    @JsonProperty("selfAccount")
    private String selfAccount;
    @JsonProperty("panNameMatchFlag")
    private String panNameMatchFlag;
    @JsonProperty("bankAccountNameMatchFlag")
    private String bankAccountNameMatchFlag;
    @JsonProperty("isHighRiskBankAccount")
    private String isHighRiskBankAccount;
    @JsonProperty("cpId")
    private String cpId;
    @JsonProperty("cpFirstName")
    private String cpFirstName;
    @JsonProperty("cpMiddleName")
    private String cpMiddleName;
    @JsonProperty("cpLastName")
    private String cpLastName;
    @JsonProperty("cpName")
    private String cpName;
    @JsonProperty("cpEmail")
    private String cpEmail;
    @JsonProperty("cpMobile")
    private String cpMobile;
    @JsonProperty("cpIdentityType")
    private String cpIdentityType;
    @JsonProperty("cpIdentityNo")
    private String cpIdentityNo;
    @JsonProperty("cpCountry")
    private String cpCountry;
    @JsonProperty("cpState")
    private String cpState;
    @JsonProperty("cpCity")
    private String cpCity;
    @JsonProperty("cpArea")
    private String cpArea;
    @JsonProperty("cpPostalCode")
    private String cpPostalCode;
    @JsonProperty("cpStreet1")
    private String cpStreet1;
    @JsonProperty("cpStreet2")
    private String cpStreet2;
    @JsonProperty("cpAddress")
    private String cpAddress;
    @JsonProperty("cpPaytmUserId")
    private String cpPaytmUserId;
    @JsonProperty("cpAccountNo")
    private String cpAccountNo;
    @JsonProperty("cpIFSC")
    private String cpIFSC;
    @JsonProperty("cpglobalCardIndex")
    private String cpglobalCardIndex;
    @JsonProperty("cpVPA")
    private String cpVPA;
    @JsonProperty("cpVpaName")
    private String cpVpaName;

    @JsonProperty("purposeCode")
    private String purposeCode;
    @JsonProperty("initiationMode")
    private String initiationMode;
    @JsonProperty("isVerifiedMerchant")
    private String isVerifiedMerchant;
    @JsonProperty("payeeMccCode")
    private String payeeMccCode;
    @JsonProperty("payeeVpa")
    private String payeeVpa;
    @JsonProperty("payerIfsc")
    private String payerIfsc;
    @JsonProperty("payerName")
    private String payerName;
    @JsonProperty("payerAccountType")
    private String payerAccountType;
    @JsonProperty("businessType")
    private String businessType;
    @JsonProperty("merchantGenre")
    private String merchantGenre;
    @JsonProperty("payerMccCode")
    private String payerMccCode;
    @JsonProperty("payerVpa")
    private String payerVpa;
    @JsonProperty("payerType")
    private String payerType;
    @JsonProperty("payeeType")
    private String payeeType;
    @JsonProperty("payeeIfsc")
    private String payeeIfsc;
    @JsonProperty("payeeName")
    private String payeeName;
    @JsonProperty("payeeAccountType")
    private String payeeAccountType;
    @JsonProperty("txnType")
    private String txnType;
    @JsonProperty("txnType1")
    private String txnType1;
    @JsonProperty("amount")
    private String amount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("operationOrigin")
    public String getOperationOrigin() {
        return operationOrigin;
    }

    @JsonProperty("operationOrigin")
    public RiskExtendInfo setOperationOrigin(String operationOrigin) {
        this.operationOrigin = operationOrigin;
        return this;
    }

    @JsonProperty("operationType")
    public String getOperationType() {
        return operationType;
    }

    @JsonProperty("operationType")
    public RiskExtendInfo setOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    @JsonProperty("rentMonthYear")
    public String getRentMonthYear() {
        return rentMonthYear;
    }

    @JsonProperty("rentMonthYear")
    public RiskExtendInfo setRentMonthYear(String rentMonthYear) {
        this.rentMonthYear = rentMonthYear;
        return this;
    }

    @JsonProperty("rentPerMonth")
    public String getRentPerMonth() {
        return rentPerMonth;
    }

    @JsonProperty("rentPerMonth")
    public RiskExtendInfo setRentPerMonth(String rentPerMonth) {
        this.rentPerMonth = rentPerMonth;
        return this;
    }

    @JsonProperty("userMerchant")
    public String getUserMerchant() {
        return userMerchant;
    }

    @JsonProperty("userMerchant")
    public RiskExtendInfo setUserMerchant(String userMerchant) {
        this.userMerchant = userMerchant;
        return this;
    }

    @JsonProperty("isRentalsPayment")
    public String getIsRentalsPayment() {
        return isRentalsPayment;
    }

    @JsonProperty("isRentalsPayment")
    public RiskExtendInfo setIsRentalsPayment(String isRentalsPayment) {
        this.isRentalsPayment = isRentalsPayment;
        return this;
    }

    @JsonProperty("paytmMerchantId")
    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    @JsonProperty("paytmMerchantId")
    public RiskExtendInfo setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
        return this;
    }

    @JsonProperty("IFSC")
    public String getIFSC() {
        return iFSC;
    }

    @JsonProperty("IFSC")
    public RiskExtendInfo setIFSC(String iFSC) {
        this.iFSC = iFSC;
        return this;
    }

    @JsonProperty("panCard")
    public String getPanCard() {
        return panCard;
    }

    @JsonProperty("panCard")
    public RiskExtendInfo setPanCard(String panCard) {
        this.panCard = panCard;
        return this;
    }

    @JsonProperty("selfAccount")
    public String getSelfAccount() {
        return selfAccount;
    }

    @JsonProperty("selfAccount")
    public RiskExtendInfo setSelfAccount(String selfAccount) {
        this.selfAccount = selfAccount;
        return this;
    }

    @JsonProperty("panNameMatchFlag")
    public String getPanNameMatchFlag() {
        return panNameMatchFlag;
    }

    @JsonProperty("panNameMatchFlag")
    public RiskExtendInfo setPanNameMatchFlag(String panNameMatchFlag) {
        this.panNameMatchFlag = panNameMatchFlag;
        return this;
    }

    @JsonProperty("bankAccountNameMatchFlag")
    public String getBankAccountNameMatchFlag() {
        return bankAccountNameMatchFlag;
    }

    @JsonProperty("bankAccountNameMatchFlag")
    public RiskExtendInfo setBankAccountNameMatchFlag(String bankAccountNameMatchFlag) {
        this.bankAccountNameMatchFlag = bankAccountNameMatchFlag;
        return this;
    }

    @JsonProperty("isHighRiskBankAccount")
    public String getIsHighRiskBankAccount() {
        return isHighRiskBankAccount;
    }

    @JsonProperty("isHighRiskBankAccount")
    public RiskExtendInfo setIsHighRiskBankAccount(String isHighRiskBankAccount) {
        this.isHighRiskBankAccount = isHighRiskBankAccount;
        return this;
    }

    @JsonProperty("cpId")
    public String getCpId() {
        return cpId;
    }

    @JsonProperty("cpId")
    public RiskExtendInfo setCpId(String cpId) {
        this.cpId = cpId;
        return this;
    }

    @JsonProperty("cpFirstName")
    public String getCpFirstName() {
        return cpFirstName;
    }

    @JsonProperty("cpFirstName")
    public RiskExtendInfo setCpFirstName(String cpFirstName) {
        this.cpFirstName = cpFirstName;
        return this;
    }

    @JsonProperty("cpMiddleName")
    public String getCpMiddleName() {
        return cpMiddleName;
    }

    @JsonProperty("cpMiddleName")
    public RiskExtendInfo setCpMiddleName(String cpMiddleName) {
        this.cpMiddleName = cpMiddleName;
        return this;
    }

    @JsonProperty("cpLastName")
    public String getCpLastName() {
        return cpLastName;
    }

    @JsonProperty("cpLastName")
    public RiskExtendInfo setCpLastName(String cpLastName) {
        this.cpLastName = cpLastName;
        return this;
    }

    @JsonProperty("cpName")
    public String getCpName() {
        return cpName;
    }

    @JsonProperty("cpName")
    public RiskExtendInfo setCpName(String cpName) {
        this.cpName = cpName;
        return this;
    }

    @JsonProperty("cpEmail")
    public String getCpEmail() {
        return cpEmail;
    }

    @JsonProperty("cpEmail")
    public RiskExtendInfo setCpEmail(String cpEmail) {
        this.cpEmail = cpEmail;
        return this;
    }

    @JsonProperty("cpMobile")
    public String getCpMobile() {
        return cpMobile;
    }

    @JsonProperty("cpMobile")
    public RiskExtendInfo setCpMobile(String cpMobile) {
        this.cpMobile = cpMobile;
        return this;
    }

    @JsonProperty("cpIdentityType")
    public String getCpIdentityType() {
        return cpIdentityType;
    }

    @JsonProperty("cpIdentityType")
    public RiskExtendInfo setCpIdentityType(String cpIdentityType) {
        this.cpIdentityType = cpIdentityType;
        return this;
    }

    @JsonProperty("cpIdentityNo")
    public String getCpIdentityNo() {
        return cpIdentityNo;
    }

    @JsonProperty("cpIdentityNo")
    public RiskExtendInfo setCpIdentityNo(String cpIdentityNo) {
        this.cpIdentityNo = cpIdentityNo;
        return this;
    }

    @JsonProperty("cpCountry")
    public String getCpCountry() {
        return cpCountry;
    }

    @JsonProperty("cpCountry")
    public RiskExtendInfo setCpCountry(String cpCountry) {
        this.cpCountry = cpCountry;
        return this;
    }

    @JsonProperty("cpState")
    public String getCpState() {
        return cpState;
    }

    @JsonProperty("cpState")
    public RiskExtendInfo setCpState(String cpState) {
        this.cpState = cpState;
        return this;
    }

    @JsonProperty("cpCity")
    public String getCpCity() {
        return cpCity;
    }

    @JsonProperty("cpCity")
    public RiskExtendInfo setCpCity(String cpCity) {
        this.cpCity = cpCity;
        return this;
    }

    @JsonProperty("cpArea")
    public String getCpArea() {
        return cpArea;
    }

    @JsonProperty("cpArea")
    public RiskExtendInfo setCpArea(String cpArea) {
        this.cpArea = cpArea;
        return this;
    }

    @JsonProperty("cpPostalCode")
    public String getCpPostalCode() {
        return cpPostalCode;
    }

    @JsonProperty("cpPostalCode")
    public RiskExtendInfo setCpPostalCode(String cpPostalCode) {
        this.cpPostalCode = cpPostalCode;
        return this;
    }

    @JsonProperty("cpStreet1")
    public String getCpStreet1() {
        return cpStreet1;
    }

    @JsonProperty("cpStreet1")
    public RiskExtendInfo setCpStreet1(String cpStreet1) {
        this.cpStreet1 = cpStreet1;
        return this;
    }

    @JsonProperty("cpStreet2")
    public String getCpStreet2() {
        return cpStreet2;
    }

    @JsonProperty("cpStreet2")
    public RiskExtendInfo setCpStreet2(String cpStreet2) {
        this.cpStreet2 = cpStreet2;
        return this;
    }

    @JsonProperty("cpAddress")
    public String getCpAddress() {
        return cpAddress;
    }

    @JsonProperty("cpAddress")
    public RiskExtendInfo setCpAddress(String cpAddress) {
        this.cpAddress = cpAddress;
        return this;
    }

    @JsonProperty("cpPaytmUserId")
    public String getCpPaytmUserId() {
        return cpPaytmUserId;
    }

    @JsonProperty("cpPaytmUserId")
    public RiskExtendInfo setCpPaytmUserId(String cpPaytmUserId) {
        this.cpPaytmUserId = cpPaytmUserId;
        return this;
    }

    @JsonProperty("cpAccountNo")
    public String getCpAccountNo() {
        return cpAccountNo;
    }

    @JsonProperty("cpAccountNo")
    public RiskExtendInfo setCpAccountNo(String cpAccountNo) {
        this.cpAccountNo = cpAccountNo;
        return this;
    }

    @JsonProperty("cpIFSC")
    public String getCpIFSC() {
        return cpIFSC;
    }

    @JsonProperty("cpIFSC")
    public RiskExtendInfo setCpIFSC(String cpIFSC) {
        this.cpIFSC = cpIFSC;
        return this;
    }

    @JsonProperty("cpglobalCardIndex")
    public String getCpglobalCardIndex() {
        return cpglobalCardIndex;
    }

    @JsonProperty("cpglobalCardIndex")
    public RiskExtendInfo setCpglobalCardIndex(String cpglobalCardIndex) {
        this.cpglobalCardIndex = cpglobalCardIndex;
        return this;
    }

    @JsonProperty("cpVPA")
    public String getCpVPA() {
        return cpVPA;
    }

    @JsonProperty("cpVPA")
    public RiskExtendInfo setCpVPA(String cpVPA) {
        this.cpVPA = cpVPA;
        return this;
    }

    @JsonProperty("cpVpaName")
    public String getCpVpaName() {
        return cpVpaName;
    }

    @JsonProperty("cpVpaName")
    public RiskExtendInfo setCpVpaName(String cpVpaName) {
        this.cpVpaName = cpVpaName;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getiFSC() {
        return iFSC;
    }

    public void setiFSC(String iFSC) {
        this.iFSC = iFSC;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public RiskExtendInfo setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
        return this;
    }

    public String getInitiationMode() {
        return initiationMode;
    }

    public RiskExtendInfo setInitiationMode(String initiationMode) {
        this.initiationMode = initiationMode;
        return this;
    }

    public String getIsVerifiedMerchant() {
        return isVerifiedMerchant;
    }

    public RiskExtendInfo setIsVerifiedMerchant(String isVerifiedMerchant) {
        this.isVerifiedMerchant = isVerifiedMerchant;
        return this;
    }

    public String getPayeeMccCode() {
        return payeeMccCode;
    }

    public RiskExtendInfo setPayeeMccCode(String payeeMccCode) {
        this.payeeMccCode = payeeMccCode;
        return this;
    }

    public String getPayeeVpa() {
        return payeeVpa;
    }

    public RiskExtendInfo setPayeeVpa(String payeeVpa) {
        this.payeeVpa = payeeVpa;
        return this;
    }

    public String getPayerIfsc() {
        return payerIfsc;
    }

    public RiskExtendInfo setPayerIfsc(String payerIfsc) {
        this.payerIfsc = payerIfsc;
        return this;
    }

    public String getPayerName() {
        return payerName;
    }

    public RiskExtendInfo setPayerName(String payerName) {
        this.payerName = payerName;
        return this;
    }

    public String getPayerAccountType() {
        return payerAccountType;
    }

    public RiskExtendInfo setPayerAccountType(String payerAccountType) {
        this.payerAccountType = payerAccountType;
        return this;
    }

    public String getBusinessType() {
        return businessType;
    }

    public RiskExtendInfo setBusinessType(String businessType) {
        this.businessType = businessType;
        return this;
    }

    public String getMerchantGenre() {
        return merchantGenre;
    }

    public RiskExtendInfo setMerchantGenre(String merchantGenre) {
        this.merchantGenre = merchantGenre;
        return this;
    }

    public String getPayerMccCode() {
        return payerMccCode;
    }

    public RiskExtendInfo setPayerMccCode(String payerMccCode) {
        this.payerMccCode = payerMccCode;
        return this;
    }

    public String getPayerVpa() {
        return payerVpa;
    }

    public RiskExtendInfo setPayerVpa(String payerVpa) {
        this.payerVpa = payerVpa;
        return this;
    }

    public String getPayerType() {
        return payerType;
    }

    public RiskExtendInfo setPayerType(String payerType) {
        this.payerType = payerType;
        return this;
    }

    public String getPayeeType() {
        return payeeType;
    }

    public RiskExtendInfo setPayeeType(String payeeType) {
        this.payeeType = payeeType;
        return this;
    }

    public String getPayeeIfsc() {
        return payeeIfsc;
    }

    public RiskExtendInfo setPayeeIfsc(String payeeIfsc) {
        this.payeeIfsc = payeeIfsc;
        return this;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public RiskExtendInfo setPayeeName(String payeeName) {
        this.payeeName = payeeName;
        return this;
    }

    public String getPayeeAccountType() {
        return payeeAccountType;
    }

    public RiskExtendInfo setPayeeAccountType(String payeeAccountType) {
        this.payeeAccountType = payeeAccountType;
        return this;
    }

    public String getTxnType() {
        return txnType;
    }

    public RiskExtendInfo setTxnType(String txnType) {
        this.txnType = txnType;
        return this;
    }

    public String getTxnType1() {
        return txnType1;
    }

    public RiskExtendInfo setTxnType1(String txnType1) {
        this.txnType1 = txnType1;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public RiskExtendInfo setAmount(String amount) {
        this.amount = amount;
        return this;
    }
}