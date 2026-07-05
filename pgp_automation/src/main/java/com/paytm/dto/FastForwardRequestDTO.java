package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.User;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.authException.AuthException;

import static com.paytm.framework.reporting.Reporter.report;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FastForwardRequestDTO {

    private String DeviceId = "9650788700";
    private String Channel = "WEB";
    private String OrderId;
    private String TokenType = "OAUTH";
    private String SSOToken;
    private String AuthMode = "USRPWD";
    private String MId;
    private String TxnAmount = "1.0";
    private String ReqType = "CLW_APP_PAY";
    private String PaymentMode = "PPI";
    private String CustomerId = "1234567";
    private String EMAIL = "anju.kumari@paytm.com";
    private String IndustryType = "Retail";
    private String AppIP = "";
    private String Currency = "INR";
    private String MSISDN = "1234";
    private String MercUnqRef;
    private String ExchangeRate;
    private String loyaltyPointRootUserId;
    private String rootUserPGMid;

    @JsonProperty("CheckSum")
    private String CheckSum = "";

    @JsonProperty("CheckSum")
    public String getCheckSum() {
        return CheckSum;
    }
    @JsonProperty("CheckSum")
    public FastForwardRequestDTO setCheckSum(String checkSum) {
        CheckSum = checkSum;
        return this;
    }


    public FastForwardRequestDTO(Constants.MerchantType merchantType, User user) throws AuthException {
        this.MId = merchantType.getId();
        this.OrderId = CommonHelpers.generateOrderId();
        this.SSOToken = user.ssoToken();

    }

    public FastForwardRequestDTO(Constants.MerchantType merchantType, User user, String orderId) throws AuthException {
        this.MId = merchantType.getId();
        this.OrderId = orderId;
        this.SSOToken = user.ssoToken();
    }


    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }


    public FastForwardRequestDTO addChecksumToRequest(String merchantKey){
        StringBuilder checkSumString = new StringBuilder();
          if (!FastForwardRequestDTO.isBlank(this.getReqType())) {
                checkSumString.append(this.getReqType()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getMId())) {
            checkSumString.append(this.getMId()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getOrderId())) {
            checkSumString.append(this.getOrderId()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getTxnAmount())) {
            checkSumString.append(this.getTxnAmount()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getCustomerId())) {
            checkSumString.append(this.getCustomerId()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getCurrency())) {
            checkSumString.append(this.getCurrency()).append("|");
        }

        if (!FastForwardRequestDTO.isBlank(this.getDeviceId())) {
            checkSumString.append(this.getDeviceId()).append("|");
        }

        if (!FastForwardRequestDTO.isBlank(this.getSSOToken())) {
            checkSumString.append(this.getSSOToken()).append("|");
        }

        if (!FastForwardRequestDTO.isBlank(this.getPaymentMode())) {
            checkSumString.append(this.getPaymentMode()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getIndustryType())) {
            checkSumString.append(this.getIndustryType()).append("|");
        }
        if (!FastForwardRequestDTO.isBlank(this.getExchangeRate())) {
            checkSumString.append(this.getExchangeRate()).append("|");
        }

        if (!FastForwardRequestDTO.isBlank(this.getLoyaltyPointRootUserId())) {
            checkSumString.append(this.getLoyaltyPointRootUserId()).append("|");
        }

        if (!FastForwardRequestDTO.isBlank(this.getRootUserPGMid())) {
            checkSumString.append(this.getRootUserPGMid()).append("|");
        }

        checkSumString.deleteCharAt(checkSumString.length()-1);
        System.out.println("Checksum String for IVR txn is :"+ checkSumString);
        report.info("Checksum String for IVR txn is :"+ checkSumString);
        this.CheckSum = PGPUtil.getChecksum(merchantKey, checkSumString.toString());
        return this;
    }

    public String getMercUnqRef() {
        return MercUnqRef;
    }

    @JsonProperty("MercUnqRef")
    public FastForwardRequestDTO setMercUnqRef(String mercUnqRef) {
        MercUnqRef = mercUnqRef;
        return this;
    }

    @JsonProperty("DeviceId")
    public String getDeviceId() {
        return DeviceId;
    }

    public FastForwardRequestDTO setDeviceId(String DeviceId) {
        this.DeviceId = DeviceId;
        return this;
    }

    @JsonProperty("Channel")
    public String getChannel() {
        return Channel;
    }

    public FastForwardRequestDTO setChannel(String Channel) {
        this.Channel = Channel;
        return this;

    }

    @JsonProperty("OrderId")
    public String getOrderId() {
        return OrderId;
    }

    public FastForwardRequestDTO setOrderId(String orderId) {
        OrderId = orderId;
        return this;
    }

    @JsonProperty("TokenType")
    public String getTokenType() {
        return TokenType;
    }

    public FastForwardRequestDTO setTokenType(String TokenType) {
        this.TokenType = TokenType;
        return this;

    }

    @JsonProperty("SSOToken")
    public String getSSOToken() {
        return SSOToken;
    }

    public FastForwardRequestDTO setSSOToken(String SSOToken) {
        this.SSOToken = SSOToken;
        return this;

    }

    @JsonProperty("AuthMode")
    public String getAuthMode() {
        return AuthMode;
    }

    public FastForwardRequestDTO setAuthMode(String AuthMode) {
        this.AuthMode = AuthMode;
        return this;
    }

    @JsonProperty("MId")
    public String getMId() {
        return MId;
    }

    public FastForwardRequestDTO setMId(String MId) {
        this.MId = MId;
        return this;

    }

    @JsonProperty("TxnAmount")
    public String getTxnAmount() {
        return TxnAmount;
    }

    public FastForwardRequestDTO setTxnAmount(String TxnAmount) {
        this.TxnAmount = TxnAmount;
        return this;

    }

    @JsonProperty("ReqType")
    public String getReqType() {
        return ReqType;
    }

    public FastForwardRequestDTO setReqType(String ReqType) {
        this.ReqType = ReqType;
        return this;

    }

    @JsonProperty("PaymentMode")
    public String getPaymentMode() {
        return PaymentMode;
    }

    public FastForwardRequestDTO setPaymentMode(String PaymentMode) {
        this.PaymentMode = PaymentMode;
        return this;

    }

    @JsonProperty("CustomerId")
    public String getCustomerId() {
        return CustomerId;
    }

    public FastForwardRequestDTO setCustomerId(String CustomerId) {
        this.CustomerId = CustomerId;
        return this;

    }

    @JsonProperty("EMAIL")
    public String getEMAIL() {
        return EMAIL;
    }

    public FastForwardRequestDTO setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
        return this;

    }

    @JsonProperty("IndustryType")
    public String getIndustryType() {
        return IndustryType;
    }

    public FastForwardRequestDTO setIndustryType(String IndustryType) {
        this.IndustryType = IndustryType;
        return this;

    }

    @JsonProperty("AppIP")
    public String getAppIP() {
        return AppIP;
    }

    public FastForwardRequestDTO setAppIP(String AppIP) {
        this.AppIP = AppIP;
        return this;

    }

    @JsonProperty("Currency")
    public String getCurrency() {
        return Currency;
    }

    public FastForwardRequestDTO setCurrency(String Currency) {
        this.Currency = Currency;
        return this;

    }



    @JsonProperty("ExchangeRate")
    public String getExchangeRate() {
        return ExchangeRate;
    }

    public FastForwardRequestDTO setExchangeRate(String ExchangeRate) {
        this.ExchangeRate = ExchangeRate;
        return this;

    }


    @JsonProperty("loyaltyPointRootUserId")
    public String getLoyaltyPointRootUserId() {
        return loyaltyPointRootUserId;
    }

    public FastForwardRequestDTO setLoyltyPointRootUserId(String loyaltyPointRootUserId) {
        this.loyaltyPointRootUserId = loyaltyPointRootUserId;
        return this;

    }

    @JsonProperty("rootUserPGMid")
    public String getRootUserPGMid() {
        return rootUserPGMid;
    }

    public FastForwardRequestDTO setRootUserPGMid(String rootUserPGMid) {
        this.rootUserPGMid = rootUserPGMid;
        return this;

    }

    @JsonProperty("MSISDN")
    public String getMSISDN() {
        return MSISDN;
    }

    public FastForwardRequestDTO setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
        return this;
    }

    @Override
    public String toString() {
        return "ClassPojo [DeviceId = " + DeviceId + ", Channel = " + Channel + ", OrderId = " + OrderId + ", TokenType = " + TokenType + ", SSOToken = " + SSOToken + ", AuthMode = " + AuthMode + ", MId = " + MId + ", TxnAmount = " + TxnAmount + ", ReqType = " + ReqType + ", PaymentMode = " + PaymentMode + ", CustomerId = " + CustomerId + ", EMAIL = " + EMAIL + ", IndustryType = " + IndustryType + ", AppIP = " + AppIP + ", Currency = " + Currency + ", MSISDN = " + MSISDN + "]";
    }

}