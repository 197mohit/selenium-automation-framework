package com.paytm.utils.merchant.dto.mappingService.response;

/**
 * Created by ankuragarwal on 27/9/18
 */
public class BankUrlInfoDTO {
    private Long id;
    private Long bankId;
    private Long payMethodId;
    private Long channelId;
    private String webPayUrl;
    private String s2sPayUrl;
    private String statusQueryUrl;
    private String refundUrl;
    private String refundStatusUrl;
    private String webResponseUrl;
    private String urlType;
    private String url;

    public Long getId() {
        return id;
    }

    public Long getBankId() {
        return bankId;
    }

    public Long getPayMethodId() {
        return payMethodId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public String getWebPayUrl() {
        return webPayUrl;
    }

    public String getS2sPayUrl() {
        return s2sPayUrl;
    }

    public String getStatusQueryUrl() {
        return statusQueryUrl;
    }

    public String getRefundUrl() {
        return refundUrl;
    }

    public String getRefundStatusUrl() {
        return refundStatusUrl;
    }

    public String getWebResponseUrl() {
        return webResponseUrl;
    }

    public String getUrlType() {
        return urlType;
    }

    public String getUrl() {
        return url;
    }

    public BankUrlInfoDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public BankUrlInfoDTO setBankId(Long bankId) {
        this.bankId = bankId;
        return this;
    }

    public BankUrlInfoDTO setPayMethodId(Long payMethodId) {
        this.payMethodId = payMethodId;
        return this;
    }

    public BankUrlInfoDTO setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }

    public BankUrlInfoDTO setWebPayUrl(String webPayUrl) {
        this.webPayUrl = webPayUrl;
        return this;
    }

    public BankUrlInfoDTO setS2sPayUrl(String s2sPayUrl) {
        this.s2sPayUrl = s2sPayUrl;
        return this;
    }

    public BankUrlInfoDTO setStatusQueryUrl(String statusQueryUrl) {
        this.statusQueryUrl = statusQueryUrl;
        return this;
    }

    public BankUrlInfoDTO setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
        return this;
    }

    public BankUrlInfoDTO setRefundStatusUrl(String refundStatusUrl) {
        this.refundStatusUrl = refundStatusUrl;
        return this;
    }

    public BankUrlInfoDTO setWebResponseUrl(String webResponseUrl) {
        this.webResponseUrl = webResponseUrl;
        return this;
    }

    public BankUrlInfoDTO setUrlType(String urlType) {
        this.urlType = urlType;
        return  this;
    }

    public BankUrlInfoDTO setUrl(String url) {
        this.url = url;
        return this;
    }
}
