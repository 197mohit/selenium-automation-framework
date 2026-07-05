package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 23/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetail {
    private String bin;
    private String channelCode;
    private String channelName;
    private String cnMax;
    private String cnMin;
    private String cvvL;
    private String cvvR;
    private String expR;
    private String isActive;
    private String isIndian;
    private String issuingBank;
    private String issuingBankCode;
    private String paymentMode;
    private String binTokenization;

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getCnMax() {
        return cnMax;
    }

    public void setCnMax(String cnMax) {
        this.cnMax = cnMax;
    }

    public String getCnMin() {
        return cnMin;
    }

    public void setCnMin(String cnMin) {
        this.cnMin = cnMin;
    }

    public String getCvvL() {
        return cvvL;
    }

    public void setCvvL(String cvvL) {
        this.cvvL = cvvL;
    }

    public String getCvvR() {
        return cvvR;
    }

    public void setCvvR(String cvvR) {
        this.cvvR = cvvR;
    }

    public String getExpR() {
        return expR;
    }

    public void setExpR(String expR) {
        this.expR = expR;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsIndian() {
        return isIndian;
    }

    public void setIsIndian(String isIndian) {
        this.isIndian = isIndian;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public String getIssuingBankCode() {
        return issuingBankCode;
    }

    public void setIssuingBankCode(String issuingBankCode) {
        this.issuingBankCode = issuingBankCode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBinTokenization() {
        return binTokenization;
    }

    public void setBinTokenization(String binTokenization) {
        this.binTokenization = binTokenization;
    }
}
