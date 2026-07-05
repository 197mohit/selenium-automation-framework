package scripts;

import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import groovy.transform.PackageScope;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;


@Owner(Constants.Owner.ROHIT)
@Feature("PGP-31841")
public class AddnpayCCdisable extends PGPBaseTest {

    @Parameters({"theme"})
    @Test(description = "verify CC tab should be visible for merchant paymodes | enhance flow")
    public void enhanced01(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDnPAY_DISABLE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
    }
    @Parameters({"theme"})
    @Test(description = "verify CC and CC savedcard tab should not be visible for add n pay paymodes | enhance flow")
    public void enhanced02(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(),paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDnPAY_DISABLE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("80")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertNotVisible();
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Parameters("isNativePlus")
    @Test(description = "verify CC and CC savedcard should be visible for merchant paymodes but not for add n pay paymodes | native+")
    public void naiveplus01(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADDnPAY_DISABLE;
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(),paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSsoToken(user.ssoToken())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.ADDnPAY_DISABLE, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String  payMethod = "CREDIT_CARD";
        String status = "false";
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.addMoneyPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain(payMethod);
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardType"))
                .contains(payMethod);
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.addMoneyPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardType"))
                .doesNotContain(payMethod);

    }

    @Parameters("isNativePlus")
    @Test(description = "Verify if CC is passed in PTC then invalid paymode error should be returned | native+")
    public void naiveplus02(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADDnPAY_DISABLE;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description="verify CC and CC savedcard should not be visible for add n pay paymodes | appinvoke")
    public void appInvoke01(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDnPAY_DISABLE)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.ADDnPAY_DISABLE,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertNotVisible();
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description="verify CC tab should be visible for merchant paymodes | appinvoke")
    public void appInvoke02(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,20.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDnPAY_DISABLE)
                .setTxnValue("20").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.ADDnPAY_DISABLE,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
    }


}
