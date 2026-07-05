package scripts.coft.theia;

import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.ff4j.FF4JFeature;
import com.paytm.utils.merchant.ff4j.FF4JFeatures;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;

public class GlobalVaultAndLocalVault extends PGPBaseTest {
    static Constants.MerchantType onusMID = Constants.MerchantType.COFT_THEIA_ONUS;
    static Constants.MerchantType offusMID = Constants.MerchantType.COFT_THEIA_OFFUS;
    static User user;
    static String txnValue="2.00";
    static String CUST_ID;
    static String onusCardId;
    static String offusCardId;

    @BeforeClass
    public void setUp() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        user = userManager.getForWrite(Label.LOGIN);
//        txnValue = Double.toString(WalletHelpers.getWalletBalance(user)+2);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.Tokenization_Year,paymentDTO.getCreditCardNumber());
        onusCardId=SavedCardHelpers.getTin();
        PaymentDTO paymentDTO2 = new PaymentDTO();
        CUST_ID= RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(offusMID,CUST_ID,paymentDTO2.getExpMonth(),paymentDTO2.Tokenization_Year,paymentDTO2.getCreditCardNumber());
        offusCardId = SavedCardHelpers.getTin();
    }

    @Test(description = "Verify global vault cards in merchantPayOptions and addMoneyPayOption")
    @Owner(Constants.Owner.RUPASANANDA)
    public void onusMerchantLoggedinFlow() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), onusMID).setCustId(user.custId()).setTxnValue(txnValue).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(onusMID.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.get("body.merchantPayOption.savedInstruments[0].cardDetails.cardId ").equals(onusCardId));
//        Assertions.assertThat(fetchPaymentOptionV2Response.get("body.addMoneyPayOption.savedInstruments[0].cardDetails.cardId").equals(onusCardId));
    }

    @Test(description = "Verify local vault cards in merchantPayOptions and global vault cards in addMoneyPayOption")
    @Owner(Constants.Owner.RUPASANANDA)
    public void ofusMerchantLoggedinFlow() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), offusMID).setTxnValue(txnValue).setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(offusMID.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.get("body.merchantPayOption.savedInstruments[0].cardDetails.cardId ").equals(offusCardId));
//        Assertions.assertThat(fetchPaymentOptionV2Response.get("body.addMoneyPayOption.savedInstruments[0].cardDetails.cardId").equals(onusCardId));
    }

    @Test(description = "Verify local vault cards in merchantPayOptions")
    @Owner(Constants.Owner.RUPASANANDA)
    public void ofusMerchantNonLoggedinFlow() throws InterruptedException {
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, offusMID).setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(offusMID.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.get("body.merchantPayOption.savedInstruments[0].cardDetails.cardId ").equals(offusCardId));
    }
}