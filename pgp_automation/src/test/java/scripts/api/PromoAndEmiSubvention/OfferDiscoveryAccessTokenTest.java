package scripts.api.PromoAndEmiSubvention;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferDiscovery;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.*;
public class OfferDiscoveryAccessTokenTest extends PGPBaseTest {
    String emi_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"ACCESS\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 1000.00,\n" +
            "}],\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 1000.00,\n" +
            "\"paymentOptions\": [" +
            "{\n" +
            "\"payMethod\": \"EMI\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "\"tenure\": [{" +
            "\"unit\": \"MONTH\",\n" +
            "\"value\": \"3\",\n" +
            "}]\n" +
            "}" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}";
    String single_paymode_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"ACCESS\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 1000.00,\n" +
            "}],\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 1000.00,\n" +
            "\"paymentOptions\": [" +
            "{\n" +
            "\"payMethod\": \"CREDIT_CARD\",\n" +
            "\"vpa\": \"\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "\"issuingNetworkCode\": \"MASTERCARD\",\n" +
            "}" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}";
    String allPaymode_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"ACCESS\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 1000.00,\n" +
            "}],\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 1000.00,\n" +
            "\"paymentOptions\": [" +
            "{\n" +
            "\"payMethod\": \"CREDIT_CARD\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"DEBIT_CARD\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"NET_BANKING\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"EMI\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "\"tenure\": [{" +
            "\"unit\": \"MONTH\",\n" +
            "\"value\": \"3\",\n" +
            "}]\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"EMI_DC\",\n" +
            "\"issuingBank\": \"ICICI\",\n" +
            "\"tenures\": [{" +
            "\"unit\": \"MONTH\",\n" +
            "\"value\": \"3\",\n" +
            "}]\n" +
            "}" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}";
    String fourPaymode_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"ACCESS\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 1000.00,\n" +
            "}],\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 1000.00,\n" +
            "\"paymentOptions\": [" +
            "{\n" +
            "\"payMethod\": \"CREDIT_CARD\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"DEBIT_CARD\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"EMI\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "\"tenure\": [{" +
            "\"unit\": \"MONTH\",\n" +
            "\"value\": \"3\",\n" +
            "}]\n" +
            "}" +
            "{\n" +
            "\"payMethod\": \"EMI_DC\",\n" +
            "\"issuingBank\": \"ICICI\",\n" +
            "\"tenures\": [{" +
            "\"unit\": \"MONTH\",\n" +
            "\"value\": \"3\",\n" +
            "}]\n" +
            "}" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}";

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when all  Paymodes are povided")
    public void TestSuccessResponseWhenAllPaymodesAreProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is Debit card")
    public void TestSuccessResponseWhenDCProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[DEBIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is credit card")
    public void TestSuccessResponseWhenCCProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is NB and issuing bank is not provided")
    public void TestSuccessResponseWhenNBWithoutIssuingBankProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isEqualTo("[[[discount]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isEqualTo("[[[percentage]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI")
    public void TestSuccessResponseWhenEMIProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("15.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("HDFC|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI_DC")
    public void TestSuccessResponseWhenEMIDCProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[ICICI Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("16.01");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("ICICI|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when more then 1 Paymode in request")
    public void TestSuccessResponseWhen4PaymodesProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(fourPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].payMethod")).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].payMethod")).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].payMethod")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.roi")).isEqualTo("15.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].planId")).isEqualTo("HDFC|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isEqualTo("percentage");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("16.01");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("ICICI|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        ;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when All Paymode are provided and Items are not provied")
    public void TestSuccessResponseWhenAllPaymodesAreProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.items");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3, 6, 9, 12], [3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isEqualTo("[[ICICI|3, ICICI|6, ICICI|9, ICICI|12], [HDFC|3]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is Debit card and Items not passed")
    public void TestSuccessResponseWhenDCProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[DEBIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is Credit card and Items are not provided")
    public void TestSuccessResponseWhenCCProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is Net banking and Items are not provided")
    public void TestSuccessResponseWhenNBWithoutIssuingBankProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[NET_BANKING]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI and Items are not provided")
    public void TestSuccessResponseWhenEMIProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("1.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("HDFC|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items[0].offerDetails.emiOfferDetails[0].gratifications")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI_DC and Items are not provided")
    public void TestSuccessResponseWhenEMIDCProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[ICICI Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.benefitText")).isEqualTo("[Low Cost EMI with Additional Offer]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("1.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("ICICI|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isEqualTo("MERCHANT");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when more then 1 Paymode is provided and Items are not provided")
    public void TestSuccessResponseWhen4PaymodesProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(fourPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.items");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.roi")).isEqualTo("1.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].planId")).isEqualTo("HDFC|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerText")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("1.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("1000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("ICICI|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when channelId is WAP")
    public void TestSuccessResponseWhenChannelWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("head.channelId", "WAP");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when negative Price and amount is provided in the request")
    public void TestSuccessResponseWhenNegativePricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].price", "-1")
                .setContext("body.paymentDetails.orderAmount", -1);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when less then 1 rs Price and amount is provided in the request")
    public void TestSuccessResponseWhenLessThan1RsPricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].price", "0.5")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.orderAmount", 0.5);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when price and amount is provided to 1 decimal ")
    public void TestSuccessResponseWhenOneDecimalPricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].price", "1.5")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "HDFC")
                .setContext("body.paymentDetails.orderAmount", 1.5);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[CREDIT_CARD]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when price is provided to more then two decimal ")
    public void TestSuccessResponseWhenMoreThanTwoDecimalPricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].price", "100.456464")
                .setContext("body.paymentDetails.orderAmount", 100.456464);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when incorrect MID id provided in the request")
    public void TestSuccessResponseWhenIncorrectMIDPass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.mid", "jhsgdfuicbc");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Access Token.");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2100");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when Price and amount is different in the request")
    public void TestSuccessResponseWhenPriceAndAmountIsDifferent() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].price", "10")
                .setContext("body.paymentDetails.orderAmount", 200);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when token type is null")
    public void TestfailureResponseWhenTokenTypeIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("head.tokenType", null)
                .setContext("body.items[0].price", "200")
                .setContext("body.paymentDetails.orderAmount", 200);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when channel id id null")
    public void TestfailureResponseWhenChannelIdIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("head.channelId", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when amount and price is null")
    public void TestfailureResponseWhenAmountAndPriceIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].price", null)
                .setContext("body.paymentDetails.orderAmount", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when Id is null in Item object")
    public void TestfailureResponseWhenIdIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].id", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when productId is null in Item object")
    public void TestfailureResponseWhenProductIdIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].productId", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when brandId is null in Item object")
    public void TestfailureResponseWhenbrandIdIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].brandId", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when categoryId is null in Item object")
    public void TestfailureResponseWhencategoryIdIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].categoryId", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when price is null in Item object")
    public void TestfailureResponseWhenPriceIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].price", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when Mid is null")
    public void TestfailureResponseWhenMidIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.mid", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when paymentDetails object is null")
    public void TestfailureResponseWhenpaymentDetailsObjectIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when orderAmount is null in paymentDetails object")
    public void TestfailureResponseWhenorderAmountIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.orderAmount", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when paymentOptions is null in paymentDetails object")
    public void TestfailureResponseWhenpaymentOptionsIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .deleteContext("body.paymentDetails.paymentOptions[0]");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when payMethod is null in paymentDetails object")
    public void TestfailureResponseWhenpayMethodIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No Offers Available");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0002");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when invalid tokenType is passed")
    public void verifyForInvalidTokenType() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.tokenType", "TXN_TOKENNNN")
                .setContext("head.token", AccessToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("System error");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("00000900");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when ChannelID is other then WEB/WAP")
    public void verifyErrorwhenChannelIDIsNotValid() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.channelId", "WAPPP")
                .setContext("head.token", AccessToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("System error");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("00000900");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when wrong productId, brandID and categoryId is passed")
    public void verifyWhenWrongProductIDAndCategoryIDisPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].categoryId", "00000")
                .setContext("body.items[0].brandId", "00000")
                .setContext("body.items[0].productId", "00000");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when quantity is passed other than 1 ")
    public void verifyWhenQuantityiSPassedOtherThan1() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.items[0].quantity", "2");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when different value of IssuingNetworkCode is passed")
    public void TestSuccessResponseForDifferentValueOfIssuingNetworkCode() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when incorrect value of payMethod is passed")
    public void TestfailureResponseWhenIncorrectPayMethodIsPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "xyz")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when Paymethod is UPI but VPA is not passed")
    public void TestSuccessResponseWhenVPAIsNotPassedForPaymethodUPI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when different value of issuingBank is passed")
    public void TestSuccessResponseForDifferentValueOfIssuingBank() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "xyz")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when items object is not passed")
    public void verifyWhenItemObjectisNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("[SUBVENTION]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is wallet")
    public void TestSuccessResponseWhenBalanceProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "BALANCE");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is wallet for Item Based")
    public void TestSuccessResponseWhenBalanceProvidedItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].id", "15036688")
                .setContext("body.items[0].productId", "1")
                .setContext("body.items[0].brandId", "10002")
                .setContext("body.items[0].categoryId", "15036688")
                .setContext("body.items[0].price", "1000.00")
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "BALANCE");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }


    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is UPI")
    public void TestSuccessResponseWhenUPIProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa", "test@paytm");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("UPI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is UPI for Item Based")
    public void TestSuccessResponseWhenUPIProvidedItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].id", "15036688")
                .setContext("body.items[0].productId", "1")
                .setContext("body.items[0].brandId", "10002")
                .setContext("body.items[0].categoryId", "15036688")
                .setContext("body.items[0].price", "1000.00")
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.paymentDetails.paymentOptions[0].vpa", "test@paytm");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("UPI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is NB")
    public void TestSuccessResponseWhenNBProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("NET_BANKING");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-47712")
    @Test(description = "verify the api response when Paymode is NB for Item Based")
    public void TestSuccessResponseWhenNBProvidedItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.items[0].id", "15036688")
                .setContext("body.items[0].productId", "1")
                .setContext("body.items[0].brandId", "10002")
                .setContext("body.items[0].categoryId", "15036688")
                .setContext("body.items[0].price", "1000.00")
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("NET_BANKING");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails[]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be RETAILER when amountBasedBankOffer=true & amountBasedSubvention=true is passed without item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_item_NOT_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be RETAILER when amountBasedBankOffer=true & amountBasedSubvention=true is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be BRAND when amountBasedBankOffer=true & amountBasedSubvention=flase is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("flowType=[BRAND]");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that discovery API  should give error when amountBasedBankOffer=false & amountBasedSubvention=false is  passed without item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_without_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be AMOUNT_BASED_SUBVENTION when amountBasedBankOffer=false & amountBasedSubvention=true is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_false_and_amountBasedSubvention_true_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_SUBVENTION]");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be AMOUNT_BASED_OFFERS when amountBasedBankOffer=true & amountBasedSubvention=false is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_true_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid.getId(), "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_OFFERS]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true for all Paymodes")
    public void testProcessDiscoveryLiteResponseTrue() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.discoveryLiteResponse", true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        // Verify the paymentDetails
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
        for (Map<String, Object> detail : paymentDetails) {
            Assertions.assertThat(detail.get("issuingBank")).isNotNull();
            Assertions.assertThat(detail.get("payMethod")).isNotNull();
            Assertions.assertThat(detail.get("benefitText")).isNotNull();
            // Verify the offerDetails
            Map<String, Object> offerDetails = (Map<String, Object>) detail.get("offerDetails");
            List<Map<String, Object>> emiOfferDetails = (List<Map<String, Object>>) offerDetails.get("emiOfferDetails");
            for (Map<String, Object> emiOfferDetail : emiOfferDetails) {
                Assertions.assertThat(emiOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("emiType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("subventionType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validUpto")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validFrom")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerDescription")).isNotNull();
            }
            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) offerDetails.get("bankOfferDetails");
            for (Map<String, Object> bankOfferDetail : bankOfferDetails) {
                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerDescription")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("additionalInfo")).get("is6DigitBin")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("title")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("text")).isNotNull();

            }
        }

    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is False for all paymodes")
    public void testProcessDiscoveryLiteResponseFalse() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.discoveryLiteResponse", false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true for DEBITCARD")
    public void testProcessDiscoveryLiteResponseTrueForDebitCard() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse", true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
        for (Map<String, Object> detail : paymentDetails) {
            Assertions.assertThat(detail.get("issuingBank")).isNotNull();
            Assertions.assertThat(detail.get("payMethod")).isEqualTo("DEBIT_CARD");
            Assertions.assertThat(detail.get("benefitText")).isNotNull();
            Map<String, Object> offerDetails = (Map<String, Object>) detail.get("offerDetails");

            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) offerDetails.get("bankOfferDetails");
            for (Map<String, Object> bankOfferDetail : bankOfferDetails) {
                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerDescription")).isNotNull();

                Map<String, Object> additionalInfo = (Map<String, Object>) bankOfferDetail.get("additionalInfo");
                Assertions.assertThat(additionalInfo.get("is6DigitBin")).isNotNull();

                Map<String, Object> offerDataResult = (Map<String, Object>) bankOfferDetail.get("offerDataResult");
                Assertions.assertThat(offerDataResult.get("title")).isNotNull();
                Assertions.assertThat(offerDataResult.get("text")).isNotNull();
            }
        }
    }



    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true for EMI Item Based")
    public void testProcessDiscoveryLiteResponseTrueForEMIItembased() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body, referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        // Verify the paymentDetails
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
        for (Map<String, Object> detail : paymentDetails) {
            Assertions.assertThat(detail.get("issuingBank")).isNotNull();
            Assertions.assertThat(detail.get("payMethod")).isEqualTo("EMI");
            Assertions.assertThat(detail.get("benefitText")).isNotNull();
            // Verify the offerDetails
            Map<String, Object> offerDetails = (Map<String, Object>) detail.get("offerDetails");
            List<Map<String, Object>> emiOfferDetails = (List<Map<String, Object>>) offerDetails.get("emiOfferDetails");
            for (Map<String, Object> emiOfferDetail : emiOfferDetails) {
                Assertions.assertThat(emiOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("emiType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("subventionType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validUpto")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validFrom")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerDescription")).isNotNull();
            }
            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) offerDetails.get("bankOfferDetails");
            for (Map<String, Object> bankOfferDetail : bankOfferDetails) {
                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerDescription")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("additionalInfo")).get("is6DigitBin")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("title")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("text")).isNotNull();

            }
        }
    }
    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true and AmountBasedPromo and AmountBasedSubvention is by default true")
    public void testProcessDiscoveryLiteResponseTrueAmountBasedPromoBydefaultTrue() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items[0]")
                .deleteContext("body.amountBasedBankOffer")
                .deleteContext("body.amountBasedSubvention")
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        // Verify the paymentDetails
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
        for (Map<String, Object> detail : paymentDetails) {
            Assertions.assertThat(detail.get("issuingBank")).isNotNull();
            Assertions.assertThat(detail.get("payMethod")).isEqualTo("CREDIT_CARD");
            Assertions.assertThat(detail.get("benefitText")).isNotNull();
            // Verify the offerDetails
            Map<String, Object> offerDetails = (Map<String, Object>) detail.get("offerDetails");
            List<Map<String, Object>> emiOfferDetails = (List<Map<String, Object>>) offerDetails.get("emiOfferDetails");
            for (Map<String, Object> emiOfferDetail : emiOfferDetails) {
                Assertions.assertThat(emiOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("emiType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("subventionType")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validUpto")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("validFrom")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(emiOfferDetail.get("offerDescription")).isNotNull();
            }
            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) offerDetails.get("bankOfferDetails");
            for (Map<String, Object> bankOfferDetail : bankOfferDetails) {
                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                Assertions.assertThat(bankOfferDetail.get("offerDescription")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("additionalInfo")).get("is6DigitBin")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("title")).isNotNull();
                Assertions.assertThat(((Map<String, Object>) bankOfferDetail.get("offerDataResult")).get("text")).isNotNull();

            }
        }

    }
    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is False and AmountBasedPromo and AmountBasedSubvention is by default true")
    public void testProcessDiscoveryLiteResponseFalseAmountBasedPromoBydefaultTrue() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.items[0]")
                .deleteContext("body.amountBasedBankOffer")
                .deleteContext("body.amountBasedSubvention")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when Item Object Passed but amountBasedBankOffer and amountBasedSubvention is not provided in request")
    public void testThatDiscoveryShouldFailWhenItemObjectPassed() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.amountBasedBankOffer")
                .deleteContext("body.amountBasedSubvention")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when Item Object Passed but amountBasedBankOffer and amountBasedSubvention is  provided true in request")
    public void testThatDiscoveryShouldFailWhenItemObjectPassed2() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
    }
    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when Item Object Passed but amountBasedBankOffer:false and amountBasedSubvention is not provided in request")
    public void testThatDiscoveryShouldFailWhenItemObjectPassed3() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .setContext("body.amountBasedBankOffer","false")
                .deleteContext("body.amountBasedSubvention")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when Item Object Passed but amountBasedSubvention:false and amountBasedBankOffer is not provided in request")
    public void testThatDiscoveryShouldFailWhenItemObjectPassed4() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body,referenceId)
                .setContext("head.token", AccessToken)
                .deleteContext("body.amountBasedBankOffer")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }
}