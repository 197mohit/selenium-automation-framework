package scripts.supercashtests;

import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.supercash.ApplySuperCash;
import com.paytm.api.CreateToken;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.apphelpers.supercashhelpers.superCashHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.SuperCashOffer;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.dto.processTransactionV1.UpiLiteRequestData;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.elasticsearch.common.recycler.Recycler;
import org.testng.annotations.Test;
import com.paytm.apphelpers.supercashhelpers.superCashHelper;

import static com.paytm.apphelpers.NativeHelpers.submitProcessTxnResponseFromReq;
import static io.restassured.RestAssured.given;

public class ApplySuperCashTests extends PGPBaseTest {

   // String orderID = CommonHelpers.generateOrderId();
    private final String creditblock ="{\"defaultDebit\":{\"accRefId\":\"328175949\",\"accountType\":\"SAVINGS\",\"bank\":\"Kotak Mahindra Bank\",\"logo-url\":\"https:static.paytmbank.comupiimagesbank-logo607420.png\",\"bankMetaData\":{\"bankHealth\":{\"category\":\"GREEN\",\"displayMsg\":\"\"},\"perTxnLimit\":\"200000\"},\"branchAddress\":\"PATNA BRANCH\",\"credsAllowed\":[{\"CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType\":\"MPIN\",\"CredsAllowedType\":\"PIN\",\"dLength\":\"6\"}],\"displayAccountNo\":\"-2101\",\"ifsc\":\"KKBK0005651\",\"maskedAccountNumber\":\"XXXXXX2101\",\"mpinSet\":\"Y\",\"name\":\"ABDULLAH SHAHID HASSAN\",\"pgBankCode\":\"NKMB\",\"priority\":7.0,\"txnAllowed\":\"ALL\"},\"primary\":true,\"name\":\"7543981502@paytm\"}";
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
    SuperCashOffer superCashOffer=new SuperCashOffer();

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for Postpaid and balance when sending both paymode  and SSO token in request")
    public void getOfferfor_PPI_Postpaid_Onus_with_SSO() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String[] paymode = {superCashHelper.balance, superCashHelper.postpid};

        ApplySuperCash applySuperCash = new ApplySuperCash();
        JsonPath applysupercashResponse = given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(), user.ssoToken().toString(), "SSO", user.custId(), "100", source, paymode, "")).post().jsonPath();
        System.out.println("Response: \n" + applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.postpid);
    }


    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for balance when sending only balance and SSO token in request")
    public void getOfferfor_PPI_Onus_with_SSO() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String []paymode={superCashHelper.balance,""};

       ApplySuperCash applySuperCash=new ApplySuperCash();
       JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),user.ssoToken().toString(),"SSO",user.custId(),"100",source,paymode,"")).post().jsonPath();
       System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance);

    }

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for balance when sending only balance and TXN token in request")
    public void getOfferfor_PPI_Onus_with_TXN_TOKEN() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String []paymode={superCashHelper.balance,""};

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), WalletOfferOnus)
                .setTxnValue(String.valueOf(100))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),txnToken,"TXN_TOKEN",user.custId(),"100",source,paymode,"")).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance); }

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for Postpaid and balance when sending both paymode and TXN token in request")
    public void getOfferfor_PPI_POSTPAID_Onus_with_TXN_TOKEN() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());

        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), WalletOfferOnus)
                .setTxnValue(String.valueOf(100))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),txnToken,"TXN_TOKEN",user.custId(),"100",source,paymode,"")).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.postpid);
    }

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for postpaid and balance when sending both paymode and ACCESS token in request")
    public void getOfferfor_PPI_POSTPAID_Onus_with_ACCESS_TOKEN() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String []paymode={superCashHelper.balance, superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        JsonPath jsonpath=new CreateToken(WalletOfferOnus,referenceId).execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");

        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),AccessToken,"ACCESS",user.custId(),"100",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse,superCashHelper.postpid);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.postpid);
    }


    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer for postpaid when sending only postpaid and ACCESS token in request")
    public void getOfferfor_POSTPAID_Onus_with_ACCESS_TOKEN() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String []paymode={superCashHelper.postpid,""};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        JsonPath jsonpath=new CreateToken(WalletOfferOnus,referenceId).execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),AccessToken,"ACCESS",user.custId(),"100",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Display_Text(applysupercashResponse,superCashHelper.postpid);
    }

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate offer when source is null in request")
    public void getOffer_with_ACCESS_TOKEN() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        JsonPath jsonpath=new CreateToken(WalletOfferOnus,referenceId).execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),AccessToken,"ACCESS",user.custId(),"100","",paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());

        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Campaign(applysupercashResponse, superCashHelper.postpid);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.balance);
        superCashHelper.validate_Display_Text(applysupercashResponse, superCashHelper.postpid);
    }

    @Owner("Manish")
    @Feature("PGP-40202")
    @Test(description = "Validate response when offer amount > txnAmount")
    public void getOffer_check_amount() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.WALLET_OFFER_ONUS;
        String source =superCashHelper.getSource(WalletOfferOnus.getId());
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        JsonPath jsonpath=new CreateToken(WalletOfferOnus,referenceId).execute().jsonPath();
        String AccessToken=jsonpath.getString("body.accessToken");
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(WalletOfferOnus.getId(),AccessToken,"ACCESS",user.custId(),"1","",paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.validate_Failed_Response(applysupercashResponse);
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with onus MID & source as ORDER")
    public void onusMerchantSourceOrderPromoNotNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_ONUS;
        String source = "ORDER";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"ORDER\"";
        String promoAvailable = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).contains(promoAvailable);
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with onus MID & source as PG")
    public void onusMerchantSourcePGPromoNotNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_ONUS;
        String source = "PG";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"ORDER\"";
        String promoAvailable = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).contains(promoAvailable);
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with offus MID & source as PG")
    public void offusMerchantSourcePGPromoNotNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_OFFUS;
        String source = "PG";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"PG\"";
        String noPromo = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).doesNotContain(noPromo);
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with offus MID & source as PG")
    public void offusMerchantSourcePGPromoNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_OFFUS;
        String source = "PG";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpecWithoutPromo(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"PG\"";
        String noPromo = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).doesNotContain(noPromo);
    }


    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with offus MID & source as ORDER")
    public void offusMerchantSourceOrderPromoNotNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_OFFUS;
        String source = "ORDER";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpec(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"PG\"";
        String noPromo = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).doesNotContain(noPromo);
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-45282")
    @Test(description = "Verify the request sent to supercash when theia's supercash API is hit with offus MID & source as ORDER")
    public void offusMerchantSourceOrderPromoNull() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUPERCASH_OFFUS;
        String source = "ORDER";
        String []paymode={superCashHelper.balance,superCashHelper.postpid};
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        ApplySuperCash applySuperCash=new ApplySuperCash();
        JsonPath applysupercashResponse=given().spec(applySuperCash.reqSpecWithoutPromo(merchant.getId(),user.ssoToken(),"SSO",user.custId(),"120",source,paymode,referenceId)).post().jsonPath();
        System.out.println("Response: \n"+applysupercashResponse.prettify());
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.balance);
        superCashHelper.Validate_Paymode(applysupercashResponse, superCashHelper.postpid);
        String supercashRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,merchant.getId(),"SUPERCASH_SERVICE");
        String sourcePassed = "\"TYPE\" : \"REQUEST\", \"REQUEST\" : HttpRequestPayload [entity={\"source\":\"PG\"";
        String noPromo = "\"promocontext\":{";
        Assertions.assertThat(supercashRequest).contains(sourcePassed);
        Assertions.assertThat(supercashRequest).doesNotContain(noPromo);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify supercash offers in FPO V1")
    public void superCashOffersInFPO_V1() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersSuperCash;
        String orderID = CommonHelpers.generateOrderId();
        String []validatePaymode={superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        //String []validatePaymode={superCashHelper.balance,superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        //WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        for (String paymode1:validatePaymode)
            {
                superCashHelper.Validate_PaymodeIn_FPO(fetchPaymentOptionsJson,paymode1);
                superCashHelper.validate_CampaignIn_FPO(fetchPaymentOptionsJson,paymode1);
            }
        String scCampignID=superCashHelper.getCampaignID_FPO(fetchPaymentOptionsJson,superCashHelper.balance);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode(superCashHelper.balance)
                .setExtendInfoOrderAlreadyCreated(false)
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .setChannelId("WEB")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        //Verify sending campaignId ID in COAP request
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\""+scCampignID);
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,initTxnDTO.getBody().getOrderId(),"PAYMENT_PG");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify supercash offers in FPO V2")
    public void superCashOffersInFPO_V2() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersSuperCash;
        String []validatePaymode={superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        //String []validatePaymode={superCashHelper.balance,superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        String orderID = CommonHelpers.generateOrderId();
        //WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();


        for (String paymode1:validatePaymode)
            {
                superCashHelper.Validate_PaymodeIn_FPO(fetchPaymentOptionsJson,paymode1);
                superCashHelper.validate_CampaignIn_FPO(fetchPaymentOptionsJson,paymode1);
            }
        String scCampignID=superCashHelper.getCampaignID_FPO(fetchPaymentOptionsJson,superCashHelper.balance);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode(superCashHelper.balance)
                .setExtendInfoOrderAlreadyCreated(false)
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .setChannelId("WEB")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        //Verify sending campaignId ID in COAP request
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\""+scCampignID);
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,initTxnDTO.getBody().getOrderId(),"PaymentPromo");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify supercash offers in FPO V5")
    public void superCashOffersInFPO_V5() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersSuperCash;
        //WalletHelpers.modifyBalance(user,10.0);
        //String []validatePaymode={superCashHelper.balance,superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        String []validatePaymode={superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI_LITE,superCashHelper.UPI};
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        for (String paymode1:validatePaymode)
            {
                superCashHelper.Validate_PaymodeIn_FPO(fetchPaymentOptionsJson,paymode1);
                superCashHelper.validate_CampaignIn_FPO(fetchPaymentOptionsJson,paymode1);
            }
        String scCampignID=superCashHelper.getCampaignID_FPO(fetchPaymentOptionsJson,superCashHelper.UPI);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),txnToken,initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo1)
                .setCreditBlock(creditblock)
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        //Verify sending campaignId ID in COAP request
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\""+scCampignID);
        Assertions.assertThat(logs).contains("upiModeSubType\":\"UPI_SAVINGS");
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,initTxnDTO.getBody().getOrderId(),"PaymentPromo");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify supercash offers in fetchQRPaymentDetails V2")
    public void superCashOffersIn_FQR_V2() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersSuperCash;
        String []validatePaymode={superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI,superCashHelper.UPI_LITE,superCashHelper.UPI_CC};
        //String []validatePaymode={superCashHelper.balance,superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI,superCashHelper.UPI_LITE,superCashHelper.UPI_CC};
       // WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb").setTXN_AMOUNT("2").build();
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");

        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId= generateResponseJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fqrpaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken()).setIsLiteEligible(true)
                .build();

        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(fqrpaymentDetailsDTO);
        fqrDetail.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        fqrDetail.setContext("body.fetchPaytmInstrumentsBalance", "False");
        fqrDetail.setContext("head.version", "v2");

        JsonPath fqrResponse = fqrDetail.execute().jsonPath();
        for (String paymode1:validatePaymode)
            {
                superCashHelper.Validate_PaymodeIn_FQR(fqrResponse,paymode1);
                superCashHelper.validate_CampaignIn_FQR(fqrResponse,paymode1);
            }

        orderDTO.setOrderId(fqrResponse.getString("body.paymentOptions.orderId"));
        String scCampignID=superCashHelper.getCampaignID_FQR(fqrResponse,superCashHelper.UPI);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "2")
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo1)
                .setCreditBlock(creditblock)
                .setPaymentFlow("NONE")
                .setUpiAccRefId("224646")
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "UPI", "PPBEX");

        //Verify sending campaignId ID in COAP request
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\""+scCampignID);
        Assertions.assertThat(logs).contains("upiModeSubType\":\"UPI_SAVINGS");
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID(),"PAYMENT_PG");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);
        //Assertions.assertThat(NQHLog).contains("upiModeSubType\":\"UPI_SAVINGS");

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify supercash offers in fetchQRPaymentDetails V1")
    public void superCashOffersIn_FQR_V1() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersSuperCash;
        String []validatePaymode={superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI};
        //String []validatePaymode={superCashHelper.balance,superCashHelper.postpid,superCashHelper.PPBL,superCashHelper.UPI_CC,superCashHelper.UPI};
        //WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb").setTXN_AMOUNT("2").build();
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");

        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId= generateResponseJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fqrpaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken()).setVersion("v1")
                .build();

        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(fqrpaymentDetailsDTO);
        fqrDetail.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS);
        fqrDetail.setContext("body.fetchPaytmInstrumentsBalance", "False");
        fqrDetail.setContext("head.version", "v1");

        JsonPath fqrResponse = fqrDetail.execute().jsonPath();

        for (String paymode1:validatePaymode)
            {
                superCashHelper.Validate_PaymodeIn_FQR(fqrResponse,paymode1);
                superCashHelper.validate_CampaignIn_FQR(fqrResponse,paymode1);
            }

        orderDTO.setOrderId(fqrResponse.getString("body.paymentOptions.orderId"));
        String scCampignID=superCashHelper.getCampaignID_FQR(fqrResponse,superCashHelper.balance);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "2")
                .setPaymentMode(superCashHelper.balance)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .setPaymentFlow("NONE")
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .setChannelId("WEB")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");

        //Verify sending campaignId ID in COAP request
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\""+scCampignID);
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID(),"PAYMENT_PG");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify UPI offer is not present in FQR response and not sending upiModeSubType in COAP request when FF4j theia.upi.supercash is disabled")
    public void UPI_OfferDisabled_In_FQRV2() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.GlobalOffersDisabledForUPI;
        String[] validatePaymode = {superCashHelper.postpid, superCashHelper.PPBL, superCashHelper.UPI_LITE, superCashHelper.UPI_CC};
        //String[] validatePaymode = {superCashHelper.balance, superCashHelper.postpid, superCashHelper.PPBL, superCashHelper.UPI_LITE, superCashHelper.UPI_CC};
        //WalletHelpers.modifyBalance(user, 10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb").setTXN_AMOUNT("2").build();
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");

        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fqrpaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken()).setIsLiteEligible(true)
                .build();

        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(fqrpaymentDetailsDTO);
        fqrDetail.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        fqrDetail.setContext("body.fetchPaytmInstrumentsBalance", "False");
        fqrDetail.setContext("head.version", "v2");

        JsonPath fqrResponse = fqrDetail.execute().jsonPath();
        for (String paymode1 : validatePaymode) {
            superCashHelper.Validate_PaymodeIn_FQR(fqrResponse, paymode1);
            superCashHelper.validate_CampaignIn_FQR(fqrResponse, paymode1);
        }
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + superCashHelper.UPI + "'}.offers"))
                .isEqualTo(null);

        orderDTO.setOrderId(fqrResponse.getString("body.paymentOptions.orderId"));
        String scCampignID = "";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "2")
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo1)
                .setCreditBlock(creditblock)
                .setPaymentFlow("NONE")
                .setUpiAccRefId("224646")
                .setSuperCashOffer(superCashOffer.setcampaignId(scCampignID))
                .build();


        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "UPI", "PPBEX");

        //Verify sending campaignId ID in COAP request
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("scCampaignId\":\"" + scCampignID);
        Assertions.assertThat(logs).doesNotContain("upiModeSubType\":\"UPI_SAVINGS");
        //.contains("upiModeSubType\":\"UPI_SAVINGS");
        //String NQHLog= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,orderDTO.getORDER_ID(),"PAYMENT_PG");
        //Assertions.assertThat(NQHLog).contains("scCampaignId\":\""+scCampignID);
        //Assertions.assertThat(NQHLog).contains("upiModeSubType\":\"UPI_SAVINGS");

    }
}

