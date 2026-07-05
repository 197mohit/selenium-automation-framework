package scripts.VisaSingleClick;


import com.paytm.api.CreateToken;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Karmvir")
@Feature("PGP-20953")

public class CreateTokenTest extends PGPBaseTest {

    @Test(description = "Access Token should create when refid and mid is provided with valid checksum")
    public void createTokenwithRefidAndMid(){
  String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken= new CreateToken(mid,referenceId);
       JsonPath jsonpath=createToken.execute().jsonPath();
       String AccessToken=jsonpath.getString("body.accessToken");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).as("Result status is F").isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).as("Result message is Fail").isEqualTo("Success");
    }

    @Test(description = "Access Token should not create when refid's length is less then 10 digit and mid is provided with valid checksum")
    public void createTokenwithMIDAndRefidIslessThen10Digit(){
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(5));
        Constants.MerchantType mid =Constants.MerchantType.PGOnly;
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath=createToken.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).as("Result status is not F").isEqualTo("F");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).as("Request message is not expected").
        isEqualTo("Request parameters are not valid. ReferenceId should fulfill: Minimum length of 10 and maximum length of 20.");

}
    @Test(description = "Access Token should not create when refid's length is Greater then 20 digit and mid is provided with valid checksum")
    public void createTokenwithMIDAndRefidisgreaterThen20Digit(){
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"12345678909876543";
        Constants.MerchantType mid =Constants.MerchantType.PGOnly;
        CreateToken createToken= new CreateToken(mid,referenceId);
        JsonPath jsonpath= createToken.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).as("Result status is not F").isEqualTo("F");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).as("Request message is not expected").
                isEqualTo("Request parameters are not valid. ReferenceId should fulfill: Minimum length of 10 and maximum length of 20.");
    }
    @Test(description ="Access Toekn should not be craeted when invalid MID is provided")
            public void createTokenwithMIDAndRefidAndInvalidMID() {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
    }


}
