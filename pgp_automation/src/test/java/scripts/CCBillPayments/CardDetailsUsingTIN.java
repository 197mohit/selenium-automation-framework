package scripts.CCBillPayments;

import com.paytm.api.billproxy.CheckCardDetailsUsingSSORequest;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.CheckCardDetailsUsingTINRequest;
import com.paytm.base.test.User;
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
public class CardDetailsUsingTIN extends PGPBaseTest {
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
    static String TIN = null;
    static String Version = null;
    static String ResponseTimestamp = null;
    static String Signature = null;
    protected static String ClientId;

    @Test(description = "Pass a valid MASTER debit card number TIN in the body and check response", groups = {"regression"})
    public void SuccessMasterCardDetailsUsingTIN() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(IssuingBank).isEqualTo("ICICI");
        softly.assertThat(CardScheme).isEqualTo("MASTER");
        softly.assertThat(CardType).isEqualTo("DEBIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("ICICI Bank");
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isEqualTo("0001");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isBlank();
        softly.assertThat(MaskedCardNumber).isEqualTo("XXXX XXXX XXXX 0001");
        softly.assertThat(TIN).isEqualTo("61a10a955dc2d36cb0eb0e3c");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();


    }

    @Test(description = "Pass a valid TIN whose status is INIT in the body and check response", groups = {"regression"})
    public void TINStatusINITCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "62147e2ed1bd60404c7a4149");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

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
            TIN = jsonPath.get("body.tin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("410");
        softly.assertThat(Responsemsg).isEqualTo("TOKEN_FAILURE");
        softly.assertThat(Message).isEqualTo("Token generation failed");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(IssuingBank).isNull();
        softly.assertThat(CardScheme).isNull();
        softly.assertThat(CardType).isNull();
        softly.assertThat(DisplayName).isNull();
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isNull();
        softly.assertThat(IsIndian).isNull();
        softly.assertThat(FirstSixDigits).isNull();
        softly.assertThat(MaskedCardNumber).isNull();
        softly.assertThat(TIN).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Pass a valid TIN whose status is Dead in the body and check response", groups = {"regression"})
    public void TINStatusDeadCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "620ca35089653228ecec0047");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(CardType).isEqualTo("DEBIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("HDFC Bank");
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isEqualTo("0421");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isBlank();
        softly.assertThat(MaskedCardNumber).isEqualTo("XXXX XXXX XXXX 0421");
        softly.assertThat(TIN).isEqualTo("620ca35089653228ecec0047");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Pass a valid TIN whose status is SUSPENDED in the body and check response", groups = {"regression"})
    public void TINStatusSuspendedCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "620b91ac6048100656f93658");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(CardType).isEqualTo("DEBIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("HDFC Bank");
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isEqualTo("0421");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isBlank();
        softly.assertThat(MaskedCardNumber).isEqualTo("XXXX XXXX XXXX 0421");
        softly.assertThat(TIN).isEqualTo("620b91ac6048100656f93658");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Pass a valid TIN whose status is FAILED in the body and check response", groups = {"regression"})
    public void TINStatusFailedCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "620b98306048100656f93666");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("410");
        softly.assertThat(Responsemsg).isEqualTo("TOKEN_FAILURE");
        softly.assertThat(Message).isEqualTo("Token generation failed");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(IssuingBank).isNull();
        softly.assertThat(CardScheme).isNull();
        softly.assertThat(CardType).isNull();
        softly.assertThat(DisplayName).isNull();
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isNull();
        softly.assertThat(IsIndian).isNull();
        softly.assertThat(FirstSixDigits).isNull();
        softly.assertThat(MaskedCardNumber).isNull();
        softly.assertThat(TIN).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Pass invalid TIN and check response", groups = {"regression"})
    public void PassInvalidTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "61a10a955dc2d36cb0eb0e3c1");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("410");
        softly.assertThat(Responsemsg).isEqualTo("TOKEN_FAILURE");
        softly.assertThat(Message).isEqualTo("Token generation failed");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(IssuingBank).isNull();
        softly.assertThat(CardScheme).isNull();
        softly.assertThat(CardType).isNull();
        softly.assertThat(DisplayName).isNull();
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isNull();
        softly.assertThat(IsIndian).isNull();
        softly.assertThat(FirstSixDigits).isNull();
        softly.assertThat(MaskedCardNumber).isNull();
        softly.assertThat(TIN).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass clinetID and check response", groups = {"regression"})
    public void ClientIDNotPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("head.clientId", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Do not pass version and check response", groups = {"regression"})
    public void VersionNotPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("head.version", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass requestTimeStamp and check response", groups = {"regression"})
    public void RequestTimeStampNotPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass channel ID and check response", groups = {"regression"})
    public void ChannelIdNotPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("head.channelId", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass TIN and check response", groups = {"regression"})
    public void TINPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.tin", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }


    @Test(description = "Do not pass userId and check response", groups = {"regression"})
    public void UserIdPassedTINCardDetails() throws Exception {
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest();
        checkCardDetailsUsingTINRequest.setContext("body.userId", "");

        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();
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
            TIN = jsonPath.get("body.tin");
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
        softly.assertThat(IssuingBank).isEqualTo("ICICI");
        softly.assertThat(CardScheme).isEqualTo("MASTER");
        softly.assertThat(CardType).isEqualTo("DEBIT_CARD");
        softly.assertThat(DisplayName).isEqualTo("ICICI Bank");
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isEqualTo("0001");
        softly.assertThat(IsIndian).isEqualTo("TRUE");
        softly.assertThat(FirstSixDigits).isBlank();
        softly.assertThat(MaskedCardNumber).isEqualTo("XXXX XXXX XXXX 0001");
        softly.assertThat(TIN).isEqualTo("61a10a955dc2d36cb0eb0e3c");
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate Success Txn with TIN for v1/fetch/cardDetails/tin/sig/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateSuccessRespWithTIN1() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();

        SignatureUtility signatureUtility = new SignatureUtility();
        String signature = signatureUtility.Signature(tin);

        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest(user.ssoToken(), tin, user.custId(), signature);
        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

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
    @Test(description = "Validate Failure Txn with TIN for v1/fetch/cardDetails/tin/sig/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateFailureRespWithTIN2() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin() + 1;

        SignatureUtility signatureUtility = new SignatureUtility();
        String signature = signatureUtility.Signature(tin);
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest(user.ssoToken(), tin, user.custId(), signature);
        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.tokenExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.cardExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.tokenStatus")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("CARD_NOT_FOUND");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Sorry, we could not find your card details. Please enter your card number.");
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate Dead Token Response with TIN for v1/fetch/cardDetails/tin/sig/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateDeadTokenRespWithTIN3() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user, tin, Constants.VAULTIDENTIFIER.OCL.get());

        SignatureUtility signatureUtility = new SignatureUtility();
        String signature = signatureUtility.Signature(tin);
        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest(user.ssoToken(), tin, user.custId(), signature);
        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body")).contains("tokenExpiry");
        Assertions.assertThat(jsonPath.getString("body.cardExpiry")).isEqualTo(null);
        Assertions.assertThat(jsonPath.getString("body.tokenStatus")).isEqualTo("DEAD");
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Success");
    }

    @Feature("PG2-14091")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47756")
    @Test(description = "Validate Invalid Signature Response with TIN for v1/fetch/cardDetails/tin/sig/request contains tokenExpiry,cardExpiry,tokenStatus")
    public void ValidateInvalidSignatureRespWithTIN4() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();

        SignatureUtility signatureUtility = new SignatureUtility();
        String signature = signatureUtility.Signature(tin) + 1;

        CheckCardDetailsUsingTINRequest checkCardDetailsUsingTINRequest = new CheckCardDetailsUsingTINRequest(user.ssoToken(), tin, user.custId(), signature);
        JsonPath jsonPath = checkCardDetailsUsingTINRequest.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.responseCode")).isEqualTo("102");
        Assertions.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("U");
        Assertions.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("INVALID_SIGNATURE");
        Assertions.assertThat(jsonPath.getString("body.message")).isEqualTo("Invalid signature");
    }
}