package scripts.api.theia.PaymentCashierPay;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class RefererUrl extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in payment cashier pay for successful DC txn in enhanced flow")
    public void refererurlenhanced(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String grepcmd = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log" + "| grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"https://pgp-automation1.paytm.in/");


    }

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in payment cashier pay for successful DC txn in AppInvoke")
    public void refererurlappinvoke(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setTxnValue(String.valueOf(35))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .build();


        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log" + "| grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"https://pgp-automation1.paytm.in/");

    }

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in payment cashier pay for successful DC txn in Checkoutjs")
    public void refererurlcheckoutjs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .build();

        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() +" " + LocalConfig.THEIA_FACADE_LOGS + "| grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"" + LocalConfig.CHECKOUTJS_URL);

    }

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in ACQUIRING_CREATE_ORDER_AND_PAY for successful COP CC txn in enhanced")
    public void refererurlenhacedCOP(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_COP, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PG2_COP.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log" + "| grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"https://pgp-automation1.paytm.in/\\");

    }

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in ACQUIRING_CREATE_ORDER_AND_PAY for successful COP DC txn in AppInvoke")
    public void refererurlappinvokeCOP(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP)
                .setTxnValue(String.valueOf(35))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.COP, initTxnDTO.getBody().getOrderId(), txnToken)
                .build();


        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log" + "| grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"https://pgp-automation1.paytm.in/");

    }

    @Owner(Constants.Owner.RAHUL_KANT)
    @Feature("PGP-38629")
    @Parameters({"theme"})
    @Test(description = "Verify refererurl in ACQUIRING_CREATE_ORDER_AND_PAY for successful COP DC txn in Checkoutjs")
    public void refererurlcheckoutjsCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COP).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PG2_COP, initTxnDTO.getBody().getOrderId(), txnToken)
                .build();

        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() +" " + LocalConfig.THEIA_FACADE_LOGS + " | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String refererurl = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(refererurl).contains("refererURL\\\":\\\"" + LocalConfig.CHECKOUTJS_URL);

    }

}
