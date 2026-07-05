package com.paytm.dto.FastForwardApp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.processTransactionV1.ExtendInfo;

public class FastForwardAppRequest {

    @JsonProperty("body")
    private Body body;
    @JsonProperty("head")
    private Head head;
    ExtendInfo extendInfo = new ExtendInfo();


    private FastForwardAppRequest(Builder builder ,boolean posId) {
        if(posId!=true){
         extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(builder.additionalInfo
                .replace("{secondaryNumber}",builder.merchantContactNo)
                 .replace("{orderId}",builder.orderId)
                .replace("{merchantNumber}",builder.merchantId)
                .replace("{qrCodeId}",builder.qrCodeId)
                .replace("{merchantGUID}",builder.merchantGUID)
                .replace("{orderAlreadyCreated}",builder.orderAlreadyCreated)
                .replace("{txnAmount}",builder.txnAmount)
                .replace("{chargeAmount}",builder.chargeAmount)
                .replace("posId:{posId}","")
        );}
        else{
            extendInfo = new ExtendInfo();
            extendInfo.setAdditionalInfo(builder.additionalInfo
                    .replace("{secondaryNumber}",builder.merchantContactNo)
                    .replace("{orderId}",builder.orderId)
                    .replace("{merchantNumber}",builder.merchantId)
                    .replace("{qrCodeId}",builder.qrCodeId)
                    .replace("{merchantGUID}",builder.merchantGUID)
                    .replace("{orderAlreadyCreated}",builder.orderAlreadyCreated)
                    .replace("{txnAmount}",builder.txnAmount)
                    .replace("{chargeAmount}",builder.chargeAmount)
                    .replace("{posId}",builder.posId)

            );}
        if(null!=(builder.orderAdditionalInfoMcc)
                && null !=(builder.orderAdditionalInfoMLogo)
                && null !=(builder.orderAdditionalInfoMName) )
        extendInfo.setOrderAdditionalInfo(
                new OrderAdditionalInfo().setMName(builder.orderAdditionalInfoMName)
                        .setMID(builder.mid).setMcc(builder.orderAdditionalInfoMcc)
                        .setMLogo(builder.orderAdditionalInfoMLogo));

        this.body = new Body()
                .setAuthMode(builder.authMode)
                .setChannel(builder.channel)
                .setCurrency(builder.currency)
                .setCustomerId(builder.customerId)
                .setDeviceId(builder.deviceId)
                .setExtendInfo(extendInfo)
                .setIndustryType(builder.industryType)
                .setOrderId(builder.orderId)
                .setPaymentMode(builder.paymentMode)
                .setReqType(builder.reqType)
                .setTxnAmount(builder.txnAmount);

        this.head = new Head()
                .setMid(builder.mid)
                .setRequestId(CommonHelpers.generateOrderId())
                .setToken(builder.token)
                .setTokenType(builder.tokenType);
    }

    public Body getBody() {
        return body;
    }

    public Head getHead() {
        return head;
    }

    public static class Builder {

        private String authMode = "USRPWD";
        private String channel = "WAP";
        private String currency = "INR";
        private String customerId = "1000002621";
        private String deviceId = "351898083343873";
        private ExtendInfo extendInfo;
        private String industryType = "Retail";
        private String additionalInfo = "merchantContactNo:{secondaryNumber}|payeeType:MERCHANT|currencyCode:INR|category:Food|subCategory:Restaurant|service:P2M|" +
                "mode:QR_CODE|offlinePostConvenience:false|mappingId:{merchantNumber}|pgEnabled:true|" +
                "merchantTransId:{orderId}|qrCodeId:{qrCodeId}|merchantVerified:false|REQUEST_TYPE:QR_MERCHANT|" +
                "EXPIRY_DATE:|NAME:AutomationMerchant004|MERCHANT_NAME:AutomationMerchant004|MOBILE_NO:7007101778|" +
                "TXN_AMOUNT:{txnAmount}|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:{merchantGUID}|ORDER_ID:PARCEL938066|" +
                "MERCHANT_STATUS:ACTIVE|CHANNEL_ID:WEB|orderAlreadyCreated:{orderAlreadyCreated}|qr_code_id:{qrCodeId}|chargeAmount:{chargeAmount}|comment:|posId:{posId}";
        private String orderId;
        private String paymentMode;
        private String reqType;
        private String txnAmount;
        private String mid;
        private String token;
        private String tokenType;
        private String chargeAmount = "0.00";
        private String merchantId =  "pulkit03505318276468";
        private String merchantGUID = "7aa1f178-90b4-48a6-8f03-cfda4b820950";
        private String orderAlreadyCreated = "false";
        private String qrCodeId = "281005050101YB35ILKGN3QU"; //QR Code ID needs to be updated with new qrcodeID if this gets expired
        private String merchantContactNo = "9999888822";
        private String orderAdditionalInfoMName = null;
        private String orderAdditionalInfoMLogo = null;
        private String orderAdditionalInfoMcc = null;
        private String posId;

        public Builder(String mid, String orderId, String txnAmount) {
            this.mid = mid;
            this.orderId = orderId;
            this.txnAmount = txnAmount;
        }

        public Builder setAuthMode(String authMode) {
            this.authMode = authMode;
            return this;
        }

        public Builder setChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Builder setIndustryType(String industryType) {
            this.industryType = industryType;
            return this;
        }

        public Builder setAdditionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
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

        public Builder setReqType(String reqType) {
            this.reqType = reqType;
            return this;
        }

        public Builder setTxnAmount(String txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setOrderAlreadyCreated(String orderAlreadyCreated) {
            this.orderAlreadyCreated = orderAlreadyCreated;
            return this;
        }

        public Builder setQrCodeId(String qrCodeId) {
            this.qrCodeId = qrCodeId;
            return this;
        }

        public Builder setChargeAmount(String chargeAmount) {
            this.chargeAmount = chargeAmount;
            return this;
        }

        public Builder setMerchantContactNumber(String merchantContactNo)
        {
            this.merchantContactNo = merchantContactNo;
            return this;
        }

        public Builder setMerchantId(String merchantId)
        {
            this.merchantId = merchantId;
            return this;
        }


        public Builder setMerchantGUID(String merchantGUID)
        {
            this.merchantGUID = merchantGUID;
            return this;
        }

        public Builder setOrderAdditionalInfoMName(String mName)
        {
            this.orderAdditionalInfoMName = mName;
            return this;

        }

        public Builder setOrderAdditionalInfoMLogo(String mLogo)
        {
            this.orderAdditionalInfoMLogo = mLogo;
            return this;
        }

        public Builder setOrderAdditionalInfoMCC(String mcc)
        {
            this.orderAdditionalInfoMcc = mcc;
            return this;
        }

        public Builder setPosId(String posId)
        {
            this.posId = posId;
            return this;
        }
        public Builder setofflineAppMode() {
            this.additionalInfo="merchantContactNo:{secondaryNumber}|payeeType:MERCHANT|currencyCode:INR|category:Food|subCategory:Restaurant|service:P2M|" +
                    "mode:QR_CODE|offlinePostConvenience:false|mappingId:{merchantNumber}|pgEnabled:true|" +
                    "merchantTransId:{orderId}|qrCodeId:{qrCodeId}|merchantVerified:false|REQUEST_TYPE:QR_MERCHANT|" +
                    "EXPIRY_DATE:|NAME:AutomationMerchant004|MERCHANT_NAME:AutomationMerchant004|MOBILE_NO:7007101778|" +
                    "TXN_AMOUNT:{txnAmount}|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:{merchantGUID}|ORDER_ID:PARCEL938066|" +
                    "MERCHANT_STATUS:ACTIVE|CHANNEL_ID:WEB|offlineAppMode: chat|orderAlreadyCreated:{orderAlreadyCreated}|qr_code_id:{qrCodeId}|chargeAmount:{chargeAmount}|comment:|posId:{posId}";
            return this;
        }

        public FastForwardAppRequest build() {
            boolean posId=false;
            return new FastForwardAppRequest(this,posId);
        }

        public FastForwardAppRequest build(Boolean posId) {
            return new FastForwardAppRequest(this,posId);
        }

    }

}
