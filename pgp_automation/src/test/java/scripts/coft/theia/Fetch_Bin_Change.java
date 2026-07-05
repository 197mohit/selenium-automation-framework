package scripts.coft.theia;

import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class Fetch_Bin_Change extends PGPBaseTest {
    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "416021").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_Master() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "550690").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("MASTER");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("MASTER");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_RUPAY() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "530562").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("false");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("RUPAY");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("RUPAY");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_Tokenbin() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "448968238").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_without_card_prefix_mapping() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "123765421").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("false");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("false");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("false");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("MAESTRO");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("MAESTRO");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_cardCountryCode_parameter() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "416021").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.countryCode")).isEqualTo("IN");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.countryName")).isEqualTo("India");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.countryNumericCode")).isEqualTo("356");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.currencyCode")).isEqualTo("INR");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.currencySymbol")).isEqualTo("₹");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_card_bin_in_alphanumeric_fetch_card_bin_request_should_display_validation_message_in_response() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "rty^%45").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_Additional_params_in_response_Corporate_card() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "4731765131526259").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.binTokenization")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isEligibleForCoft")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.isCoftPaymentSupported")).isEqualTo("true");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelName")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.channelCode")).isEqualTo("VISA");
        Assertions.assertThat(fetchBinsJson.getString("body.supportedCardSubTypes")).isEqualTo("[CORPORATE_CARD]");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_card_bin_less_than_6_digit_fetch_card_bin_request_should_display_validation_message_in_response() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "41601").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_card_bin_between_6to9_digit_fetch_card_bin_request_should_display_data_in_response() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "47664166").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_card_bin_More_then_9_digit_fetch_card_bin_request_should_display_data_in_response() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "4160177654").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_card_bin_as_card_number_fetch_card_bin_request_should_display_data_in_response() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "4761360075863216").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_user_enters_9_digit_card_bin_is_not_mapped_with_card_number_then_cardprefix_or_acccountbinrange_should_display_in_response_or_not() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "123456789").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("UNPAY Prepaid card is not allowed for this payment. Please try paying using other cards/options.");

    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-40009")
    @Test(description = "Verify coft fetch bin changes ")
    public void verify_coft_fetchbin_changes_when_cardbin_detail_parameter_length_should_be_of_6_digits() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.COFT_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "416021").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body.binDetail.bin");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail.cardPrefix")).isEqualTo(fetchBinsJson.getString("body.binDetail.bin"));
        Assertions.assertThat(bin.length()).isEqualTo(6);

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable true in case of CC of 6 digit bin when bin is eligible at bin center")
    public void verify_CC_6_digit_bin_isEmiAvailable_true() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "476136").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("true");

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable true in case of CC of 9 digit bin when bin is eligible at bin center ")
    public void verify_CC_9_digit_bin_isEmiAvailable_true() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "476136007").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("true");

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable false in case of CC of 6 digit bin when bin is not eligible at bin center")
    public void verify_CC_6_digit_bin_isEmiAvailable_false() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("false");

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable false in case of CC of 9 digit bin when bin is not eligible at bin center ")
    public void verify_CC_9_digit_bin_isEmiAvailable_false() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865010").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("false");

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable false in case of DC of 6 digit bin when bin is eligible at bin center")
    public void verify_DC_6_digit_bin_isEmiAvailable_true() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "457274").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("true");

    }
    @Owner(Constants.Owner.AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify isEmiAvailable false in case of DC of 9 digit bin when bin is eligible at bin center ")
    public void verify_DC_9_digit_bin_isEmiAvailable_true() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("1000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "457274165").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        String bin = fetchBinsJson.getString("body");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("true");

    }
}
