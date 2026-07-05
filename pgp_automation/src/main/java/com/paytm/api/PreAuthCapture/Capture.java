package com.paytm.api.PreAuthCapture;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.PreAuthCapture.PreAuthCaptureDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class Capture extends BaseApi {

    private final PreAuthCaptureDTO.PsCaptureRequestDTO request;

    public Capture() {
        this(PreAuthCaptureDTO.PsCaptureRequestDTO.withDefaults());
    }

    public Capture(PreAuthCaptureDTO.PsCaptureRequestDTO request) {
        this.request = request;
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PS_CAPTURE);
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        syncBody();
    }

    public PreAuthCaptureDTO.PsCaptureRequestDTO getRequestDto() {
        return request;
    }

    private void syncBody() {
        getRequestSpecBuilder().setBody(request);
    }

    public Capture setMid(String mid) {
        request.getBody().setMid(mid);
        syncBody();
        return this;
    }

    public Capture setPreAuthId(String preAuthId) {
        request.getBody().setPreAuthId(preAuthId);
        syncBody();
        return this;
    }

    public Capture setTxnAmount(String txnAmount) {
        request.getBody().setTxnAmount(txnAmount);
        syncBody();
        return this;
    }

    public Capture setOrderId(String orderId) {
        request.getBody().setOrderId(orderId);
        syncBody();
        return this;
    }

    public Capture setPayMode(String payMode) {
        request.getBody().setPayMode(payMode);
        syncBody();
        return this;
    }

    public Capture setTerminalCapture(String terminalCapture) {
        request.getBody().setTerminalCapture(terminalCapture);
        syncBody();
        return this;
    }

    public Capture setMercUnqRef(String mercUnqRef) {
        request.getBody().setMercUnqRef(mercUnqRef);
        syncBody();
        return this;
    }

    public Capture setPlanId(String planId) {
        request.getBody().setPlanId(planId);
        syncBody();
        return this;
    }

    public Capture setSignature(String signature) {
        request.getHead().setSignature(signature);
        syncBody();
        return this;
    }

    public Capture setClientId(String clientId) {
        request.getHead().setClientId(clientId);
        syncBody();
        return this;
    }
}
