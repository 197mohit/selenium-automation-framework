package scripts.UI;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

@Owner(Constants.Owner.PUSHKAL)
@Feature("PGP_36677_37725")
public class AddAndPayOverrideTest extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Test(description = "When Hybrid and AddnPay is enabled then OVERRIDE_ADDNPAY_BEHAVIOUR " +
            "restricts addnpay preference and allows only hybrid transaction for one time payment")
    public void paymentModeisHybridWhenHybridandAddnPayIsEnabled(@Optional("enhancedweb_revamp") String theme)
            throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 10.00);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADD_N_PAY_OVERRIDE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Object appData = cashierPage.getAppData();
        Assertions.assertThat(appData.toString()).containsIgnoringCase("type=HYBRID");
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.validatePaymentMode("HYBRID");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS");
        txnStatus.validatePaymentMode("HYBRID").AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Test(description = "(PGP_37725) When addnpay preference is enabled then OVERRIDE_ADDNPAY_BEHAVIOUR will only allow " +
            "wallet transaction for subscription payment")
    public void paymentModeisWalletForSubscription(@Optional("enhancedweb_revamp") String theme)
            throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.ADDNPAY_OVERRIDE_SUBS, theme)
                .setTOKEN_TYPE("SSO")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Object appData = cashierPage.getAppData();
        Assertions.assertThat(appData.toString()).containsIgnoringCase("type=NONE");
        cashierPage.tabCreditCard().assertNotVisible();
    }

    @Test(description = "The hybrid txn is successful in deferred checkout when mid has both " +
            "addnpay and hybrid with OVERRIDE_ADDNPAY_BEHAVIOUR preference enabled")
    public void paymentModeisHybridForDeferredCheckout() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 10.00);
        SavedCardHelpers.deleteSavedCard(user);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(Constants.MerchantType.ADD_N_PAY_OVERRIDE.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.ADD_N_PAY_OVERRIDE.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),Constants.MerchantType.ADD_N_PAY_OVERRIDE)
                .setTxnValue("11")
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.ADD_N_PAY_OVERRIDE, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("SUCCESS");
        responsePage.validatePaymentMode("HYBRID");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("11")
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }
}
