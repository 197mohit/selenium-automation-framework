package com.paytm.apphelpers;


import com.paytm.appconstants.Constants;
import com.paytm.utils.merchant.api.pgp.refund.RefundStatusV1;
import com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response.Body;
import com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response.RefundDetailInfoList;
import com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response.RefundStatusV1Response;
import com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response.RetryInfo;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;

public class RefundStatusV1Helper {

    private static final String REFUND_SUCESS_MSG = "Refund Successfull";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";
    private static final String SUCCESS = "SUCCESS";
    private static final String TXN_FAILURE = "TXN_FAILURE";
    private static final String RESP_CODE_SUCCESS = "10";
    private RefundStatusV1Response refundStatusV1Response;
    private Body body;
    private SoftAssertions validateSoftly = new SoftAssertions();
    private List<Map<String, Object>> list;

    public RefundStatusV1Helper(String mid, String orderId, String refId, String merchantKey, boolean untilPending) throws PGPException {
        RefundStatusV1 refundStatusV1_api = new RefundStatusV1(mid, orderId, refId, merchantKey);
        Response response;
        if (!untilPending) {
            response = refundStatusV1_api.execute();
            if (response.statusCode() != 200)
                throw new PGPException("Exception in RefundStatusV1, status code: " + response.statusCode());
            else {
                try {
                    refundStatusV1Response = response.as(RefundStatusV1Response.class);
                } catch (Exception ex) {
                    if (ex.getMessage().contains("Unrecognized field"))
                        Assertions.fail("Change is RefundStatusV1Response DTO", ex);
                }
            }

            body = refundStatusV1Response.getBody();
        } else {
            int timeOutInSeconds = 200;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < (long) (timeOutInSeconds * 1000)) {
                response = refundStatusV1_api.execute();
                if (response.statusCode() != 200)
                    throw new PGPException("Exception in RefundStatusV1, status code: " + response.statusCode());
                else {
                    try {
                        refundStatusV1Response = response.as(RefundStatusV1Response.class);
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("Unrecognized field"))
                            Assertions.fail("Change is RefundStatusV1Response DTO", ex);
                    }
                }
                if (refundStatusV1Response.getBody().getAcceptRefundStatus() == null) {
                    continue;
                } else if (!refundStatusV1Response.getBody().getAcceptRefundStatus().equals("PENDING") &&
                        !refundStatusV1Response.getBody().getUserCreditInitiateStatus().equals("PENDING")) {
                    body = refundStatusV1Response.getBody();
                    break;
                } else {
                    body = refundStatusV1Response.getBody();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException var14) {
                        var14.printStackTrace();
                    }
                }
            }
        }
    }



    public RefundStatusV1Helper(String mid, String orderId, String refId, String tokenType,String token, boolean untilPending) throws PGPException {
        RefundStatusV1 refundStatusV1 = new RefundStatusV1(mid,orderId,refId,tokenType,token);

        Response response;
        if (!untilPending) {
            response = refundStatusV1.execute();
            if (response.statusCode() != 200)
                throw new PGPException("Exception in RefundStatusV1, status code: " + response.statusCode());
            else {
                try {
                    refundStatusV1Response = response.as(RefundStatusV1Response.class);
                } catch (Exception ex) {
                    if (ex.getMessage().contains("Unrecognized field"))
                        Assertions.fail("Change is RefundStatusV1Response DTO", ex);
                }
            }

            body = refundStatusV1Response.getBody();
        } else {
            int timeOutInSeconds = 200;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < (long) (timeOutInSeconds * 1000)) {
                response = refundStatusV1.execute();
                if (response.statusCode() != 200)
                    throw new PGPException("Exception in RefundStatusV1, status code: " + response.statusCode());
                else {
                    try {
                        refundStatusV1Response = response.as(RefundStatusV1Response.class);
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("Unrecognized field"))
                            Assertions.fail("Change is RefundStatusV1Response DTO", ex);
                    }
                }
                if (refundStatusV1Response.getBody().getAcceptRefundStatus() == null) {
                    continue;
                } else if (!refundStatusV1Response.getBody().getAcceptRefundStatus().equals("PENDING") &&
                        !refundStatusV1Response.getBody().getUserCreditInitiateStatus().equals("PENDING")) {
                    body = refundStatusV1Response.getBody();
                    break;
                } else {
                    body = refundStatusV1Response.getBody();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException var14) {
                        var14.printStackTrace();
                    }
                }
            }
        }
    }



    public RefundStatusV1Helper(Constants.MerchantType merchantType, String orderId,
                                String refId, boolean untilPending) throws PGPException {
        this(merchantType.getId(), orderId, refId, merchantType.getKey(), untilPending);
    }

    public void asserAll() {
        validateSoftly.assertAll();
    }

    @Step
    public RefundStatusV1Helper validateSuccessRefund() {
        validateResultStatus(TXN_SUCCESS);
        validateResultMsg(REFUND_SUCESS_MSG);
        validateResultCode(RESP_CODE_SUCCESS);
        validateAcceptRefundStatus(SUCCESS);
        validateUserCreditInitiateStatus(SUCCESS);
        return this;
    }

    public RefundStatusV1Helper validateAgentInfo()
    {
        validateSoftly.assertThat(body.getAgentInfo().getName())
                .as("Agent Info Name mismatch")
                .isNotEmpty().isNotNull();

        validateSoftly.assertThat(body.getAgentInfo().getEmployeeId())
                .as("Agent Info Employee ID mismatch")
                .isNotEmpty().isNotNull();

        validateSoftly.assertThat(body.getAgentInfo().getPhoneNo())
                .as("Agent Info PhoneNo mismatch")
                .isNotEmpty().isNotNull();

        validateSoftly.assertThat(body.getAgentInfo().getEmail())
                .as("Agent Info Email mismatch")
                .isNotEmpty().isNotNull();

        return this;
    }

    public RefundStatusV1Helper validateMaskedVPA()
    {
        validateSoftly.assertThat(body.getMaskedVpa())
                .as("Masked VPA mismatch")
                .isNotEmpty().isNotNull();
        return this;
    }

    @Step
    public RefundStatusV1Helper validateResultStatus(String expected) {
        validateSoftly.assertThat(body.getResultInfo().getResultStatus())
                .as("ResultStatus mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateResultMsg(String expected) {
        validateSoftly.assertThat(body.getResultInfo().getResultMsg())
                .as("ResultMsg mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateResultCode(String expected) {
        validateSoftly.assertThat(body.getResultInfo().getResultCode())
                .as("ResultCode mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateTxnId(String expected) {
        validateSoftly.assertThat(body.getTxnId())
                .as("txnId mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateOrderId(String expected) {
        validateSoftly.assertThat(body.getOrderId())
                .as("orderId mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRetryFlag(String expected) {
        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryFlag())
                .as("retryFlag field not found")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateTxnAmount(String expected) {
        validateSoftly.assertThat(body.getTxnAmount())
                .as("txnAmount mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validatRefundTypeRetryInfo(String expected) {
        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryInfo().getRefundType())
                .as("Retry Info Refund Type mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateMid(String expected) {
        validateSoftly.assertThat(body.getMid())
                .as("Mid mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateTxnDate(String expected) {
        validateSoftly.assertThat(body.getTxnDate())
                .as("txnDate mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validatePayMethodRetryInfo(String expected) {
        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryInfo().getPayMethod())
                .as("Pay Method Mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    public RefundStatusV1Helper retryInfoissuingBankName(String expected) {
        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryInfo().getIssuingBankName())
                .as("IssuingBankName mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper retryInfoCardScheme(String expected) {
        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryInfo().getCardScheme())
                .as("CardScheme mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateUserCreditExpectedDate() {

        validateSoftly.assertThat(body.getRefundDetailInfoList().get(0).getRetryInfo().getUserCreditExpectedDate())
                .as("UserCreditExpectedDate not null")
                .isNotNull();
        return this;
    }

    @Step
    public RefundStatusV1Helper validateTotalRefundAmount(String expected) {
        validateSoftly.assertThat(body.getTotalRefundAmount())
                .as("TotalRefundAmount mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateTotalRefundAmount(Double expected) {
        validateSoftly.assertThat(Double.valueOf(body.getTotalRefundAmount()))
                .as("TotalRefundAmount mismatch")
                .isEqualTo(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateMerchantRefundRequestDate(String expected) {
        validateSoftly.assertThat(body.getMerchantRefundRequestDate())
                .as("merchantRefundRequestDate mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateAcceptRefundTimestamp(String expected) {
        validateSoftly.assertThat(body.getAcceptRefundTimestamp())
                .as("acceptRefundTimestamp mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateUserCreditInitiateTimestamp(String expected) {
        validateSoftly.assertThat(body.getUserCreditInitiateTimestamp())
                .as("userCreditInitiateTimestamp mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateAcceptRefundStatus(String expected) {
        validateSoftly.assertThat(body.getAcceptRefundStatus())
                .as("acceptRefundStatus mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateUserCreditInitiateStatus(String expected) {
        validateSoftly.assertThat(body.getUserCreditInitiateStatus())
                .as("userCreditInitiateStatus mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRefId(String expected) {
        validateSoftly.assertThat(body.getRefId())
                .as("refid mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRefundAmount(String expected) {
        validateSoftly.assertThat(body.getRefundAmount())
                .as("refundAmount mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRefundAmount(Double expected) {
        validateSoftly.assertThat(Double.valueOf(body.getRefundAmount()))
                .as("refundAmount mismatch")
                .isEqualTo(expected);
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRefundDetailInfoList(PAY_METHODS payMethod, Double refundAmount, String bankName) {
        List<RefundDetailInfoList> refundDetailInfoLists = getRefundDetailInfoLists();
        for (RefundDetailInfoList refundDetailInfoList : refundDetailInfoLists) {
            if (refundDetailInfoList.getPayMethod().equalsIgnoreCase(payMethod.toString())) {
                validateSoftly.assertThat(Double.valueOf(refundDetailInfoList.getRefundAmount()))
                        .as("Refund Amount mismatch in refundDetailInfoList")
                        .isEqualTo(refundAmount);
                /*if (null == bankName) {
                } else if (bankName.isEmpty()) {
                    validateSoftly.assertThat(refundDetailInfoList.getIssuingBankName())
                            .as("issuingBankName is empty")
                            .isNotEmpty();
                } else
                    validateSoftly.assertThat(refundDetailInfoList.getIssuingBankName())
                            .as("Bank name not available in refundDetailInfoList")
                            .containsIgnoringCase(bankName); */
                validateSoftly.assertThat(refundDetailInfoList.getUserCreditExpectedDate())
                        .as("userCreditExpectedDate is empty")
                        .isNotEmpty();
                validateSoftly.assertThat(refundDetailInfoList.getRefundType())
                        .as("Refund type is empty")
                        .isNotEmpty();
                return this;
            }
        }
        validateSoftly.fail(payMethod.toString() + " not found in refundDetailInfoList " + body.toString());
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRefundDetailInfoList(PAY_METHODS payMethod, String refundAmount, String bankName) {
        List<RefundDetailInfoList> refundDetailInfoLists = getRefundDetailInfoLists();
        for (RefundDetailInfoList refundDetailInfoList : refundDetailInfoLists) {
            if (refundDetailInfoList.getPayMethod().equalsIgnoreCase(payMethod.toString())) {
                validateSoftly.assertThat(refundDetailInfoList.getRefundAmount())
                        .as("Refund Amount mismatch in refundDetailInfoList")
                        .isEqualToIgnoringCase(refundAmount);
                /*if (null == bankName) {
                } else if (bankName.isEmpty()) {
                    validateSoftly.assertThat(refundDetailInfoList.getIssuingBankName())
                            .as("issuingBankName is empty")
                            .isNotEmpty();
                } else
                    validateSoftly.assertThat(refundDetailInfoList.getIssuingBankName())
                            .as("Bank name not available in refundDetailInfoList")
                            .containsIgnoringCase(bankName);*/
                validateSoftly.assertThat(refundDetailInfoList.getUserCreditExpectedDate())
                        .as("userCreditExpectedDate is empty")
                        .isNotEmpty();
                validateSoftly.assertThat(refundDetailInfoList.getRefundType())
                        .as("Refund type is empty")
                        .isNotEmpty();
                return this;
            }
        }
        validateSoftly.fail(payMethod.toString() + " not found in refundDetailInfoList " + body.toString());
        return this;
    }

    @Step
    public RefundStatusV1Helper validateRetryInfo(String refundType,String bankName,PAY_METHODS payMethod,String cardScheme,String refundAmount) {
        List<RefundDetailInfoList> refundDetailInfoLists = getRefundDetailInfoLists();
        for (RefundDetailInfoList refundDetailInfoList : refundDetailInfoLists) {
            RetryInfo retryInfo = refundDetailInfoList.getRetryInfo();
            if (retryInfo != null) {
                validateSoftly.assertThat(retryInfo.getRefundType())
                        .as("RefundType mismatch")
                        .isEqualToIgnoringCase(refundType);
                validateSoftly.assertThat(retryInfo.getRetryUserCreditInitiateTimestamp())
                        .as("RetryUserCreditInitiateTimestamp not available")
                        .isNotEmpty();
                validateSoftly.assertThat(retryInfo.getIssuingBankName())
                        .as(" issuingBankName mismatch")
                        .isEqualToIgnoringCase(bankName);
                validateSoftly.assertThat(retryInfo.getPayMethod())
                        .as("paymethod mismatch")
                        .isEqualToIgnoringCase(payMethod.toString());
                validateSoftly.assertThat(retryInfo.getUserCreditExpectedDate())
                        .as("userCreditExpectedDate not available")
                        .isNotEmpty();
                validateSoftly.assertThat(retryInfo.getMaskedCardNumber())
                        .as("MaskedCardNumber not available")
                        .isNotEmpty();
                validateSoftly.assertThat(refundDetailInfoList.getRetryFlag())
                        .as("retry Flag not available")
                        .isNotEmpty();
                validateSoftly.assertThat(refundDetailInfoList.getRefundAmount())
                        .as("Refund Amount mismatch")
                        .isEqualToIgnoringCase(refundAmount);
                validateSoftly.assertThat(retryInfo.getCardScheme())
                        .as("CardScheme mismatch")
                        .isEqualToIgnoringCase(cardScheme);
            } else {
                throw new PGPException("Refund Failure");
            }
        }
        validateSoftly.fail( "refundDetailInfoList not found" + body.toString());
        return this;
    }

    public List<RefundDetailInfoList> getRefundDetailInfoLists() {
        return body.getRefundDetailInfoList();
    }

    public enum PAY_METHODS {
        CREDIT_CARD,
        DEBIT_CARD,
        WALLET,
        PPBL,
        BALANCE

    }


}
