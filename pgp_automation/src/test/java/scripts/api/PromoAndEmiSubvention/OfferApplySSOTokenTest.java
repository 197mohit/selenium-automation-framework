package scripts.api.PromoAndEmiSubvention;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.*;

public class OfferApplySSOTokenTest extends PGPBaseTest {
    String emi_body_item_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5fe081da-c1d6-4923-a24d-34976cb01600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2155512\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"issuingNetworkCode\": \"VISA\",\n" +
            "                     \"vpa\": \"\",\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5177b1cc1e594c959ba232c7e2754e0f1687860859762\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"emiOfferDetails\": {\n" +
            "                \"offerId\": \"2141488\"\n" +
            "            },\n" +
            "            \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2151610\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"vpa\": \"\",\n" +
            "                     \"issuingBank\": \"\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_with_tenure="{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5fe081da-c1d6-4923-a24d-34976cb01600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"emiOfferDetails\": {\n" +
            "                \"offerId\": \"2141488\"\n" +
            "            },\n" +
            "            \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2151610\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "        },\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"quantity\": 1,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2155512\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_card_token = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5fe081da-c1d6-4923-a24d-34976cb01600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2155512\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                   \n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardTokenInfo\": {\n" +
            "                        \"cardToken\": \"4610151810000195\",\n" +
            "                        \"panUniqueReference\": \"V0010013021361288827541720480\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_item_based_with_tenure = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5fe081da-c1d6-4923-a24d-34976cb01600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2155512\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"issuingNetworkCode\": \"VISA\",\n" +
            "                    \"vpa\": \"\",\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify API should give success response when all mandatory params are passed when token type is SSO")
    public void TestSuccessResponseWhenEMIPaymethodIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response if channelId  provided  is WAP")
    public void TestSuccessResponseWhenChannelIdIsProvidedAsWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.channelId", "WAP")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify API should give valid error response  invalid SSO token is provided ")
    public void TestFailedResponseWhenWrongSSOIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token", "4fe081da-c1d6-4923-a24d-34976cb01600")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SSO Token is invalid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when token is not passed in request")
    public void TestfailedResponseWhenTokenIsBlank() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token", "")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of  id is not passed")
    public void TestfailedResponseWhenIdIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].id", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of productId is not passed")
    public void TestfailedResponseWhenProductIdIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].productId", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of brandId is not passed")
    public void TestfailedResponseWhenBrandIdIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].brandId", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of categoryId is not passed")
    public void TestfailedResponseWhenCategoryIdIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].categoryId", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of price is not passed")
    public void TestfailedResponseWhenPriceIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of mid is not passed")
    public void TestfailedResponseWhenMidIsBlank() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.mid", "")
                .setContext("head.token", ssoToken)
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of orderAmount is not passed")
    public void TestfailedResponseWhenOrderAmountIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.orderAmount", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when value of issuingNetworkCode is not passed")
    public void TestSuccessResponseWhenIssuingNetworkCodeIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when value of issuingBank is not passed")
    public void TestSuccessResponseWhenIssuingBankIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of payMethod is not passed")
    public void TestfailedResponseWhenPayMethodIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of cardNo is not passed")
    public void TestfailedResponseWhencardNoIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of paymentDetailsObject is not passed")
    public void TestfailedResponseWhenPaymentDetailsObjectIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails", null)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of paymentOptionsObject is not passed")
    public void TestfailedResponseWhenPaymentOptionsObjectIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions", null)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  token field is not passed")
    public void TestfailedResponseWhentokenIsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("head.token")
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  token type field is not passed")
    public void TestfailedResponseWhentokenTypeIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("head.tokenType")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  channelId field is not passed")
    public void TestfailedResponseWhenchannelIdIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("head.channelId")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when id field is not passed in item object")
    public void TestfailedResponseWhenIdIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].id")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when productId  field is not passed in item object")
    public void TestfailedResponseWhenProductIdIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].productId")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when brandId field is not passed in item object")
    public void TestfailedResponseWhenBrandIdIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].brandId")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when categoryId field is not passed in item object")
    public void TestfailedResponseWhenCategoryIdIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].categoryId")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when price field is not passed in item object")
    public void TestfailedResponseWhenPriceIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].price")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when mid field is not passed")
    public void TestfailedResponseWhenMidIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.mid")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when PaymentDetails Object is not passed")
    public void TestfailedResponseWhenPaymentDetailsObjectIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when PaymentOptions Object is not passed")
    public void TestfailedResponseWhenPaymentOptionsObjectIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.paymentOptions")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when orderAmount field is not passed")
    public void TestfailedResponseWhenOrderAmountIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.orderAmount")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when issuingBank field is not passed")
    public void TestSuccessResponseWhenissuingBankIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when issuingNetworkCode field is not passed")
    public void TestSuccessResponseWhenissuingNetworkCodeIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.mid",mid.getId())
                .setContext("head.token", ssoToken)
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when payMethod field is not passed")
    public void TestfailedResponseWhenpayMethodIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.paymentOptions[0].payMethod")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when cardNo field is not passed")
    public void TestfailedResponseWhencardNoIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  tokenType passed is JWT")
    public void TestfailedResponseWhentokenTypePassedAsJWT() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.tokenType", "JWT")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  channelId passed is APP")
    public void TestfailedResponseWhenChannelIdPassedAsAPP() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.channelId", "APP")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when id is passed in integer")
    public void TestSuccessResponseWhenIdIsPassedInInteger() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].id", 123456)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when brandId is passed in integer")
    public void TestSuccessResponseWhenBrandIdIsPassedInInteger() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].brandId", 123)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify response when brandId is passed which does not belongs to any product.")
    public void TestSuccessResponseWhenRandomBrandIdIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].brandId", "123000")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify response when categoryId is passed which does not belongs to any product.")
    public void TestSuccessResponseWhenRandomcategoryIdIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].categoryId", "1234000")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when categoryId is passed in integer")
    public void TestSuccessResponseWhenCategoryIdIsPassedInInteger() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].categoryId", 1234)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when negative price is passed")
    public void TestfailedResponseWhenPriceIsNegative() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "-2000.25")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when price is provided to more then two decimal")
    public void TestfailedResponseWhenMoreThanTwoDecimalPricePass() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "2000.256")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify response when price and orderAmount provided is less than 1")
    public void TestfailedResponseWhenLessThan1RsPricePass() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "0.25")
                .setContext("body.paymentDetails.orderAmount", "0.25")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify response when price and orderAmount is provided to 1 decimal")
    public void TestSuccessResponseWhenOneDecimalPricePass() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "1.5")
                .setContext("body.paymentDetails.orderAmount", "1.5")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when price and orderAmount is passed in string")
    public void TestSuccessResponseWhenPriceAndOrderAmountIsPassedInString() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "2000.25")
                .setContext("body.paymentDetails.orderAmount", "2000.25")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify response when price and order amount are different.")
    public void TestfailedResponseWhenPriceAndAmountIsDifferent() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].price", "2000.25")
                .setContext("body.paymentDetails.orderAmount", "3000.25")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when quantity is passed as more than 1")
    public void TestfailedResponseWhenQuantityIsMoreThanOne() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].quantity", 2)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when quantity is not passed ")
    public void TestSuccessResponseWhenQuantityIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].quantity")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when quantity is passed in string")
    public void TestSuccessResponseWhenQuantityIsPassedInString() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].quantity", "1")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when value of quantity is not passed")
    public void TestSuccessResponseWhenQuantityIsBlank() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.items[0].quantity", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when only bankOfferDetails object is passed")
    public void TestSuccessResponseWhenOnlyBankOfferDetailsObjectIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when only emiOfferDetails object is passed")
    public void TestSuccessResponseWhenOnlyEmiOfferDetailsObjectIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].offerDetails.bankOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success when OfferDetails object is not passed")
    public void TestSuccessResponseWhenOfferDetailsObjectIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("body.items[0].offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when invalid mid is passed")
    public void TestfailedResponseWhenInvalidMidIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.mid", "qa12FU972299525967343")
                .setContext("head.token", ssoToken)
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No Plans Available");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when negative orderAmount is passed")
    public void TestfailedResponseWhenOrderAmountIsNegative() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.orderAmount", "-2000.25")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when orderAmount is provided to more then two decimal")
    public void TestfailedResponseWhenMoreThanTwoDecimalOrderAmountPass() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.orderAmount", "2000.256")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when  wrong paymethod is provided")
    public void TestfailedResponseWhenWrongPaymethodIsPass() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "XYZ")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when cardTokenInfo object  is provided")
    public void TestSuccessResponseWhenCardTokenInfoObjectIsPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2155512")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken","4610151810000195")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference","4610151810000195")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error  when value of cardToken is not provided")
    public void TestfailedResponseWhenCardTokenValueIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", "")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when field cardToken is not provided")
    public void TestfailedResponseWhenCardTokenFieldIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when value of panUniqueReference is not provided")
    public void TestfailedResponseWhenPanUniqueReferenceIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference","")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify error when field panUniqueReference is not provided")
    public void TestfailedResponseWhenPanUniqueReferenceFieldIsNotPassed() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }

    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when tenure is passed")
    public void TestSuccessResponseWhentenureIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value","3");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isEqualTo("[[HDFC|3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when tenure value is passed in string")
    public void TestSuccessResponseWhenTenureValueInStringIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value","3")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isEqualTo("[[HDFC|3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify failed response when tenure value is invalid")
    public void TestFailedResponseWhenInvalidTenureValueIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",4)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify failed response when tenure unit is invalid")
    public void TestFailedResponseWhenInvalidTenureUnitIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","xyz")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("invalid tenure details");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when tenure value is passed for paymethod EMI_DC")
    public void TestSuccessResponseWhenPaymethodEMI_DCIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isEqualTo("[[ICICI|3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when paymethod is CREDIT_CARD")
    public void TestSuccessResponseWhenPaymethodCreditCardIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.items[0].productId","8903287020001")
                .deleteContext("body.items[0].offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when paymethod is DEBIT_CARD")
    public void TestSuccessResponseWhenPaymethodDebitCardIsProvided() throws Exception {
         Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","DEBIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.items[0].productId","8903287020001")
                .deleteContext("body.items[0].offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[DEBIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify amount based subvention with discount bank offer when token type is SSO Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithDiscountBankOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body..offerDetails.bankOfferDetails[0].offerId","2151610")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify item based subvention with cashback bank offer  when token type is SSO Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithCashbackOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2155512")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when paymethod EMI_DC when token type is SSO Token")
    public void TestSuccessResponseWhenPaymethodEMIDCIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= "REGOFFERAPPLYSSOMOCK0007";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",ssoToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3, 6, 9, 12]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isEqualTo("[[ICICI|3, ICICI|6, ICICI|9, ICICI|12]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when paymethod is CREDIT_CARD with bankoffer in item based request when token type is SSO Token")
    public void TestSuccessResponseWhenPaymethodCreditCardwithBankOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",ssoToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.custId",custId)
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("body.mid",mid.getId())
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2151610")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when paymethod is DEBIT_CARD in amount based request when token type is SSO Token")
    public void TestSuccessResponseWhenPaymethodDebitCardInAmountBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",ssoToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","DEBIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .deleteContext("body.offerDetails")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[DEBIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in item based when token type is SSO Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsItemBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","9999")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","9999")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }

    @Test(description = "Verify when wrong  brandid , category id is passed in item based  when token type is SSO Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongItemdetailsIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","9999")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","9999")
                .setContext("body.items[0].brandId","9")
                .setContext("body.items[0].categoryId","9")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in amount based  when token type is SSO Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsAmountBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","9999")
                .setContext("body..offerDetails.bankOfferDetails[0].offerId","9999")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify failed when out of range amount is passed in amount based  when token type is SSO Token for EMI paymode")
    public void TestFailedResponseWhenEMIPaymethodWithOutofRangeAmountBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",1112000.25)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is BALANCE in amount based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodBALANCEIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is BALANCE in Item based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodBALANCEIsProvidedItemBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","2000.25")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is UPI in amount based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodUPIIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("UPI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is UPI in Item based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodUPIIsProvidedItemBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","2000.25")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("UPI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is NB in amount based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodNBIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("result message is not success").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("NET_BANKING");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is NB in Item based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodNBIsProvidedItemBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","2000.25")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .deleteContext("body.offerDetails")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("NET_BANKING");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.bankOfferDetails")).isNotNull();
    }
    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify api  should give error in applyoffer API request when amountBasedBankOffer=false & amountBasedSubvention=false is passed without item object in it")
    public void Error_in_api_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_with_item_NOT_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be RETAILER when amountBasedBankOffer=true & amountBasedSubvention=true is passed without item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_item_NOT_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be BRAND when amountBasedBankOffer=false & amountBasedSubvention=false is passed with item object in request")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[BRAND]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that flow type  when amountBasedBankOffer=true & amountBasedSubvention=false is passed with item object in request")
    public void Test_api_response_when_both_amountBasedBankOffer_true_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","false")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_OFFERS]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that flow type  when amountBasedBankOffer=false & amountBasedSubvention=true is passed with item object in request")
    public void Test_api_response_when_both_amountBasedBankOffer_false_and_amountBasedSubvention_true_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_SUBVENTION]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that flow type  when amountBasedBankOffer=true & amountBasedSubvention=false is passed without item object in request")
    public void Test_api_response_when_both_amountBasedBankOffer_true_and_amountBasedSubvention_false_without_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Owner(KARMVIR)
    @Feature("PGP-46696")
    @Test(description = "Verify that flow type  when amountBasedBankOffer=false & amountBasedSubvention=true is passed without item object in request")
    public void Test_api_response_when_both_amountBasedBankOffer_false_and_amountBasedSubvention_true_without_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when BOEleigble and Subvention amount provided and both are partial amount for EMI paymode")
    public void Test_api_response_when_both_SubventionAmountAndBOEligibleAmountPaased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0002";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Credit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(1047.24);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(1045.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(1045.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(1047.24);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(349.08);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(4.5);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹50.0 discount applied successfully.");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when BOEleigble and Subvention amount provided and both are partial amount for EMI_DC paymode")
    public void Test_api_response_when_both_SubventionAmountAndBOEligibleAmountPaasedEMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0001";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Debit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(1042.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(1050.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(1050.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(1051.74);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(350.58);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(9.0);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹50.0 discount applied successfully.");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when BOEleigbleAmount provided and both are partial amount for NB paymode")
    public void Test_api_response_when_BOEligibleAmountPaasedNB() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("NET_BANKING");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].effectiveAmount")).isEqualTo(1050.00);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].payableAmount")).isEqualTo(1050.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].totalPayableAmount")).isEqualTo(1050.00);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹50.0 discount applied successfully.");
    }
    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when only Subvention amount provided and subvention is partial amount for EMI paymode")
    public void Test_api_response_when_Only_SubventionAmountPaasedEMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0003";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Credit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(987.15);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(985.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(985.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(987.15);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(329.05);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.65);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(4.5);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹110.0 discount applied successfully.");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when only Subvention amount provided and subvention is partial amount for EMI_DC paymode")
    public void Test_api_response_when_Only_SubventionAmountPaasedEMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0004";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Debit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(982.65);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(990.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(990.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(991.65);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(330.55);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.65);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(9.0);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹110.0 discount applied successfully.");
    }
    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when only BOEligible amount provided and BOEligible is partial amount for EMI paymode")
    public void Test_api_response_when_Only_BOEligibleAmountPaasedEMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0005";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Credit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(1041.24);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(1039.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(1039.5);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(1041.24);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(347.08);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(10.5);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹50.0 discount applied successfully.");
    }

    @Owner(KARMVIR)
    @Feature("PGP-46698")
    @Test(description = "Verify the api response when only BOEligible amount provided and BOEligible is partial amount for EMI_DC paymode")
    public void Test_api_response_when_Only_BoeligibleAmountPaasedEMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0006";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure,referenceId)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2151610")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].cardType")).isEqualTo("Debit Card");
        Assertions.assertThat(jsonPath.getInt("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo(3);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isEqualTo(1030.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].loanAmount")).isEqualTo(1050.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo(1100.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].payableAmount")).isEqualTo(1050.0);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isEqualTo(1051.74);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2141488");
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isEqualTo(350.58);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isEqualTo(1.74);
        Assertions.assertThat(jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value")).isEqualTo(21.0);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2151610");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode")).isEqualTo("PROMO1234");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isEqualTo("₹50.0 discount applied successfully.");
    }


    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showKFSLink and showLendingConsent are returned in response")
    public void Test_showKFSLink_showLendingConsent_returned_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showKFSLink is returned as true in response")
    public void Test_showKFSLink_returned_true_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showLendingConsent is returned as true in response for banks set in theia.lending.consent.enabled.banks")
    public void Test_showLendingConsent_returned_true_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[true]");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showLendingConsent is returned as false in response for banks not set in theia.lending.consent.enabled.banks")
    public void Test_showLendingConsent_returned_false_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4444333322221111");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[false]");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is not EMI_DC showKFSLink and showLendingConsent are returned as false in response")
    public void Test_showKFSLink_showLendingConsent_returned_false_for_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4718650100010336");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[false]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[false]");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Best Offer) - EMI/EMI_DC")
    public void TestSuccessOfferApplywithMultipleItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Best Offer) - CC/DC/NB")
    public void TestSuccessOfferApplywithMultipleItem_CC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].tenure",null);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails")).isNotEmpty();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.bankOfferDetails")).isNotEmpty();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.emiOfferDetails")).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Best Offer) - UPI")
    public void TestSuccessOfferApplywithMultipleItem_UPI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","arsh.test2@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].tenure",null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode",null);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[UPI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails")).isNotEmpty();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.bankOfferDetails")).isNotEmpty();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.emiOfferDetails")).isNull();
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Offer specified) - EMI/EMI_DC")
    public void TestSuccessOfferApplywithMultipleItem_EMI_OfferSpecified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224","2388893",bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224","2388893",bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails.offerId")).contains("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails.offerId")).contains("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Offer specified) - CC/DC/NB")
    public void TestSuccessOfferApplywithMultipleItem_CC_OfferSpecified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224",null,bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224",null,bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].tenure",null);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails.offerId")).contains("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.bankOfferDetails.offerId")).contains("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.emiOfferDetails")).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response when Multiple Items are sent for Paymode (Offer specified) - UPI")
    public void TestSuccessOfferApplywithMultipleItem_UPI_OfferSpecified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2394972"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224",null,bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224",null,bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","arsh.test2@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].tenure",null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode",null);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[UPI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails.offerId")).contains("2394972");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.emiOfferDetails")).isEqualTo("[]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[1].offerDetails.bankOfferDetails.offerId")).contains("2394972");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].unifiedGratifications.emiOfferDetails")).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Single Item")
    public void TestSuccessOfferApplywithSingleItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Amount Based Subvention and Single Item based Offers")
    public void TestSuccessOfferApply_AmtBasedSubvention_ItemBasedOffers_SingleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "212312", "25612", 800.00, "25512");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Amount Based Subvention and Multiple Item based Offers")
    public void TestSuccessOfferApply_AmtBasedSubvention_ItemBasedOffers_MultipleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "212312", "25612", 400.00, "25512");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "212312", "25612", 400.00, "25512");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Amount Based Offers and Single Item based Subvention")
    public void TestSuccessOfferApply_ItemBasedSubvention_AmtBasedOffers_SingleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "12312", "5612", 800.00, "5512");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Amount Based Offers and Multiple Item based Subvention")
    public void TestSuccessOfferApply_ItemBasedSubvention_AmtBasedOffers_MultipleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "12312", "5612", 400.0, "5512");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "12312", "5612", 400.0, "5512");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].id")).containsIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the offerApply Response for Amount Based Offers and Amount Based Subvention")
    public void TestSuccessOfferApply_AmtBasedSubvention_AmtBasedOffers() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase("001");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedGratifications.bankOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedEmiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.unifiedSubventionType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalEmiInterest")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "When more than 10 items are sent")
    public void TestFailureOfferApply_MoreThan10Items() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item5 = new SimplifiedUnifiedOffers.Items("Item005_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item6 = new SimplifiedUnifiedOffers.Items("Item006_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item7 = new SimplifiedUnifiedOffers.Items("Item007_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item8 = new SimplifiedUnifiedOffers.Items("Item008_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item9 = new SimplifiedUnifiedOffers.Items("Item009_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item10 = new SimplifiedUnifiedOffers.Items("Item010_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item11 = new SimplifiedUnifiedOffers.Items("Item011_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        items.add(item7);
        items.add(item8);
        items.add(item9);
        items.add(item10);
        items.add(item11);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 12100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "If brandId/categoryId has special characters")
    public void TestFailureOfferApply_brandIdCategoryIdHasSpecialCharacters() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084@", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224@");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
}