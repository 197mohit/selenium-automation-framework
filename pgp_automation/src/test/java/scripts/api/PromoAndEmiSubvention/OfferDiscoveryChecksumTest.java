package scripts.api.PromoAndEmiSubvention;
import com.paytm.api.CreateToken;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferDiscovery;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.*;
public class OfferDiscoveryChecksumTest extends PGPBaseTest {
    String emi_body = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"token\": \"e/iKoK/nhfyFKvYEgFIoEPlctaba82T9+ukbeZE69vy27pihXKPCJa3c8MPwQon1+LB6Y1qPzjAUYe2GLw9bbMUEqzeJRPq7infrEzLzD7I=\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}\n" +
            "}";
    String emi_body_discoveryLite = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"token\": \"e/iKoK/nhfyFKvYEgFIoEPlctaba82T9+ukbeZE69vy27pihXKPCJa3c8MPwQon1+LB6Y1qPzjAUYe2GLw9bbMUEqzeJRPq7infrEzLzD7I=\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"discoveryLiteResponse\":\"true\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}\n" +
            "}";
    String single_paymode_body = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"token\": \"e/iKoK/nhfyFKvYEgFIoEPlctaba82T9+ukbeZE69vy27pihXKPCJa3c8MPwQon1+LB6Y1qPzjAUYe2GLw9bbMUEqzeJRPq7infrEzLzD7I=\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}\n" +
            "}";
    String allPaymode_body = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"token\": \"e/iKoK/nhfyFKvYEgFIoEPlctaba82T9+ukbeZE69vy27pihXKPCJa3c8MPwQon1+LB6Y1qPzjAUYe2GLw9bbMUEqzeJRPq7infrEzLzD7I=\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"NET_BANKING\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]}]}}\n" +
            "}";
    String fourPaymode_body = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"token\": \"e/iKoK/nhfyFKvYEgFIoEPlctaba82T9+ukbeZE69vy27pihXKPCJa3c8MPwQon1+LB6Y1qPzjAUYe2GLw9bbMUEqzeJRPq7infrEzLzD7I=\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}\n" +
            "}";

    String emi_body_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}";
    String emi_body_dc_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}";
    String emi_body_checksum_disoveryLite="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"discoveryLiteResponse\":true,\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}";

    String single_paymode_body_cc_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}";
    String single_paymode_body_dc_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}";
    String single_paymode_body_nb_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"NET_BANKING\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}";
    String single_paymode_body_incorrect_mid_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"jhsgdfuicbc\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}";
    String allPaymode_body_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"NET_BANKING\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]}]}}";
    String allPaymode_body_without_item_checksum="{\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"true\",\"amountBasedSubvention\":\"true\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"NET_BANKING\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]}]}}";
    String allPaymode_body_negative_price_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":\"-1\"}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":-1,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"NET_BANKING\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"unit\":\"MONTH\",\"value\":3}]}]}}";

    String fourPaymode_body_checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"DEBIT_CARD\",\"issuingBank\":\"HDFC\"},{\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]},{\"payMethod\":\"EMI_DC\",\"issuingBank\":\"ICICI\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]}}";
    String requestWithUserDetails="{\"head\":{\"channelId\":\"WEB\",\"token\":\"L4zsnDmaDIOzEvAMca67LkDbm0+nPMRCEzaEBhHSQHKKt8rjwd63nYU2xCkyCkrM0wg7w7R7RG74gUpuA5+FHwhBnmKY+GmhI/8YAfxD6uk=\",\"tokenType\":\"CHECKSUM\"},\"body\":{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"userDetails\":{\"custId\":\"1000036031\",\"paytmUserId\":\"1000036031\"},\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"NET_BANKING\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}}";
    String requestWithUserDetails_Checksum="{\"items\":[{\"id\":\"1234586674\",\"productId\":\"P12\",\"brandId\":\"10002\",\"categoryId\":\"10001\",\"price\":10000.0}],\"userDetails\":{\"custId\":\"1000036031\",\"paytmUserId\":\"1000036031\"},\"mid\":\"qa12FU97229952596781\",\"amountBasedBankOffer\":\"false\",\"amountBasedSubvention\":\"false\",\"paymentDetails\":{\"orderAmount\":10000.0,\"paymentOptions\":[{\"payMethod\":\"NET_BANKING\",\"issuingNetworkCode\":\"MASTERCARD\"}]}}";
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when all  Paymodes are povided")
    public void TestSuccessResponseWhenAllPaymodesAreProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),allPaymode_body_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", checksum);
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
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_dc_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
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
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_cc_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
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
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_nb_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI")
    public void TestSuccessResponseWhenEMIProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),emi_body_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", checksum);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.issuingBank")).isEqualTo("[HDFC]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankName")).isEqualTo("[HDFC Bank]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.payMethod")).isEqualTo("[EMI]");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isNotNull();
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when Paymode is EMI_DC")
    public void TestSuccessResponseWhenEMIDCProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),emi_body_dc_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body)
                .setContext("head.token", checksum)
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi")).isNotNull();
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
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when more then 1 Paymode in request")
    public void TestSuccessResponseWhen4PaymodesProvided() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),fourPaymode_body_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(fourPaymode_body)
                .setContext("head.token", checksum);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].bankName")).isEqualTo("HDFC Bank");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].payMethod")).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.amountBearer.brand")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items.offerDetails.bankOfferDetails[0].gratifications.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails.items[0].offerDetails.bankOfferDetails[0].gratifications.info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].bankName")).isEqualTo("HDFC Bank");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[1].payMethod")).isEqualTo("DEBIT_CARD");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].issuingBank")).isEqualTo("HDFC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].bankName")).isEqualTo("HDFC Bank");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].payMethod")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].tenure.roi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].planId")).isEqualTo("HDFC|3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].minPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].maxPrice")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0]")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validUpto")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].validFrom")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].tnc")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[2].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].issuingBank")).isEqualTo("ICICI");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].bankName")).isEqualTo("ICICI Bank");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].payMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].bankLogo")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].benefitText")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.value")).isEqualTo("3");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.unit")).isEqualTo("MONTH");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].tenure.roi")).isEqualTo("16.01");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].effectiveAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].loanAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].originalAmount")).isEqualTo("10000.0");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].payableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].totalPayableAmount")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].planId")).isEqualTo("ICICI|3");
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
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.type")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.value")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].info.cap")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[3].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].amountBearer.bank")).isNotNull();
    }
    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "verify the api response when All Paymode are provided and Items are not provied")
    public void TestSuccessResponseWhenAllPaymodesAreProvidedItemsNotPassed() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),allPaymode_body_without_item_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", checksum)
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
    @Test(description="Verify the api response when channelId is WAP")
    public void TestSuccessResponseWhenChannelWAP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),allPaymode_body_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", checksum)
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
        String checksum= PGPUtil.getChecksum(mid.getKey(),allPaymode_body_negative_price_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(allPaymode_body)
                .setContext("head.token", checksum)
                .setContext("body.items[0].price", "-1")
                .setContext("body.paymentDetails.orderAmount", -1);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when incorrect MID id provided in the request")
    public void TestSuccessResponseWhenIncorrectMIDPass() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_incorrect_mid_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
                .setContext("body.mid", "jhsgdfuicbc");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Mid is invalid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("2006");
    }

    @Owner(SHWETANK)
    @Feature("PGP-44841")
    @Test(description = "Verify the api response when token type is null")
    public void TestfailureResponseWhenTokenTypeIsNull() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_cc_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
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
        String checksum= PGPUtil.getChecksum(mid.getKey(),single_paymode_body_cc_checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(single_paymode_body)
                .setContext("head.token", checksum)
                .setContext("head.channelId", null);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
    }
    @Owner("Nirottam")
    @Feature("PGP-49321")
    @Test(description = "verify Successfull api response when UserDetails is passed in request")
    public void TestSuccessResponseWithUserDetails() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),requestWithUserDetails_Checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(requestWithUserDetails)
                .setContext("head.token", checksum)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");

    }
    @Owner("Nirottam")
    @Feature("PGP-49321")
    @Test(description = "verify Successfull api response  and offerText when UserDetails is passed in request")
    public void TestSuccessResponseAndOfferTextWithUserDetails() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),requestWithUserDetails_Checksum);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(requestWithUserDetails)
                .setContext("head.token", checksum)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank");
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails.offerDetails.items[0].offerDetails.bankOfferDetails[0].offerText")).isNotNull();

    }

    @Owner(KARMVIR)
    @Feature("PGP-49051")
    @Test(description = "Verify the api response when discoveryLiteResponse is true for EMI Paymodes")
    public void testProcessDiscoveryLiteResponseTrue() {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String checksum= PGPUtil.getChecksum(mid.getKey(),emi_body_checksum_disoveryLite);
        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(emi_body_discoveryLite)
                .setContext("head.token", checksum)
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
}