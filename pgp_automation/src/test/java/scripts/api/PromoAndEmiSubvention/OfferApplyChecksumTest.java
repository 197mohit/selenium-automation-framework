package scripts.api.PromoAndEmiSubvention;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import static com.paytm.appconstants.Constants.Owner.SHWETANK;
public class OfferApplyChecksumTest extends PGPBaseTest {
    String emi_body_item_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"l1bkJm4RnPsuNv6n8NWVI9tDqcycpB9mE44IkIE4ZxZ4DY91kdauKDAveJVOLNFo80xucgI2+OxQdDj23Tc17FMizu6KES0XoFq/g97qqq4=\"\n" +
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
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"l1bkJm4RnPsuNv6n8NWVI9tDqcycpB9mE44IkIE4ZxZ4DY91kdauKDAveJVOLNFo80xucgI2+OxQdDj23Tc17FMizu6KES0XoFq/g97qqq4=\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"emiOfferDetails\": {\n" +
            "                \"offerId\": \"2141488\"\n" +
            "            },\n" +
            "            \"bankOfferDetails\": [\n" +
            "                {\n" +
            "                    \"offerId\": \"2151610\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\":2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_card_token = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"l1bkJm4RnPsuNv6n8NWVI9tDqcycpB9mE44IkIE4ZxZ4DY91kdauKDAveJVOLNFo80xucgI2+OxQdDj23Tc17FMizu6KES0XoFq/g97qqq4=\"\n" +
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
            "                     \"cardTokenInfo\": {\n" +
            "                        \"cardToken\": \"4610151810000195\",\n" +
            "                        \"panUniqueReference\": \"V0010013021361288827541720480\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String EMI_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"items\":[{\"id\":\"123\",\"productId\":\"8903287020001\",\"brandId\":\"327\",\"categoryId\":\"3271\",\"price\":2000.25,\"offerDetails\":{\"emiOfferDetails\":{\"offerId\":\"2164614\"},\"bankOfferDetails\":[{\"offerId\":\"2155512\"}]}}],\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"cardNo\":\"4718650100010336\"}]}}";
    String EMI_amount_based_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"true\",\"amountBasedSubvention\":\"true\",\"offerDetails\":{\"emiOfferDetails\":{\"offerId\":\"2141488\"},\"bankOfferDetails\":[{\"offerId\":\"2151610\"}]},\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"cardNo\":\"4718650100010336\"}]}}";
    String EMI_DC_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"items\":[{\"id\":\"123\",\"productId\":\"8903287020001\",\"brandId\":\"327\",\"categoryId\":\"3271\",\"price\":2000.25,\"offerDetails\":{\"emiOfferDetails\":{\"offerId\":\"2164614\"},\"bankOfferDetails\":[{\"offerId\":\"2155512\"}]}}],\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"EMI_DC\",\"cardNo\":\"4799320857008816\"}]}}";
    String CC_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"items\":[{\"id\":\"123\",\"productId\":\"8903287020001\",\"brandId\":\"327\",\"categoryId\":\"3271\",\"price\":2000.25,\"offerDetails\":{\"bankOfferDetails\":[{\"offerId\":\"2151610\"}]}}],\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"cardNo\":\"4718650100010336\"}]}}";
    String DC_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"true\",\"amountBasedSubvention\":\"true\",\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"DEBIT_CARD\",\"cardNo\":\"4799320857008816\"}]}}";
    String CardTokenInfo_bodyforchecksum="{\"mid\":\"qa12FU97229952596781\",\"custId\":\"1000177185\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"items\":[{\"id\":\"123\",\"productId\":\"8903287020001\",\"brandId\":\"327\",\"categoryId\":\"3271\",\"price\":2000.25,\"quantity\":1,\"offerDetails\":{\"emiOfferDetails\":{\"offerId\":\"2164614\"},\"bankOfferDetails\":[{\"offerId\":\"2155512\"}]}}],\"paymentDetails\":{\"orderAmount\":2000.25,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"cardTokenInfo\":{\"cardToken\":\"4610151810000195\",\"panUniqueReference\":\"V0010013021361288827541720480\"}}]}}";
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify item based subvention with cashback bank offer  when token type is CHECKSUM for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithCashbackOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),EMI_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",checksum)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId","1000177185")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2155512")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001")
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
    @Test(description = "Verify amount based subvention with discount bank offer when token type is CHECKSUM  for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithDiscountBankOfferIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),EMI_amount_based_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",checksum)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId","1000177185")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body..offerDetails.bankOfferDetails[0].offerId","2151610")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4718650100010336");
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
    @Test(description = "Verify success response if channelId  provided  is WAP when token type is CHECKSUM  for EMI paymode")
    public void TestSuccessResponseWhenChannelIdIsProvidedAsWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),EMI_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.channelId", "WAP")
                .setContext("head.token",checksum)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId","1000177185")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2155512")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001")
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
    @Test(description = "Verify error response when request is different w.r.t CHECKSUM is provided for EMI paymode")
    public void TestFailedResponseWhenWrongChecksumIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("body.mid",mid.getId())
                .setContext("head.token","123")
                .setContext("body.custId","1000177185")
                .setContext("head.token","123")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Checksum provided is invalid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2005");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when cardTokenInfo object is provided when token type is CHECKSUM for EMI paymode")
    public void TestSuccessResponseWhenCardTokenInfoObjectIsPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum = PGPUtil.getChecksum(mid.getKey(), CardTokenInfo_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .setContext("head.token",checksum)
                .setContext("body.custId","1000177185")
                .setContext("body.mid",mid.getId())
                .setContext("body.items[0].price",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2155512")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken","4610151810000195")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference","V0010013021361288827541720480")
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
    @Test(description = "Verify success response when paymethod EMI_DC when token type is CHECKSUM")
    public void TestSuccessResponseWhenPaymethodEMI_DCIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),EMI_DC_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",checksum)
                .setContext("body.custId","1000177185")
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
    @Test(description = "Verify success response when paymethod is CREDIT_CARD with bankoffer  in item based request when token type is CHECKSUM")
    public void TestSuccessResponseWhenPaymethodCreditCardIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),CC_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",checksum)
                .setContext("body.custId","1000177185")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2151610")
                .setContext("body.items[0].brandId","327")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001")
                .setContext("body.mid",mid.getId());
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
    @Test(description = "Verify success response when paymethod is DEBIT_CARD in amount based request when token type is CHECKSUM")
    public void TestSuccessResponseWhenPaymethodDebitCardIsProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),DC_bodyforchecksum);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",checksum)
                .setContext("body.custId","1000177185")
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


}
