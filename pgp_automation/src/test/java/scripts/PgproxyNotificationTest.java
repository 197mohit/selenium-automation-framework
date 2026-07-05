package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.RefundSuccessNotifyApi;
import com.paytm.api.notification.OnlineSettlementNotify;
import com.paytm.api.notification.WithdrawNotify;
import com.paytm.api.notification.WithdrawNotifyWalletSettlement;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.PAREEKSHITH;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class PgproxyNotificationTest extends PGPBaseTest {
    private static final String auto = "MERCHANT_AUTO_WITHDRAW";
    private static final String manual = "MERCHANT_WITHDRAW";
    private static final String IFSC_CODE = "WALL0123456";
    private static final String accountNumber = "1234567890";
    private static final String txnAmount = "10000";
    private static final String txnAmount1 = "20000";
    private final Constants.MerchantType merchantType = Constants.MerchantType.TWSSettlementMerchant;
    private final Constants.MerchantType bwMerchantType = Constants.MerchantType.BW_Only;

    @Owner("Anushka_Goldi")
    @Feature("PGP-38888")
    @Test(description = "Check in RT Settlement that settleType and bankStatus fields are in log of pgproxy Notification")
    public void CheckFieldOfDTOinRTSettlement() throws InterruptedException {
        String mid = Constants.MerchantType.DTO_FIELDS_ALIPAYMID.getId();
        String settleType= "TRANSACTION_WISE_SETTLEMENT";
        OnlineSettlementNotify onlineSettlementNotify = new OnlineSettlementNotify(mid, settleType);
        JsonPath withDrawJson1 = onlineSettlementNotify.execute().jsonPath();

        String grepcmd = "grep \"" + "\" /paytm/logs/pgproxy-notification.log  | " +
                "grep \"" + Constants.MerchantType.DTO_FIELDS_ALIPAYMID.getId() + "\" | grep \"Request received\" | grep \"\" ";
        String pgproxyNotificationLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);

        Assertions.assertThat(pgproxyNotificationLogs).contains("request")
                .contains("body")
                .contains("\"bankStatus\":\"PENDING\"");

        Assertions.assertThat(pgproxyNotificationLogs).contains("request")
                .contains("body")
                .contains("\"settleType\":\"TRANSACTION_WISE_SETTLEMENT\"");
    }

    @Owner("Anushka_Goldi")
    @Feature("PGP-38888")
    @Test(description = "Check in Refund that issuerBankInstCode fields is in log of pgproxy Notification")
    public void CheckFieldOfDTOinRefundApi() throws InterruptedException {
        String mid = Constants.MerchantType.DTO_FIELDS_ALIPAYMID.getId();
        RefundSuccessNotifyApi refundSuccessNotifyApi = new RefundSuccessNotifyApi(mid);
        JsonPath withDrawJson1 = refundSuccessNotifyApi.execute().jsonPath();

        String grepcmd = "awk '/"+mid+"/ &&  /Request received/' /paytm/logs/pgproxy-notification.log";
        String pgproxyNotificationLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);

        Assertions.assertThat(pgproxyNotificationLogs).contains("request")
                .contains("body")
                .contains("\"issuerBankInstCode\":\"PNB\"");
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-37875")
    @Test(description = "Stop messages for Manual withdraw for BW merchants")
    public void StopWithdrawSMSForBWMerchantwithflagon() throws InterruptedException {
        String orderId = CommonHelpers.generateOrderId();
        WithdrawNotifyWalletSettlement withdrawNotifyWalletSettlement = new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("SUCCESS")
                .setResultId("00000000")
                .setResultStatus("S")
                .setResultMsg("success")
                .setOperation(manual)
                .build();
        JsonPath walletManualWithdrawJson = withdrawNotifyWalletSettlement.execute().jsonPath();
        String grepcmd = "grep \"" + "\" /paytm/logs/communicationGateway.log  | " +
                "grep \"" + merchantType.getId() + "\" | grep \"MessageService.process()\"";
        String commgatewaylogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commgatewaylogs).isEmpty();
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-37875")
    @Test(description = "Send messages for Auto withdraw for BW merchants")
    public void SendAutoWithdrawSMSForBWMerchantwithflagon() throws InterruptedException {
        String orderId = CommonHelpers.generateOrderId();
        WithdrawNotifyWalletSettlement withdrawNotifyWalletSettlement = new WithdrawNotifyWalletSettlement.Builder(bwMerchantType.getId(), orderId)
                .setFundAmount(txnAmount1)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("SUCCESS")
                .setResultId("00000000")
                .setResultStatus("S")
                .setResultMsg("success")
                .setOperation(auto)
                .build();
        JsonPath walletManualWithdrawJson = withdrawNotifyWalletSettlement.execute().jsonPath();
        Thread.sleep(10000);
        String grepcmd = "grep \"" + "\" /paytm/logs/communicationGateway.log  | " +
                "grep \"" + bwMerchantType.getId() + "\" | grep \"MessageService.process()\"";
        String commgatewaylogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        if (commgatewaylogs.isEmpty()) { commgatewaylogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd); }
        Assertions.assertThat(commgatewaylogs).contains("msg=Rs. 200.00 has been settled to your linked Paytm wallet");
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-37875")
    @Test(description = "Stop messages for successful settlement for TWS merchants")
    public void StopSettlementSMSForTWSMerchantwithflagon() throws InterruptedException {
        OnlineSettlementNotify onlineSettlementNotify = new OnlineSettlementNotify(merchantType.getId(), "SUCCESS", "TRANSACTION_WISE_SETTLEMENT");
        onlineSettlementNotify.setContext("request.body.bankStatus","SUCCESS");
        onlineSettlementNotify.setContext("request.body.merchantSolutionType","OFFLINE");
        onlineSettlementNotify.setContext("request.body.notificationStatus","SUCCESS");
        JsonPath SettlementNotify = onlineSettlementNotify.execute().jsonPath();
        String grepcmd = "grep \"" + "\" /paytm/logs/communicationGateway.log  | " +
                "grep \"" + merchantType.getId() + "\" | grep \"MessageService.process()\"";
        String commgatewaylogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commgatewaylogs).isEmpty();
    }
}
