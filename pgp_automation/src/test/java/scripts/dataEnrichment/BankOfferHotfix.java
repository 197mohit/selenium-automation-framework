package scripts.dataEnrichment;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import java.util.*;
import static com.paytm.appconstants.Constants.Owner.MAYURI;

//make one promo of 11.5497% of 7727.00
public class BankOfferHotfix extends CheckoutJsBase {
    @Owner(MAYURI)
    @Feature("PGP-46477")
    @Test(description = "Verify Initiate txn API when passing txnAmount=7727.00 and instantDiscount=892.44 ")
    public void validateInitiateTxnAPI() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        User user = userManager.getForRead(Label.BASIC);

        HashMap<String, Object> offer = new HashMap<>();
        offer.put("promocodeApplied","TESTPG2EMI42");
        offer.put("promotext","₹892.44 discount applied successfully.");
        offer.put("instantDiscount","892.44");
        offer.put("cashbackAmount",null);
        offer.put("payMethod","CREDIT_CARD");
        offer.put("promoVisibility","false");
        offer.put("responseCode",null);
        offer.put("transactionAmount","7727.00");
        offer.put("paytmCashbackAmount",null);

        List<HashMap<String, Object>> offerBreakupList1 = new ArrayList<>();
        offerBreakupList1.add(offer);

        HashMap <String, Object> hm = new HashMap<>();
        hm.put("totalInstantDiscount", "892.44");
        hm.put("totalCashbackAmount", null);
        hm.put("offerBreakup", offerBreakupList1);
        hm.put("totalTransactionAmount","7727.00");
        hm.put("totalPaytmCashbackAmount",null);

        HashMap<String, Object> paymentOffersAppliedResponse = new HashMap<String, Object>(hm);

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied).setTxnValue("7727.00")
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);

        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        Assert.assertEquals(iniJsonPath.getString("body.resultInfo.resultMsg"),"Success");
        Assert.assertNotNull(iniJsonPath.getString("body.txnToken"));
    }

    @Owner(MAYURI)
    @Feature("PGP-46477")
    @Parameters({"theme"})
    @Test(description = "Verify Initiate txn API and e2e txn when passing txnAmount=7727.00 and instantDiscount=892.44 ")
    public void validateInitiateTxnAPISuccessE2E(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        User user = userManager.getForRead(Label.BASIC);

        PaymentOptions paymentOptions = new PaymentOptions("7727.00", "CREDIT_CARD", "HDFC",PaymentDTO.PROMO_CC_CARD_HDFC , "", null);
        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(merchantType.getId())
                .setPromocode("TESTPG2EMI46")
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("7727.00")
                .build();
        ApiV1ApplyPromo applypromo = new ApiV1ApplyPromo(applyPromoDTO);

        Response response = applypromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType, paymentOffersApplied).setTxnValue("7727.00").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("6834.54")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .validateResponsePageParameters()
                .assertAll();
    }
}
