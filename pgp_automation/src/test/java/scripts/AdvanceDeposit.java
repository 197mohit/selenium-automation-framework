package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.base.test.Group.Status.TO_BE_FIXED;

/**
 * mock property value:
 * theia --> facade --> kybdata.service.base.url=https://pgp-automation.paytm.in/mockbank
 */
@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Somesh")
public class AdvanceDeposit extends PGPBaseTest {

    @Feature("PGP-22531")
    @Story(Constants.Sprint.SPRINT_THEMATIC)
    @Parameters({"theme"})
    @Test(description = "Check that user is able to complete a txn even when KYB rejects the role of the user.")
    public void t1(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_DEPOSIT,theme)
                .setTemplateId("2019110000000000000000000000000000000010")
                .setbId("A0dstl0uvkqei010_reject")
               // .setCorporateCustId(user.custId())
                .setSSO_TOKEN(user.walletToken())
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabAdvanceDeposit().assertNotVisible();
        cashierPage.verifyPaymentModeDisplayed(Constants.PayMode.CC);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .AssertAll();
    }

    @Feature("PGP-22531")
    @Story(Constants.Sprint.SPRINT_THEMATIC)
    @Parameters({"theme"})
    @Test(description = "Check that txn with Advance account is done when KYB accepts the role of the user.")
    public void t2(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_DEPOSIT,theme)
                .setTemplateId("2019110000000000000000000000000000000010")
                .setbId("A0dstl0uvkqei010")
                .setCorporateCustId(user.custId())
                .setSSO_TOKEN(user.walletToken())
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.ADVANCE_DEPOSIT_ACCOUNT);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("Paytm Advance Account")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .AssertAll();
    }

    @Feature("PGP-22531")
    @Story(Constants.Sprint.SPRINT_THEMATIC)
    @Parameters({"theme"})
    @Test(description = "Validate if advance account is visible on cashier page if logged in on cashier page with mapped user id.")
    public void t3(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_DEPOSIT,theme)
                .setTemplateId("2019110000000000000000000000000000000010")
                .setbId("A0dstl0uvkqei010")
                .setCorporateCustId(user.custId())
//                .setSSO_TOKEN(user.walletToken())
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.ADVANCE_DEPOSIT_ACCOUNT);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("Paytm Advance Account")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Successful Payment Via Advance Deposit")
    public void paymentViaAdvanceDeposit(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_DEPOSIT,theme)
                .setSSO_TOKEN(user.walletToken())
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.ADVANCE_DEPOSIT_ACCOUNT);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                 .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                 .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                 .validateMid(orderDTO.getMID())
                 .validatePaymentMode("Paytm Advance Account")
                 .validateStatus("TXN_SUCCESS")
                 .validateTxnType("SALE")
                 .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Advance Deposit Low Balance Error Message Case")
    public void lowBalanceErrorMessageAdvanceDeposit(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ADVANCE_DEPOSIT,theme)
                .setSSO_TOKEN(user.walletToken())
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabAdvanceDeposit().click();
//        cashierPage.advanceDepositLowBalanceErrorMessage().waitUntilVisible();
//        cashierPage.advanceDepositLowBalanceErrorMessage().assertText("You don’t have enough balance for this payment");
    }
    protected String Validate_InitTxn(InitTxnDTO initTxnDTO) {
        String txnToken;
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        if (StringUtils.contains(response.jsonPath().getString("body.resultInfo.resultCode"), "1001")) {
            String resultCode = response.jsonPath().get("body.resultInfo.resultCode").toString();
            return resultCode;
        } else {
            txnToken = response.jsonPath().getString("body.txnToken");
            Assertions.assertThat(txnToken).withFailMessage("Txn token is %s", txnToken).isNotNull();
        }
        return txnToken;
    }
    protected JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Successful Payment Via Advance Deposit in NATIVE flow")
    public void paymentViaAdvanceDepositNative(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADVANCE_DEPOSIT).build();
        String txnToken = Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.ADVANCE_DEPOSIT,initTxnDTO.orderFromBody(), txnToken, "ADVANCE_DEPOSIT_ACCOUNT").build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "ADVANCE_DEPOSIT_ACCOUNT", "false");
        new CheckoutPage().createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("Paytm Advance Account")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .AssertAll();
    }
}