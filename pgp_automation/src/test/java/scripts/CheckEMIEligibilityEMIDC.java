package scripts;

import com.paytm.api.nativeAPI.CheckEMIEligibility;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.checkEMIEligibility.CheckEMIEligibilityRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.paytm.appconstants.Constants.Owner.MAYURI;

public class CheckEMIEligibilityEMIDC extends PGPBaseTest {

    public void iciciSuccessAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.ICICI.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].planId").toString()).as("planId mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].interestRate").toString()).as("interestRate mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].ofMonths").toString()).as("ofMonths mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.currency").toString()).as("currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.value").toString()).as("value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.currency").toString()).as("maxAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.value").toString()).as("maxAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].emiAmount.currency").toString()).as("emiAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].emiAmount.value").toString()).as("emiAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].totalAmount.currency").toString()).as("totalAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].totalAmount.value").toString()).as("totalAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.currency").toString()).as("processingFee currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.value").toString()).as("processingFee value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].annualPercentageRate").toString()).as("annualPercentageRate mismatch").isNotNull();
    }

    public void hdfcSuccessAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].planId").toString()).as("planId mismatch").isEqualToIgnoringCase("HDFC|3");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].interestRate").toString()).as("interestRate mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].ofMonths").toString()).as("ofMonths mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.currency").toString()).as("currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.value").toString()).as("value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.currency").toString()).as("maxAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.value").toString()).as("maxAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].emiAmount.currency").toString()).as("emiAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].emiAmount.value").toString()).as("emiAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].totalAmount.currency").toString()).as("totalAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].totalAmount.value").toString()).as("totalAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.currency").toString()).as("processingFee currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.value").toString()).as("processingFee value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].annualPercentageRate").toString()).as("annualPercentageRate mismatch").isNotNull();

    }

    public void systemErrorAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("U");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("System error");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("00000900");
    }

    public void invalidReqParamsAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");

    }

    public void midNotConfiguredAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("EMI not configured on merchant");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("3004");

    }

    public void userNotEligibleAssertions(CheckEMIEligibilityRequest checkEMIEligibilityRequest,String mid){
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].message").toString()).as("message mismatch").isEqualToIgnoringCase("User not eligible for HDFC Debit Card EMI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("false");

    }
    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility with Valid SSO Token")
    public void checkEMIEligiBilityWithSSOToken() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version ="v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when version not passed")
    public void checkEMIEligiBilityWithSSOTokenVersionNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version = "";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9818686101";
        String txnAmount = "10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

        @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when version key not passed")
    public void checkEMIEligiBilityWithSSOTokenVersionKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= null;
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
        }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when incorrect value of version key not passed")
    public void checkEMIEligiBilityWithSSOTokenVersionIncorrectValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v123";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when requestTimestamp key not passed")
    public void checkEMIEligiBilityWithSSOTokenRequestTimestampKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = null;
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when incorrect value of RequestTimestamp key not passed")
    public void checkEMIEligiBilityWithSSOTokenRequestTimestampIncorrectValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "smzkdhf1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when channelId key not passed")
    public void checkEMIEligiBilityWithSSOTokenChannelIdKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId =null;
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when incorrect channelId key not passed")
    public void checkEMIEligiBilityWithSSOTokenChannelIdIncorrectValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="abcxz";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        systemErrorAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when WAP value passed in channelId key ")
    public void checkEMIEligiBilityWithSSOTokenChannelIdWAPValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WAP";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when clientId key not passed")
    public void checkEMIEligiBilityWithSSOTokenClientIdKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId=null;
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when incorrect ClientId key  passed")
    public void checkEMIEligiBilityWithSSOTokenClientIdIncorrectValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="testing";
        String tokenType="SSO";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when incorrect value in tokenType key  passed")
    public void checkEMIEligiBilityWithSSOTokenTokenTypeIncorrectValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="abcxyz";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when tokenType key not passed")
    public void checkEMIEligiBilityWithSSOTokenTokenTypeKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType=null;
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }


    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when tokenType blank value passed")
    public void checkEMIEligiBilityWithSSOTokenTypeKeyNoValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String token="";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,token,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when token key not passed")
    public void checkEMIEligiBilityWithSSOTokenTokenKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String token = null;
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,token,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }


    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when token blank value passed")
    public void checkEMIEligiBilityWithSSOTokenTokenTypeKeyNoValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String token="";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,token,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when ssoToken key not passed")
    public void checkEMIEligiBilityWithSSOSsoTokenKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String SSO=null;
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,SSO,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }


    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when ssoToken blank value passed")
    public void checkEMIEligiBilityWithSSOssoTokenKeyNoValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String SSO="";
        String mobileNumber="9818686101";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,SSO,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when mobileNumber key not passed")
    public void checkEMIEligiBilityWithSSOMobileNumberKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber=null;
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);

    }


    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when mobileNumber blank value passed")
    public void checkEMIEligiBilityWithSSOMobileNumberKeyNoValuePassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when mobileNumber different value passed than sso")
    public void checkEMIEligiBilityWithSSOMobileNumberDifferentThanSSO() throws Exception {
        String ssoToken1 = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken1);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="";
        String ssoToken2=userManager.getForRead(Label.EMIDCELIGIBLE).ssoToken();
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken1,ssoToken2,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        hdfcSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when SSO different value passed")
    public void checkEMIEligiBilityWithSSODifferentSSOPassed() throws Exception {
        String ssoToken1 = userManager.getForRead(PGPBaseTest.Label.NONEMIDC).ssoToken();
        System.out.println(ssoToken1);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String ssoToken2=userManager.getForRead(Label.EMIDCELIGIBLE).ssoToken();
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken1,ssoToken2,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        hdfcSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Incorrect mobileNumber value passed")
    public void checkEMIEligiBilityWithSSOWhenIncorrectMobileNumberPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="testing";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        userNotEligibleAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Incorrect length mobileNumber value passed")
    public void checkEMIEligiBilityWithSSOWhenIncorrectLengthMobileNumberPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="965477312";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        userNotEligibleAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Incorrect MID value passed")
    public void checkEMIEligiBilityWithSSOWhenIncorrectMIDPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = "qa14te0147304885555";
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Mid is invalid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("2006");
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when EMI not configured on MID")
    public void checkEMIEligiBilityWithSSOEMINotConfiguredOnMIDPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.Static_True_Recent_False.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        midNotConfiguredAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when no value passed in MID")
    public void checkEMIEligiBilityWithSSONoValuePassedInMID() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = "";
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when MID key not passed")
    public void checkEMIEligiBilityWithSSOEMIMIDKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = null;
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        systemErrorAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Incorrect txnAmount passed")
    public void checkEMIEligiBilityWithSSOIncorrectTxnAmountPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10a";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        systemErrorAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when no value passed in txnAmount")
    public void checkEMIEligiBilityWithSSONoValuePassedInTxnAmount() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].planId").toString()).as("planId mismatch").isEqualToIgnoringCase("HDFC|3");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].interestRate").toString()).as("interestRate mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].ofMonths").toString()).as("ofMonths mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.currency").toString()).as("currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.value").toString()).as("value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.currency").toString()).as("maxAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.value").toString()).as("maxAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.currency").toString()).as("processingFee currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.value").toString()).as("processingFee value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].annualPercentageRate").toString()).as("annualPercentageRate mismatch").isNotNull();

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when txnAmount key not passed")
    public void checkEMIEligiBilityWithSSOEMITxnAmountKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount=null;
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].planId").toString()).as("planId mismatch").isEqualToIgnoringCase("HDFC|3");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].interestRate").toString()).as("interestRate mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].ofMonths").toString()).as("ofMonths mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.currency").toString()).as("currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].minAmount.value").toString()).as("value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.currency").toString()).as("maxAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].maxAmount.value").toString()).as("maxAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.currency").toString()).as("processingFee currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].processingFee.value").toString()).as("processingFee value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiDetails[0].annualPercentageRate").toString()).as("annualPercentageRate mismatch").isNotNull();

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when no value passed in channelCode")
    public void checkEMIEligiBilityWithSSONoValuePassedInChannelCode() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version= "v1";
        String requestTimestamp = "1709617075290";
        String channelId ="WEB";
        String clientId="paytm";
        String tokenType="SSO";
        String mobileNumber="9654773125";
        String txnAmount="10";
        String ChannelCode="";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version,requestTimestamp,channelId,clientId,tokenType,ssoToken,ssoToken,mobileNumber, mid,txnAmount, ChannelCode, emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].planId").toString()).as("planId mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].interestRate").toString()).as("interestRate mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].ofMonths").toString()).as("ofMonths mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].minAmount.currency").toString()).as("currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].minAmount.value").toString()).as("value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].maxAmount.currency").toString()).as("maxAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].maxAmount.value").toString()).as("maxAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].emiAmount.currency").toString()).as("emiAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].emiAmount.value").toString()).as("emiAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].totalAmount.currency").toString()).as("totalAmount currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].totalAmount.value").toString()).as("totalAmount value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].processingFee.currency").toString()).as("processingFee currency mismatch").isEqualToIgnoringCase("INR");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].processingFee.value").toString()).as("processingFee value mismatch").isNotNull();
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiDetails[0].annualPercentageRate").toString()).as("annualPercentageRate mismatch").isNotNull();
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Incorrect value passed in channelCode")
    public void checkEMIEligiBilityWithSSOIncorrectValuePassedInChannelCode() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        String ChannelCode = "testing";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, ChannelCode, emiTypes);
        midNotConfiguredAssertions(checkEMIEligibilityRequest, mid);
    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when channelCode key not passed")
    public void checkEMIEligiBilityWithSSOWhenChannelCodeKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        String ChannelCode = null;
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, ChannelCode, emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when CREDIT_CARD value passed in emiTypes")
    public void checkEMIEligiBilityWithSSOCreditCardValuePassedInemiTypes() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        iciciSuccessAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when No value passed in emiTypes")
    public void checkEMIEligiBilityWithSSONoValuePassedInemiTypes() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("");
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when Invalid value passed in emiTypes")
    public void checkEMIEligiBilityWithSSOInvalidValuePassedInemiTypes() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("testing");
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

    @Owner(MAYURI)
    @Feature("PAPR-5456")
    @Test(description = "Check EMI Eligibility when  emiTypes key not passed")
    public void checkEMIEligiBilityWithSSOEmiTypesKeyNotPassed() throws Exception {
        String ssoToken = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.PG2_AMEX_EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add(null);
        String version = "v1";
        String requestTimestamp = "1709617075290";
        String channelId = "WEB";
        String clientId = "paytm";
        String tokenType = "SSO";
        String mobileNumber = "9654773125";
        String txnAmount = "10";
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(version, requestTimestamp, channelId, clientId, tokenType, ssoToken, ssoToken, mobileNumber, mid, txnAmount, Constants.Bank.ICICI.toString(), emiTypes);
        invalidReqParamsAssertions(checkEMIEligibilityRequest, mid);

    }

}
