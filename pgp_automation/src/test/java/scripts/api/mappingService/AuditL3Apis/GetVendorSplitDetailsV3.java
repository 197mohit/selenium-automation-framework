package scripts.api.mappingService.AuditL3Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetVendorSplitDetailsV3 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get Vendor SplitDetails V3 API paytmResultInfo ")
    void verifyGetVendorSplitDetailsV3_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_VendorSplitDetails_V3(Constants.MerchantType.Vendor_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetVendorSplitDetailsV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get Vendor SplitDetails V3 API response ")
    void verifyGetVendorSplitDetailsV3_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_VendorSplitDetails_V3(Constants.MerchantType.Vendor_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetVendorSplitDetailsV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.parentMid"), Constants.MerchantType.Vendor_MID.getId().toString());
        Assert.assertNotNull(withDrawJson1.getString("response.vendorDetails"));
        Assert.assertEquals(withDrawJson1.getString("response.partiallyMatched"), "false");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get VendorSplitDetails V3 API vendorDetails response ")
    void verifyGetVendorSplitDetailsV3_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_VendorSplitDetails_V3(Constants.MerchantType.Vendor_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetVendorSplitDetailsV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"0"+"].oldpgMerchantId"), "UGtFdXkZ700595556033");
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"0"+"].paytmMerchantId"), "UGtFdXkZ700595556033");
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"0"+"].splitPercentage"), "20.0");
        Assert.assertNull(withDrawJson1.getString("response.vendorDetails["+"0"+"].partnerId"));
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"0"+"].isAggregator"), "false");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get VendorSplitDetails V3 API vendorDetails response ")
    void verifyGetVendorSplitDetailsV3_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_VendorSplitDetails_V3(Constants.MerchantType.Vendor_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetVendorSplitDetailsV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"1"+"].oldpgMerchantId"), "cWKOWSXM935902209982");
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"1"+"].paytmMerchantId"), "cWKOWSXM935902209982");
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"1"+"].splitPercentage"), "10.0");
        Assert.assertNull(withDrawJson1.getString("response.vendorDetails["+"1"+"].partnerId"));
        Assert.assertEquals(withDrawJson1.getString("response.vendorDetails["+"1"+"].isAggregator"), "false");
    }
}
