package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.dto.auth.UserAttributeRequestDTO;
import io.restassured.http.ContentType;

public class UserAttributes extends BaseApi {

    String basePath = "/{custId}/userType/attributes";

    public UserAttributes(String authBaseUri, String custId, String authorization, UserAttributeRequestDTO attributeRequestDTO) {
        basePath = basePath.replace("{custId}", custId);
        setMethod(BaseApi.MethodType.PUT);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath(basePath);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("authorization", authorization);
        getRequestSpecBuilder().setBody(attributeRequestDTO);
    }

}
