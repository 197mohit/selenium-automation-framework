package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.CheckEMIEligibility;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.GetEMIDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.checkEMIEligibility.CheckEMIEligibilityRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.Body;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.Head;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;

import static com.paytm.base.test.Group.Status.TO_BE_FIXED;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.EMI;
import com.paytm.utils.merchant.merchant.util.EMIs;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.ResponseSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.hamcrest.core.IsEqual;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

@Owner("Deepak")
public class EMISubvention extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private final ResponseSpecification validateSchema = new ResponseSpecBuilder()
            .rootPath("body.emiDetails")
            .expectBody("channelCode", everyItem(isOneOf("HDFC", "ICICI", "AMEX", "BAJAJFN")))
            .build();

    // TODO need to fix assertions
    private static ResponseSpecification emiDetailsSchema(String mid, String bankCode) {
        String backCodeDb = bankCode == "ICICI" ? "ICIE" : bankCode;
        ResponseSpecification verficationSchema = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .rootPath("body.emiDetails.find {it.channelCode =='" + bankCode + "'}.emiChannelInfos")
                .expectBody("emiId", containsInAnyOrder(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "ID").toArray()))
                .expectBody("interestRate", containsInAnyOrder(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "INTEREST").toArray()))
                .expectBody("ofMonths", containsInAnyOrder(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "MONTH").toArray()))
                .expectBody("minAmount.value", containsInAnyOrder(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "MIN_AMT").toArray()))
                .expectBody("maxAmount.value", containsInAnyOrder(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "MAX_AMT").toArray()))
                .build();

        return verficationSchema;
    }

    private final static ResponseSpecification resultSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", IsEqual.equalTo("S"))
            .expectBody("resultCode", IsEqual.equalTo("0000"))
            .expectBody("resultMsg", IsEqual.equalTo("Success"))
            .build();


    private final static RequestSpecification reqSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setBaseUri(LocalConfig.PGP_HOST)
            .setBasePath("/theia/api/v1/getEmiDetails")
            .build();


    @Test(description = "Verify EMI Details for EMI Enabled Merchant")
    public void getEmiDetailsForEMIEnabledMerchant() {
        String mid = Constants.MerchantType.EMI.getId();
        EMIs emis = new Merchant(mid).getEmis();
        Map<String, String> jwtTokenMap = new HashMap<>();
        jwtTokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(jwtTokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        given().spec(reqSpec).body(getEMIDetailsRequest).queryParam("mid", mid).post()
                .then().spec(resultSchema)
                .spec(validateSchema)
                .root("body.emiDetails.emiChannelInfos")
                .body("emiId", everyItem(isIn(emis.stream().map(EMI::getId).collect(Collectors.toList()))))                .body("interestRate", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getInterest())).collect(Collectors.toList())))))
                .body("ofMonths", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMonths())).collect(Collectors.toList())))))
                .body("minAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMinAmt())).collect(Collectors.toList())))))
                .body("maxAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMaxAmt())).collect(Collectors.toList())))));
    }


    @Test(description = "Verify EMI Details for EMI Disabled Merchant")
    public void getEmiDetailsForEMIDisabledMerchant() {
        String mid = Constants.MerchantType.EMI_DC.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("EMI not configured on merchant");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("3004");
    }


    @Test(description = "Verify EMI Details for Invalid MID")
    public void getEmiDetailsForInvalidMID() {
        String mid = "bGnCao46620984716293s"; // a Non-existing Mid
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        System.out.println(response.asString());
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Mid is invalid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("2006");
    }


    @Test(description = "Verify EMI Details for Invalid JWT Token")
    public void getEmiDetailsForInvalidJWTToken() {
        String mid = Constants.MerchantType.EMI.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.JWT_EMI_KEY); // Correct Issuer is subvention so it will generate incorrect token.
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        System.out.println(response.asString());
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Token validation failed");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1002");
    }


    @Test(description = "Verify EMI Details for Blank ClientId")
    public void getEmiDetailsForBlankClientId() {
        String mid = Constants.MerchantType.EMI.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest().setHead(new Head().setClientId("").setSignature(signature)).setBody(new Body(mid));

        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Token validation failed");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1002");
    }


    @Test(description = "Verify EMI Details for Invalid Client Id")
    public void getEmiDetailsForInvalidClientId() {
        String mid = Constants.MerchantType.EMI.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest().setHead(new Head().setClientId("abcd").setSignature(signature)).setBody(new Body(mid));
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Token validation failed");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1002");
    }


    @Test(description = "Verify EMI Details for Different MID's IN Query And Request")
    public void verifyEmiDetailsForDiffMIDInRequestAndQueryParams() {
        String mid = Constants.MerchantType.EMI.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);

        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(mid, signature);
        Response response = new GetEMIDetails(getEMIDetailsRequest, Constants.MerchantType.PGOnly.getId()).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");
    }

    @Test(description = "Verify EMI Details for EMI CC Enabled Merchant")
    public void verifyEmiDetailsWithCCEMI() {
        String mid = Constants.MerchantType.EMI.getId();
        EMIs emis = new Merchant(mid).getEmis();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);

        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        given().spec(reqSpec).body(getEMIDetailsRequest).queryParam("mid", mid).post()
                .then().spec(resultSchema)
                .root("body.emiDetails.emiChannelInfos")

                .body("emiId", everyItem(everyItem(isIn(emis.stream().map(EMI::getId).collect(Collectors.toList())))))
                .body("interestRate", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getInterest())).collect(Collectors.toList())))))
                .body("ofMonths", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMonths())).collect(Collectors.toList())))))
                .body("minAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMinAmt())).collect(Collectors.toList())))))
                .body("maxAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMaxAmt())).collect(Collectors.toList())))));
    }

    @Issue("PGP-17280")
    @Test(description = "Verify EMI Details for EMI DC Enabled Merchant", groups = {Group.Status.BUG})
    public void verifyEmiDetailsWithDCEMI() {
        String mid = Constants.MerchantType.EMI_DC.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        given().spec(reqSpec).body(getEMIDetailsRequest).queryParam("mid", mid).post()
                .then().spec(resultSchema)
                .spec(emiDetailsSchema(mid, "ICICI"));
    }

    @Test(description = "Verify EMI Details for BAJAJFIN EMI Enabled Merchant")
    public void verifyEmiDetailsWithBajajFinEMI() {
        String mid = Constants.MerchantType.BAJAJFINEMI.getId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        given().spec(reqSpec).body(getEMIDetailsRequest).queryParam("mid", mid).post()
                .then().spec(resultSchema)
                .spec(emiDetailsSchema(mid, "BAJAJFN"));
    }

    @Test(description = "Verify EMI Details for All EMI Enabled Merchant")
    public void verifyForAllEMIEnabled() {
        String mid = Constants.MerchantType.EMI.getId();
        EMIs emis = new Merchant(mid).getEmis();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        String signature = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.subvention, LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid);
        given().spec(reqSpec).body(getEMIDetailsRequest).queryParam("mid", mid).post()
                .then().spec(resultSchema)
                .root("body.emiDetails.emiChannelInfos")
                .body("emiId", everyItem(everyItem(isIn(emis.stream().map(EMI::getId).collect(Collectors.toList())))))
                .body("interestRate", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getInterest())).collect(Collectors.toList())))))
                .body("ofMonths", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMonths())).collect(Collectors.toList())))))
                .body("minAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMinAmt())).collect(Collectors.toList())))))
                .body("maxAmount.value", everyItem(everyItem(isIn(emis.stream().map(emi -> String.valueOf(emi.getMaxAmt())).collect(Collectors.toList())))));

    }

    //PGPQA-4936
    //@Test(enabled = false, description = "Check EMI Eligibility with Valid SSO Token")
    public void checkEMIEligiBilityWithSSOToken() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }

    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with Invalid SSO Token")
    public void checkEMIEligiBilityWithInvalidSSOToken() throws Exception {

        String ssoToken = userManager.getForRead(Label.LOGIN).ssoToken();
        String invalidSSO = ssoToken + "123";
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(invalidSSO, mid, Constants.Bank.HDFC.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch")
                .isEqualToIgnoringCase("Invalid Token");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("3001");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with Invalid MID")
    public void checkEMIEligiBilityWithInvalidMID() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        String mid = "dasasdas213121";
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Mid is invalid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("2006");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with EMI enabled MID")
    public void checkEMIEligiBilityWithEMIEnabledMID() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }


    @Test(description = "Check EMI Eligibility with EMI disabled MID")
    public void checkEMIEligiBilityWithEMIDisabledMID() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.WalletOnly.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("EMI not configured on merchant");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("3004");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with blank ChannelCode")
    public void checkEMIEligiBilityWithBlankChannelCode() throws Exception {
        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC.toString(), emiTypes)
                .setHead(new com.paytm.dto.NativeDTO.checkEMIEligibility.Head())
                .setBody(new com.paytm.dto.NativeDTO.checkEMIEligibility.Body()
                        .setMid(mid)
                        .setChannelCode("")
                        .setEmiTypes(emiTypes));
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");
    }

    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with ChannelCode enabled on merchant")
    public void checkEMIEligiBilityWithChannelCodeEnabledOnMerchant() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility with ChannelCode disabled on merchant")
    public void checkEMIEligiBilityWithChannelCodeDisabledOnMerchant() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.BAJAJFN.toString(), emiTypes);

        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].message").toString()).as("message mismatch").isEqualToIgnoringCase("You are not eligible for availing EMI on your Credit Card");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("false");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for EMIDC Merchant")
    public void checkEMIEligiBilityWithEMITypeDebitCardOnEMIDCMerchant() throws Exception {

        String ssoToken = userManager.getForRead(Label.EMIDC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);

        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for EMICC Merchant")
    public void checkEMIEligiBilityWithEMITypeCreditCard() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);

        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for EMITYPE=DEBITCARD Non-EMIDC Merchant")
    public void checkEMIEligiBilityWithEMITypeDebitCardOnNonEMIDCMerchant() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].message").toString()).as("message mismatch").isEqualToIgnoringCase("You are not eligible for availing EMI on your Debit Card");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("false");
    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having EMICC AND EMIDC of Same Bank")
    public void checkEMIEligiBilityWithEMIDCAndEMICCOfSameBank() throws Exception {

        String ssoToken = userManager.getForRead(Label.PPBL).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);

        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having EMICC AND EMIDC of Differnt Bank")
    public void checkEMIEligiBilityWithEMIDCAndEMICCOfDifferntBank() throws Exception {

        String ssoToken = userManager.getForRead(Label.EMIDC).ssoToken();
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[1].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }

    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having EMIDC but User is Non-EMIDC")
    public void checkEMIEligiBilityWithUserNotconfiguredOnEMIDCInDB() throws Exception {

        String ssoToken = userManager.getForRead(Label.NONEMIDC).ssoToken();
        String mid = Constants.MerchantType.EMIOnly_DC.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].message").toString()).as("message mismatch").isEqualToIgnoringCase("You are not eligible for availing EMI on your Debit Card");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("false");

    }

    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant and User both having EMIDC")
    public void checkEMIEligiBilityWithUserConfiguredOnEMIDCInDB() throws Exception {

        String ssoToken = userManager.getForRead(Label.EMIDC).ssoToken();
        String mid = Constants.MerchantType.EMIOnly_DC.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having ChannelCode HDFC")
    public void checkEMIEligiBilityForChannelCodeHDFC() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having ChannelCode ICICI")
    public void checkEMIEligiBilityForChannelCodeICICI() throws Exception {

        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.ICICI.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("ICICI");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }


    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having ChannelCode BAJAJFN")
    public void checkEMIEligiBilityForChannelCodeBajajFN() throws Exception {
        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.BAJAJFINEMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Channelcode.BAJAJFN.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Channelcode.BAJAJFN.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }

    //PGPQA-4936
    //@Test(enabled = false,description = "Check EMI Eligibility for Merchant having ChannelCode AMEX")
    public void checkEMIEligiBilityForChannelCodeAMEX() throws Exception {
        String ssoToken = userManager.getForRead(Label.BASIC).ssoToken();
        System.out.println(ssoToken);
        String mid = Constants.MerchantType.EMI.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("CREDIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, "AMEX", emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase("AMEX");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify PGOnly Merchants having EMI Id in Initiate Transaction")
    public void verifyPGOnlyWithEMIIDInInitiateTransaction(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid = Constants.MerchantType.EMISubvention.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.HDFC_ONLY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMISubvention)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("1")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMISubvention, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(emiDetails.get(0).get("planId"))
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setEMI_TYPE("CREDIT_CARD")
                .setINDUSTRY_TYPE_ID("Retail").build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    //    @Parameters({"isNativePlus"})// Hybrid EMI not supported, as discussed with Afaq sir.
    //   @Test(description = "Verify Hybrid Merchants having EMI Id in Initiate Transaction", enabled = false)
    public void verifyHybridFlowInWithEMIIdInInitiateTransaction(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.Hybrid.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.HDFC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setTxnValue("100")
                .setEmiId(emiDetails.get(0).get("emiId")).build();
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()) - 90.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(emiDetails.get(0).get("planId"))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(groups = TO_BE_FIXED, description = "Verify PGOnly EMIDC Merchant having EMIID in Initiate Transaction")
    public void verifyEMIDCWithEMIID(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        String mid = Constants.MerchantType.EMI_DC.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.ICICI);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI_DC)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("12")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(mid)
                .build();
        FetchPaymentOption.executeFetchPaymtOption(mid, initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DC, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setAUTH_MODE("otp")
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setPlanId(emiDetails.get(0).get("planId"))
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.ICIE.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("EMI_DC")
                .validateBankName(Constants.Bank.ICICI.toString() + " BANK")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    //    @Parameters({"isNativePlus"})
//    @Test(description = "Verify Hybrid EMIDC Merchant having EMIID in Initiate Transaction",enabled = false)
    public void verifyHybridEMIDCWithEMIID(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.EMIDC);
        String mid = Constants.MerchantType.Hybrid.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.ICICI);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setTxnValue("100")
                .setEmiId(emiDetails.get(0).get("emiId")).build();
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()) - 90.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI_DC)
                .setChannelCode(Constants.Bank.ICICI.toString())
                .setEMI_TYPE("DEBIT_CARD")
                .setPlanId(emiDetails.get(0).get("planId"))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.ICIE.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICI.toString() + " BANK")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.ICIE.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.ICICI.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters("isNativePlus")
    @Test(description = "Verify PGONLY Bajaj Finserv EMI Merchant having EMIID in Initiate Transaction")
    public void verifyBAJAJEMIWithEMIID(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid = Constants.MerchantType.BAJAJFINEMI.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Channelcode.BAJAJFN.toString());

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BAJAJFINEMI)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BAJAJFINEMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.BAJAJFN.toString())
                .setEMI_TYPE("CREDIT_CARD")
                .setPlanId(emiDetails.get(0).get("planId"))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();
        bajajFinservBankPage.inputOtp(PaymentDTO.bankOtp);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.BAJAJFN.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.BAJAJFNEMI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    //    @Parameters("isNativePlus")
//    @Test(description = "Verify Hybrid Bajaj Finserv EMI Merchant having EMIID in Initiate Transaction", enabled=false)
    public void verifyHybridBAJAJEMIWithEMIID(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.Hybrid.getId();
        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.BAJAJFN);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("100")
                .build();
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()) - 90.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.BAJAJFN.toString())
                .setPlanId(emiDetails.get(0).get("planId"))
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();
        bajajFinservBankPage.inputOtp(PaymentDTO.bankOtp);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.BAJAJFN.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.BAJAJFN.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "BAJAJFN")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.BAJAJFN.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.BAJAJFN.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters("isNativePlus")
    @Test(description = "Verify PGonly EMI Merchant with saved Card having EMIID in Initiate Transaction")
    public void verifyEMIWithEMIIDWithSavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.EMI.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());

        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.HDFC_ONLY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiDetails.get(0).get("planId"))
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setEMI_TYPE("CREDIT_CARD")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    //    @Parameters("isNativePlus")
//    @Test(description = "Verify Hybrid  EMI Merchant with Saved Card having EMIID in Initiate Transaction", enabled = false)
    public void verifyHybridEMIWithEMIIDWithSavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.Hybrid.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        List<Map<String, String>> emiDetails = PGPHelpers.getEMIDetails(mid, Constants.Bank.HDFC);
        System.out.println("EMI_ID" + emiDetails.get(0).get("emiId"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setEmiId(emiDetails.get(0).get("emiId"))
                .setTxnValue("100")
                .build();
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()) - 90.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiDetails.get(0).get("planId"))
                .setChannelCode(Constants.Bank.HDFC.toString())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

    }
}



