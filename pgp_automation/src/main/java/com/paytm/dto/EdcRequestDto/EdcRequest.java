package com.paytm.dto.EdcRequestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.processTransactionV1.ExtendInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EdcRequest {
    @JsonProperty("body")
    private Body body;
    @JsonProperty("head")
    private Head head;

    public Body getBody() {
        return body;
    }

    public Head getHead() {
        return head;
    }

    public EdcRequest(Builder builder)
    {
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(builder.additionalInfo
                .replace("{merchantTransId}",builder.orderId)
                .replace("{paytmMerchantId}",builder.mid)
        );

        this.body = new Body()
                .setProductType(builder.productType)
                .setProductId(builder.productId)
                .setPosId(builder.posId)
                .setPayAndConfirmRequest(builder.payAndConfirmRequest)
                .setOrderId(builder.orderId)
                .setOrderDetails(builder.orderDetails)
                .setMid(builder.mid)
                .setInvoiceDetails(builder.invoiceDetails)
                .setImageRequired(builder.imageRequired)
                .setExpiryDate(builder.expiryDate)
                .setDisplayName(builder.displayName)
                .setContactPhoneNo(builder.contactPhoneNo)
                .setBusinessType(builder.businessType)
                .setAmount(builder.amount)
                .setExtendInfo(extendInfo);


        this.head = new Head()
                .setSignature(builder.signature);
    }



    public static class Builder {

        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.plusDays(7);

        private String productType = "null";
        private String productId = "425653500";
        private String posId = "Dynamic042";
        private String payAndConfirmRequest = "true";
        private String orderId;
        private String orderDetails = "Dynamic042";
        private String invoiceDetails = "null";
        private String imageRequired = "true";
        private String expiryDate = then.format(format);
        private String displayName = "Dynamic043";
        private String contactPhoneNo = "9999161601";
        private String businessType = "QR_ORDER";
        private String amount;
        private String additionalInfo = "confirmTimeOut:864000|edcRequest:true|invoiceNumber:000001|isEdcRequest:true|linkBasedInvoicePayment:false|" +
                "merchantName:edcpay|merchantTransId:{orderId}|paytmMerchantId:{merchantId}|paytmTid:12347660|" +
                "posDate:0325|posId:12347660|posTime:202300|posYear:2025|" +
                "PRODUCT_CODE:51051000100000000047|topupAndPay:false|udf1:12347660";
        private String signature;
        private String mid;

        public Builder(String mid, String orderId, String amount){
            this.mid = mid;
            this.orderId = orderId;
            this.amount = amount;
        }

        public Builder setSignature(String signature){
            this.signature = signature;
            return this;
        }

        public EdcRequest build() {
            EdcRequest edcRequest = new EdcRequest(this);
            String checksum = PGPHelpers.getNativeChecksum("%tgiy7%ul2L9tN9d",edcRequest.getBody()); // Merchant will remain same
            edcRequest.getHead().setSignature(checksum);
            return edcRequest;
        }

    }
}
