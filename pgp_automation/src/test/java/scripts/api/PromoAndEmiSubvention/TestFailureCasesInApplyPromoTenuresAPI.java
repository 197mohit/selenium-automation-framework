package scripts.api.PromoAndEmiSubvention;


import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApplyPromoV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;

public class TestFailureCasesInApplyPromoTenuresAPI extends PGPBaseTest {

    private static final String CARD_NO = "4769953850007926";
    String tenure_json_body = "{\"head\":{\"version\":\"v1\",\"requestTimestamp\":\"1544614590000\",\"requestId\":\"1234\",\"channelId\":\"WEB\",\"token\":\"\",\"tokenType\":\"SSO\"},\"body\":{\"items\":[{\"id\":\"12345\",\"productId\":\"27902\",\"brandId\":\"table\",\"categoryList\":[\"66781\"],\"merchantId\":\"995183\",\"model\":\"awasdfasd\",\"ean\":\"P30\",\"price\":2000.0,\"quantity\":1.0,\"discoverability\":\"n\",\"verticalId\":\"VID2\",\"isPhysical\":false,\"isEmiEnabled\":true}],\"filters\":{\"bankCode\":\"HDFC\",\"cardType\":\"CREDIT_CARD\"},\"mid\":\"\"}}";

    String tenure2ApplyPromo_body = "{\"head\":{\"requestId\":\"9dd70277-8737-4fbf-a8f1-08532301e00e\",\"requestTimeStamp\":\"2\",\"channelId\":\"WEB\",\"tokenType\":\"SSO\",\"token\":\"\"},\"body\":{\"mid\":\"\",\"promocode\":\"MULTITIEMTESTQADC62\",\"paymentOptions\":[{\"transactionAmount\":\"2000\",\"payMethod\":\"EMI\",\"bankCode\":\"HDFC\",\"cardNo\":\"\"}],\"custId\":\"\",\"totalTransactionAmount\":\"2000\"}}" ;

    @Owner(SATWIK_SHARMA)
    @Feature("PGP-54538")
    @Test(description = "Test when emiSubvention/tenures failed due to no plans " +
            "it should map error to theia stating NoEmiOptionAvailable" +
            "We have used mock to achieve the same ")
    public void testNoEmiOptionAvailableErrorInCaseNoPlansAreAvailable() throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;

        String ssoToken= AuthHelpers.getSSOToken("5670122111","Paytm@1234");

        ApiV1Tenure apiV1Tenure = setupEmiSubventionTenures(ssoToken, mid.getId());

        JsonPath jsonpath = apiV1Tenure.execute().jsonPath();
        String responseCode = jsonpath.getString("body.resultInfo.resultCode");
        String responseMsg = jsonpath.getString("body.resultInfo.resultMsg");

        Assert.assertEquals(responseCode,"EMI_006");
        Assert.assertEquals(responseMsg,"No emi options are available for these items");


    }


    @Owner(SATWIK_SHARMA)
    @Feature("PGP-54538")
    @Test(description = "test when affordability gives error No offers available " +
            "it should map the same error msg instead something went wrong" +
            "            \"We have used mock to achieve the same \")\n")
    public void testNoOffersAvailable()
    {

        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;

        String ssoToken= AuthHelpers.getSSOToken("5670122111","Paytm@1234");

        ApplyPromoV2 applyPromoV2 = setupApplyPromoV2(ssoToken, mid.getId());

        JsonPath jsonpath = applyPromoV2.execute().jsonPath();
        String responseCode = jsonpath.getString("body.resultInfo.resultCode");
        String responseMsg = jsonpath.getString("body.resultInfo.resultMsg");

        Assert.assertEquals(responseCode,"9999");
        Assert.assertEquals(responseMsg,"No offer available");


    }



    @Owner(SATWIK_SHARMA)
    @Feature("PGP-54538")
    @Test(description = "Test whatever error message is configured on Offer ID for failures conditions" +
            " ,in PROMO " +
            "the same message should be thrown by theia if  those condition breaks")
    public void testTheiaMapsTheErrorConfiguredOverCampaignOfferInCaseThatConditionFailed()
    {

        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;

        String ssoToken= AuthHelpers.getSSOToken("5670122111","Paytm@1234");

        ApplyPromoV2 applyPromoV2 = setupApplyPromoV2(ssoToken, mid.getId());
        applyPromoV2.setContext("body.paymentOptions[0].payMethod","CREDIT_CARD");
        applyPromoV2.setContext("body.paymentOptions[0].transactionAmount", "500");
        applyPromoV2.setContext("body.totalTransactionAmount", "500");
        JsonPath jsonpath = applyPromoV2.execute().jsonPath();
        String responseCode = jsonpath.getString("body.resultInfo.resultCode");
        String responseMsg = jsonpath.getString("body.resultInfo.resultMsg");

        Assert.assertEquals(responseCode,"9999");
        Assert.assertEquals(responseMsg,"InvalidPayment Method");    }



    private ApiV1Tenure setupEmiSubventionTenures(String ssoToken, String mid) {
        return (ApiV1Tenure) new ApiV1Tenure(tenure_json_body,mid)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid)
                .setContext("body.items[0].price", "1986");
    }


    private ApplyPromoV2  setupApplyPromoV2(String ssoToken, String mid )
    {
        return (ApplyPromoV2) new ApplyPromoV2(tenure2ApplyPromo_body,mid)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid)
                .setContext("body.paymentOptions[0].transactionAmount", "1987")
                .setContext("body.totalTransactionAmount", "1987")
                .setContext("body.custId", "1704163241")
                .setContext("body.paymentOptions[0].cardNo", CARD_NO);
    }


}
