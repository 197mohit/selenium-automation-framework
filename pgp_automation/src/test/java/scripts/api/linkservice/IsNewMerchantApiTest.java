package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.IsNewMerchant;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40210")
public class IsNewMerchantApiTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @DataProvider(name = "Dataset")
    public Object[][] linkTypeSet()
    {
        return new Object[][]
                {
                        {"FIXED"},
                        {"GENERIC"},
                        {"INVOICE"}
                };
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful isNewMerchant link response when newMerchant is true")
    public void isNewMerchant_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        IsNewMerchant isNewMerchant=new IsNewMerchant().buildRequest(mid,"PAYMENT_BUTTON");
        JsonPath withDrawJson2 = isNewMerchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ISNEW_MERCHANT_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ISNEW_MERCHANT_CODE);
        Assert.assertEquals(withDrawJson2.getString("body.newMerchant"),"true");
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful isNewMerchant link response when mid is null")
    public void isNewMerchant_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        IsNewMerchant isNewMerchant=new IsNewMerchant().buildRequest(null,"PAYMENT_BUTTON");
        JsonPath withDrawJson2 = isNewMerchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ISNEW_MERCHANT_NULLMID_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ISNEW_MERCHANT_NULLMID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful isNewMerchant link response when newMerchant is false")
    public void isNewMerchant_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        IsNewMerchant isNewMerchant=new IsNewMerchant().buildRequest(mid,"PAYMENT_BUTTON");
        JsonPath withDrawJson2 = isNewMerchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ISNEW_MERCHANT_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ISNEW_MERCHANT_CODE);
        Assert.assertEquals(withDrawJson2.getString("body.newMerchant"),"false");
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful isNewMerchant link response when featurename is null")
    public void isNewMerchant_04(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        IsNewMerchant isNewMerchant=new IsNewMerchant().buildRequest(mid,null);
        JsonPath withDrawJson2 = isNewMerchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ISNEW_MERCHANT_NULL_FEATURE_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ISNEW_MERCHANT_NULL_FEATURE_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful isNewMerchant link response when featurename is wrong")
    public void isNewMerchant_05(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        IsNewMerchant isNewMerchant=new IsNewMerchant().buildRequest(mid,"ABCD");
        JsonPath withDrawJson2 = isNewMerchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),ISNEW_MERCHANT_WRONG_FEATURE_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),ISNEW_MERCHANT_WRONG_FEATURE_CODE);
    }

}
