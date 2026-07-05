package scripts.coft.savedCardService;

import com.paytm.api.coft.saveCard.SavecardServicegetGcinV1;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CoftHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class SavecardServicegetGcinV1Test extends PGPBaseTest {

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message is displayed if wrong accesstoken is sent in request")
    public void verifyWrongAccessToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String accessToken = CoftHelper.getAccessToken(user.custId());

        SavecardServicegetGcinV1 savecardServicegetGcinV1 = new SavecardServicegetGcinV1();
        savecardServicegetGcinV1.buildRequest(accessToken+"1",user.custId(), PaymentDTO.VISA_COFT_CARD_NUMBER);
        JsonPath savecardServicegetGcinV1Response = savecardServicegetGcinV1.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savecardServicegetGcinV1Response.getString("responseStatus")).isEqualTo("FAILURE");
        softly.assertAll();

    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message is displayed if wrong tokenType is sent in request")
    public void verifyWrongTokenType() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String accessToken = CoftHelper.getAccessToken(user.custId());

        SavecardServicegetGcinV1 savecardServicegetGcinV1 = new SavecardServicegetGcinV1();
        savecardServicegetGcinV1.buildRequest(accessToken,user.custId(), PaymentDTO.VISA_COFT_CARD_NUMBER).setContext("head.tokenType","Test");
        JsonPath savecardServicegetGcinV1Response = savecardServicegetGcinV1.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savecardServicegetGcinV1Response.getString("responseStatus")).isEqualTo("FAILURE");
        softly.assertAll();

    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message is displayed if wrong userId is sent in request")
    public void verifyWrongUserId() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String accessToken = CoftHelper.getAccessToken(user.custId());

        SavecardServicegetGcinV1 savecardServicegetGcinV1 = new SavecardServicegetGcinV1();
        savecardServicegetGcinV1.buildRequest(accessToken,user.custId()+"1", PaymentDTO.VISA_COFT_CARD_NUMBER);
        JsonPath savecardServicegetGcinV1Response = savecardServicegetGcinV1.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savecardServicegetGcinV1Response.getString("responseStatus")).isEqualTo("FAILURE");
        softly.assertAll();

    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message is displayed if card is not sent in request")
    public void verifyCardNotSent() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String accessToken = CoftHelper.getAccessToken(user.custId());

        SavecardServicegetGcinV1 savecardServicegetGcinV1 = new SavecardServicegetGcinV1();
        savecardServicegetGcinV1.buildRequest(accessToken,user.custId(), "");
        JsonPath savecardServicegetGcinV1Response = savecardServicegetGcinV1.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savecardServicegetGcinV1Response.getString("body.resultInfo.resultStatus")).isEqualTo("FAIL");
        softly.assertAll();

    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message is displayed if card is not sent in request")
    public void verifySuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String accessToken = CoftHelper.getAccessToken(user.custId());

        SavecardServicegetGcinV1 savecardServicegetGcinV1 = new SavecardServicegetGcinV1();
        savecardServicegetGcinV1.buildRequest(accessToken,user.custId(), PaymentDTO.VISA_COFT_CARD_NUMBER);
        JsonPath savecardServicegetGcinV1Response = savecardServicegetGcinV1.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savecardServicegetGcinV1Response.getString("head.responseTimestamp")).isNotNull();
        softly.assertThat(savecardServicegetGcinV1Response.getString("head.version")).isNotNull();
        softly.assertThat(savecardServicegetGcinV1Response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savecardServicegetGcinV1Response.getString("body.resultInfo.resultCode")).isEqualTo("00000000");
        softly.assertThat(savecardServicegetGcinV1Response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softly.assertThat(savecardServicegetGcinV1Response.getString("body.gcin")).isNotNull();
        softly.assertAll();

    }
}
