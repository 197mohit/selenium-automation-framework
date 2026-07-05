package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.UltimateBeneficiaryDetails;
import com.paytm.dto.processTransactionV1.TipDetails;
import com.paytm.dto.processTransactionV1.TxnAmount;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchPaymentOptionsDTO {
    private Body body;
    //= new Body();
    private Head head;

    public FetchPaymentOptionsDTO(FetchPaymentOptionsDTO.Builder builder) {
        this.head = new Head(builder.channelId)
                .setTokenType(builder.tokenType)
                .setToken(builder.token)
                .setTxnToken(builder.txnToken)
                .setVersion(builder.version)
                .setWorkFlow(builder.workFlow)
                .setRequestId(builder.requestId);
        this.body = new Body()
                .setPostpaidOnboardingSupported(builder.postpaidOnboardingSupported)
                .setGenerateOrderId(builder.generateOrderId)
                .setMid(builder.mid)
                .setsupportedPayModesForAddNPay(builder.supportedPayModesForAddNPay)
                .setEMIOption(builder.emiOption)
                .setDisablePaymentMode(builder.disablePaymentMode)
                .setEnablePaymentMode(builder.enablePaymentMode)
                .setAmount(builder.amount)
                .setOrderAmount(builder.orderAmount)
                .setTxnAmount(builder.txnAmount)
                .setEmiSubventionRequired(builder.isEmiSubventionRequired)
                .setEmiSubventedTransactionAmount(builder.emiSubventedTransactionAmount)
                .setEmiSubventionCustomerId(builder.emiSubventionCustomerId)
                .setItems(builder.items)
                .setAddMoneyFeeAppliedOnWallet(builder.addMoneyFeeAppliedOnWallet)
                .setReferenceId(builder.referenceId)
                .setPaytmSsoToken(builder.paytmSsoToken)
                .setExtendInfo(builder.extendInfo)
                .setGoods(builder.goods)
                .setApplyPaymentOffers(builder.applyPaymentOffer)
                .setFetchAllPaymentOffers(builder.fetchAllPaymentOffers)
                .setEightDigitBinRequired(builder.eightDigitBinRequired)
                .setCardHashRequired(builder.cardHashRequired)
                .setFetchPaytmInstrumentsBalance(builder.fetchPaytmInstrumentsBalance)
                .setDeepLinkRequired(builder.deepLinkRequired)
                .setUltimateBeneficiaryDetails(builder.ultimateBeneficiaryDetails)
                .setIsLiteEligible(builder.isLiteEligible)
                .setOfflineFlow(builder.offlineFlow)
                .setFetchAddMoneyOptions(builder.fetchAddMoneyOptions)
                .setFetchAllItemOffers(builder.fetchAllItemOffers)
                .setApplyItemOffers(builder.applyItemOffers)
                .setFetchUnifiedOffers(builder.fetchUnifiedOffers)
                .settpap(builder.tpap);

    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "ClassPojo [body = " + body + ", Head = " + head + "]";
    }

    public static class Builder {
        private String channelId;
        private String txnToken;
        private String version;
        private String postpaidOnboardingSupported;
        private String tokenType;
        private String generateOrderId;
        private String mid;
        private String token;
        private String supportedPayModesForAddNPay;
        private String emiOption;
        private Double amount;
        private DisablePaymentMode[] disablePaymentMode;
        private EnablePaymentMode[] enablePaymentMode;
        private String orderAmount;
        private TxnAmount txnAmount;
        private Boolean isEmiSubventionRequired;
        private String emiSubventedTransactionAmount, emiSubventionCustomerId;
        private Boolean addMoneyFeeAppliedOnWallet;
        private List<Item> items = null;
        private List<Good> goods = null;
        private ExtendInfo extendInfo;
        private String fetchAllPaymentOffers;
        private String requestId;
        private String workFlow;
        private String applyPaymentOffer;
        private String eightDigitBinRequired;
        private String cardHashRequired;
        private String fetchPaytmInstrumentsBalance;
        private UltimateBeneficiaryDetails ultimateBeneficiaryDetails = null;
        private Boolean deepLinkRequired;
        private Boolean isLiteEligible;
        private String offlineFlow;
        private Boolean fetchAddMoneyOptions;
        private fetchAllItemOffers fetchAllItemOffers;
        private applyItemOffers applyItemOffers;
        private FetchUnifiedOffers fetchUnifiedOffers;
        private Boolean tpap;

        public FetchUnifiedOffers getFetchUnifiedOffers() {
            return fetchUnifiedOffers;
        }

        public Builder setFetchUnifiedOffers(FetchUnifiedOffers fetchUnifiedOffers) {
            this.fetchUnifiedOffers = fetchUnifiedOffers;
            return this;
        }

        public String getCardHashRequired() {
            return cardHashRequired;
        }

        public Builder setCardHashRequired(String cardHashRequired) {
            this.cardHashRequired = cardHashRequired;
            return this;
        }

        public String getFetchAllPaymentOffers() {
            return fetchAllPaymentOffers;
        }

        public Builder setFetchAllPaymentOffers(String fetchAllPaymentOffers) {
            this.fetchAllPaymentOffers = fetchAllPaymentOffers;
            return this;
        }

        public String getApplyPaymentOffer() {
            return applyPaymentOffer;
        }

        public Builder setApplyPaymentOffers(String applyPaymentOffer) {
            this.applyPaymentOffer = applyPaymentOffer;
            return this;
        }

        public String getEightDigitBinRequired() {
            return eightDigitBinRequired;
        }

        public Builder setEightDigitBinRequired(String eightDigitBinRequired) {
            this.eightDigitBinRequired = eightDigitBinRequired;
            return this;
        }

        public String getWorkFlow() {
            return workFlow;
        }

        public Builder setWorkFlow(String workFlow) {
            this.workFlow = workFlow;
            return this;
        }

        public String getRequestId() {
            return requestId;
        }

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public Builder setReferenceId(String referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public String getPaytmSsoToken() {
            return paytmSsoToken;
        }

        public Builder setPaytmSsoToken(String paytmSsoToken) {
            this.paytmSsoToken = paytmSsoToken;
            return this;
        }

        private String referenceId;
        private String paytmSsoToken;

        public Builder(String txnToken) {
            this.channelId = "WEB";
            this.txnToken = txnToken;
        }

        public Builder(String tokenType, String token) {
            this.tokenType = tokenType;
            this.token = token;
        }

        public Builder(String tokenType, String token, String mid) {
            this.tokenType = tokenType;
            this.token = token;
            this.mid = mid;
        }

        public Builder() {
            this.channelId = "WEB";
        }

        public Builder(String txnToken, boolean postpaidOnboardingSupported) {
            String postpaidStatus = Boolean.toString(postpaidOnboardingSupported);
            this.postpaidOnboardingSupported = postpaidStatus;
            this.channelId = "WEB";
            this.txnToken = txnToken;
        }

        public Builder(String txnToken, UltimateBeneficiaryDetails ultimateBeneficiaryDetails){
            this(txnToken);
            this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
        }

        public Builder setItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder setGoods(List<Good> goods) {
            this.goods = goods;
            return this;
        }

        public Builder setEmiSubventionRequired(Boolean emiSubventionRequired) {
            isEmiSubventionRequired = emiSubventionRequired;
            return this;
        }

        public Builder setEmiSubventedTransactionAmount(String emiSubventedTransactionAmount) {
            this.emiSubventedTransactionAmount = emiSubventedTransactionAmount;
            return this;
        }

        public Builder setEmiSubventionCustomerId(String emiSubventionCustomerId) {
            this.emiSubventionCustomerId = emiSubventionCustomerId;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setTxnToken(String token) {
            this.txnToken = token;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setsupportedPayModesForAddNPay(String supportedPayModesForAddNPay){
            this.supportedPayModesForAddNPay = supportedPayModesForAddNPay;
            return this;
        }

        public Builder setAddMoneyFeeAppliedOnWallet(Boolean addMoneyFeeAppliedOnWallet) {
            this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
            return this;
        }

        public Builder setGenerateOrderId(String generateOrderId) {
            this.generateOrderId = generateOrderId;
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Builder setAmount(Double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setPostpaidOnboardingSupported(String postpaidOnboardingSupported) {
            this.postpaidOnboardingSupported = postpaidOnboardingSupported;
            return this;
        }

        public Builder setEMIOption(String emiOption) {
            this.emiOption = emiOption;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setDisablePaymentMode(DisablePaymentMode[] disablePaymentMode) {
            this.disablePaymentMode = disablePaymentMode;
            return this;
        }

        public Builder setEnablePaymentMode(EnablePaymentMode[] enablePaymentMode) {
            this.enablePaymentMode = enablePaymentMode;
            return this;
        }

        public Builder setOrderAmount(String orderAmount) {
            this.orderAmount = orderAmount;
            return this;
        }
        public Builder setTxnAmount(TxnAmount txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public String getFetchPaytmInstrumentsBalance() {
            return fetchPaytmInstrumentsBalance;
        }

        public Builder setFetchPaytmInstrumentsBalance(String fetchPaytmInstrumentsBalance) {
            this.fetchPaytmInstrumentsBalance = fetchPaytmInstrumentsBalance;
            return this;
        }

        public Builder setDeepLinkRequiedField(Boolean isDeepLinkReq) {
            this.deepLinkRequired = isDeepLinkReq;
            return this;
        }
        public String getOfflineFlow() {
            return offlineFlow;
        }
        public Builder setOfflineFlow(String offlineFlow) {
            this.offlineFlow = offlineFlow;
            return this;
        }
        public Builder setIsLiteEligible(Boolean isLiteEligible) {
           this.isLiteEligible = isLiteEligible;
            return this;
        }

        public Boolean getTpap() {
            return tpap;
        }

        public Builder setTpap(Boolean tpap) {
            this.tpap = tpap;
            return this;
        }
        public Boolean getFetchAddMoneyOptions() {
            return fetchAddMoneyOptions;
        }
        public Builder setFetchAddMoneyOptions(Boolean fetchAddMoneyOptions) {
            this.fetchAddMoneyOptions = fetchAddMoneyOptions;
            return this;
        }

        public FetchPaymentOptionsDTO build() {
            return new FetchPaymentOptionsDTO(this);
        }

        public Builder setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
            this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
            return this;
        }
        public fetchAllItemOffers getFetchAllItemOffers() {
            return fetchAllItemOffers;
        }

        public Builder setFetchAllItemOffers(fetchAllItemOffers fetchAllItemOffers) {
            this.fetchAllItemOffers = fetchAllItemOffers;
            return this;
        }

        public applyItemOffers getApplyItemOffers() {
            return applyItemOffers;
        }

        public Builder setApplyItemOffers(applyItemOffers applyItemOffers) {
            this.applyItemOffers = applyItemOffers;
            return this;
        }

        public TxnAmount getTxnAmount() {
            return txnAmount;
        }
    }
}
