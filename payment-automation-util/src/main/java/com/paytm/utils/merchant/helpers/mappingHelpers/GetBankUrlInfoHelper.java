package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetBankUrlInfo;
import com.paytm.utils.merchant.dto.mappingService.response.BankUrlInfoDTO;

/**
 * Created by ankuragarwal on 28/9/18
 */
public class GetBankUrlInfoHelper {

    private ValidateSoftly validateSoftly;
    private BankUrlInfoDTO bankUrlInfoDTO;

    public GetBankUrlInfoHelper(String bankId, String payMethodId, String channelId) {
        validateSoftly = ValidateSoftly.getInstance();
        bankUrlInfoDTO = GetBankUrlInfo.executeBankUrlInfo(bankId, payMethodId, channelId);
    }

    public GetBankUrlInfoHelper validateId(Long id) {
        this.validateSoftly.validate(bankUrlInfoDTO.getId(),
                "", "Validated URL INFO id").isEqualTo(id);
        return this;
    }

    public GetBankUrlInfoHelper validateBankId(Long bankId) {
        this.validateSoftly.validate(bankUrlInfoDTO.getBankId(),
                "", "Validated bankId").isEqualTo(bankId);
        return this;
    }

    public GetBankUrlInfoHelper validatePayMethodId(Long payMethodId) {
        this.validateSoftly.validate(bankUrlInfoDTO.getPayMethodId(),
                "", "Validated payMethodId").isEqualTo(payMethodId);
        return this;
    }

    public GetBankUrlInfoHelper validateChannelId(Long channelId) {
        this.validateSoftly.validate(bankUrlInfoDTO.getChannelId(),
                "", "Validated channelId").isEqualTo(channelId);
        return this;
    }

    public GetBankUrlInfoHelper validateWebPayUrl(String webPayUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getWebPayUrl(),
                "", "Validated webPayUrl").isEqualTo(webPayUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateS2sPayUrl(String s2sPayUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getS2sPayUrl(),
                "", "Validated s2sPayUrl").isEqualTo(s2sPayUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateStatusQueryUrl(String statusQueryUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getStatusQueryUrl(),
                "", "Validated statusQueryUrl").isEqualTo(statusQueryUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateRefundUrl(String refundUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getRefundUrl(),
                "", "Validated refundUrl").isEqualTo(refundUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateRefundStatusUrl(String refundStatusUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getRefundStatusUrl(),
                "", "Validated refundStatusUrl").isEqualTo(refundStatusUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateWebResponseUrl(String webResponseUrl) {
        this.validateSoftly.validate(bankUrlInfoDTO.getWebResponseUrl(),
                "", "Validated webResponseUrl").isEqualTo(webResponseUrl);
        return this;
    }

    public GetBankUrlInfoHelper validateUrlType(String urlType) {
        this.validateSoftly.validate(bankUrlInfoDTO.getUrlType(),
                "", "Validated urlType").isEqualTo(urlType);
        return this;
    }

    public GetBankUrlInfoHelper validateUrl(String url) {
        this.validateSoftly.validate(bankUrlInfoDTO.getUrl(),
                "", "Validated url").isEqualTo(url);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }
}
