package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.StatusCheckRequest;
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
public class StatusCheckTest extends PGPBaseTest {

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

    @Test(description = "Successful status query with CIN")
    public static void SuccessStatusForCIN() throws Exception {
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);

        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Failure status query for CIN VISA Credit Cards")
    public void FailureStatusQueryForCIN() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.amount", "2");

        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Pending status query for CIN VISA Credit Cards")
    public void PendingStatusQueryForCIN() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.amount", "6");

        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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


    @Test(description = "status query for Invalid CIN VISA Credit Cards")
    public void StatusQueryForInvalidCIN() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d121");

        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "status query for Invalid MOCK ")
    public void StatusQueryForInvalidMOCK() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.entityName", "MOCK1");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "status query for Invalid USER ")
    public void StatusQueryForInvalidUser() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.userId", "1000711378888888");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
        if (!(jsonPath == null)) {
            StatusCheckRequestId = jsonPath.get("body.statusRequestId");
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
        softly.assertThat(StatusCheckRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "status query for clientID not Passed  ")
    public void StatusQueryForClientIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("head.clientId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for version not Passed  ")
    public void StatusQueryForVersionIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("head.version", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();

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

    @Test(description = "status query for requestTimeStamp not Passed  ")
    public void StatusQueryForRequestTimeStampNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("head.requestTimeStamp", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for channelId not Passed  ")
    public void StatusQueryForChannelIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("head.channelId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for uniqueId not Passed  ")
    public void StatusQueryForUniqueIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.uniqueId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();

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

    @Test(description = "status query for orderId not Passed  ")
    public void StatusQueryForOrderIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.orderId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for statusReqID not Passed  ")
    public void StatusQueryForStatusReqIDNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.statusRequestId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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


    @Test(description = "status query for cin not Passed  ")
    public void StatusQueryForCINNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.cin", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();

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

    @Test(description = "status query for entityName not Passed  ")
    public void StatusQueryForEntityNameNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.entityName", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();

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


    @Test(description = "status query for amount not Passed  ")
    public void StatusQueryForAmountNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.amount", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for cardHolderName not Passed  ")
    public void StatusQueryForCardHolderNameNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.cardHolderName", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();
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

    @Test(description = "status query for userId not Passed  ")
    public void StatusQueryForUserIdNotPassed() throws Exception{
        String StatusCheckRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        StatusCheckRequest statusCheckRequest = new StatusCheckRequest();
        statusCheckRequest.setContext("body.statusRequestId", number);
        statusCheckRequest.setContext("body.userId", "");
        JsonPath jsonPath = statusCheckRequest.execute().jsonPath();

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


