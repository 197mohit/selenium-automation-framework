package scripts.api.fpoMigration;

import com.paytm.api.FetchBalance;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.PRAMOD_KUMAR;



import com.paytm.api.FetchBalance;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.PRAMOD_KUMAR;
public class fpoMigratioWallet extends PGPBaseTest {
        private final CheckoutJsCheckoutPage checkoutJSPage = new CheckoutJsCheckoutPage();

        public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
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

        public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse){
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("post");
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
            Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
        }
        public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO) {
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNDATE()).isNotNull();
        }

        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "Validate success checkout lpv response for balance ")
        public void validateSuccessWalletBalanceinFPO() throws Exception {
            User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
            String mid = Constants.MerchantType.FPO_BALANCE_MIGRATION.getId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.FPO_BALANCE_MIGRATION)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String newOrderId = initTxnDTO.getBody().getOrderId();
            initTxnDTO.getBody().setOrderId(newOrderId);
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");

            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                    mid, initTxnResponse.getBody().getTxnToken(), newOrderId, "PTM" + newOrderId)
                    .setPaymentMode("NET_BANKING")
                    .setChannelCode(Constants.Bank.ICICI.toString())
                    .setAuthMode("USRPWD")
                    .setMpin("1234")
                    .build();
            ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[BALANCE]");
            String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LitePayviewConsultResponseBody");
            Assertions.assertThat((Walletlogs))
                    .contains("paytmWalletBalance").
                    contains("otherSubWalletBalance");

        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "Validate checkout lpv where paymethod is blank")
        public void validateSuccessWalletBalanceinFetchWalletBalance() throws Exception {
            User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
            String mid = Constants.MerchantType.FPO_BALANCE_MIGRATION_FF4J_DISABLED.getId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.FPO_BALANCE_MIGRATION_FF4J_DISABLED)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String newOrderId = initTxnDTO.getBody().getOrderId();
            initTxnDTO.getBody().setOrderId(newOrderId);
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");

            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                    mid, initTxnResponse.getBody().getTxnToken(), newOrderId, "PTM" + newOrderId)
                    .setPaymentMode("NET_BANKING")
                    .setChannelCode(Constants.Bank.ICICI.toString())
                    .setAuthMode("USRPWD")
                    .setMpin("1234")
                    .build();
            ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            assertPTCCommonResponse(ptcResponse);
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
            NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateCurrency("INR")
                    .validateMid(mid)
                    .validateOrderId(initTxnDTO.orderFromBody())
                    .validatePaymentMode("NB")
                    .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                    .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                    .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                    .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateGatewayName(Constants.Bank.ICICI.toString())
                    .validateBankName(Constants.Bank.ICICINB.toString())
                    .assertAll();
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[]");
            String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FETCH_WALLET_BALANCE");
            Assertions.assertThat((Walletlogs))
                    .contains("\"status\":\"SUCCESS\"")
                    .contains("\"statusCode\":\"SUCCESS\"")
                    .contains("paytmWalletBalance").
                    contains("otherSubWalletBalance");

        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "validate checkout lpv response for balance when user is not logged in")
        public void validateFPOResponsewithoutSSO() throws Exception {
            User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.FPO_BALANCE_MIGRATION_FF4J_DISABLED)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String newOrderId = initTxnDTO.getBody().getOrderId();
            initTxnDTO.getBody().setOrderId(newOrderId);
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[]");
        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "Validate Success walletBalancein FPO percenatge is 100 in ff4j strategy")
        public void validateSuccessWalletBalanceinFPOforpercentageBasedUser() throws Exception {
            User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
            String mid = Constants.MerchantType.FPO_BALANCE_MIGRATION.getId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.FPO_BALANCE_MIGRATION)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String newOrderId = initTxnDTO.getBody().getOrderId();
            initTxnDTO.getBody().setOrderId(newOrderId);
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[BALANCE]");

        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "Validate Success walletBalancein FPO percenatge is 0 in ff4j strategy")
        public void validateSuccessfetchWalletBalanceinFPOforpercentageBasedUser() throws Exception {
            User user = userManager.getForRead(Label.UPIPUSHPG2);
            String mid = Constants.MerchantType.FPO_BALANCE_MIGRATION.getId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.FPO_BALANCE_MIGRATION)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String newOrderId = initTxnDTO.getBody().getOrderId();
            initTxnDTO.getBody().setOrderId(newOrderId);
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[]");
            String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FETCH_WALLET_BALANCE");
            Assertions.assertThat((Walletlogs))
                    .contains("\"status\":\"SUCCESS\"")
                    .contains("\"statusCode\":\"SUCCESS\"")
                    .contains("paytmWalletBalance").
                    contains("otherSubWalletBalance");

        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "fetchuserbalance api success")
        public void successfulFetchBalanceWallet() throws Exception {
            User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
            Constants.MerchantType merchant = Constants.MerchantType.FPO_BALANCE_MIGRATION;
            FetchBalance fetchBalance  = new FetchBalance(merchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
            JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
            Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[BALANCE]");
        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "fetchuserbalance api success")
        public void successfulFetchBalancesubWalletwherepercentisZERO() throws Exception {
            User user = userManager.getForWrite(Label.UPIPUSHPG2);
            Constants.MerchantType merchant = Constants.MerchantType.FPO_BALANCE_MIGRATION;
            FetchBalance fetchBalance  = new FetchBalance(merchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
            JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
            Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[]");
            String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"FETCH_WALLET_BALANCE");
            Assertions.assertThat((Walletlogs))
                    .contains("\"status\":\"SUCCESS\"")
                    .contains("\"statusCode\":\"SUCCESS\"")
                    .contains("paytmWalletBalance").
                    contains("otherSubWalletBalance");
        }
        @Owner(PRAMOD_KUMAR)
        @Feature("PGP-45059")
        @Test(description = "fpo v5 api success")
        public void validatefpoV5(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
            User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
            String txnamount = "2";
            Constants.MerchantType merchant = Constants.MerchantType.FPO_BALANCE_MIGRATION;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount)
                    .setOrderId(CommonHelpers.generateOrderId())
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
            config.merchant.setMid(Constants.MerchantType.FPO_BALANCE_MIGRATION.getId());
            config.data.setOrderId(initTxnDTO.orderFromBody());
            config.data.setToken(txnToken);
            config.data.setAmount(txnamount);
            checkoutJSPage.createCheckoutJsOrder(config);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(Constants.PayMode.SAVED_UPI);

            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"LITEPAYVIEW_CONSULT");
            Assertions.assertThat(logs).contains("balancePayMethods=[BALANCE]");
        }
    @Owner(PRAMOD_KUMAR)
    @Feature("PGP-45059")
    @Test(description = "fetchuserbalance when subwallet has some balance" )
    public void successfulFetchBalancesubWalletwhereGiftVoucherhasSomeAmount() throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = Constants.MerchantType.FPO_BALANCE_MIGRATION;
        FetchBalance fetchBalance  = new FetchBalance(merchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"LITEPAYVIEW_CONSULT");
        Assertions.assertThat(logs).contains("balancePayMethods=[]");
        String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"FETCH_WALLET_BALANCE");
        Assertions.assertThat((Walletlogs))
                .contains("\"status\":\"SUCCESS\"")
                .contains("\"statusCode\":\"SUCCESS\"")
                .contains("paytmWalletBalance").
                contains("otherSubWalletBalance");
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PGP-45059")
    @Test(description = "fetchuserbalance api success when subwallet has zero balance")
    public void successfulFetchBalanceSubWallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = Constants.MerchantType.FPO_BALANCE_MIGRATION;
        FetchBalance fetchBalance  = new FetchBalance(merchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"LITEPAYVIEW_CONSULT");
        Assertions.assertThat(logs).contains("balancePayMethods=[]");
        String Walletlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"FETCH_WALLET_BALANCE");
        Assertions.assertThat((Walletlogs))
                .contains("\"status\":\"SUCCESS\"")
                .contains("\"statusCode\":\"SUCCESS\"")
                .contains("paytmWalletBalance").
                contains("otherSubWalletBalance");
    }
    }

