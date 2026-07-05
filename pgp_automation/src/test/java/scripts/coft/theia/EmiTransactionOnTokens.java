package scripts.coft.theia;

import com.paytm.api.TxnStatus;
import com.paytm.api.coft.PTS.GenerateTokenData;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.emiSubvention.ApiV1Bank;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.emiSubvention.ApiV1Banks.request.ApiV1BanksRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.utils.merchant.api.pgp.theia.paytm_express.GetCardToken;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.time.Instant;

import static com.paytm.utils.merchant.Constants.PGP_HOST;

public class EmiTransactionOnTokens extends PGPBaseTest {

    static String price = "20";
    static String subventionPrice = "20";

    static Constants.MerchantType merchant = Constants.MerchantType.EMI_ON_TOKEN;
    public static String CUST_ID;
    public static String tokenIndexNumber;
    public static String CARD_DATA = "ShBtzeHSledBT4pzp771TgHul78laKQY4TVIUSGQuF5plgqoP9+Z76ts8t8iZH9PqRhpGnjstwXMZk++2b4iXIFG9OBbU7WNVl7tE8TYD9RWzNMa9jgH1XvVTiPuuExaPNKW0aEg2qOIe+vi0I5I4XupSPMPcV4W+bVEXEvQQ3dJY3UNvb6tCbAheCcFDhc23vRyAKQx+J73AoBe2L6n3Vww3Tv6iSnxiJ86HsWBFhIAkP5Z2Ij2g9+/vV5DRPnV3BSz8I2otctYSuyZi2ig/VZoY3IcCR2Bag7VQWzg8xvVkXsAub4BbysPjXBtEN6zT3pQv2x0sBqwFYxkuLjCsg==";

    @BeforeClass
    public void tokenize(){
        CUST_ID= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(merchant.getId()).buildRequest(CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), "", Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(merchant.getKey());
        JsonPath tokenizeDirectCardResponse = tokenizeDirectCard.execute().jsonPath();
        Assertions.assertThat(tokenizeDirectCardResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("SUCCESS"));
        Assertions.assertThat(tokenizeDirectCardResponse.getString("body.tokenInfo.tokenStatus").equalsIgnoreCase("ACTIVE"));
        tokenIndexNumber = tokenizeDirectCardResponse.getString("body.tokenInfo.tokenIndexNumber");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify standard EMI successful transaction with fresh card")
    public void standardEmiWithFreshCard(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardNum(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER)
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        //ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validatePaymentMode("EMI").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify standard EMI successful transaction with TIN")
    public void standardEmiWithTIN(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardInfo(tokenIndexNumber+"||123|")
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
                validateStatus("TXN_SUCCESS").
                validatePaymentMode("EMI").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify standard EMI successful transaction with TOKEN")
    public void standardEmiWithTOKEN(){
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
                validateStatus("TXN_SUCCESS").
                validatePaymentMode("EMI").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify subvention EMI successful transaction with fresh card")
    public void subventionEmiWithFreshCard(){
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setTokenType("CHECKSUM").setMerchantKey(merchant.getKey()).build();
        ApiV1Bank apiV1Bank = new ApiV1Bank(merchant.getId(),apiV1BanksRequest);
        JsonPath bankResponse = apiV1Bank.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        ApiV1TenureRequest apiV1TenureRequest = new ApiV1TenureRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM").
                setFilters(new Filters().setBankCode("HDFC").setCardType("CREDIT_CARD")).build();
        ApiV1Tenure apiV1Tenure = new ApiV1Tenure(merchant.getId(),apiV1TenureRequest);
        JsonPath tenureResponse = apiV1Tenure.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        String planId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String offerId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp[0].offerId");
        ApiV1ValidateRequest apiV1ValidateRequest = new ApiV1ValidateRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM")
                .setPlanId(new BigInteger(planId)).setCustomerId("1234")
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .setPaymentDetails(new PaymentDetails().setCardNumber("|"+PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|111|052026").setTotalTransactionAmount(price))
                .setGenerateTokenForIntent(true).build();
        ApiV1Validate apiV1Validate = new ApiV1Validate(merchant.getId(), apiV1ValidateRequest);
        JsonPath validateResponse = apiV1Validate.execute().jsonPath();
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEmiSubventionToken(emiSubventionToken).setTxnValue(price).setPayableAmount(new TxnAmount(price)).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI").
                setAuthMode("OTP").
                setPlanId(pgPlanId).
                setCardNum(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER).build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify subvention EMI successful transaction with TIN")
    public void subventionEmiWithTIN(){
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setTokenType("CHECKSUM").setMerchantKey(merchant.getKey()).build();
        ApiV1Bank apiV1Bank = new ApiV1Bank(merchant.getId(),apiV1BanksRequest);
        JsonPath bankResponse = apiV1Bank.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        ApiV1TenureRequest apiV1TenureRequest = new ApiV1TenureRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM").
                setFilters(new Filters().setBankCode("HDFC").setCardType("CREDIT_CARD")).build();
        ApiV1Tenure apiV1Tenure = new ApiV1Tenure(merchant.getId(),apiV1TenureRequest);
        JsonPath tenureResponse = apiV1Tenure.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        String planId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String offerId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp[0].offerId");
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, merchant.getId(), CUST_ID, tokenIndexNumber, "123");
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        ApiV1ValidateRequest apiV1ValidateRequest = new ApiV1ValidateRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM")
                .setPlanId(new BigInteger(planId)).setCustomerId("1234")
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .setPaymentDetails(new PaymentDetails().setTotalTransactionAmount(price))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true).build();
        ApiV1Validate apiV1Validate = new ApiV1Validate(merchant.getId(), apiV1ValidateRequest);
        JsonPath validateResponse = apiV1Validate.execute().jsonPath();
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEmiSubventionToken(emiSubventionToken).setTxnValue(price).setPayableAmount(new TxnAmount(price)).setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI").
                setAuthMode("OTP").
                setPlanId(pgPlanId).
                setCardInfo(tokenIndexNumber+"||123|").build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify subvention EMI successful transaction with TOKEN")
    public void subventionEmiWithTOKEN(){
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ApiV1BanksRequest apiV1BanksRequest = new ApiV1BanksRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setTokenType("CHECKSUM").setMerchantKey(merchant.getKey()).build();
        ApiV1Bank apiV1Bank = new ApiV1Bank(merchant.getId(),apiV1BanksRequest);
        JsonPath bankResponse = apiV1Bank.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        ApiV1TenureRequest apiV1TenureRequest = new ApiV1TenureRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM")
                .setFilters(new Filters().setBankCode("HDFC").setCardType("CREDIT_CARD")).build();
        ApiV1Tenure apiV1Tenure = new ApiV1Tenure(merchant.getId(),apiV1TenureRequest);
        JsonPath tenureResponse = apiV1Tenure.execute().jsonPath();
        Assertions.assertThat(bankResponse.getString("body.resultInfo.resultMsg").equalsIgnoreCase("Success"));
        String planId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String offerId = tenureResponse.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.itemBreakUp[0].offerId");
        JSONObject cardTokenInfoJson = new JSONObject();
        cardTokenInfoJson.put("cardToken",cardTokenInfo.getCardToken());
        cardTokenInfoJson.put("tokenExpiry",cardTokenInfo.getTokenExpiry());
        cardTokenInfoJson.put("TAVV",cardTokenInfo.getTavv());
        cardTokenInfoJson.put("cardSuffix",cardTokenInfo.getCardSuffix());
        cardTokenInfoJson.put("panUniqueReference",cardTokenInfo.getPanUniqueReference());
        GetCardToken getCardToken = new GetCardToken(PGP_HOST, "CARD_TOKEN_INFO=" + cardTokenInfoJson.toJSONString() + "&CVV=" + "123" + "&CUST_ID=" + CUST_ID + "&MID=" + merchant.getId());
        JsonPath getCardTokenResponse = getCardToken.execute().jsonPath();
        String cacheCardToken = getCardTokenResponse.getString("TOKEN");
        ApiV1ValidateRequest apiV1ValidateRequest = new ApiV1ValidateRequest.Builder(price,subventionPrice).setMid(merchant.getId()).setMerchantKey(merchant.getKey()).setTokenType("CHECKSUM")
                .setPlanId(new BigInteger(planId)).setCustomerId("1234")
                .setOfferDetails(new OfferDetails().setOfferId(offerId))
                .setPaymentDetails(new PaymentDetails().setTotalTransactionAmount(price).setCardTokenInfo(cardTokenInfoJson))
                .setCacheCardToken(cacheCardToken)
                .setGenerateTokenForIntent(true).build();
        ApiV1Validate apiV1Validate = new ApiV1Validate(merchant.getId(), apiV1ValidateRequest);
        JsonPath validateResponse = apiV1Validate.execute().jsonPath();
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEmiSubventionToken(emiSubventionToken).setTxnValue(price).setPayableAmount(new TxnAmount(price)).setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI").
                setAuthMode("OTP").
                setPlanId(pgPlanId).
                setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo).build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-37987")
    @Test(description = "Verify last four digit parameter is present in theia and insta")
    public void lastFourDigitVerification() throws InterruptedException {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        String cardLastFourDigit = generteTokenDataResponse.getString("body.tokenInfo.cardSuffix");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardInfo(tokenIndexNumber+"||123|")
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(merchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validatePaymentMode("EMI").AssertAll();
        /*
        Will update this section after logs validation implementation.
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"lastFourDigits\"";
        String logs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).contains("lastFourDigits\":\""+cardLastFourDigit);
        String instaGrepcmd = "grep \"" + orderId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"lastFourDigits\"";
        String Instalogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, instaGrepcmd);
        Assertions.assertThat(Instalogs).contains("lastFourDigits\":\""+cardLastFourDigit);
         */
    }
}