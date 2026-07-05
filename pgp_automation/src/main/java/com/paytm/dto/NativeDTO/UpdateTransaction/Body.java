package com.paytm.dto.NativeDTO.UpdateTransaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.Good;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "txnAmount",
        "goods",
        "shippingInfo",
        "extendInfo"
})
public class Body {

    @JsonProperty("txnAmount")
    private TxnAmount txnAmount;
    @JsonProperty("goods")
    private List<Good> goods = null;
    @JsonProperty("shippingInfo")
    private Object shippingInfo;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;

    @JsonProperty("txnAmount")
    public TxnAmount getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(TxnAmount txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonProperty("goods")
    public List<Good> getGoods() {
        return goods;
    }

    @JsonProperty("goods")
    public Body setGoods(List<Good> goods) {
        this.goods = goods;
        return this;
    }

    @JsonProperty("shippingInfo")
    public Object getShippingInfo() {
        return shippingInfo;
    }

    @JsonProperty("shippingInfo")
    public Body setShippingInfo(Object shippingInfo) {
        this.shippingInfo = shippingInfo;
        return this;
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

}