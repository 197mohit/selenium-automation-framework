package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test
import scripts.api.savecardService.SaveCard

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class SaveTransactionCardDetail extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/add/savecard/transaction')
                        .build()
        )
    }

    final def root = {
        [
//                paymentTypeId: null,
//                cardNumber   : null,
//                userId       : null,
//                status       : null,
//                cardType     : null,
//                expiryDate   : null,
//                firstSixDigit: null,
//                lastFourDigit: null,
transactionId: null,
//                created_on   : null,
//                updated_on   : null,
//                custId       : null,
//                mId          : null,
        ]
    }

    @Test
    void testSuccess() {
        def root = root()
        def saveCard = new SaveCard()
        def txnId = new Random().nextLong().abs() as String
        new SavedCardHelpers().saveCardCache(new Random().nextLong().abs() as String, saveCard.AesEncCardNumDebit, saveCard.AesEncExpDebit, "4444333322221111", txnId)
        root.transactionId = txnId
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Integer.class))
    }
}
