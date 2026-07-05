package com.paytm.api.PgPlusBO;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;

import java.io.File;
import java.util.TreeMap;

public class UploadProof extends BaseApi {
    private Constants.MerchantType merchantType;
    private String disputeId, requestTimestamp, orderStatus, action;

    public UploadProof(Constants.MerchantType merchantType, String disputeId,
                       String requestTimestamp, String orderStatus, String action) {
        this.merchantType = merchantType;
        this.disputeId = disputeId;
        this.requestTimestamp = requestTimestamp;
        this.orderStatus = orderStatus;
        this.action = action;
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PgPlusBo.UPLOAD_PROOF);
    }

    public UploadProof addMultipartData(String filePath, int numberFilesUploaded) {
        getRequestSpecBuilder().addMultiPart("mid", this.merchantType.getId());
        getRequestSpecBuilder().addMultiPart("disputeId", this.disputeId);
        getRequestSpecBuilder().addMultiPart("requestTimestamp", this.requestTimestamp);
        getRequestSpecBuilder().addMultiPart("orderStatus", this.orderStatus);
        getRequestSpecBuilder().addMultiPart("action", this.action);
        getRequestSpecBuilder().addMultiPart("signature", generateSignature());
        int numberFiles = 0;
        while (numberFiles < numberFilesUploaded) {
            File file = new File(filePath);
            getRequestSpecBuilder().addMultiPart("files", file);
            numberFiles++;
        }
        return this;
    }

    private String generateSignature() {
        TreeMap<String, String> t = new TreeMap<>();
        t.put("mid", this.merchantType.getId());
        t.put("disputeId", this.disputeId);
        t.put("requestTimestamp", this.requestTimestamp);
        t.put("orderStatus", this.orderStatus);
        t.put("action", this.action);
        String checksum = PGPUtil.getChecksum(this.merchantType.getKey(), t);
        return checksum;
    }
}