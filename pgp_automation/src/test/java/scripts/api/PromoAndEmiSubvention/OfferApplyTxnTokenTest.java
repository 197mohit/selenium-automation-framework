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
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.*;

public class OfferApplyTxnTokenTest extends PGPBaseTest {
    String emi_body_item_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"TXN_TOKEN\",\n" +
            "        \"token\": \"c6bc064c73724c15a70cd31f250e9d611687786709233\"\n" +
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
            "                     \"vpa\": \"\",\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"TXN_TOKEN\",\n" +
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
            "                     \"vpa\": \"\",\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_card_token = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"TXN_TOKEN\",\n" +
            "        \"token\": \"c6bc064c73724c15a70cd31f250e9d611687786709233\"\n" +
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

    String emi_body_with_tenure="{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"TXN_TOKEN\",\n" +
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

    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify item based subvention with cashback bank offer  when token type is TXN token for EMI paymodee")
    public void TestSuccessResponseWhenEMIPaymethodWithCashbackOfferIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token", txnToken)
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
    @Test(description = "Verify amount based subvention with discount bank offer when token type is TXN  for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithDiscountBankOfferIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
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
    @Test(description = "Verify success response when all mandatory params are passed when token type is TXN Token and  paaram txnToken is used instead of token")
    public void TestSuccessResponseWhenEMIPaymethodWithTxnTokenIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .deleteContext("head.token")
                .setContext("head.txnToken", txnToken)
                .setContext("body.custId",custId)
                .setContext("body.mid",mid.getId());
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
    @Test(description = "Verify success response if channelId  provided  is WAP when token type is TXN for EMI paymode")
    public void TestSuccessResponseWhenChannelIdIsProvidedAsWAP() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.channelId", "WAP")
                .setContext("head.token", txnToken)
                .setContext("body.custId",custId)
                .setContext("body.mid",mid.getId());
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
    @Test(description = "Verify error when expired/wrong txn token is provided for EMI paymode")
    public void TestFailedResponseWhenWrongTxnTokenIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token", "123")
                .setContext("body.custId",custId)
                .setContext("body.mid",mid.getId());
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Owner(SHWETANK)
    @Feature("PGP-46204")
    @Test(description = "Verify success response when cardTokenInfo object is provided when token type is TXN for EMI paymode")
    public void TestSuccessResponseWhenCardTokenInfoObjectIsPassed() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_card_token)
                .setContext("head.token",txnToken)
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
    @Test(description = "Verify success response when paymethod EMI_DC when token type is TXN")
    public void TestSuccessResponseWhenPaymethodEMI_DCIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
    @Test(description = "Verify success response when paymethod is CREDIT_CARD with bankoffer  in item based request when token type is TXN")
    public void TestSuccessResponseWhenPaymethodCreditCardIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("body.custId",custId)
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
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
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
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in item based when token type is TXN Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsItemBasedIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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

    @Test(description = "Verify when wrong  brandid , category id is passed in item based  when token type is TXN Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongItemdetailsIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
    @Test(description = "Verify when wrong emiOfferDetails and wrong bankOfferDetails offerid  is passed in amount based  when token type is TXN Token for EMI paymode")
    public void TestSuccessResponseWhenEMIPaymethodWithWrongOfferIdsAmountBasedIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
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
    @Test(description = "Verify failed when out of range amount is passed in amount based  when token type is TXN Token for EMI paymode")
    public void TestFailedResponseWhenEMIPaymethodWithOutofRangeAmountBasedIsProvided() throws Exception {
        Constants.MerchantType  mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
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
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("2000.25")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0002";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0001";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0003";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0004";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0005";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "REGOFFERAPPLYSSOMOCK0006";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
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
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - source is required in request header")
    public void Test_Error_Response_01() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23001";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("source is required in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - token is invalid in request header")
    public void Test_Error_Response_02() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23002";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("token is invalid in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - source is required in request header,token is invalid in request header")
    public void Test_Error_Response_03() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23003";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("source is required in request header,token is invalid in request header");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items can't be null or empty")
    public void Test_Error_Response_04() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23004";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("items can't be null or empty");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - gratifications.value is not equal to sum of amountBearer")
    public void Test_Error_Response_05() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23005";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E008");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Offer is not applicable");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - quantity must be equal to 1")
    public void Test_Error_Response_06() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.items[0].quantity",2)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("quantity must be equal to 1");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - model is Mandatory for flow type BRAND")
    public void Test_Error_Response_07() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23007";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - items.price should be equal to or greater than paymentDetails.originalAmount")
    public void Test_Error_Response_08() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
                .setContext("body.paymentDetails.orderAmount",3000)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - cardIndexNo can't be null or blank for payMethod EMI")
    public void Test_Error_Response_09() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23009";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("cardIndexNo can't be null or blank for payMethod EMI");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions should have only 1 payment option")
    public void Test_Error_Response_10() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23010";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("paymentOptions should have only 1 payment option");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - userDetails can't be null or blank")
    public void Test_Error_Response_11() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",2791)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - custId can't be null or blank")
    public void Test_Error_Response_12() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",2792)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are invalid");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.boTransactionAmount can't be greater than originalAmount")
    public void Test_Error_Response_13() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23013";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E004");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("PromoAmount cannot be greater than Order Amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - paymentOptions.emiTransactionAmount can't be greater than originalAmount")
    public void Test_Error_Response_14() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23014";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E005");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SubventionAmount cannot be greater than order amount");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - invalid tenure details")
    public void Test_Error_Response_15() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","M")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("invalid tenure details");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - No plans on LPVC")
    public void Test_Error_Response_16() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.mid",mid.getId())
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4280905009022164")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","NKMB");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - No plans found. Invalid tenure details")
    public void Test_Error_Response_17() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",5)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("No plans available");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - Internal Server Error")
    public void Test_Error_Response_18() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23018";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E007");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("There seems to be a problem in processing. Please try again");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response - Your Session has expired.")
    public void Test_Error_Response_19() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken+"p")
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",5)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("true");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56042")
    @Test(description = "Verify Error Response when ADS returns - payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN")
    public void Test_Error_Response_20() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= "Mock23020";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4799320857008816")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("E999");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("payMethod should be either of EMI, EMI_DC, EMI_CARDLESS, NET_BANKING, BNPL, PAYTM_EMI, DEBIT_CARD, CREDIT_CARD, PAYTM_DIGITAL_CREDIT, UPI, BALANCE, EMI_LOAN");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(RONIKA)
    @Feature("PGP-58209")
    @Test(description = "Verify OfferApply Response when bin is emi ineligible and paymode is EMI")
    public void Verify_OfferApply_Response_When_Bin_Is_Emi_Ineligible() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4293440001480769")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0003");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("The card entered is not EMI eligible. Try with another card.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(RONIKA)
    @Feature("PPSL-794")
    @Test(description = "Verify OfferApply Response when Cardnumber starts with 0")
    public void Verify_OfferApply_Response_When_Cardnumber_Starts_With_0() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","0471865010001033")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Card Number. Card Number cannot start with Zero.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(RONIKA)
    @Feature("PPSL-794")
    @Test(description = "Verify OfferApply Response when Random IssuingBank is sent")
    public void Verify_OfferApply_Response_When_RandomIssuingBank_Is_Sent() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4718650100010336")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","TEST");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("The selected bank does not match with your card's issuing bank. Please select the correct bank or try a different card.");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    @Owner(RONIKA)
    @Feature("PPSL-794")
    @Test(description = "Verify OfferApply Response when DebitCardInfo is sent with CreditCard PayMethod")
    public void Verify_OfferApply_Response_When_DebitCardInfo_Is_Sent_With_CreditCard_PayMethod() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId= user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based,referenceId)
                .setContext("head.token",txnToken)
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","5166400031031058")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("The card type of bin does not match with the selected payment method");
        Assertions.assertThat(jsonPath.getString("body.showErrorMessage")).isEqualTo("false");
    }
    
    @Owner(PUSPA)
    @Feature("PGP-61357")
    @Test(description = "Verify OfferApply with JWE Encryption - encKeyId and encryptedParam")
    public void Verify_OfferApply_With_JWE_Encryption_EncKeyId_And_EncryptedParam() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId = user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        // JWE Encryption parameters - Encrypts: {"cardNo": "4718650100010336"}
        String encKeyId = "KP_1";
        String encryptedParam = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ.TBkKRlpjOJUXRTtAq04S3DrG3qFgG8O3Ken3LkObaxlpdcCrtudK6c5nF1mPAkxIdKMzroMV83YPa_iiI9LB-Y66L2HycdB6O7rgRrrb2rbDTgZGZzefwJ8_X5AS9jrzwkwA4gyo12lIc0YwkEqry1wRbkB2JXAK0UH9dhl8bsjxYsS_LpnvUGveEqOYR0EjXzJQiAKRHQ5SehcgyD-ElM56hjfs6Oawb0kuKiiSLvNIFz-E9GIeENjcWf_1uxlMvBNWeQtoY3_bTjiOboW1iKLM-ZyoIOlmSy8t9cwQ7rYfMlGu5jv8ELJAN3kYUOKKeIH3BAELLab2wAHAftzcwg.WFRjQsKm4dDitdIv.7REBdKuE4FLkNXJz2sPpsEsvsyW8RkGQlz3FvGRJ.gIMXUVc2tkSX3U4MwXXpWQ";
        
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based, referenceId)
                .setContext("head.token", txnToken)
                .setContext("head.tokenType", "TXN_TOKEN")
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.orderAmount", 1000)
                .deleteContext("body.items")
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", true)
                .deleteContext("body.paymentDetails.paymentOptions[0].applySubvention")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].vpa")
                .setContext("body.encKeyId", encKeyId)
                .setContext("body.encryptedParam", encryptedParam);
        
        JsonPath jsonPath = offerApply.execute().jsonPath();
        
        // Assertions
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status should be SUCCESS")
                .isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result code should be 0000 for success")
                .isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken"))
                .as("Unified offers token should be present")
                .isNotNull()
                .isNotEmpty();
    }
    
    @Owner(PUSPA)
    @Feature("PGP-61357")
    @Test(description = "Verify OfferApply with JWE Encryption for UPI VPA - encKeyId and encryptedParam")
    public void Verify_OfferApply_With_JWE_Encryption_For_UPI_VPA() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId = user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        
        // Initialize transaction with amount 1000
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        // JWE Encryption parameters - Encrypts: {"vpa": "test@paytm"}
        String encKeyId = "KP_1";
        String encryptedParam = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ.YyibzybqlFHcEPN_SRPMDxcoOwOFW-GO1jfPkiZfeHgOv7xFGjqCQ__GvrD8sgjnasKarMmdlCa2FquIioBT3ztgmrBeRvZztlbFlwDhUcia4dnLKQvbVc2wdVythF8gJt6nuqt_-qbyynaCfZGpbPKm65rZ5biRS6rFJiYo3SfT9MPllWFTARMvKeY08stnOwlAmHe0qTdwZGd6lInMIEah93PZjsPLpawghdocGp7R4I4_GJgeyFdx-I1eOgC7WTnLyfu1sIP8DMKAdIxGwL5lEc6E3-ISEoAAUL46trGjnw3cEjzprYAax0i-lOjmTL9BneD5TmKOPMnkK65bDA.sax0Jau5imxv5Fw_.8dAiaTWcl6XQsM5l3qwz1PDBwMwa.c5vHMsUMSLNhLYjWQjarkQ";
        
        // Build request body for UPI payment
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based, referenceId)
                .setContext("head.token", txnToken)
                .setContext("head.tokenType", "TXN_TOKEN")
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.orderAmount", 1000)
                .deleteContext("body.items")
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI")
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", true)
                .deleteContext("body.paymentDetails.paymentOptions[0].applySubvention")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].vpa")
                .setContext("body.encKeyId", encKeyId)
                .setContext("body.encryptedParam", encryptedParam);
        
        JsonPath jsonPath = offerApply.execute().jsonPath();
        
        // Assertions
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status should be SUCCESS")
                .isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result code should be 0000 for success")
                .isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken"))
                .as("Unified offers token should be present")
                .isNotNull()
                .isNotEmpty();
    }
    
    @Owner(PUSPA)
    @Feature("PGP-61357")
    @Test(description = "Verify OfferApply with JWE Encryption for cardNo - encKeyId and encryptedParam")
    public void Verify_OfferApply_With_JWE_Encryption_For_CardNo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId = user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        
        // Initialize transaction with amount 1000
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        // JWE Encryption parameters - Encrypts: {"cardNo": "4444333322221111"}
        String encKeyId = "KP_1";
        String encryptedParam = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ.TBkKRlpjOJUXRTtAq04S3DrG3qFgG8O3Ken3LkObaxlpdcCrtudK6c5nF1mPAkxIdKMzroMV83YPa_iiI9LB-Y66L2HycdB6O7rgRrrb2rbDTgZGZzefwJ8_X5AS9jrzwkwA4gyo12lIc0YwkEqry1wRbkB2JXAK0UH9dhl8bsjxYsS_LpnvUGveEqOYR0EjXzJQiAKRHQ5SehcgyD-ElM56hjfs6Oawb0kuKiiSLvNIFz-E9GIeENjcWf_1uxlMvBNWeQtoY3_bTjiOboW1iKLM-ZyoIOlmSy8t9cwQ7rYfMlGu5jv8ELJAN3kYUOKKeIH3BAELLab2wAHAftzcwg.WFRjQsKm4dDitdIv.7REBdKuE4FLkNXJz2sPpsEsvsyW8RkGQlz3FvGRJ.gIMXUVc2tkSX3U4MwXXpWQ";
        
        // Build request body for CREDIT_CARD payment with encrypted cardNo
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based, referenceId)
                .setContext("head.token", txnToken)
                .setContext("head.tokenType", "TXN_TOKEN")
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.orderAmount", 1000)
                .deleteContext("body.items")
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", true)
                .deleteContext("body.paymentDetails.paymentOptions[0].applySubvention")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","444433332222111")
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingBank")
                .deleteContext("body.paymentDetails.paymentOptions[0].vpa")
                .setContext("body.encKeyId", encKeyId)
                .setContext("body.encryptedParam", encryptedParam);
        
        JsonPath jsonPath = offerApply.execute().jsonPath();
        
        // Assertions
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status should be SUCCESS")
                .isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result code should be 0000 for success")
                .isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken"))
                .as("Unified offers token should be present")
                .isNotNull()
                .isNotEmpty();
        

    }
    
    @Owner(PUSPA)
    @Feature("PGP-61357")
    @Test(description = "Verify OfferApply with JWE Encryption for NB - encKeyId and encryptedParam")
    public void Verify_OfferApply_With_JWE_Encryption_For_NB() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String custId = user.custId();
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        
        // Initialize transaction with amount 1000
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        // JWE Encryption parameters - Encrypts: {"bankCode": "HDFC"}
        String encKeyId = "KP_1";
        String encryptedParam = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ.YyibzybqlFHcEPN_SRPMDxcoOwOFW-GO1jfPkiZfeHgOv7xFGjqCQ__GvrD8sgjnasKarMmdlCa2FquIioBT3ztgmrBeRvZztlbFlwDhUcia4dnLKQvbVc2wdVythF8gJt6nuqt_-qbyynaCfZGpbPKm65rZ5biRS6rFJiYo3SfT9MPllWFTARMvKeY08stnOwlAmHe0qTdwZGd6lInMIEah93PZjsPLpawghdocGp7R4I4_GJgeyFdx-I1eOgC7WTnLyfu1sIP8DMKAdIxGwL5lEc6E3-ISEoAAUL46trGjnw3cEjzprYAax0i-lOjmTL9BneD5TmKOPMnkK65bDA.sax0Jau5imxv5Fw_.8dAiaTWcl6XQsM5l3qwz1PDBwMwa.c5vHMsUMSLNhLYjWQjarkQ";
        
        // Build request body for NET_BANKING payment with encrypted bankCode
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based, referenceId)
                .setContext("head.token", txnToken)
                .setContext("head.tokenType", "TXN_TOKEN")
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer", "true")
                .setContext("body.amountBasedSubvention", "true")
                .setContext("body.paymentDetails.orderAmount", 1000)
                .deleteContext("body.items")
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING")
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", true)
                .deleteContext("body.paymentDetails.paymentOptions[0].applySubvention")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .deleteContext("body.paymentDetails.paymentOptions[0].vpa")
                .setContext("body.encKeyId", encKeyId)
                .setContext("body.encryptedParam", encryptedParam);
        
        JsonPath jsonPath = offerApply.execute().jsonPath();
        
        // Assertions
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status should be SUCCESS")
                .isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result code should be 0000 for success")
                .isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken"))
                .as("Unified offers token should be present")
                .isNotNull()
                .isNotEmpty();
    }

}