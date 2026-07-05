package scripts.Promo.ANDFlow;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import scripts.LogValidationRetryAnalyser;
import scripts.api.theia.applyPromo.SSOTokenApplyPromoV1Test;

import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.Owner.BHARAT;

@Owner("Bharat Gandhi")
@Feature("PGP-29683")
public class CheckoutSCM extends PGPBaseTest {
    @Owner(BHARAT)
    @Parameters({"isNativePlus"})
    @Test(description = "validate checkout promo api using CIN", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateCheckoutPromo_using_CIN() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");

        Promo promocode = new Promo();
        merchant.getPromos().add(promocode);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();
        PaymentDTO paymentDTO = new PaymentDTO();

        SavedCardHelpers.deleteSavedCard(user);
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_ICICI);

        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", token)
                .setMid(promoMerchant.getId())
                .setAmount(100.0)
                .setGenerateOrderId(null)
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true")
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);

        JsonPath fporesponse = fpo.execute().jsonPath();
        String fpocin = fporesponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId").replace("[", "").replace("]", "");
        String fpobin8hash = fporesponse.getString("body.merchantPayOption.savedInstruments.cardDetails.firstEightDigit").replace("[]", "").replace("]", "");


        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", "", cin, null);

        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo applypromo = new ApiV1ApplyPromo(applyPromoDTO);
        Response applypromoresponse = applypromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = applypromoresponse.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token, promoMerchant, paymentOffersApplied)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        //String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(fpocin + "||123|")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdcheckoutreq = "grep \"" + orderId + "\"  |grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/checkout\" | grep \"REQUEST\"\n";
        String theiafacadelogscheckoutreq = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdcheckoutreq);
        Assertions.assertThat(theiafacadelogscheckoutreq).contains("cardIndexNo", cin).contains("bin8hash", fpobin8hash);

        String grepcmdcheckoutres = "grep \"" + orderId + "\"  |grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/checkout\" | grep \"RESPONSE\"\n";
        String theiafacadelogscheckoutres = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdcheckoutres);
        Assertions.assertThat(theiafacadelogscheckoutres).contains("promocode", promoMerchant.getId()).contains("Promocode applied successfully");

    }

    @Owner(BHARAT)
    @Parameters({"isNativePlus"})
    @Test(description = "", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateCheckoutPromo_using_new_card() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SoftAssert softAssert = new SoftAssert();

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);


        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo applypromo = new ApiV1ApplyPromo(applyPromoDTO);

        Response applypromoresponse = applypromo.execute();

        String cin = PGPHelpers.getCIN(paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_ICICI);

        HashMap<String, Object> paymentOffersAppliedResponse = applypromoresponse.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token, promoMerchant, paymentOffersApplied)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + paymentDTO.PROMO_CC_CARD_HDFC + "|123|082022")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdcheckoutreq = "grep \"" + orderId + "\"  |grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/checkout\" | grep \"REQUEST\"\n";
        String theiafacadelogscheckoutreq = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdcheckoutreq);
        Assertions.assertThat(theiafacadelogscheckoutreq).contains("cardIndexNo", cin);

        String grepcmdcheckoutres = "grep \"" + orderId + "\"  |grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/checkout\" | grep \"RESPONSE\"\n";
        String theiafacadelogscheckoutres = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdcheckoutres);
        Assertions.assertThat(theiafacadelogscheckoutres).contains("promocode", promoMerchant.getId()).contains("good promo");


    }
}


