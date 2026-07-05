package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {
    private ResultInfo resultInfo;
    private String paymentFlow;
    private MerchantPayOption merchantPayOption;
    private AddMoneyPayOption addMoneyPayOption;
    private AddMoneyMerchantDetails addMoneyMerchantDetails;
    private MerchantDetails merchantDetails;
    private String merchantOfferMessage;
    private PromoCodeData promoCodeData;
    private boolean walletOnly;
    private boolean zeroCostEmi;
    private String orderId;
    private SubscriptionDetail subscriptionDetail;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }
    public SubscriptionDetail getSubscriptionDetail() {
        return subscriptionDetail;
    }

    public void setSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        this.subscriptionDetail = subscriptionDetail;
    }

    public String getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(String paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public MerchantPayOption getMerchantPayOption() {
        return merchantPayOption;
    }

    public void setMerchantPayOption(MerchantPayOption merchantPayOption) {
        this.merchantPayOption = merchantPayOption;
    }

    public AddMoneyPayOption getAddMoneyPayOption() {
        return addMoneyPayOption;
    }

    public void setAddMoneyPayOption(AddMoneyPayOption addMoneyPayOption) {
        this.addMoneyPayOption = addMoneyPayOption;
    }

    public AddMoneyMerchantDetails getAddMoneyMerchantDetails() {
        return addMoneyMerchantDetails;
    }

    public void setAddMoneyMerchantDetails(AddMoneyMerchantDetails addMoneyMerchantDetails) {
        this.addMoneyMerchantDetails = addMoneyMerchantDetails;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
    }

    public String getMerchantOfferMessage() {
        return merchantOfferMessage;
    }

    public void setMerchantOfferMessage(String merchantOfferMessage) {
        this.merchantOfferMessage = merchantOfferMessage;
    }

    public PromoCodeData getPromoCodeData() {
        return promoCodeData;
    }

    public void setPromoCodeData(PromoCodeData promoCodeData) {
        this.promoCodeData = promoCodeData;
    }

    public boolean getWalletOnly() {
        return walletOnly;
    }

    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    public boolean getZeroCostEmi() {
        return zeroCostEmi;
    }

    public void setZeroCostEmi(boolean zeroCostEmi) {
        this.zeroCostEmi = zeroCostEmi;
    }
}
