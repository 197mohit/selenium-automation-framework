package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class FetchBalance extends BaseApi{

	Response response;

	public FetchBalance(String mid, String orderId, String ssoToken,String payMode){
		setMethod(BaseApi.MethodType.POST);
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
		getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
		getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BALANCE);
		getRequestSpecBuilder().addQueryParam("mid", mid);
		getRequestSpecBuilder().addQueryParam("orderId", orderId);
		getRequestSpecBuilder().setBody("{ \"head\":{ \"version\":\"v1\", \"requestTimestamp\":\"Time\", \"channelId\":\"WEB\",\"tokenType\":\"SSO\", \"token\":\""+ssoToken+"\" }, \"body\":{ \"paymentMode\":\""+payMode+"\",\"mid\":\""+mid+"\" } }");

	}
	public FetchBalance(String mid, String orderId, String txnToken,String payMode,String arg){
		setMethod(BaseApi.MethodType.POST);
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
		getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BALANCE);
		getRequestSpecBuilder().addQueryParam("mid", mid);
		getRequestSpecBuilder().addQueryParam("orderId", orderId);
		getRequestSpecBuilder().setBody("{ \"head\":{ \"version\":\"v1\", \"requestTimestamp\":\"Time\", \"channelId\":\"WEB\", \"txnToken\":\""+txnToken+"\" }, \"body\":{ \"paymentMode\":\""+payMode+"\" } }");

	}

	public FetchBalance() {

	}

	public FetchBalance qrcodeidwithqueryparam(String qrCodeId, String orderId, String ssoToken, String payMode){
		setMethod(BaseApi.MethodType.POST);
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
		getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BALANCE);
		getRequestSpecBuilder().addQueryParam("orderId", orderId);
		getRequestSpecBuilder().setBody("{ \"head\":{ \"version\":\"v1\", \"requestTimestamp\":\"Time\", \"channelId\":\"WEB\",\"tokenType\":\"SSO\", \"token\":\""+ssoToken+"\" }, \"body\":{ \"paymentMode\":\""+payMode+"\",\"qrCodeId\":\""+qrCodeId+"\" } }");

		return this;
	}

	public FetchBalance qrcodeidwithoutqueryparam(String qrCodeId, String orderId, String ssoToken, String payMode){
		setMethod(BaseApi.MethodType.POST);
		getRequestSpecBuilder().setContentType(ContentType.JSON);
		getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
		getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BALANCE);
		getRequestSpecBuilder().setBody("{ \"head\":{ \"version\":\"v1\", \"requestTimestamp\":\"Time\", \"channelId\":\"WEB\",\"tokenType\":\"SSO\", \"token\":\""+ssoToken+"\" }, \"body\":{ \"paymentMode\":\""+payMode+"\",\"qrCodeId\":\""+qrCodeId+"\" } }");

		return this;
	}
}
