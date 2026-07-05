package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.dto.processTransactionV1.response.EmiSubventionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class ProcessTxnV1Request {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    private ProcessTxnV1Request(Builder builder) {
        this.head = new Head()
                .setTokenType(builder.tokenType)
                .setToken(builder.token)
                .setTxnToken(builder.txnToken)
                .setChannelId(builder.channelId);
        this.body = new Body()
                .setMid(builder.mid)
                .setOrderId(builder.orderId)
                .setSso(builder.sso)
                .setPaymentMode(builder.paymentMode)
                .setqrCodeId(builder.qrCodeId)
                .setCardInfo(builder.cardInfo)
                .setCoftConsent(builder.coftConsent)
                .setChannelCode(builder.channelCode)
                .setCustId(builder.custId)
                .setTxnAmount(builder.txnAmount)
                .setRiskExtendInfo(builder.riskExtendInfo)
                .setExtendInfo(builder.extendInfo)
                .setStoreInstrument(builder.storeInstrument)
                .setAuthMode(builder.authMode)
                .setCardPreAuthType(builder.cardPreAuthType)
                .setMandateAuthMode(builder.mandateAuthMode)
                .setBankIfsc(builder.bankIfsc)
                .setUserName(builder.userName)
                .setUserNameCamel(builder.userNameCamel)
                .setAccountHolderName(builder.accountHolderName)
                .setAccountType(builder.accountType)
                .setAccountTypeCamel(builder.accountTypeCamel)
                .setIndustryTypeId(builder.industryTypeId)
                .setMerchantKey(builder.merchantKey)
                .setAccountNumber(builder.accountNumber)
                .setAccountNumberCamel(builder.accountNumberCamel)
                .setMpin(builder.mpin)
                .setRequestType(builder.requestType)
                .setPlanId(builder.planId)
                .setEmiType(builder.emiType)
                .setPayerAccount(builder.payerAccount)
                .setAddMoney(builder.addMoney)
                .setPaymentFlow(builder.paymentFlow)
                .setWebsite(builder.website)
                .setAggMid(builder.aggMid)
                .setAggType(builder.aggType)
                .setSubsId(builder.subsId)
                .setEcomTokenInfo(builder.ecomTokenInfo)
                .setMERC_UNQ_REF(builder.MERC_UNQ_REF)
                .setpspApp(builder.pspApp)
                .setosType(builder.osType)
                .setemiSubventionInfo(builder.emiSubventionInfo)
                .setconvertToAddAndPayTxn(builder.convertToAddAndPayTxn)
                .setPreferredOtpPage(builder.preferredOtpPage)
                .setSeqNumber(builder.seqNumber)
                .setUpiAccRefId(builder.upiAccRefId)
                .setmerchantVpa(builder.merchantVpa)
                .setcardTokenInfo(builder.cardTokenInfo)
                .setUpiLiteRequestData(builder.upiLiteRequestData)
                .setTipDetails(builder.tipDetails)
                .setCreditBlock(builder.creditBlock)
                .setHybridPayModeDetails(builder.hybridPayModeDetails)
                .setCreditBlock(builder.creditBlock)
                .setSuperCashOffer(builder.superCashOffer)
                .setQrImageRequired(builder.qrImageRequired);
    }


    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public static class Builder {
        private String tokenType;
        private String token;
        private String txnToken;
        private String mid;
        private String orderId;
        private String sso;
        private String aggType;
        private String merchantVpa;
        private String qrCodeId;
        private String aggMid;
        private String paymentMode;
        private String channelCode = "ICICI";
        private String custId = CommonHelpers.generateOrderId();
        private String txnValue = "2";
        private String riskExtendInfo = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Gaurav+corner|mode:recentBeneficiary|mode:recentBeneficiary";
        private String additionalInfo = "additionalInfo=merchantGuid:51f4f8c5-2e1b-43ec-b09b-cee6e3956588|MERCHANT_NAME:DDFS_Swaroskvi|qrCodeId:2810050501011T36KWBJHQEX|category:Beauty and Wellness|REQUEST_TYPE:QR_MERCHANT|offlinePostConvenience:0|service:P2M|CHANNEL_ID:QRCODE|merchantOrderId:19IN40100000035816237|MERCHANT_STATUS:ACTIVE|EXPIRY_DATE:1556102557000|merchantTransId:19IN40100000035816237|MID:DDFST347706250301107|orderAlreadyCreated:{orderAlreadyCreated}|mappingId:DDFST347706250301107|posId:IN401|totalAmount:1.00|industryType:Retail|NAME:DDFS_Swaroskvi|mode:QR|MOBILE_NO:9999000000|payeeType:MERCHANT|MERCHANT_GUID:51f4f8c5-2e1b-43ec-b09b-cee6e3956588|currencyCode:INR|pgEnabled:1|INDUSTRY_TYPE_ID:Retail|subCategory:Gym and Fitness|comment:|userLbsLatitude:28.554528567660846|userLbsLongitude:77.08408091675527|mode:QR";
        private TxnAmount txnAmount;
        private ExtendInfo extendInfo = new ExtendInfo();
        private boolean extendInfoOrderAlreadyCreated = false;
        private String cardNum = "4718650100010336";
        private String cvv = "618";
        private String expDate = "072026";
        private String cardInfo = "|" + this.cardNum + "|" + this.cvv + "|" + this.expDate;
        private String storeInstrument;
        private String authMode;
        private String cardPreAuthType;
        private String mandateAuthMode;
        private String bankIfsc;
        /** Maps to JSON key {@code USER_NAME}. */
        private String userName;
        /** Maps to JSON key {@code userName}; bank-mandate PTC uses with {@link #userName}. */
        private String userNameCamel;
        /** Bank-mandate PTC body only; set from {@link com.paytm.dto.OrderDTO#getUserName()}; otherwise null (key omitted). */
        private String accountHolderName;
        /** Maps to JSON key {@code ACCOUNT_TYPE}. */
        private String accountType;
        /** Maps to JSON key {@code accountType}; bank-mandate PTC uses with {@link #accountType}. */
        private String accountTypeCamel;
        private String industryTypeId;
        private String merchantKey;
        private String accountNumber;
        private String accountNumberCamel;
        private String mpin, requestType;
        private String planId,emiType;
        private String payerAccount = "testvpa0091234@paytm";
        private Integer addMoney = null;
        private String paymentFlow;
        private String subsId;
        private String website = "retail";
        private String channelId;
        private EcomTokenInfo ecomTokenInfo;
        private String MERC_UNQ_REF;
        private String pspApp;
        private String osType;
        private boolean convertToAddAndPayTxn;
        private String preferredOtpPage;
        private SuperCashOffer superCashOffer;
        @JsonIgnore
        private EmiSubventionInfo emiSubventionInfo;
        private String seqNumber;
        private String upiAccRefId;
        private CardTokenInfo cardTokenInfo;
        private CoftConsent coftConsent;
        private UpiLiteRequestData upiLiteRequestData;
        private TipDetails tipDetails;
        private String creditBlock;
        private List<HybridPayModeDetail> hybridPayModeDetails;
        private boolean qrImageRequired;


        public Builder(String mid, String tokenType, String token, String orderId, String txnAmount) {
            this.mid = mid;
            this.tokenType = tokenType;
            this.token = token;
            this.orderId = orderId;
            this.paymentMode = "CREDIT_CARD";
            this.txnAmount = new TxnAmount().setValue(txnAmount);
        }

        public Builder(String mid, String txnToken, String orderId) {
            this(mid, null, null, orderId, null);
            this.txnToken = txnToken;
            this.txnAmount = null;
            this.requestType = "NATIVE";
            this.cardInfo = null;
            this.channelCode = null;
        }

        public Builder(List<HybridPayModeDetail> hybridPayModeDetailList, String mid, String orderId, String txnToken) {
            this(mid, orderId,txnToken);
            this.txnToken = txnToken;
            this.orderId = orderId;
            this.mid = mid;
            this.txnAmount = null;
            this.requestType = "NATIVE";
            this.cardInfo = null;
            this.channelCode = null;
            this.setPaymentFlow("HYBRID");
            this.setHybridPayModeDetails(hybridPayModeDetailList);
        }

        public Builder(Constants.MerchantType merchantType, String tokenType, String token)
        {
            this.mid=merchantType.getId();
            this.token=token;
            this.tokenType = tokenType;
        }

        public Builder(String mid, String txnToken, String orderId, String seqNumber) {
            this.mid = mid;
            this.orderId = orderId;
            this.txnToken = txnToken;
            this.txnAmount = null;
            this.requestType = "NATIVE";
            this.cardInfo = null;
            this.channelCode = null;
            this.seqNumber = "PTM9FAB586753034B6087BE3C25CFF7C5F4";
            this.upiAccRefId = "10504";
        }

        public Builder(String mid, String txnToken, String orderId, String seqNumber,UpiLiteRequestData param,String dummy) {
            this(mid,txnToken,orderId,seqNumber);
            upiLiteRequestData = new UpiLiteRequestData();
        }

        public Builder(String mid, String tokenType, String token, String orderId, String txnAmount, String param,String paymentMode,String dummy) {
            this.mid = mid;
            this.tokenType = tokenType;
            this.token = token;
            this.orderId = orderId;
            this.paymentMode = paymentMode;
            this.seqNumber = "PTM9FAB586753034B6087BE3C25CFF7C5F4";
            this.upiAccRefId = "236077";
            this.payerAccount ="7259493013@paytm";
            this.txnAmount = new TxnAmount().setValue(txnAmount);
            this.cardInfo = null;
            this.channelCode = null;
            upiLiteRequestData = new UpiLiteRequestData();
        }


        public Builder(String token){
            this.token= token;
            this.tokenType= "SSO";
        }

        public Builder setQRCodeId(String qrCodeId)
        {
            this.qrCodeId = qrCodeId;
            return this;
        }

        public Builder setAddMoney(Integer addMoney) {
            this.addMoney = addMoney;
            return this;
        }

        public Builder setTxnAmount(TxnAmount txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public Builder setSuperCashOffer(SuperCashOffer superCashOffer) {
            this.superCashOffer = superCashOffer;
            return this;
        }

        public Builder setmerchantVpa(String merchantVpa)
        {
            this.merchantVpa = merchantVpa;
            return this;
        }

        public Builder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder setAggType(String aggType) {
            this.aggType = aggType;
            return this;
        }

        public Builder setMERC_UNQ_REF(String MERC_UNQ_REF) {
            this.MERC_UNQ_REF = MERC_UNQ_REF;
            return this;
        }

        public Builder setAggMid(String aggMid) {
            this.aggMid = aggMid;
            return this;
        }


        public Builder setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Builder setAuthMode(String authMode) {
            this.authMode = authMode;
            return this;
        }

        public Builder setCardPreAuthType(String cardPreAuthType) {
            this.cardPreAuthType = cardPreAuthType;
            return this;
        }

        public Builder setMandateAuthMode(String mandateAuthMode) {
            this.mandateAuthMode = mandateAuthMode;
            return this;
        }

        public Builder setBankIfsc(String bankIfsc) {
            this.bankIfsc = bankIfsc;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setUserNameCamel(String userNameCamel) {
            this.userNameCamel = userNameCamel;
            return this;
        }

        public Builder setAccountHolderName(String accountHolderName) {
            this.accountHolderName = accountHolderName;
            return this;
        }

        public Builder setAccountType(String accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder setAccountTypeCamel(String accountTypeCamel) {
            this.accountTypeCamel = accountTypeCamel;
            return this;
        }

        public Builder setIndustryTypeId(String industryTypeId) {
            this.industryTypeId = industryTypeId;
            return this;
        }

        public Builder setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
            return this;
        }

        public Builder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder setAccountNumberCamel(String accountNumberCamel) {
            this.accountNumberCamel = accountNumberCamel;
            return this;
        }

        public Builder setMpin(String mpin) {
            this.mpin = mpin;
            return this;
        }

        public Builder setStoreInstrument(String storeInstrument) {
            this.storeInstrument = storeInstrument;
            return this;
        }

        public Builder setCardNum(String cardNum) {
            this.cardNum = cardNum;
            this.cardInfo = "|" + this.cardNum + "|" + this.cvv + "|" + this.expDate;
            return this;
        }

        public Builder setCvv(String cvv) {
            this.cvv = cvv;
            return this;
        }

        public Builder setExpDate(String expDate) {
            this.expDate = expDate;
            return this;
        }

        public Builder setExtendInfoOrderAlreadyCreated(boolean extendInfoOrderAlreadyCreated) {
            this.extendInfoOrderAlreadyCreated = extendInfoOrderAlreadyCreated;
            this.extendInfo.setAdditionalInfo(additionalInfo.replace(
                    "{orderAlreadyCreated}", Boolean.toString(extendInfoOrderAlreadyCreated)));
            return this;
        }

        public Builder setChange(boolean convertToAddAndPayTxn) {
            this.convertToAddAndPayTxn = convertToAddAndPayTxn;
            return this;
        }

        public Builder setExtendInfoStaticFlow()
        {
            this.extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|".replace("{qrCode}",qrCodeId));
            return this;
        }

        public Builder setExtendInfoDynamicFlow()
        {
            this.extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|merchantContactNo:9899267758|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|ORDER_ID:{ORDER_ID}|orderAlreadyCreated:true|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|".replace("{qrCode}",qrCodeId).replace("{ORDER_ID}",orderId));
            return this;
        }

        public Builder setExtendedInfoCloseOrderOffline(String additionalInfo){
            additionalInfo.replace("{qrCode}",qrCodeId).replace("{ORDER_ID}",orderId);
            this.extendInfo.setAdditionalInfo(additionalInfo);
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
            return this;
        }

        public Builder setCardInfo(String cardInfo) {
            this.cardInfo = cardInfo;
            return this;
        }

        public Builder setChannelCode(String channelCode) {
            this.channelCode = channelCode;
            return this;
        }

        public Builder setCustId(String custId) {
            this.custId = custId;
            return this;
        }

        public Builder setTxnValue(String txnValue) {
            this.txnValue = txnValue;
            return this;
        }

        public Builder setRiskExtendInfo(String riskExtendInfo) {
            this.riskExtendInfo = riskExtendInfo;
            return this;
        }

        public Builder setPlanId(String planId)
        {
            this.planId = planId;
            return this;
        }

        public Builder setEmiType(String emiType)
        {
            this.emiType = emiType;
            return this;
        }

        public Builder setPayerAccount(String payerAccount)
        {
            this.payerAccount = payerAccount;
            return this;
        }
        public Builder setPaymentFlow(String paymentFlow)
        {
            this.paymentFlow = paymentFlow;
            return this;
        }

        public Builder setSubsId(String subsId)
        {
            this.subsId = subsId;
            return this;
        }
        public Builder setWebsite(String website) {
            this.website = website;
            return this;
        }
        public Builder setEcomTokenInfo(EcomTokenInfo ecomTokenInfo) {
            this.ecomTokenInfo = ecomTokenInfo;
            return this;
        }

        public Builder setChannelId(String channelId){
            this.channelId = channelId;
            return this;
        }
        public Builder setpspApp(String pspApp) {
            this.pspApp = pspApp;
            return this;
        }

        public Builder setosType(String osType) {
            this.osType = osType;
            return this;
        }

        public Builder setemiSubventionInfo(EmiSubventionInfo emiSubventionInfo) {
            this.emiSubventionInfo = emiSubventionInfo;
            return this;
        }
        public Builder setconvertToAddAndPayTxn(boolean convertToAddAndPayTxn) {
            this.convertToAddAndPayTxn = convertToAddAndPayTxn;
            return this;
        }
        public Builder setPreferredOtpPage(String preferredOtpPage) {
            this.preferredOtpPage = preferredOtpPage;
            return this;
        }
        public Builder setSeqNumber(String seqNumber) {
            this.seqNumber = seqNumber;
            return this;
        }
        public Builder setUpiAccRefId(String upiAccRefId) {
            this.upiAccRefId = upiAccRefId;
            return this;
        }

        public Builder setExtendInfoStaticFlowWithPosId()
        {
            this.extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|posId:678900|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|".replace("{qrCode}",qrCodeId));
            return this;
        }

        public Builder setExtendInfoDynamicFlowWithPosId()
        {
            this.extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|merchantContactNo:9899267758|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|ORDER_ID:{ORDER_ID}|orderAlreadyCreated:true|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|posId:119988|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|".replace("{qrCode}",qrCodeId).replace("{ORDER_ID}",orderId));
            return this;
        }
        public Builder setcardTokenInfo(CardTokenInfo cardTokenInfo) {
            this.cardTokenInfo = cardTokenInfo;
            return this;
        }

        public Builder setCoftConset(CoftConsent coftConsent) {
            this.coftConsent = coftConsent;
            return this;
        }

        public Builder setUpiLiteRequestData(UpiLiteRequestData upiLiteRequestData) {
            this.upiLiteRequestData = upiLiteRequestData;
            return this;
        }

        public Builder setTipDetails(TipDetails tipDetails) {
            this.tipDetails = tipDetails;
            return this;
        }

        public Builder setCreditBlock(String creditBlock) {
            this.creditBlock = creditBlock;
            return this;
        }

        public void setHybridPayModeDetails(List<HybridPayModeDetail> hybridPayModeDetails) {
            this.hybridPayModeDetails = hybridPayModeDetails;
        }

        public List<HybridPayModeDetail> getHybridPayModeDetails() {
            return hybridPayModeDetails;
        }
        public Builder setQrImageRequired(boolean qrImageRequired) {
            this.qrImageRequired = qrImageRequired;
            return this;
        }

        public ProcessTxnV1Request build() {
            return new ProcessTxnV1Request(this);
        }
    }

}
