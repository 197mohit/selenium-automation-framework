package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.StatusCheckRequestTIN;
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
public class StatusCheckTIN extends PGPBaseTest {

    static String ResponseCode = null;
    static String Responsemsg = null;
    static HashMap<String, String> ExtendedInfo = new HashMap<>();
    static String Message = null;
    static String ResponseStatus = null;
    static String ReferenceNumber = null;
    static String Version = null;
    static String ResponseTimestamp = null;
    static String Signature = null;
    protected static String ClientId;

    @Test(description = "Successful status query with TIN")
    public static void SuccessStatusQueryForTIN() throws Exception {
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StatusCheckRequestTINId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("S~S");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ReferenceNumber).isNotNull();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "Failure Status Query for TIN VISA Credit Cards")
    public void FailureStatusQueryForTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.amount", "2");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StatusCheckRequestTINId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("400");
        softly.assertThat(Responsemsg).isEqualTo("FAILURE_FROM_BANK");
        softly.assertThat(Message).isEqualTo("F~F");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isNotNull();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Pending Status Query for TIN VISA Credit Cards")
    public void PendingStatusQueryForTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.amount", "6");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StatusCheckRequestTINId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("100");
        softly.assertThat(Responsemsg).isEqualTo("PENDING");
        softly.assertThat(Message).isEqualTo("U~U");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ReferenceNumber).isNotNull();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Pending Status Query for Expired TIN ")
    public void StatusQueryForExpiredTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "619cbd56f93f837e22651ee9");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StatusCheckRequestTINId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("200");
        softly.assertThat(Responsemsg).isEqualTo("SUCCESS");
        softly.assertThat(Message).isEqualTo("S~S");
        softly.assertThat(ResponseStatus).isEqualTo("S");
        Assertions.assertThat(ReferenceNumber).isNotNull();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for Dead TIN ")
    public void StatusQueryForDeadTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "620ca35089653228ecec0047");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("419");
        softly.assertThat(Responsemsg).isEqualTo("INVALID_TOKEN");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for Failed TIN ")
    public void StatusQueryForFailedTIN() throws Exception{
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "620b98306048100656f93666");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("419");
        softly.assertThat(Responsemsg).isEqualTo("INVALID_TOKEN");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for Suspended TIN ")
    public void StatusQueryForSuspendedTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "620b91ac6048100656f93658");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("419");
        softly.assertThat(Responsemsg).isEqualTo("INVALID_TOKEN");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }


    @Test(description = "Status Query for Invalid TIN VISA Credit Cards")
    public void StatusQueryForInvalidTIN() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "61a10a955dc2d36cb0eb0e3c1");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("419");
        softly.assertThat(Responsemsg).isEqualTo("INVALID_TOKEN");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for Invalid MOCK ")
    public void StatusQueryForInvalidMOCK() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.entityName", "MOCK1");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("104");
        softly.assertThat(Responsemsg).isEqualTo("SYSTEM_ERROR");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for Invalid USER ")
    public void StatusQueryForInvalidUser() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.userId", "1000711378888888");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            StatusCheckRequestTINId = jsonPath.get("body.statusRequestId");
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
            ReferenceNumber = jsonPath.get("body.referenceNumber");
            ExtendedInfo = jsonPath.get("body.extendedInfo");
            ClientId = jsonPath.get("head.clientId");
            Version = jsonPath.get("head.version");
            ResponseTimestamp = jsonPath.get("head.responseTimestamp");
            Signature = jsonPath.get("head.signature");
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ResponseCode).isEqualTo("419");
        softly.assertThat(Responsemsg).isEqualTo("INVALID_TOKEN");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "Status Query for clientID not Passed  ")
    public void StatusQueryForClientIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("head.clientId", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for version not Passed  ")
    public void StatusQueryForVersionIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("head.version", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();
    }

    @Test(description = "Status Query for requestTimeStamp not Passed  ")
    public void StatusQueryForRequestTimeStampNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("head.requestTimeStamp", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for channelId not Passed  ")
    public void StatusQueryForChannelIdNotPassed() throws Exception{
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("head.channelId", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for uniqueId not Passed  ")
    public void StatusQueryForUniqueIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.uniqueId", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for orderId not Passed  ")
    public void StatusQueryForOrderIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.orderId", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for statusRequestId not Passed  ")
    public void StatusQueryForStatusRequestIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", "");

        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }


    @Test(description = "Status Query for tin not Passed  ")
    public void StatusQueryForTINNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.tin", "");
        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for entityName not Passed  ")
    public void StatusQueryForEntityNameNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.entityName", "");
        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }


    @Test(description = "Status Query for amount not Passed  ")
    public void StatusQueryForAmountNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.amount", "");
        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for cardHolderName not Passed  ")
    public void StatusQueryForCardHolderNameNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.cardHolderName", "");
        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }

    @Test(description = "Status Query for userId not Passed  ")
    public void StatusQueryForUserIdNotPassed() throws Exception{
        String StatusCheckRequestTINId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequestTIN statusCheckRequestTIN = new StatusCheckRequestTIN();
        statusCheckRequestTIN.setContext("body.statusRequestId", number);
        statusCheckRequestTIN.setContext("body.userId", "");
        JsonPath jsonPath = statusCheckRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            ResponseCode = jsonPath.get("body.responseCode");
            Responsemsg = jsonPath.get("body.responseMessage");
            Message = jsonPath.get("body.message");
            ResponseStatus = jsonPath.get("body.responseStatus");
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
        softly.assertThat(ClientId).isEqualTo("NA");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isBlank();
        softly.assertAll();

    }
}


