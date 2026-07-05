package scripts.OfferJS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.OfferWidgetJS.OfferWidgetDiscovery;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.configmanager.TestDataUtil;
import com.paytm.pages.OfferWidgetPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;

/**
 * @author satwiksharma
 * @Date : 10/06/2024
 */
public class OfferWidgetDiscoveryE2E extends PGPBaseTest {

    private Map<String, Object> config;

    @BeforeClass
    public void setup() throws Exception {
        config = TestDataUtil.loadData("src/main/resources/OfferJs/config.json", Map.class);
    }

    @Test(description = "Offer Discovery Widget Test")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-43535")
    public void testOfferDiscoveryWidgetE2E() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :"+(String)config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);

        offerWidget.clickButtonLoadJS();
        offerWidget.doConfiguration(json);

        offerWidget.waitUntilLoads();

        offerWidget.clickInitialiseOfferJS();
        offerWidget.clickInvokeOfferJS();
        offerWidget.clickViewAllOffers();
        offerWidget.assertAllPayModeArePresent();
    }

    @Test(description = "Offer Discovery Widget Test Compare Plans screen")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56319")
    public void testOfferDiscoveryWidget_ComparePlans() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :" + config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);

        offerWidget.invokeWidget(json);
        offerWidget.clickViewAllOffers();
        offerWidget.openComparePlans();
        offerWidget.assertComparePlansTable();
    }

    @Test(description = "Offer Discovery Widget Test - Widget Benefit Text Standard EMI with Bank Offers")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56319")
    public void testOfferDiscoveryWidget_WidgetBenefitText_StandardEMIWithBankOffers() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        data.replace("amount", "15708");

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :" + config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);
        offerWidget.invokeWidget(json);
        Assertions.assertThat(offerWidget.FirstBenefitText().getText()).matches("Standard EMI with Instant Discount starting at ₹4209.76 on HDFC Bank and selected cards");
        Assertions.assertThat(offerWidget.SecondBenefitText().getText()).matches("Get 20% Instant Discount up to ₹10000 on HDFC Bank Credit Card and other offers.");
        offerWidget.clickViewAllOffers();
        offerWidget.waitUntilLoads();
        offerWidget.payNoCostEmi().assertNotVisible();
    }
    @Test(description = "Offer Discovery Widget Test - Widget Benefit Text Standard EMI without Bank Offers")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56319")
    public void testOfferDiscoveryWidget_WidgetBenefitText_StandardEMIWithoutBankOffers() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        data.replace("amount", "15709");

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :" + config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);
        offerWidget.invokeWidget(json);
        Assertions.assertThat(offerWidget.FirstBenefitText().getText()).matches("EMI Available at ₹5,262.54. Available on 6 Banks");
        Assertions.assertThat(offerWidget.SecondBenefitText().getText()).matches("Get 20% Instant Discount up to ₹10000 on HDFC Bank Credit Card and other offers.");
        offerWidget.clickViewAllOffers();
        offerWidget.waitUntilLoads();
        offerWidget.payNoCostEmi().assertNotVisible();
    }

    @Test(description = "Offer Discovery Widget Test - Widget Benefit Text Subvention with Bank Offers")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56319")
    public void testOfferDiscoveryWidget_WidgetBenefitText_SubventionWithBankOffers() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        data.replace("amount", "15707");

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :" + config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);
        offerWidget.invokeWidget(json);
        Assertions.assertThat(offerWidget.FirstBenefitText().getText()).matches("No Cost EMI starting at ₹5,235.67 on HDFC Bank and selected cards");
        Assertions.assertThat(offerWidget.SecondBenefitText().getText()).matches("Get 20% Instant Discount up to ₹10000 on HDFC Bank Credit Card and other offers.");
        offerWidget.clickViewAllOffers();
        offerWidget.waitUntilLoads();
        offerWidget.payNoCostEmi().assertVisible();
    }

    @Test(description = "Offer Discovery Widget Test - User Detail not sent in Config")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56726")
    public void testOfferDiscoveryWidgetE2E_UserDetailNotSentInConfig() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        data.remove("userDetail");

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :"+config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);

        offerWidget.clickButtonLoadJS();
        offerWidget.doConfiguration(json);

        offerWidget.waitUntilLoads();

        offerWidget.clickInitialiseOfferJS();
        offerWidget.clickInvokeOfferJS();
        offerWidget.waitUntilAllAJAXCallsFinish();
        offerWidget.linkShowAllOffers().assertVisible();
    }
    @Test(description = "Offer Discovery Widget Test - Mobile Number not sent in Config")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56726")
    public void testOfferDiscoveryWidgetE2E_MobileNumberNotSentInConfig() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        data.replace("userDetail",new HashMap<>());

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :"+config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);

        offerWidget.clickButtonLoadJS();
        offerWidget.doConfiguration(json);

        offerWidget.waitUntilLoads();

        offerWidget.clickInitialiseOfferJS();
        offerWidget.clickInvokeOfferJS();
        offerWidget.waitUntilAllAJAXCallsFinish();
        offerWidget.linkShowAllOffers().assertVisible();
    }
    @Test(description = "Offer Discovery Widget Test - Mobile Number sent Empty in Config")
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56726")
    public void testOfferDiscoveryWidgetE2E_MobileNumberSentEmptyInConfig() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantType"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        OfferWidgetDiscovery offerWidgetDiscovery = new OfferWidgetDiscovery();

        Map<String, Object> data = (Map<String, Object>) config.get("data");
        data.put("accessToken", AccessToken);
        data.put("referenceId", referenceId);
        Map<String, String> userDetail = new HashMap<>();
        userDetail.put("mobileNumber", "");
        data.replace("userDetail",userDetail);

        offerWidgetDiscovery.setData(data);

        Map<String, Object> merchant = (Map<String, Object>) config.get("merchant");
        merchant.put("mid", mid.getId());
        offerWidgetDiscovery.setMerchant(merchant);

        offerWidgetDiscovery.setRoot((String) config.get("root"));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(offerWidgetDiscovery);
        OfferWidgetPage offerWidget = new OfferWidgetPage();
        System.out.println("values are :"+config.get("loginPageUrl"));
        offerWidget.launchLoginPage((String) config.get("loginPageUrl"));

        String profile = System.getProperty("currentProfile");
        String url = String.format((String) config.get("jsUrlTemplate"), profile, mid.getId());
        offerWidget.enterJSUrl(url);

        offerWidget.clickButtonLoadJS();
        offerWidget.doConfiguration(json);

        offerWidget.waitUntilLoads();

        offerWidget.clickInitialiseOfferJS();
        offerWidget.clickInvokeOfferJS();
        offerWidget.waitUntilAllAJAXCallsFinish();
        offerWidget.linkShowAllOffers().assertVisible();
    }
}
