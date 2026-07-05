package scripts;

import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.List;

@Owner("Akshat")
@Feature("PGP-32821")

public class cardsFilteredForGraceDays extends PGPBaseTest {
    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
        return paymethodList;
    }

    @Parameters({"theme"})
    @Test(description = "Verify that CC SUBS transaction fails when grace days > 0  ")
    public void TC_001_CC_transactionFailed_for_GraceDays(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme)
                .setSUBS_PPI_ONLY("")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verify that DC SUBS transaction fails when grace days > 0  ")
    public void TC_002_DC_transactionFailed_for_GraceDays(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .setSUBS_PPI_ONLY("")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verify that CC DC filtered out when grace days > 0 " + "paymode= blank")
    public void TC_003_CcDc_filteredOut_for_unknownPayMode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme, user)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_GRACE_DAYS("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("CREDIT CARD"));
        Assert.assertFalse(paymethodList.contains("DEBIT CARD"));

    }
    @Test(description = "Verify that Native CC SUBS transaction fails when grace days > 0")
    public void TC_004_nativeCC_transactionFailed_for_GraceDays() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subs payment mode");

    }

    @Test(description = "Verify that Native DC SUBS transaction fails when grace days > 0")
    public void TC_005_nativeDC_transactionFailed_for_GraceDays() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("2022");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Subs payment mode");

    }
    @Test(description = "Verify that CC DC filtered out when grace days > 0 " + "paymode= blank")
    public void TC_006_CcDc_filteredIn_FPO() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOptionV2.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'CREDIT_CARD'}.priority"))
                .as("paymode mismatch")
                .isNull();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'DEBIT_CARD'}.priority"))
                .as("paymode mismatch")
                .isNull();

    }

}
