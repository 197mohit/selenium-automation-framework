package scripts;

import com.paytm.api.user.card.bin.query.BinModifyApi;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.appconstants.Constants;
import com.paytm.framework.conditions.HardAssertion;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import static com.paytm.dto.PaymentDTO.BLOCKED_BIN_NO;


@Owner(Constants.Owner.HIMANSHU)
@Feature("PGP-33435")
public class BinBlock {

    SoftAssertions softly = new SoftAssertions();

    //Common methods
    public void modifyBin(String binNo, String blockedStatus)
    {
        BinModifyApi api = new BinModifyApi(binNo,blockedStatus);
        Response response = api.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }
    public Response binQuery(String binNo, String blockedStatus)
    {
        BinQueryApi api = new BinQueryApi(binNo,blockedStatus);
        Response response = api.execute();
        return response;
    }

    //Test Case
    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-33435")
    @Test(description = "Validate payments are blocked for a bin after blocking it and unblocked after ublocking.")
    public void binBlock()
    {

        //Unblock
        modifyBin(BLOCKED_BIN_NO,"false");
        //Check if bin query api response is false
        Response response = binQuery(BLOCKED_BIN_NO,"false");
        softly.assertThat(response.getStatusCode()).isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("cardBinInfo.cardBin")).isEqualTo(BLOCKED_BIN_NO);
        softly.assertThat(response.jsonPath().getString("cardBinInfo.blocked")).isEqualTo("false");
        //Block
        modifyBin(BLOCKED_BIN_NO,"true");
        //Check if bin query api response is true
        response=binQuery(BLOCKED_BIN_NO,"true");
        softly.assertThat(response.getStatusCode()).isEqualTo(200);
        softly.assertThat(response.jsonPath().getString("cardBinInfo.cardBin")).isEqualTo(BLOCKED_BIN_NO);
        softly.assertThat(response.jsonPath().getString("cardBinInfo.blocked")).isEqualTo("true");
        softly.assertAll();
    }
}