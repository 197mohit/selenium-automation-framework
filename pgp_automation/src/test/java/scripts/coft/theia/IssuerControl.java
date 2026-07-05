package scripts.coft.theia;

import com.paytm.LocalConfig;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.utils.ff4j.FF4JClient;
import com.paytm.utils.ff4j.FF4JClientImpl;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;

public class IssuerControl {
    public static Constants.MerchantType mid = Constants.MerchantType.ISSUER_CONTROL_MERCHANT;
    public static String custId = RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
    public static String allowedCard = PaymentDTO.ISSUER_HDFC_CC;
    public static String blockedCard = PaymentDTO.ISSUER_ICICI_CC;
    public static String allowedTin;
    public static String blockedTin;
    RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);

    @BeforeClass
    public void setUp() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCardOnMidCustId(mid,custId,paymentDTO.getExpMonth(),paymentDTO.Tokenization_Year,allowedCard);
        allowedTin = SavedCardHelpers.getTin();
        SavedCardHelpers.addCardOnMidCustId(mid,custId,paymentDTO.getExpMonth(),paymentDTO.Tokenization_Year,blockedCard);
        blockedTin = SavedCardHelpers.getTin();
    }
    @Test(description = "Verify Fetch Bin is Blocked with bin other than HDFC.")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public  void fetchBinBlocked(){
        FF4JClientImpl ff4JClient= new FF4JClientImpl();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(initTxnResponseDTO.getBody().getTxnToken(), mid.getId(), orderId,blockedCard.substring(0,9),"false");
        JsonPath fetchBinResponse = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("F"));
    }

    @Test(description = "Verify Fetch Bin is allowed with HDFC bin.")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public static void fetchBinAllowed(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(initTxnResponseDTO.getBody().getTxnToken(), mid.getId(), orderId,allowedCard.substring(0,9),"false");
        JsonPath fetchBinResponse = fetchBinDetail.execute().jsonPath();
        System.out.println(fetchBinResponse.getString("body.resultInfo.resultStatus"));
        Assertions.assertThat(fetchBinResponse.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
    }

    @Test(description = "Verify blocked bin cards are not visible in FPO response")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public static void fpoBlocked(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(mid.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath FPOResponse = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(!FPOResponse.getString("body.merchantPayOption.savedInstruments[0].cardDetails.cardId ").equalsIgnoreCase(blockedTin));
    }

    @Test(description = "Verify allowed bin cards are visible in FPO response")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public void fpoAllowed(){
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("theia.allowIssuerOnMid");
        redisHelper.delete("FF4J_FEATURE_theia.allowIssuerOnMid");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(mid.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath FPOResponse = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(FPOResponse.getString("body.merchantPayOption.savedInstruments[0].cardDetails.cardId ").equalsIgnoreCase(allowedTin));
    }

    @Test(description = "Verify Failure PTC response for transaction with blocked bin card")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public  void ptcBlockedCard(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(mid.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(blockedCard)
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath response = processTransactionV1.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("F"));
        Assertions.assertThat(response.getString("body.txnInfo.RESPMSG").equalsIgnoreCase("Payment not allowed presently on your card. Please try paying using other cards/options"));
    }

    @Test(description = "Verify Failure PTC response for transaction with blocked bin saved card")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public  void ptcBlockedSavedCard(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(mid.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(blockedTin+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath response = processTransactionV1.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("F"));
        softAssertions.assertThat(response.getString("body.txnInfo.RESPMSG").equalsIgnoreCase("Payment not allowed presently on your card. Please try paying using other cards/options"));
        softAssertions.assertAll();
    }

    @Test(description = "Verify Success PTC response for transaction with allowed bin card")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public  void ptcAllowedCard(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(mid.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(allowedCard)
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath response = processTransactionV1.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
    }

    @Test(description = "Verify Success PTC response for transaction with allowed bin saved card")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-44637")
    public  void ptcAllowedSavedCard(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,mid).setCustId(custId).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(mid.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(allowedTin+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath response = processTransactionV1.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus").equalsIgnoreCase("S"));
    }
}
