package scripts.Native;

import com.paytm.appconstants.Constants.*;
import com.paytm.api.nativeAPI.DynamicFPO;
import com.paytm.api.nativeAPI.DynamicFQR;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamicPaymentInstrumentPriority extends PGPBaseTest {

    private final String client = "iosapp";
    private final String version = "10.29.0";
    private final String payMethod = "CREDIT_CARD";
    private final String accRefId = "227222";
    private final String liteBalance = "2.00";

    public JsonPath getDynamicAPIResponse(MerchantType mid, String sso, String client, String version, String payMethod, String accRefId, String liteBalance, Boolean isFPO)
    {
        String uniqueId = CommonHelpers.generateOrderId();
        if(isFPO) {
            DynamicFPO request = new DynamicFPO(mid, sso, client, version, payMethod, accRefId, liteBalance, uniqueId);
            JsonPath response = request.execute().jsonPath();
            return response;
        }
        else {
            DynamicFQR request = new DynamicFQR(mid, sso, client, version, payMethod, accRefId, liteBalance, uniqueId);
            JsonPath response = request.execute().jsonPath();
            return response;
        }
    }

    public String getPayModePriority(String paymentMode, JsonPath response, Boolean isFPO) {
        String payModePriority = "body.";
        if(!isFPO) payModePriority += "paymentOptions.";
        payModePriority += "merchantPayOption.paymentModes.find{it.paymentMode  == '{paymentMode}'}.priority".replace("{paymentMode}",paymentMode);
        return response.getString(payModePriority);
    }

    public String getPayModePriority(String paymentMode, String accRefId, JsonPath response, Boolean isFPO) {
        if (paymentMode != "UPI") return getPayModePriority(paymentMode,response,isFPO);
        String payModePriority = "body.";
        if(!isFPO) payModePriority += "paymentOptions.";
        payModePriority += "merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.find{it.accRefId == '{accRefId}'}.priority".replace("{accRefId}",accRefId);
        return response.getString(payModePriority);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FPO response when Dynamic Priority is defined with RecentPayMode Experiment True and StaticInstrument available")
    public void dynamicPaymentInstrumentPriority_01() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, true)).isEqualTo("1");
        softly.assertThat(getPayModePriority(payMethod, response, true)).isEqualTo("2");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("false");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FPO response when Dynamic Priority is defined with RecentPayMode Experiment True and Static Instrument not available")
    public void dynamicPaymentInstrumentPriority_02() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, "BALANCE", accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, true)).isEqualTo("1");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("true");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FQR response when Dynamic Priority is defined with Recent Paymode Experiment False and Static Instrument available")
    public void dynamicPaymentInstrumentPriority_03() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, false)).isEqualTo("1");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop")).isNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FQR response when Dynamic Priority is defined with Recent Paymode Experiment False and Static Instrument not available")
    public void dynamicPaymentInstrumentPriority_04() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("UPI", response, false)).isNull();
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop")).isNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FPO response when Dynamic Priority is defined with RecentPayMode Experiment True and StaticInstrument not available and recentPayMode has zero balance")
    public void dynamicPaymentInstrumentPriority_05() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,0.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, "BALANCE", accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("UPI_LITE", response, true)).isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, true)).isEqualTo("2");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("false");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FQR response when Dynamic Priority is defined with RecentPayMode Experiment True and StaticInstrument not available and Lite(recent) and Wallet have zero balance")
    public void dynamicPaymentInstrumentPriority_06() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,0.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, "UPI_LITE", accRefId, "0.00", false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("UPI", accRefId, response, false)).isEqualTo("1");
        softly.assertThat(getPayModePriority("UPI_LITE", response, false)).isEqualTo("2");
        softly.assertThat(getPayModePriority("BALANCE", response, false)).isEqualTo("3");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("false");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FPO response when Dynamic Priority is defined with RecentPayMode Experiment True and StaticInstrument not available and recentPayMode is UPI ID")
    public void dynamicPaymentInstrumentPriority_07() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, "UPI", accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("UPI", accRefId, response, true)).isEqualTo("1");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("true");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify v2/FQR response when Dynamic Priority is defined but appVersion is blacklisted for client")
    public void dynamicPaymentInstrumentPriority_08() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, "1.0.0", "UPI", accRefId, liteBalance, false);
        Assertions.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop")).isNull();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-45065")
    @Test(description = "Verify HMS RED accounts are set to end of priority ordering for v2/FPO response when Dynamic Priority is defined")
    public void dynamicPaymentInstrumentPriority_09() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1.00);
        MerchantType mid = MerchantType.Static_False_Recent_True;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, "UPI", accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        String HMS = "body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.find{it.accRefId == '227221'}.bankMetaData.bankHealth.category";
        softly.assertThat(response.getString(HMS)).isEqualTo("RED");
        softly.assertThat(getPayModePriority("UPI", "227221", response, true)).isEqualTo("6");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop")).isEqualTo("true");
        softly.assertAll();
    }
}
