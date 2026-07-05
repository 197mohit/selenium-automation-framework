package scripts.api.PromoAndEmiSubvention;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.KARMVIR;
import static com.paytm.appconstants.Constants.Owner.SHWETANK;

import static com.paytm.appconstants.Constants.Owner.*;

public class OfferApplyAccessTokenTest extends PGPBaseTest {
    String emi_body_item_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"ACCESS\",\n" +
            "        \"token\": \"5177b1cc1e594c959ba232c7e2754e0f1687860859762\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020001\",\n" +
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
            "                            \"offerId\": \"2340886\"\n" +
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
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"ACCESS\",\n" +
            "        \"token\": \"5177b1cc1e594c959ba232c7e2754e0f1687860859762\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
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
    String emi_body_card_token = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"ACCESS\",\n" +
            "        \"token\": \"+OxQdDj23Tc17FMizu6KES0XoFq/g97qqq4=\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020001\",\n" +
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
            "                            \"offerId\": \"2340886\"\n" +
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
            "                     \"cardTokenInfo\": {\n" +
            "                        \"cardToken\": \"4610151810000195\",\n" +
            "                        \"panUniqueReference\": \"V0010013021361288827541720480\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify item based subvention with cashback bank offer  when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithCashbackOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.PPBL);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2340886")
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
    @Test(description = "Verify amount based subvention with discount bank offer when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithDiscountBankOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
    @Test(description = "Verify success response if channelId  provided  is WAP when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenChannelIdIsProvidedAsWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.PPBL);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("head.channelId", "WAP")
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2340886")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011")
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
    @Test(description = "Verify error when expired/wrong access token is provided for EMI paymode")
    public void TestFailedResponseWhenWrongTxnTokenIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("head.token","123")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Access Token.");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2100");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when cardTokenInfo object  is provided when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenCardTokenInfoObjectIsPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.PPBL);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.custId",custId)
                .setContext("body.mid",mid.getId())
                .setContext("body.items[0].price",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2340886")
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
    @Test(description = "Verify success response when paymethod EMI_DC when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodEMI_DCIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.custId",custId)
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
    @Test(description = "Verify success response when paymethod is CREDIT_CARD with bankoffer in item based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodCreditCardIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.PPBL);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
    @Test(description = "Verify success response when paymethod is DEBIT_CARD in amount based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodDebitCardIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in item based when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsItemBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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

    @Test(description = "Verify when wrong  brandid , category id is passed in item based  when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongItemdetailsIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in amount based  when token type is Access Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsAmountBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
    @Test(description = "Verify failed when out of range amount is passed in amount based  when token type is Access Token for EMI paymode")
    public void TestFailedResponseWhenEMIPaymethodWithOutofRangeAmountBasedIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
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
    @Feature("PGP-47712")
    @Test(description = "Verify success response when paymethod is NB in Item based request when token type is Access Token")
    public void TestSuccessResponseWhenPaymethodNBIsProvidedItemBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",AccessToken)
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

    @Owner(PUSPA)
    @Feature("PGP-55994")
    @Test(description = "Verify APR in  response of applyPromo for paymode EMI_DC")
    public void verifyAPROfferApply() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.apr")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails[0].apr[0]")).contains("16.32");
    }
    @Owner(PUSPA)
    @Feature("PGP-55994")
    @Test(description = "Verify APR does not return in  response of applyPromo for paymode EMI")
    public void verifyAPRnotInOfferApply() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",AccessToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4718650100010336")
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).doesNotContain("apr");
    }
}