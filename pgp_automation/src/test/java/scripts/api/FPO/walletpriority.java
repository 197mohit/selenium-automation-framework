package scripts.api.FPO;

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
import org.testng.annotations.Test;
public class walletpriority extends PGPBaseTest{

    private final String client = "iosapp";
    private final String version = "11.12.26";
    private final String payMethod = "CREDIT_CARD";
    private final String payMethod1 = "DEBIT_CARD";
    private final String payMethod2 = "NET_BANKING";
    private final String accRefId = "227222";
    private final String liteBalance = "2.00";

    public JsonPath getDynamicAPIResponse(Constants.MerchantType mid, String sso, String client, String version, String payMethod, String accRefId, String liteBalance, Boolean isFPO)
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
    public JsonPath getDynamicAPIResponse1(Constants.MerchantType mid, String sso, String client, String version, String liteBalance, Boolean isFPO)
    {
        String uniqueId = CommonHelpers.generateOrderId();
        if(isFPO) {
            DynamicFPO request = new DynamicFPO(mid, sso, client, version, liteBalance, uniqueId);
            JsonPath response = request.execute().jsonPath();
            return response;
        }
        else {
            DynamicFQR request = new DynamicFQR(mid, sso, client, version, liteBalance, uniqueId);
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

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero with RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_01() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("2");
        softly.assertThat(getPayModePriority(payMethod, response, true))
                .isEqualTo("3");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo(null);
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero with RecentPayMode when mid have wallet only")
    public void walletpriority_02() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority(payMethod, response, true))
                .isEqualTo("2");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo(null);
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_03() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("2");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero without RecentPayMode when mid have wallet only")
    public void walletpriority_04() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("1");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FQR response when wallet balance is zero with RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_05() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("2");
        softly.assertThat(getPayModePriority(payMethod, response, false))
                .isEqualTo("3");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo(null);
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FQR response when wallet balance is zero with RecentPayMode when mid have wallet only ")
    public void walletpriority_06() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority(payMethod, response, false))
                .isEqualTo("2");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo(null);
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_07() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("2");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_08() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("1");
        softly.assertAll();
    }

    /*All the below cases from 9 to 16 works when the ff4j property native.paymode.order value should be
     "PAYTM_DIGITAL_CREDIT,BALANCE,ADVANCE_DEPOSIT_ACCOUNT,UPI,NET_BANKING_PPBL,SAVED_CARD,NET_BANKING,EMI,SAVED_VPA"*/

    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero with RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_09() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,2.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod1, accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority(payMethod1, response , true))
                .isEqualTo("2");
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("3");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo("false");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero with RecentPayMode when mid have wallet only")
    public void walletpriority_10() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,5.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority(payMethod, response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("2");
        softly.assertThat(response.getString("body.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo("true");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_11() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, true))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("2");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero without RecentPayMode when mid have wallet only")
    public void walletpriority_12() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,11.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, true);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, true))
                .isEqualTo("1");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FQR response when wallet balance is not zero with RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_13() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,1.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority(payMethod, response, false))
                .isEqualTo("2");
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("3");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo("false");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FQR response when wallet balance is not zero with RecentPayMode when mid have wallet only ")
    public void walletpriority_14() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,1.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse(mid, user.ssoToken(), client, version, payMethod, accRefId, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority(payMethod, response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("2");
        softly.assertThat(response.getString("body.paymentOptions.merchantPayOption.isRecentPayModeAtTop"))
                .isEqualTo("true");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_15() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,100.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("PAYTM_DIGITAL_CREDIT", response, false))
                .isEqualTo("1");
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("2");
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-49023")
    @Test(description = "Verify v2/FPO response when wallet balance is not zero without RecentPayMode when mid have wallet and postpaid")
    public void walletpriority_16() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,10.00);
        Constants.MerchantType mid = Constants.MerchantType.Static_False_Recent_False;
        JsonPath response = getDynamicAPIResponse1(mid, user.ssoToken(), client, version, liteBalance, false);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(getPayModePriority("BALANCE", response, false))
                .isEqualTo("1");
        softly.assertAll();
    }
}