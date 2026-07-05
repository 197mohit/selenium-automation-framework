package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test
import scripts.api.savecardService.SaveCard

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class AddSavedCardByMIdCustId extends TestSetUp {

    def primary

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/savedcardservice/savedCardService/v1/add/savecardBymIdCustId')
                        .build()
        )
    }

    final def root = {
        cards.find().with {
            [
                    paymentTypeId: '0',
                    cardNumber   : new SaveCard().AesEncCardNumDebit,
                    expiryDate   : new SaveCard().AesEncExpDebit,
                    created_on   : null,
                    updated_on   : null,
                    entityId     : null,
                    mId          : m().id,
                    firstSixDigit: it.no[0..5],
                    lastFourDigit: it.no[-4..-1],
                    custId       : m().users[0].id,
                    userId       : null,
                    cardType     : null,
            ].findAll { it.value != null }
        }
    }

//    @Merchant(edit = true)
//    @Test(enabled = false)//TODO this api renders MID & CUSTID combination unusable. Need to raise bug for it.
    void testSuccess() {
        def root = root()
        m().users[0].savedCards.clear()
        req().body(root).post().then()
                .statusCode(200)
                .body('responseStatus', equalTo('SUCCESS'),
                'httpCode', equalTo('200'),
                'httpSubCode', equalTo('200'),
                'codeDetail', equalTo('Success'),
                'response', isA(Integer.class))
        assert !m().users[0].savedCards.empty
    }
}
