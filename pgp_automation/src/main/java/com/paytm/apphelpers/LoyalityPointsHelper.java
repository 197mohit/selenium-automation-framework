package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

public class LoyalityPointsHelper {

	private static Map<String, Object> getFrom_ALIPAY_USER(User user) {
		String dbQuery = "select * from alipay_paytm_user where paytm_id='" + user.custId() + "'";
		List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
		if (resultList.isEmpty())
			Assertions.fail("No result found in DB for Query: " + dbQuery);
		return resultList.get(0);
	}

	public static void updateBalance(User user,int loyalityPoints){
		BaseApi baseApi = new BaseApiV2();
		baseApi.setMethod(BaseApi.MethodType.POST);
		baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
		baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.ALIPAY);
		baseApi.getRequestSpecBuilder().setBasePath("/alipayplus/fund/user/loyaltypoints/reward.htm");

		Map<String, Object> dbResult = getFrom_ALIPAY_USER(user);
		String alipayId = dbResult.get("alipay_id").toString();

		baseApi.getRequestSpecBuilder().setBody("{\n" +
				"  \"request\": {\n" +
				"    \"head\": {\n" +
				"      \"clientId\": \"2016030715243903536806\",\n" +
				"      \"function\": \"alipayplus.fund.user.loyaltypoints.reward\",\n" +
				"      \"reserve\": {\n" +
				"        \"source\": \"end2end\"\n" +
				"      },\n" +
				"      \"clientSecret\": \"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\",\n" +
				"      \"reqTime\": \"2001-07-04T12:08:56+05:30\",\n" +
				"      \"accessToken\": \"234567a\",\n" +
				"      \"version\": \"fixed-a\",\n" +
				"      \"reqMsgId\": \"04850cf2-7ead-4fab-ba3b-a5b2e6f1f4b2\"\n" +
				"    },\n" +
				"    \"body\": {\n" +
				"      \"notificationUrl\": \"http://jarvis.alipay.net/test_tool/spi/mockSpiResponse\",\n" +
				"      \"fundType\": \"LP_REWARD\",\n" +
				"      \"fundAmount\": {\n" +
				"        \"currency\": \"INR\",\n" +
				"        \"value\": \""+loyalityPoints+"\"\n" +
				"      },\n" +
				"      \"requestId\": \""+CommonHelpers.getRandomWithSize(15)+"}}\",\n" +
				"      \"envInfo\": {\n" +
				"        \"appVersion\": \"1.0\",\n" +
				"        \"tokenId\": \"a8d359d6-ca3d-4048-9295-bbea5f6715a09\",\n" +
				"        \"websiteLanguage\": \"en_US\",\n" +
				"        \"sessionId\": \"8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter\",\n" +
				"        \"orderOsType\": \"IOS\",\n" +
				"        \"terminalType\": \"APP\",\n" +
				"        \"merchantAppVersion\": \"1.0\",\n" +
				"        \"orderTerminalType\": \"APP\",\n" +
				"        \"clientKey\": \"e5806b64-598d-414f-b7f7-83f9576eb6fb\",\n" +
				"        \"clientIp\": \"10.15.8.189\",\n" +
				"        \"osType\": \"Windows.PC\",\n" +
				"        \"sdkVersion\": \"1.0\"\n" +
				"      },\n" +
				"     \n" +
				"      \"userId\": \""+alipayId+"\",\n" +
				"      \"points\": \""+loyalityPoints+"\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"signature\": \"signature string\"\n" +
				"}");
		JsonPath json = baseApi.execute().jsonPath();
		Assertions.assertThat(json.getString("response.body.resultInfo.resultCode")).as("Update balance API failed").isEqualTo("ACCEPTED_SUCCESS");

	}

}
