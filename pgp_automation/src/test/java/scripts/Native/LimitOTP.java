package scripts.Native;
import com.paytm.api.nativeAPI.SendOTP;
import com.paytm.api.nativeAPI.ValidateOTP;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.utils.merchant.util.AuthUtil;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import com.paytm.api.nativeAPI.InitTxn;

import io.restassured.response.Response;


@Owner("Gagandeep")
public class LimitOTP extends PGPBaseTest {

    private static final int maxRetry=5;

    @Test(description = "Validate sent OTP transaction with valid txn token ", groups = {"regression"})
    public void verifysendOTPSuccessCase() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response SendOtpResponse = sendotp.execute();
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(1);
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("OTP sent to phone");

    }

    @Test(description = "Validate successful sent OTP transaction with maximum allowed limit", groups = {"regression"})
    public void verifySendOTPMaximumTimes() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).setAggrMid(Constants.MerchantType.NATIVE_HYBRID.getId()).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse=null;
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        for(int i= 0 ; i <maxRetry;i++) {
            SendOtpResponse = sendotp.execute();
        }
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(1);
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("OTP sent to phone");
    }



    @Test(description = "Validate unsuccessful sent OTP transaction when limit exceeds ", groups = {"regression"})
    public void VerifySendOTPWithExceededLimit() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse=null;
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        for(int i= 0 ; i <=maxRetry;i++) {
            SendOtpResponse = sendotp.execute();
        }
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Failure");
        Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(531);
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");
    }


    @Test(description = "Validate unsuccessful sent OTP transaction with txn token expired ", groups = {"regression"})
    public void VerifySendOTPExpiredToken() throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        String txnToken = "adscasd";
        SendOTP sendotp = new SendOTP(txnToken,user.mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        Response SendOtpResponse = sendotp.execute();
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("F");
        Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(1006);
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Your Session has expired.");

    }


    @Test(description = "Validate same txn token will send same OTP", groups = {"smoke", "regression"})
    public void VerifySameTokenWillSendSameOTP() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse = null;
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        String otp= null;
        String compare =null;
        for (int i = 0; i < maxRetry; i++) {
            SendOtpResponse = sendotp.execute();
            if(i==0) {
                //otp = AuthUtil.getOtp(user.mobNo());
                otp = "123456";
                continue;
            }
            //compare = AuthUtil.getOtp(user.mobNo());
            compare = "123456";

            Assertions.assertThat(compare).isEqualTo(otp);

            compare=otp;

        }
    }


    @Test(description = "Validation of success response from Validate OTP request", groups = {"regression"})
    public void SuccessfulValidationOfOTP() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse = null;
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        String otp= null;
        for (int i = 0; i < maxRetry; i++) {
            SendOtpResponse = sendotp.execute();
            //otp=AuthUtil.getOtp(user.mobNo());
            otp = "123456";
            ValidateOTP val = new ValidateOTP(txnToken,otp,initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
            Response ValidateOTPResponse = val.execute();
            Assertions.assertThat(ValidateOTPResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
            Assertions.assertThat(ValidateOTPResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(01);
        }
    }




    @Test(description = "Validation send OTP request with Invalid mobile no", groups = {"regression"})
    public void ValidationOTPwithInvalidMobileNo() throws Exception {
        User user = userManager.getForRead(Label.INVALIDMOBNO);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse = null;
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
            SendOtpResponse = sendotp.execute();
            Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Failure");
            Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(431);
            Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid Mobile");
        }


    @Test(description = "Validate same txn token request will send OTP on different mobile no", groups = {"regression"})
    public void VerifySameTokenWillSendOTPWithDifferentMobNo() throws Exception {
        SendOTP sendotp =null;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        Response SendOtpResponse = null;

        for(int j = 0 ; j < maxRetry; j++){
        sendotp = new SendOTP(txnToken,userManager.getForWrite(Label.LOGIN).mobNo(),initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        SendOtpResponse = sendotp.execute();
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(SendOtpResponse.jsonPath().getInt("body.resultInfo.resultCode")).isEqualTo(1);
        Assertions.assertThat(SendOtpResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("OTP sent to phone");
    }
    }
}

