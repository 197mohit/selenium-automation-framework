package scripts.coft.theia;

import static com.paytm.apphelpers.PGPHelpers.getFromALIPAY_USER;

import com.paytm.LocalConfig;
import com.paytm.api.coft.PTS.FetchAllV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.HashMap;

public class CardSorting extends PGPBaseTest {

  public static User user;
  public static String custId;
  public static Constants.MerchantType onusMID = Constants.MerchantType.COFT_THEIA_ONUS;
  public static Constants.MerchantType offusMID = Constants.MerchantType.COFT_THEIA_OFFUS;
  public static HashMap<Integer, String> onusRankMap = new HashMap<Integer, String>();
  public static HashMap<Integer, String> offusRankMap = new HashMap<Integer, String>();
  public static String[] cardList = PaymentDTO.cardSortingList;
  RedisHelper redisHelper = RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,
      LocalConfig.PG_REDIS_CLUSTER_PASS);

  @BeforeClass
  public void setUp() throws Exception {
    SoftAssertions softAssertions = new SoftAssertions();
    FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
    FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
    custId = RandomStringUtils.randomAlphabetic(4) + Instant.now().toEpochMilli();
    user = userManager.getForWrite(Label.LOGIN);
    SavedCardHelpers.deleteSavedCard(user);
    PaymentDTO paymentDTO = new PaymentDTO();
    for (String card : cardList) {
      SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), PaymentDTO.Tokenization_Year, card);
      SavedCardHelpers.addCardOnMidCustId(offusMID, custId, paymentDTO.getExpMonth(),
          PaymentDTO.Tokenization_Year, card);
    }
    String alipayUserId = getFromALIPAY_USER(user.custId()).get("oldpg_id").toString();
    FetchAllV2 onusFetchAll = new FetchAllV2().buildRequest("PAYTM_USER_CARD", alipayUserId,
        user.custId(), onusMID.getId(), onusMID.getId());
    JsonPath onusFetchAllResponse = onusFetchAll.execute().jsonPath();
    int onusCardSize = onusFetchAllResponse.getList("cardInfos").size();
    softAssertions.assertThat(onusCardSize).isNotEqualTo(0);
    softAssertions.assertAll();
    for (int i = 0; i < onusCardSize; i++) {
      onusRankMap.put(Integer.parseInt(
              onusFetchAllResponse.getString("cardInfos[" + i + "].assetDisplayStrategy.rank")),
          onusFetchAllResponse.getString("cardInfos[" + i + "].cardId"));
    }
    FetchAllV2 fetchAll = new FetchAllV2().buildRequest("MERCHANT_USER_CARD", offusMID.getId(),
        custId);
    JsonPath offusFetchAllResponse = fetchAll.execute().jsonPath();
    int cardSize = offusFetchAllResponse.getList("cardInfos").size();
    softAssertions.assertThat(cardSize).isNotEqualTo(0);
    softAssertions.assertAll();
    for (int i = 0; i < cardSize; i++) {
      offusRankMap.put(Integer.parseInt(
              offusFetchAllResponse.getString("cardInfos[" + i + "].assetDisplayStrategy.rank")),
          offusFetchAllResponse.getString("cardInfos[" + i + "].cardId"));
    }
  }

  @Test(description = "Verify cards are visible according to rank for Onus flow.")
  @Owner(Constants.Owner.RUPASANANDA)
  @Feature("PGP-46178")
  public void onusSorting() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), onusMID).setCustId(
        user.custId()).setTxnValue("10").build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(
        initTxnResponseDTO.getBody().getTxnToken()).build();
    FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(onusMID.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
    System.out.println("FPO list : " + fetchPaymentOptionV2Response.getString(
        "body.merchantPayOption.savedInstruments"));
    for (int i = 0; i < onusRankMap.size(); i++) {
      Assert.assertEquals(fetchPaymentOptionV2Response.getString(
              "body.merchantPayOption.savedInstruments[" + i + "].cardDetails.cardId"),
          onusRankMap.get(i + 1), "Cards are not sorted");
      Assert.assertEquals(fetchPaymentOptionV2Response.getString(
              "body.merchantPayOption.savedInstruments[" + i + "].cardDetails.cardId"),
          onusRankMap.get(i + 1), "Cards are not sorted");
    }
  }

  @Test(description = "Verify cards are visible according to rank for offus loggedIn flow.")
  @Owner(Constants.Owner.RUPASANANDA)
  @Feature("PGP-46178")
  public void offusLoggedInSorting() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, offusMID).setCustId(custId)
        .setTxnValue("10").setSsoToken(user.ssoToken()).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(
        initTxnResponseDTO.getBody().getTxnToken()).build();
    FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(offusMID.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
    for (int i = 0; i < offusRankMap.size(); i++) {
      Assert.assertEquals(fetchPaymentOptionV2Response.getString(
              "body.merchantPayOption.savedInstruments[" + i + "].cardDetails.cardId"),
          offusRankMap.get(i + 1), "Cards are not sorted");
    }
  }

  @Test(description = "Verify cards are visible according to rank for offus non loggedIn flow.")
  @Owner(Constants.Owner.RUPASANANDA)
  @Feature("PGP-46178")
  public void offusNonLoggedInSorting() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, offusMID).setCustId(custId)
        .setTxnValue("10").build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(
        initTxnResponseDTO.getBody().getTxnToken()).build();
    FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(offusMID.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
    for (int i = 0; i < offusRankMap.size(); i++) {
      Assert.assertEquals(fetchPaymentOptionV2Response.getString(
              "body.merchantPayOption.savedInstruments[" + i + "].cardDetails.cardId"),
          offusRankMap.get(i + 1), "Cards are not sorted");
    }
  }

  @Test(description = "Verify assetTargetType & assetDisplayStrategy are sent in cache card request.")
  @Owner(Constants.Owner.RUPASANANDA)
  @Feature("PGP-46178")
  public void cacheCardRequest() throws Exception {
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, offusMID).setCustId(custId).build();
    InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
    String orderId = initTxnDTO.getBody().getOrderId();
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(
        initTxnResponseDTO.getBody().getTxnToken()).build();
    FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(offusMID.getId(), orderId,
        fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
    String tin = fetchPaymentOptionV2Response.getString(
        "body.merchantPayOption.savedInstruments[3].cardDetails.cardId");
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
        .Builder(offusMID.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
        .setPaymentMode("CREDIT_CARD")
        .setCardInfo(tin + "||123|")
        .setAuthMode("otp")
        .build();
    ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.theia_facade, orderId,
        "asset/v2/cache/save\", \"TYPE\" : \"REQUEST\", \"REQUEST\"");
    System.out.println(logs);
    Assertions.assertThat(logs.contains("\"assetTargetType\":\"MERCHANT_USER_CARD\""));
    Assertions.assertThat(logs.contains("\"assetDisplayStrategy\":{\"rank\":\"3\""));
    Assertions.assertThat(logs.contains("strategyRule"));
  }
}
