package scripts.mgv;

import com.paytm.CreateToken;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.GiftVoucher;
import com.paytm.utils.merchant.intersections.MerchantUserIntersection;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV1Test;
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test;

import java.util.Date;
import java.util.UUID;

import static com.paytm.appconstants.Constants.MerchantType.MGV_ADDNPAY;
import static com.paytm.appconstants.Constants.MerchantType.MGV_HYBRID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Rahul Kumar")
public class TestNativeMGVFlow extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    public String Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson.param("paymentMode", "GIFT_VOUCHER").get("body.merchantPayOption.paymentModes.find { it.paymentMode == paymentMode}.payChannelOptions.find().templateId");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native MGV transaction for Hybrid type of merchant")
    public void TC_PT_MGV_001(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String tempID = Validate_FetchPayInstrument(txnToken, initTxnDTO, "GIFT_VOUCHER", "false");
        OrderDTO orderDTO = new OrderFactory.MGV_Native(Constants.MerchantType.MGV_HYBRID, initTxnDTO.orderFromBody(), txnToken, "GIFT_VOUCHER", tempID).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native MGV transaction for AddNPAy type of merchant")
    public void TC_PT_MGV_002(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_ADDNPAY).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String tempID = Validate_FetchPayInstrument(txnToken, initTxnDTO, "GIFT_VOUCHER", "false");
        OrderDTO orderDTO = new OrderFactory.MGV_Native(Constants.MerchantType.MGV_ADDNPAY, initTxnDTO.orderFromBody(), txnToken, "GIFT_VOUCHER", tempID).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Failure Native MGV transaction for ADDNPAY type of merchant")
    public void TC_PT_MGV_003(@Optional("false") Boolean isNativePlus) throws Exception {


        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_ADDNPAY).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String tempID = Validate_FetchPayInstrument(txnToken, initTxnDTO, "GIFT_VOUCHER", "false");
        OrderDTO orderDTO = new OrderFactory.MGV_Native(Constants.MerchantType.MGV_ADDNPAY, initTxnDTO.orderFromBody(), txnToken, "GIFT_VOUCHER", tempID).setTXN_AMOUNT(String.valueOf(Balance)).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Failure Native MGV transaction for HYBRID type of merchant")
    public void TC_PT_MGV_004(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String tempID = Validate_FetchPayInstrument(txnToken, initTxnDTO, "GIFT_VOUCHER", "false");
        OrderDTO orderDTO = new OrderFactory.MGV_Native(Constants.MerchantType.MGV_HYBRID, initTxnDTO.orderFromBody(), txnToken, "GIFT_VOUCHER", tempID).setTXN_AMOUNT(String.valueOf(Balance)).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    // User has never purchased MGV


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v1 response")
    public void TC_PT_MGV_005() throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJson = fetchPaymentOption.execute();

        fetchPaymentOptionsJson.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v2 response")
    public void TC_PT_MGV_005_V2() throws Exception {


        User user = userManager.getForWrite(Label.NOMGV);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        Response fetchPaymentOptionsJsonv2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO)
                .execute();

        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v2 SSO response")
    public void TC_PT_MGV_006() throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2 = new SSOTokenFetchPayOptionsV2Test();

        Response fetchPaymentOptionsJsonV2 = given(fetchPayOptionsV2.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));

    }


    @Test(description = "Verify When User already have Voucher Flag is True in Access Token fetch Payment Option V1")
    public void TC_PT_MGV_007() throws Exception {

        User user = userManager.getForWrite(Label.NOMGV);
        String refId = UUID.randomUUID().toString().substring(0, 18);
        Constants.MerchantType merchantType = MGV_HYBRID;
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));
    }


    @Test(description = "Verify When User already have Voucher Flag is True in Access Token fetch Payment Option V2")
    public void TC_PT_MGV_008() throws Exception {

        User user = userManager.getForWrite(Label.NOMGV);

        String refId = UUID.randomUUID().toString().substring(0, 18);

        Constants.MerchantType merchantType = MGV_HYBRID;

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


    @Test(description = "Verify When User already have Voucher Flag is True in checksum fetch Payment Option v1")
    public void TC_PT_MGV_009() throws Exception {

        User user = userManager.getForWrite(Label.NOMGV);

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}", merchantType.getId())
                .replace("{REFID}", refId).replace("{SSOID}", userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(), body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));

    }

    @Test(description = "Verify When User already have Voucher Flag is True in checksum fetch Payment Option v2")
    public void TC_PT_MGV_010() throws Exception {

        User user = userManager.getForWrite(Label.NOMGV);

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}", merchantType.getId())
                .replace("{REFID}", refId).replace("{SSOID}", userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(), body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));
    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v1 SSO response")
    public void TC_PT_MGV_011() throws Exception {


        User user = userManager.getForWrite(Label.NOMGV);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV1Test fetchPayOptionsV1 = new SSOTokenFetchPayOptionsV1Test();

        Response fetchPaymentOptionsJsonV1 = given(fetchPayOptionsV1.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV1.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));
    }


    //User Already have MGV

    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v1 response")
    public void TC_PT_MGV_012() throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJson = fetchPaymentOption.execute();

        fetchPaymentOptionsJson.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        equalTo(true));


    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v2 response")
    public void TC_PT_MGV_012_V2() throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpov2.execute();


        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        equalTo(true));


    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag is True in FPO v2 SSO response")
    public void TC_PT_MGV_013() throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2 = new SSOTokenFetchPayOptionsV2Test();

        Response fetchPaymentOptionsJsonV2 = given(fetchPayOptionsV2.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


    @Test(description = "Verify When User already have Voucher isNewFlag will not come  in Access Token fetch Payment Option V1")
    public void TC_PT_MGV_014() throws Exception {

        User user = userManager.getForWrite(Label.MGV);

        String refId = UUID.randomUUID().toString().substring(0, 18);
        Constants.MerchantType merchantType = MGV_HYBRID;
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));


    }


    @Test(description = "Verify When User already have isNewFlag will not come in Access Token fetch Payment Option V2")
    public void TC_PT_MGV_015() throws Exception {

        User user = userManager.getForWrite(Label.MGV);

        String refId = UUID.randomUUID().toString().substring(0, 18);

        Constants.MerchantType merchantType = MGV_HYBRID;

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));

    }


    @Test(description = "Verify When User already have Voucher Flag isNewFlag will not come  in checksum fetch Payment Option v1")
    public void TC_PT_MGV_016() throws Exception {

        User user = userManager.getForWrite(Label.MGV);

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}", merchantType.getId())
                .replace("{REFID}", refId).replace("{SSOID}", userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(), body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));


    }


    @Test(description = "Verify When User already have Voucher Flag isNewFlag will not come  in checksum fetch Payment Option v2")
    public void TC_PT_MGV_017() throws Exception {

        User user = userManager.getForWrite(Label.MGV);

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}", merchantType.getId())
                .replace("{REFID}", refId).replace("{SSOID}", userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(), body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));


    }


    @Test(description = "Verify When User Has Never Purchased the MGV isNewUser Flag isNewFlag is True in FPO v1 SSO response")
    public void TC_PT_MGV_018() throws Exception {

        User user = userManager.getForWrite(Label.NOMGV);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV1Test fetchPayOptionsV1 = new SSOTokenFetchPayOptionsV1Test();

        Response fetchPaymentOptionsJsonV1 = given(fetchPayOptionsV1.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV1.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


//when User has more than 1 voucher


    @Test(description = "Verify When User Has more than one Voucher isNewUser Flag wil not come in FPO v1 response")
    public void TC_PT_MGV_019() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJson = fetchPaymentOption.execute();

        fetchPaymentOptionsJson.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.isEmptyOrNullString());


    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser Flag wil not come in FPO v2 response")
    public void TC_PT_MGV_019_V2() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

//FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpov2.execute();


        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));

    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser Flag  Flag wil not come in FPO v2 SSO response")
    public void TC_PT_MGV_020() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2 = new SSOTokenFetchPayOptionsV2Test();

        Response fetchPaymentOptionsJsonV2 = given(fetchPayOptionsV2.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));





    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser  will not come  in Access Token fetch Payment Option V1")
    public void TC_PT_MGV_021() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        String refId = UUID.randomUUID().toString().substring(0, 18);

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS",accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));



    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser  will not come in Access Token fetch Payment Option V2")
    public void TC_PT_MGV_022() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        String refId = UUID.randomUUID().toString().substring(0, 18);

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS",accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));


    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser  will not come  in checksum fetch Payment Option v1")
    public void TC_PT_MGV_023() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}",merchantType.getId())
                .replace("{REFID}",refId).replace("{SSOID}",userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(),body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM",Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp =  fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));

    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser will not come  in checksum fetch Payment Option v2")
    public void TC_PT_MGV_024() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}",merchantType.getId())
                .replace("{REFID}",refId).replace("{SSOID}",userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(),body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM",Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp =  fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));

    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser  Flag isNewFlag will not come  in FPO v1 SSO response")
    public void TC_PT_MGV_025() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        //Applying voucher with Hybrid Type Merchant

        Constants.MerchantType merchantType = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));

        //Applying voucher with AddNPay Type Merchant

        Constants.MerchantType merchantType2 = MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u2 = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m2 = new Merchant(merchantType2.getId(), true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2, u2);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV1Test fetchPayOptionsV1 = new SSOTokenFetchPayOptionsV1Test();

        Response fetchPaymentOptionsJsonV1 = given(fetchPayOptionsV1.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV1.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}",
                        not(hasKey("isNewUser")));


    }


//when User has expired MGV


    @Test(description = "Verify When User Has expired MGV isNewUser Flag wil not come in FPO v1 response")
    public void TC_PT_MGV_026() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FetchPayment Options V1

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJson = fetchPaymentOption.execute();

        fetchPaymentOptionsJson.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.isEmptyOrNullString());


    }


    @Test(description = "Verify When User Has expired MGV isNewUser Flag wil not come in FPO v1 response")
    public void TC_PT_MGV_026_V2() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MGV_HYBRID).build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

//FetchPayment Options V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response fetchPaymentOptionsJsonv2 = fpov2.execute();


        fetchPaymentOptionsJsonv2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));

    }


    @Test(description = "Verify When User Has expired MGV voucher isNewUser Flag  Flag wil not come in FPO v2 SSO response")
    public void TC_PT_MGV_027() throws Exception {

        User user = userManager.getForWrite(Label.EXPIREDMGV);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2 = new SSOTokenFetchPayOptionsV2Test();

        Response fetchPaymentOptionsJsonV2 = given(fetchPayOptionsV2.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV2.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));



    }


    @Test(description = "Verify When User Has expired MGV voucher isNewUser  will not come  in Access Token fetch Payment Option V1")
    public void TC_PT_MGV_028() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);

        String refId = UUID.randomUUID().toString().substring(0, 18);
        Constants.MerchantType merchantType = MGV_HYBRID;
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS",accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));

    }


    @Test(description = "Verify When User Has expired MGV voucher isNewUser  will not come in Access Token fetch Payment Option V2")
    public void TC_PT_MGV_029() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);


        String refId = UUID.randomUUID().toString().substring(0, 18);

        Constants.MerchantType merchantType = MGV_HYBRID;

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);

        JsonPath jsonPath = createToken.execute().jsonPath();

        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS",accessToken)
                .setMid(merchantType.getId()).build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp = fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));

    }


    @Test(description = "Verify When User Has expired MGV voucher isNewUser  will not come  in checksum fetch Payment Option v1")
    public void TC_PT_MGV_030() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);

        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}",merchantType.getId())
                .replace("{REFID}",refId).replace("{SSOID}",userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(),body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM",Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOption fpoV1 = new FetchPaymentOption(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpoV1.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp =  fpoV1.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }


    @Test(description = "Verify When User Has expired MGV voucher isNewUser will not come  in checksum fetch Payment Option v2")
    public void TC_PT_MGV_031() throws Exception {


        User user = userManager.getForWrite(Label.EXPIREDMGV);


        String body = "{\"mid\":\"{MID}\",\"referenceId\":\"{REFID}\",\"paytmSsoToken\":\"{SSOID}\"}";

        Constants.MerchantType merchantType = MGV_HYBRID;

        String refId = UUID.randomUUID().toString().substring(0, 10);

        String userToken = user.ssoToken();

        body = body.replace("{MID}",merchantType.getId())
                .replace("{REFID}",refId).replace("{SSOID}",userToken);

        String Checksum = PGPUtil.getChecksum(merchantType.getKey(),body);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM",Checksum)
                .setMid(merchantType.getId())
                .setReferenceId(refId)
                .setPaytmSsoToken(userToken)
                .build();

        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(Constants.MerchantType.MGV_HYBRID.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);

        Response AccessTokenfetchPaymentOptionsResp =  fpov2.execute();

        AccessTokenfetchPaymentOptionsResp.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));



    }


    @Test(description = "Verify When User Has more than one Voucher isNewUser  Flag isNewFlag will not come  in FPO v1 SSO response")
    public void TC_PT_MGV_032() throws Exception {

        User user = userManager.getForWrite(Label.EXPIREDMGV);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(Constants.MerchantType.MGV_HYBRID.getId()).build();

        SSOTokenFetchPayOptionsV1Test fetchPayOptionsV1 = new SSOTokenFetchPayOptionsV1Test();

        Response fetchPaymentOptionsJsonV1 = given(fetchPayOptionsV1.reqBldr()
                .removeQueryParam("mid")
                .addQueryParam("mid", Constants.MerchantType.MGV_HYBRID.getId())
                .build()).body(fetchPaymentOptionsDTO).post();

        fetchPaymentOptionsJsonV1.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isNewUser",
                        Matchers.equalTo(true));


    }

}
