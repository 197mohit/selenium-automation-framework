package scripts.SimplifiedUnifiedOffers;

import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.util.ArrayList;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.dto.PaymentDTO.*;

public class SimplifiedUnifiedOffersTest extends PGPBaseTest {
    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env="+LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String theme = "enhancedweb";
    private static final String DISABLED_PAYMENT_MODE_ERROR_MSG = "{paymentMode} is not allowed for this transaction, kindly use some other payment mode";
    private static final String postConvFlag = "";
    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }
    String EMI_card_txn_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"f79ca04f-be5a-4245-8c23-62d532131600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000002621\",\n" +
            "        \"paytmUserId\": \"1000002621\",\n" +
            "        \"items\": null,\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 500.00,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "             \"transactionAmount\": 500,\n" +
            "            \"payMethod\": \"EMI\",\n" +
            "            \"issuingBank\": \"HDFC\",\n" +
            "            \"issuingNetworkCode\": \"VISA\",\n" +
            "            \"cardNo\":\"4718650100010336\",\n" +
            "            \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n";
    String promoEMIRequest= "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"f79ca04f-be5a-4245-8c23-62d532131600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000002621\",\n" +
            "        \"paytmUserId\": \"1000002621\",\n" +
            "        \"items\": null,\n" +
            "        \"offerDetails\": {\n" +
            "                   \n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2151610\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 500.00,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "             \"transactionAmount\": 500,\n" +
            "            \"payMethod\": \"EMI\",\n" +
            "            \"issuingBank\": \"HDFC\",\n" +
            "            \"issuingNetworkCode\": \"VISA\",\n" +
            "            \"cardNo\":\"4718650100010336\",\n" +
            "            \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n";

    public String getPaymentStatusBody="{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"signature\": \"\",\n" +
            "        \"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjYXJ0IiwibWlkIjoicWExMWlkMTYyMzE1NzU0OTE3MjgiLCJvcmRlcklkIjoicWExMV9kZWFsMTIifQ.8S-NCa6PpcTqBe-AQQFVINBOO2nVfyR1VePL9rwNGxE\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{qa12FU97229952596781}\",\n" +
            "        \"orderId\": \"{simply2507}\"\n" +
            "    }\n" +
            "}";


    @Test(description ="Test the success response of init txn api when inside simplifiedUnifiedOffers object only promo object provided in request")
    public void TestPFOForSimplifiedUnifiedOffer005() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }
    @Test(description ="Test the success response of init txn api when inside simplifiedUnifiedOffers\" +\n" +
            "            \" object only subvention object provided in request")
    public void TestPFOForSimplifiedUnifiedOffer006() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
    }

    @Test(description ="Test the success response of init txn api when inside simplifiedUnifiedOffers\" +\n" +
            "            \" object both subvention and promo details object provided in request")
    public void TestPFOForSimplifiedUnifiedOffer007() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }
    @Test(description ="Test the success response of init txn api when inside simplifiedUnifiedOffers\" +\n" +
            "            \" object both subvention and promo details object with Item details provided in request")
    public void TestPFOForSimplifiedUnifiedOffer008() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
        ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
    }
    @Test(description ="Test the success response of init txn when amount based promo " +
            "and amount based subvention is false and item object passed")
    public void TestPFOForSimplifiedUnifiedOffer009() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }

    @Test(description ="Test the success response of init txn when amount based promo " +
            "and amount based subvention is false and item object not passed")
    public void TestPFOForSimplifiedUnifiedOffer010() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","false","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","1100","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Test(description ="Test the success response of init txn when amount based promo is true and available promo is true " +
            "and amount based subvention is false and item object passed")
    public void TestPFOForSimplifiedUnifiedOffer011() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }
    @Test(description ="Test the success response of init txn when amount based promo is false and available promo is false " +
            "and amount based subvention is true and item object passed")
    public void TestPFOForSimplifiedUnifiedOffer012() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }

    @Test(description ="Test the success response of init txn when amount based promo is true and available promo is false " +
            "and amount based subvention is true and item object passed")
    public void TestPFOForSimplifiedUnifiedOffer013() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Test(description ="Test the success response of init txn when amount based promo is true and available promo is false " +
            "and amount based subvention is true and offerId provided in both promo and subvention details")
    public void TestPFOForSimplifiedUnifiedOffer014() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","2135498");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }
    @Test(description ="Test the success response of init txn when amount based promo is true and available promo is false " +
            "and amount based subvention is true and offerId provided in subvention details and promo code in promo details")
    public void TestPFOForSimplifiedUnifiedOffer015() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();

    }
    @Test(description ="Test the success response of init txn when promo code and Offerid is not provided and applyAvailablePromo is false and IsamountBasedOffer: false")
    public void TestPFOForSimplifiedUnifiedOffer016() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","false","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");


    }
    @Test(description ="Test the success response of init txn when amount and Offerid is not provided and IsamountBasedOffer: true ")
    public void TestPFOForSimplifiedUnifiedOffer017() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");


    }
    @Test(description ="Test the success response of init txn when amount and Offerid is not provided and IsamountBasedOffer:true and Item object passed")
    public void TestPFOForSimplifiedUnifiedOffer018() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","","","HDFC|3");
        SimplifiedUnifiedOffers.Items items= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(items);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS,simplifiedUnifiedOffers)
                .setTxnValue("1100")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
    }

    @Test(description ="Test the success response of init txn when Id is not provided in Item object")
    public void TestPFOForSimplifiedUnifiedOffer019() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","","","HDFC|3");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }

    @Test(description ="Test the success response of init txn when productId is not provided in Item object")
    public void TestPFOForSimplifiedUnifiedOffer020() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","","","HDFC|3");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }
    @Test(description ="Test the success response of init txn when brandId is not provided in Item object")
    public void TestPFOForSimplifiedUnifiedOffer021() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","","","HDFC|3");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1",""
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");

    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that PromoDetails and SubventionDetails object inside simplifiedUnifiedOffers should be returned in fpo response.As we are sending both object in INIT txn api")
    public void TestPFOForSimplifiedUnifiedOffer022() throws Exception {
        String isAmountBasedOffer="true";
        String isAmountBasedSubvention="true";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,"");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo")).isEqualTo(applyAvailablePromo);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.promoCode[0]")).isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo")).isEqualTo(validatePromoCode);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer")).isEqualTo(isAmountBasedOffer);
       Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention")).isEqualTo(isAmountBasedSubvention);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount")).isEqualTo(subventionAmount);

    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that only PromoDetailsobject inside simplifiedUnifiedOffers should be returned in fpo if we are sending only Promo details object in INIT txn api and Promo Code provided")
    public void TestPFOForSimplifiedUnifiedOffer023() throws Exception {
        String isAmountBasedOffer="true";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,"");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo")).isEqualTo(applyAvailablePromo);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.promoCode[0]")).isEqualTo(promoCode.get(0));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo")).isEqualTo(validatePromoCode);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer")).isEqualTo(isAmountBasedOffer);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.offerId")).isNullOrEmpty();
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that only PromoDetailsobject inside simplifiedUnifiedOffers should be returned in fpo if we are sending only Promo details object in INIT txn api and Offerid provided")
    public void TestPFOForSimplifiedUnifiedOffer024() throws Exception {
        String isAmountBasedOffer="true";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String offerId="2217113";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,offerId);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo")).isEqualTo(applyAvailablePromo);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.promoCode[0]")).isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo")).isEqualTo(validatePromoCode);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer")).isEqualTo(isAmountBasedOffer);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.offerId")).isEqualTo(offerId);
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that PromoDetailsobject and Items inside simplifiedUnifiedOffers should be returned in fpo if we are sending only Promo details with Items object in INIT txn api")
    public void TestPFOForSimplifiedUnifiedOffer025() throws Exception {
        String isAmountBasedOffer="false";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,"");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo")).isEqualTo(applyAvailablePromo);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.promoCode[0]")).isEqualTo(promoCode.get(0));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0]")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo")).isEqualTo(validatePromoCode);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer")).isEqualTo(isAmountBasedOffer);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.offerId")).isNullOrEmpty();
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that only SubventionDetails object inside simplifiedUnifiedOffers should be returned in fpo response.As we are sending only subvention object in INIT txn api")
    public void TestPFOForSimplifiedUnifiedOffer026() throws Exception {
        String isAmountBasedSubvention="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention")).isEqualTo(isAmountBasedSubvention);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount")).isEqualTo(subventionAmount);

    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that only SubventionDetails object inside simplifiedUnifiedOffers should be returned in fpo response.As we are sending only subvention object with OfferId in INIT txn api")
    public void TestPFOForSimplifiedUnifiedOffer027() throws Exception {
        String isAmountBasedSubvention="true";
        String offerId="2141488";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,offerId,"");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention")).isEqualTo(isAmountBasedSubvention);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount")).isEqualTo(subventionAmount);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.offerId")).isEqualTo(offerId);
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that only SubventionDetails and Items object inside simplifiedUnifiedOffers should be returned in fpo response.As we are sending subvention and Items Object in init api")
    public void TestPFOForSimplifiedUnifiedOffer028() throws Exception {
        String isAmountBasedSubvention="false";
        String offerId="2141488";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,offerId,"");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNullOrEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("subventionDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention")).isEqualTo(isAmountBasedSubvention);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount")).isEqualTo(subventionAmount);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.offerId")).isEqualTo(offerId);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0]")).isNotNull();
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that  SubventionDetails ,Promo Details and Items object inside simplifiedUnifiedOffers should be returned in fpo response.As we are sending subvention,Promo and  Items Object in init api")
    public void TestPFOForSimplifiedUnifiedOffer029() throws Exception {
        String isAmountBasedOffer="false";
        String isAmountBasedSubvention="false";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,null,"");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails")).as("PromoDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails")).as("subventionDetails object is not returned in response").
                isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0]")).isNotNull();
    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that emiBankDetails object should be returned when subvention is applied (subvention)")
    public void TestPFOForSimplifiedUnifiedOffer030() throws Exception {
        String isAmountBasedOffer="false";
        String isAmountBasedSubvention="false";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,"");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,"","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.emiBankDetails")).size().isEqualTo(2);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[1].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankName")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].subventionTypes")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails")).isNotNull();

    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that emiBankDetails object should be returned when subvention is applied (standard)")
    public void TestPFOForSimplifiedUnifiedOffer031() throws Exception {
        String isAmountBasedOffer="false";
        String isAmountBasedSubvention="false";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,null,"");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.emiBankDetails")).size().isEqualTo(2);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[1].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails")).isNotNull();

    }
    @Feature("PGP-50241")
    @Owner(KARMVIR)
    @Test(description="Validate that Unified Offers should be returned on the bases of catogrisation")
    public void TestPFOForSimplifiedUnifiedOffer032() throws Exception {
        String isAmountBasedOffer="false";
        String isAmountBasedSubvention="false";
        String applyAvailablePromo="true";
        String validatePromoCode="true";
        String subventionAmount="1100.0";
        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,applyAvailablePromo,validatePromoCode,isAmountBasedOffer,"");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails(isAmountBasedSubvention,subventionAmount,"","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.unifiedOffers")).size().isEqualTo(5);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle")).isEqualTo("EMI Linked Offers");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType")).isEqualTo("EMI_LINKED_OFFERS");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[1].categoryTitle")).isEqualTo("Netbanking Offers");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[1].categoryType")).isEqualTo("NETBANKING_OFFERS");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[2].categoryTitle")).isEqualTo("Card Linked Offers");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[2].categoryType")).isEqualTo("CARD_LINKED_OFFERS");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[3].categoryTitle")).isEqualTo("UPI Linked Offers");//UPI_LINKED_OFFERS
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[3].categoryType")).isEqualTo("UPI_LINKED_OFFERS");//UPI Linked Offers
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[4].categoryTitle")).isEqualTo("Paytm Featured Offers");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[4].categoryType")).isEqualTo("PAYTM_FEATURED_OFFERS");


    }
    @Test(description ="Test that UnifiedOffer object should return and PaymentOffers should not retrun" +
            " in FPO when fetchAllPaymentOffers is true and sending simplified unified offer object in INIT api")
    public void TestPFOForSimplifiedUnifiedOffer001() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true",null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("false").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[]")).
                as("Unified Offers are not returned in response").isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).as("Payment offers returned in response").
                doesNotContain("paymentOffers[]");
    }


    @Test(description ="Test that UnifiedOffer object should not returned and PaymentOffers should  return" +
            " in FPO when fetchAllPaymentOffers is true and not sending simplified unified offer object in INIT api")
    public void TestPFOForSimplifiedUnifiedOffer002() throws Exception {

        User user = userManager.getForRead(Label.PPBL);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("false").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).
                as("Unified Offers are returned in response").doesNotContain("unifiedOffers[]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOffers")).as("Payment offers are not returned in response").
                isNotNull();
    }
    @Test(description ="Test that UnifiedOffer object should not returned" +
            " in FPO when fetchAllPaymentOffers is false and  sending simplified unified offer object in INIT api")
    public void TestPFOForSimplifiedUnifiedOffer003() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("false").setApplyPaymentOffers("false").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).
                as("Unified Offers are returned in response").doesNotContain("unifiedOffers[]");
    }
    @Test(description ="Test that UnifiedOffer object should returned" +
            " in fpo for saved card when applyPaymentOffer is true , bulk Apply api should call ")
    public void TestPFOForSimplifiedUnifiedOffer004() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("1100")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0]")).
                as("Unified Offers are not returned in response").isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[]")).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].items[0].bankOfferDetails[0]." +
                "prePromoText")).isEqualTo("Offer Available");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of CC with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer01(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.PPBL);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer02(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer03(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI_DC with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer04(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.EMIDC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setChannelCode("ICICI")
                .setCardInfo(cardInfo)
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of CC with Cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer05(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2155487");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC with Cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer06(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO1236");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI with cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer07(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2155487");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI_DC with cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer08(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.EMIDC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2155487");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setCardInfo(cardInfo)
                .setChannelCode("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of CC for item based offers")
    public void TestSuccessTxnSimplifiedUnifiedOffer09(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC for item based offers")
    public void TestSuccessTxnSimplifiedUnifiedOffer10(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI for item based bank offers and zero cost subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer11(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("false","1100", "", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
        Assertions.assertThat(logs).contains("\"subventionType\":\"ZERO_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI for item based bank offers and Low cost subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer13(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.PPBL);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("false","1100", "", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|6")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
        Assertions.assertThat(logs).contains("\"subventionType\":\"LOW_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI_DC for Item based offers and subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer012(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.EMIDC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("false","1100", "", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setCardInfo(cardInfo)
                .setChannelCode("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
        Assertions.assertThat(logs).contains("\"subventionType\":\"ZERO_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI for zero cost amount based subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer14(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1100", "2141488", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"subventionType\":\"ZERO_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI Low cost amount based subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer15(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1100", "2141488", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|9")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"subventionType\":\"LOW_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI_DC amount based subvention")
    public void TestSuccessTxnSimplifiedUnifiedOffer015(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.EMIDC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1100", "2141488", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setCardInfo(cardInfo)
                .setChannelCode("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"subventionType\":\"LOW_COST\"");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of CC apply Available promo is true then best promo should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer16(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC apply Available promo is true then best promo should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer17(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI when applyAvailablePromo:true and AmountbasedSubvention:true and OfferId:null then best promo and best subvention should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer18(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1100", "", "");

        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of EMI_DC when applyAvailablePromo:true and AmountbasedSubvention:true and OfferId:null then best promo and best subvention should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer19(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.EMIDC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1100", "", "");

        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setChannelCode("ICICI")
                .setCardInfo(cardInfo)
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
        Assertions.assertThat(logs).contains("\"isSubventionOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the Failure txn of DC when ValidatePromo is true and invalid promo code is provided")
    public void TestSuccessTxnSimplifiedUnifiedOffer20(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("231234231");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateRespCode("810");
        responsePage.validateRespMsg("Payment failed due to a technical error. Please try after some time.");
        responsePage.validateCurrency("INR");
        responsePage.validateOrderId(initTxnDTO.orderFromBody());

    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC when ValidatePromo is false and invalid promo code is provided")
    public void TestSuccessTxnSimplifiedUnifiedOffer21(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("34234");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount("1100.00")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of NB with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer22(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of NB with cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer23(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of NB for item based offers")
    public void TestSuccessTxnSimplifiedUnifiedOffer24(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084",1100.00, "6224");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of NB apply Available promo is true then best promo should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer25(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of WALLET with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer26(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1200.00);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of WALLET with cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer27(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1200.00);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of WALLET for item based offers")
    public void TestSuccessTxnSimplifiedUnifiedOffer28(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1200.00);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002",1100.00, "10001");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of WALLET apply Available promo is true then best promo should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer29(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1200.00);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of UPI with discounted promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer30(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of UPI with cashback promo provided in Init api")
    public void TestSuccessTxnSimplifiedUnifiedOffer31(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100.00";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of UPI for item based offers")
    public void TestSuccessTxnSimplifiedUnifiedOffer32(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","false","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002",1100.00, "10001");
        ArrayList<SimplifiedUnifiedOffers.Items> ItemList= new ArrayList();
        ItemList.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,ItemList);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of UPI apply Available promo is true then best promo should apply")
    public void TestSuccessTxnSimplifiedUnifiedOffer33(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only amount based subvention")
    public void validateOnlySubventionAmountBased() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        String CustId = "REGSUOTMOCK0004";
        //String CustId = user.custId();

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"661.1");
        Assert.assertEquals(subventionAmount,"2000");
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertEquals(loanAmount,"1980.0");
        Assert.assertEquals(emiType,"brandEMI");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"1.0\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only promo based")
    public void validateOnlyPromoBased() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2151610");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"false");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"601.0");
        Assert.assertEquals(loanAmount,"1800.0");
        Assert.assertEquals(emiType,"bankEMI");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only amount based subvention and discount promo")
    public void validateOnlySubventionAmountBasedAndPromoDiscount() throws Exception {
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        String CustID = "REGSUOTMOCK0001";

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2151610");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");

        Assert.assertEquals(emiAmount,"594.99");
        Assert.assertEquals(subventionAmount,"1800");
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertEquals(loanAmount,"1782.0");
        Assert.assertEquals(emiType,"brandEMI");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"1.0\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only amount based subvention and cashback promo")
    public void validateOnlySubventionAmountBasedAndPromoCashback() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        String CustID = "REGSUOTMOCK0003";
        //String CustID = user.custId();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2193500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String additionalCashBack= PG2LogsValidationHelper.getKeyParameterValueFromLogs("additionalCashBack",detailExtendInfoLogs);
        String additionalCashBackAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("additionalCashBackAmount",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");

        Assert.assertEquals(emiAmount,"661.1");
        Assert.assertEquals(subventionAmount,"2000");
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertEquals(loanAmount,"1980.0");
        Assert.assertEquals(emiType,"brandEMI");
        Assert.assertEquals(additionalCashBack,"8.91");
        Assert.assertEquals(additionalCashBackAmount,"17820");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"1.0\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only promo chashback based")
    public void validateOnlyPromoCashbackBased() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2193500");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String additionalCashBack= PG2LogsValidationHelper.getKeyParameterValueFromLogs("additionalCashBack",detailExtendInfoLogs);
        String additionalCashBackAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("additionalCashBackAmount",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);

        Assert.assertEquals(isSubventionCreated,"false");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"667.78");
        Assert.assertEquals(loanAmount,"2000.0");
        Assert.assertEquals(emiType,"bankEMI");
        Assert.assertEquals(additionalCashBack,"9.0");
        Assert.assertEquals(additionalCashBackAmount,"18000");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo and extendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only item based subvention")
    public void validateOnlyItemBasedSubvention() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        String CustId = "REGSUOTMOCK0006";
        //String CustId = user.custId();

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","2000","2145679","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15035551617174","123","18084"
                ,2000.00,"6224");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);
        String modelName= PG2LogsValidationHelper.getKeyParameterValueFromLogs("modelName",detailExtendInfoLogs);
        String brandId= PG2LogsValidationHelper.getKeyParameterValueFromLogs("brandId",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"true");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"666.67");
        Assert.assertEquals(subventionAmount,"4898");
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertEquals(loanAmount,"1951.02");
        Assert.assertEquals(emiType,"retailerEMI");
        Assert.assertEquals(modelName,"123");
        Assert.assertEquals(brandId,"18084");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"2.44\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo and extendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only item based subvention and promo discount")
    public void validateOnlyItemBasedSubventionAndPromoDiscount() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();

        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");

        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,2000.00,"10001");

        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);
        String modelName= PG2LogsValidationHelper.getKeyParameterValueFromLogs("modelName",detailExtendInfoLogs);
        String brandId= PG2LogsValidationHelper.getKeyParameterValueFromLogs("brandId",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"true");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"666.67");
        Assert.assertEquals(subventionAmount,"4898");
        Assert.assertEquals(subventionType,"ZERO_COST");
        Assert.assertEquals(loanAmount,"1951.02");
        Assert.assertEquals(emiType,"SUBVENTION");
        Assert.assertEquals(modelName,"1");
        Assert.assertEquals(brandId,"10002");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"2.44\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }



    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_ORDER_MODIFY in theia facade for only amount based subvention")
    public void validateOnlySubventionAmountBasedcotp() throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        String mid=Constants.MerchantType.EMI_DC_CC.getId().toString();
        String CustID = "REGSUOTMOCK0002";

        //String CustID = user.custId();
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2165743","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.EMI_DC_CC)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"666.67");
        Assert.assertEquals(subventionAmount,"665");
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertEquals(loanAmount,"1993.35");
        Assert.assertEquals(emiType,"brandEMI");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"0.33\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_CREATE_ORDER_AND_PAY in theia facade for only promo based")
    public void validateOnlyPromoBasedInCotp() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.EMI_DC_CC.getId().toString();
        String CustId = "REGSUOTMOCK0005";
        //String CustId = user.custId();

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2236834");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, Constants.MerchantType.EMI_DC_CC)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"false");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");
        Assert.assertEquals(emiAmount,"602.0");
        Assert.assertEquals(loanAmount,"1800.0");
        Assert.assertEquals(emiType,"bankEMI");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description ="validating detailExtendInfo in ACQUIRING_ORDER_MODIFY in theia facade for only amount based subvention and discount promo")
    public void validateOnlySubventionAmountBasedAndPromoDiscountInCotp() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.EMI_DC_CC.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();

        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2236834");

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2165743","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.EMI_DC_CC)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String originalAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("originalAmount",extendInfoLogs);


        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.0\"");

        Assert.assertEquals(emiAmount,"600.0");
        Assert.assertEquals(subventionAmount,"598");
        Assert.assertEquals(subventionType,"ZERO_COST");
        Assert.assertEquals(loanAmount,"1794.02");
        Assert.assertEquals(emiType,"SUBVENTION");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"0.33\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="Test that EMISubventioninfo should be sent in COP request for subvention txn",enabled = false)
    public void validateEMISubventioninfoInCop() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);

        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(planId,"306133932066376709");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assert.assertEquals(tenure,"3");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="validating subvention detail and promocheckout data in cop logs",enabled = false)
    public void validateOnlySubventionANDPromo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);

        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO000123");
        Assert.assertEquals(promotext,"₹400.0 discount applied successfully.");
        Assert.assertEquals(planId,"306133932066376709");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="validating  promocheckout data in cop logs",enabled = false)
    public void validateOnlyPromo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();

        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);

        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);

        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO000123");
        Assert.assertEquals(promotext,"₹400.0 discount applied successfully.");
        Assert.assertEquals(planId,"306133932066376709");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assert.assertEquals(tenure,"3");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="validating subvention detail and promocheckout data in cop logs",enabled = false)
    public void validateOnlySubventionANDPromoEMI_DC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4572741654006328|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);

        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);
        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO000123");
        Assert.assertEquals(promotext,"₹400.0 discount applied successfully.");
        Assert.assertEquals(planId,"306133932066376704");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assert.assertEquals(tenure,"3");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="Test that EMISubventioninfo should be sent in COP request for subvention txn for ENI DC Txn",enabled = false)
    public void validateEMISubventioninfoInCopEMI_DC() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4572741654006328|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);

        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(planId,"306133932066376704");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assert.assertEquals(tenure,"3");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="validating  promocheckout data in cop logs using EMI DC",enabled = false)
    public void validateOnlyPromoEMI_DC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();

        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4572741654006328|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);

        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);
        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO000123");
        Assert.assertEquals(promotext,"₹400.0 discount applied successfully.");
        Assert.assertEquals(planId,"306133932066376704");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assert.assertEquals(tenure,"3");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":534.22");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"eligibleAmt\":1600.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"payableAmount\":\"200000\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description ="Test that payableAmount should be sent in COP in extendInfo request for subvention txn for ENI DC Txn")
    public void validateEMISubventioninfoInCopEMI_DCForPayableAmount() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4572741654006328|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        String payableAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("payableAmount",extendInfoLogs);
        Assert.assertEquals(payableAmount,"200000");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description ="Test that EMISubventioninfo should be there in getPaymentStatus api respose for amount based")
    public void validateEMISubventioninfoInGetPaymentStatus() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122027")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));
    }

    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description ="Test that EMISubventioninfo and promoCheckoutData should be there in getPaymentStatus api respose for amount based+Promo")
    public void validateEMISubventioninfoInWithPromo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));
        String planId=GetPaymentStatus.getParameterValue(res.getString("body.emiSubventionInfo"),"planId");
        Assert.assertNotNull(res.getString("body.paymentPromoCheckoutData"));

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description ="Test that promoCheckoutData should be there in getPaymentStatus api respose for only Promo")
    public void validateWithOnlyPromo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217113");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        String planId=GetPaymentStatus.getParameterValue(res.getString("body.emiSubventionInfo"),"planId");
        Assert.assertNotNull(res.getString("body.paymentPromoCheckoutData"));

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description ="Test that EMISubventioninfo and offercheckout should be sent in PAY request for item based subvention ")
    public void validateEMISubventionInfoOnlyItem() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","2000","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,2000.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);

        String planId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        Assert.assertEquals(status,"1");
        Assert.assertNotNull(promocode);
        Assert.assertEquals(promotext,"₹200.0 cashback applied successfully.");
        Assert.assertEquals(planId,"307312565796316169");
        Assert.assertEquals(subventionAmount,"2000.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"gratificationCashback\":200.0");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"gratificationType\":\"CASHBACK\"");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"subventionAmount\":\"2000.0\"");
        Assertions.assertThat(emiSubventionInfoLogs).contains("brandId\":\"10002\"");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"emi\":683.4");

    }
//    Duplicate Case - Behaviour Verified in other cases
//    @Owner("Nirottam")
//    @Feature("PGP-50287")
//    @Test(description ="Test that EMISubventioninfo and offercheckout should be in getPaymentStatus api for item based subvention",enabled = false)
    public void validateGetPaymentStatusForItemOnly() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String mid=Constants.MerchantType.SIMPLIFIED_OFFERS.getId().toString();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("false","2000",null,"");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084"
                ,2000.00,"6224");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), Constants.MerchantType.SIMPLIFIED_OFFERS)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        String txnToken= response.getString("body.txnToken");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.paymentPromoCheckoutData"));
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));

    }

    @Owner("AKSHAT_NAYAK")
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify SubventionType as DISCOUNT and emiType as brandEMI in COP request")
    public void TestSimplifiedUnifiedOfferSubventionTypeDISCOUNT(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EmiInfo_COP;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2192568","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setCardInfo("|4761360075860428|545|122027")
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"brandEMI\"");
    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with postpaid for simplified flow for amount based.")
    public void TestE2ESuccessTxnWithPostpaid_01(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with postpaid for simplified flow for item based.")
    public void TestE2ESuccessTxnWithPostpaid_02(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","18084"
                ,1100.00,"6224");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with ppbl for simplified flow for amount based.")
    public void TestE2ESuccessTxnWithPPBL_01(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("PPBL")
                .setAuthMode("3D")
                .setMpin("1234")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PPBL\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Test e2e success txn with ppbl for simplified flow for item based.")
    public void TestE2ESuccessTxnWithPPBL_02(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","false","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers.Items item= new SimplifiedUnifiedOffers.Items("15036688","1","10002"
                ,1100.00,"10001");
        ArrayList<SimplifiedUnifiedOffers.Items> listItem= new ArrayList<>();
        listItem.add(item);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails,subventionDetails,listItem);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("PPBL")
                .setAuthMode("3D")
                .setMpin("1234")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PPBL\"");

    }

    @Owner("AKSHAT_NAYAK")
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify emiType as bankdEMI in COP request")
    public void TestSimplifiedUnifiedOfferEMITypeBankEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EmiInfo_COP;
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","2193630");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"emiType\":\"bankEMI\"");
    }

    @Owner("AKSHAT_NAYAK")
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify SubventionType as DISCOUNT and emiType as retailerEMI in COP request")
    public void TestSimplifiedUnifiedOfferSubventionTypeDISCOUNTemiTypeRetailerEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EmiInfo_COP;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2192568","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|6")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"retailerEMI\"");
    }
    @Owner("AKSHAT_NAYAK")
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify SubventionType as CASHBACK and emiType as brandEMI in COP request")
    public void TestSimplifiedUnifiedOfferSubventionTypeCASHBACKemiTypeBrandEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(user.custId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|6")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"subventionType\":\"CASHBACK\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"brandEMI\"");
    }

    @Owner(KARMVIR)
    @Feature("PGP-51181")
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of CC with discounted promo provided in Init api non logged in flow")
    public void TestSuccessTxnSimplifiedUnifiedOfferCCNon_loggedIn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Feature("PGP-51181")
    @Parameters("isNativePlus")
    @Test(description ="Test the success txn of DC with discounted promo provided in Init api for Non loggedIn flow")
    public void TestSuccessTxnSimplifiedUnifiedOfferDCNon_loggedIn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when partial subvention and Bo amount provided")
    public void TestE2ESuccessResponseWhenPartialSubventionAndBoAmountprovided(@Optional("true") boolean isNativePlus) throws Exception{
        String txnAmount="1100";
        User user = userManager.getForRead(Label.BASIC);
        String CustId = "REGSUOTMOCK1004";
        //String CustId = user.custId();
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("NETBANKINGSUBV");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","500","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setCustId(CustId)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"emiTransactionAmount\":50000");
        Assertions.assertThat(logs).contains("\"boTransactionAmount\":50000");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial subvention amount provided")
    public void TestE2ESuccessResponseWhenOnlyPartialSubventionProvided(@Optional("true") boolean isNativePlus) throws Exception{
        String txnAmount="1100";
        User user = userManager.getForRead(Label.PPBL);
        String CustId = "REGSUOTMOCK1003";
        //String CustId = user.custId();
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("NETBANKINGSUBV");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","500","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setCustId(CustId)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"emiTransactionAmount\":50000");
    //    Assertions.assertThat(logs).contains("\"boTransactionAmount\":50000");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial bank offer amount provided")
    public void TestE2ESuccessResponseWhenOnlyPartialBankOfferAmountProvided(@Optional("true") boolean isNativePlus) throws Exception{
        String txnAmount="1100";
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        String CustId = "REGSUOTMOCK1002";
        //String CustId = user.custId();
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("NETBANKINGSUBV");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","2141488","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setCustId(CustId)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
   //     Assertions.assertThat(logs).contains("\"emiTransactionAmount\":50000");
        Assertions.assertThat(logs).contains("\"boTransactionAmount\":50000");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial bank offer amount provided for Net Banking paymode")
    public void TestE2ESuccessResponseWhenOnlyPartialBankOfferAmountProvidedNB(@Optional("true") boolean isNativePlus) throws Exception{
        String txnAmount="1000";
        User user = userManager.getForRead(Label.WALLETBALANCE);
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        String CustId = "REGSUOTMOCK1001";
        //String CustId = user.custId();

        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("NETBANKINGSUBV");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        //     Assertions.assertThat(logs).contains("\"emiTransactionAmount\":50000");
        Assertions.assertThat(logs).contains("\"boTransactionAmount\":50000");
    }

    @Feature("PGP-56756")
    @Owner(VIDHI)
    @Parameters("isNativePlus")
    @Test(description = "Verify the coft response when Pref ENABLE_COFT_PROMO_PAR_CONFIG is OFF in promo/subvention flow")
    public void verifyCoftResponsePrefON(@Optional("true") boolean isNativePlus) throws Exception {
        String txnAmount="1000";
        User user = userManager.getForRead(Label.WALLETBALANCE);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(null,"true","false","true","",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000",null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO=new InitTxnDTO.
                Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|4718650102608848|111|012027")
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"COFT");
        Assertions.assertThat(logs).contains("Error from card network");
    }
    @Feature("PGP-56756")
    @Owner(VIDHI)
    @Parameters("isNativePlus")
    @Test(description = "Verify the coft response when Pref ENABLE_COFT_PROMO_PAR_CONFIG is OFF in promo/subvention flow")
    public void verifyCoftResponsePrefOFF(@Optional("true") boolean isNativePlus) throws Exception {
        String txnAmount="1000";
        User user = userManager.getForRead(Label.WALLETBALANCE);
        Constants.MerchantType merchantType= Constants.MerchantType.EmiInfo_COP;
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(null,"true","false","true","",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000",null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO=new InitTxnDTO.
                Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|4718650102608848|111|012027")
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"COFT");
        Assertions.assertThat(logs).contains("PAR not found");
    }
}
