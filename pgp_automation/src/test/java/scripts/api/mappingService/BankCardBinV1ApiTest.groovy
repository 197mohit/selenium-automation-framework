package scripts.api.mappingService

import io.qameta.allure.Owner
import io.qameta.allure.Story
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Gagandeep Singh')
@Story('Bin Migration')
class BankCardBinV1ApiTest extends BankCardBinApiTest {

    def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/mapping-service/get/bankcard/v1/bin/{bin}')
                        .build()
        )
    }

    ResponseSpecification schema() { new ResponseSpecBuilder()
            .expectStatusCode(isIn(200))
            .expectContentType(ContentType.JSON)
            .expectBody('', allOf(hasKey('id'), hasKey('bin'), hasKey('isIndian'), hasKey('bank'), hasKey('cardType'), hasKey('cardName'), hasKey('bankCode'), hasKey('instId'), hasKey('iDebitEnabled'), hasKey('cardEnabled'), hasKey('ccDirectEnabled'), hasKey('displayBankName'), hasKey('isEmiPlanActive'), hasKey('oneClickSupported'), hasKey('zeroSuccessRate'), hasKey('active')))
            .expectBody("id", allOf(isA(Integer.class), notNullValue()))
            .expectBody("bin", allOf(isA(Integer.class), notNullValue()))
            .expectBody("isIndian", allOf(instanceOf(Boolean.class), isIn(false, true)))
            .expectBody("bank", allOf(isA(String.class)))
            .expectBody("cardType", allOf(isA(String.class), isIn("DEBIT_CARD", "CREDIT_CARD")))
            .expectBody("cardName", allOf(isA(String.class), isIn("VISA", "MASTER", "MAESTRO", "RUPAY", "AMEX", "DINERS", "BAJAJFN")))
            .expectBody("bankCode", allOf(isA(String.class)))
            .expectBody("iDebitEnabled", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("cardEnabled", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("ccDirectEnabled", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("isEmiPlanActive", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("oneClickSupported", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("zeroSuccessRate", allOf(isA(Boolean.class), isIn(false, true)))
            .expectBody("binInfoSource", allOf(isA(String.class), isIn("DATABASE_MATCH","PATTERN_MATCH","DEFAULT_MATCH"))) // Database_Match -- P + bin fetching PG_DataBaseMatch -- For PG database
            .expectBody("active", allOf(isA(Boolean.class), isIn(false, true)))
            .build()
        }


//testforZeroSuccessRate

    @Test
    void 'testForZeroSuccessRateTrue'() {
        req().pathParam('bin', cards.find { it.type == "credit" && it.scheme=='diners'}.tap {
            assert it.setZeroSuccessRate(true)
        }.no[0..5]).get().then()
                .body('zeroSuccessRate', equalTo(true))

    }

    @Test
    void 'testForZeroSuccessRateFalse'() {
        req().pathParam('bin', cards.find { it.type == "credit" && it.scheme=='diners'}.tap {
            assert it.setZeroSuccessRate(false)
        }.no[0..5]).get().then()
                .body('zeroSuccessRate', equalTo(false))

    }
}
