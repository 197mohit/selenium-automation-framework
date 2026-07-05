package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.api.theia.ItemLevelApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersAppliedv2;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.ItemLevelInitTXN;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.ItemLevelPromoDTO;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;

public class ItemLevelPromoTest extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    //Change v2Flag = True to run PaymentOffersAppliedv2 Case

        @Owner(Constants.Owner.AJEESH)
        @Feature("PGP-30037")
        @Test(description = "Validate Final TxnAmount is Discounted for DCPayMode")
        public void ValidateFinalTxnAmountisDiscountedforDCPayMode() throws Exception{
        Constants.MerchantType promoMerchant = Constants.MerchantType.ITEM_LEVEL_PROMO;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String orderID = CommonHelpers.generateOrderId();
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        String ssoToken=user.ssoToken();
        Boolean v2Flag = false;
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "DEBIT_CARD", "HDFC",new PaymentDTO().getDebitCardNumber(), "", null);
        ItemLevelPromoDTO applyPromoDTO = new ItemLevelPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(ssoToken)
                .setMID(promoMerchant.getId())
                .setPromocode("discount")
                .setPaymentOptions(new com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ItemLevelApplyPromo applypromo = new ItemLevelApplyPromo(applyPromoDTO);
        Response response = applypromo.execute();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");
        PaymentOffersAppliedv2 paymentOffersApplied = new PaymentOffersAppliedv2(paymentOffersAppliedResponse);
        ItemLevelInitTXN api = new ItemLevelInitTXN(merchant.getId(),orderID,ssoToken,paymentOffersApplied,v2Flag);
        Response response2 = api.execute();
        String txnToken = response2.jsonPath().getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(promoMerchant,orderID, txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PGP-30037")
    @Test(description = "Validate Final Txn Amount is Discounted for CCPayMode")
    public void ValidateFinalTxnAmountisDiscountedforCCPayMode() throws Exception{
        Constants.MerchantType promoMerchant = Constants.MerchantType.ITEM_LEVEL_PROMO;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String orderID = CommonHelpers.generateOrderId();
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        String ssoToken=user.ssoToken();
        Boolean v2Flag = false;
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "HDFC",new PaymentDTO().getCreditCardNumber(), "", null);
        ItemLevelPromoDTO applyPromoDTO = new ItemLevelPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(promoMerchant.getId())
                .setPromocode("discount")
                .setPaymentOptions(new com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ItemLevelApplyPromo applypromo = new ItemLevelApplyPromo(applyPromoDTO);
        Response response = applypromo.execute();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");
        PaymentOffersAppliedv2 paymentOffersApplied = new PaymentOffersAppliedv2(paymentOffersAppliedResponse);
        ItemLevelInitTXN api = new ItemLevelInitTXN(merchant.getId(),orderID,ssoToken,paymentOffersApplied,v2Flag);
        Response response2 = api.execute();
        String txnToken = response2.jsonPath().getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(promoMerchant,orderID, txnToken, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PGP-30037")
    @Test(description = "Validate Final Txn Amount is Discounted for UPIPayMode")
    public void ValidateFinalTxnAmountisDiscountedforUPIPayMode() throws Exception{
        Constants.MerchantType promoMerchant = Constants.MerchantType.ITEM_LEVEL_PROMO;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String orderID = CommonHelpers.generateOrderId();
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        String ssoToken=user.ssoToken();
        Boolean v2Flag = false;
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "UPI", "HDFC","", "",new PaymentDTO().getVpa() );
        ItemLevelPromoDTO applyPromoDTO = new ItemLevelPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(promoMerchant.getId())
                .setPromocode("discount")
                .setPaymentOptions(new com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ItemLevelApplyPromo applypromo = new ItemLevelApplyPromo(applyPromoDTO);
        Response response = applypromo.execute();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");
        PaymentOffersAppliedv2 paymentOffersApplied = new PaymentOffersAppliedv2(paymentOffersAppliedResponse);
        ItemLevelInitTXN api = new ItemLevelInitTXN(merchant.getId(),orderID,ssoToken,paymentOffersApplied,v2Flag);
        Response response2 = api.execute();
        String txnToken = response2.jsonPath().getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(promoMerchant,orderID, txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(Constants.Owner.AJEESH)
    @Feature("PGP-30037")
    @Test(description = "Validate Final Txn Amount is Discounted for NBPayMode")
    public void ValidateFinalTxnAmountisDiscountedforNBPayMode() throws Exception{
        Constants.MerchantType promoMerchant = Constants.MerchantType.ITEM_LEVEL_PROMO;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String orderID = CommonHelpers.generateOrderId();
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        String ssoToken=user.ssoToken();
        Boolean v2Flag = false;
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "NET_BANKING", "ICICI","", "", null);
        ItemLevelPromoDTO applyPromoDTO = new ItemLevelPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(promoMerchant.getId())
                .setPromocode("discount")
                .setPaymentOptions(new com.paytm.dto.PromoDTO.ItemLevelPromoDTO.PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ItemLevelApplyPromo applypromo = new ItemLevelApplyPromo(applyPromoDTO);
        Response response = applypromo.execute();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");
        PaymentOffersAppliedv2 paymentOffersApplied = new PaymentOffersAppliedv2(paymentOffersAppliedResponse);
        ItemLevelInitTXN api = new ItemLevelInitTXN(merchant.getId(),orderID,ssoToken,paymentOffersApplied,v2Flag);
        Response response2 = api.execute();
        String txnToken = response2.jsonPath().getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(promoMerchant,orderID, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
}
