package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;

public class UPICollectPendingTxn extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();



    @Owner(Constants.Owner.HIMANSHU)
    @Test(description = "Verify UPI Collect pending transaction on enhanced flow")
    public void pendingUPICollect_Enhanced(@Optional("enhancedweb_revamp") String theme) throws Exception
    {

        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.UPIPUSHPG2, theme).setTXN_AMOUNT("15").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads("480");
        responsePage
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.UPIPUSHPG2.getId())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .assertAll();
    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.HIMANSHU)
    @Test(description = "Verify UPI Collect pending transaction on checkout js flow")
    public void pendingUPICollect_CheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        Constants.MerchantType merchant = MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setOrderId(LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId())
                .setTxnValue("15")
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads("580");
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(newOrderId)
                .validateRespMsg(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }

}
