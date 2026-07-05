package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO;

import java.util.List;

public class ProductCondition {

    private MaxAmount maxAmount;
    private List<String> payIntegrationTypes;
    private long orderTimeout;
    private String needLogin;
    private boolean supportMergeOrder;
    private List<String> payMethods;
    private String settleStrategy;
    private String settleCycle;
    private String settleAccountType;
    private String settleMethod;
    private String bankAccountType;
    private String cardToken;
    private String settleBalanceAccount;
    private String cardIndexNo;
    private boolean settleAfterClearingResult;
    private String chargebackPayoutAccountSource;
    private String chargebackDefaultPayoutAccount;
    private boolean supportRefund;
    private boolean supportMultiRefund;
    private String refundExpiryTime;
    private String pendingOrderRefundSource;
    private String defaultPendingPayoutAccount;
    private String refundPayoutAccounts;
    private String completedOrderRefundSource;
    private String defaultCompletedRefundAccount;
    private String extendInfo;
    private List<FeeItems> feeItems;
    private boolean supportPreCreateOrder;
    private String orderingMode;
    private String currency;
    private String stagePaymentType;
    private String acquiringMode;
    private String settleCurrency;
    private String refundCurrency;
    private String timeoutForInactiveOrder;
    private List<String> refundOptions;
    private String supportMultipleConfirm;
    private String confirmTimeout;
    private String supportExcessConfirm;
    private String excessProportion;
    private String allowPrnValidation;
    private String maxPrnValidRetryAllowCount;
    private String maxPrnValidRetryAllowTime;
    private String prnExpiryTime;
    private boolean isCancelAllowed;
    private int maxCancelAllowTime;

    public MaxAmount getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(MaxAmount maxAmount) {
        this.maxAmount = maxAmount;
    }

    public List<String> getPayIntegrationTypes() {
        return payIntegrationTypes;
    }

    public void setPayIntegrationTypes(List<String> payIntegrationTypes) {
        this.payIntegrationTypes = payIntegrationTypes;
    }

    public long getOrderTimeout() {
        return orderTimeout;
    }

    public void setOrderTimeout(long orderTimeout) {
        this.orderTimeout = orderTimeout;
    }

    public String getNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(String needLogin) {
        this.needLogin = needLogin;
    }

    public boolean isSupportMergeOrder() {
        return supportMergeOrder;
    }

    public void setSupportMergeOrder(boolean supportMergeOrder) {
        this.supportMergeOrder = supportMergeOrder;
    }

    public List<String> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<String> payMethods) {
        this.payMethods = payMethods;
    }

    public String getSettleStrategy() {
        return settleStrategy;
    }

    public void setSettleStrategy(String settleStrategy) {
        this.settleStrategy = settleStrategy;
    }

    public String getSettleCycle() {
        return settleCycle;
    }

    public void setSettleCycle(String settleCycle) {
        this.settleCycle = settleCycle;
    }

    public String getSettleAccountType() {
        return settleAccountType;
    }

    public void setSettleAccountType(String settleAccountType) {
        this.settleAccountType = settleAccountType;
    }

    public String getSettleMethod() {
        return settleMethod;
    }

    public void setSettleMethod(String settleMethod) {
        this.settleMethod = settleMethod;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public String getSettleBalanceAccount() {
        return settleBalanceAccount;
    }

    public void setSettleBalanceAccount(String settleBalanceAccount) {
        this.settleBalanceAccount = settleBalanceAccount;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public boolean isSettleAfterClearingResult() {
        return settleAfterClearingResult;
    }

    public void setSettleAfterClearingResult(boolean settleAfterClearingResult) {
        this.settleAfterClearingResult = settleAfterClearingResult;
    }

    public String getChargebackPayoutAccountSource() {
        return chargebackPayoutAccountSource;
    }

    public void setChargebackPayoutAccountSource(String chargebackPayoutAccountSource) {
        this.chargebackPayoutAccountSource = chargebackPayoutAccountSource;
    }

    public String getChargebackDefaultPayoutAccount() {
        return chargebackDefaultPayoutAccount;
    }

    public void setChargebackDefaultPayoutAccount(String chargebackDefaultPayoutAccount) {
        this.chargebackDefaultPayoutAccount = chargebackDefaultPayoutAccount;
    }

    public boolean isSupportRefund() {
        return supportRefund;
    }

    public void setSupportRefund(boolean supportRefund) {
        this.supportRefund = supportRefund;
    }

    public boolean isSupportMultiRefund() {
        return supportMultiRefund;
    }

    public void setSupportMultiRefund(boolean supportMultiRefund) {
        this.supportMultiRefund = supportMultiRefund;
    }

    public String getRefundExpiryTime() {
        return refundExpiryTime;
    }

    public void setRefundExpiryTime(String refundExpiryTime) {
        this.refundExpiryTime = refundExpiryTime;
    }

    public String getPendingOrderRefundSource() {
        return pendingOrderRefundSource;
    }

    public void setPendingOrderRefundSource(String pendingOrderRefundSource) {
        this.pendingOrderRefundSource = pendingOrderRefundSource;
    }

    public String getDefaultPendingPayoutAccount() {
        return defaultPendingPayoutAccount;
    }

    public void setDefaultPendingPayoutAccount(String defaultPendingPayoutAccount) {
        this.defaultPendingPayoutAccount = defaultPendingPayoutAccount;
    }

    public String getRefundPayoutAccounts() {
        return refundPayoutAccounts;
    }

    public void setRefundPayoutAccounts(String refundPayoutAccounts) {
        this.refundPayoutAccounts = refundPayoutAccounts;
    }

    public String getCompletedOrderRefundSource() {
        return completedOrderRefundSource;
    }

    public void setCompletedOrderRefundSource(String completedOrderRefundSource) {
        this.completedOrderRefundSource = completedOrderRefundSource;
    }

    public String getDefaultCompletedRefundAccount() {
        return defaultCompletedRefundAccount;
    }

    public void setDefaultCompletedRefundAccount(String defaultCompletedRefundAccount) {
        this.defaultCompletedRefundAccount = defaultCompletedRefundAccount;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }

    public List<FeeItems> getFeeItems() {
        return feeItems;
    }

    public void setFeeItems(List<FeeItems> feeItems) {
        this.feeItems = feeItems;
    }

    public boolean isSupportPreCreateOrder() {
        return supportPreCreateOrder;
    }

    public void setSupportPreCreateOrder(boolean supportPreCreateOrder) {
        this.supportPreCreateOrder = supportPreCreateOrder;
    }

    public String getOrderingMode() {
        return orderingMode;
    }

    public void setOrderingMode(String orderingMode) {
        this.orderingMode = orderingMode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStagePaymentType() {
        return stagePaymentType;
    }

    public void setStagePaymentType(String stagePaymentType) {
        this.stagePaymentType = stagePaymentType;
    }

    public String getAcquiringMode() {
        return acquiringMode;
    }

    public void setAcquiringMode(String acquiringMode) {
        this.acquiringMode = acquiringMode;
    }

    public String getSettleCurrency() {
        return settleCurrency;
    }

    public void setSettleCurrency(String settleCurrency) {
        this.settleCurrency = settleCurrency;
    }

    public String getRefundCurrency() {
        return refundCurrency;
    }

    public void setRefundCurrency(String refundCurrency) {
        this.refundCurrency = refundCurrency;
    }

    public String getTimeoutForInactiveOrder() {
        return timeoutForInactiveOrder;
    }

    public void setTimeoutForInactiveOrder(String timeoutForInactiveOrder) {
        this.timeoutForInactiveOrder = timeoutForInactiveOrder;
    }

    public List<String> getRefundOptions() {
        return refundOptions;
    }

    public void setRefundOptions(List<String> refundOptions) {
        this.refundOptions = refundOptions;
    }

    public String getSupportMultipleConfirm() {
        return supportMultipleConfirm;
    }

    public void setSupportMultipleConfirm(String supportMultipleConfirm) {
        this.supportMultipleConfirm = supportMultipleConfirm;
    }

    public String getConfirmTimeout() {
        return confirmTimeout;
    }

    public void setConfirmTimeout(String confirmTimeout) {
        this.confirmTimeout = confirmTimeout;
    }

    public String getSupportExcessConfirm() {
        return supportExcessConfirm;
    }

    public void setSupportExcessConfirm(String supportExcessConfirm) {
        this.supportExcessConfirm = supportExcessConfirm;
    }

    public String getExcessProportion() {
        return excessProportion;
    }

    public void setExcessProportion(String excessProportion) {
        this.excessProportion = excessProportion;
    }

    public String getAllowPrnValidation() {
        return allowPrnValidation;
    }

    public void setAllowPrnValidation(String allowPrnValidation) {
        this.allowPrnValidation = allowPrnValidation;
    }

    public String getMaxPrnValidRetryAllowCount() {
        return maxPrnValidRetryAllowCount;
    }

    public void setMaxPrnValidRetryAllowCount(String maxPrnValidRetryAllowCount) {
        this.maxPrnValidRetryAllowCount = maxPrnValidRetryAllowCount;
    }

    public String getMaxPrnValidRetryAllowTime() {
        return maxPrnValidRetryAllowTime;
    }

    public void setMaxPrnValidRetryAllowTime(String maxPrnValidRetryAllowTime) {
        this.maxPrnValidRetryAllowTime = maxPrnValidRetryAllowTime;
    }

    public String getPrnExpiryTime() {
        return prnExpiryTime;
    }

    public void setPrnExpiryTime(String prnExpiryTime) {
        this.prnExpiryTime = prnExpiryTime;
    }

    public boolean getIsCancelAllowed() {
        return isCancelAllowed;
    }

    public void setIsCancelAllowed(boolean cancelAllowed) {
        isCancelAllowed = cancelAllowed;
    }

    public int getMaxCancelAllowTime() {
        return maxCancelAllowTime;
    }

    public void setMaxCancelAllowTime(int maxCancelAllowTime) {
        this.maxCancelAllowTime = maxCancelAllowTime;
    }
}
