package scripts;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.SendOTP;
import com.paytm.api.nativeAPI.ValidateOTP;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.utils.merchant.util.AuthUtil;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("vidhi.gupta")
@Feature("PGP-28989")
@Epic(Constants.Sprint.SPRINT37_0)
public class SubscriptionDoubleOTP extends PGPBaseTest {


    @Step()
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    @Step("Validate fetchPaymentOption paymode status")
    private FetchPaymentOptResponseDTO execute_validateFetchPaymentOption(String txnToken, String mid, String orderId, String payMethod, boolean isDisabledStatus) {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnToken, mid, orderId);
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, payMethod, isDisabledStatus))
                .as(payMethod + " paymethod is disabled or not found")
                .isTrue();
        return fetchPaymentOptResponseDTO;
    }

    @Test(description = "Verify Authorize flag in first FPO before OTP validation when subs_wallet_limit=Y")
    public void TC_SDO001_ValidateAuthorizeFlagFPOBeforeOTPValidation() throws Exception {
        String orderId = String.valueOf(CommonHelpers.getRandomWithSize(10));
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setOrderId(orderId)
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath AuthorizedoubleOTP = response.jsonPath();
        Assertions.assertThat(AuthorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag in FPO is false")
                .isEqualTo("false");

    }

    @Test(description = "Verify Authorize flag in second FPO after OTP validation when subs_wallet_limit=Y and wallet has insufficient balance")
    public void TC_SDO002_ValidateAuthorizeFlagFPOAfterOTPValidation() throws Exception {

        String orderId = String.valueOf(CommonHelpers.getRandomWithSize(10));
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;

        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setOrderId(orderId)
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath authorizedoubleOTP = response.jsonPath();
        Assertions.assertThat(authorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag in FPO is false")
                .isEqualTo("false");

        SendOTP sendOTP = new SendOTP(txnToken, user.mobNo(), merchant.getId(), orderId);
        sendOTP.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        System.out.println(otp);
        ValidateOTP validateOTP = new ValidateOTP(txnToken, otp, merchant.getId(), orderId);
        validateOTP.execute();

        Response response1 = fetchPaymentOption.execute();

        JsonPath AuthorizedoubleOTP = response1.jsonPath();
        Assertions.assertThat(AuthorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag after OTP validation is TRUE")
                .isEqualTo("true");

    }

    @Test(description = "Verify Authorize flag in second FPO after OTP validation when subs_wallet_limit=Y and wallet has sufficient balance")
    public void TC_SDO003_ValidateAuthorizeFlagFPOAfterOTPValidation() throws Exception {

        String orderId = String.valueOf(CommonHelpers.getRandomWithSize(10));
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        Double txnAmount = 10.0;
        WalletHelpers.modifyBalance(user, 20.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath authorizedoubleOTP = response.jsonPath();
        Assertions.assertThat(authorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag in FPO is false")
                .isEqualTo("false");

        SendOTP sendOTP = new SendOTP(txnToken, user.mobNo(), merchant.getId(), orderId);
        sendOTP.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP validateOTP = new ValidateOTP(txnToken, otp, merchant.getId(), orderId);
        validateOTP.execute();

        Response response1 = fetchPaymentOption.execute();

        JsonPath AuthorizedDoubleOTP = response1.jsonPath();
        Assertions.assertThat(AuthorizedDoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag after OTP validation is TRUE")
                .isEqualTo("true");

    }

    @Test(description = "Verify Authorize flag in second FPO after OTP validation when subs_wallet_limit=N and wallet has insufficient balance")
    public void TC_SDO004_ValidateAuthorizeFlagFPOAfterOTPValidation() throws Exception {

        String orderId = String.valueOf(CommonHelpers.getRandomWithSize(10));
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setOrderId(orderId)
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath authorizedoubleOTP = response.jsonPath();
        Assertions.assertThat(authorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag in FPO is false")
                .isEqualTo("false");

        SendOTP sendOTP = new SendOTP(txnToken, user.mobNo(), merchant.getId(), orderId);
        sendOTP.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        System.out.println(otp);
        ValidateOTP validateOTP = new ValidateOTP(txnToken, otp, merchant.getId(), orderId);
        validateOTP.execute();

        Response response1 = fetchPaymentOption.execute();

        JsonPath AuthorizedoubleOTP = response1.jsonPath();
        Assertions.assertThat(AuthorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag after OTP validation is TRUE")
                .isEqualTo("true");

    }

    @Test(description = "Verify Authorize flag in second FPO after OTP validation when subs_wallet_limit=N and wallet has sufficient balance")
    public void TC_SDO005_ValidateAuthorizeFlagFPOAfterOTPValidation() throws Exception {

        String orderId = String.valueOf(CommonHelpers.getRandomWithSize(10));
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        Double txnAmount = 10.0;
        WalletHelpers.modifyBalance(user, 20.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath authorizedoubleOTP = response.jsonPath();
        Assertions.assertThat(authorizedoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag in FPO is false")
                .isEqualTo("false");

        SendOTP sendOTP = new SendOTP(txnToken, user.mobNo(), merchant.getId(), orderId);
        sendOTP.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP validateOTP = new ValidateOTP(txnToken, otp, merchant.getId(), orderId);
        validateOTP.execute();
        Response response1 = fetchPaymentOption.execute();

        JsonPath AuthorizedDoubleOTP = response1.jsonPath();
        Assertions.assertThat(AuthorizedDoubleOTP.getString("body.otpAuthorised")).as("OTP authorized flag after OTP validation is TRUE")
                .isEqualTo("true");

    }


}
