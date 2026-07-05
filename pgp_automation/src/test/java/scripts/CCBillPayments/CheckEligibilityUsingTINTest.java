package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.CheckEligibilityUsingTINRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Owner("Mayuri")
@Feature("PGP-35804")
public class CheckEligibilityUsingTINTest extends PGPBaseTest {

    static String Responsemsg = null;
    static String EligibilityRequestId = null;
    static String UniqueId = null;
    static HashMap<String, String> ExtendedInfo = new HashMap<>();
    static String ResponseCode = null;
    static String Message = null;
    static String ResponseStatus = null;
    static String ClientId = null;
    static String Version = null;
    static String ResponseTimestamp = null;
    static String Signature = null;

    @Test(description = "Check Eligibilty Success response for  for MASTER TIN", groups = {"regression"})
    public static void checkEligibiltySuccessForTIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();


        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        Assertions.assertThat(UniqueId).isNotNull();
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("Success");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ExtendedInfo).isNotNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Check Eligibilty response for  for Expired  TIN token expired", groups = {"regression"})
    public static void checkEligibiltySuccessForExpiredToken() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "619cbd56f93f837e22651ee9");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        Assertions.assertThat(UniqueId).isNotNull();
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("Success");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ExtendedInfo).isNotNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();


    }

    @Test(description = "Check Eligibilty Failure response for  for MASTER TIN", groups = {"regression"})
    public static void checkEligibiltyFailureForMasterTIN() throws Exception {
        String expectedEligibilityRequestId = "7381774352";

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", expectedEligibilityRequestId);
        checkEligibilityUsingTINRequest.setContext("body.tin", "61a10a955dc2d36cb0eb0e3c");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(expectedEligibilityRequestId);
        Assertions.assertThat(UniqueId).isNotNull();
        softly.assertThat(ResponseCode).isEqualTo("400");
        softly.assertThat(Responsemsg).isEqualTo("FAILURE_FROM_BANK");
        softly.assertThat(Message).isEqualTo("Failure from bank");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();


    }

    @Test(description = "Check Eligibilty Failure response for  for Dead TIN", groups = {"regression"})
    public static void checkEligibiltyFailureForDeadTIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "620ca35089653228ecec0047");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        Assertions.assertThat(UniqueId).isBlank();
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(Message).isEqualTo("Card token expired");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty Failure response for  for Suspended TIN", groups = {"regression"})
    public static void checkEligibiltyFailureForSuspendedTIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "620b91ac6048100656f93658");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        Assertions.assertThat(UniqueId).isBlank();
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(Message).isEqualTo("Card token expired");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();


    }

    @Test(description = "Check Eligibilty Failure response for  for Failed TIN", groups = {"regression"})
    public static void checkEligibiltyFailureForFailedTIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "61d5aa002ce4a1645eaf0f1f");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        Assertions.assertThat(UniqueId).isBlank();
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(Message).isEqualTo("Card token expired");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Check Eligibilty response when eligibilityRequestId is not passed", groups = {"regression"})
    public static void checkEligibiltyForEligibilityRequestIdNotPassed() throws Exception {

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", "");
        checkEligibilityUsingTINRequest.setContext("body.tin", "61d5aa002ce4a1645eaf0f1f");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();


        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when TIN is not passed", groups = {"regression"})
    public static void checkEligibiltyForTINdNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when entityName is not passed", groups = {"regression"})
    public static void checkEligibiltyForEntityNamedNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.entityName", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when userId is not passed", groups = {"regression"})
    public static void checkEligibiltyForUserIdNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.userId", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when Incorrect TIN is passed", groups = {"regression"})
    public static void checkEligibiltyForIncorrectTINPassed() throws Exception {

        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "61a10a955dc2d36cb0eb0e3ca");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        Assertions.assertThat(UniqueId).isBlank();
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(Message).isEqualTo("Card token expired");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Check Eligibilty response when TIN is NOT passed", groups = {"regression"})
    public static void checkEligibiltyForTINNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.tin", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when Incorrect entityName is passed", groups = {"regression"})
    public static void checkEligibiltyForIncorrectEntityNamePassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.entityName", "MOCK123");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            EligibilityRequestId = jsonPath.get("body.eligibilityRequestId");
            UniqueId = jsonPath.get("body.uniqueId");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("104");
        softly.assertThat(Responsemsg).isEqualTo("SYSTEM_ERROR");
        softly.assertThat(Message).isEqualTo("Internal system error");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when userId is Blank", groups = {"regression"})
    public static void checkEligibiltyForUserIdBlank() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("body.userId", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when version is not Passed", groups = {"regression"})
    public static void checkEligibiltyForVersionNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("head.version", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when requestTimeStamp is not Passed", groups = {"regression"})
    public static void checkEligibiltyForRequestTimeStampNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Check Eligibilty response when channelId is not Passed", groups = {"regression"})
    public static void checkEligibiltyForChannelIdNotPassed() throws Exception {
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibilityUsingTINRequest checkEligibilityUsingTINRequest = new CheckEligibilityUsingTINRequest();
        checkEligibilityUsingTINRequest.setContext("body.eligibilityRequestId", number);
        checkEligibilityUsingTINRequest.setContext("head.clientId", "");
        JsonPath jsonPath = checkEligibilityUsingTINRequest.execute().jsonPath();

        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            ResponseCode = jsonPath.get("body.responseCode");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
            ExtendedInfo = jsonPath.get("body.extendedInfo");

        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("101");
        softly.assertThat(Responsemsg).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(Message).isEqualTo("Illegal parameters");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("NA");
        softly.assertThat(Version).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

}