package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.Attachment;

import java.util.Map;
import java.util.StringJoiner;


public class OrderDTO {

    private String AUTH_MODE;
    private String TOKEN_TYPE;
    private String REQUEST_TYPE;
    private String TXN_AMOUNT;
    private String CUST_ID;
    private String MSISDN;
    private String SSO_TOKEN;
    private String GOODS_INFO;
    private String PAYTM_TOKEN;
    private String MID;
    private String merchantKey;
    private String INDUSTRY_TYPE_ID;
    private String WEBSITE;
    private String BANK_CODE;
    private String PAYMENT_DETAILS;
    private String STORE_CARD;
    private String addMoney;
    private String WALLET_AMOUNT;
    private String PAYMENT_TYPE_ID;
    private String SUBS_START_DATE;
    private String SUBS_EXPIRY_DATE;
    private String SUBS_PPI_ONLY;
    private String SUBS_AMOUNT_TYPE;
    private String SUBS_MAX_AMOUNT;
    private String SUBS_FREQUENCY;
    private String SUBS_FREQUENCY_UNIT;
    private String SUBS_GRACE_DAYS;
    private String SUBS_ENABLE_RETRY;
    private String SUBS_RETRY_COUNT;
    private String SUBS_PAYMENT_MODE;
    private String THEME;
    private String CHANNEL_ID;
    private String ORDER_ID;
    private String SAVED_CARD_ID;
    private String CONNECTION_TYPE;
    private String SUBS_SERVICE_ID;
    private String EMAIL;
    private String CC_BILL_NO;
    private String PASSCODE;
    private String ADDITIONAL_INFO;
    private String PROMO_CAMP_ID;
    private String TXN_TOKEN;
    private String fetchAllPaymentOffers;
    private String applyPaymentOffer;
    private String cardInfo;
    private String channelCode;
    private String paymentFlow;
    private String payerAccount;
    private String mpin;
    private String planId;
    private String encCardInfo;
    private String native_mid;
    private String native_orderId;
    private String native_channelId;
    private String native_txnToken;
    private String native_paymentMode;
    private String native_cardInfo;
    private String native_authMode;
    private String mandateAuthMode;
    private String native_channelCode;
    private String UDF_1;
    private String UDF_2;
    private String UDF_3;
    private String COMMENTS;
    private String PRN;
    private String PCC_CODE;
    private String UNIQUE_REFERENCE_VALUE;
    private String UNIQUE_REFERENCE_LABEL;
    private String POS_ID;
    private String MASKED_CUSTOMER_MOBILE_NUMBER;
    private String MERC_UNQ_REF;
    private String accountNumber; // mutual fund variable
    private String aggrMID;
    private String CHECKSUM;
    private String SUBSCRIPTION_ID;
    private String EMI_OPTIONS;
    private String Subwallet_Details;
    private String txnId = "";
    private String storeInstrument = "";
    private String IS_SAVED_CARD;
    private String cardTokenRequired;
    @JsonProperty("PAYMENT_MODE_ONLY")
    private String PAYMENT_MODE_ONLY;
    @JsonProperty("PAYMENT_MODE_DISABLE")
    private String PAYMENT_MODE_DISABLE;
    private String account_number;
    private String bankIfsc;
    private String ACCOUNT_TYPE;
    private String USER_NAME;
    /** CamelCase init/JSON key; omitted from payload when empty. */
    private String accountType;
    /** Serialized as {@code USER_NAME} in API payloads. */
    private String userName;
    private String EMI_TYPE;
    private String splitSettlementInfo;
    private String templateId;
    private String bId;
    private String corporateCustId;
    private String ENC_DATA;
    private String riskExtendInfo;
    private String extendInfo;
    private String MobileNumber;
    private String appVersion;
    private String ACCOUNT_REF_ID;
    private String BANK_IFSC;
    private String subscriptionPurpose;
    private String IDEBIT_OPTION;
    private String merchantCode;
    private String msg;
    private String risk_extended_info;
    private String locale;
    private String LINK_NAME;
    private String SHORT_URL;
    private String LONG_URL;
    private String LINK_DESCRIPTION;
    private String LINK_ID;
    private String CALLBACK_URL;
    private String ULTIMATE_BENEFICIARY_NAME;


    private OrderDTO(Builder builder) {
        this.AUTH_MODE = builder.AUTH_MODE;
        this.TOKEN_TYPE = builder.TOKEN_TYPE;
        this.REQUEST_TYPE = builder.REQUEST_TYPE;
        this.TXN_AMOUNT = builder.TXN_AMOUNT;
        this.CUST_ID = builder.CUST_ID;
        this.MSISDN = builder.MSISDN;
        this.SSO_TOKEN = builder.SSO_TOKEN;
        this.GOODS_INFO = builder.GOODS_INFO;
        this.PAYTM_TOKEN = builder.PAYTM_TOKEN;
        this.MID = builder.MID;
        this.merchantKey = builder.merchantKey;
        this.INDUSTRY_TYPE_ID = builder.INDUSTRY_TYPE_ID;
        this.WEBSITE = builder.WEBSITE;
        this.BANK_CODE = builder.BANK_CODE;
        this.PAYMENT_DETAILS = builder.PAYMENT_DETAILS;
        this.STORE_CARD = builder.STORE_CARD;
        this.addMoney = builder.addMoney;
        this.WALLET_AMOUNT = builder.WALLET_AMOUNT;
        this.PAYMENT_TYPE_ID = builder.PAYMENT_TYPE_ID;
        this.SUBS_START_DATE = builder.SUBS_START_DATE;
        this.SUBS_EXPIRY_DATE = builder.SUBS_EXPIRY_DATE;
        this.SUBS_PPI_ONLY = builder.SUBS_PPI_ONLY;
        this.SUBS_AMOUNT_TYPE = builder.SUBS_AMOUNT_TYPE;
        this.SUBS_MAX_AMOUNT = builder.SUBS_MAX_AMOUNT;
        this.SUBS_FREQUENCY = builder.SUBS_FREQUENCY;
        this.SUBS_FREQUENCY_UNIT = builder.SUBS_FREQUENCY_UNIT;
        this.SUBS_GRACE_DAYS = builder.SUBS_GRACE_DAYS;
        this.SUBS_ENABLE_RETRY = builder.SUBS_ENABLE_RETRY;
        this.SUBS_RETRY_COUNT = "";
        this.SUBS_PAYMENT_MODE = builder.SUBS_PAYMENT_MODE;
        this.THEME = builder.THEME;
        this.CHANNEL_ID = builder.CHANNEL_ID;
        this.ORDER_ID = builder.ORDER_ID;
        this.SAVED_CARD_ID = builder.SAVED_CARD_ID;
        this.CONNECTION_TYPE = builder.CONNECTION_TYPE;
        this.SUBS_SERVICE_ID = builder.SUBS_SERVICE_ID;
        this.EMAIL = builder.EMAIL;
        this.CC_BILL_NO = builder.CC_BILL_NO;
        this.PASSCODE = builder.PASSCODE;
        this.ADDITIONAL_INFO = builder.ADDITIONAL_INFO;
        this.TXN_TOKEN = builder.TXN_TOKEN;
        this.cardInfo = builder.cardInfo;
        this.applyPaymentOffer = builder.applyPaymentOffer;
        this.fetchAllPaymentOffers = builder.fetchAllPaymentOffers;
        this.channelCode = builder.channelCode;
        this.paymentFlow = builder.paymentFlow;
        this.payerAccount = builder.payerAccount;
        this.mpin = builder.mpin;
        this.planId = builder.planId;
        this.encCardInfo = builder.encCardInfo;
        this.native_mid = builder.native_mid;
        this.native_orderId = builder.native_orderId;
        this.native_channelId = builder.native_channelId;
        this.native_txnToken = builder.native_txnToken;
        this.native_paymentMode = builder.native_paymentMode;
        this.native_cardInfo = builder.native_cardInfo;
        this.native_authMode = builder.native_authMode;
        this.mandateAuthMode = builder.mandateAuthMode;
        this.native_channelCode = builder.native_channelCode;
        this.PROMO_CAMP_ID = builder.PROMO_CAMP_ID;
        this.UDF_1 = builder.UDF_1;
        this.UDF_2 = builder.UDF_2;
        this.UDF_3 = builder.UDF_3;
        this.COMMENTS = builder.COMMENTS;
        this.PRN = builder.PRN;
        this.PCC_CODE = builder.PCC_CODE;
        this.UNIQUE_REFERENCE_VALUE = builder.UNIQUE_REFERENCE_VALUE;
        this.UNIQUE_REFERENCE_LABEL = builder.UNIQUE_REFERENCE_LABEL;
        this.POS_ID = builder.POS_ID;
        this.MASKED_CUSTOMER_MOBILE_NUMBER = builder.MASKED_CUSTOMER_MOBILE_NUMBER;
        this.MERC_UNQ_REF = builder.MERC_UNQ_REF;
        this.accountNumber = builder.accountNumber;
        this.aggrMID = builder.aggMid;
        this.SUBSCRIPTION_ID = builder.SUBSCRIPTION_ID;
        this.EMI_OPTIONS = builder.EMI_OPTIONS;
        this.Subwallet_Details = builder.Subwallet_Details;
        this.IS_SAVED_CARD = builder.IS_SAVED_CARD;
        this.cardTokenRequired = builder.cardTokenRequired;
        this.PAYMENT_MODE_ONLY = builder.PAYMENT_MODE_ONLY;
        this.PAYMENT_MODE_DISABLE = builder.PAYMENT_MODE_DISABLE;
        this.account_number = builder.account_number;
        this.bankIfsc = builder.bankIfsc;
        this.ACCOUNT_TYPE = builder.ACCOUNT_TYPE;
        this.USER_NAME = builder.USER_NAME;
        this.accountType = builder.accountType;
        this.userName = builder.userName;
        this.splitSettlementInfo = builder.splitSettlementInfo;
        this.templateId = builder.templateId;
        this.ENC_DATA = builder.ENC_DATA;
        this.riskExtendInfo = builder.riskExtendInfo;
        this.extendInfo= builder.extendInfo;
        this.MobileNumber = builder.ContactNumber;
        this.bId = builder.bId;
        this.corporateCustId = builder.corporateCustId;
        this.appVersion = builder.appVersion;
        this.storeInstrument = builder.storeInstrument;
        this.ACCOUNT_REF_ID = builder.ACCOUNT_REF_ID;
        this.BANK_IFSC=builder.BANK_IFSC;
        this.subscriptionPurpose = builder.subscriptionPurpose;
        this.IDEBIT_OPTION = builder.IDEBIT_OPTION;
        this.EMI_TYPE = builder.EMI_TYPE;
        this.merchantCode = builder.merchantCode;
        this.msg = builder.msg;
        this.risk_extended_info = builder.risk_extended_info;
        this.locale = builder.locale;
        this.LINK_NAME = builder.LINK_NAME;
        this.SHORT_URL = builder.SHORT_URL;
        this.LONG_URL = builder.LONG_URL;
        this.LINK_DESCRIPTION = builder.LINK_DESCRIPTION;
        this.LINK_ID = builder.LINK_ID;
        this.CALLBACK_URL = builder.CALLBACK_URL;
        this.ULTIMATE_BENEFICIARY_NAME = builder.ULTIMATE_BENEFICIARY_NAME;
    }


    public String getTemplateId() {
        return templateId;
    }

    public String getbId() {
        return bId;
    }

    public String getCorporateCustId() {
        return corporateCustId;
    }

    public String getAppVersion(){
        return appVersion;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public OrderDTO setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    public String getENC_DATA() {
        return ENC_DATA;
    }

    public OrderDTO setENC_DATA(String ENC_DATA) {
        this.ENC_DATA = ENC_DATA;
        return this;
    }

    public String getMERC_UNQ_REF() {
        return MERC_UNQ_REF;
    }

    public OrderDTO setMERC_UNQ_REF(String MERC_UNQ_REF) {
        this.MERC_UNQ_REF = MERC_UNQ_REF;
        return this;
    }

    public String getTxnId() {
        return txnId;
    }

    public OrderDTO setTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    public String getStoreInstrument() {
        return storeInstrument;
    }

    public OrderDTO setStoreInstrument(String storeInstrument) {
        this.storeInstrument = storeInstrument;
        return this;
    }

    public OrderDTO setOrderId(String orderId) {
        this.ORDER_ID = orderId;
        return this;
    }

    public String getSubwallet_Details() {
        return Subwallet_Details;
    }

    public String getIsSaveCard(){
        return this.IS_SAVED_CARD;

    }

    public OrderDTO setSubwallet_Details(String subwallet_Details) {
        Subwallet_Details = subwallet_Details;
        return this;
    }

    @JsonProperty("account_number")
    public String getAccount_number() {
        return account_number;
    }

    @JsonProperty("bankIfsc")
    public String getBankIfsc() {
        return bankIfsc;
    }

    @JsonProperty("ACCOUNT_TYPE")
    public String getACCOUNT_TYPE() {
        return ACCOUNT_TYPE;
    }

    @JsonProperty("accountType")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getAccountType() {
        return accountType;
    }

    @JsonProperty("USER_NAME")
    public String getUSER_NAME() {
        return USER_NAME;
    }

    @JsonProperty("userName")
    public String getUserName() {
        return userName;
    }

    @JsonProperty("AUTH_MODE")
    public String getAUTH_MODE() {
        return AUTH_MODE;
    }

    @JsonProperty("TOKEN_TYPE")
    public String getTOKEN_TYPE() {
        return TOKEN_TYPE;
    }

    @JsonProperty("REQUEST_TYPE")
    public String getREQUEST_TYPE() {
        return REQUEST_TYPE;
    }

    @JsonProperty("TXN_AMOUNT")
    public String getTXN_AMOUNT() {
        return TXN_AMOUNT;
    }

    public OrderDTO setTXN_AMOUNT(String TXN_AMOUNT) {
       this.TXN_AMOUNT = TXN_AMOUNT;
       return this;
    }

    @JsonProperty("CUST_ID")
    public String getCUST_ID() {
        return CUST_ID;
    }

    @JsonProperty("MSISDN")
    public String getMobileNumber() {
        return MSISDN;
    }

    @JsonProperty("SSO_TOKEN")
    public String getSSO_TOKEN() {
        return SSO_TOKEN;
    }

    @JsonProperty("GOODS_INFO")
    public String getGOODS_INFO() {
        return GOODS_INFO;
    }

    @JsonProperty("PAYTM_TOKEN")
    public String getPAYTM_TOKEN() {
        return PAYTM_TOKEN;
    }

    @JsonProperty("MID")
    public String getMID() {
        return MID;
    }

    @JsonProperty("merchantKey")
    public String getMerchantKey() {
        return merchantKey;
    }

    public OrderDTO setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
        return this;
    }

    @JsonProperty("INDUSTRY_TYPE_ID")
    public String getINDUSTRY_TYPE_ID() {
        return INDUSTRY_TYPE_ID;
    }

    @JsonProperty("WEBSITE")
    public String getWEBSITE() {
        return WEBSITE;
    }

    @JsonProperty("BANK_CODE")
    public String getBANK_CODE() {
        return BANK_CODE;
    }

    @JsonProperty("PAYMENT_DETAILS")
    public String getPAYMENT_DETAILS() {
        return PAYMENT_DETAILS;
    }

    @JsonProperty("STORE_CARD")
    public String getSTORE_CARD() {
        return STORE_CARD;
    }

    @JsonProperty("addMoney")
    public String getAddMoney() {
        return addMoney;
    }

    @JsonProperty("WALLET_AMOUNT")
    public String getWALLET_AMOUNT() {
        return WALLET_AMOUNT;
    }

    @JsonProperty("PAYMENT_TYPE_ID")
    public String getPAYMENT_TYPE_ID() {
        return PAYMENT_TYPE_ID;
    }

    @JsonProperty("SUBS_START_DATE")
    public String getSUBS_START_DATE() {
        return SUBS_START_DATE;
    }

    @JsonProperty("SUBS_EXPIRY_DATE")
    public String getSUBS_EXPIRY_DATE() {
        return SUBS_EXPIRY_DATE;
    }

    @JsonProperty("SUBS_PPI_ONLY")
    public String getSUBS_PPI_ONLY() {
        return SUBS_PPI_ONLY;
    }

    @JsonProperty("SUBS_AMOUNT_TYPE")
    public String getSUBS_AMOUNT_TYPE() {
        return SUBS_AMOUNT_TYPE;
    }

    @JsonProperty("SUBS_MAX_AMOUNT")
    public String getSUBS_MAX_AMOUNT() {
        return SUBS_MAX_AMOUNT;
    }

    @JsonProperty("SUBS_FREQUENCY")
    public String getSUBS_FREQUENCY() {
        return SUBS_FREQUENCY;
    }

    @JsonProperty("SUBS_FREQUENCY_UNIT")
    public String getSUBS_FREQUENCY_UNIT() {
        return SUBS_FREQUENCY_UNIT;
    }

    @JsonProperty("SUBS_GRACE_DAYS")
    public String getSUBS_GRACE_DAYS() {
        return SUBS_GRACE_DAYS;
    }

    @JsonProperty("SUBS_ENABLE_RETRY")
    public String getSUBS_ENABLE_RETRY() {
        return SUBS_ENABLE_RETRY;
    }

    @JsonProperty("SUBS_RETRY_COUNT")
    public String getSUBS_RETRY_COUNT() {
        return SUBS_RETRY_COUNT;
    }

    @JsonProperty("SUBS_PAYMENT_MODE")
    public String getSUBS_PAYMENT_MODE() {
        return SUBS_PAYMENT_MODE;
    }

    @JsonProperty("THEME")
    public String getTHEME() {
        return THEME;
    }

    @JsonProperty("CHANNEL_ID")
    public String getCHANNEL_ID() {
        return CHANNEL_ID;
    }

    @JsonProperty("ORDER_ID")
    public String getORDER_ID() {
        return ORDER_ID;
    }

    @JsonProperty("SAVED_CARD_ID")
    public String getSAVED_CARD_ID() {
        return SAVED_CARD_ID;
    }

    @JsonProperty("CONNECTION_TYPE")
    public String getCONNECTION_TYPE() {
        return CONNECTION_TYPE;
    }

    @JsonProperty("SUBS_SERVICE_ID")
    public String getSUBS_SERVICE_ID() {
        return SUBS_SERVICE_ID;
    }

    @JsonProperty("EMAIL")
    public String getEMAIL() {
        return EMAIL;
    }

    @JsonProperty("CC_BILL_NO")
    public String getCC_BILL_NO() {
        return CC_BILL_NO;
    }

    @JsonProperty("PASSCODE")
    public String getPASSCODE() {
        return PASSCODE;
    }

    @JsonProperty("ADDITIONAL_INFO")
    public String getADDITIONAL_INFO() {
        return ADDITIONAL_INFO;
    }

    @JsonProperty("TXN_TOKEN")
    public String getTXN_TOKEN() {
        return TXN_TOKEN;
    }

    @JsonProperty("EMI_TYPE")
    public String getEMI_TYPE() {
        return EMI_TYPE;
    }

    @JsonProperty("locale")
    public String getLocale() {
        return this.locale;
    }

    @JsonProperty("LINK_ID")
    public String getLINKID(){
        return LINK_ID;
    }

    @JsonProperty("LINK_NAME")
    public String getLinkName() {
        return LINK_NAME;
    }

    @JsonProperty("SHORT_URL")
    public String getShortUrl(){
        return SHORT_URL;
    }

    @JsonProperty("LONG_URL")
    public String getLongUrl(){
        return LONG_URL;
    }

    @JsonProperty("LINK_DESCRIPTION")
    public String getLinkDescription(){
        return LINK_DESCRIPTION;
    }

    @JsonProperty("CALLBACK_URL")
    public String getCallBackURL(){
        return CALLBACK_URL;
    }

    @JsonProperty("ULTIMATE_BENEFICIARY_NAME")
    public String getUltimateBeneficiaryName(){
        return ULTIMATE_BENEFICIARY_NAME;
    }

    public String getApplyPaymentOffer() {
        return applyPaymentOffer;
    }

    public String getFetchAllPaymentOffers(){
        return fetchAllPaymentOffers;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getPaymentFlow() {
        return paymentFlow;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public String getMpin() {
        return mpin;
    }

    public String getPlanId() {
        return planId;
    }

    public String getEncCardInfo() {
        return encCardInfo;
    }

    public String getNative_mid() {
        return native_mid;
    }

    public String getNative_orderId() {
        return native_orderId;
    }

    public String getNative_channelId() {
        return native_channelId;
    }

    public String getNative_txnToken() {
        return native_txnToken;
    }

    public String getNative_paymentMode() {
        return native_paymentMode;
    }

    public String getNative_cardInfo() {
        return native_cardInfo;
    }

    public String getNative_authMode() {
        return native_authMode;
    }

    public String getMandateAuthMode() {
        return mandateAuthMode;
    }

    public String getNative_channelCode() {
        return native_channelCode;
    }

    @JsonProperty("SUBSCRIPTION_ID")
    public String getSUBSCRIPTION_ID(){
        return this.SUBSCRIPTION_ID;
    }

    public void setSUBSCRIPTION_ID(String subsID){
        this.SUBSCRIPTION_ID = subsID;
    }


    public String getPROMO_CAMP_ID() {
        return PROMO_CAMP_ID;
    }


    public String getUDF2() {
        return UDF_2;
    }

    @JsonProperty("CHECKSUMHASH")
    public String getChecksum() {
        return CHECKSUM;
    }

    public OrderDTO setChecksum(String checksum) {
        this.CHECKSUM = checksum;
        return this;
    }

    //@JsonProperty("SUBSCRIPTION_ID")
    /*public String getSUBS_ID() {
        return SUBSCRIPTION_ID;
    }

    public OrderDTO setSUBS_ID(String SUBS_ID) {
        this.SUBSCRIPTION_ID = SUBS_ID;
        return this;
    }*/
    @JsonProperty("templateID")
    public String getTemplateID() {
        return templateId;
    }

    public String getEMI_OPTIONS() {
        return EMI_OPTIONS;
    }

    public OrderDTO setEMI_OPTIONS(String EMI_OPTIONS) {
        this.EMI_OPTIONS = EMI_OPTIONS;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccount_number(String account_number){
        this.account_number = account_number;
    }

    public String getAggrMid() {
        return aggrMID;
    }

    public void setAggrMid(String aggMid) {
        this.aggrMID = aggMid;
    }

    public OrderDTO setPAYMENT_DETAILS(String PAYMENT_DETAILS) {
        this.PAYMENT_DETAILS = PAYMENT_DETAILS;
        return this;
    }

    public String getCardTokenRequired() {
        return cardTokenRequired;
    }

    @JsonProperty("PAYMENT_MODE_ONLY")
    public String getPAYMENT_MODE_ONLY() {
        return PAYMENT_MODE_ONLY;
    }

    @JsonProperty("PAYMENT_MODE_DISABLE")
    public String getPAYMENT_MODE_DISABLE() {
        return PAYMENT_MODE_DISABLE;
    }

    public String getSplitSettlementInfo() {
        return splitSettlementInfo;
    }

    @JsonProperty("ContactNumber")
    public String getContactNumber() {
        return MobileNumber;
    }

    public String getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public OrderDTO setRiskExtendInfo(String riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
        return this;
    }


    public String getACCOUNT_REF_ID() {
        return ACCOUNT_REF_ID;
    }

    public OrderDTO setACCOUNT_REF_ID(String ACCOUNT_REF_ID) {
        this.ACCOUNT_REF_ID = ACCOUNT_REF_ID;
        return this;
    }

    public String getBANK_IFSC() {
        return BANK_IFSC;
    }

    public OrderDTO setBANK_IFSC(String BANK_IFSC) {
        this.BANK_IFSC = BANK_IFSC;
        return this;
    }

    public String getSubscriptionPurpose() {
        return subscriptionPurpose;
    }

    public OrderDTO setSubscriptionPurpose(String subscriptionPurpose) {
        this.subscriptionPurpose = subscriptionPurpose;
        return this;
    }

    public String getIDEBIT_OPTION() {
        return IDEBIT_OPTION;
    }

    public OrderDTO setIDEBIT_OPTION(String IDEBIT_OPTION) {
        this.IDEBIT_OPTION = IDEBIT_OPTION;
        return this;
    }


    public String getMerchantCode() {
        return merchantCode;
    }

    public OrderDTO setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public OrderDTO setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getRisk_extended_info() {
        return risk_extended_info;
    }

    public OrderDTO setRisk_extended_info(String riskExtendInfo) {
        this.risk_extended_info = riskExtendInfo;
        return this;
    }

    public String asQuery() {
        return this.asJSON()//TODO issue with merchant checkout page bean validation logic -> not able to enter bean if any value contains '&' because logic splits string on '&', so not able to parse; some of our merchant keys have & in them so not able to enter
                .trim()
                .replaceAll("\n", "")
                .replaceAll(" ", "")
                .replaceAll("\\{", "")
                .replaceAll("\\}", "")
                .replaceAll("\"", "")
                .replaceAll(":", "=")
                .replaceAll(",", "&");
    }


    @Override
    public String toString() {
        return this.asJSON();
    }

    @Attachment(value = "Order", type = "application/json")
    private String asJSON() {
        //TODO try plugin method to achieve this as explained in allure doc
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);//TODO sort without case-sensitivity
        String orderAsJSON = "";
        try {
            orderAsJSON = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return orderAsJSON;
    }


    public static class Builder {

        String AUTH_MODE = "";
        String TOKEN_TYPE = "";
        String REQUEST_TYPE = "";
        String TXN_AMOUNT = "";
        String CUST_ID = "";
        String MSISDN = "";
        String SSO_TOKEN = "";
        String GOODS_INFO = "";
        String PAYTM_TOKEN = "";
        String MID = "";
        String merchantKey = "";
        String INDUSTRY_TYPE_ID = "";
        String WEBSITE = "";
        String BANK_CODE = "";
        String PAYMENT_DETAILS = "";
        String STORE_CARD = "";
        String addMoney = "";
        String WALLET_AMOUNT = "";
        String PAYMENT_TYPE_ID = "";
        String SUBS_START_DATE = "";
        String SUBS_EXPIRY_DATE = "";
        String SUBS_PPI_ONLY = "";
        String SUBS_AMOUNT_TYPE = "";
        String SUBS_MAX_AMOUNT = "";
        String SUBS_FREQUENCY = "";
        String SUBS_FREQUENCY_UNIT = "";
        String SUBS_GRACE_DAYS = "";
        String SUBS_ENABLE_RETRY = "";
        String SUBS_RETRY_COUNT = "";
        String SUBS_PAYMENT_MODE = "";
        String THEME = "";
        String CHANNEL_ID = "";
        String ORDER_ID = "";
        String SAVED_CARD_ID = "";
        String CONNECTION_TYPE = "";
        String SUBS_SERVICE_ID = "";
        String EMAIL = "";
        String CC_BILL_NO = "";
        String PASSCODE = "";
        String ADDITIONAL_INFO = "";
        String TXN_TOKEN = "";
        String fetchAllPaymentOffers = "";
        String applyPaymentOffer = "";
        String cardInfo = "";
        String channelCode = "";
        String paymentFlow = "";
        String payerAccount = "";
        String mpin = "";
        String planId = "";
        String encCardInfo = "";
        String native_mid = "";
        String native_orderId = "";
        String native_channelId = "";
        String native_txnToken = "";
        String native_paymentMode = "";
        String native_cardInfo = "";
        String native_authMode = "";
        String mandateAuthMode = "";
        String native_channelCode = "";
        String UDF_1 = "";
        String UDF_2 = "";
        String UDF_3 = "";
        String COMMENTS = "";
        String PRN = "";
        String PCC_CODE = "";
        String UNIQUE_REFERENCE_VALUE = "";
        String UNIQUE_REFERENCE_LABEL = "";
        String POS_ID = "";
        String MASKED_CUSTOMER_MOBILE_NUMBER = "";
        String MERC_UNQ_REF = "";
        //String SUBS_ID = "";
        String EMI_OPTIONS = "";
        String Subwallet_Details = "";
        String accountNumber = "";
        String aggMid = "";
        String PROMO_CAMP_ID = "";
        String IS_SAVED_CARD = "";
        String SUBSCRIPTION_ID= "";
        String cardTokenRequired = "";
        String PAYMENT_MODE_ONLY = "";
        String PAYMENT_MODE_DISABLE = "";
        String EMI_TYPE="";
        String account_number = "";
        String bankIfsc = "";
        String ACCOUNT_TYPE = "";
        String USER_NAME = "";
        String accountType = "";
        String userName = "";
        String splitSettlementInfo = "";
        String templateId="";
        String bId = "";
        String corporateCustId = "";
        String ENC_DATA = "";
        String riskExtendInfo ="";
        String ContactNumber="";
        String extendInfo = "";
        String appVersion = "";
        String storeInstrument = "";
        String ACCOUNT_REF_ID = "";
        String BANK_IFSC="";
        String subscriptionPurpose="";
        String IDEBIT_OPTION="";
        String merchantCode="";
        String msg="";
        String locale = "";
        String LINK_NAME = "";
        String SHORT_URL = "";
        String LONG_URL = "";
        String LINK_DESCRIPTION = "";
        String LINK_ID = "";
        String CALLBACK_URL = "";
        String risk_extended_info;
        String ULTIMATE_BENEFICIARY_NAME = "";


        public Builder() {
        }


        public String getbId() {
            return bId;
        }

        public Builder setbId(String bId) {
            this.bId = bId;
            return this;
        }

        public String getCorporateCustId() {
            return corporateCustId;
        }

        public Builder setCorporateCustId(String corporateCustId) {
            this.corporateCustId = corporateCustId;
            return this;
        }

        public String getAppVersion() {return appVersion;}

        public Builder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public String getENC_DATA() {
            return ENC_DATA;
        }

        public Builder setENC_DATA(String ENC_DATA) {
            this.ENC_DATA = ENC_DATA;
            return this;
        }

        public String getMERC_UNQ_REF() {
            return MERC_UNQ_REF;
        }

        public Builder setMERC_UNQ_REF(String MERC_UNQ_REF) {
            this.MERC_UNQ_REF = MERC_UNQ_REF;
            return this;
        }

        public String getAccount_number() {
            return account_number;
        }

        public Builder setAccount_number(String account_number) {
            this.account_number = account_number;
            return this;
        }

        public String getBankIfsc() {
            return bankIfsc;
        }

        public Builder setBankIfsc(String bankIfsc) {
            this.bankIfsc = bankIfsc;
            return this;
        }

        public String getACCOUNT_TYPE() {
            return ACCOUNT_TYPE;
        }

        public Builder setACCOUNT_TYPE(String ACCOUNT_TYPE) {
            this.ACCOUNT_TYPE = ACCOUNT_TYPE;
            return this;
        }

        public String getUSER_NAME() {
            return USER_NAME;
        }

        public Builder setUSER_NAME(String USER_NAME) {
            this.USER_NAME = USER_NAME;
            return this;
        }

        public String getAccountType() {
            return accountType;
        }

        public Builder setAccountType(String accountType) {
            this.accountType = accountType;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /* public String getSUBSCRIPTION_ID() {
            return SUBSCRIPTION_ID;
        }*/

        public Builder setSUBSCRIPTION_ID(String SUBSCRIPTION_ID) {
            this.SUBSCRIPTION_ID = SUBSCRIPTION_ID;
            return this;
        }


        public String getSubwallet_Details() {
            return Subwallet_Details;
        }

        public Builder setSubwallet_Details(String subwallet_Details) {
            Subwallet_Details = subwallet_Details;
            return this;
        }

        public Builder setIsSaveCard(String saveCard){
            IS_SAVED_CARD = saveCard;
            return this;
        }


        public Builder setPROMO_CAMP_ID(String PROMO_CAMP_ID) {
            this.PROMO_CAMP_ID = PROMO_CAMP_ID;
            return this;
        }


        public String getChannel(String theme) {
            if (theme.equalsIgnoreCase("merchantLow5") || theme.equalsIgnoreCase("merchantLow")) {
                return "WAP";
            } else {
                return "WEB";
            }
        }

        public Builder setCardInfo(String cardInfo) {
            this.cardInfo = cardInfo;
            return this;
        }

        public Builder setChannelCode(String channelCode) {
            this.channelCode = channelCode;
            return this;
        }

        public Builder setPaymentFlow(String paymentFlow) {
            this.paymentFlow = paymentFlow;
            return this;
        }

        public Builder setPayerAccount(String payerAccount) {
            this.payerAccount = payerAccount;
            return this;
        }

        public Builder setMpin(String mpin) {
            this.mpin = mpin;
            return this;
        }

        public Builder setPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder setEncCardInfo(String encCardInfo) {
            this.encCardInfo = encCardInfo;
            return this;
        }

        public Builder setTXN_TOKEN(String TXN_TOKEN) {
            this.TXN_TOKEN = TXN_TOKEN;
            return this;
        }

        public Builder setAUTH_MODE(String AUTH_MODE) {
            this.AUTH_MODE = AUTH_MODE;
            return this;
        }

        public Builder setTOKEN_TYPE(String TOKEN_TYPE) {
            this.TOKEN_TYPE = TOKEN_TYPE;
            return this;
        }

        public Builder setREQUEST_TYPE(String REQUEST_TYPE) {
            this.REQUEST_TYPE = REQUEST_TYPE;
            return this;
        }

        public Builder setTXN_AMOUNT(String TXN_AMOUNT) {
            this.TXN_AMOUNT = TXN_AMOUNT;
            return this;
        }

        public Builder setStoreInstrument(String storeInstrument) {
            this.storeInstrument = storeInstrument;
            return this;
        }

        public Builder setCUST_ID(String CUST_ID) {
            this.CUST_ID = CUST_ID;
            return this;
        }

        public Builder setMSISDN(String MSISDN) {
            this.MSISDN = MSISDN;
            return this;
        }

        public Builder setSSO_TOKEN(String SSO_TOKEN) {
            this.SSO_TOKEN = SSO_TOKEN;
            return this;
        }

        public Builder setGoodsInfo(String GOODS_INFO) {
            this.GOODS_INFO = GOODS_INFO;
            return this;
        }

        public Builder setPAYTM_TOKEN(String PAYTM_TOKEN) {
            this.PAYTM_TOKEN = PAYTM_TOKEN;
            return this;
        }

        public Builder setMID(String MID) {
            this.MID = MID;
            return this;
        }

        public Builder setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
            return this;
        }

        public Builder setINDUSTRY_TYPE_ID(String INDUSTRY_TYPE_ID) {
            this.INDUSTRY_TYPE_ID = INDUSTRY_TYPE_ID;
            return this;
        }

        public Builder setWEBSITE(String WEBSITE) {
            this.WEBSITE = WEBSITE;
            return this;
        }

        public Builder setBANK_CODE(String BANK_CODE) {
            this.BANK_CODE = BANK_CODE;
            return this;
        }

        public Builder setPAYMENT_DETAILS(String PAYMENT_DETAILS) {
            this.PAYMENT_DETAILS = PAYMENT_DETAILS;
            return this;
        }

        public Builder setSTORE_CARD(String STORE_CARD) {
            this.STORE_CARD = STORE_CARD;
            return this;
        }


        // mutual fund changes

        public Builder setAggMid(String aggMid) {
            this.aggMid = aggMid;
            return this;
        }

        public String getPROMO_CAMP_ID() {
            return PROMO_CAMP_ID;
        }

        public Builder setAddMoney(String addMoney) {
            this.addMoney = addMoney;
            return this;
        }

        public Builder setWALLET_AMOUNT(String WALLET_AMOUNT) {
            this.WALLET_AMOUNT = WALLET_AMOUNT;
            return this;
        }

        public Builder setPAYMENT_TYPE_ID(String PAYMENT_TYPE_ID) {
            this.PAYMENT_TYPE_ID = PAYMENT_TYPE_ID;
            return this;
        }

        public Builder setSUBS_START_DATE(String SUBS_START_DATE) {
            this.SUBS_START_DATE = SUBS_START_DATE;
            return this;
        }

        public Builder setSUBS_EXPIRY_DATE(String SUBS_EXPIRY_DATE) {
            this.SUBS_EXPIRY_DATE = SUBS_EXPIRY_DATE;
            return this;
        }

        public Builder setSUBS_PPI_ONLY(String SUBS_PPI_ONLY) {
            this.SUBS_PPI_ONLY = SUBS_PPI_ONLY;
            return this;
        }

        public Builder setSUBS_AMOUNT_TYPE(String SUBS_AMOUNT_TYPE) {
            this.SUBS_AMOUNT_TYPE = SUBS_AMOUNT_TYPE;
            return this;
        }

        public Builder setSUBS_MAX_AMOUNT(String SUBS_MAX_AMOUNT) {
            this.SUBS_MAX_AMOUNT = SUBS_MAX_AMOUNT;
            return this;
        }

        public Builder setSUBS_FREQUENCY(String SUBS_FREQUENCY) {
            this.SUBS_FREQUENCY = SUBS_FREQUENCY;
            return this;
        }

        public Builder setSUBS_FREQUENCY_UNIT(String SUBS_FREQUENCY_UNIT) {
            this.SUBS_FREQUENCY_UNIT = SUBS_FREQUENCY_UNIT;
            return this;
        }

        public Builder setSUBS_GRACE_DAYS(String SUBS_GRACE_DAYS) {
            this.SUBS_GRACE_DAYS = SUBS_GRACE_DAYS;
            return this;
        }

        public Builder setSUBS_ENABLE_RETRY(String SUBS_ENABLE_RETRY) {
            this.SUBS_ENABLE_RETRY = SUBS_ENABLE_RETRY;
            return this;
        }

        public Builder setSUBS_RETRY_COUNT(String SUBS_RETRY_COUNT) {
            this.SUBS_RETRY_COUNT = SUBS_RETRY_COUNT;
            return this;
        }

        public Builder setSUBS_PAYMENT_MODE(String SUBS_PAYMENT_MODE) {
            this.SUBS_PAYMENT_MODE = SUBS_PAYMENT_MODE;
            return this;
        }

        public Builder setTHEME(String THEME) {
            this.THEME = THEME;
            return this;
        }

        public Builder setCHANNEL_ID(String CHANNEL_ID) {
            this.CHANNEL_ID = CHANNEL_ID;
            return this;
        }

        public Builder setORDER_ID(String ORDER_ID) {
            this.ORDER_ID = ORDER_ID;
            return this;
        }

        public Builder setSAVED_CARD_ID(String SAVED_CARD_ID) {
            this.SAVED_CARD_ID = SAVED_CARD_ID;
            return this;
        }

        public Builder setCONNECTION_TYPE(String CONNECTION_TYPE) {
            this.CONNECTION_TYPE = CONNECTION_TYPE;
            return this;
        }

        public Builder setSUBS_SERVICE_ID(String SUBS_SERVICE_ID) {
            this.SUBS_SERVICE_ID = SUBS_SERVICE_ID;
            return this;
        }

        public Builder setEMAIL(String EMAIL) {
            this.EMAIL = EMAIL;
            return this;
        }

        public Builder setPASSCODE(String PASSCODE) {
            this.PASSCODE = PASSCODE;
            return this;
        }

        public Builder setADDITIONAL_INFO(String ADDITIONAL_INFO) {
            this.ADDITIONAL_INFO = ADDITIONAL_INFO;
            return this;
        }

        public Builder setCC_BILL_NO(String CC_BILL_NO) {
            this.CC_BILL_NO = CC_BILL_NO;
            return this;
        }

        public Builder setPaymentMode(String paymentMode) {
            this.native_paymentMode = paymentMode;
            return this;
        }

        public Builder setNative_mid(String native_mid) {
            this.native_mid = native_mid;
            return this;
        }

        public Builder setNative_orderId(String native_orderId) {
            this.native_orderId = native_orderId;
            return this;
        }

        public Builder setNative_channelId(String native_channelId) {
            this.native_channelId = native_channelId;
            return this;
        }

        public Builder setNative_txnToken(String native_txnToken) {
            this.native_txnToken = native_txnToken;
            return this;
        }

        public Builder setNative_paymentMode(String native_paymentMode) {
            this.native_paymentMode = native_paymentMode;
            return this;
        }

        public Builder setNative_cardInfo(String native_cardInfo) {
            this.native_cardInfo = native_cardInfo;
            return this;
        }

        public Builder setNative_authMode(String native_authMode) {
            this.native_authMode = native_authMode;
            return this;
        }

        public Builder setMandateAuthMode(String mandateAuthMode) {
            this.mandateAuthMode = mandateAuthMode;
            return this;
        }

        public Builder setNative_channelCode(String native_channelCode) {
            this.native_channelCode = native_channelCode;
            return this;
        }

        public String getEMI_OPTIONS() {
            return EMI_OPTIONS;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public Builder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public String getAggrMid() {
            return aggMid;
        }

        public Builder setEMI_OPTIONS(String EMI_OPTIONS) {
            this.EMI_OPTIONS = EMI_OPTIONS;
            return this;
        }

        public Builder setCardTokenRequired(boolean cardTokenRequired) {
            this.cardTokenRequired = Boolean.toString(cardTokenRequired);
            return this;
        }

        @JsonProperty("PAYMENT_MODE_ONLY")
        public Builder setPAYMENT_MODE_ONLY(String PAYMENT_MODE_ONLY) {
            this.PAYMENT_MODE_ONLY = PAYMENT_MODE_ONLY;
            return this;
        }

        @JsonProperty("PAYMENT_MODE_DISABLE")
        public Builder setPAYMENT_MODE_DISABLE(String PAYMENT_MODE_DISABLE) {
            this.PAYMENT_MODE_DISABLE = PAYMENT_MODE_DISABLE;
            return this;
        }

        @JsonProperty("templateID")
        public Builder setTemplateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        @JsonProperty("locale")
        public Builder setLocale(String locale) {
            this.locale = locale;
            return this;
        }


        @JsonProperty("UDF_2")
        public Builder setUDF_2(String udf_2) {
            this.UDF_2 = udf_2;
            return this;
        }


        @JsonProperty("EMI_TYPE")
        public String getEMI_TYPE() {
            return EMI_TYPE;
        }

        public Builder setEMI_TYPE(String EMI_Type) {
            this.EMI_TYPE = EMI_Type;
            return this;
        }

        public Builder setSplitSettlementInfo(String splitSettlementInfo) {
            this.splitSettlementInfo = splitSettlementInfo;
            return this;
        }

        public String getRiskExtendInfo() {
            return riskExtendInfo;
        }

        public Builder setRiskExtendInfo(String riskExtendInfo) {
            this.riskExtendInfo = riskExtendInfo;
            return this;
        }

        public String getExtendInfo() {
            return extendInfo;
        }

        public Builder setExtendInfo(String extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }


        public String getACCOUNT_REF_ID() {
            return ACCOUNT_REF_ID;
        }

        public Builder setACCOUNT_REF_ID(String ACCOUNT_REF_ID) {
            this.ACCOUNT_REF_ID = ACCOUNT_REF_ID;
            return this;
        }

        public String getBANK_IFSC() {
            return BANK_IFSC;
        }

        public Builder setBANK_IFSC(String BANK_IFSC) {
            this.BANK_IFSC = BANK_IFSC;
            return this;
        }

        public String getSubscriptionPurpose() {
            return subscriptionPurpose;
        }

        public Builder setSubscriptionPurpose(String subscriptionPurpose) {
            this.subscriptionPurpose = subscriptionPurpose;
            return this;}

        public String getIDEBIT_OPTION() {
            return IDEBIT_OPTION;
        }

        public Builder setIDEBIT_OPTION(String IDEBIT_OPTION) {
            this.IDEBIT_OPTION = IDEBIT_OPTION;
            return this;}

            public Builder setMobileNumber(String MobileNumber) {
            this.ContactNumber = MobileNumber;
            return this;
        }

        public String getRisk_extended_info() {
            return risk_extended_info;
        }

        public Builder setRisk_extended_info(String riskExtendInfo) {
                this.risk_extended_info = riskExtendInfo;
            return this;
        }

        public Builder setLinkName(String linkName) {
            this.LINK_NAME = linkName;
            return this;
        }

        public Builder setShortUrl(String shortUrl){
            this.SHORT_URL = shortUrl;
            return this;
        }

        public Builder setLongUrl(String longUrl){
            this.LONG_URL = longUrl;
            return this;
        }

        public Builder setLinkDescription(String linkDescription){
            this.LINK_DESCRIPTION = linkDescription;
            return this;
        }

        public Builder setLINKID(String linkId){
            this.LINK_ID = linkId;
            return this;
        }

        public Builder setCallBack_URL(String url){
            this.CALLBACK_URL = url;
            return this;
        }

        public Builder setUltimateBeneficiaryName(String beneficiaryName){
            this.ULTIMATE_BENEFICIARY_NAME = beneficiaryName;
            return this;
        }

        public OrderDTO build() {
            final OrderDTO orderDTO = new OrderDTO(this);
            System.out.println("orderDTO = " + orderDTO);
            return orderDTO;
        }

        @Override
        public String toString() {
            return "OrderDTO{" +
                    "AUTH_MODE='" + AUTH_MODE + '\'' +
                    ", TOKEN_TYPE='" + TOKEN_TYPE + '\'' +
                    ", REQUEST_TYPE='" + REQUEST_TYPE + '\'' +
                    ", TXN_AMOUNT='" + TXN_AMOUNT + '\'' +
                    ", CUST_ID='" + CUST_ID + '\'' +
                    ", MSISDN='" + MSISDN + '\'' +
                    ", SSO_TOKEN='" + SSO_TOKEN + '\'' +
                    ", PAYTM_TOKEN='" + PAYTM_TOKEN + '\'' +
                    ", MID='" + MID + '\'' +
                    ", merchantKey='" + merchantKey + '\'' +
                    ", INDUSTRY_TYPE_ID='" + INDUSTRY_TYPE_ID + '\'' +
                    ", WEBSITE='" + WEBSITE + '\'' +
                    ", BANK_CODE='" + BANK_CODE + '\'' +
                    ", PAYMENT_DETAILS='" + PAYMENT_DETAILS + '\'' +
                    ", STORE_CARD='" + STORE_CARD + '\'' +
                    ", addMoney='" + addMoney + '\'' +
                    ", WALLET_AMOUNT='" + WALLET_AMOUNT + '\'' +
                    ", PAYMENT_TYPE_ID='" + PAYMENT_TYPE_ID + '\'' +
                    ", SUBS_START_DATE='" + SUBS_START_DATE + '\'' +
                    ", SUBS_EXPIRY_DATE='" + SUBS_EXPIRY_DATE + '\'' +
                    ", SUBS_PPI_ONLY='" + SUBS_PPI_ONLY + '\'' +
                    ", SUBS_AMOUNT_TYPE='" + SUBS_AMOUNT_TYPE + '\'' +
                    ", SUBS_MAX_AMOUNT='" + SUBS_MAX_AMOUNT + '\'' +
                    ", SUBS_FREQUENCY='" + SUBS_FREQUENCY + '\'' +
                    ", SUBS_FREQUENCY_UNIT='" + SUBS_FREQUENCY_UNIT + '\'' +
                    ", SUBS_GRACE_DAYS='" + SUBS_GRACE_DAYS + '\'' +
                    ", SUBS_ENABLE_RETRY='" + SUBS_ENABLE_RETRY + '\'' +
                    ", SUBS_RETRY_COUNT='" + SUBS_RETRY_COUNT + '\'' +
                    ", SUBS_PAYMENT_MODE='" + SUBS_PAYMENT_MODE + '\'' +
                    ", THEME='" + THEME + '\'' +
                    ", CHANNEL_ID='" + CHANNEL_ID + '\'' +
                    ", ORDER_ID='" + ORDER_ID + '\'' +
                    ", SAVED_CARD_ID='" + SAVED_CARD_ID + '\'' +
                    ", CONNECTION_TYPE='" + CONNECTION_TYPE + '\'' +
                    ", SUBS_SERVICE_ID='" + SUBS_SERVICE_ID + '\'' +
                    ", EMAIL='" + EMAIL + '\'' +
                    ", CC_BILL_NO='" + CC_BILL_NO + '\'' +
                    ", PASSCODE='" + PASSCODE + '\'' +
                    ", ADDITIONAL_INFO='" + ADDITIONAL_INFO + '\'' +
                    ", TXN_TOKEN='" + TXN_TOKEN + '\'' +
                    ", cardInfo='" + cardInfo + '\'' +
                    ", channelCode='" + channelCode + '\'' +
                    ", paymentFlow='" + paymentFlow + '\'' +
                    ", payerAccount='" + payerAccount + '\'' +
                    ", mpin='" + mpin + '\'' +
                    ", planId='" + planId + '\'' +
                    ", encCardInfo='" + encCardInfo + '\'' +
                    ", native_mid='" + native_mid + '\'' +
                    ", native_orderId='" + native_orderId + '\'' +
                    ", native_channelId='" + native_channelId + '\'' +
                    ", native_txnToken='" + native_txnToken + '\'' +
                    ", native_paymentMode='" + native_paymentMode + '\'' +
                    ", native_cardInfo='" + native_cardInfo + '\'' +
                    ", native_authMode='" + native_authMode + '\'' +
                    ", mandateAuthMode='" + mandateAuthMode + '\'' +
                    ", native_channelCode='" + native_channelCode + '\'' +
                    ", UDF_1='" + UDF_1 + '\'' +
                    ", UDF_2='" + UDF_2 + '\'' +
                    ", UDF_3='" + UDF_3 + '\'' +
                    ", COMMENTS='" + COMMENTS + '\'' +
                    ", PRN='" + PRN + '\'' +
                    ", PCC_CODE='" + PCC_CODE + '\'' +
                    ", UNIQUE_REFERENCE_VALUE='" + UNIQUE_REFERENCE_VALUE + '\'' +
                    ", UNIQUE_REFERENCE_LABEL='" + UNIQUE_REFERENCE_LABEL + '\'' +
                    ", POS_ID='" + POS_ID + '\'' +
                    ", MASKED_CUSTOMER_MOBILE_NUMBER='" + MASKED_CUSTOMER_MOBILE_NUMBER + '\'' +
                    ", MERC_UNQ_REF='" + MERC_UNQ_REF + '\'' +
                    ", EMI_OPTIONS='" + EMI_OPTIONS + '\'' +
                    ", Subwallet_Details='" + Subwallet_Details + '\'' +
                    ", accountNumber='" + accountNumber + '\'' +
                    ", aggMid='" + aggMid + '\'' +
                    ", PROMO_CAMP_ID='" + PROMO_CAMP_ID + '\'' +
                    ", IS_SAVED_CARD='" + IS_SAVED_CARD + '\'' +
                    ", SUBSCRIPTION_ID='" + SUBSCRIPTION_ID + '\'' +
                    ", cardTokenRequired='" + cardTokenRequired + '\'' +
                    ", MobileNumber='" + ContactNumber + '\'' +
                    ", splitSettlementInfo='" + splitSettlementInfo + '\'' +
                    ", storeInstrument='" + storeInstrument + '\'' +
                    ", LINK_NAME='" + LINK_NAME + '\'' +
                    ", SHORT_URL='" + SHORT_URL + '\'' +
                    ", LONG_URL='" + LONG_URL + '\'' +
                    ", LINK_DESCRIPTION='" + LINK_DESCRIPTION + '\'' +
                    ", LINK_ID='" +  LINK_ID + '\'' +
                    ", CALLBACK_URL='" + CALLBACK_URL +'\'' +
                    ", ultimateBeneficiaryName='" + ULTIMATE_BENEFICIARY_NAME +'\'' +
                    '}';
        }
    }
}