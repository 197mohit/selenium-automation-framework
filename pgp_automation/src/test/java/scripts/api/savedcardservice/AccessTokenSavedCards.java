package scripts.api.savedcardservice;

import com.paytm.CreateToken;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.UUID;

@Owner("VIDHI GUPTA")
@Feature("PGP-29551")
public class AccessTokenSavedCards extends PGPBaseTest {

    @Test(description = "Saved cards should not come when FF4J flag theia.blacklistSavedCardsInFPOV2WithAccessToken = ON")
    public void savedCardsOnAccessTokenFPOV2WithFlagON() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", AccessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpoReq = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpoReq.getRequestSpecBuilder()
                .addQueryParam("referenceId", refId);
        Response AccessSavedCards = fpoReq.execute();

        AccessSavedCards.then()
                .body("body.merchantPayOption.savedInstruments", Matchers.empty());
    }

    @Test(description = "Saved cards should come when FF4J flag theia.blacklistSavedCardsInFPOV2WithAccessToken = OFF")
    public void savedCardsOnAccessTokenFPOV2WithFlagOFF() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", AccessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpoReq = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpoReq.getRequestSpecBuilder()
                .addQueryParam("referenceId", refId);
        Response AccessSavedCards = fpoReq.execute();

        AccessSavedCards.then()
                .body("body.merchantPayOption.savedInstruments", Matchers.not(Matchers.empty()));
    }

    @Test(description = "Saved cards should not come when FF4J flag theia.blacklistSavedCardsInFPOV1WithAccessToken = ON")
    public void savedCardsOnAccessTokenFPOV1WithFlagON() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", AccessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOption fpoReq = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        fpoReq.getRequestSpecBuilder()
                .addQueryParam("referenceId", refId);

        Response AccessSavedCards = fpoReq.execute();
        AccessSavedCards.then()
                .body("body.merchantPayOption.savedInstruments", Matchers.empty());
    }

    @Test(description = "Saved cards should come when FF4J flag theia.blacklistSavedCardsInFPOV1WithAccessToken = OFF")
    public void savedCardsOnAccessTokenFPOV1WithFlagOFF() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", AccessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOption fpoReq = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        fpoReq.getRequestSpecBuilder()
                .addQueryParam("referenceId", refId);

        Response AccessSavedCards = fpoReq.execute();
        AccessSavedCards.then()
                .body("body.merchantPayOption.savedInstruments", Matchers.not(Matchers.empty()));
    }


}