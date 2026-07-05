package scripts.CCBillPayments;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.billproxy.FulfilmentRequest;
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
    public class FulfilmentTest extends PGPBaseTest {

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

        @Test(description = "Successful fulfilment for CIN VISA Credit Cards")
        public void SuccessFulfilmentForCIN() throws Exception{
            String FulfilmentRequestId= null;
            long number = ThreadLocalRandom.current().nextLong(0,999999999);

            FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
            fulfilmentRequest.setContext("body.fulfilmentRequestId", number);

            JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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

    @Test(description = "Failure fulfilment for CIN VISA Credit Cards")
    public void FailureFulfilmentForCIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.amount", "2");

        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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

    @Test(description = "Pending fulfilment for CIN VISA Credit Cards")
    public void PendingFulfilmentForCIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.amount", "3");

        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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


    @Test(description = "fulfilment for Invalid CIN VISA Credit Cards")
    public void FulfilmentForInvalidCIN() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.cin", "2020072717530a2ebbb09d36bac1c63ba066e376f1d121");

        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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

    @Test(description = "fulfilment for Invalid MOCK ")
    public void FulfilmentForInvalidMOCK() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.entityName", "MOCK123");

        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.userId", "10000360888888");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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

    @Test(description = "fulfilment for clientID not Passed  ")
    public void FulfilmentForClientIdNotPassed() throws Exception{
        String FulfilmentRequestId= null;
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("head.clientId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("head.version", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("head.requestTimeStamp", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("head.channelId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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

        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.uniqueId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();

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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.orderId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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

    @Test(description = "fulfilment for fulfilmentID not Passed  ")
    public void FulfilmentForFulfilmentIDNotPassed() throws Exception{

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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


    @Test(description = "fulfilment for cin not Passed  ")
    public void FulfilmentForCINNotPassed() throws Exception{
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.cin", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.entityName", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.amount", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.cardHolderName", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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
        long number = ThreadLocalRandom.current().nextLong(0,999999999);

        FulfilmentRequest fulfilmentRequest = new FulfilmentRequest();
        fulfilmentRequest.setContext("body.fulfilmentRequestId", number);
        fulfilmentRequest.setContext("body.userId", "");
        JsonPath jsonPath = fulfilmentRequest.execute().jsonPath();
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


