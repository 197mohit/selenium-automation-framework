package scripts.CCBillPayments;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.api.billproxy.CheckCardDetailsUsingSSORequest;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Feature("PGP-35804")
@Owner("Mayuri")
public class CardDetailsUsingSSO extends PGPBaseTest {
    static String ResponseCode = null;
    static String Responsemsg = null;
    static String ExtendedInfo = null;
    static String Message = null;
    static String ResponseStatus = null;
    static String IssuingBank = null;
    static String CardScheme = null;
    static String CardType = null;
    static String DisplayName = null;
    static String CardVariant = null;
    static String LastFourDigits = null;
    static String IsIndian = null;
    static String FirstSixDigits = null;
    static String MaskedCardNumber = null;
    static String CIN = null;
    static String Version = null;
    static String ResponseTimestamp = null;
    static String Signature = null;
    protected static String ClientId;
    static String tokenExpiry = null;
    static String cardExpiry = null;
    static String tokenStatus = null;

    @Test(description = "Pass a valid MASTER credit card number in the body and check response")
    public void SuccessMasterCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            IssuingBank = jsonPath.get("body.issuingBank");
            CardScheme = jsonPath.get("body.cardScheme");
            CardType = jsonPath.get("body.cardType");
            DisplayName = jsonPath.get("body.displayName");
            CardVariant = jsonPath.get("body.cardVariant");
            LastFourDigits = jsonPath.get("body.lastFourDigits");
            IsIndian = jsonPath.get("body.isIndian");
            FirstSixDigits = jsonPath.get("body.firstSixDigits");
            MaskedCardNumber = jsonPath.get("body.maskedCardNumber");
            CIN = jsonPath.get("body.cin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("Success");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(IssuingBank).isEqualTo("HDFC");
        softly.assertThat(CardScheme).isEqualTo("MASTER");
        softly.assertThat(CardType).isEqualTo("CREDIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("display_name_test");
        softly.assertThat(CardVariant).isEqualTo("Diners Privilege");
        softly.assertThat(LastFourDigits).isEqualTo("0016");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isEqualTo("550690");
        softly.assertThat(MaskedCardNumber).isEqualTo("5506 90XX XXXX 0016");
        softly.assertThat(CIN).isEqualTo("20211122200203fd95b36b9219fff06b8f415b4036cb9");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Pass a valid VISA credit card number in the body and check response")
    public void SuccessVisaCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("body.cardNumber", "4718650100010336");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            IssuingBank = jsonPath.get("body.issuingBank");
            CardScheme = jsonPath.get("body.cardScheme");
            CardType = jsonPath.get("body.cardType");
            DisplayName = jsonPath.get("body.displayName");
            CardVariant = jsonPath.get("body.cardVariant");
            LastFourDigits = jsonPath.get("body.lastFourDigits");
            IsIndian = jsonPath.get("body.isIndian");
            FirstSixDigits = jsonPath.get("body.firstSixDigits");
            MaskedCardNumber = jsonPath.get("body.maskedCardNumber");
            CIN = jsonPath.get("body.cin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("Success");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(IssuingBank).isEqualTo("HDFC");
        softly.assertThat(CardScheme).isEqualTo("VISA");
        softly.assertThat(CardType).isEqualTo("CREDIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("HDFC Bank");
        Assertions.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isEqualTo("0336");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isEqualTo("471865");
        softly.assertThat(MaskedCardNumber).isEqualTo("4718 65XX XXXX 0336");
        softly.assertThat(CIN).isEqualTo("2020072717530a2ebbb09d36bac1c63ba066e376f1d12");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Pass a invalid card number in the body and check response")
    public void InvalidCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("body.cardNumber", "55069");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Pass a channelId=WEB MASTER credit card number in the body and check response")
    public void ChannelIdWEBMasterCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("head.clientId", "WEB");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            IssuingBank = jsonPath.get("body.issuingBank");
            CardScheme = jsonPath.get("body.cardScheme");
            CardType = jsonPath.get("body.cardType");
            DisplayName = jsonPath.get("body.displayName");
            CardVariant = jsonPath.get("body.cardVariant");
            LastFourDigits = jsonPath.get("body.lastFourDigits");
            IsIndian = jsonPath.get("body.isIndian");
            FirstSixDigits = jsonPath.get("body.firstSixDigits");
            MaskedCardNumber = jsonPath.get("body.maskedCardNumber");
            CIN = jsonPath.get("body.cin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("Success");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(IssuingBank).isEqualTo("HDFC");
        softly.assertThat(CardScheme).isEqualTo("MASTER");
        softly.assertThat(CardType).isEqualTo("CREDIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("display_name_test");
        softly.assertThat(CardVariant).isEqualTo("Diners Privilege");
        softly.assertThat(LastFourDigits).isEqualTo("0016");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isEqualTo("550690");
        softly.assertThat(MaskedCardNumber).isEqualTo("5506 90XX XXXX 0016");
        softly.assertThat(CIN).isEqualTo("20211122200203fd95b36b9219fff06b8f415b4036cb9");
        softly.assertThat(ClientId).isEqualTo("WEB");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass clientID in the head and check response")
    public void ClientIdNotPassedCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("head.clientId", "");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Test(description = "Do not pass version in the head and check response")
    public void VersionClientIdNotPassedCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("head.version", "");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Test(description = "Do not pass RequestTimeStamp in the head and check response")
    public void RequestTimeStampNotPassedCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Test(description = "Do not pass channelID in the head and check response")
    public void ChannelIdNotPassedCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("head.channelId", "");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Test(description = "Do not pass card number in the body and check response")
    public void CardNumberNotPassedCardDetailsUsingSSO() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken());

        checkCardDetailsUsingSSORequest.setContext("body.cardNumber", "");

        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isBlank();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate Success Txn with TIN for v1/fetch/cardDetails/tin/sso/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateSuccessRespWithTIN1() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();

        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken(), tin, user.custId());
        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body")).contains("tokenExpiry");
        Assertions.assertThat(jsonPath.getString("body.cardExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.tokenStatus")).isEqualTo("ACTIVE");
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Success");
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate FAILURE Response with TIN for v1/fetch/cardDetails/tin/sso/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateFailureRespWithTIN2() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin() + 1;

        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken(), tin, user.custId());
        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body")).contains("tokenExpiry");
        Assertions.assertThat(jsonPath.getString("body.cardExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.tokenStatus")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("CARD_NOT_FOUND");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Sorry, we could not find your card details. Please enter your card number.");
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate Dead Token Response with TIN for v1/fetch/cardDetails/tin/sso/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateDeadTokenRespWithTIN3() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user, tin, Constants.VAULTIDENTIFIER.OCL.get());

        CheckCardDetailsUsingSSORequest checkCardDetailsUsingSSORequest = new CheckCardDetailsUsingSSORequest(user.ssoToken(), tin, user.custId());
        JsonPath jsonPath = checkCardDetailsUsingSSORequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body")).contains("tokenExpiry");
        Assertions.assertThat(jsonPath.getString("body.cardExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.tokenStatus")).isEqualTo("DEAD");
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Success");
    }

}
