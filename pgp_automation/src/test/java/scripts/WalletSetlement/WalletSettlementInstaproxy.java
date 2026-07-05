package scripts.WalletSetlement;

import com.paytm.api.Instaproxy.withdrawRequest;
import com.paytm.api.Instaproxy.withdrawStatus;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;


@Owner("Gagandeep")
@Epic("Wallet-Settlement")
@Feature("PGP-17977")
public class WalletSettlementInstaproxy extends PGPBaseTest {

    private final Constants.MerchantType merchantType = Constants.MerchantType.WALLET_SETTLEMENT;

    Supplier<String> ExtSerialNoGenerator = () -> {
        DateFormat sdf = new SimpleDateFormat("YYYYDD");
        Date date = new Date();
        String time = String.valueOf(date.getTime());
        return sdf.format(date) + time;
    };


    private void validateSuccessfulWithdrawResp(Response response) {
        SoftAssertions validateSoftly = new SoftAssertions();
        validateSoftly.assertThat(response.jsonPath().getString("response.body.resultInfo.resultStatus")).isEqualToIgnoringCase("A");
        validateSoftly.assertThat(response.jsonPath().getString("response.body.resultInfo.resultCodeId")).isEqualToIgnoringCase("00000009");
        validateSoftly.assertThat(response.jsonPath().getString("response.body.resultInfo.resultCode")).isEqualToIgnoringCase("ACCEPTED_SUCCESS");
        validateSoftly.assertThat(response.jsonPath().getString("response.body.resultInfo.resultMsg")).isEqualToIgnoringCase("ACCEPTED_SUCCESS");
        validateSoftly.assertAll();
    }


    private void validateSuccessfulStatusResp(Response response) {
        SoftAssertions validateSoftly = new SoftAssertions();
        validateSoftly.assertThat(response.jsonPath().getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        validateSoftly.assertThat(response.jsonPath().getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("00000000");
        validateSoftly.assertThat(response.jsonPath().getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("SUCCESS");
        validateSoftly.assertAll();
    }

    @Test(description = "Validate Successful transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL ")
    public void successTransaction() {

        String EsnNo = ExtSerialNoGenerator.get();

        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo).build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        //Assertion from CallBack received from Bank

        validateSuccessfulStatusResp(StatusResp);

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultStatus")).isEqualToIgnoringCase("01");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("SUCCESS-0");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("SUCCESS");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }



    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:RWL_100 Insta will map to proper response msg:MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED")
    public void FailureTransactionWithErrorCode_RWL_1000() {

        String EsnNo = ExtSerialNoGenerator.get();

        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("90").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-RWL_1000");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:GE_1027 :Insta will map to proper response msg:FGW_USER_NOT_EXIST")
    public void FailureTransactionWithErrorCode_GE_1027() {

        String EsnNo = ExtSerialNoGenerator.get();

        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("91").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013868");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_USER_NOT_EXIST");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-GE_1027");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_USER_NOT_EXIST");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }




    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:OEC_1023 :Insta will map to proper response msg: WALLET_NOT_ACTIVATED")
    public void FailureTransactionWithErrorCode_OEC_1023() {

        String EsnNo = ExtSerialNoGenerator.get();

        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("92").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("WALLET_NOT_ACTIVATED");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-OEC_1023");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("WALLET_NOT_ACTIVATED");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }

    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:RWL_3002 :Insta will map to proper response msg: FGW_BANK_FAIL_SPECIFIC_REASON")
    public void FailureTransactionWithErrorCode_RWL_3002() {

        String EsnNo = ExtSerialNoGenerator.get();

        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("93").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");



        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-RWL_3002");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:UDL_0006 :Insta will map to proper response msg: FGW_AMOUNT_LIMIT_EXCEEDED")
    public void FailureTransactionWithErrorCode_UDL_0006() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("94").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013819");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_AMOUNT_LIMIT_EXCEEDED");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-UDL_0006");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_AMOUNT_LIMIT_EXCEEDED");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:RWL_3005 :Insta will map to proper response msg: FGW_TRANS_LIMIT_EXCEEDED")
    public void FailureTransactionWithErrorCode_RWL_3005() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("97").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_USER_KYC_NOT_COMPLETED");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-RWL_3005");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_USER_KYC_NOT_COMPLETED");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:UDL_009 :Insta will map to proper response msg: FGW_TRANS_LIMIT_EXCEEDED")
    public void FailureTransactionWithErrorCode_UDL_0009() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("98").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013825");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_TRANS_LIMIT_EXCEEDED");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-UDL_0009");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_TRANS_LIMIT_EXCEEDED");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }



    @Test(description = "Validate Failure transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL" +
            "Error Code:403 :Insta will map to proper response msg: FGW_BANK_FAIL_SPECIFIC_REASON")
    public void FailureTransactionWithErrorCode_403() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("99").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");


        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-403");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    //Pending to Success

    @Test(description = "Validate Pending to Success callback is pending but status is success transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL")
    public void SuccessTransactionPendingToSuccess() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("95").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        // Status Api transaction assertions

        validateSuccessfulStatusResp(StatusResp);

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultStatus")).isEqualToIgnoringCase("01");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("SUCCESS-0");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("SUCCESS");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


    //Pending to Failure

    @Test(description = "Validate Pending to Success callback is pending but status is success transaction for wallet settlement" +
            ",insta will hit Api from property file and transactionType would be CORPORATE_NODAL")
    public void FailureTransactionPendingToFailure() {


        String EsnNo = ExtSerialNoGenerator.get();
        withdrawRequest Request = new withdrawRequest.Builder(merchantType.getId(), EsnNo)
                .setAmount("96").build();

        SoftAssertions validateSoftly = new SoftAssertions();

        Response response = Request.execute();

        validateSuccessfulWithdrawResp(response);

        String Mbid = response.jsonPath().getString("response.body.mbid");

        withdrawStatus StatusQuery = new withdrawStatus.Builder(Mbid, Request).build();

        Response StatusResp = StatusQuery.execute();

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCodeId")).isEqualToIgnoringCase("23013224");

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.resultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");



        //Assertion from CallBack received from Bank

        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCodeId")).isEqualToIgnoringCase("FAILURE-RWL_3002");
        validateSoftly.assertThat(StatusResp.jsonPath()
                .getString("response.body.transInfo.thirdResultInfo.resultCode")).isEqualToIgnoringCase("FGW_BANK_FAIL_SPECIFIC_REASON");

        validateSoftly.assertAll();

        withdrawStatus.StatusFromMock statusFromMock = new withdrawStatus.StatusFromMock(EsnNo);
        Response MockStatus = statusFromMock.execute();

        //Assertion for Request received to Mock from Insta

        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("benIfsc")).isEqualToIgnoringCase("WALL0123456");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("transactionType")).isEqualToIgnoringCase("CORPORATE_NODAL");
        validateSoftly.assertThat(MockStatus.jsonPath()
                .getString("properties.transfer_mode")).isEqualToIgnoringCase("wallet");

        validateSoftly.assertAll();

    }


}
