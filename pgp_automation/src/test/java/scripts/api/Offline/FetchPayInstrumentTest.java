package scripts.api.Offline;

import com.paytm.api.offline.FetchPayInstrument;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OfflineDto.FetchPayInstrumentRequest;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by anjukumari on 18/02/19
 */
@Owner("Deepak")
public class FetchPayInstrumentTest extends PGPBaseTest {

    public Response validateFetchPayInstrument(String mid, String token) {
        FetchPayInstrumentRequest fetchPayInstrumentRequest = new FetchPayInstrumentRequest();
        fetchPayInstrumentRequest.changeMidInRequest(mid);
        fetchPayInstrumentRequest.changeTokenInRequest(token);
        Response response = FetchPayInstrument.executeFetchPaymtInstrument(fetchPayInstrumentRequest);
        Assertions.assertThat(response.getStatusCode()).as("Status Code is: " + response.getStatusCode() + " expected was: 200").isEqualTo(200);
        return response;
    }


    @Test(description = "Validate success response of fetch pay instrument APIs when valid token is passed")
    public void FetchPayIns() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        validateFetchPayInstrument(Constants.MerchantType.PPBL_PAYTMCC_VPA.getId(), user.ssoToken());
    }


    @Test(description = "Validate Failure response of fetch pay instrument APIs with invalid token")
    public void FetchPayIns_InvalidToken() {
        FetchPayInstrumentRequest fetchPayInstrumentRequest = new FetchPayInstrumentRequest();
        fetchPayInstrumentRequest.changeMidInRequest(Constants.MerchantType.PPBL_PAYTMCC_VPA.getId());
        fetchPayInstrumentRequest.changeTokenInRequest("abc");
        Response result = FetchPayInstrument.executeFetchPaymtInstrument(fetchPayInstrumentRequest);
        Assertions.assertThat(result.getStatusCode()).as("This APIs does not work for invalid user token, Response code should not be 200, but response code is : " + result.getStatusCode()).isNotEqualTo(200);
    }

    @Test(description = "Validate saved card in response of fetch pay instrument APIs when valid token is passed")
        public void FetchPayIns_saveCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String card1 = "4109139965359183";
        String card2 = "5507032420388415";
        SavedCardHelpers.addCard(user, "06", "2022", card1);
        SavedCardHelpers.addCard(user, "07", "2023", card2);
        String cardId1 = SavedCardHelpers.getSavedCardId(user, 0);
        String cardId2 = SavedCardHelpers.getSavedCardId(user, 1);
        String cardIndex1 = SavedCardHelpers.getCIN(cardId1);
        String cardIndex2 = SavedCardHelpers.getCIN(cardId2);
        Response result = validateFetchPayInstrument(Constants.MerchantType.PGOnly.getId(), user.ssoToken());
        List savedCardList = result.jsonPath().getList("body.payMethodViews.merchantSavedInstruments.savedCards.cardDetails.cardId");
        Assertions.assertThat(savedCardList)
                .as("Card saved on user not is present in response")
                .contains(cardIndex1, cardIndex2); // We have enabled flags now CIN is coming in FPI Api instead of cardID
    }


    @Test(description = "Validate PPBL in response of fetch pay instrument APIs when valid token is passed and ppbl is enabled on user and merchant")
    public void FetchPayIns_PPBL() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Response result = validateFetchPayInstrument(Constants.MerchantType.PGOnly.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("PPBL not found in merchant pay methods").contains("NET_BANKING_PPBL");
    }


    @Test(description = "Validate PAYTM_DIGITAL_CREDIT in response of fetch pay instrument APIs when valid token is passed and ppbl is enabled on user and merchant")
    public void FetchPayIns_PaytmDigitalCredit() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Response result = validateFetchPayInstrument(Constants.MerchantType.Hybrid.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("Paytm digital credit not found in merchant pay methods").contains("PAYTM_DIGITAL_CREDIT");
    }


    @Test(description = "Validate credit card in response of fetch pay instrument APIs when valid token is passed and credit card is enabled on user and merchant")
    public void FetchPayIns_creditcard() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Response result = validateFetchPayInstrument(Constants.MerchantType.Hybrid.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("Credit card not found in merchant pay methods").contains("CREDIT_CARD");
    }

    @Test(description = "Validate balance in response of fetch pay instrument APIs when valid token is passed and wallet is enabled on user and merchant")
    public void FetchPayIns_balance() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Response result = validateFetchPayInstrument(Constants.MerchantType.WalletOnly.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("Wallet not found in merchant pay methods").contains("BALANCE");
    }

    @Test(description = "Validate netbanking in response of fetch pay instrument APIs when valid token is passed and netbanking is enabled on user and merchant")
    public void FetchPayIns_netbanking() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Response result = validateFetchPayInstrument(Constants.MerchantType.Hybrid.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("Netbanking not found in merchant pay methods").contains("NET_BANKING");
    }


    @Test(description = "Validate UPI in response of fetch pay instrument APIs when valid token is passed and UPI is enabled on user and merchant")
    public void FetchPayIns_UPI() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Response result = validateFetchPayInstrument(Constants.MerchantType.EMI.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getList("body.payMethodViews.merchantPayMethods.findAll{ merchantPayMethods -> merchantPayMethods.isDisabled.status == 'false'}.payMethod")).as("UPI not found in merchant pay methods").contains("UPI");
    }

    @Test(description = "Validate Saved VPA getting returned in response of fetch pay instrument APIs when valid token is passed and VPA is saved on that number.")
    public void FetchPayIns_SavedVpaV2() throws Exception {
        User user = userManager.getForRead(Label.VPACHECKED);
        Response result = validateFetchPayInstrument(Constants.MerchantType.PPBLC_ONLY.getId(), user.ssoToken());
        Assertions.assertThat(result.jsonPath().getString("body.payMethodViews.merchantSavedInstruments.sarvatraVpa[0]")).as("SarvatraVpa not found").endsWith("@paytm");
        Assertions.assertThat(result.jsonPath().getString("body.payMethodViews.merchantSavedInstruments.sarvatraUserProfile.status")).as("SarvatraUserProfileStatus is not success.").isEqualTo("success");
        Assertions.assertThat(result.jsonPath().getString("body.payMethodViews.merchantSavedInstruments.sarvatraUserProfile.response.vpaDetails[0].name")).as("SarvatraUserProfile VPADetails not found").endsWith("@paytm");;
    }

}
