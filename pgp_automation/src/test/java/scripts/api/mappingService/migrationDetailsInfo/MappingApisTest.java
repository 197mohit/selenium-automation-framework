package scripts.api.mappingService.migrationDetailsInfo;

import com.paytm.api.MappingService.*;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Owner("Anushka_Goldi")
@Feature("PGP-37785")
public class MappingApisTest extends PGPBaseTest {
    List<String> merchList= new ArrayList<>();


    @Test(description = "To verify Response of MerchantList API")
    public void verifyingResponseMerchantListAPI()  {
        MerchantIdListDetail addMerchant = new MerchantIdListDetail();
        merchList.add("testli61258254741921");
        merchList.add("bxOMNS49814198661147");
        merchList.add("XIKiJh27422061885632");
        addMerchant.buildRequest(merchList);
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("response.messaage")).isEqualTo("Success");
        int s = withDrawJson.getList("merchantInfoList").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("merchantInfoList[\" + i + \"].paytmId")).isNotNull();
            softly.assertThat(withDrawJson.getString("merchantInfoList[\" + i + \"].alipayId")).isNotNull();
            softly.assertThat(withDrawJson.getString("merchantInfoList[\" + i + \"].industryTypeId")).isNotNull();
        }
        softly.assertAll();
    }

    @Owner("Himanshu")
    @Test(description = "To verify Response of Vendor Migrate API")
    public void verifyingVendorMigrateAPI()  {
        String mid= Constants.MerchantType.SPLIT_SETTLEMENT_ADDNPAY.getId();
        VendorDetails addMerchant = new VendorDetails(mid);
        addMerchant.buildRequest(mid);
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson.getString("resultResp.paytmMerchantId")).isNotNull();
        softly.assertThat(withDrawJson.getString("resultResp.alipayMerchantId")).isNotNull();
        softly.assertThat(withDrawJson.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertAll();
    }

    @Owner("Himanshu")
    @Test(description = "To verify Response of MPA Balance API")
    public void verifyingMPABalanceAPI()  {
        String mid= Constants.MerchantType.MIGRATIONDETAIL.getId();
        MerchantMpaBalanceDetail Merchant = new MerchantMpaBalanceDetail(mid);
        JsonPath withDrawJson = Merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("paytmResultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("paytmResultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("paytmResultInfo.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson.getString("response.accountNo")).isNotNull();
        softly.assertThat(withDrawJson.getString("response.accountStatus")).isEqualTo("ENABLE");
        softly.assertThat(withDrawJson.getString("response.debitFreezeStatus")).isEqualTo("ENABLE");
        softly.assertThat(withDrawJson.getString("response.creditFreezeStatus")).isEqualTo("ENABLE");
        softly.assertThat(withDrawJson.getString("response.accountType")).isEqualTo("MERCHANT_PAYABLE_ACCOUNT");
        softly.assertAll();
    }

    @Owner("Anushka_Goldi")
    @Test(description = "To verify ResultInfo of MERCHANT-PREFERENCE-INFO")
    public void verifyingresultInfoPreferenceInfo()  {
        String mid = Constants.MerchantType.MIGRATIONDETAIL.getId();
        GetMerchantPreferenceInfo merchant = new GetMerchantPreferenceInfo(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("resultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("resultInfo.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson.getString("merchantId")).isNotEmpty();
        int s=withDrawJson.getList("merchantPreferenceInfos").size();
        for(int i=0;i<s;i++)
        {
            softly.assertThat(withDrawJson.getString("merchantPreferenceInfos[\" + i + \"].prefStatus")).isNotNull();
        }
        softly.assertAll();
    }

    @Owner("Anushka_Goldi")
    @Test(description = "To verify Response of contract detail")
    public void verifyingContactDetailsInfo()  {
        String ContID = "2021120351051010016800343499156";
        ContactDetails merchant = new ContactDetails(ContID);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("paytmResultInfo.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("paytmResultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("paytmResultInfo.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson.getString("response.resultInfo.resultCode")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("response.resultInfo.resultCodeId")).isEqualTo("00000000");
        softly.assertThat(withDrawJson.getString("response.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("response.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("response.contractBasic.contractId")).isNotEmpty();
        softly.assertThat(withDrawJson.getString("response.contractBasic.merchantId")).isNotEmpty();
        softly.assertAll();
    }

    @Owner("Anushka_Goldi")
    @Test(description = "To verify Response of EMI_DC eligibility")
    public void verifyingEMI_DCeligibility()  {
        String ContNo = "8826170616";
        String BankName = "HDFC";
        EMIDCeligibilityDetail merchant = new EMIDCeligibilityDetail(ContNo, BankName);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("bankName")).isEqualTo(BankName);
        softly.assertThat(withDrawJson.getString("emiOnDcEnable")).isEqualTo("true");
        softly.assertAll();
    }
}