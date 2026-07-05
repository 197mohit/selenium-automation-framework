package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


@Owner(Constants.Owner.ROHIT)
public class SendOnusMerchantidentifierFlag extends PGPBaseTest {
    CheckoutPage checkoutPage = new CheckoutPage();
    @Feature("PGP-17381")
    @Parameters({"theme"})
    @Test(description = "verify onusmerchant is true and orderID sent in payment request for upi txn with onus merchant ")
    public void verifyPaymentRequestForOnusMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateCheckSum(MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getKey())
                .assertAll();
        String grepcmd = "grep 'Payment Request| BankCode:PPBLC | PayMethod:UPI' /paytm/logs/instaproxy.log | grep "+orderDTO.getORDER_ID()+"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("onusmerchant=true");
        Assertions.assertThat(logs).contains("orderID="+orderDTO.getORDER_ID()+"");
        }


    @Parameters({"theme"})
    @Test(description = "verify onusmerchant is false and orderID is null in payment request for upi collect txn with offus merchant ")
    public void verifyPaymentRequestForOffusMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI_COLLECT, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateCheckSum(MerchantType.UPI_COLLECT.getKey())
                .assertAll();
        String grepcmd = "grep 'Payment Request| BankCode:PPBLC | PayMethod:UPI' /paytm/logs/instaproxy.log | grep "+orderDTO.getORDER_ID()+"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("onusmerchant=false");
        Assertions.assertThat(logs).contains("orderID=NULL");
    }
}