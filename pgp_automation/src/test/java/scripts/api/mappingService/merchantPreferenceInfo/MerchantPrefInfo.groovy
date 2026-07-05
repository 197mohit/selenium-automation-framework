package scripts.api.mappingService.merchantPreferenceInfo

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_MERCH_PREFERENCE_INFO
import static com.paytm.appconstants.Constants.MerchantType.PGOnly
import static io.restassured.RestAssured.given

public class MerchantPrefInfo extends TestSetUp {


    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_MERCH_PREFERENCE_INFO)
                        .build()
        )
    }

    //Verify Scan & Pay Description Offline Text

    @Merchant(edit = true, value = { it.id == PGOnly.id })
    @Test
    void 'Verify Scan & Pay Description Offline Flag'() {

        def resp = req().pathParam('mid', m().id).get().body()
                .jsonPath().get('merchantPreferenceInfos').find
                {
                    it.prefType == 'OFFLINE_SNP_DESC_FLAG'
                }

        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(resp.PrefType == 'OFFLINE_SNP_DESC_FLAG')
        softly.assertThat(resp.Status == 'ACTIVE')
        softly.assertThat(resp.PrefValue == 'Y')
    }

    @Merchant(edit = true, value = { it.id == PGOnly.id })
    @Test
    void 'Verify Scan & Pay Description Offline Text'() {


        def resp = req().pathParam('mid', m().id).get().body()
                .jsonPath().get('merchantPreferenceInfos').find
                {
                    it.prefType == 'OFFLINE_SNP_DESC_TXT'
                }

        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(resp.PrefType == 'OFFLINE_SNP_DESC_TXT')
        softly.assertThat(resp.Status == 'ACTIVE')
        softly.assertThat(resp.PrefValue == 'Y')
    }

}
