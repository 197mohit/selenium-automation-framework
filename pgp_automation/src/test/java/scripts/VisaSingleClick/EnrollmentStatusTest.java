package scripts.VisaSingleClick;

import com.paytm.api.CreateToken;
import com.paytm.api.VisaSingleClick.EnrollmentStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.EnrollmentStatusDTO.AccountDataList;
import com.paytm.dto.EnrollmentStatusDTO.EnrollmentStatusDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Arrays;

@Owner("Karmvir")
@Feature("PGP-20953")

public class EnrollmentStatusTest extends PGPBaseTest {


    @Test(description = "To test the enrollment status on enrolled card with SSO for Enrolled Card")
    public void enrollmentStatusofEnrolledCardWithSSOForEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String sso=user.ssoToken();
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(sso)
                .setTokenType("SSO").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
//    @Test(description = "To test the enrollment status for DeEnrolled Card with sso", enabled=false)
    public void enrollmentStatusofDenrolledCardWithSSO() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String sso=user.ssoToken();
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(sso)
                .setTokenType("SSO").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMjktLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is enrolled").isEqualTo("InBlackList");
    }

    @Test(description = "To test the enrollment status  with SSO when 2 card are enrolled and 1 card is not enrolled")
    public void enrollmentStatusofCardWithSSOOneCardIsNotEnrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String sso=user.ssoToken();
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(sso)
                .setTokenType("SSO").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[1].resultCode")).as("Crad is enrolled").isEqualTo("NotParticipated");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[2].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
    @Test(description = "To test the enrollment status on enrolled card with SSO AND CustId Are Of Different Users when 2 card are enrolled and 1 card is de enroll")
    public void enrollmentStatusofEnrolledCardWithSSOANDCustIdAreOfDifferentUsersOneCardIsDeenrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String sso=user.ssoToken();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId("1000036031").setToken(sso)
                .setTokenType("SSO").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Result status is S").isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is Success").isEqualTo("SSO Token is invalid");

    }

    @Test(description = "To test the enrollment status with ACCESS Token For One Enrolled Card")
    public void enrollmentStatusofEnrolledCardWithACCESSTokenForEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        String custid=user.custId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(AccessToken)
                .setTokenType("ACCESS").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");

    }

//    @Test(description = "To test the enrollment status with ACCESS Token For One DeEnrolled Card", enabled=false)
    public void enrollmentStatusofEnrolledCardWithACCESSTokenForDeEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        String custid=user.custId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(AccessToken)
                .setTokenType("ACCESS").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is enrolled").isEqualTo("InBlackList");

    }
    @Test(description = "To test the enrollment status api with ACCESS Token when 2 card are enrolled and 1 card is not enrol")
    public void enrollmentStatusofEnrolledCardWithACCESSTokenOneCardNotEnrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        String custid=user.custId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(AccessToken)
                .setTokenType("ACCESS").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[1].resultCode")).as("Crad is enrolled").isEqualTo("NotParticipated");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[2].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
    @Test(description = "To test the enrollment status with Invalid ACCESS Token")
    public void enrollmentStatusofEnrolledCardWithInvalidACCESSTokenOneCardIsNotEnrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        String custid=user.custId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(AccessToken)
                .setTokenType("ACCESS").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId+"6532","");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Result status is S").isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is Success").isEqualTo("Token validation failed");

    }
    @Test(description = "To test the enrollment status with ACCESS Token and Invalid cust id")
    public void enrollmentStatusofEnrolledCardWithACCESSTokenAndInvalidCustId() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId("gct6732d").setToken(AccessToken)
                .setTokenType("ACCESS").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Result status is F").isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is not Success").isEqualTo("Success");

    }
    @Test(description = "To test the enrollment status with Checksum For One Enrolled Card")
    public void enrollmentStatusofEnrolledCardWithChecksumForEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid)
                .setTokenType("CHECKSUM").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }


    @Test(description = "To test the enrollment status with Checksum when 2 card are enrolled and 1 card is not enrol")
    public void enrollmentStatusofEnrolledCardWithChecksumOneCardIsNotEnrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid)
                .setTokenType("CHECKSUM").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[1].resultCode")).as("Crad is enrolled").isEqualTo("NotParticipated");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[2].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
    @Test(description = "To test the enrollment status with Checksum and Invalid Cust id")
    public void enrollmentStatusofEnrolledCardWithChecksumAndInvalidCustId() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId("653518673")
                .setTokenType("CHECKSUM").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Result status is F").isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is not Success").isEqualTo("Success");
    }
    @Test(description = "To test the enrollment status with Txn Token For Enrolled card")
    public void enrollmentStatusofEnrolledCardWithTxnTokenOneForEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        String sso=user.ssoToken();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(txnToken)
                .setTokenType("TXN_TOKEN").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,orderId);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
//    @Test(description = "To test the enrollment status with Txn Token For DeEnrolled card" , enabled=false)
    public void enrollmentStatusofEnrolledCardWithTxnTokenOneForDeEnrolledCard() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        String sso=user.ssoToken();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(txnToken)
                .setTokenType("TXN_TOKEN").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,orderId);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is enrolled").isEqualTo("InBlackList");
    }
    @Test(description = "To test the enrollment status with Txn Token when 2 card are enrolled and 1 card is not enrol")
    public void enrollmentStatusofEnrolledCardWithTxnTokenOneCardIsNotEnrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        String sso=user.ssoToken();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken(txnToken)
                .setTokenType("TXN_TOKEN").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwMTEtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,orderId);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is failure").isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[0].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[1].resultCode")).as("Crad is not enrolled").isEqualTo("NotParticipated");
        Assertions.assertThat(jsonPath.getString("body.accountStatusDataList[2].resultCode")).as("Crad is not enrolled").isEqualTo("NotInBlackList");
    }
    @Test(description = "To test the enrollment status with Invalid Txn Token")
    public void enrollmentStatusofEnrolledCardWithInvalidTxnTokenOneCardIsDeenrolled() throws Exception {
        User user=userManager.getForRead(Label.SINGLECLICKENROLLCARD);
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        String custid=user.custId();
        String sso=user.ssoToken();
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();

        EnrollmentStatusDTO enrollmentStatusDTO = new EnrollmentStatusDTO.Builder().setRequestId("1233").setAppId("net.one97.paytm").setCustId(custid).setToken("vwcg67t1c6bcw71b7c1bc1")
                .setTokenType("TXN_TOKEN").setAccountDataList(Arrays.asList(new AccountDataList[]{new
                        AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAxMjgtLTEwMDA2NDg5MTItbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=","401200"),
                        new AccountDataList("ea137e39ba0860da513ca95d95f49302d15376cd8f528101","NDAxMjAwLTAwNDUtLTEwMDAyMjYyMzctbmV0Lm9uZTk3LnBheXRtLTYxNTkwNzRkZTVkZjQyZDg=","401200")}))
                .build();
        JsonPath jsonPath=EnrollmentStatus.checkEnrollmentStatus(enrollmentStatusDTO,mid,referenceId,"65738cc");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Result Status is not F").isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).as("Result message is Success").isEqualTo("Token validation failed");

    }
}
