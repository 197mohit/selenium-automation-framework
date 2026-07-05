package scripts.api.PromoAndEmiSubvention;

import com.paytm.api.CreateToken;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApplyPromoV2Api;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferDiscovery;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.configmanager.TestDataUtil;
import com.paytm.dto.ApplyPromoV2DTO.ApplyPromoV2DTO;
import com.paytm.dto.ApplyPromoV2DTO.PaymentOption;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.*;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;
import static com.paytm.apphelpers.PGPHelpers.pause;
import static jodd.util.ThreadUtil.sleep;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;


public class TestUnifiedFlows extends PGPBaseTest {

    private Map<String, Object> config;

    @BeforeClass
    public void setup() throws Exception {
        config = TestDataUtil.loadData("src/main/resources/OfferJs/config.json", Map.class);
    }

    String credit_card_txn_amount_based = "{\"head\":{\"channelId\":\"WEB\",\"tokenType\":\"SSO\",\"token\":\"{{ssoToken}}\"},\"body\":{\"custId\":\"{{userid}}\",\"offerDetails\":{\"bankOfferDetails\":[{\"offerId\":\"2357753\"}]},\"paymentDetails\":{\"orderAmount\":\"2210\",\"paymentOptions\":[{\"applyBankOffer\":true,\"applySubvention\":false,\"payMethod\":\"EMI\",\"issuingBank\":\"HDFC\",\"cardNo\":\"{{cardNoHdfc}}\",\"tenure\":[{\"value\":3,\"unit\":\"MONTH\"}]}]},\"amountBasedBankOffer\":true,\"amountBasedSubvention\":true,\"mid\":\"{{mid}}\"}}";
    String offerDiscoveryPayload = "{\"head\":{\"tokenType\":\"ACCESS\",\"token\":\"44588ff15e1a4f1fbb5ec65bfa356d8d1723540887451\",\"channelId\":\"WEB\"},\"body\":{\"mid\":\"qa12ps69382732062304\",\"referenceId\":\"1058644224OfferJs\",\"amountBasedBankOffer\":true,\"amountBasedSubvention\":true,\"paymentDetails\":{\"orderAmount\":\"10000\",\"paymentOptions\":[{\"payMethod\":\"CREDIT_CARD\"},{\"payMethod\":\"DEBIT_CARD\"},{\"payMethod\":\"EMI\"},{\"payMethod\":\"EMI_DC\"}]}}}";

    private static final String CARD_NO = "4769953850007926";
    private static final String CARD_NO_CARDBIN_OFFER = "4895380115392363";
    private static final String CHANNEL_CODE = "HDFC";
    private static final String EMI_TYPE = "CREDIT_CARD";
    private static final String PLAN_ID = "HDFC|3";
    private static final String UNLOCK_API = "ads/v2/offer/unlock";
    private static final String RESPONSE = "RESPONSE";
    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();


    @Owner(SATWIK_SHARMA)
    @Feature("PGP-54910")
    @Test(description = "When there is a failure case\n" +
            "from Acq at the time of COP \n" +
            "Theia used to initiate unlock API call to affordability \n" +
            "verify offerUnlock API does not throw affordabilityTxnId can't be  null or blank, type can't be  null or blank")
    public void testRiskRejectInPTCDoesNotBreakOfferUnlockAPI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;

        /**
         *
         * Do not Change this custId & SSO Tokencode block we have written mock to acheive
         * RISK_REJECT from Acq payorder to repro the scenario
         *
         */


        String ssoToken= AuthHelpers.getSSOToken("5670122111","Paytm@1234");
        String custId="1704271688";

        OfferApply offerApply = setupOfferApply(ssoToken, mid.getId(), custId);



        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        TxnAmount txnAmount = new TxnAmount(payableAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Transaction declined in staging");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), UNLOCK_API, RESPONSE);


        Assertions.assertThat(logsResponse).doesNotContain("affordabilityTxnId can't be  null or blank, type can't be  null or blank");

    }

    private OfferApply setupOfferApply(String ssoToken, String mid, String custId) {
        return (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid)
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2210)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2357753")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", "3")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit", "MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", CARD_NO);
    }


    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56063")
    @Test(description = "Verify that for Fresh Card Transactions on unified Flow the cardBin is passed in the offerCheckout API and not the tokenBin after Alt Id Generation - Unified Custom Checkout" )
    public void testCardBinPassedinOfferCheckoutNotTokenBin_ApplyOffer() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2391740")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo",CARD_NO_CARDBIN_OFFER);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        TxnAmount txnAmount = new TxnAmount(payableAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + CARD_NO_CARDBIN_OFFER + "|545|122027")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        String logsResponse2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/gc/generateTokenData", "RESPONSE");
        Assertions.assertThat(logsResponse2).contains("\"resultMsg\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"bin\":\"489538011\"");

    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-56063")
    @Test(description = "Verify that for Fresh Card Transactions on unified Flow the cardBin is passed in the offerCheckout API and not the tokenBin after Alt Id Generation")
    public void testCardBinPassedinOfferCheckoutNotTokenBin_SimplifiedUnified(@Optional("true") boolean isNativePlus) throws Exception{
        String txnAmount="2000";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2391740","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + CARD_NO_CARDBIN_OFFER + "|545|122027")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        String logsResponse2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/gc/generateTokenData", "RESPONSE");
        Assertions.assertThat(logsResponse2).contains("\"resultMsg\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"bin\":\"489538011\"");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56063")
    @Test(description="Verify that for Fresh Card Transactions on unified Flow the cardBin is passed in the offerCheckout API and not the tokenBin after Alt Id Generation - ApplyPromo Custom Checkout migrated to unified")
    public void testCardBinPassedinOfferCheckoutNotTokenBin_applyPromo() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI","","",CARD_NO_CARDBIN_OFFER,"3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        double payableAmount = 0;
        if (transactionAmount != null && cashback != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                //.setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + CARD_NO_CARDBIN_OFFER + "|545|122027")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        String logsResponse2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/gc/generateTokenData", "RESPONSE");
        Assertions.assertThat(logsResponse2).contains("\"resultMsg\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"bin\":\"489538011\"");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-56063")
    @Test(description = "Verify that for Fresh Card Transactions on unified Flow the cardBin is passed in the offerCheckout API and not the tokenBin after Alt Id Generation - Simplified Old flow migrated to unified")
    public void testCardBinPassedinOfferCheckoutNotTokenBin_SimplifiedOld(@Optional("true") boolean isNativePlus) throws Exception{
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO_CARDBIN_OFFER);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForWrite(Label.BASIC);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), "1000", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("1000.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + CARD_NO_CARDBIN_OFFER + "|545|122027")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        String logsResponse2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/gc/generateTokenData", "RESPONSE");
        Assertions.assertThat(logsResponse2).contains("\"resultMsg\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"bin\":\"489538011\"");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56063")
    @Test(description="Verify that for AltId Transactions on unified Flow the cardBin is passed in the offerCheckout API and not the tokenBin - ApplyPromo Custom Checkout migrated to unified")
    public void testCardBinPassedinOfferCheckoutNotTokenBin_applyPromo_AltId() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken("4769953850009925");
        cardTokenInfo.setTokenExpiry("122027");
        cardTokenInfo.setCardSuffix("2363");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013022106003487586699918");
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI","","HDFC",null,"3");
        paymentOption.setCardTokenInfo(cardTokenInfo);
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        double payableAmount = 0;
        if (transactionAmount != null && cashback != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        cardTokenInfo.setTokenType("ALTERNATE");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("||545|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).contains("\"bin\":\"489538011\"");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Standard EMI Txn" )
    public void testDummyValuesNotBeingSentToATSforAmtBasedTxn_STANDARD() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        String transactionAmount="1000";
        TxnAmount txnAmount = new TxnAmount(transactionAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setTxnValue(transactionAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + "4718650100010336" + "|545|122027")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":false");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56779")
    @Test(description="Verify that Txn Amount is being used for applying offers instead of Payable Amount with SimplifiedPaymentOffers Object")
    public void testTxnAmountBeingUsedForOfferApplyNotPayableAmount_SimplifiedPaymentOffers() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        String custid=user.custId();
        String transactionAmount="800";
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD",null,"",CARD_NO,null);
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("ACCESS")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount(transactionAmount)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String cashback = jsonPath.getString("body.paymentOffer.totalCashbackAmount");
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && (cashback != null || discount != null)) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(cashback != null ? cashback : "0") - Double.valueOf(discount != null ? discount : "0");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", cashback = " + cashback);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false", (String) null);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + CARD_NO + "|545|122027")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setTxnAmount(null)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56779")
    @Test(description="Verify that Txn Amount is being used for applying offers instead of Payable Amount with SimplifiedSubvention Object")
    public void testTxnAmountBeingUsedForOfferApplyNotPayableAmount_SimplifiedSubvention() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        String custid=user.custId();
        String transactionAmount="900";
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO())))
                .setGenerateTokenForIntent(true)
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(custid, transactionAmount, true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),txnToken,initTxnDTO.orderFromBody().toString())
                .setPaymentMode("EMI")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO()))
                .setAuthMode("otp")
                .setEmiType("CREDIT_CARD")
                .setChannelCode(CHANNEL_CODE)
                .setPlanId("HDFC|3")
                .setTxnAmount(null)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56779")
    @Test(description="Verify that Txn Amount is being used for applying offers instead of Payable Amount with SimplifiedUnifiedOffers Object")
    public void testTxnAmountBeingUsedForOfferApplyNotPayableAmount_SimlifiedUnifiedOffers() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        String custId=user.custId();
        String transactionAmount="800";
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", user.ssoToken())
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",800)
                .setContext("body.offerDetails",null)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo",CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].applySubvention",true);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", transactionAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + PGPHelpers.getFormattedPaymentDetails("EMI", new PaymentDTO()))
                .setAuthMode("otp")
                .setEmiType("CREDIT_CARD")
                .setChannelCode(CHANNEL_CODE)
                .setPlanId("HDFC|3")
                .setTxnAmount(null)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56878")
    @Test(description="Verify that FPO Reponse has offerDetails object in Items object and the items.offerDetails object contains the bankOfferDetails and emiBankOfferDetails objects")
    public void testFPOResponse_UnifiedOffersinSavedinstruments() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.EMI_CC);
        Constants.MerchantType mid= Constants.MerchantType.EMI_NEW_FLOW;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
        SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        String transactionAmount="800";
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", transactionAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
                .setTxnValue(transactionAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].payMethod").equals("CREDIT_CARD")){
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].items[0].offerDetails.bankOfferDetails")).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isNotNull();
        } else if (fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].payMethod").equals("EMI")) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails")).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails")).isNotNull();
        }
        if(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].payMethod").equals("CREDIT_CARD")){
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].offerDetails[0].items[0].offerDetails.bankOfferDetails")).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].offerDetails[0].items[0].offerDetails.emiOfferDetails")).isNotNull();
        } else if (fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].payMethod").equals("EMI")) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].tenureDetails[0].items[0].offerDetails.bankOfferDetails")).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.savedInstruments[0].unifiedOffers[1].tenureDetails[0].items[0].offerDetails.emiOfferDetails")).isNotNull();
        }

    }

    @Test(description = "testAccessTokenExpiryCaseForWidget - directly checking the scenario in backend call")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-57116")
    public void testAccessTokenExpiryCaseForWidget()  {

        Constants.MerchantType mid = Constants.MerchantType.valueOf((String) config.get("merchantTypeAccessToken"));
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + config.get("referenceIdSuffix");
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");


        OfferDiscovery offerDiscovery = (OfferDiscovery) new OfferDiscovery(offerDiscoveryPayload)
                .setContext("head.token", AccessToken)
                .setContext("body.referenceId",referenceId);
        JsonPath jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        pause(40);

         offerDiscovery = (OfferDiscovery) new OfferDiscovery(offerDiscoveryPayload)
                .setContext("head.token", AccessToken)
                .setContext("body.referenceId",referenceId);
         jsonPath = offerDiscovery.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Access Token.");
    }
}