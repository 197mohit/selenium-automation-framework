package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class Postpaid extends BaseApi {

	public Postpaid(String balance) {
		setMethod(MethodType.POST);
		getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
		getRequestSpecBuilder().setBasePath("/mockbank/lms/postpaid/v4/user/setBalance/"+balance);
	}

}
