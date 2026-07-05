package com.paytm.api.VisaSingleClick;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.EnrollmentStatusDTO.EnrollmentStatusDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;


public class EnrollmentStatus  extends BaseApi{

    public EnrollmentStatus(EnrollmentStatusDTO enrollmentStatusDTO, com.paytm.appconstants.Constants.MerchantType mid, String referenceId, String orderId){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/api/v1/card/enrollmentStatus");
        getRequestSpecBuilder().addQueryParam("mid", mid.getId());
        getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        if (orderId!="" || orderId!=null) {
            getRequestSpecBuilder().addQueryParam("orderId", orderId);
        }
        if(enrollmentStatusDTO.getHead().getTokenType().equalsIgnoreCase("CHECKSUM")) {
            String token = PGPHelpers.getNativeChecksum(mid.getKey(), enrollmentStatusDTO.getBody());
            System.out.println("Checksum=" + token);
            enrollmentStatusDTO.getHead().setToken(token);
        }
        getRequestSpecBuilder().setBody(enrollmentStatusDTO);
    }


    public  static   JsonPath checkEnrollmentStatus(EnrollmentStatusDTO enrollmentStatusDTO, com.paytm.appconstants.Constants.MerchantType mid, String referenceId, String orderId) {
        return new EnrollmentStatus(enrollmentStatusDTO, mid, referenceId, orderId).execute().jsonPath();
    }
}
