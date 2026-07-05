package com.paytm.utils.merchant.api.pgp.refund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.request.RefundStatusV1Req;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.http.ContentType;

public class RefundStatusV1 extends BaseApi {


    /**
     *
     * @param mid
     * @param orderId
     * @param refId
     * @param merchantKey
     * @throws PGPException
     */
    public RefundStatusV1(String mid, String orderId, String refId, String merchantKey) throws PGPException{
        RefundStatusV1Req refundStatusV1Req = new RefundStatusV1Req(mid, orderId, refId);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString, checksum = null;
        try {
            jsonString = mapper.writeValueAsString(refundStatusV1Req.getBody());
            checksum = PGPUtil.getChecksum(merchantKey, jsonString);
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while creating checksum", e);
        }
        refundStatusV1Req.getHead().setSignature(checksum);
        refundStatusV1Req.getHead().setClientId("");
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/refund/api/v1/refundStatus");
        getRequestSpecBuilder().setBody(refundStatusV1Req);
    }

    public RefundStatusV1(String mid, String orderId, String refId, String tokenType, String token) throws PGPException{
        RefundStatusV1Req refundStatusV1Req = new RefundStatusV1Req(mid, orderId, refId,tokenType,token);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString=null ;
        try {
            jsonString = mapper.writeValueAsString(refundStatusV1Req.getBody());
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while creating checksum", e);
        }

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/refund/api/v1/refundStatus");
        getRequestSpecBuilder().setBody(refundStatusV1Req);
    }


    public BaseApi getApi(){
        return this;
    }

}
