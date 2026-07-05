package scripts.Sprint28;

import com.paytm.api.LinkBasedService;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.linkbasedservice.LinkOTPLimitFactory;
import com.paytm.pages.linkbasedservice.LinkPaymentOTPLimit;
import com.paytm.pages.linkbasedservice.LinkPaymentOTPLimitWEB;
import io.qameta.allure.Epic;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

//Will Enable TC for this once link based issue has been fixed
@Epic(Constants.Sprint.SPRINT28_2)
@Story("PGP-16003")
@Owner("Tarun")
public class LimitOtpLinkBased extends PGPBaseTest {
    private static final String INVALID_OTP = CommonHelpers.getInvalidOTP();
    private static final int INVALID_OTP_MAX_COUNT =3;
    private static final String txnAmount = "1";

    private void assertOTPRetryLimit(LinkPaymentOTPLimit linkPaymentOTPLimit) throws InterruptedException {
        for(int i=1; i<=INVALID_OTP_MAX_COUNT;i++)
        {
            linkPaymentOTPLimit.enterOTP(INVALID_OTP);
            if (i != INVALID_OTP_MAX_COUNT)
                Assertions.assertThat(linkPaymentOTPLimit.errorMessage().getText()).isEqualTo("Invalid OTP entered.");
            else
                Assertions.assertThat(linkPaymentOTPLimit.errorMessage().getText()).isEqualTo("You have exceeded the max retries for sending OTP. Please try after sometime");
        }
    }
//old link cases comment out
/*    @Deprecated
    @Test(description = "Verify after 3 incorrect OTP, we should get breach limit message",enabled = false)
    public void verifyBreachNumber() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        LinkBasedService linkBasedService  = new LinkBasedService(merchantType.getId(),txnAmount);
        JsonPath jsonPath = linkBasedService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("API failed").isEqualTo("SUCCESS");
        LinkPaymentOTPLimit linkPaymentOTPLimit = new LinkPaymentOTPLimitWEB();
        linkPaymentOTPLimit.launchLoginPage(jsonPath.getString("body.longUrl"));
        linkPaymentOTPLimit.enterUserAndAmount(user,txnAmount);
        assertOTPRetryLimit(linkPaymentOTPLimit);

    }*/

//    @Deprecated
//    @Parameters({"theme"})
//    @Test(description = "Verify 3rd correct OTP will lead to success of transaction",enabled = false)
    public void verifySuccessTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        LinkBasedService linkBasedService  = new LinkBasedService(merchantType.getId(),txnAmount);
        JsonPath jsonPath = linkBasedService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("API failed").isEqualTo("SUCCESS");

        LinkPaymentOTPLimit linkPaymentOTPLimit = LinkOTPLimitFactory.getLinkPage(theme);

        linkPaymentOTPLimit.launchLoginPage(jsonPath.getString("body.longUrl"));
        linkPaymentOTPLimit.enterUserAndAmount(user,txnAmount);

        linkPaymentOTPLimit.enterOTP("111111");//Incorrect OTP
        Assertions.assertThat(linkPaymentOTPLimit.errorMessage().getText()).isEqualTo("Invalid OTP entered.");

        linkPaymentOTPLimit.enterOTP("222222");//Incorrect OTP
        Assertions.assertThat(linkPaymentOTPLimit.errorMessage().getText()).isEqualTo("Invalid OTP entered.");

        linkPaymentOTPLimit.enterCorrectOTP(user);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        String orderId = linkPaymentOTPLimit.fetchOrderId();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }
//old link cases comment out
    /*
    @Deprecated
    @Test(description = "Verify number has 3 otp limit for a link and for same number , again 3 otp limit for diff link",enabled = false)
    public void generateOTPLimit() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        LinkBasedService linkBasedService  = new LinkBasedService(merchantType.getId(),txnAmount);
        JsonPath jsonPath = linkBasedService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("API failed").isEqualTo("SUCCESS");
        LinkPaymentOTPLimit linkPaymentOTPLimit = new LinkPaymentOTPLimitWEB();
        linkPaymentOTPLimit.launchLoginPage(jsonPath.getString("body.longUrl"));
        linkPaymentOTPLimit.enterUserAndAmount(user,txnAmount);

        assertOTPRetryLimit(linkPaymentOTPLimit);

        LinkBasedService linkBasedService2  = new LinkBasedService(merchantType.getId(),txnAmount);
        JsonPath jsonPath2 = linkBasedService2.execute().jsonPath();
        Assertions.assertThat(jsonPath2.getString("body.resultInfo.resultStatus")).as("API failed").isEqualTo("SUCCESS");
        linkPaymentOTPLimit.launchLoginPage(jsonPath2.getString("body.longUrl"));

        linkPaymentOTPLimit.enterUserAndAmount(user,txnAmount);

        assertOTPRetryLimit(linkPaymentOTPLimit);

    }*/



//    @Deprecated
//    @Parameters({"theme"})
//    @Test(description = "Verify change number functionality after otp limit breach",enabled = false)
    public void changeNumberFunctionality(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        User changedUser = userManager.getForWrite(Label.AUTOLOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        LinkBasedService linkBasedService  = new LinkBasedService(merchantType.getId(),txnAmount);
        JsonPath jsonPath = linkBasedService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("API failed").isEqualTo("SUCCESS");
        LinkPaymentOTPLimitWEB linkPaymentOTPLimit = new LinkPaymentOTPLimitWEB();
        linkPaymentOTPLimit.launchLoginPage(jsonPath.getString("body.longUrl"));
        linkPaymentOTPLimit.enterUserAndAmount(user,txnAmount);

        assertOTPRetryLimit(linkPaymentOTPLimit);

        linkPaymentOTPLimit.changeNumber();
        linkPaymentOTPLimit.enterUserAndAmount(changedUser,txnAmount);

        assertOTPRetryLimit(linkPaymentOTPLimit);

    }


}



