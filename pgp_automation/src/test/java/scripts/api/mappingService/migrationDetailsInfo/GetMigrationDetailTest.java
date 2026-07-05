package scripts.api.mappingService.migrationDetailsInfo;

import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Owner("Anushka")
@Feature("PGP-37785")
public class GetMigrationDetailTest extends PGPBaseTest {

    @Test(description = "To verify ResultInfo of ExtendedInfo from MigrationDetails API")
    public void verifyingresultInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.resultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.resultInfo.messaage")).isEqualTo("Success");
        softly.assertAll();
    }

    @Test(description = "To verify ExtendedInfo MID of MigrationDetails API")
    public void verifyingMIDExtendedInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.merchantId").isEmpty()).isFalse();
        softly.assertAll();
    }

    @Test(description = "To verify ExtendedInfo of MigrationDetails API")
    public void verifyingExtendedInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.status")).isEqualTo("ACTIVE");
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.keySize")).isNotEmpty();
        softly.assertAll();
    }


    @Test(description = "To verify ResultInfo of MERCHANT-EMI-INFO API")
    public void verifyingresultInfoEmiInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.resultInfo.resultCode")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.resultInfo.resultCodeId")).isEqualTo("00000000");
        softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.resultInfo.resultMsg")).isEqualTo("success");
        softly.assertAll();
    }

    @Test(description = "To verify emiConfigInfos of MERCHANT-EMI-INFO API")
    public void verifyingemiConfigInfos()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.emiConfigInfos").contains("recordId")).isTrue();
        int s=withDrawJson.getList("MERCHANT-EMI-INFO.emiConfigInfos").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.emiConfigInfos[\" + i + \"].recordId")).isNotNull();
            softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.emiConfigInfos[\" + i + \"].merchantId")).isNotNull();
            softly.assertThat(withDrawJson.getString("MERCHANT-EMI-INFO.emiConfigInfos[\" + i + \"].emiInfo.cardAcquiringMode")).isNotNull();
        }
        softly.assertAll();
    }

    @Test(description = "To verify Response of MERCHANT-MAPPING-INFO")
    public void verifyingMrchMappingInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-MAPPING-INFO.paytmId")).isNotEmpty();
        softly.assertThat(withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "To verify MERCHANT-MAPPING-INFO API")
    public void verifyingMappingInfos()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-MAPPING-INFO.industryTypeId")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "To verify ResultInfo of MERCHANT-PREFERENCE-INFO")
    public void verifyingresultInfoPreferenceInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.resultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.resultInfo.messaage")).isEqualTo("Success");
        softly.assertAll();
    }

    @Test(description = "To verify MERCHANT-PREFERENCE-INFO")
    public void verifyingMIDPreferenceInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.merchantId")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "To verify merchantPreferenceInfos of MERCHANT-PREFERENCE-INFO")
    public void verifyingmerchantPreferenceInfos()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        int s=withDrawJson.getList("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos[\" + i + \"].prefStatus")).isNotNull();
        }
        softly.assertAll();
    }

    @Test(description = "To verify ResultInfo of MERCHANT-ACQUIRING-INFO")
    public void verifyingresultInfoAquiringInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultCode")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultCodeId")).isEqualTo("00000000");
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultMsg")).isEqualTo("success");
        softly.assertAll();
    }

    @Test(description = "To verify MERCHANT-ACQUIRING-INFO")
    public void verifyingAquiringInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos")).isNotEmpty();
        int s=withDrawJson.getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos[\" + i + \"].recordId")).isNotNull();
            softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos[\" + i + \"].merchantId")).isNotNull();

        }
        softly.assertAll();
    }
}