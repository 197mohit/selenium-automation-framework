package com.paytm.dto.upiIntent.staticQR;

import com.fasterxml.jackson.annotation.*;
import com.paytm.LocalConfig;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;
import com.paytm.dto.PaymentDTO;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "header",
        "body"
})
public class StaticQrUpiPSPRequest {

    @JsonProperty("header")
    private Header header;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    //  JWT for theia/v1/order/pay/upipsp
    public StaticQrUpiPSPRequest(Builder builder) {
        body = new Body()
                .setRequestType(builder.getRequestType())
                .setIss(builder.getIss())
                .setCustID(builder.getCustID())
                .setMid(builder.getMid())
                .setPayerVpa(builder.getPayerVpa())
                .setTxnAmount(builder.getTxnAmount())
                .setPayeeVpa(builder.getPayeeVpa())
                .setOrderId(builder.orderId)
                .setPayerName(builder.payerName)
                .setPayerPSP(builder.payerPSP)
                .setType(builder.getTypeField());
        if (builder.getPayerPaymentInstrument()!=null)
        {
            body.setPayerPaymentInstrument(builder.getPayerPaymentInstrument());
        }
        if (builder.getPayerPaymentInstrument()!=null && builder.getPayerPaymentInstrumentFee()!=null)
        {
            body.setPayerPaymentInstrumentFee(builder.getPayerPaymentInstrumentFee());
        }
        if (builder.getCreditCardInfo() != null) {
            body.setCreditCardInfo(builder.getCreditCardInfo());
        }
        if (builder.getExtendInfo() != null) {
            body.setExtendInfo(builder.getExtendInfo());
        }


        Map<String, String> map = new HashMap<>();
        map.put("mid", builder.getMid());
//        map.put("payeeVpa", builder.getPayeeVpa());
        map.put("payerVpa", builder.getPayerVpa());
        map.put("requestType", builder.getRequestType());
        map.put("txnAmount", builder.getTxnAmount());
        if(builder.orderId != null)
            map.put("orderId", builder.orderId);
        String signature = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY);
        header = new Header()
                .setClientId(null)
                .setRequestTimestamp(CommonHelpers.getDate().toString())
                .setVersion("v1")
                .setRequestMsgId("paytm"+CommonHelpers.generateOrderId())
                .setSignature(signature);
    }

//  JWT for upi-psp-processor/v1/order/pay/upipsp
    public StaticQrUpiPSPRequest(Builder builder,String upiModesubType , String orderId ) {
        body = new Body()
                .setRequestType(builder.getRequestType())
                .setIss(builder.getIss())
                .setCustID(builder.getCustID())
                .setMid(builder.getMid())
                .setPayerVpa(builder.getPayerVpa())
                .setTxnAmount(builder.getTxnAmount())
                .setPayeeVpa(builder.getPayeeVpa())
                .setOrderId(builder.orderId)
                .setPayerName(builder.payerName)
                .setPayerPSP(builder.payerPSP)
                .setType(builder.getTypeField())
                .setPayerPaymentInstrument(builder.getPayerPaymentInstrument())
                .setPayerPaymentInstrumentFee(builder.getPayerPaymentInstrumentFee());


        Map<String, String> map = new HashMap<>();
        map.put("mid", builder.getMid());
//        map.put("payeeVpa", builder.getPayeeVpa());
        map.put("payerVpa", builder.getPayerVpa());
        map.put("requestType", builder.getRequestType());
        map.put("txnAmount", builder.getTxnAmount());
        if(builder.orderId != null)
            map.put("orderId", builder.orderId);
        String signature = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
        header = new Header()
                .setClientId(null)
                .setRequestTimestamp(CommonHelpers.getDate().toString())
                .setVersion("v1")
                .setRequestMsgId("paytm"+CommonHelpers.generateOrderId())
                .setSignature(signature);
    }

    //  JWT for theia/v1/order/pay/upipsp
    public StaticQrUpiPSPRequest(Builder builder, String upiModesubType) {
        body = new Body()
                .setRequestType(builder.getRequestType())
                .setIss(builder.getIss())
                .setCustID(builder.getCustID())
                .setMid(builder.getMid())
                .setPayerVpa(builder.getPayerVpa())
                .setTxnAmount(builder.getTxnAmount())
                .setPayeeVpa(builder.getPayeeVpa())
                .setOrderId(builder.orderId)
                .setPayerName(builder.payerName)
                .setPayerPSP(builder.payerPSP)
                .setType(builder.getTypeField())
                .setPayerPaymentInstrument(builder.getPayerPaymentInstrument())
                .setRiskExtendInfo(builder.getRiskExtendInfo());


        Map<String, String> map = new HashMap<>();
        map.put("mid", builder.getMid());
//        map.put("payeeVpa", builder.getPayeeVpa());
        map.put("payerVpa", builder.getPayerVpa());
        map.put("requestType", builder.getRequestType());
        map.put("txnAmount", builder.getTxnAmount());
        if(builder.orderId != null)
            map.put("orderId", builder.orderId);
        String signature = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY);
        header = new Header()
                .setClientId(null)
                .setRequestTimestamp(CommonHelpers.getDate().toString())
                .setVersion("v1")
                .setRequestMsgId("paytm"+CommonHelpers.generateOrderId())
                .setSignature(signature);
    }

    public static class Builder {
        private String requestType;
        private String iss;
        private String custID;
        private String mid;
        private String payerVpa;
        private String txnAmount;
        private String payeeVpa;
        private String orderId;
        private String payerName;
        private String payerPSP;
        private String typeField;
        private String payerPaymentInstrument;
        private CreditCardInfo creditCardInfo;
        private UpiPspExtendInfo extendInfo;
        private RiskExtendInfo riskExtendInfo;
        @Getter
        @Setter
        private String payerPaymentInstrumentFee;

        public Builder(String requestType,String mid,String orderId, String txnAmount,RiskExtendInfo riskExtendInfo) {
            this.requestType = "SEAMLESS_3D_FORM";
            this.iss = "ts";
            this.custID = "";
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = new PaymentDTO().getVpa();
            this.orderId = orderId;
            this.payeeVpa = "paytmqr@paytm";
            this.riskExtendInfo=riskExtendInfo;
        }
        public Builder(String mid, String txnAmount) {
            this.requestType = "SEAMLESS_3D_FORM";
            this.iss = "ts";
            this.custID = "";
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = new PaymentDTO().getVpa();
            this.payeeVpa = "paytmqr@paytm";
        }

        public Builder(String mid, String txnAmount, String qrCodeID, String upiModeSubType) {
            this.requestType = "SEAMLESS_3D_FORM";
            this.iss = "ts";
            this.custID = "";
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = new PaymentDTO().getVpa();
            this.payeeVpa = "paytmqr"+qrCodeID+"@paytm";
            this.payerPaymentInstrument = upiModeSubType;
        }


        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return  this;
        }

        public String getRequestType() {
            return requestType;
        }

        public Builder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public String getIss() {
            return iss;
        }

        public Builder setIss(String iss) {
            this.iss = iss;
            return this;
        }

        public String getCustID() {
            return custID;
        }

        public Builder setCustID(String custID) {
            this.custID = custID;
            return this;
        }

        public String getMid() {
            return mid;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getPayerVpa() {
            return payerVpa;
        }

        public Builder setPayerVpa(String payerVpa) {
            this.payerVpa = payerVpa;
            return this;
        }

        public String getTxnAmount() {
            return txnAmount;
        }

        public Builder setTxnAmount(String txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public String getPayeeVpa() {
            return payeeVpa;
        }

        public Builder setPayeeVpa(String payeeVpa) {
            this.payeeVpa = payeeVpa;
            return this;
        }

        public String getPayerName() {
            return payerName;
        }

        public Builder setPayerName(String payerName) {
            this.payerName = payerName;
            return this;
        }

        public String getPayerPSP() {
            return payerPSP;
        }

        public Builder setPayerPSP(String payerPSP) {
            this.payerPSP = payerPSP;
            return this;
        }

        public String getPayerPaymentInstrument(){ return payerPaymentInstrument;}

        public Builder setPayerPaymentInstrument(String payerPaymentInstrument) {
            this.payerPaymentInstrument = payerPaymentInstrument;
            return  this;
        }

        public CreditCardInfo getCreditCardInfo() {
            return creditCardInfo;
        }

        public Builder setCreditCardInfo(CreditCardInfo creditCardInfo) {
            this.creditCardInfo = creditCardInfo;
            return this;
        }

        public UpiPspExtendInfo getExtendInfo() {
            return extendInfo;
        }

        public Builder setExtendInfo(UpiPspExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public String getTypeField(){ return typeField;}

        public Builder setType(String typeField) {
            this.typeField = typeField;
            return  this;
        }
        public RiskExtendInfo getRiskExtendInfo() {
            return riskExtendInfo;
        }

        public Builder setRiskExtendInfo(RiskExtendInfo riskExtendInfo) {
            this.riskExtendInfo = riskExtendInfo;
            return this;
        }

        public StaticQrUpiPSPRequest build() {
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(this);
            return staticQrUpiPSPRequest;
        }

        public Builder(String requestType , String mid , String orderId , String txnAmount , String type){
            this.requestType = requestType;
            this.iss = "ts";
            this.custID = "12";
            this.orderId = orderId;
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = new PaymentDTO().getVpa();
            this.payeeVpa = "paytmqr28100505010192YRG8VPDJZS@paytm";
            this.typeField = type;
        }

        public Builder(String requestType , String mid , String orderId , String txnAmount , String qrCodeID, String upiModeSubType){
            this.requestType = requestType;
            this.iss = "ts";
            this.custID = "12";
            this.orderId = orderId;
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = new PaymentDTO().getVpa();
            this.payeeVpa = "paytmqr"+qrCodeID+"@paytm";
            this.payerPaymentInstrument = upiModeSubType;
        }

        public Builder(String mid , String orderId , String txnAmount,String payeeVpa,String payerVpa, String upiModeSubType,String payerPaymentInstrumentFee){
            this.requestType = "SEAMLESS_3D_FORM";
            this.iss = "ts";
            this.custID = "12";
            this.orderId = orderId;
            this.mid = mid;
            this.txnAmount = txnAmount;
            this.payerVpa = payerVpa;
            this.payeeVpa = payeeVpa;
            this.payerPaymentInstrument = upiModeSubType;
            this.payerPaymentInstrumentFee=payerPaymentInstrumentFee;
        }

        public Builder(String mid, String orderId, String txnAmount, String payeeVpa, String payerVpa,
                       String type, CreditCardInfo creditCardInfo, UpiPspExtendInfo extendInfo) {
            this.requestType = "SEAMLESS_3D_FORM";
            this.iss = "ts";
            this.custID = "1000036031";
            this.mid = mid;
            this.orderId = orderId;
            this.txnAmount = txnAmount;
            this.payeeVpa = payeeVpa;
            this.payerVpa = payerVpa;
            this.typeField = type;
            this.creditCardInfo = creditCardInfo;
            this.extendInfo = extendInfo;
            this.payerName = "ABC";
            this.payerPSP = "ptyes";
        }
    }

    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    @JsonProperty("header")
    public void setHeader(Header header) {
        this.header = header;
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

}
