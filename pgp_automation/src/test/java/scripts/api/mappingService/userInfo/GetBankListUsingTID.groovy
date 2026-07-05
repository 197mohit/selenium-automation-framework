package scripts.api.mappingService.userInfo


//import com.paytm.appconstants.Constants.MappingService.GET_BANK_DETAILS_FROM_TID

import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Owner.POONAM;
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner(POONAM)
class GetBankListUsingTID extends TestSetUp {


    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath("mapping-service/eos/merchant/device/details/bankslist/tid/11000065")

    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    @Test
    void 'verify size of bank channels being configured for same tid'() {
        def GetBankListFromTID = req()
                .get()
                .then()
        Assertions.assertThat(GetBankListFromTID.body("data.size()", is(2)));
    }

}