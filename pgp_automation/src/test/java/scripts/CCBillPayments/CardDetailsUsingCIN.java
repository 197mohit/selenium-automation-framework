package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.CheckCardDetailsUsingCINRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Feature("PGP-35804")
@Owner("Mayuri")
public class CardDetailsUsingCIN extends PGPBaseTest {
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

    @Test(description = "Pass a valid MASTER credit card number CIN in the body and check response", groups = {"regression"})
    public void SuccessMasterCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();

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

    @Test(description = "Pass a invalid CIN in the body and check response", groups = {"regression"})
    public void PassInvalidCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d1");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();

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
        softly.assertThat(ResponseCode).isEqualTo("410");
        softly.assertThat(Responsemsg).isEqualTo("TOKEN_FAILURE");
        softly.assertThat(Message).isEqualTo("Token generation failed");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        softly.assertThat(ExtendedInfo).isNull();
        softly.assertThat(IssuingBank).isNull();
        softly.assertThat(CardScheme).isNull();
        softly.assertThat(CardType).isNull();
        softly.assertThat(DisplayName).isNull();
        softly.assertThat(CardVariant).isNull();
        softly.assertThat(LastFourDigits).isNull();
        softly.assertThat(IsIndian).isNull();
        softly.assertThat(FirstSixDigits).isNull();
        softly.assertThat(MaskedCardNumber).isNull();
        softly.assertThat(CIN).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        softly.assertThat(ResponseTimestamp).isNotNull();
        softly.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Do not pass client id and check response", groups = {"regression"})
    public void ClientIdNotPassedCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("head.clientId", "");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();
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

    @Test(description = "Do not pass version and check response", groups = {"regression"})
    public void VersionNotPassedCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("head.version", "");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();

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

    @Test(description = "Do not pass request timestamp and check response", groups = {"regression"})
    public void RequestTimestampNotPassedCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();

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

    @Test(description = "Do not pass channel ID and check response", groups = {"regression"})
    public void ChannelIDNotPassedCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("head.channelId", "");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();

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

    @Test(description = "Do not pass CIN and check response", groups = {"regression"})
    public void CINPassedCardDetailsUsingCIN() throws Exception {
        CheckCardDetailsUsingCINRequest checkCardDetailsUsingCINRequest = new CheckCardDetailsUsingCINRequest();
        checkCardDetailsUsingCINRequest.setContext("body.cin", "");

        JsonPath jsonPath = checkCardDetailsUsingCINRequest.execute().jsonPath();
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

}