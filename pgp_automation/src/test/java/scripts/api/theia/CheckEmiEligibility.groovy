package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.CHECK_EMI_ELIGIBILITY
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class CheckEmiEligibility extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(CHECK_EMI_ELIGIBILITY)
                .addQueryParam('mid', m()?.id ?: new Random().nextLong().abs() as String)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        requestId: new Random().nextLong().abs() as String,
                        tokenType: 'SSO',
                        token    : user().tokens['sso'].id
                ],
                body: [
                        userInfo   : null,//[ssoToken: user().tokens['sso'].id],
                        mid        : m().id,
                        payMethod  : null,
                        channelCode: null,
                        emiId      : null,
                        emiTypes   : null//['DEBIT_CARD', 'CREDIT_CARD'],

                ]
        ]
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        def emi = m().emis.find().id
        root.body.emiId = emi
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body')
                .body('emiEligibility', isA(List.class))
                .root('body.emiEligibility')
                .body('channelCode', everyItem(not(isEmptyOrNullString())),
                'emiType', everyItem(not(isEmptyOrNullString())),
                'eligible', everyItem(isA(Boolean.class)))
    }


}
