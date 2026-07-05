package com.paytm.dto.NativeDTO.InitTxn;


import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.NativeDTO.SubwalletAmount;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestType",
        "isNativeAddMoney",
        "mid",
        "unifiedOffersToken",
        "aggType",
        "offlineFlow",
        "emiSubventionToken",
        "isNativeAddMoney",
        "validateAccountNumber",   // mutual fund changes
        "allowUnverifiedAccount", // mutual fund changes
        "aggMid",                 // mutual fund changes
        "orderId",
        "websiteName",
        "accountNumber",            //UPIMandate
        "cardTokenRequired",
        "emiOption",
        "subwalletAmount",
        "chargeAmount",
        "txnAmount",
        "payableAmount",
        "subscriptionPaymentMode",
        "subsPPIOnly",
        "subscriptionAmountType",
        "subscriptionMaxAmount",
        "subscriptionFrequency",
        "subscriptionFrequencyUnit",
        "subscriptionExpiryDate",
        "subscriptionEnableRetry",
        "subscriptionGraceDays",
        "subscriptionStartDate",
        "subscriptionRetryCount",
        "userInfo",
        "paytmSsoToken",
        "disablePaymentMode",
        "enablePaymentMode",
        "promoCode",
        "callbackUrl",
        "goods",
        "shippingInfo",
        "invoiceDetails",
        "extendInfo",
        "emiId",
        "peonUrl",
        "mandateType",
        "mandateAuthMode",
        "addMoneyFeeAppliedOnWallet",
        "cardHash",
        "tpvInfos",
        "payerAccountDetails",
        "splitSettlementInfo",
        "paymodeSequence",
        "cashierAdditionalInfo"

})
public class Body {

    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("isNativeAddMoney")
    private String isNativeAddMoney;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("unifiedOffersToken")
    private String unifiedOffersToken;
    @JsonProperty("aggType")
    private String aggType;
    @JsonProperty("offlineFlow")
    private String offlineFlow;
    @JsonProperty("cardHash")
    private String cardHash;
    @JsonProperty("emiSubventionToken")
    private String emiSubventionToken;
    @JsonProperty("emiId")
    private String emiId;
    @JsonProperty("peonUrl")
    private String peonUrl;
    @JsonProperty("validateAccountNumber")
    private String validateAccountNumber;
    @JsonProperty("allowUnverifiedAccount")
    private String allowUnverifiedAccount;
    @JsonProperty("mandateType")
    private String mandateType;
    /** Null = omit from JSON (class-level {@code NON_NULL}); does not affect other paymodes. */
    @JsonProperty("mandateAuthMode")
    private String mandateAuthMode;
    @JsonProperty("aggMid")
    private String aggrMid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("websiteName")
    private String websiteName;
    @JsonProperty("cardTokenRequired")
    private String cardTokenRequired;
    @JsonProperty("emiOption")
    private String emiOption;
    @JsonProperty("subwalletAmount")
    private SubwalletAmount subwalletAmount;
    @JsonProperty("chargeAmount")
    private TxnAmount chargeAmount;
    @JsonProperty("txnAmount")
    private TxnAmount txnAmount;
    @JsonProperty("payableAmount")
    private TxnAmount payableAmount;
    @JsonProperty("userInfo")
    private UserInfo userInfo;
    @JsonProperty("paytmSsoToken")
    private String paytmSsoToken;
    @JsonProperty("disablePaymentMode")
    private DisablePaymentMode[] disablePaymentMode;
    @JsonProperty("enablePaymentMode")
    private EnablePaymentMode[] enablePaymentMode;
    @JsonProperty("promoCode")
    private String promoCode;
    @JsonProperty("callbackUrl")
    private String callbackUrl;
    @JsonProperty("goods")
    private Good[] goods;
    @JsonProperty("shippingInfo")
    private ShippingInfo[] shippingInfo;
    @JsonProperty("invoiceDetails")
    private InvoiceDetails invoiceDetails;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("subscriptionPaymentMode")
    private String subscriptionPaymentMode;
    @JsonProperty("subscriptionAmountType")
    private String subscriptionAmountType;
    @JsonProperty("subscriptionMaxAmount")
    private String subscriptionMaxAmount;
    @JsonProperty("subscriptionFrequency")
    private String subscriptionFrequency;
    @JsonProperty("subscriptionFrequencyUnit")
    private String subscriptionFrequencyUnit;
    @JsonProperty("subscriptionExpiryDate")
    private String subscriptionExpiryDate;
    @JsonProperty("subscriptionEnableRetry")
    private String subscriptionEnableRetry;
    @JsonProperty("subscriptionGraceDays")
    private String subscriptionGraceDays;
    @JsonProperty("subscriptionStartDate")
    private String subscriptionStartDate;
    @JsonProperty("subscriptionRetryCount")
    private String subscriptionRetryCount;
    @JsonProperty("subsPPIOnly")
    private String subsPPIOnly;
    @JsonProperty("riskFeeDetails")
    private RiskFeeDetails riskFeeDetails;
    @JsonProperty("appInvokeDevice")
    private String appInvokeDevice;
    @JsonProperty("orderPricingInfo")
    private OrderPricingInfo orderPricingInfo;
    @JsonProperty("simplifiedPaymentOffers")
    private SimplifiedPaymentOffers simplifiedPaymentOffers;
    @JsonProperty("paymentOffersApplied")
    private PaymentOffersApplied paymentOffersApplied;
    @JsonProperty("addMoneyFeeAppliedOnWallet")
    private Boolean addMoneyFeeAppliedOnWallet;
    @JsonProperty("splitSettlementInfo")
    private SplitSettlementInfo splitSettlementInfo;
    @JsonProperty("mandateAccountDetails")
    private MandateAccountDetails mandateAccountDetails;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("riskExtendInfo")
    private RiskExtendInfo riskExtendInfo;
    @JsonProperty("simplifiedSubvention")
    private SimplifiedSubvention simplifiedSubvention;
    @JsonProperty("simplifiedUnifiedOffers")
    private SimplifiedUnifiedOffers simplifiedUnifiedOffers;
    @JsonProperty("bankAccountNumbers")
    private List<String> bankAccountNumbers;
    @JsonProperty("ultimateBeneficiaryDetails")
    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;
    @JsonProperty("additionalInfo")
    private AdditionalInfo additionalInfo;
    @JsonProperty("mutualFundFeedInfo")
    private MutualFundFeedInfo mutualFundFeedInfo;
    @JsonProperty("linkDetails")
    private LinkDetails linkDetails;
    @JsonProperty("paymodeSequence")
    private String paymodeSequence;
    @JsonProperty("vanInfo")
    private VanInfo vanInfo;
    @JsonProperty("tpvInfos")
    private List<TpvInfo> tpvInfos = null;
    @JsonProperty("payerAccountDetails")
    private List<PayerAccountDetail> payerAccountDetails = null;
    @JsonProperty("autoRefund")
    private Boolean autoRefund;
    @JsonProperty("affordabilityInfo")
    private AffordabilityInfo affordabilityInfo;
    @JsonProperty("affordabilityDetails")
    private AffordabilityDetails[] affordabilityDetails;
    @JsonProperty("cashierAdditionalInfo")
    private CashierAdditionalInfo cashierAdditionalInfo;
    @JsonProperty("encryptedParams")
    private String encryptedParams;
    @JsonProperty("encKeyId")
    private String encKeyId;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /* public Body(String callbackUrl, String websiteName, DisablePaymentMode[] disablePaymentMode, EnablePaymentMode[] enablePaymentMode, String requestType, String paytmSsoToken, String mid, UserInfo userInfo, String promoCode, TxnAmount txnAmount, String emiOptions, String orderId, String cardTokenRequired, ExtendInfo_ extendInfo, String allowUnverifiedAccount, String validateAccountNumber, String aggMid) {
         this(callbackUrl, websiteName, disablePaymentMode, enablePaymentMode, requestType, paytmSsoToken, aggMid, userInfo, promoCode, txnAmount, emiOptions, orderId, cardTokenRequired, extendInfo);
         this.allowUnverifiedAccount = allowUnverifiedAccount;
         this.validateAccountNumber = validateAccountNumber;
         this.aggrMid = aggMid;
         this.mid = mid;
     }
 */
    public Body(String callbackUrl, String websiteName, DisablePaymentMode[] disablePaymentMode, EnablePaymentMode[] enablePaymentMode, String requestType, String paytmSsoToken, String mid, UserInfo userInfo, String promoCode, TxnAmount txnAmount, String emiOptions, String orderId, String cardTokenRequired, ExtendInfo extendInfo, String allowUnverifiedAccount,
                String validateAccountNumber, String aggMid, String subscriptionPaymentMode, String subscriptionAmountType,
                String subscriptionMaxAmount, String subscriptionFrequency, String subscriptionFrequencyUnit, String subscriptionExpiryDate,
                String subscriptionEnableRetry, String subscriptionGraceDays, String subscriptionStartDate, String subscriptionRetryCount, String isNativeAddMoney, String subsPPIOnly, String emiId, RiskFeeDetails riskFeeDetails, String peonUrl,String appInvokeDevice, SimplifiedPaymentOffers simplifiedPaymentOffers,PaymentOffersApplied paymentOffersApplied, String aggType,String offlineFlow,OrderPricingInfo orderPricingInfo, String mandateType, String mandateAuthMode, Boolean addMoneyFeeAppliedOnWallet,String accountNumber,String cardHash,SplitSettlementInfo splitSettlementInfo,
                SimplifiedSubvention simplifiedSubvention,SimplifiedUnifiedOffers simplifiedUnifiedOffers, MandateAccountDetails mandateAccountDetails, String paymodeSequence, VanInfo vanInfo, Boolean autoRefund,CashierAdditionalInfo cashierAdditionalInfo, UltimateBeneficiaryDetails ultimateBeneficiaryDetails
    ) {
        this(callbackUrl, websiteName, disablePaymentMode, enablePaymentMode, requestType,
                paytmSsoToken, aggMid, userInfo, promoCode, txnAmount, emiOptions, orderId,
                cardTokenRequired, extendInfo, subscriptionPaymentMode, subscriptionAmountType,
                subscriptionMaxAmount, subscriptionFrequency, subscriptionFrequencyUnit, subscriptionExpiryDate,
                subscriptionEnableRetry, subscriptionGraceDays, subscriptionStartDate, subscriptionRetryCount,riskFeeDetails,mandateType,mandateAuthMode,addMoneyFeeAppliedOnWallet,accountNumber,cardHash,splitSettlementInfo
        );
        this.allowUnverifiedAccount = allowUnverifiedAccount;
        this.validateAccountNumber = validateAccountNumber;
        this.aggrMid = aggMid;
        this.mid = mid;
        this.isNativeAddMoney = isNativeAddMoney;
        this.subsPPIOnly = subsPPIOnly;
        this.emiId=emiId;
        this.peonUrl=peonUrl;
        this.appInvokeDevice=appInvokeDevice;
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
        this.paymentOffersApplied = paymentOffersApplied;
        this.aggType = aggType;
        this.offlineFlow = offlineFlow;
        this.orderPricingInfo = orderPricingInfo;
        this.mandateType=mandateType;
        this.accountNumber=accountNumber;
        this.mandateAccountDetails = mandateAccountDetails;
        this.simplifiedSubvention = simplifiedSubvention;
        this.simplifiedUnifiedOffers= simplifiedUnifiedOffers;
        this.paymodeSequence = paymodeSequence;
        this.vanInfo = vanInfo;
        this.autoRefund = autoRefund;
        this.cashierAdditionalInfo= cashierAdditionalInfo;
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public Body(String callbackUrl, String websiteName,
                DisablePaymentMode[] disablePaymentMode,
                EnablePaymentMode[] enablePaymentMode, String requestType,
                String paytmSsoToken, String mid, UserInfo userInfo,
                String promoCode, TxnAmount txnAmount, String emiOptions,
                String orderId, String cardTokenRequired, ExtendInfo extendInfo,
                String subscriptionPaymentMode, String subscriptionAmountType,
                String subscriptionMaxAmount, String subscriptionFrequency,
                String subscriptionFrequencyUnit, String subscriptionExpiryDate,
                String subscriptionEnableRetry, String subscriptionGraceDays,
                String subscriptionStartDate, String subscriptionRetryCount,RiskFeeDetails riskFeeDetails,String mandateType, String mandateAuthMode, Boolean addMoneyFeeAppliedOnWallet, String accountNumber, String cardHash,SplitSettlementInfo splitSettlementInfo) {
        this.requestType = requestType;
        this.mid = mid;
        this.orderId = orderId;
        this.websiteName = websiteName;
        this.callbackUrl = callbackUrl;
        this.websiteName = websiteName;
        this.paytmSsoToken = paytmSsoToken;
        this.disablePaymentMode = disablePaymentMode;
        this.enablePaymentMode = enablePaymentMode;
        this.mandateType = mandateType;
        this.mandateAuthMode = mandateAuthMode;
        this.userInfo = userInfo;
        this.promoCode = promoCode;
        this.txnAmount = txnAmount;
        this.shippingInfo = new ShippingInfo[]{new ShippingInfo()};
        this.goods = new Good[]{new Good()};
        this.emiOption = emiOptions;
        this.cardTokenRequired = cardTokenRequired;
        this.extendInfo = extendInfo;
        this.subscriptionPaymentMode = subscriptionPaymentMode;
        this.subscriptionAmountType = subscriptionAmountType;
        this.subscriptionMaxAmount = subscriptionMaxAmount;
        this.subscriptionFrequency = subscriptionFrequency;
        this.subscriptionFrequencyUnit = subscriptionFrequencyUnit;
        this.subscriptionExpiryDate = subscriptionExpiryDate;
        this.subscriptionEnableRetry = subscriptionEnableRetry;
        this.subscriptionGraceDays = subscriptionGraceDays;
        this.subscriptionStartDate = subscriptionStartDate;
        this.subscriptionRetryCount = subscriptionRetryCount;
        this.riskFeeDetails = riskFeeDetails;
        this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
        this.accountNumber=accountNumber;
        this.cardHash = cardHash;
        this.splitSettlementInfo = splitSettlementInfo;

    }

    public Body() {
    }

    @JsonProperty("mandateAuthMode")
    public String getMandateAuthMode() {
        return mandateAuthMode;
    }

    @JsonProperty("mandateAuthMode")
    public void setMandateAuthMode(String mandateAuthMode) {
        this.mandateAuthMode = mandateAuthMode;
    }

    @JsonProperty("payableAmount")
    public TxnAmount getPayableAmount() {
        return payableAmount;
    }

    @JsonProperty("payableAmount")
    public Body setPayableAmount(TxnAmount payableAmount) {
        this.payableAmount = payableAmount;
        return this;
    }

    public String getEmiSubventionToken() {
        return emiSubventionToken;
    }

    public Body setEmiSubventionToken(String emiSubventionToken) {
        this.emiSubventionToken = emiSubventionToken;
        return this;
    }
    public String getUnifiedOffersToken() {return unifiedOffersToken;}
    public Body setUnifiedOffersToken(String unifiedOffersToken) {
        this.unifiedOffersToken = unifiedOffersToken;
        return this;
    }

    public String getCardHash() {
        return cardHash;
    }

    public Body setCardHash(String cardHash) {
        this.cardHash = cardHash;
        return this;
    }

    public Boolean getAddMoneyFeeOnWallet() {
        return addMoneyFeeAppliedOnWallet;
    }

    public Body setAddMoneyFeeOnWallet(Boolean addMoneyFeeAppliedOnWallet) {
        this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
        return this;
    }

    public SplitSettlementInfo getSplitSettlementInfo() {
        return splitSettlementInfo;
    }

    public Body setSplitSettlementInfo(SplitSettlementInfo splitSettlementInfo) {
        this.splitSettlementInfo = splitSettlementInfo;
        return this;
    }

    public String getSubscriptionPaymentMode() {
        return subscriptionPaymentMode;
    }

    public void setSubscriptionPaymentMode(String subscriptionPaymentMode) {
        this.subscriptionPaymentMode = subscriptionPaymentMode;
    }

    public String getSubscriptionAmountType() {
        return subscriptionAmountType;
    }

    public void setSubscriptionAmountType(String subscriptionAmountType) {
        this.subscriptionAmountType = subscriptionAmountType;
    }

    public String getEmiId() {
        return emiId;
    }

    public Body setEmiId(String emiId) {
        this.emiId = emiId;
        return this;
    }

    public String getSubscriptionMaxAmount() {
        return subscriptionMaxAmount;
    }

    public void setSubscriptionMaxAmount(String subscriptionMaxAmount) {
        this.subscriptionMaxAmount = subscriptionMaxAmount;
    }

    public String getSubscriptionFrequency() {
        return subscriptionFrequency;
    }

    public void setSubscriptionFrequency(String subscriptionFrequency) {
        this.subscriptionFrequency = subscriptionFrequency;
    }

    public String getSubscriptionFrequencyUnit() {
        return subscriptionFrequencyUnit;
    }

    public void setSubscriptionFrequencyUnit(String subscriptionFrequencyUnit) {
        this.subscriptionFrequencyUnit = subscriptionFrequencyUnit;
    }

    public String getSubscriptionExpiryDate() {
        return subscriptionExpiryDate;
    }

    public void setSubscriptionExpiryDate(String subscriptionExpiryDate) {
        this.subscriptionExpiryDate = subscriptionExpiryDate;
    }

    public String getSubscriptionEnableRetry() {
        return subscriptionEnableRetry;
    }

    public void setSubscriptionEnableRetry(String subscriptionEnableRetry) {
        this.subscriptionEnableRetry = subscriptionEnableRetry;
    }

    public String getSubscriptionGraceDays() {
        return subscriptionGraceDays;
    }

    public void setSubscriptionGraceDays(String subscriptionGraceDays) {
        this.subscriptionGraceDays = subscriptionGraceDays;
    }

    public String getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(String subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public String getSubscriptionRetryCount() {
        return subscriptionRetryCount;
    }

    public void setSubscriptionRetryCount(String subscriptionRetryCount) {
        this.subscriptionRetryCount = subscriptionRetryCount;
    }


    @JsonProperty("subsPPIOnly")
    public String getSubsPPIOnly() {
        return subsPPIOnly;
    }

    @JsonProperty("subsPPIOnly")
    public void setSubsPPIOnly(String subsPPIOnly) {
        this.subsPPIOnly = subsPPIOnly;
    }

    @JsonProperty("appInvokeDevice")
    public String getAppInvokeDevice() {
        return appInvokeDevice;
    }

    @JsonProperty("appInvokeDevice")
    public void setAppInvokeDevice(String appInvokeDevice) {
        this.appInvokeDevice = appInvokeDevice;
    }

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("isNativeAddMoney")
    public void setIsNativeAddMoney(String isNativeAddMoney) {
        this.isNativeAddMoney = isNativeAddMoney;
    }

    @JsonProperty("validateAccountNumber")
    public String getValidateAccountNumber() {
        return this.validateAccountNumber;
    }


    @JsonProperty("allowUnverifiedAccount")
    public String getAllowUnverifiedAccount() {
        return this.allowUnverifiedAccount;
    }

    @JsonProperty("allowUnverifiedAccount")
    public void setAllowUnverifiedAccount(String allowUnverifiedAccount) {
        this.allowUnverifiedAccount = allowUnverifiedAccount;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("websiteName")
    public String getWebsiteName() {
        return websiteName;
    }

    @JsonProperty("websiteName")
    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    @JsonProperty("cardTokenRequired")
    public String getCardTokenRequired() {
        return cardTokenRequired;
    }

    @JsonProperty("cardTokenRequired")
    public void setCardTokenRequired(String cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
    }

    @JsonProperty("emiOption")
    public String getEmiOption() {
        return emiOption;
    }

    @JsonProperty("emiOption")
    public void setEmiOption(String emiOption) {
        this.emiOption = emiOption;
    }

    @JsonProperty("subwalletAmount")
    public SubwalletAmount getSubwalletAmount() {
        return subwalletAmount;
    }

    @JsonProperty("subwalletAmount")
    public void setSubwalletAmount(SubwalletAmount subwalletAmount) {
        this.subwalletAmount = subwalletAmount;
    }

    @JsonProperty("chargeAmount")
    public TxnAmount getChargeAmount() {
        return chargeAmount;
    }

    @JsonProperty("chargeAmount")
    public Body setChargeAmount(TxnAmount chargeAmount) {
        this.chargeAmount = chargeAmount;
        return this;
    }

    @JsonProperty("txnAmount")
    public TxnAmount getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(TxnAmount txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("userInfo")
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @JsonProperty("userInfo")
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @JsonProperty("simplifiedPaymentOffers")
    public SimplifiedPaymentOffers getSimplifiedPaymentOffers() {
        return simplifiedPaymentOffers;
    }

    @JsonProperty("simplifiedPaymentOffers")
    public void setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers) {
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
    }

    @JsonProperty("simplifiedSubvention")
    public SimplifiedSubvention getSimplifiedSubvention() {
        return simplifiedSubvention;
    }

    @JsonProperty("simplifiedSubvention")
    public void setSimplifiedSubvention(SimplifiedSubvention simplifiedSubvention) {
        this.simplifiedSubvention = simplifiedSubvention;
    }

    public SimplifiedUnifiedOffers getSimplifiedUnifiedOffers() {
        return simplifiedUnifiedOffers;
    }

    public void setSimplifiedUnifiedOffers(SimplifiedUnifiedOffers simplifiedUnifiedOffers) {
        this.simplifiedUnifiedOffers = simplifiedUnifiedOffers;
    }

    @JsonProperty("ultimateBeneficiaryDetails")
    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }
    @JsonProperty("ultimateBeneficiaryDetails")
    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    @JsonProperty("paymentOffersApplied")
    public PaymentOffersApplied getPaymentOffersApplied() {
        return paymentOffersApplied;
    }

    @JsonProperty("paymentOffersApplied")
    public void setPaymentOffersApplied(PaymentOffersApplied paymentOffersApplied) {
        this.paymentOffersApplied = paymentOffersApplied;
    }

    @JsonProperty("paytmSsoToken")
    public String getPaytmSsoToken() {
        return paytmSsoToken;
    }

    @JsonProperty("paytmSsoToken")
    public void setPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
    }

    @JsonProperty("disablePaymentMode")
    public DisablePaymentMode[] getDisablePaymentMode() {
        return disablePaymentMode;
    }

    @JsonProperty("disablePaymentMode")
    public void setDisablePaymentMode(DisablePaymentMode[] disablePaymentMode) {
        this.disablePaymentMode = disablePaymentMode;
    }

    @JsonProperty("enablePaymentMode")
    public EnablePaymentMode[] getEnablePaymentMode() {
        return enablePaymentMode;
    }

    @JsonProperty("enablePaymentMode")
    public void setEnablePaymentMode(EnablePaymentMode[] enablePaymentMode) {
        this.enablePaymentMode = enablePaymentMode;
    }
    @JsonProperty("promoCode")
    public String getPromoCode() {
        return promoCode;
    }

    @JsonProperty("promoCode")
    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    @JsonProperty("callbackUrl")
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty("callbackUrl")
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @JsonProperty("goods")
    public Good[] getGoods() {
        return goods;
    }

    @JsonProperty("goods")
    public void setGoods(Good[] goods) {
        this.goods = goods;
    }

    @JsonProperty("aggType")
    public String getAggType() {
        return aggType;
    }

    @JsonProperty("aggType")
    public void setAggType(String aggType) {
        this.aggType = aggType;
    }

    @JsonProperty("offlineFlow")
    public String getOfflineFlow() {
        return offlineFlow;
    }

    @JsonProperty("offlineFlow")
    public void setOfflineFlow(String offlineFlow) {
        this.offlineFlow = offlineFlow;
    }

    @JsonProperty("orderPricingInfo")
    public OrderPricingInfo getOrderPricingInfo() {
        return orderPricingInfo;
    }
    @JsonProperty("orderPricingInfo")
    public void setOrderPricingInfo(OrderPricingInfo orderPricingInfo) {
        this.orderPricingInfo = orderPricingInfo;
    }


    @JsonProperty("shippingInfo")
    public ShippingInfo[] getShippingInfo() {
        return shippingInfo;
    }

    @JsonProperty("shippingInfo")
    public void setShippingInfo(ShippingInfo[] shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    @JsonProperty("invoiceDetails")
    public InvoiceDetails getInvoiceDetails() {
        return invoiceDetails;
    }

    @JsonProperty("invoiceDetails")
    public void setInvoiceDetails(InvoiceDetails invoiceDetails) {
        this.invoiceDetails = invoiceDetails;
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
    @JsonProperty("vanInfo")
    public VanInfo getVanInfo() {
        return vanInfo;
    }

    @JsonProperty("vanInfo")
    public void setVanInfo(VanInfo vanInfo) {
        this.vanInfo = vanInfo;
    }

    @JsonProperty("tpvInfos")
    public List<TpvInfo> getTpvInfos() {
        return tpvInfos;
    }

    @JsonProperty("tpvInfos")
    public Body setTpvInfos(List<TpvInfo> tpvInfos) {
        this.tpvInfos = tpvInfos;
        return  this;
    }

    @JsonProperty("payerAccountDetails")
    public List<PayerAccountDetail> getPayerAccountDetails() {
        return payerAccountDetails;
    }

    @JsonProperty("payerAccountDetails")
    public Body setPayerAccountDetails(List<PayerAccountDetail> payerAccountDetails) {
        this.payerAccountDetails = payerAccountDetails;
        return this;
    }

    @JsonProperty("autoRefund")
    public Boolean getAutoRefund() {
        return autoRefund;
    }

    @JsonProperty("autoRefund")
    public void setAutoRefund(Boolean autoRefund) {
        this.autoRefund = autoRefund;
    }


    public RiskExtendInfo getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public Body setRiskExtendInfo(RiskExtendInfo riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
        return this;
    }


    @JsonProperty("isNativeAddMoney")
    public String getIsNativeAddMoney() {
        return isNativeAddMoney;
    }

    public List<String> getBankAccountNumbers() {
        return bankAccountNumbers;
    }

    public void setBankAccountNumbers(List<String> bankAccountNumbers) {
        this.bankAccountNumbers = bankAccountNumbers;
    }

    public AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    public Body setAdditionalInfo(AdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public MutualFundFeedInfo getMutualFundFeedInfo() {
        return mutualFundFeedInfo;
    }

    public Body setMutualFundFeedInfo(MutualFundFeedInfo mutualFundFeedInfo) {
        this.mutualFundFeedInfo = mutualFundFeedInfo;
        return this;
    }

    public LinkDetails getLinkDetails() {
        return linkDetails;
    }

    public void setLinkDetails(LinkDetails linkDetails) {
        this.linkDetails = linkDetails;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setValidateAccountNumber(String validateAccountNumber) {
        this.validateAccountNumber = validateAccountNumber;
    }
    @JsonProperty("affordabilityInfo")
    public AffordabilityInfo getAffordabilityInfo() {
        return affordabilityInfo;
    }

    @JsonProperty("affordabilityInfo")
    public void setAffordabilityInfo(AffordabilityInfo affordabilityInfo) {
        this.affordabilityInfo = affordabilityInfo;
    }
    @JsonProperty("affordabilityDetails")
    public AffordabilityDetails[] getAffordabilityDetails() {
        return affordabilityDetails;
    }

    @JsonProperty("affordabilityDetails")
    public void setAffordabilityDetails(AffordabilityDetails[] affordabilityDetails) {
        this.affordabilityDetails = affordabilityDetails;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    @JsonProperty("cashierAdditionalInfo")
    public CashierAdditionalInfo getCashierAdditionalInfo() {
        return cashierAdditionalInfo;
    }

    @JsonProperty("cashierAdditionalInfo")
    public Body setCashierAdditionalInfo(CashierAdditionalInfo cashierAdditionalInfo) {
        this.cashierAdditionalInfo = cashierAdditionalInfo;
        return this;
    }

    @JsonProperty("encryptedParams")
    public String getEncryptedParams() {
        return encryptedParams;
    }

    @JsonProperty("encryptedParams")
    public void setEncryptedParams(String encryptedParams) {
        this.encryptedParams = encryptedParams;
    }

    @JsonProperty("encKeyId")
    public String getEncKeyId() {
        return encKeyId;
    }

    @JsonProperty("encKeyId")
    public void setEncKeyId(String encKeyId) {
        this.encKeyId = encKeyId;
    }

    public Body(String callbackUrl, String websiteName, String isNativeAddMoney, DisablePaymentMode[]
            disablePaymentMode, EnablePaymentMode[] enablePaymentMode, String requestType, String paytmSsoToken, String
                        mid, UserInfo userInfo, String promoCode, TxnAmount txnAmount, String emiOptions, String orderId, String
                        cardTokenRequired, ExtendInfo extendInfo) {
        this.requestType = requestType;
        this.mid = mid;
        this.isNativeAddMoney = isNativeAddMoney;
        this.orderId = orderId;
        this.websiteName = websiteName;
        this.callbackUrl = callbackUrl;
        this.websiteName = websiteName;
        this.paytmSsoToken = paytmSsoToken;
        this.disablePaymentMode = disablePaymentMode;
        this.enablePaymentMode = enablePaymentMode;
        this.userInfo = userInfo;
        this.promoCode = promoCode;
        this.txnAmount = txnAmount;
        this.shippingInfo = new ShippingInfo[]{new ShippingInfo()};
        this.extendInfo = new ExtendInfo();
        this.goods = new Good[]{new Good()};
        this.emiOption = emiOptions;
        this.cardTokenRequired = cardTokenRequired;
        this.extendInfo = extendInfo;
    }


    public Body(String callbackUrl, String websiteName, DisablePaymentMode[] disablePaymentMode, EnablePaymentMode[]
            enablePaymentMode, String requestType, String paytmSsoToken, String mid, UserInfo userInfo, String
                        promoCode, TxnAmount txnAmount, String emiOptions, String orderId, String cardTokenRequired, ExtendInfo
                        extendInfo, String allowUnverifiedAccount, String validateAccountNumber, String aggMid, String isNativeAddMoney) {
        this(callbackUrl, websiteName, isNativeAddMoney, disablePaymentMode, enablePaymentMode, requestType, paytmSsoToken, aggMid, userInfo, promoCode, txnAmount, emiOptions, orderId, cardTokenRequired, extendInfo);
        this.allowUnverifiedAccount = allowUnverifiedAccount;
        this.validateAccountNumber = validateAccountNumber;
        this.aggrMid = aggMid;
        this.mid = mid;
    }

    @Override
    public String toString() {
        return "Body{" +
                "requestType='" + requestType + '\'' +
                ", isNativeAddMoney='" + isNativeAddMoney + '\'' +
                ", mid='" + mid + '\'' +
                ", aggType='" + aggType + '\'' +
                ", offlineFlow='" + offlineFlow + '\'' +
                ", cardHash='" + cardHash + '\'' +
                ", emiSubventionToken='" + emiSubventionToken + '\'' +
                ", emiId='" + emiId + '\'' +
                ", peonUrl='" + peonUrl + '\'' +
                ", validateAccountNumber='" + validateAccountNumber + '\'' +
                ", allowUnverifiedAccount='" + allowUnverifiedAccount + '\'' +
                ", mandateType='" + mandateType + '\'' +
                ", mandateAuthMode='" + mandateAuthMode + '\'' +
                ", aggrMid='" + aggrMid + '\'' +
                ", orderId='" + orderId + '\'' +
                ", websiteName='" + websiteName + '\'' +
                ", cardTokenRequired='" + cardTokenRequired + '\'' +
                ", emiOption='" + emiOption + '\'' +
                ", subwalletAmount=" + subwalletAmount +
                ", chargeAmount=" + chargeAmount +
                ", txnAmount=" + txnAmount +
                ", payableAmount=" + payableAmount +
                ", userInfo=" + userInfo +
                ", paytmSsoToken='" + paytmSsoToken + '\'' +
                ", disablePaymentMode=" + Arrays.toString(disablePaymentMode) +
                ", enablePaymentMode=" + Arrays.toString(enablePaymentMode) +
                ", promoCode='" + promoCode + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", goods=" + Arrays.toString(goods) +
                ", shippingInfo=" + Arrays.toString(shippingInfo) +
                ", extendInfo=" + extendInfo +
                ", subscriptionPaymentMode='" + subscriptionPaymentMode + '\'' +
                ", subscriptionAmountType='" + subscriptionAmountType + '\'' +
                ", subscriptionMaxAmount='" + subscriptionMaxAmount + '\'' +
                ", subscriptionFrequency='" + subscriptionFrequency + '\'' +
                ", subscriptionFrequencyUnit='" + subscriptionFrequencyUnit + '\'' +
                ", subscriptionExpiryDate='" + subscriptionExpiryDate + '\'' +
                ", subscriptionEnableRetry='" + subscriptionEnableRetry + '\'' +
                ", subscriptionGraceDays='" + subscriptionGraceDays + '\'' +
                ", subscriptionStartDate='" + subscriptionStartDate + '\'' +
                ", subscriptionRetryCount='" + subscriptionRetryCount + '\'' +
                ", subsPPIOnly='" + subsPPIOnly + '\'' +
                ", riskFeeDetails=" + riskFeeDetails +
                ", appInvokeDevice='" + appInvokeDevice + '\'' +
                ", orderPricingInfo=" + orderPricingInfo +
                ", simplifiedPaymentOffers=" + simplifiedPaymentOffers +
                ", paymentOffersApplied=" + paymentOffersApplied +
                ", addMoneyFeeAppliedOnWallet=" + addMoneyFeeAppliedOnWallet +
                ", splitSettlementInfo=" + splitSettlementInfo +
                ", accountNumber='" + accountNumber + '\'' +
                ", riskExtendInfo=" + riskExtendInfo +
                ", simplifiedSubvention=" + simplifiedSubvention +
                ", simplifiedUnifiedOffers=" + simplifiedUnifiedOffers +
                ", ultimateBeneficiaryDetails=" + ultimateBeneficiaryDetails +
                ", bankAccountNumbers=" + bankAccountNumbers +
                ", additionalInfo=" + additionalInfo +
                ", mutualFundFeedInfo=" + mutualFundFeedInfo +
                ", linkDetails=" + linkDetails +
                ", additionalProperties=" + additionalProperties +
                ", vanInfo=" + vanInfo +
                ",TpvInfo=" + tpvInfos +
                ", autoRefund=" + autoRefund +
                ", cashierAdditionalInfo=" + cashierAdditionalInfo +
                '}';
    }
}

