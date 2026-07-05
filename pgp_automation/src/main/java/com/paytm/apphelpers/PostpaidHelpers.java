package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.api.FetchBalance;
import com.paytm.api.Postpaid;
import com.paytm.base.test.User;
import com.paytm.utils.merchant.dto.auth.Attributes;
import com.paytm.utils.merchant.dto.auth.UserAttributeRequestDTO;
import com.paytm.utils.merchant.util.AuthUtil;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

public class PostpaidHelpers {

	public final static String WHITELISTED = "WHITELISTED";
	public final static String LIVE = "LIVE";

	public static void updatePostpaidUserAttributes(User user, String attributeType) {
		removeUserAttributes(user);
		addPostpaidAttribute(user, attributeType);
	}

	private static boolean removeUserAttributes(User user) {
		UserAttributeRequestDTO userAttributeRequestDTO = new UserAttributeRequestDTO();
		userAttributeRequestDTO.setAction("REMOVE");
		userAttributeRequestDTO.setUserType("POSTPAID_USER");
		Response response = AuthUtil.setUserAttribute(LocalConfig.AUTH_HOST, user.custId(), userAttributeRequestDTO);
		JsonPath jsonPath = response.jsonPath();
		if(!jsonPath.getString("status").equalsIgnoreCase("SUCCESS")) {

			if(jsonPath.getString("responseCode").equalsIgnoreCase("1909"))
				return true;

			throw new AuthException("User Attribute is not updated");
		}
		return true;
	}

	private static boolean addPostpaidAttribute(User user, String attributeType) {
		UserAttributeRequestDTO userAttributeRequestDTO = new UserAttributeRequestDTO();
		userAttributeRequestDTO.setAction("ADD");
		userAttributeRequestDTO.setUserType("POSTPAID_USER");
		Attributes attributes = new Attributes();
		attributes.setPOSTPAIDSTATUS(attributeType);
		userAttributeRequestDTO.setAttributes(attributes);
		Response response = AuthUtil.setUserAttribute(LocalConfig.AUTH_HOST, user.custId(), userAttributeRequestDTO);
		JsonPath jsonPath = response.jsonPath();
		if(!jsonPath.getString("status").equalsIgnoreCase("SUCCESS")) {

			if(jsonPath.getString("responseCode").equalsIgnoreCase("1908"))
				return true;

			throw new AuthException("User Attribute is not updated");
		}
		return true;
	}

	public static void updateBalance(String balance) {
		
		Postpaid postpaid = new Postpaid(balance);
		Response response = postpaid.execute();
		Assertions.assertThat(response.getStatusCode()).as("Status Code").isEqualTo(200);
	}

	public static double getBalance(String mid, String orderId, String txnToken){
		FetchBalance fetchBalance = new FetchBalance(mid, orderId, null, "PAYTM_DIGITAL_CREDIT");
		fetchBalance.getRequestSpecBuilder().setBody("{ \"head\":{ \"version\":\"v1\", \"requestTimestamp\":\"Time\", \"channelId\":\"WEB\", \"txnToken\":\""+txnToken+"\" }, \"body\":{ \"paymentMode\":\"PAYTM_DIGITAL_CREDIT\" } }");

		Response response = fetchBalance.execute();
		Assertions.assertThat(response.statusCode()).isEqualTo(200);
		return response.jsonPath().getDouble("body.balanceInfo.value");
	}

}
