package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.processTransactionV1.response.EmiSubventionInfo;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "orderId",
        "sso",
        "qrCodeId",
        "paymentMode",
        "cardInfo",
        "authMode",
        "cardPreAuthType",
        "mandateAuthMode",
        "channelCode",
        "bankIfsc",
        "userName",
        "account_holder_name",
        "accountType",
        "merchantKey",
        "custId",
        "saveForFuture",
        "paymentFlow",
        "payerAccount",
        "website",
        "mpin",
        "planId",
        "emiType",
        "encCardInfo",
        "PROMO_CAMP_ID",
        "cardTokenRequired",
        "aggMid",
        "aggType",
        "account_number",
        "accountNumber",
        "storeInstrument",
        "txnAmount",
        "coftConsent",
        "extendInfo",
        "riskExtendInfo",
        "requestType",
        "addMoney",
        "ecomTokenInfo",
        "emiSubventionInfo",
        "convertToAddAndPayTxn",
        "preferredOtpPage",
        "seqNumber",
        "upiAccRefId",
        "cardTokenInfo",
        "creditBlock",
        "upiLiteRequestData",
        "creditBlock",
        "hybridPayModeDetails"

})
public class Body {

    @JsonProperty("emiSubventionInfo")
    private EmiSubventionInfo emiSubventionInfo;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("sso")
    private String sso;
    @JsonProperty("qrCodeId")
    private String qrCodeId;
    @JsonProperty("merchantVpa")
    private String merchantVpa;
    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("cardInfo")
    private String cardInfo;
    @JsonProperty
    private CoftConsent coftConsent;
    @JsonProperty("authMode")
    private String authMode;
    @JsonProperty("cardPreAuthType")
    private String cardPreAuthType;
    @JsonProperty("mandateAuthMode")
    private String mandateAuthMode;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("bankIfsc")
    private String bankIfsc;
    /** Not serialized; use {@link #userNameCamel} for the {@code userName} JSON key on v1 PTC. */
    @JsonIgnore
    private String userName;
    /** CamelCase {@code userName} on v1 PTC. Null omits the key. */
    @JsonProperty("userName")
    private String userNameCamel;
    /** Bank-mandate PTC only; null keeps the key out of the body ({@link JsonInclude.Include#NON_NULL}). */
    @JsonProperty("account_holder_name")
    private String accountHolderName;
    /** Not serialized; use {@link #accountTypeCamel} for the {@code accountType} JSON key on v1 PTC. */
    @JsonIgnore
    private String accountType;
    /** CamelCase {@code accountType} on v1 PTC. */
    @JsonProperty("accountType")
    private String accountTypeCamel;
    /** Not serialized on v1 PTC ({@code INDUSTRY_TYPE_ID} removed). */
    @JsonIgnore
    private String industryTypeId;
    @JsonProperty("merchantKey")
    private String merchantKey;
    @JsonProperty("custId")
    private String custId;
    @JsonProperty("saveForFuture")
    private String saveForFuture;
    @JsonProperty("paymentFlow")
    private String paymentFlow;
    @JsonProperty("payerAccount")
    private String payerAccount;
    @JsonProperty("website")
    private String website = "retail";
    @JsonProperty("mpin")
    private String mpin;
    @JsonProperty("planId")
    private String planId;
    @JsonProperty("emiType")
    private String emiType;
    @JsonProperty("encCardInfo")
    private String encCardInfo;
    @JsonProperty("PROMO_CAMP_ID")
    private String pROMOCAMPID;
    @JsonProperty("cardTokenRequired")
    private String cardTokenRequired;
    @JsonProperty("aggMid")
    private String aggMid;
    @JsonProperty("aggType")
    private String aggType;
    @JsonProperty("SUBSCRIPTION_ID")
    private String subsId;
    @JsonProperty("account_number")
    private String accountNumber;
    /** CamelCase key; used with {@link #accountNumber} when both are required (e.g. bank mandate PTC). */
    @JsonProperty("accountNumber")
    private String accountNumberCamel;
    @JsonProperty("storeInstrument")
    private String storeInstrument;
    @JsonProperty("txnAmount")
    private TxnAmount txnAmount;
    @JsonProperty("extendInfo")

    private ExtendInfo extendInfo;
    @JsonProperty("riskExtendInfo")
    private String riskExtendInfo;
    @JsonProperty("requestType")
    @JsonIgnore
    private String requestType;
    @JsonProperty("addMoney")
    private Integer addMoney;
    @JsonProperty("ecomTokenInfo")
    private EcomTokenInfo ecomTokenInfo;
    @JsonProperty("MERC_UNQ_REF")
    private String MERC_UNQ_REF;
    @JsonProperty("osType")
    private String osType;
    @JsonProperty("pspApp")
    private String pspApp;
    @JsonProperty("convertToAddAndPayTxn")
    private boolean convertToAddAndPayTxn;
    @JsonProperty("preferredOtpPage")
    private String preferredOtpPage;
    @JsonProperty("cardTokenInfo")
    private CardTokenInfo cardTokenInfo;
    @JsonProperty("tipDetails")
    private TipDetails tipDetails;
    @JsonProperty("creditBlock")
    private String creditBlock;
    @JsonProperty("hybridPayModeDetails")
    private List<HybridPayModeDetail> hybridPayModeDetails;
    @JsonProperty("qrImageRequired")
    private Boolean qrImageRequired;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("superCashOffer")
    private SuperCashOffer superCashOffer;

    @JsonProperty("upiLiteRequestData")
    private UpiLiteRequestData upiLiteRequestData;

    @JsonProperty("emiSubventionInfo")
    public EmiSubventionInfo getemiSubventionInfo() {
        return emiSubventionInfo;
    }

    @JsonProperty("seqNumber")
    private String seqNumber;

    @JsonProperty("upiAccRefId")
    private String upiAccRefId;

    @JsonProperty("emiSubventionInfo")
    public Body setemiSubventionInfo(EmiSubventionInfo emiSubventionInfo) {
        this.emiSubventionInfo = emiSubventionInfo;
        return this;
    }

    @JsonProperty("addMoney")
    public Integer getAddMoney() {
        return addMoney;
    }

    @JsonProperty("addMoney")
    public Body setAddMoney(Integer addMoney) {
        this.addMoney = addMoney;
        return this;
    }

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public Body setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("qrCodeId")
    public String getqrCodeId() {
        return qrCodeId;
    }
    @JsonProperty("qrCodeId")
    public Body setqrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
        return this;
    }
    @JsonProperty("merchantVpa")
    public String getmerchantVpa(){return merchantVpa;}
    @JsonProperty("merchantVpa")
    public Body setmerchantVpa(String merchantVpa){
        this.merchantVpa = merchantVpa;
        return this;
    }

    @JsonProperty("MERC_UNQ_REF")
    public String getMERC_UNQ_REF() {
        return MERC_UNQ_REF;
    }

    @JsonProperty("MERC_UNQ_REF")
    public Body setMERC_UNQ_REF(String MERC_UNQ_REF) {
        this.MERC_UNQ_REF = MERC_UNQ_REF;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("sso")
    public String getSso() {
        return sso;
    }

    @JsonProperty("sso")
    public Body setSso(String sso) {
        this.sso = sso;
        return this;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public Body setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
        return this;
    }

    @JsonProperty("cardInfo")
    public String getCardInfo() {
        return cardInfo;
    }

    @JsonProperty("cardInfo")
    public Body setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
        return this;
    }

    @JsonProperty("coftConsent")
    public Body setCoftConsent(CoftConsent coftConsent) {
        this.coftConsent = coftConsent;
        return this;
    }

    @JsonProperty("authMode")
    public String getAuthMode() {
        return authMode;
    }

    @JsonProperty("authMode")
    public Body setAuthMode(String authMode) {
        this.authMode = authMode;
        return this;
    }

    @JsonProperty("cardPreAuthType")
    public String getCardPreAuthType() {
        return cardPreAuthType;
    }

    @JsonProperty("cardPreAuthType")
    public Body setCardPreAuthType(String cardPreAuthType) {
        this.cardPreAuthType = cardPreAuthType;
        return this;
    }

    @JsonProperty("mandateAuthMode")
    public String getMandateAuthMode() {
        return mandateAuthMode;
    }

    @JsonProperty("mandateAuthMode")
    public Body setMandateAuthMode(String mandateAuthMode) {
        this.mandateAuthMode = mandateAuthMode;
        return this;
    }

    @JsonProperty("bankIfsc")
    public String getBankIfsc() {
        return bankIfsc;
    }

    @JsonProperty("bankIfsc")
    public Body setBankIfsc(String bankIfsc) {
        this.bankIfsc = bankIfsc;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public Body setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @JsonProperty("userName")
    public String getUserNameCamel() {
        return userNameCamel;
    }

    @JsonProperty("userName")
    public Body setUserNameCamel(String userNameCamel) {
        this.userNameCamel = userNameCamel;
        return this;
    }

    @JsonProperty("account_holder_name")
    public String getAccountHolderName() {
        return accountHolderName;
    }

    @JsonProperty("account_holder_name")
    public Body setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        return this;
    }

    public String getAccountType() {
        return accountType;
    }

    public Body setAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }

    @JsonProperty("accountType")
    public String getAccountTypeCamel() {
        return accountTypeCamel;
    }

    @JsonProperty("accountType")
    public Body setAccountTypeCamel(String accountTypeCamel) {
        this.accountTypeCamel = accountTypeCamel;
        return this;
    }

    public String getIndustryTypeId() {
        return industryTypeId;
    }

    public Body setIndustryTypeId(String industryTypeId) {
        this.industryTypeId = industryTypeId;
        return this;
    }

    @JsonProperty("merchantKey")
    public String getMerchantKey() {
        return merchantKey;
    }

    @JsonProperty("merchantKey")
    public Body setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
        return this;
    }

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public Body setChannelCode(String channelCode) {
        this.channelCode = channelCode;
        return this;
    }

    @JsonProperty("custId")
    public String getCustId() {
        return custId;
    }

    @JsonProperty("custId")
    public Body setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    @JsonProperty("saveForFuture")
    public String getSaveForFuture() {
        return saveForFuture;
    }

    @JsonProperty("saveForFuture")
    public Body setSaveForFuture(String saveForFuture) {
        this.saveForFuture = saveForFuture;
        return this;
    }

    @JsonProperty("paymentFlow")
    public String getPaymentFlow() {
        return paymentFlow;
    }

    @JsonProperty("paymentFlow")
    public Body setPaymentFlow(String paymentFlow) {
        this.paymentFlow = paymentFlow;
        return this;
    }

    @JsonProperty("payerAccount")
    public String getPayerAccount() {
        return payerAccount;
    }

    @JsonProperty("payerAccount")
    public Body setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
        return this;
    }

    @JsonProperty("website")
    public String getWebsite() {
        return website;
    }

    @JsonProperty("website")
    public Body setWebsite(String website) {
        this.website = website;
        return this;
    }

    @JsonProperty("mpin")
    public String getMpin() {
        return mpin;
    }

    @JsonProperty("mpin")
    public Body setMpin(String mpin) {
        this.mpin = mpin;
        return this;
    }

    @JsonProperty("planId")
    public String getPlanId() {
        return planId;
    }

    @JsonProperty("planId")
    public Body setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("emiType")
    public String getEmiType() {
        return emiType;
    }

    @JsonProperty("emiType")
    public Body setEmiType(String emiType) {
        this.emiType = emiType;
        return this;
    }

    @JsonProperty("encCardInfo")
    public String getEncCardInfo() {
        return encCardInfo;
    }

    @JsonProperty("encCardInfo")
    public Body setEncCardInfo(String encCardInfo) {
        this.encCardInfo = encCardInfo;
        return this;
    }

    @JsonProperty("PROMO_CAMP_ID")
    public String getPROMOCAMPID() {
        return pROMOCAMPID;
    }

    @JsonProperty("PROMO_CAMP_ID")
    public Body setPROMOCAMPID(String pROMOCAMPID) {
        this.pROMOCAMPID = pROMOCAMPID;
        return this;
    }

    @JsonProperty("cardTokenRequired")
    public String getCardTokenRequired() {
        return cardTokenRequired;
    }

    @JsonProperty("cardTokenRequired")
    public Body setCardTokenRequired(String cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
        return this;
    }

    @JsonProperty("aggMid")
    public String getAggMid() {
        return aggMid;
    }

    @JsonProperty("aggMid")
    public Body setAggMid(String aggMid) {
        this.aggMid = aggMid;
        return this;
    }

    @JsonProperty("aggType")
    public String getAggType() {
        return aggType;
    }

    @JsonProperty("aggType")
    public Body setAggType(String aggType) {
        this.aggType = aggType;
        return this;
    }

    @JsonProperty("SUBSCRIPTION_ID")
    public String getSubsId() {
        return subsId;
    }

    @JsonProperty("SUBSCRIPTION_ID")
    public Body setSubsId(String subsId) {
        this.subsId = subsId;
        return this;
    }

    @JsonProperty("account_number")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("account_number")
    public Body setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumberCamel() {
        return accountNumberCamel;
    }

    @JsonProperty("accountNumber")
    public Body setAccountNumberCamel(String accountNumberCamel) {
        this.accountNumberCamel = accountNumberCamel;
        return this;
    }

    @JsonProperty("storeInstrument")
    public String getStoreInstrument() {
        return storeInstrument;
    }

    @JsonProperty("storeInstrument")
    public Body setStoreInstrument(String storeInstrument) {
        this.storeInstrument = storeInstrument;
        return this;
    }

    @JsonProperty("txnAmount")
    public TxnAmount getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(TxnAmount txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public Body setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    @JsonProperty("riskExtendInfo")
    public String getRiskExtendInfo() {
        return riskExtendInfo;
    }

    @JsonProperty("riskExtendInfo")
    public Body setRiskExtendInfo(String riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
        return this;
    }

    @JsonProperty("ecomTokenInfo")
    public EcomTokenInfo getEcomTokenInfo() {
        return ecomTokenInfo;
    }

    @JsonProperty("ecomTokenInfo")
    public Body setEcomTokenInfo(EcomTokenInfo ecomTokenInfo) {
        this.ecomTokenInfo = ecomTokenInfo;
        return this;
    }

    @JsonProperty("requestType")
    public Body setpspApp(String pspApp) {
        this.pspApp = pspApp;
        return this;
    }
    @JsonProperty("requestType")
    public Body setosType(String osType) {
        this.osType = osType;
        return this;
    }

    @JsonProperty("convertToAddAndPayTxn")
    public Body setconvertToAddAndPayTxn(boolean convertToAddAndPayTxn) {
        this.convertToAddAndPayTxn = convertToAddAndPayTxn;
        return this;
    }
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
    @JsonProperty("preferredOtpPage")
    public Body setPreferredOtpPage(String preferredOtpPage) {
        this.preferredOtpPage = preferredOtpPage;
        return this;
    }
    @JsonProperty("upiLiteRequestData")
    public UpiLiteRequestData getUpiLiteRequestData(){return upiLiteRequestData;}
    @JsonProperty("upiLiteRequestData")
    public Body setUpiLiteRequestData(UpiLiteRequestData upiLiteRequestData) {
        this.upiLiteRequestData = upiLiteRequestData;
        return this;
    }

    @JsonProperty("superCashOffer")
    public SuperCashOffer getSuperCashOffer(){return superCashOffer;}
    @JsonProperty("superCashOffer")
    public Body setSuperCashOffer(SuperCashOffer superCashOffer) {
        this.superCashOffer = superCashOffer;
        return this;
    }
    @JsonProperty("seqNumber")
    public String getSeqNumber() {
        return seqNumber;
    }

    @JsonProperty("seqNumber")
    public Body setSeqNumber(String seqNumber) {
        this.seqNumber = seqNumber;
        return this;
    }
    @JsonProperty("upiAccRefId")
    public String getUpiAccRefId() {
        return upiAccRefId;
    }

    @JsonProperty("upiAccRefId")
    public Body setUpiAccRefId(String upiAccRefId) {
        this.upiAccRefId = upiAccRefId;
        return this;
    }
    @JsonProperty("cardTokenInfo")
    public CardTokenInfo getcardTokenInfo() {
        return cardTokenInfo;
    }
    @JsonProperty("cardTokenInfo")
    public Body setcardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
        return this;
    }
    @JsonProperty("tipDetails")
    public TipDetails getTipDetails() {
        return tipDetails;
    }
    @JsonProperty("tipDetails")
    public Body setTipDetails(TipDetails tipDetails) {
        this.tipDetails = tipDetails;
        return this;
    }

    @JsonProperty("creditBlock")
    public String getCreditBlock() {
        return creditBlock;
    }

    @JsonProperty("creditBlock")
    public Body setCreditBlock(String creditBlock) {
        this.creditBlock = creditBlock;
        return this;
    }

    @JsonProperty("hybridPayModeDetails")
    public List<HybridPayModeDetail> getHybridPayModeDetails() {
        return hybridPayModeDetails;
    }

    @JsonProperty("hybridPayModeDetails")
    public Body setHybridPayModeDetails(List<HybridPayModeDetail> hybridPayModeDetails) {
        this.hybridPayModeDetails = hybridPayModeDetails;
        return this;
    }
    @JsonProperty("qrImageRequired")
    public Body setQrImageRequired(Boolean qrImageRequired) {
        this.qrImageRequired = qrImageRequired;
        return this;
    }
    @JsonProperty("qrImageRequired")
    public Boolean getQrImageRequired() {
        return qrImageRequired;
    }

}