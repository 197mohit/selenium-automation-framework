package scripts.api.mappingService.mappingDrop3;

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

public class Entityurlinfoformid extends PGPBaseTest {
    String mid = Constants.MerchantType.Attribute_key_mid1.getId();
    String urltype = "REQUEST";
    String websitename = "retail";



    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Entityurlinfoformid API with mid,urltype,websitename ")
    void Verifyentityurlinfoformid1() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Entityurlinfoformid(mid,urltype,websitename);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyEntityurlinfoformid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantId"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("postBackurl"), "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse");
        Assert.assertEquals(withDrawJson1.getString("urlTypeId"), "REQUEST");
        Assert.assertEquals(withDrawJson1.getString("status"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("comments"), null);
        Assert.assertNotNull(withDrawJson1.getString("createdOn"));
        Assert.assertNotNull(withDrawJson1.getString("modifiedOn"));
        Assert.assertEquals(withDrawJson1.getString("websiteName"), "retail");
        Assert.assertEquals(withDrawJson1.getString("requestName"), "");
        Assert.assertEquals(withDrawJson1.getString("notificationStatusUrl"), "https://automation-pg-ext.paytm.in/mockbank/peon");
        Assert.assertEquals(withDrawJson1.getString("imageName"), null);
        Assert.assertEquals(withDrawJson1.getString("imageData"), null);
        Assert.assertEquals(withDrawJson1.getString("mid"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("refundUrl"), null);

    }


}
