package scripts.PureHybrid;


import com.paytm.CreateToken;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.UUID;

public class AddnPayHybrid extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Feature("")
    @Owner("ANUSHKA_GOLDI")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using DC when ADDANDPAY & Hybrid both added on mid")
    public void validate_AddnpayHYBRID_Txn_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        String merchantId= Constants.MerchantType.AddNpayHybrid.getId();
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
//        if(!cashierPage.checkBoxPPI().isChecked()){
//            cashierPage.checkBoxPPI().click();
//        }
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Feature("")
    @Owner("ANUSHKA_GOLDI")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using NB when ADDANDPAY & Hybrid both added on mid")
    public void validate_AddnpayHYBRID_Txn_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        String merchantId= Constants.MerchantType.AddNpayHybrid.getId();
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
//        if(!cashierPage.checkBoxPPI().isChecked()){
//            cashierPage.checkBoxPPI().click();
//        }
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY);
    }

    @Feature("")
    @Owner("ANUSHKA_GOLDI")
    @Parameters({"theme"})
    @Test(description = "Validate ADDANDPAY & HYBRID both")
    public void validate_AddnpayHYBRID_Txn_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        double txnAmount = 2.0;
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,txnAmount -1.0);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddNpayHybrid)
                .setSsoToken(user.ssoToken())
                .setTxnValue(Double.toString(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertEquals(fetchPaymentOptionsJson.getString("body.supportedPaymentFlows"),"[HYBRID, ADDANDPAY]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.hybridMode")).containsOnlyOnce("PRIMARY");
    }

}
