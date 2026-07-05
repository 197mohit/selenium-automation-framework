package com.paytm.api.PreAuthCapture;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.PreAuthCapture.PreAuthCaptureDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PreAuth extends BaseApi {

    private final PreAuthCaptureDTO.PsPreAuthRequestDTO request;

    public PreAuth() {
        this(PreAuthCaptureDTO.PsPreAuthRequestDTO.withDefaults());
    }

    public PreAuth(PreAuthCaptureDTO.PsPreAuthRequestDTO request) {
        this.request = request;
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PS_PRE_AUTH);
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        syncBody();
    }

    public PreAuth(String mid, String orderId, Boolean multiCaptureAllowed, String txnAmount) {
        this();
        setMid(mid);
        setOrderId(orderId);
        setMultiCaptureAllowed(multiCaptureAllowed);
        setTxnAmount(txnAmount);
    }

    public PreAuthCaptureDTO.PsPreAuthRequestDTO getRequestDto() {
        return request;
    }

    private void syncBody() {
        getRequestSpecBuilder().setBody(request);
    }

    public PreAuth setMid(String mid) {
        request.getBody().setMid(mid);
        syncBody();
        return this;
    }

    public PreAuth setOrderId(String orderId) {
        request.getBody().setOrderId(orderId);
        syncBody();
        return this;
    }

    public PreAuth setTxnAmount(String txnAmount) {
        if (txnAmount != null && !txnAmount.isEmpty()) {
            request.getBody().setTxnAmount(Integer.parseInt(txnAmount));
        }
        syncBody();
        return this;
    }

    public PreAuth setTxnAmount(Integer txnAmount) {
        request.getBody().setTxnAmount(txnAmount);
        syncBody();
        return this;
    }

    public PreAuth setPaytmSsoToken(String paytmSsoToken) {
        request.getBody().setPaytmSsoToken(paytmSsoToken);
        syncBody();
        return this;
    }

    public PreAuth setPreAuthBlockSeconds(String preAuthBlockSeconds) {
        if (preAuthBlockSeconds != null && !preAuthBlockSeconds.isEmpty()) {
            request.getBody().setPreAuthBlockSeconds(Integer.parseInt(preAuthBlockSeconds));
        }
        syncBody();
        return this;
    }

    public PreAuth setPreAuthBlockSeconds(Integer preAuthBlockSeconds) {
        request.getBody().setPreAuthBlockSeconds(preAuthBlockSeconds);
        syncBody();
        return this;
    }

    public PreAuth setCardPreAuthType(String cardPreAuthType) {
        request.getBody().setCardPreAuthType(cardPreAuthType);
        syncBody();
        return this;
    }

    public PreAuth setPaymentMode(String paymentMode) {
        request.getBody().setPaymentMode(paymentMode);
        syncBody();
        return this;
    }

    public PreAuth setMultiCaptureAllowed(boolean multiCaptureAllowed) {
        request.getBody().setMultiCaptureAllowed(multiCaptureAllowed);
        syncBody();
        return this;
    }

    public PreAuth setWebsiteName(String websiteName) {
        request.getBody().setWebsiteName(websiteName);
        syncBody();
        return this;
    }

    public PreAuth setTxnTokenRequired(boolean txnTokenRequired) {
        request.getBody().setTxnTokenRequired(txnTokenRequired);
        syncBody();
        return this;
    }

    public PreAuth setCallbackUrl(String callbackUrl) {
        request.getBody().setCallbackUrl(callbackUrl);
        syncBody();
        return this;
    }

    public PreAuth setPeonUrl(String peonUrl) {
        request.getBody().setPeonUrl(peonUrl);
        syncBody();
        return this;
    }

    public PreAuth setCustId(String custId) {
        if (request.getBody().getUserInfo() == null) {
            request.getBody().setUserInfo(new PreAuthCaptureDTO.PsPreAuthUserInfoDTO());
        }
        request.getBody().getUserInfo().setCustId(custId);
        syncBody();
        return this;
    }
}
