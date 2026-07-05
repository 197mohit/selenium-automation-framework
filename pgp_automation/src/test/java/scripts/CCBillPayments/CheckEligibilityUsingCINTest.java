package scripts.CCBillPayments;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.CheckEligibiltyRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Owner("Mayuri")
@Feature("PGP-35804")
public class CheckEligibilityUsingCINTest extends PGPBaseTest {

    static String Responsemsg = null;
    static String UniqueId = null;
    static HashMap<String, String> ExtendedInfo = new HashMap<>();
    static String ResponseCode = null;
    static String Message = null;
    static String ResponseStatus = null;
    static String ClientId = null;
    static String Version = null;
    static String ResponseTimestamp = null;
    static String Signature = null;
    protected static String requestID;


    @Test(description = "Check Eligibilty Success response for  for VISA CIN", groups = {"regression"})
    public static void checkEligibiltySuccessForVisaCIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        //this CIN is of VISA card number = 4718650100010336
        checkEligibiltyRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d12");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();
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

    @Test(description = "Check Eligibilty Success response for  for MASTER CIN", groups = {"regression"})
    public static void checkEligibiltySuccessForMasterCIN() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        //this CIN is of VISA card number = 5129670504622379
        checkEligibiltyRequest.setContext("body.cin", "20200819638704ad1ebeb0b2e2ed819476f314006c3cd");
        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

    @Test(description = "Check Eligibilty Failure response for  for VISA CIN", groups = {"regression"})
    public static void checkEligibiltyFailureForVisaCIN() throws Exception {
        String ExpectedEligibilityRequestId = "7381774322";
        String EligibilityRequestId = null;

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", ExpectedEligibilityRequestId);
        //this CIN is of VISA card number = 4718650100010336
        checkEligibiltyRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d12");
        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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
        softly.assertThat(EligibilityRequestId).isEqualTo(ExpectedEligibilityRequestId);
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

    @Test(description = "Check Eligibilty Failure response for  for MASTER CIN", groups = {"regression"})
    public static void checkEligibiltyFailureForMasterCIN() throws Exception {
        String ExpectedEligibilityRequestId = "7381774332";
        String EligibilityRequestId = null;

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", ExpectedEligibilityRequestId);
        //this CIN is of VISA card number = 5129670504622379
        checkEligibiltyRequest.setContext("body.cin", "20200819638704ad1ebeb0b2e2ed819476f314006c3cd");
        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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
        softly.assertThat(EligibilityRequestId).isEqualTo(ExpectedEligibilityRequestId);
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

    @Test(description = "Check Eligibilty response when eligibilityRequestId is not passed", groups = {"regression"})
    public static void checkEligibiltyForEligibilityRequestIdNotPassed() throws Exception {

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();
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

    @Test(description = "Check Eligibilty response when cin is not passed", groups = {"regression"})
    public static void checkEligibiltyForCINdNotPassed() throws Exception {
        String EligibilityRequestId = null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        //this CIN is of VISA card number = 4718650100010336
        checkEligibiltyRequest.setContext("body.cin", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("body.entityName", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();


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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("body.userId", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

    @Test(description = "Check Eligibilty response when Incorrect CIN is passed", groups = {"regression"})
    public static void checkEligibiltyForIncorrectCINPassed() throws Exception {
        String EligibilityRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d12g");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(EligibilityRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Check Eligibilty response when Incorrect entityName is passed", groups = {"regression"})
    public static void checkEligibiltyForIncorrectEntityNamePassed() throws Exception {
        String EligibilityRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("body.entityName", "MOCK1");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();
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
        softly.assertThat(ResponseCode).isEqualTo("104");
        Assertions.assertThat(UniqueId).isBlank();
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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("body.userId", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("head.version", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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

        CheckEligibiltyRequest checkEligibiltyRequest = new CheckEligibiltyRequest();
        checkEligibiltyRequest.setContext("body.eligibilityRequestId", number);
        checkEligibiltyRequest.setContext("head.channelId", "");

        JsonPath jsonPath = checkEligibiltyRequest.execute().jsonPath();

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