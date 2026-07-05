package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.FulfilmentRequestTIN;
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
public class FulfilmentUsinTIN extends PGPBaseTest {

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

    @Test(description = "Successful fulfilment for TIN VISA Credit Cards")
    public void SuccessFulfilmentForTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);

        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Failure fulfilment for TIN VISA Credit Cards")
    public void FailureFulfilmentForTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.amount", "2");

        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Pending fulfilment for TIN VISA Credit Cards")
    public void PendingFulfilmentForTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.amount", "3");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "Pending fulfilment for Expired TIN ")
    public void FulfilmentForExpiredTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "619cbd56f93f837e22651ee9");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
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

    @Test(description = "fulfilment for Dead TIN ")
    public void FulfilmentForDeadTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "620ca35089653228ecec0047");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "fulfilment for Failed TIN ")
    public void FulfilmentForFailedTIN() throws Exception{

        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "620b98306048100656f93666");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "fulfilment for Suspended TIN ")
    public void FulfilmentForSuspendedTIN() throws Exception{

        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "620b98306048100656f93666");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "fulfilment for Invalid TIN ")
    public void FulfilmentForInvalidTIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "61a10a955dc2d36cb0eb0e3c1");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "fulfilment for Failed MOCK ")
    public void FulfilmentForFailedMOCK() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.entityName", "MOCK1");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("104");
        softly.assertThat(Responsemsg).isEqualTo("SYSTEM_ERROR");
        softly.assertThat(Message).isEqualTo("Exception~Invalid data from MS");
        softly.assertThat(ResponseStatus).isEqualTo("U");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();
    }

    @Test(description = "fulfilment for Invalid USER ")
    public void FulfilmentForInvalidUser() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.userId", "1000711378888888");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();



        if (!(jsonPath == null)) {
            FulfilmentRequestId = jsonPath.get("body.fulfilmentRequestId");
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
        softly.assertThat(FulfilmentRequestId).isEqualTo(String.valueOf(number));
        softly.assertThat(ResponseCode).isEqualTo("415");
        softly.assertThat(Responsemsg).isEqualTo("CARD_TOKEN_EXPIRED");
        softly.assertThat(ResponseStatus).isEqualTo("F");
        Assertions.assertThat(ReferenceNumber).isBlank();
        Assertions.assertThat(ExtendedInfo).isNull();
        softly.assertThat(ClientId).isEqualTo("IN");
        softly.assertThat(Version).isEqualTo("v1");
        Assertions.assertThat(ResponseTimestamp).isNotNull();
        Assertions.assertThat(Signature).isNotNull();
        softly.assertAll();

    }

    @Test(description = "fulfilment for clientID not Passed  ")
    public void FulfilmentForClientIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("head.clientId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for version not Passed  ")
    public void FulfilmentForVersionIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("head.version", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for requestTimeStamp not Passed  ")
    public void FulfilmentForRequestTimeStampNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("head.requestTimeStamp", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for channelId not Passed  ")
    public void FulfilmentForChannelIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("head.channelId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for uniqueId not Passed  ")
    public void FulfilmentForUniqueIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.uniqueId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
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

    @Test(description = "fulfilment for orderId not Passed  ")
    public void FulfilmentForOrderIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.orderId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for fulfilmentRequestId not Passed  ")
    public void FulfilmentForFulfilmentRequestIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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


    @Test(description = "fulfilment for tin not Passed  ")
    public void FulfilmentForTINNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.tin", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
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

    @Test(description = "fulfilment for entityName not Passed  ")
    public void FulfilmentForEntityNameNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.entityName", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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


    @Test(description = "fulfilment for amount not Passed  ")
    public void FulfilmentForAmountNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.amount", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();

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

    @Test(description = "fulfilment for cardHolderName not Passed  ")
    public void FulfilmentForCardHolderNameNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.cardHolderName", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
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

    @Test(description = "fulfilment for userId not Passed  ")
    public void FulfilmentForUserIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequestTIN fulfilmentRequestTIN = new FulfilmentRequestTIN();
        fulfilmentRequestTIN.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequestTIN.setContext("body.userId", "");
        JsonPath jsonPath = fulfilmentRequestTIN.execute().jsonPath();
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
