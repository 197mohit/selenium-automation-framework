package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MbIdLimitEmiPlan extends BaseApi{
	String request = "{\"channelId\":\"null\",\"status\":\"true\", \"channelType\":\"WAP\" }";

	public String getRequest()
	{return request;}

	public MbIdLimitEmiPlan(String mid) {
		setMethod(MethodType.POST);
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
		getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
		getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.MBIDLIMIT_EMIPLAN);
		getRequestSpecBuilder().setBody(getRequest());
		this.setContext("mid",mid);
	}
}
