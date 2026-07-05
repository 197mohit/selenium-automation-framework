package scripts.api.PromoAndEmiSubvention;

import com.paytm.api.theia.PromoAndEmiSubvention.OfferDiscovery;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.PUSPA;

public class OfferDiscoveryWithSSOToken extends PGPBaseTest {
    String support_for_SSOToken ="{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"7c5855a2-23dd-40c7-abdb-dd6d19846000\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \n" +
            "        \n" +
            "        \"custId\": \"1001534822\",\n" +
            "        \"discoveryLiteResponse\": true,\n" +
            "        \"withCalculation\": true,\n" +
            "        \"paytmUserId\": \"1000036031\",\n" +
            "        \"amountBasedBankOffer\": true,\n" +
            "        \"amountBasedSubvention\": true,\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 1000,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI_DC\"\n" +
            "                    \n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify SSO Token  support in Offer Discovery API discoveryLiteResponse = false")
    public void verifySSoTokenSupportLiteTrue() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken=user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify SSO Token in UPI  support in Offer Discovery API discoveryLiteResponse = false")
    public void verifySSoTokenSupportLiteTrueUPI() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken=user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify SSO Token  support for DEBIT Card  in Offer Discovery API discoveryLiteResponse = false")
    public void verifySSoTokenSupportLiteTrueDEBIT() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken=user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify SSO Token  support for EMI_DC in Offer Discovery API discoveryLiteResponse = false")
    public void verifySSoTokenSupportLiteTrueDC() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken=user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify SSO Token  support in Offer Discovery API discoveryLiteResponse=true")
    public void verifySSoTokenSupportLiteFalse() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken=user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
    @Owner(PUSPA)
    @Feature("PGP-54974")
    @Test(description = "Verify  Invalid SSO Token error mgs in Offer Discovery API")
    public void verifyInvalidSSoTokenErrorMgs() throws Exception {
        String txnToken="hdsgfjhafdshgafshgdfavf";
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType","SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SSO Token is invalid");
    }
    @Owner(PUSPA)
    @Feature("PGP-55241")
    @Test(description = "all-offers false in offerDiscovery API req when orderAmount is not passed")
    public void verifyallOffersFalse() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken = user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType", "SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .deleteContext("body.paymentDetails.orderAmount")
                .setContext("body.all-offers", true)
                .setContext("body.discoveryLiteResponse", false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "/ads/v2/offer/discovery?all-offers=true");

    }

    @Owner(PUSPA)
    @Feature("PGP-55241")
    @Test(description = "all-offers true in offerDiscovery API req when orderAmount is  passed")
    public void verifyallOffersTruewithAmount() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken = user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType", "SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.all-offers", true)
                .setContext("body.discoveryLiteResponse", false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "/ads/v2/offer/discovery");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(PUSPA)
    @Feature("PGP-55241")
    @Test(description = "all-offers true in offerDiscoveryLite API req when orderAmount is not passed")
    public void verifyallOffersTrueLite() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnToken = user.ssoToken();
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(support_for_SSOToken)
                .setContext("head.tokenType", "SSO")
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .deleteContext("body.paymentDetails.orderAmount")
                .setContext("body.all-offers", true)
                .setContext("body.discoveryLiteResponse", true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery/lite?all-offers=true");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
}
