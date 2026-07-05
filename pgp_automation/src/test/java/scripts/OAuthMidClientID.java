package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import groovy.lang.Tuple2;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.function.BiFunction;



@Epic(Constants.Sprint.SPRINT29_2)
@Feature("PGP-18446")
@Owner("Gagandeep")
public class OAuthMidClientID extends PGPBaseTest {
    private static final String merchant_ae_staging_mid = "2H0lBn73853686334772";
    private static final String merchant_staging_uber_mid = "4TEAQO71553514450074";
    private static final String paytm_client_staging_mid = "No MID Mapped";
    private final HashMap<String, Tuple2> ClientMidMap = new HashMap<>();
    private final BiFunction<User, String, String> ssoToken = (userInfo, Mid) ->
            userInfo.ssoToken((String) ClientMidMap.get(Mid).get(0), (String) ClientMidMap.get(Mid).get(1));
    private final CheckoutPage checkoutPage = new CheckoutPage();


    OAuthMidClientID() {
        ClientMidMap.put(merchant_ae_staging_mid, new Tuple2("merchant-ae-staging", "5a5426a5-57a0-476a-94bf-ca848b97ed4f"));
        ClientMidMap.put(merchant_staging_uber_mid, new Tuple2("merchant-staging-uber", "a7d80af9-6386-48ff-b9b6-71ad80257b34"));
        ClientMidMap.put(paytm_client_staging_mid, new Tuple2("paytm-pg-client-staging", "a7426be0-a2dd-47cf-a181-b37c801f34c6"));
    }

    @Test(description = "Validate if client MID mapping present a basic user info is passed")
    public void successfulUserInfoWhenMidMappedwithClientID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String token = ssoToken.apply(user, merchant_staging_uber_mid);
        String message = user.getUserInfoWithSSOAndMid(token, merchant_staging_uber_mid);
        Assert.assertEquals(message.split("=")[3].split(",")[0], user.mobNo(), "Unable to retrieve MObile Number");

    }

    @Test(description = "Validate if clientID is NOT mapped it will throw error")
    public void midNotMappedWithClientID() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String token = ssoToken.apply(user, merchant_ae_staging_mid);
        String message = user.getUserInfoWithSSOAndMid(token, merchant_staging_uber_mid); //Different MID is passed
        Assertions.assertThat(message)
                .isEqualToIgnoringCase("Invalid clientId to Mid Mapping");

    }

    @Test(description = "Validate No clientID is mapped to any MID basic userInfo is passed")
    public void noClientIDMappedToMID() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String token = ssoToken.apply(user, paytm_client_staging_mid);
        String message = user.getUserInfoWithSSOAndMid(token, Constants.MerchantType.AddMoney.getId()); //Different MID is passed But CLient ID is NOT Mapped with Any MID
        Assert.assertEquals(message.split("=")[3].split(",")[0], user.mobNo(), "Unable to retrieve MObile Number");

    }

    @Test(description = "Validate basic successful transaction when clientID and MID is mapped")
    public void sucessfulTxnWhenClientIDAndMIDisMapped() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String token = ssoToken.apply(user, merchant_staging_uber_mid);
        Constants.MerchantType merchantType = Constants.MerchantType.CLIENT_MAPPED_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify unsuccessful transaction when clientID and MID is NOT mapped")
    public void unsucessfulTxnWhenClientIDAndMIDisNotMapped() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String token = ssoToken.apply(user, merchant_ae_staging_mid);
        Constants.MerchantType merchantType = Constants.MerchantType.CLIENT_MAPPED_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token, merchantType)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String message = response.jsonPath().get("body.resultInfo.resultMsg").toString();
        Assert.assertEquals(message,"SSO Token is invalid","Getting txn token irrespective of clientID and MID is NOT mapped");


    }

}