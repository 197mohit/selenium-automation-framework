package scripts.api.savedcardservice

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given

//API has 0 traffic as per 'https://wiki.mypaytm.com/display/PGP/Analysis+%7C+SavedCard+Service+Migration' so won't be automating
@Owner('Deepak')
class GetSavedCardByUserIdInstruments extends TestSetUp {

    final def req = { userId, instrumentsCSV ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/v1/get/savedcard/userId/instruments/${userId}/${instrumentsCSV}")
                        .build()
        )
    }
}
