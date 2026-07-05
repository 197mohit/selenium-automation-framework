package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.UltimateBeneficiaryDetails;
import com.paytm.dto.processTransactionV1.TxnAmount;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Body {

    private String postpaidOnboardingSupported;
    private String generateOrderId;
    private String mid;
    private String supportedPayModesForAddNPay;
    private String emiOption;
    private DisablePaymentMode[] disablePaymentMode;
    private EnablePaymentMode[] enablePaymentMode;
    private Double amount;
    private String orderAmount;
    private TxnAmount txnAmount;
    private Boolean isEmiSubventionRequired;
    private String emiSubventedTransactionAmount;
    private String emiSubventionCustomerId;
    private String referenceId;
    private String paytmSsoToken;
    private ExtendInfo extendInfo;
    private String fetchAllPaymentOffers;
    private String applyPaymentOffer;
    private String eightDigitBinRequired;
    private String cardHashRequired;
    private String fetchPaytmInstrumentsBalance;
    private Boolean deepLinkRequired;
    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;
    private Boolean isLiteEligible;
    private String offlineFlow;
    private Boolean fetchAddMoneyOptions;
    private FetchUnifiedOffers fetchUnifiedOffers;

    private fetchAllItemOffers fetchAllItemOffers;

    private applyItemOffers applyItemOffers;
    private Boolean tpap;

    public fetchAllItemOffers getFetchAllItemOffers() {
        return fetchAllItemOffers;
    }

    public Body setFetchAllItemOffers(fetchAllItemOffers fetchAllItemOffers) {
        this.fetchAllItemOffers = fetchAllItemOffers;
        return this;
    }

    public applyItemOffers getapplyItemOffers() {
        return applyItemOffers;
    }

    public Body setApplyItemOffers(applyItemOffers applyItemOffers) {
        this.applyItemOffers = applyItemOffers;
        return this;
    }


    public String getCardHashRequired() {
        return cardHashRequired;
    }

    public Body setCardHashRequired(String cardHashRequired) {
        this.cardHashRequired = cardHashRequired;
        return this;
    }

    public String getFetchAllPaymentOffers() {
        return fetchAllPaymentOffers;
    }

    public Body setFetchAllPaymentOffers(String fetchAllPaymentOffers) {
        this.fetchAllPaymentOffers = fetchAllPaymentOffers;
        return this;
    }

    public String getApplyPaymentOffer() {
        return applyPaymentOffer;
    }

    public Body setApplyPaymentOffers(String applyPaymentOffer) {
        this.applyPaymentOffer = applyPaymentOffer;
        return this;
    }

    public String getEightDigitBinRequired() {
        return eightDigitBinRequired;
    }

    public Body setEightDigitBinRequired(String eightDigitBinRequired) {
        this.eightDigitBinRequired = eightDigitBinRequired;
        return this;
    }


    public String getReferenceId() {
        return referenceId;
    }

    public Body setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public String getPaytmSsoToken() {
        return paytmSsoToken;
    }

    public Body setPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
        return this;
    }

    private Boolean addMoneyFeeAppliedOnWallet;
    private List<Item> items = null;
    private List<Good> goods = null;

    public Body() {
    }

    public Boolean isEmiSubventionRequired() {
        return isEmiSubventionRequired;
    }

    public Body setEmiSubventionRequired(Boolean emiSubventionRequired) {
        isEmiSubventionRequired = emiSubventionRequired;
        return this;
    }

    public String getEmiSubventedTransactionAmount() {
        return emiSubventedTransactionAmount;
    }

    public Body setEmiSubventedTransactionAmount(String emiSubventedTransactionAmount) {
        this.emiSubventedTransactionAmount = emiSubventedTransactionAmount;
        return this;
    }

    public String getEmiSubventionCustomerId() {
        return emiSubventionCustomerId;
    }

    public Body setEmiSubventionCustomerId(String emiSubventionCustomerId) {
        this.emiSubventionCustomerId = emiSubventionCustomerId;
        return this;
    }

    public Boolean getAddMoneyFeeAppliedOnWallet() {
        return addMoneyFeeAppliedOnWallet;
    }

    public Body setAddMoneyFeeAppliedOnWallet(Boolean addMoneyFeeAppliedOnWallet) {
        addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
        return this;
    }


    public List<Item> getItems() {
        return items;
    }

    public Body setItems(List<Item> items) {
        this.items = items;
        return this;
    }

    public List<Good> getGoods() {
        return goods;
    }

    public Body setGoods(List<Good> goods) {
        this.goods = goods;
        return this;
    }

    public String getMid() {
        return mid;
    }

    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getSupportedPayModesForAddNPay() {
        return supportedPayModesForAddNPay;
    }

    public Body setsupportedPayModesForAddNPay(String supportedPayModesForAddNPay){
        this.supportedPayModesForAddNPay = supportedPayModesForAddNPay;
        return this;
    }

    public String getGenerateOrderId() {
        return generateOrderId;
    }

    public Body setGenerateOrderId(String generateOrderId) {
        this.generateOrderId = generateOrderId;
        return this;
    }

    public String getPostpaidOnboardingSupported() {
        return postpaidOnboardingSupported;
    }

    public Body setPostpaidOnboardingSupported(String postpaidOnboardingSupported) {
        this.postpaidOnboardingSupported = postpaidOnboardingSupported;
        return this;
    }


    public String getEMIOption() {
        return emiOption;
    }

    public Body setEMIOption(String emiOption) {
        this.emiOption = emiOption;
        return this;
    }

    public DisablePaymentMode[] getDisablePaymentMode() {
        return disablePaymentMode;
    }

    public Body setDisablePaymentMode(DisablePaymentMode[] disablePaymentMode) {
        this.disablePaymentMode = disablePaymentMode;
        return this;
    }

    public EnablePaymentMode[] getEnablePaymentMode() {
        return enablePaymentMode;
    }

    public Body setEnablePaymentMode(EnablePaymentMode[] enablePaymentMode) {
        this.enablePaymentMode = enablePaymentMode;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public Body setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public Body setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
        return this;
    }
    public Body setTxnAmount(TxnAmount txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    public Body setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    public String getFetchPaytmInstrumentsBalance() {
        return fetchPaytmInstrumentsBalance;
    }

    public Body setFetchPaytmInstrumentsBalance(String fetchPaytmInstrumentsBalance) {
        this.fetchPaytmInstrumentsBalance = fetchPaytmInstrumentsBalance;
        return this;
    }

    public Boolean getDeepLinkRequired() {
        return deepLinkRequired;
    }

    public Body setDeepLinkRequired(Boolean deepLinkRequired) {
        this.deepLinkRequired = deepLinkRequired;
        return this;
    }

    public String getOfflineFlow() {
        return offlineFlow;
    }
    public Body setOfflineFlow(String offlineFlow) {
        this.offlineFlow = offlineFlow;
        return this;
    }

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public Body setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
        return this;
    }

    public Boolean getFetchAddMoneyOptionst() {
        return fetchAddMoneyOptions;
    }
    public Body setFetchAddMoneyOptions(Boolean fetchAddMoneyOptions) {
        this.fetchAddMoneyOptions = fetchAddMoneyOptions;
        return this;
    }

    public Boolean getIsLiteEligible() {
        return isLiteEligible;
    }

    public Body setIsLiteEligible(Boolean isLiteEligible) {
        this.isLiteEligible = isLiteEligible;
        return this;
    }
    public Boolean gettpap() {
        return tpap;
    }

    public Body settpap(Boolean tpap) {
        this.tpap = tpap;
        return this;
    }


    Object resultInfo;
    Object merchantDetails;

    public FetchUnifiedOffers getFetchUnifiedOffers() {
        return fetchUnifiedOffers;
    }

    public Body setFetchUnifiedOffers(FetchUnifiedOffers fetchUnifiedOffers) {
        this.fetchUnifiedOffers = fetchUnifiedOffers;
        return this;
    }

    public TxnAmount getTxnAmount() {
        return txnAmount;
    }



   /* @JsonProperty("binDetail")
    private BinData binDetail;

    @JsonProperty("authModes")
    private List<String> authModes;

    @JsonProperty("hasLowSuccessRate")
    private StatusInfo hasLowSuccessRate;

    @JsonProperty("iconUrl")
    private String iconUrl;

    @JsonProperty("emiDetail")
    private EmiChannel emiChannel;
*/

}

