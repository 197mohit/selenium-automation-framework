package scripts.notificationTemplate;

import com.paytm.ServerConfigProvider;
import com.paytm.api.notification.OnlineSettlementNotify;
import com.paytm.api.notification.WithdrawNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.MerchantType.*;

@Owner(Constants.Owner.HIMANSHU)
@Feature("PGP-38316")

public class PGP38316NewFGWMappingwithContextualErrorMessagesFailedSettlementTxn extends PGPBaseTest
{
    public String bizType,instErrorCode,txnAmount;
    private static final String accountNumber = "******1797";
    @Test(description = "Verify that we are getting correct template is getting picked for Merchant auto withdraw settlement failure with bank fail specific reason")
    public void FgwBankFailSpecificReason_MerchantAutoWithdraw() throws Exception
    {
        txnAmount="5000";
        bizType="MERCHANT_AUTO_WITHDRAW";
        instErrorCode="FGW_BANK_FAIL_SPECIFIC_REASON";
        Constants.MerchantType merchantType = MerchantAutoWithdrawSettlement;
        String orderId = CommonHelpers.generateOrderId();
        String cmdToFetchCommGatewayLogger = "grep " + merchantType.getId() + " /paytm/logs/communicationGateway.log | grep 'Inside LOW_PRIORITY_SMS message queue'";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(instErrorCode)
                .setOperation(bizType)
                .setAccount(accountNumber)
                .build();
        withdrawNotify.execute();
        String CommGatewayLogger = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmdToFetchCommGatewayLogger);
        Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmdToFetchCommGatewayLogger), s -> !"".equals(s));
        Assertions.assertThat(CommGatewayLogger).contains("Transfer of Rs. 50.00 in a/c **1797 is declined by your bank. We will try again tomorrow with today's payments. Open pytm.biz/Settlement to transfer now. Team Paytm");
    }
    @Test(description = "Verify that we are getting correct template is getting picked for Merchant withdraw settlement failure with fgw account frozen")
    public void FgwAccountFrozen_MerchantWithdraw()
    {
        txnAmount="5000";
        bizType="MERCHANT_WITHDRAW";
        instErrorCode="FGW_ACCOUNT_FROZEN";
        Constants.MerchantType merchantType = MerchantWithdrawSettlement;
        String orderId = CommonHelpers.generateOrderId();
        String cmdToFetchCommGatewayLogger = "grep " + merchantType.getId() + " /paytm/logs/communicationGateway.log | grep 'Inside LOW_PRIORITY_SMS message queue'";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(instErrorCode)
                .setOperation(bizType)
                .setAccount(accountNumber)
                .build();
        withdrawNotify.execute();
        String CommGatewayLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmdToFetchCommGatewayLogger), s -> !"".equals(s));
        Assertions.assertThat(CommGatewayLogger).contains("Unable to settle Rs. 50.00 in your a/c **1797 as this account has frozen by your bank. Open pytm.biz/acpymtstg to change bank a/c to get settlements. Paytm");
    }

    @Test(description = "Verify that we are getting correct template is getting picked for Online settlement failure with bank fail specific reason")
    public void FgwBankFailSpecificReason_OnlineSettlement()
    {
        bizType="ONLINE_SETTLEMENT";
        instErrorCode="FGW_BANK_FAIL_SPECIFIC_REASON";
        Constants.MerchantType merchantType = ONLINE_SETTLEMENT;
        String cmdToFetchCommGatewayLogger = "grep " + merchantType.getId() + " /paytm/logs/communicationGateway.log | grep 'Inside LOW_PRIORITY_SMS message queue'";
        OnlineSettlementNotify onlineSettlementNotify = new OnlineSettlementNotify(merchantType.getId(),instErrorCode,bizType);
        onlineSettlementNotify.execute();
        String CommGatewayLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmdToFetchCommGatewayLogger), s -> !"".equals(s));
        Assertions.assertThat(CommGatewayLogger).contains("Alert! Bank transfer of Rs 429.00 has been declined by your bank for a/c 409000**7762. Your money is safe with us. You should receive the money within next 24 hours.View settlement details on pytm.biz/Settlement. Team Paytm");
    }

    @Test(description = "Verify that we are getting correct template is getting picked for realtime settlement failure with fgw account frozen")
    public void FgwAccountFrozen_RealtimeSettlement()
    {
        bizType="TRANSACTION_WISE_SETTLEMENT";
        instErrorCode="FGW_ACCOUNT_FROZEN";
        Constants.MerchantType merchantType = TWSSettlementMerchant;
        String cmdToFetchCommGatewayLogger = "grep " + merchantType.getId() + " /paytm/logs/communicationGateway.log | grep 'Inside LOW_PRIORITY_SMS message queue'";
        OnlineSettlementNotify onlineSettlementNotify = new OnlineSettlementNotify(merchantType.getId(),instErrorCode,bizType);
        onlineSettlementNotify.execute();
        String CommGatewayLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmdToFetchCommGatewayLogger), s -> !"".equals(s));
        Assertions.assertThat(CommGatewayLogger).contains("Unable to settle Rs. 429.00 in your a/c 409000**7762 as this account has frozen by your bank. Open pytm.biz/acpymtstg to change bank a/c to get settlements. Paytm");
    }
}