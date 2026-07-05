package scripts;

import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;import com.google.gson.JsonObject;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.*;
import play.api.libs.json.Json;
import java.util.Date;


public class ChargeFeeOnAddMoney extends PGPBaseTest {

    //--------------------------ADDNPAY & ADDMONEY CASES -  FLAG OFF theia.enable.addMoney.surcharge -------------------------------
    @Feature("PAPR-3741")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify AddMoneyPcfEnabled flag is FALSE in FPO for AddnPay txn when ff4j theia.enable.addMoney.surcharge is OFF")
    public void AddMoneyPcfEnabled_flag_in_FPO_flag_OFF() throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay).setTxnValue("100").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("false");
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify AddMoneyPcfEnabled flag is FALSE in FPO for ADD_MONEY txn when ff4j theia.enable.addMoney.surcharge is FALSE")
    public void AddMoneyPcfEnabled_flag_in_FPO_ADDMONEY_flag_OFF() throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN,Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay).setIsNativeAddMoney("true").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("false");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify ADDMONEY fee for AddnPay txn via CC when ff4j theia.enable.addMoney.surcharge is OFF")
    public void AddMoneyFeeTxn_ADDNPAY_CC_Flag_OFF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("10")
                .build();
        WalletHelpers.modifyBalance(user, 2.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd1 = "ACQUIRING_PAY_ORDER";
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd1);
        Assertions.assertThat(logs1).contains("\"chargeAmount\":{\"currency\":\"INR\",\"value\":0}");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify ADDMONEY fee for ADD_MONEY txn via CC when ff4j theia.enable.addMoney.surcharge is OFF")
    public void AddMoneyFeeTxn_ADDMONEY_CC_Flag_OFF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddnPay, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd1 = "FUND_ORDER_PAY";
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd1);
        Assertions.assertThat(logs1).contains("chargeAmount=Money [currency=INR, amount=0]");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify ONUS ADD_MONEY txn via CC when ff4j theia.enable.addMoney.surcharge is OFF")
    public void ONUS_AddMoney_CC_Flag_OFF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.PPBL_VAULT_MID, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd1 = "ACQUIRING_PAY_ORDER";
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd1);
        Assertions.assertThat(logs1).contains("\"chargeAmount\":{\"currency\":\"INR\",\"value\":0}");
    }

    //---------------------------------- FLAG ON theia.enable.addMoney.surcharge ------------------------------------------------
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify AddMoneyPcfEnabled flag is true in FPO for AddnPay txn when ff4j theia.enable.addMoney.surcharge is ON")
    public void AddMoneyPcfEnabled_flag_in_FPO_flag_ON() throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADD_MONEY_SURCHARGE).setTxnValue("100").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("true");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify AddMoneyPcfEnabled flag is true in FPO for ADDMONEY txn when ff4j theia.enable.addMoney.surcharge is ON")
    public void AddMoneyPcfEnabled_flag_in_FPO_ADDMONEY_flag_ON() throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADD_MONEY_SURCHARGE).setIsNativeAddMoney("true").build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("true");
    }

    @Owner(Constants.Owner.VIDHI)
    @Parameters({"theme"})
    @Test(description = "To verify the feeRateCode in BOSS_CHARGE_FEE_BATCH_CONSULT request on ADDNPAY txn via CC when ff4j theia.enable.addMoney.surcharge is ON ")
    public void addMoneyFeeTxn_ADDNPAY_CC_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADD_MONEY_SURCHARGE, theme, user)
                .setTXN_AMOUNT("200")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4166464311356935");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "FEE_BATCH_CONSULT";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("\"feeRateCode\":\"FR1\"");
        Assertions.assertThat(logs).contains("\"addMoneySurcharge\":\"true\"");
    }
    @Owner(Constants.Owner.VIDHI)
    @Parameters({"theme"})
    @Test(description = "To verify the is3PconvFee in PAYMENT_CASHIER_PAY request on ADDNPAY txn via CC when ff4j theia.enable.addMoney.surcharge is ON ")
    public void addMoneyFeeTxn_ADDNPAY_CC_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADD_MONEY_SURCHARGE, theme, user)
                .setTXN_AMOUNT("200")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4166464311356935");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("3PconvFeeAmount");
        Assertions.assertThat(logs).contains("\"is3PconvFee\":\"true\"");
    }
    @Owner(Constants.Owner.VIDHI)
    @Parameters({"theme"})
    @Test(description = "To verify the AddMoney fee on UI is same from API on ADDNPAY txn via CC when ff4j theia.enable.addMoney.surcharge is ON ")
    public void addMoneyFeeTxn_ADDNPAY_CC_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        Double walBal=2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADD_MONEY_SURCHARGE, theme, user)
                .setTXN_AMOUNT("200")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)))+walBal;
        cashierPage.buttonPGPayNow().click();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        String Fee = logs.substring(logs.indexOf("3PconvFeeAmount")+18,logs.indexOf("\",\"userMobile"));
        Double addMoneyFee= (double) (Integer.parseInt(Fee))/100;
        Assertions.assertThat(actualChargeFeeAmt).isEqualTo(addMoneyFee);
    }
    @Owner(Constants.Owner.VIDHI)
    @Parameters({"theme"})
    @Test(description = "To verify successful transaction with ADDMONEY FEE on ADDNPAY txn via CC when ff4j theia.enable.addMoney.surcharge is ON ")
    public void addMoneyFeeTxn_ADDNPAY_CC_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOPOSTPAID);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADD_MONEY_SURCHARGE, theme, user)
                .setTXN_AMOUNT("200")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    //--------------------------SUBSCRIPTION ADD N PAY CASES -  FLAG ON theia.enable.addMoney.surcharge -------------------------------

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-47533")
    @Test(description = "To verify AddMoneyPcfEnabled flag is TRUE in FPO for Subscription AddnPay txn when ff4j theia.enable.addMoney.surcharge is ON")
    public void Subscription_AddMoneyPcfEnabled_flag_in_FPO_flag_ON() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10.00")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("true");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify AddMoneyPcfEnabled flag is FALSE in FPO for Subscription AddnPay txn when MID is not in ff4j theia.enable.addMoney.surcharge")
    public void Subscription_AddMoneyPcfEnabled_flag_in_FPO_flag_OFF() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10.00")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.isAddMoneyPcfEnabled")).isEqualTo("false");

    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the feeRateCode in FEE_BATCH_CONSULT request on Subscription ADDNPAY txn when ff4j theia.enable.addMoney.surcharge is ON ")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4166464311356935");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "FEE_BATCH_CONSULT";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("\"feeRateCode\":\"FR1\"");
        Assertions.assertThat(logs).contains("\"addMoneySurcharge\":\"true\"");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000004\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the is3PconvFee in ACQUIRING_PAY_ORDER request on Subscription ADDNPAY txn when ff4j theia.enable.addMoney.surcharge is ON ")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4166464311356935");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("3PconvFeeAmount");
        Assertions.assertThat(logs).contains("\"is3PconvFee\":\"true\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the AddMoney fee on UI is same from API on Subscription ADDNPAY txn when ff4j theia.enable.addMoney.surcharge is ON ")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        cashierPage.buttonPGPayNow().click();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        String Fee = logs.substring(logs.indexOf("3PconvFeeAmount") + 18, logs.indexOf("\",\"userMobile"));
        Double addMoneyFee = (double) (Integer.parseInt(Fee)) / 100;
        Assertions.assertThat(actualChargeFeeAmt).isEqualTo(addMoneyFee);

    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful PARTIAL ADDNPAY subscription transaction with ADDMONEY FE")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful FULL ADDNPAY subscription transaction with ADDMONEY fee")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 0.0;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful Subscription ADDNPAY  transaction on >4000 amt with ADDMONEY FEE ")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_CC_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4500.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4166464311356935");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        String UIamt=cashierPage.getTotalAmountOnCCDC().getText();
        UIamt=UIamt.replaceAll(",","");
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(UIamt));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the feeRateCode in FEE_BATCH_CONSULT request on Subscription ADDNPAY corporate DC txn")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "FEE_BATCH_CONSULT";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("\"feeRateCode\":\"FR1\"");
        Assertions.assertThat(logs).contains("\"addMoneySurcharge\":\"true\"");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000004\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the is3PconvFee in ACQUIRING_PAY_ORDER request on Subscription ADDNPAY corporate DC txn")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        Assertions.assertThat(logs).contains("3PconvFeeAmount");
        Assertions.assertThat(logs).contains("\"is3PconvFee\":\"true\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify the AddMoney fee on UI is same from API on Subscription ADDNPAY corporate DC txn")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        actualChargeFeeAmt = Math.round(actualChargeFeeAmt * 100.0) / 100.0;
        cashierPage.buttonPGPayNow().click();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        String Fee = logs.substring(logs.indexOf("3PconvFeeAmount") + 18, logs.indexOf("\",\"userMobile"));
        Double addMoneyFee = (double) (Integer.parseInt(Fee)) / 100;
        Assertions.assertThat(actualChargeFeeAmt).isEqualTo(addMoneyFee);
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful PARTIAL ADDNPAY DC corporate subscription transaction with ADDMONEY FEE")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful FULL ADDNPAY DC corporate subscription transaction with ADDMONEY fee")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 0.0;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("200.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "To verify successful Subscription ADDNPAY corporate DC transaction on >4000 amt with ADDMONEY FEE ")
    public void Subscription_addMoneyFeeTxn_ADDNPAY_DC_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.LOGIN);
        Double walBal = 2.00;
        WalletHelpers.modifyBalance(user, walBal);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL_TXN, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4500.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.CORPORATE_INDIAN_DC);
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        String UIamt=cashierPage.getTotalAmountOnCCDC().getText();
        UIamt=UIamt.replaceAll(",","");
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(UIamt));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt))) + walBal;
        Assertions.assertThat(actualChargeFeeAmt).isNotEqualTo("");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

}

