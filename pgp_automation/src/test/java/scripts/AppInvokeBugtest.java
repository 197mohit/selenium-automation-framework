package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import java.util.Date;

@Owner(Constants.Owner.ROHIT)
public class AppInvokeBugtest extends PGPBaseTest {

    @Parameters({"theme"})
    @Test(description = "validate acquiring_create_order api called")
    public void autoInvoke(@Optional("enhancedwap") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOINVOKE, theme)
                .setORDER_ID(CommonHelpers.generateOrderId())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.AUTOINVOKE.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");
    }

    @Parameters({"theme"})
    @Test(description = "end to end transaction")
    public void autoInvoketxn(@Optional("enhancedwap") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOINVOKE, theme)
                .setORDER_ID(CommonHelpers.generateOrderId())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "validate Pending status when payment is not done")
    public void autoInvokePending(@Optional("enhancedwap_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AUTOINVOKE, theme)
                .setORDER_ID(CommonHelpers.generateOrderId())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.");
    }

    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Validate AutoAppInvokeAllowed flag when requestType = payments")
    public void validateAutoAppInvokeAllowedFlag() throws Exception {

        String MERCHANT_ID = Constants.MerchantType.AUTO_APP.getId();
        String ORDER_ID = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(Constants.MerchantType.AUTO_APP, ORDER_ID, user.ssoToken())
                .setRequestType("Payment")
                .setChannelId("WAP")
                .setTxnValue("1")
                .setSsoToken("")
                .setCustId("Test101")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken = response.get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(MERCHANT_ID, ORDER_ID, fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0.1; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Mobile Safari/537.36");
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        String isAutoAppInvokeAllowed = fetchPaymentOptionResponse.get("body.merchantDetails.autoAppInvokeAllowed").toString();

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(isAutoAppInvokeAllowed, "true");
        softAssert.assertAll();
    }
}
