package scripts.CCBillPayments;

import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CCBillPayments.FetchBin.Head;
import com.paytm.dto.CCBillPayments.Tokenize.Body;
import com.paytm.dto.CCBillPayments.Tokenize.CardTokenize;
import com.paytm.dto.CCBillPayments.Tokenize.CardTokenizerequest;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.base.test.Group.Status.TO_BE_FIXED;

@Owner("Deepak")
public class CardTokenizeTest extends PGPBaseTest {
    @Test(description = "To generate cache card token using CC number",groups = {"regression"})
    public static void CardTokenizeusingCCNumber() throws Exception{
            User user = userManager.getForRead(Label.BASIC);
            Body body = new Body();
            Head head = new Head();
            body.setCardNumber(PaymentDTO.MASTERCARD_CC_BILL_PAYMENT);
            CardTokenize tokenize = new CardTokenize();
            tokenize.setBody(body);
            tokenize.setHead(head);
            CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.ccNumber, user);
            JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
            String Responsemsg = null;
            String CardToken = null;
            if (!(jsonPath == null)) {
                Responsemsg = jsonPath.get("body.responseMessage");
                CardToken = jsonPath.get("body.cardToken");
            }
            Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("Success");
            Assertions.assertThat(CardToken).isNotNull();

    }

    @Test(description = "Invalid CardNumber to generate card token",groups = {"regression"})
    public static void CardTokenizeusingInvalidCCNumber() throws Exception{
            User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
            Body body = new Body();
            Head head = new Head();
            body.setCardNumber(PaymentDTO.INVALID_CARD);
            CardTokenize tokenize = new CardTokenize();
            tokenize.setBody(body);
            tokenize.setHead(head);
            CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.ccNumber, user);
            JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
            String responsemsg = null;
            String cardToken = null;
            if (!(jsonPath == null)) {
                responsemsg = jsonPath.get("body.responseMessage");
                cardToken = jsonPath.get("body.cardToken");
            }
            Assertions.assertThat(responsemsg).isEqualToIgnoringCase("TOKEN_FAILURE");
            Assertions.assertThat(cardToken).isNull();

    }

    @Test(description = "To generate cache card token using SavedCardID",groups = {"regression"})
    public static void CardTokenizeusingvalidSavedcardId() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO=new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Body body = new Body();
        Head head = new Head();
        body.setSavedCardId(SavedCardHelpers.getSavedCardId(user,0));
        CardTokenize tokenize = new CardTokenize();
        tokenize.setBody(body);
        tokenize.setHead(head);
        CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.savedCardId, user);
        JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
        String Responsemsg = null;
        String CardToken = null;
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            CardToken = jsonPath.get("body.cardToken");
        }
        Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("Success");
        Assertions.assertThat(CardToken).isNotNull();
    }

    @Test(description = "To check card token genreration gets failed in case invalid SavedCardID is passed",groups = {"regression"})
    public static void CardTokenizeusingInvalidSavedcardId() throws Exception{
            User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
            Body body = new Body();
            Head head = new Head();
            body.setSavedCardId("0");
            CardTokenize tokenize = new CardTokenize();
            tokenize.setBody(body);
            tokenize.setHead(head);
            CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.savedCardId, user);
            JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
            String Responsemsg = null;
            String CardToken = null;
            if (!(jsonPath == null)) {
                Responsemsg = jsonPath.get("body.responseMessage");
                CardToken = jsonPath.get("body.cardToken");
            }
            Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("TOKEN_FAILURE");
            Assertions.assertThat(CardToken).isNull();
    }

    //TODO need to create seperate api for /savedCardService/v1/add/recentCCBillPayment and fetch creditcardid from there
    @Test(description = "To check card token is generated in case valid CreditcardId is passed", groups = {"regression", TO_BE_FIXED})
    public static void cardTokenizeUsingValidCreditCardId() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO=new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Body body = new Body();
        Head head = new Head();
        body.setCreditCardId(SavedCardHelpers.getSavedCardId(user,0));
        CardTokenize tokenize = new CardTokenize();
        tokenize.setBody(body);
        tokenize.setHead(head);
        CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.creditCardId, user);
        JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
        String Responsemsg = null;
        String CardToken = null;
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
            CardToken = jsonPath.get("body.cardToken");
        }
        Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("Success");
        Assertions.assertThat(CardToken).isNotNull();
    }


    @Test(description = "To check card token genreration gets failed in case invalid CreditcardId is passed",groups = {"regression"})
    public static void cardTokenizeUsingInvalidCreditCardId() throws Exception{
            User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
            Body body = new Body();
            Head head = new Head();
            body.setCreditCardId("0");
            CardTokenize tokenize = new CardTokenize();
            tokenize.setBody(body);
            tokenize.setHead(head);
            CardTokenizerequest cardTokenizerequest = new CardTokenizerequest(tokenize, CardTokenize.TokenizeType.creditCardId, user);
            JsonPath jsonPath = cardTokenizerequest.execute().jsonPath();
            String Responsemsg = null;
            String CardToken = null;
            if (!(jsonPath == null)) {
                Responsemsg = jsonPath.get("body.responseMessage");
                CardToken = jsonPath.get("body.cardToken");
            }
            Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("TOKEN_FAILURE");
            Assertions.assertThat(CardToken).isNull();

    }
}
