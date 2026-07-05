package scripts.api.linkservice;

import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


public class LinkSplitSettlement extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }

    @Owner("Anushka")
    @Feature("PGP-32824")
    @Parameters({"theme"})
    @Test(description = "Split settlement on Links")
    public void Link_Split_Settlement(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String Aggr_MID = Constants.MerchantType.LINK_SPLIT_SETTLEMENT_AGG.getId();
        setUserAndMId(Aggr_MID.toString());
        CreateNewLink newLink = new CreateNewLink(Aggr_MID);
        JsonPath withDrawJson1 = newLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"None","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForOldFlow();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String grep= "grep \"" +  "\" /paytm/logs/theia_facade.log  | " +
                "grep \"" + Constants.MerchantType.LINK_SPLIT_SETTLEMENT_AGG.getId() +
                    "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\" ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grep);

        Assertions.assertThat(theiaFaacadeLogs).contains("\"MID\":\"Splits50781612685970\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"targetMerchantId\":\"216820000008158453470\",\"paytmMerchantId\":\"216820000008158453470\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("userId")
                .contains("externalUserId")
                .contains("nickname")
                .contains("\"externalUserType\":\"MERCHANT\"");
    }

}
