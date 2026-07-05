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
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.paytm.appconstants.Constants.Owner.*;
public class OfferDiscoveryTxnTokenTest extends PGPBaseTest {
    String emi_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
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
    String emi_body_with_userDetails = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"userDetails\": {\n" +
            "            \"custId\": \"1704126101\"\n" +
            "        },"+
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
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
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
            "\"paymentOptions\": [" +
            "{\n" +
            "\"payMethod\": \"CREDIT_CARD\",\n" +
            "\"issuingBank\": \"HDFC\",\n" +
            "\"issuingNetworkCode\": \"MASTERCARD\",\n" +
            "}" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}";
    String allPaymode_body = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
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
    String allPaymode_body_upi = "{\n" +
            "\"head\": {\n" +
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
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
            "\"payMethod\": \"UPI\",\n" +
            "\"issuingBank\": \"ALL\",\n" +
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
            "\"tokenType\": \"TXN_TOKEN\",\n" +
            "\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
            "\"channelId\": \"WEB\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"items\": [{\n" +
            "\"id\": \"1234586674\",\n" +
            "\"productId\": \"P12\",\n" +
            "\"brandId\": \"10002\",\n" +
            "\"categoryId\": \"10001\",\n" +
            "\"price\": 10000.00,\n" +
            "}],\n" +
            "\"mid\": \"qa12FU97229952596781\",\n" +
            "\"amountBasedBankOffer\": \"false\",\n" +
            "\"amountBasedSubvention\": \"false\",\n" +
            "\"paymentDetails\": {\n" +
            "\"orderAmount\": 10000.00,\n" +
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken);
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isEqualTo("[[[1000.0], [0.0]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isEqualTo("[[[[1000.0], [900.0]]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isEqualTo("[[[discount]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isEqualTo("[[[percentage]]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isEqualTo("[[[10.0]]]");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI")
    public void TestSuccessResponseWhenEMIProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken);
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].maxEmiAmount").equals("10000.0"));
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].minEmiAmount").equals("100.0"));
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI_DC")
    public void TestSuccessResponseWhenEMIDCProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].maxEmiAmount").equals("10000.0"));
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].minEmiAmount").equals("100.0"));
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when more then 1 Paymode in request")
    public void TestSuccessResponseWhen4PaymodesProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(fourPaymode_body)
                .setContext("head.token", txnToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();

        // Assert the result message
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        // Get the payment details list
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");

        // Iterate through paymentDetails using streams
        paymentDetails.forEach(paymentDetail -> {
            String payMethod = (String) paymentDetail.get("payMethod");
            Assertions.assertThat(payMethod).isNotNull();

            // Common assertions for all payment methods
            Assertions.assertThat(paymentDetail.get("issuingBank")).isEqualTo("HDFC");
            Assertions.assertThat(paymentDetail.get("bankName")).isEqualTo("HDFC Bank");
            Assertions.assertThat(paymentDetail.get("bankLogo")).isNotNull();
            Assertions.assertThat(paymentDetail.get("benefitText")).isNotNull();
            Assertions.assertThat(paymentDetail.get("offerDetails")).isNotNull();

            if ("CREDIT_CARD".equals(payMethod) || "DEBIT_CARD".equals(payMethod)) {
                // Assertions for CREDIT_CARD or DEBIT_CARD
                List<Map<String, Object>> offerDetails = (List<Map<String, Object>>) paymentDetail.get("offerDetails");
                offerDetails.forEach(offerDetail -> {
                    Assertions.assertThat(offerDetail.get("effectiveAmount")).isNotNull();
                    Assertions.assertThat(offerDetail.get("originalAmount")).isNotNull();
                    Assertions.assertThat(offerDetail.get("payableAmount")).isNotNull();
                    Assertions.assertThat(offerDetail.get("totalPayableAmount")).isNotNull();

                    List<Map<String, Object>> items = (List<Map<String, Object>>) offerDetail.get("items");
                    items.forEach(item -> {
                        Map<String, Object> itemOfferDetails = (Map<String, Object>) item.get("offerDetails");
                        Assertions.assertThat(itemOfferDetails).isNotNull();

                        List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("bankOfferDetails");
                        Assertions.assertThat(bankOfferDetails).isNotNull();
                        bankOfferDetails.forEach(bankOfferDetail -> {
                            Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();

                            List<Map<String, Object>> gratifications = (List<Map<String, Object>>) bankOfferDetail.get("gratifications");
                            Assertions.assertThat(gratifications).isNotNull();
                            gratifications.forEach(gratification -> {
                                Assertions.assertThat(gratification.get("value")).isNotNull();
                                Assertions.assertThat(gratification.get("type")).isNotNull();
                                Map<String, Object> info = (Map<String, Object>) gratification.get("info");
                                Assertions.assertThat(info).isNotNull();
                                Assertions.assertThat(info.get("type")).isNotNull();
                                Assertions.assertThat(info.get("value")).isNotNull();
                                Assertions.assertThat(info.get("cap")).isNotNull();
                                Assertions.assertThat(gratification.get("amountBearer")).isNotNull();
                            });
                        });
                    });
                });
            } else if ("EMI".equals(payMethod) || "EMI_DC".equals(payMethod)) {
                // Assertions for EMI or EMI_DC
                List<Map<String, Object>> tenureDetails = (List<Map<String, Object>>) paymentDetail.get("tenureDetails");
                Assertions.assertThat(tenureDetails).isNotNull();
                tenureDetails.forEach(tenureDetail -> {
                    Map<String, Object> tenure = (Map<String, Object>) tenureDetail.get("tenure");
                    Assertions.assertThat(tenure).isNotNull();
                    Assertions.assertThat(tenure.get("value")).isNotNull();
                    Assertions.assertThat(tenure.get("unit")).isNotNull();
                    Assertions.assertThat(tenure.get("roi")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("effectiveAmount")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("loanAmount")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("originalAmount")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("payableAmount")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("totalPayableAmount")).isNotNull();
                    Assertions.assertThat(tenureDetail.get("planId")).isNotNull();

                    List<Map<String, Object>> items = (List<Map<String, Object>>) tenureDetail.get("items");
                    Assertions.assertThat(items).isNotNull();
                    items.forEach(item -> {
                        Map<String, Object> itemOfferDetails = (Map<String, Object>) item.get("offerDetails");
                        Assertions.assertThat(itemOfferDetails).isNotNull();

                        List<Map<String, Object>> emiOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("emiOfferDetails");
                        Assertions.assertThat(emiOfferDetails).isNotNull();
                        emiOfferDetails.forEach(emiOfferDetail -> {
                            Assertions.assertThat(emiOfferDetail.get("emiType")).isNotNull();
                            Assertions.assertThat(emiOfferDetail.get("emi")).isNotNull();
                            Assertions.assertThat(emiOfferDetail.get("interest")).isNotNull();
                            Assertions.assertThat(emiOfferDetail.get("minPrice")).isNotNull();
                            Assertions.assertThat(emiOfferDetail.get("maxPrice")).isNotNull();
                            Assertions.assertThat(emiOfferDetail.get("offerText")).isNull();
                        });

                        List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("bankOfferDetails");
                        Assertions.assertThat(bankOfferDetails).isNotNull();
                        bankOfferDetails.forEach(bankOfferDetail -> {
                            Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("validUpto")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("validFrom")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                            Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();

                            List<Map<String, Object>> gratifications = (List<Map<String, Object>>) bankOfferDetail.get("gratifications");
                            Assertions.assertThat(gratifications).isNotNull();
                            gratifications.forEach(gratification -> {
                                Assertions.assertThat(gratification.get("value")).isNotNull();
                                Assertions.assertThat(gratification.get("type")).isNotNull();
                                Map<String, Object> info = (Map<String, Object>) gratification.get("info");
                                Assertions.assertThat(info).isNotNull();
                                Assertions.assertThat(info.get("type")).isNotNull();
                                Assertions.assertThat(info.get("value")).isNotNull();
                                Assertions.assertThat(info.get("cap")).isNotNull();
                                Assertions.assertThat(gratification.get("amountBearer")).isNotNull();
                            });
                        });
                    });
                });
            }
        });
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when All Paymode are provided and Items are not provied")
    public void TestSuccessResponseWhenAllPaymodesAreProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.items");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isEqualTo("[[3], [3, 6, 9, 12]]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is Debit card and Items not passed")
    public void TestSuccessResponseWhenDCProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token",txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD"  );
        JsonPath jsonPath= offerDiscovery.execute().jsonPath();
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token",txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD"  );
        JsonPath jsonPath= offerDiscovery.execute().jsonPath();
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token",txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING"  );
        JsonPath jsonPath= offerDiscovery.execute().jsonPath();
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("10000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.items");
        JsonPath jsonPath= offerDiscovery.execute().jsonPath();
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();;
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();;
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isEqualTo("1.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId")).isEqualTo("ICICI|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isNotNull();
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
    @Test(description = "verify the api response when more then 1 Paymode is provided and Items are not provided")
    public void TestSuccessResponseWhen4PaymodesProvidedItemsNotPassed() throws Exception {
            Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                    .setTxnValue("10000.00")
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(fourPaymode_body)
                    .setContext("head.token", txnToken)
                    .deleteContext("body.items")
                    .setContext("body.amountBasedBankOffer", "true")
                    .setContext("body.amountBasedSubvention", "true");
            JsonPath jsonPath = offerDiscovery.execute().jsonPath();

            // Assert the result message
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

            // Get the payment details list
            List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");

            // Iterate through paymentDetails using streams
            paymentDetails.forEach(paymentDetail -> {
                String payMethod = (String) paymentDetail.get("payMethod");
                Assertions.assertThat(payMethod).isNotNull();

                if (!"EMI_DC".equals(payMethod) && !"EMI".equals(payMethod)) {
                    // Assertions for payment methods other than EMI_DC and EMI
                    List<Map<String, Object>> offerDetails = (List<Map<String, Object>>) paymentDetail.get("offerDetails");
                    Assertions.assertThat(offerDetails).isNotNull();
                    offerDetails.forEach(offerDetail -> {
                        Assertions.assertThat(offerDetail.get("effectiveAmount")).isNotNull();
                        List<Map<String, Object>> items = (List<Map<String, Object>>) offerDetail.get("items");
                        Assertions.assertThat(items).isNotNull();
                        items.forEach(item -> {
                            Map<String, Object> itemOfferDetails = (Map<String, Object>) item.get("offerDetails");
                            Assertions.assertThat(itemOfferDetails).isNotNull();
                            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("bankOfferDetails");
                            Assertions.assertThat(bankOfferDetails).isNotNull();
                            bankOfferDetails.forEach(bankOfferDetail -> {
                                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("offerId")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("promocode")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                                List<Map<String, Object>> gratifications = (List<Map<String, Object>>) bankOfferDetail.get("gratifications");
                                Assertions.assertThat(gratifications).isNotNull();
                                gratifications.forEach(gratification -> {
                                    Assertions.assertThat(gratification.get("amountBearer")).isNotNull();
                                    Assertions.assertThat(gratification.get("value")).isNotNull();
                                    Assertions.assertThat(gratification.get("type")).isNotNull();
                                    Map<String, Object> info = (Map<String, Object>) gratification.get("info");
                                    Assertions.assertThat(info).isNotNull();
                                    Assertions.assertThat(info.get("type")).isNotNull();
                                    Assertions.assertThat(info.get("value")).isNotNull();
                                });
                            });
                        });
                    });
                } else {
                    // Assertions for EMI_DC or EMI payment methods
                    List<Map<String, Object>> tenureDetails = (List<Map<String, Object>>) paymentDetail.get("tenureDetails");
                    Assertions.assertThat(tenureDetails).isNotNull();
                    tenureDetails.forEach(tenureDetail -> {
                        Map<String, Object> tenure = (Map<String, Object>) tenureDetail.get("tenure");
                        Assertions.assertThat(tenure).isNotNull();
                        Assertions.assertThat(tenure.get("value")).isNotNull();
                        Assertions.assertThat(tenure.get("unit")).isNotNull();
                        Assertions.assertThat(tenure.get("roi")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("effectiveAmount")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("loanAmount")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("originalAmount")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("payableAmount")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("totalPayableAmount")).isNotNull();
                        Assertions.assertThat(tenureDetail.get("planId")).isNotNull();
                        List<Map<String, Object>> items = (List<Map<String, Object>>) tenureDetail.get("items");
                        Assertions.assertThat(items).isNotNull();
                        items.forEach(item -> {
                            Map<String, Object> itemOfferDetails = (Map<String, Object>) item.get("offerDetails");
                            Assertions.assertThat(itemOfferDetails).isNotNull();
                            List<Map<String, Object>> emiOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("emiOfferDetails");
                            Assertions.assertThat(emiOfferDetails).isNotNull();
                            emiOfferDetails.forEach(emiOfferDetail -> {
                                Assertions.assertThat(emiOfferDetail.get("emiType")).isNotNull();
                                Assertions.assertThat(emiOfferDetail.get("emi")).isNotNull();
                                Assertions.assertThat(emiOfferDetail.get("interest")).isNotNull();
                                Assertions.assertThat(emiOfferDetail.get("minPrice")).isNotNull();
                                Assertions.assertThat(emiOfferDetail.get("maxPrice")).isNotNull();
                                Assertions.assertThat(emiOfferDetail.get("offerText")).isNull();
                            });
                            List<Map<String, Object>> bankOfferDetails = (List<Map<String, Object>>) itemOfferDetails.get("bankOfferDetails");
                            Assertions.assertThat(bankOfferDetails).isNotNull();
                            bankOfferDetails.forEach(bankOfferDetail -> {
                                Assertions.assertThat(bankOfferDetail.get("type")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("validUpto")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("validFrom")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("tnc")).isNotNull();
                                Assertions.assertThat(bankOfferDetail.get("offerText")).isNotNull();
                                List<Map<String, Object>> gratifications = (List<Map<String, Object>>) bankOfferDetail.get("gratifications");
                                Assertions.assertThat(gratifications).isNotNull();
                                gratifications.forEach(gratification -> {
                                    Assertions.assertThat(gratification.get("value")).isNotNull();
                                    Assertions.assertThat(gratification.get("type")).isNotNull();
                                    Map<String, Object> info = (Map<String, Object>) gratification.get("info");
                                    Assertions.assertThat(info).isNotNull();
                                    Assertions.assertThat(info.get("type")).isNotNull();
                                    Assertions.assertThat(info.get("value")).isNotNull();
                                    Assertions.assertThat(info.get("cap")).isNotNull();
                                    Assertions.assertThat(gratification.get("amountBearer")).isNotNull();
                                });
                            });
                        });
                    });
                }
            });
        }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description="Verify the api response when channelId is WAP")
    public void TestSuccessResponseWhenChannelWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken)
                .setContext("head.channelId", "WAP");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when negative Price and amount is provided in the request")
    public void TestSuccessResponseWhenNegativePricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token",txnToken)
                .setContext("body.items[0].price","0.5")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.paymentDetails.orderAmount",0.5);
        JsonPath jsonPath= offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when price and amount is provided to 1 decimal ")
    public void TestSuccessResponseWhenOneDecimalPricePass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1.5")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", "jhsgdfuicbc");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1006");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when Price and amount is different in the request")
    public void TestSuccessResponseWhenPriceAndAmountIsDifferent() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.orderAmount", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Only FlowType 1 is supported for all-offers request .");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when paymentOptions is null in paymentDetails object")
    public void TestfailureResponseWhenpaymentOptionsIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("payMethod can't be null or blank");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when invalid tokenType is passed")
    public void verifyForInvalidTokenType() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.tokenType", "TXN_TOKENNNN")
                .setContext("head.token", txnToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("System error");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("00000900");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when ChannelID is other then WEB/WAP")
    public void verifyErrorwhenChannelIDIsNotValid() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.channelId", "WAPPP")
                .setContext("head.token", txnToken);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("System error");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("00000900");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify error when wrong productId, brandID and categoryId is passed")
    public void verifyWhenWrongProductIDAndCategoryIDisPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.items[0].categoryId","00000")
                .setContext("body.items[0].brandId","00000")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.items[0].quantity","2");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when different value of IssuingNetworkCode is passed")
    public void TestSuccessResponseForDifferentValueOfIssuingNetworkCode() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","10000.00")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","10000.00")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","15036688")
                .setContext("body.items[0].price","10000.00")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be RETAILER when amountBasedBankOffer=true & amountBasedSubvention=true is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be BRAND when amountBasedBankOffer=true & amountBasedSubvention=true is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[BRAND]");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that discovery API  should give error when amountBasedBankOffer=false & amountBasedSubvention=false is  passed without item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_without_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.items")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_SUBVENTION]");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46696")
    @Test(description = "Verify that Flow Type should be AMOUNT_BASED_OFFERS when amountBasedBankOffer=true & amountBasedSubvention=false is passed with item object in it")
    public void Test_api_response_when_both_amountBasedBankOffer_true_and_amountBasedSubvention_false_with_item_Passsed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_OFFERS]");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true for all Paymodes")
    public void testProcessDiscoveryLiteResponseTrue() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();

    }
    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is False for all paymodes")
    public void testProcessDiscoveryLiteResponseFalse() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.discoveryLiteResponse",false);
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",true);
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.discoveryLiteResponse",true);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        // Verify the paymentDetails
        List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
        for (Map<String, Object> detail : paymentDetails) {
            Assertions.assertThat(detail.get("issuingBank")).isNotNull();
            Assertions.assertThat(detail.get("bankName")).isNotNull();
            Assertions.assertThat(detail.get("bankLogo")).isNotNull();
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
                InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                        .setTxnValue("1000.00")
                        .build();
                String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
                OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                        .setContext("head.token", txnToken)
                        .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                        .setContext("body.discoveryLiteResponse", true);
                JsonPath jsonPath = offerDiscovery.execute().jsonPath();
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                // Verify the paymentDetails
                List<Map<String, Object>> paymentDetails = jsonPath.getList("body.paymentDetails");
                for (Map<String, Object> detail : paymentDetails) {
                    Assertions.assertThat(detail.get("issuingBank")).isNotNull();
                    Assertions.assertThat(detail.get("bankName")).isNotNull();
                    Assertions.assertThat(detail.get("bankLogo")).isNotNull();
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .deleteContext("body.amountBasedBankOffer")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.discoveryLiteResponse",false);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showKFSLink and showLendingConsent are returned in response")
    public void Test_showKFSLink_showLendingConsent_returned_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[ICICI Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showKFSLink is returned as true in response")
    public void Test_showKFSLink_returned_true_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[ICICI Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[true]");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showLendingConsent is returned as true in response for banks set in theia.lending.consent.enabled.banks")
    public void Test_showLendingConsent_returned_true_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[ICICI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[ICICI Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[true]");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is EMI_DC showLendingConsent is returned as false in response for banks not set in theia.lending.consent.enabled.banks")
    public void Test_showLendingConsent_returned_false_for_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","HDFC");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI_DC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[true]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[false]");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-53624")
    @Test(description = "Verify that when Paymethod is not EMI_DC showKFSLink and showLendingConsent are returned as false in response")
    public void Test_showKFSLink_showLendingConsent_returned_false_for_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","HDFC");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showKFSLink")).isEqualTo("[false]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.showLendingConsent")).isEqualTo("[false]");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the Discovery Response when Multiple Items are sent for Paymode - EMI/EMI_DC")
    public void TestSuccessOfferDiscoverywithMultipleItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2200.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Verify the Discovery Response when Multiple Items are sent for Paymode - CC/DC/NB")
    public void TestSuccessOfferDiscoverywithMultipleItem_CC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2200.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("CREDIT_CARD");
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
    @Test(description = "Verify the Discovery Response when Multiple Items are sent for Paymode - UPI")
    public void TestSuccessOfferDiscoverywithMultipleItem_UPI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2200.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("UPI");
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
    @Test(description = "Verify the Discovery Response when Multiple Items are sent for all Paymodes - EMI/EMI_DC/CC/DC/NB/UPI")
    public void TestSuccessOfferDiscoverywithMultipleItem_All() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body_upi)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2200.00)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("UPI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("CREDIT_CARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("DEBIT_CARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("NET_BANKING");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.unifiedGratifications")).isNotNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify the Discovery Response for Amount Based Subvention and Single Item based Offers")
    public void TestSuccessOfferDiscovery_AmtBasedSubvention_ItemBasedOffers_SingleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "212312", "25612", 800.00, "25512");
        items.add(item1);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",800.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Verify the Discovery Response for Amount Based Subvention and Multiple Item based Offers")
    public void TestSuccessOfferDiscovery_AmtBasedSubvention_ItemBasedOffers_MultipleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "212312", "25612", 400.00, "25512");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "212312", "25612", 400.00, "25512");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",800.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Verify the Discovery Response for Amount Based Offers and Single Item based Subvention")
    public void TestSuccessOfferDiscovery_ItemBasedSubvention_AmtBasedOffers_SingleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "12312", "5612", 800.00, "5512");
        items.add(item1);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",800.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Verify the Discovery Response for Amount Based Offers and Multiple Item based Subvention")
    public void TestSuccessOfferDiscovery_ItemBasedSubvention_AmtBasedOffers_MultipleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "12312", "5612", 400.00, "5512");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "12312", "5612", 400.00, "5512");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",800.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Verify the Discovery Response for Amount Based Offers and Subvention")
    public void TestSuccessOfferDiscovery_AmtBasedSubvention_AmtBasedOffers_MultipleItem() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",800.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.amountBasedSubvention",true)
                .setContext("body.items",null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.tenure.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.originalAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.planId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].id")).containsIgnoringCase("001");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.emiOfferDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.tenureDetails.items.offerDetails.bankOfferDetails")).isNotNull();
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
    @Test(description = "Partial Amount Offers and Subvention for Single Item")
    public void TestSuccessOfferDiscoverywithPartialAmountSingleItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 2000.00, "6224");
        items.add(item1);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2000.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount",1200)
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount",1200)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).contains("EMI");
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
    @Test(description = "Partial Amount Offers and Subvention for Multi Item")
    public void TestFailureOfferDiscoverywithPartialAmountMultipleItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1339.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1339.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2678)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount",1100)
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount",1100)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Single Item is Allowed For Partial Amount ");

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "When more than 5 items are sent")
    public void TestFailureOfferDiscovery_MoreThan5Items() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item5 = new SimplifiedUnifiedOffers.Items("Item005_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item6 = new SimplifiedUnifiedOffers.Items("Item006_" + orderId, "123", "18084", 1100.00, "6224");

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",6600)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "If brandId/categoryId has special characters, the transaction has to be failed at Theia")
    public void TestFailureOfferDiscovery_brandIdCategoryIdHasSpecialCharacters() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("1000.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084@", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224@");
        items.add(item1);
        items.add(item2);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", txnToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.paymentDetails.orderAmount",2200.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank",null)
                .setContext("body.items",items);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - gratifications.value is not equal to sum of amountBearer")
    public void Test_Error_Response_01() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43001";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E008");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Offer is not applicable");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - channel is required in request header")
    public void Test_Error_Response_02() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43002";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("channel is required in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - token is invalid in request header")
    public void Test_Error_Response_03() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43003";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("token is invalid in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items can't be null or empty")
    public void Test_Error_Response_04() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43004";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("items can't be null or empty");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items.price should be equal to or greater than paymentDetails.originalAmount")
    public void Test_Error_Response_05() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.paymentDetails.orderAmount", "15000.0")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - quantity must be equal to 1")
    public void Test_Error_Response_06() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                //.setContext("body.userDetails.custId", custId)
                .setContext("body.items[0].quantity",2)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - productId is Mandatory for flow type BRAND")
    public void Test_Error_Response_07() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43007";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - productCode can't be null or blank")
    public void Test_Error_Response_08() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43008";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.boTransactionAmount can't be greater than originalAmount")
    public void Test_Error_Response_09() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43009";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("PromoAmount cannot be greater than Order Amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.emiTransactionAmount can't be greater than originalAmount")
    public void Test_Error_Response_10() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43010";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E005");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SubventionAmount cannot be greater than order amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - No plans found. Invalid tenure details")
    public void Test_Error_Response_11() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.mid", mid.getId())
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "NKMB")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - Internal Server Error")
    public void Test_Error_Response_12() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43012";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E007");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("There seems to be a problem in processing. Please try again");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response for Session Expired")
    public void Test_Error_Response_13() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken+"p")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN")
    public void Test_Error_Response_14() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock43014";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - gratifications.value is not equal to sum of amountBearer - Discovery Lite")
    public void Test_Error_Response_01_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53001";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E008");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Offer is not applicable");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - channel is required in request header - Discovery Lite")
    public void Test_Error_Response_02_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53002";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("channel is required in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - token is invalid in request header - Discovery Lite")
    public void Test_Error_Response_03_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53003";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("token is invalid in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items can't be null or empty - Discovery Lite")
    public void Test_Error_Response_04_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53004";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("items can't be null or empty");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items.price should be equal to or greater than paymentDetails.originalAmount - Discovery Lite")
    public void Test_Error_Response_05_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.orderAmount", "15000.0")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - quantity must be equal to 1 - Discovery Lite")
    public void Test_Error_Response_06_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "false")
                .setContext("body.amountBasedSubvention", "false")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.items[0].quantity",2)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - productId is Mandatory for flow type BRAND - Discovery Lite")
    public void Test_Error_Response_07_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53007";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - productCode can't be null or blank - Discovery Lite")
    public void Test_Error_Response_08_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53008";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.boTransactionAmount can't be greater than originalAmount - Discovery Lite")
    public void Test_Error_Response_09_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53009";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("PromoAmount cannot be greater than Order Amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.emiTransactionAmount can't be greater than originalAmount - Discovery Lite")
    public void Test_Error_Response_10_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53010";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E005");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SubventionAmount cannot be greater than order amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - No plans found. Invalid tenure details - Discovery Lite")
    public void Test_Error_Response_11_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.mid", mid.getId())
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "NKMB")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - Internal Server Error - Discovery Lite")
    public void Test_Error_Response_12_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53012";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E007");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("There seems to be a problem in processing. Please try again");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response for Session Expired - Discovery Lite")
    public void Test_Error_Response_13_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken+"p")
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN - Discovery Lite")
    public void Test_Error_Response_14_dicoveryLite() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custId = "Mock53014";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", mid)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_with_userDetails)
                .setContext("head.token", txnToken)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.userDetails.custId", custId)
                .deleteContext("body.items")
                .setContext("body.discoveryLiteResponse",true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
}