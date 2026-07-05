package scripts.VisaSingleClick;

import com.paytm.api.CreateToken;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.api.VisaSingleClick.DeEnrollCard;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Karmvir")
@Feature("PGP-20953")

public class DeEnrollCardTest extends PGPBaseTest {


    @Test(description="To test enrolled card is de-enrol with SSO token and valid card alias and custid")
 public void DeEnrolCardWithSSO() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        String sso= user.ssoToken();
        DeEnrollCard deEnrollCard= new DeEnrollCard(sso,"SSO","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }
@Test(description = "To test when SSO token and cust id are of two different users")
    public void DeEnrollCardwithTwodDifferentUsers() throws Exception
{
    String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
    Constants.MerchantType mid = Constants.MerchantType.PGOnly;
    User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
    String sso= user.ssoToken();
    DeEnrollCard deEnrollCard= new DeEnrollCard(sso,"SSO","401200","1000226237","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"","net.one97.paytm");
    JsonPath savedJson = deEnrollCard.execute().jsonPath();
    Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
    Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token validation success").isEqualTo("SSO Token is invalid");

}

    @Test(description = "To test when Inavlid SSO is provided")
    public void DeEnrollCardwithInvalidSSO() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("SSO-chweuuybc-cebuve","SSO","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token validation success").isEqualTo("SSO Token is invalid");

    }
    @Test(description="To test enrolled card is de-enrol with SSO token and invalid BIN")
    public void DeEnrolCardWithSSOAndInvalidBin() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        String sso= user.ssoToken();
        DeEnrollCard deEnrollCard= new DeEnrollCard(sso,"SSO","rwq7613",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }
    @Test(description = "To test when invalid cust id is provided using SSO token")
    public void DeEnrollCardwithInvalidCustId() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String sso=user.ssoToken();
        DeEnrollCard deEnrollCard= new DeEnrollCard(sso,"SSO","401200","abcd","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token validation success").isEqualTo("SSO Token is invalid");
    }

    @Test(description="To test enrolled card is de-enrol with SSO token and valid card alias,custid and invalid App id")
    public void DeEnrolCardWithSSOInvalidappId() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        String sso= user.ssoToken();
        DeEnrollCard deEnrollCard= new DeEnrollCard(sso,"SSO","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","abcd");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled/app id is correct").isEqualTo("Internal Error");

    }

    @Test(description = "To test enrolled card should de enroll when Access token is provided")
    public  void  DeEnrollCardWithAccesstoken() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid=user.custId();
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        DeEnrollCard deEnrollCard= new DeEnrollCard(AccessToken,"ACCESS","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"7658","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");
    }
    @Test(description = "To test enrolled card should not de enroll when reference id used in de-enroll api is  different from ref id used in create token")
    public  void  DeEnrollCardWithDiffRefIds() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid=user.custId();
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        DeEnrollCard deEnrollCard= new DeEnrollCard(AccessToken,"ACCESS","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId+"63764","7658","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token is valid").isEqualTo("Token validation failed");
    }
    @Test(description = "To test enrolled card should not de enroll when Inavalid access token is provided")
    public  void  DeEnrollCardWithInavlidToken() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid=user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("AccessToken632dg7gd1273671","ACCESS","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"7658","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token is valid").isEqualTo("Token validation failed");
    }

    @Test(description = "To test enrolled card should de enroll when Access toekn is provided with Invalid Bin")
    public  void  DeEnrollCardWithAccesstokenAndinvalidBin() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid=user.custId();
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        DeEnrollCard deEnrollCard= new DeEnrollCard(AccessToken,"ACCESS","vqw344",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"7658","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");
    }

//    @Test(description = "To test when invalid cust id is provided using Access token" , enabled = false)
    public void DeEnrollCardwithInvalidCustIdUsingAccessToken() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        DeEnrollCard deEnrollCard= new DeEnrollCard(AccessToken,"ACCESS","401200","10003cshja","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token validation success").isEqualTo("De Enrollment Failed");

    }

    @Test(description = "To test enrolled card should de enroll when Access token is provided and Invalid AppId is Provided")
    public  void  DeEnrollCardWithAccesstokenAndInvalidAppId() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid=user.custId();
        CreateToken createToken = new CreateToken(mid, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        DeEnrollCard deEnrollCard= new DeEnrollCard(AccessToken,"ACCESS","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"7658","abcd");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled/app id is correct").isEqualTo("Internal Error");

    }

    @Test(description="To test enrolled card is de-enrol with Checksum token and valid card alias and custid")
    public void DeEnrolCardWithChecksum() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("","CHECKSUM","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }
    @Test(description="To test enrolled card is de-enrol with invalid Checksum token and valid card alias and custid")
    public void DeEnrolCardWithInvalidChecksum() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("GYTXJBo6TYqUKLEBZvZGWbrwNSkaCfeTHd1KIMrNsz5WPfSmnvrRtifOe9be2JyYQqh+Z/OCYlgQYuJRz8evC8PgW7rUsWS3PLWATewrucQ=",
                "CHECKSUM","401200",custid,
                "NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is S").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Checksum id Valid").isEqualTo("Checksum provided is invalid");

    }
    @Test(description="To test enrolled card is de-enrol with Checksum token and invalid Bin")
    public void DeEnrolCardWithChecksumAndInvalidBin() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("","CHECKSUM","abc432",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }

//    @Test(description = "To test when invalid cust id is provided using Checksum token", enabled = false)
    public void DeEnrollCardwithInvalidCustIdUsingChecksum() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String sso=user.ssoToken();
        DeEnrollCard deEnrollCard= new DeEnrollCard("","CHECKSUM","401200","1000abcd","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Token validation success").isEqualTo("SSO Token is invalid");

    }

    @Test(description="To test enrolled card is de-enrol with Checksum token and valid card alias,custid and invalid App ID")
    public void DeEnrolCardWithChecksumAndInavlidAppId() throws Exception
    {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user=userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custid= user.custId();
        DeEnrollCard deEnrollCard= new DeEnrollCard("","CHECKSUM","401200",custid,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=",mid ,referenceId,"4546","abcd");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled/app id is correct").isEqualTo("Internal Error");

    }

    @Test(description = "To test enrolled card is de-enrol with Txn_Token token and valid card alias and custid")
    public  void  DeEnrolCardWithTxnToken() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custId = user.custId();
        String sso=user.ssoToken();
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        DeEnrollCard deEnrollCard= new DeEnrollCard(txnToken,"TXN_TOKEN","401200",custId,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,orderId,"net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }
    @Test(description = "To test enrolled card is de-enrol with Inavlid Txn token and valid card alias and custid")
    public  void  DeEnrolCardWithInvalidTxnToken() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custId = user.custId();
        String sso=user.ssoToken();
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        String orderId = initTxnDTO.getBody().getOrderId();
        DeEnrollCard deEnrollCard= new DeEnrollCard(txnToken,"TXN_TOKEN","401200",custId,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,"456798078fdg187","net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is S").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled").isEqualTo("Token validation failed");

    }

    @Test(description = "To test enrolled card is de-enrol with Txn_Token token and invalid BIN")
    public  void  DeEnrolCardWithTxnTokenAndInavlidBin() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custId = user.custId();
        String sso=user.ssoToken();
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        DeEnrollCard deEnrollCard= new DeEnrollCard(txnToken,"TXN_TOKEN","abc453",custId,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,orderId,"net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is F").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is not de-enrolled").isEqualTo("De Enrollment Success");

    }

//    @Test(description = "To test enrolled card is de-enrol with Txn token and Invalid cust id", enabled=false)
    public  void  DeEnrolCardWithInvalidCustId() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String sso=user.ssoToken();
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        DeEnrollCard deEnrollCard= new DeEnrollCard(txnToken,"TXN_TOKEN","401200","435678","NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,orderId,"net.one97.paytm");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is S").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled").isEqualTo("De Enrollment Failed");

    }
    @Test(description = "To test enrolled card is de-enrol with Txn_Token token and valid card alias,custid and Invalid App id")
    public  void  DeEnrolCardWithTxnTokenAndInvalidAppId() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        Constants.MerchantType mid = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String custId = user.custId();
        String sso=user.ssoToken();
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(sso, mid).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        DeEnrollCard deEnrollCard= new DeEnrollCard(txnToken,"TXN_TOKEN","401200",custId,"NDAxMjAwLTAxNTEtLTEwMDAwMzYwMzEtbmV0Lm9uZTk3LnBheXRtLTZkYjVlMDc5YzhkODY5Y2Y=", mid,referenceId,orderId,"abcd");
        JsonPath savedJson = deEnrollCard.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Result message is not F").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Card is de-enrolled/app id is correct").isEqualTo("Internal Error");

    }
}
