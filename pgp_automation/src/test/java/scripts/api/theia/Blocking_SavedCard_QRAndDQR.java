package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.api.RedisAPI;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

public class Blocking_SavedCard_QRAndDQR extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutJsCheckoutPage = new CheckoutJsCheckoutPage();
    private SoftAssertions softly = new SoftAssertions();
    RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);

    @BeforeClass
    public void enableFF4J()
    {
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
    }
    @BeforeTest
    public void enableFF4J_FetchSavedCardsForOcl()
    {
        FF4JFlags.enable("theia.disableFetchSavedCardsForOcl");
        redisHelper.delete("FF4J_FEATURE_theia.disableFetchSavedCardsForOcl");
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56596")
    @Test(description = "Verify fetch all call not sending in SQ FQR for Offline MID and theia.disableFetchSavedCardsForOcl FF4J Flag is On")
    public void fetchAll_call_NotIn_SQ_FQR() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        //MID: qa12id49893344049514
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setMerchantVpa("paytm.ud956915885@pty")
                    .setTpap(true)
                    .setOffline(true)
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            int size = fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.paymentModes.size");
            String OrderId = fetchQRResponse.jsonPath().getString("body.paymentOptions.orderId");
            for (int i = 0; i < size; i++) {
                Boolean flag = false;

            if (fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("CREDIT_CARD") ||
                        fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("DEBIT_CARD")) {
                flag = true;
            }
            if (flag==true) {
                softly.assertThat(fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.savedInstruments.size")).isEqualTo(0);
                String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, OrderId, "GET_TOKENIZED_CARDS_IN_FPO", "ASSET_CENTER_SERVICE");
                softly.assertThat(logs).isEqualTo("");
            }

            }
        }
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56596")
    @Test(description = "Verify fetch all call sending in SQ FQR for Offline MID and theia.disableFetchSavedCardsForOcl FF4J Flag is OFF")
    public void fetchAll_call_In_SQ_FQR() throws Exception {
        FF4JFlags.disable("theia.disableFetchSavedCardsForOcl");
        redisHelper.delete("FF4J_FEATURE_theia.disableFetchSavedCardsForOcl");

        User user = userManager.getForWrite(Label.UPILITECC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.COBRANDED_CC);
        String tin=SavedCardHelpers.getTin();
        //MID: qa12id49893344049514
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setMerchantVpa("paytm.ud956915885@pty")
                    .setTpap(true)
                    .setOffline(true)
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            int size = fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.paymentModes.size");
            String OrderId = fetchQRResponse.jsonPath().getString("body.paymentOptions.orderId");
            for (int i = 0; i < size; i++) {
                Boolean flag = false;

                if (fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("CREDIT_CARD") ||
                        fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("DEBIT_CARD")) {
                    flag = true;
                }
                if (flag==true) {
                    softly.assertThat(fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.savedInstruments.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);
                    String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, OrderId, "GET_TOKENIZED_CARDS_IN_FPO", "ASSET_CENTER_SERVICE");
                    softly.assertThat(logs).contains(tin);
                }

            }
        }
        FF4JFlags.enable("theia.disableFetchSavedCardsForOcl");
        redisHelper.delete("FF4J_FEATURE_theia.disableFetchSavedCardsForOcl");
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56596")
    @Test(description = "Verify fetch all call not sending in DQR FQR for Offline MID and theia.disableFetchSavedCardsForOcl FF4J Flag is On")
    public void fetchAll_call_NotIn_DQR_FQR() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        {

            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setMerchantVpa("paytmocl.d956915885@axis") //paytmocl.d956915885@axis
                    .setTpap(true)
                    .setOffline(true)
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            int size = fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.paymentModes.size");
            String OrderId = fetchQRResponse.jsonPath().getString("body.paymentOptions.orderId");
            for (int i = 0; i < size; i++) {
                Boolean flag = false;

                if (fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("CREDIT_CARD") ||
                        fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("DEBIT_CARD")) {
                    flag = true;
                }
                if (flag==true) {
                    softly.assertThat(fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.savedInstruments.size")).isEqualTo(0);
                    String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, OrderId, "GET_TOKENIZED_CARDS_IN_FPO", "ASSET_CENTER_SERVICE");
                    softly.assertThat(logs).isEqualTo("");
                }

            }
        }
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56596")
    @Test(description = "Verify saved card are visible on ONLINE ONUS mid but not on OFFLINE(OCL) MID")
    public void savedCard_Present_On_ONLINE_Not_On_OFFLINE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_ONUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.COBRANDED_CC);
        String tin=SavedCardHelpers.getTin();

        String lastFourDigit=PaymentDTO.VISA_COFT_CARD.substring(12,16);
        String orderID = CommonHelpers.generateOrderId();
        //Check savedcard is present for ONLINE MID
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("200.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.savedInstruments.size");
        Assertions.assertThat(size>0).isEqualTo(true);
        for (int i=0;i<size;i++){
            if(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit")==lastFourDigit)
            {
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].assetType")).isEqualTo("TOKEN");
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit")).isEqualTo(lastFourDigit);
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.cardId")).isEqualTo(tin);
                softly.assertThat(!fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.cardDetails").contains(tin)).isEqualTo(true);
                break;
            }
            else System.out.println("Saved Instrument not found");
        }

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxSavedCardCVV().clearAndType("9801");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
        // Check Savedcard should not present for OFFLINE MID
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setMerchantVpa("paytmocl.d956915885@axis") //paytmocl.d956915885@axis
                    .setTpap(true)
                    .setOffline(true)
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            int size1 = fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.paymentModes.size");
            String OrderId = fetchQRResponse.jsonPath().getString("body.paymentOptions.orderId");
            for (int i = 0; i < size1; i++) {
                Boolean flag = false;

                if (fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("CREDIT_CARD") ||
                        fetchQRResponse.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes[" + i + "].paymentMode").equals("DEBIT_CARD")) {
                    flag = true;
                }
                if (flag==true) {
                    int arraysavedcard=fetchQRResponse.jsonPath().getInt("body.paymentOptions.merchantPayOption.savedInstruments.size");
                    softly.assertThat(arraysavedcard).isEqualTo(0);
                    String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, OrderId, "GET_TOKENIZED_CARDS_IN_FPO", "ASSET_CENTER_SERVICE");
                    softly.assertThat(logs).doesNotContain(tin);
                }

            }

    }
}
