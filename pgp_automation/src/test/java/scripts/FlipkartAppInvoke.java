package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.conditions.Condition;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.function.BooleanSupplier;

@Owner(Constants.Owner.PUSHKAL)
@Feature("PGP-27524")
public class FlipkartAppInvoke extends PGPBaseTest {

    String shortLink = "SHORT_LINK=";

    @Test(description = "validate whether UPI transaction is successfully performed when app invoke flow is " +
            "triggered for txn amount > wallet amount using v1/PTC txn")
    public void flipkartAppInvoke(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 10.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDnPAY_DISABLE)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.ADDnPAY_DISABLE, "TXN_TOKEN", txnToken)
                .setChannelId("WAP")
                .setPaymentFlow("NONE")
                .setCustId(null)
                .setPayerAccount(null)
                .setWebsite(null)
                .setExtendInfo(null)
                .setRiskExtendInfo(null)
                .setTokenType("TXN_TOKEN")
                .setOrderId(initTxnDTO.orderFromBody())
                .setAuthMode("USRPWD")
                .setPaymentMode("BALANCE")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        softAssertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        softAssertions.assertAll();

        String callBackUrl = processTxnV1Response.getBody().getCallBackUrl();
        DriverManager.getDriver().get(callBackUrl);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.stripPayUsingPaytmApp().isClickable());
        cashierPage.stripPayUsingPaytmApp().click();

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/pgproxy-notification.log | " +
                "grep \"CreateSendSmsBody\"";
        String pgProxyLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION,grepcmd);
        String subStr = pgProxyLogs.substring(pgProxyLogs.indexOf(shortLink),pgProxyLogs.length()-1);
        String shortLinkUrl = subStr.substring(shortLink.length(),subStr.indexOf(","));

        DriverManager.getDriver().get(shortLinkUrl);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .assertAll();

    }
}
