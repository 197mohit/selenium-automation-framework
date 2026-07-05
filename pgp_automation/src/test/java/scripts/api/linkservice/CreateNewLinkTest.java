package scripts.api.linkservice;

import com.paytm.api.AxisBankMockApi;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.ExpireLink;
import com.paytm.api.linkAPI.GenerateQr;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.AssertDelegateTarget;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40289")
public class CreateNewLinkTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    @DataProvider(name = "Dataset")
    public Object[][] linkTypeSet(){
        return new Object[][]{
                {"FIXED"},
                {"GENERIC"},
                {"INVOICE"}
        };
    }
    public static String formatDate(Date date) {
        // Define the desired date format
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        // Format the given date
        return formatter.format(date);
    }


    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify link name cannot be blank for fixed,generic,invoice link.")
    public void createLink_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        createNewLink.deleteContext("body.linkName");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),LINKNAME_BLANK);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify link description cannot be blank for fixed,generic,invoice link.")
    public void createLink_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        createNewLink.deleteContext("body.linkDescription");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),LINK_DESCRIPTION_BLANK);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify amount cannot be blank for fixed link.")
    public void createLink_03() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),AMOUNT_FIXED);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify amount cannot be blank for Invoice link.")
    public void createLink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),AMOUNT_INVOICE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify amount in case of Generic link.")
    public void createLink_05() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESSMSG);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify Invoice id cannot be blank in case of Invoice link.")
    public void createLink_06() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","10");
        createNewLink.deleteContext("body.invoiceId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVOICEID_BLANK);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify invalid expiry date in case of Invoice,fixed,generic link.")
    public void createLink_07(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"10");
        createNewLink.setContext("body.expiryDate","21/07/2022 15:57:28");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVALID_EXPIRY_DATE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify invalid expiry date for more than 5 years in case of Invoice,fixed,generic link.")
    public void createLink_08(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"10");
        createNewLink.setContext("body.expiryDate","28/06/2040 15:57:28");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVALID_EXPIRY_DATE_5YEAR);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify invalid customer phone no. in case of Invoice,fixed,generic link.")
    public void createLink_09(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"10");
        createNewLink.setContext("body.customerContact.customerMobile","956065353");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVALID_CUSTOMER_PHONE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify invalid customer email in case of FIXED,generic,invoice link.")
    public void createLink_10(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"10");
        createNewLink.setContext("body.customerContact.customerEmail","abhi.gupta@paytm");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVALID_CUSTOMER_EMAIL);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);

    }



    @Owner("Himanshu Arora")
    @Test(description = "verify invalid Invoice phone no. in case of INVOICE link.")
    public void createLink_11() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","10");
        createNewLink.setContext("body.invoicePhoneNo","88888888");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVOICE_PHONE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify invalid Invoice EMAIL ID in case of INVOICE link.")
    public void createLink_12() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","10");
        createNewLink.setContext("body.invoiceEmail","88888888@paytm");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),INVOICE_EMAIL);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify skip login in case of Fixed,generic,invoice link.")
    public void createLink_13(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"10");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),CREATE_LINK_SUCCESS);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_SUCCESSMSG);
    }



    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify link description cannot contain special characters for fixed,generic,invoice link.")
    public void createLink_14(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        createNewLink.setContext("body.linkDescription","party@");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),LINK_DESCRIPTION_ERROR);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify link name cannot contain special characters for fixed,generic,invoice link.")
    public void createLink_15(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        createNewLink.setContext("body.linkName","party@");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),LINK_NAME_ERROR);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }



    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify link id  for Generic,fixed,invoice link.")
    public void createLink_16(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        String linkid=withDrawJson1.getString("body.linkId");
        Assertions.assertThat(linkid).isNotNull();
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify lONG URL  for Generic,fixed,invoice link.")
    public void createLink_17(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String longurl=withDrawJson1.getString("body.longUrl");
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assertions.assertThat(longurl).isNotNull();
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify link type  for Payment button")
    public void createLink_18() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String linktype=withDrawJson1.getString("body.linkType");
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(linktype,"PAYMENT_BUTTON");
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify Payment button is supported on mid")
    public void createLink_19() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),PAYMENT_BUTTON_ERROR01);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify Sending notification case for Payment button")
    public void createLink_20() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("head");
        Map<String,String> objectBody= withDrawJson1.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForCreateLinkResponse(objectHead);
        linkHelper.containsExactlyInBodyForCreateLinkResponse(objectBody);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),PAYMENT_BUTTON_ERROR02);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultStatus"),CREATE_LINK_FAILEDMSG);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(dataProvider = "Dataset",description = "verify qrCode for Generic,fixed,invoice link.")
    public void createLink_21(String linkType) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType,"200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String qrCode=withDrawJson1.getString("body.qrCode");
        Assertions.assertThat(qrCode).isNotNull();
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link.")
    public void createLink_22() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,linkId);
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assertions.assertThat(qrCode).isNotNull();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_SUCCESS);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when linkid is null.")
    public void createLink_23() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,null);
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_EMPTY_LINKID);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when mid is null.")
    public void createLink_24() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(null,linkId);
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_MIDNULL);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when linkid is empty.")
    public void createLink_25() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,"");
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_EMPTY_LINKID);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when mid is wrong.")
    public void createLink_26() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest("abcd",linkId);
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_MID_WRONGVALUE);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when LINKID is alphabetic.")
    public void createLink_27() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,"abcd");
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_ALPHABETIC_LINKID);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when linkid is alphanumeric.")
    public void createLink_28() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,"abcd1234");
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_ALPHABETIC_LINKID);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when linkid contains special chracters.")
    public void createLink_29() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest(mid,"@#");
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_ALPHABETIC_LINKID);
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-41057")
    @Test(description = "verify successful generate qr link response for FIXED link when mid contains special chracters.")
    public void createLink_30() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.generateQRCode",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        GenerateQr generateQr=new GenerateQr().buildRequest("@#",linkId);
        JsonPath withDrawJson2 = generateQr.execute().jsonPath();
        String qrCode=withDrawJson2.getString("body.qrCode");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),GENERATEQR_MID_WRONGVALUE);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-48615")
    @Test(description = "verify mapping-service api is called when ff4j flag link.mapping.service.pg2.changes.active is on.")
    public void MappingApi_01() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"mapping-service");
        Assertions.assertThat(linkServiceLogs).contains("\"api\":\"/link/create/\"");
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-48615")
    @Test(description = "verify mapping-service api is called when ff4j flag link.mapping.service.pg2.changes.active is on.")
    public void MappingApi_02() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"mapping-service");
        Assertions.assertThat(linkServiceLogs).contains("\"api\":\"/link/create/\"");
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-48615")
    @Test(description = "verify mapping-service api is called when ff4j flag link.mapping.service.pg2.changes.active is on.")
    public void MappingApi_03() throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.PGOnly.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"mapping-service");
        Assertions.assertThat(linkServiceLogs).contains("\"api\":\"/link/create/\"");
    }

    @Owner("PUSPA")
    @Feature("PGP-52775")
    @Parameters({"theme"})
    @Test(description = "link payment for new logic change when FF4J Flag: theia.linkPaymentIdentifierLogicChange is ON")
    public void Link_Payment_NewLogic(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        CreateNewLink newLink = new CreateNewLink(Constants.MerchantType.LINK_PGONLY.getId());
        JsonPath withDrawJson1 = newLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

    }

    @Owner("VIDHI")
    @Feature("PGP-52544")
    @Parameters("theme")
    @Test(description="Verify the success Link transaction when appostrophe is present in link description")
    public void Link_Apostrophe_desc_SuccessTxn001(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_MID.getId();
        String linkDescription="DOMINO's";
        CreateNewLink newLink=new CreateNewLink().buildRequest(merchant,"FIXED","10",linkDescription,"");
        JsonPath jsonPath=newLink.execute().jsonPath();
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        CashierPage cashierPage=CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO=new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        Assertions.assertThat(cashierPage.linkDescriptionOnLinkStatusPage().getText()).contains(linkDescription);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("VIDHI")
    @Feature("PGP-52544")
    @Parameters({"theme"})
    @Test(description = "Verify the failed Link transaction when appostrophe is present in link description")
    public void Link_Apostrophe_desc_FailedTxn001(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        String merchant = Constants.MerchantType.LINK_MID.getId();
        String linkDescription="DOMINO's";
        CreateNewLink newLink = new CreateNewLink().buildRequest(merchant,"FIXED","99.98",linkDescription,"");
        JsonPath jsonPath = newLink.execute().jsonPath();
        String paymentLink = jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        Assertions.assertThat(cashierPage.linkDescriptionOnLinkStatusPage().getText()).contains(linkDescription);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Payment Failed");
    }

    @Owner("VIDHI")
    @Feature("PGP-52544")
    @Parameters("theme")
    @Test(description = "Verify the success Link transaction when 2 special characters are present in link description")
    public void Link_Apostrophe_desc_SuccessTxn002(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_MID.getId();
        String linkDescription="DOMINO's 2.0";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"FIXED","10",linkDescription,"");
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        CashierPage cashierPage=CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO=new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        Assertions.assertThat(cashierPage.linkDescriptionOnLinkStatusPage().getText()).contains(linkDescription);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }

    @Owner("VIDHI")
    @Feature("PGP-52544")
    @Parameters({"theme"})
    @Test(description = "Verify the failed Link transaction when 2 special characters are present  in link description")
    public void Link_Apostrophe_desc_FailedTxn002(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        String merchant = Constants.MerchantType.LINK_MID.getId();
        String linkDescription="DOMINO's 2.0";
        CreateNewLink newLink = new CreateNewLink().buildRequest(merchant,"FIXED","99.98",linkDescription,"");
        JsonPath jsonPath = newLink.execute().jsonPath();
        String paymentLink = jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        Assertions.assertThat(cashierPage.linkDescriptionOnLinkStatusPage().getText()).contains(linkDescription);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Payment Failed");
    }
    @Owner("VIDHI")
    @Feature("PGP-52544")
    @Parameters("theme")
    @Test(description = "Verify the success Link transaction when no special character is present in link description")
    public void Link_Apostrophe_desc_SuccessTxn003(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_MID.getId();
        String linkDescription="DOMINOs";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"FIXED","10",linkDescription,"");
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        CashierPage cashierPage=CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO=new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        Assertions.assertThat(cashierPage.linkDescriptionOnLinkStatusPage().getText()).contains(linkDescription);
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
    }
    @Owner("Karmvir")
    @Feature("PGP-55982")
    @Parameters({"theme"})
    @Test(description = "Test that E2e Link payment txn for new DQR flow when JS_CHECKOUT_ONLINE_FLOW_ENABLED and ONLINE_FLOW_ENABLED on merchant")
    public void Link_Payment_E2E_Success_Txn_NEWDQR(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        CreateNewLink newLink = new CreateNewLink(Constants.MerchantType.AXIS_BANK_WEB_DQR.getId());
        String callBackUrl= com.paytm.utils.merchant.Constants.PGP_HOST+"/instaproxy/bankresponse/AXIF/UPI/RESPONSE";
        JsonPath withDrawJson1 = newLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilAllAJAXCallsFinish();
        String cahsierPayRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,Constants.MerchantType.AXIS_BANK_WEB_DQR.getId(),"ACQUIRING_PAY_ORDER" ,"REQUEST");
        Assertions.assertThat(cahsierPayRequest).contains("\"additionalInfo\":\"pushDataToDynamicQr:true\"");
        String commonStorage = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,Constants.MerchantType.AXIS_BANK_WEB_DQR.getId(),"COMMON_STORAGE_FETCH_FORM" ,"RESPONSE");
        System.out.println(commonStorage);
        String esn= commonStorage.split("tr=")[1].split("&am")[0];
        JsonPath Path= AxisBankMockApi.axisBankMockApi(callBackUrl,"paytms.1000db@axis",esn,"10.00");
        org.fest.assertions.api.Assertions.assertThat(Path.getString("orderStatus")).isEqualTo("Success");

    }

    @Owner("VIDHI")
    @Feature("PGP-55721")
    @Parameters("theme")
    @Test(description = "Verify the Subscription start date field on subs link Page ")
    public void Subscription_link_Date_UI_001(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        String merchant = Constants.MerchantType.LINK_SUBS_MID.getId();
        String linkDescription = "DOMINOs";
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(merchant, "SUBSCRIPTION_LINK", "10", linkDescription, "");
        JsonPath jsonPath = createNewLink.execute().jsonPath();
        System.out.println(jsonPath);
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        linkPaymentLoginPage.waitUntilLoads();
        String linkSubsStartDateDesc= linkPaymentLoginPage.linkSubsStartDateDesc().getText();
        Assert.assertEquals(linkSubsStartDateDesc,"Subscription Start Date");
    }
    @Owner("VIDHI")
    @Feature("PGP-55721")
    @Parameters("theme")
    @Test(description = "Verify the Subscription start date value on subs link Page ")
    public void Subscription_link_Date_UI_002(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_SUBS_MID.getId();
        String date=formatDate(new Date());
        String linkDescription = "abc";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"SUBSCRIPTION_LINK","10",linkDescription,"");
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        System.out.println(jsonPath);
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        linkPaymentLoginPage.waitUntilLoads();
        String linkSubsStartDate=linkPaymentLoginPage.linkSubsStartDate().getText();
        System.out.println(linkSubsStartDate);
        Assertions.assertThat(linkSubsStartDate.contains(date));
    }

    @Owner("VIDHI")
    @Feature("PPSL-814")
    @Parameters("theme")
    @Test(description = "Verify the Subscription Expiry date Text")
    public void Subscription_link_Expiry_Date_Text(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_SUBS_MID.getId();
        String date=formatDate(new Date());
        String linkDescription = "abc";
        String planId="36386";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"SUBSCRIPTION_LINK","10",linkDescription,planId);
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        System.out.println(jsonPath);
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        linkPaymentLoginPage.waitUntilLoads();
        String linkSubsExpiryDateDesc=linkPaymentLoginPage.linkSubsExpiryDateDesc().getText();
        System.out.println(linkSubsExpiryDateDesc);
        Assert.assertEquals(linkSubsExpiryDateDesc,"Valid Till");
    }

    @Owner("VIDHI")
    @Feature("PPSL-814")
    @Parameters("theme")
    @Test(description = "Verify the 'Contact Merchant' option on UI when Pref HIDE_CONTACT_SUBS_LINK = N")
    public void Subscription_link_ContactMerchant_Pref_N(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.LINK_SUBS_MID.getId();
        String date=formatDate(new Date());
        String linkDescription = "abc";
        String planId="36386";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"SUBSCRIPTION_LINK","10",linkDescription,planId);
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        System.out.println(jsonPath);
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        linkPaymentLoginPage.waitUntilLoads();
        linkPaymentLoginPage.linkSubsContactMerchant().assertVisible();
    }

    @Owner("VIDHI")
    @Feature("PPSL-814")
    @Parameters("theme")
    @Test(description = "Verify the 'Contact Merchant' option on UI when Pref HIDE_CONTACT_SUBS_LINK = Y")
    public void Subscription_link_ContactMerchant_Pref_Y(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant=Constants.MerchantType.SUBS_UI_TEXT.getId();
        String date=formatDate(new Date());
        String linkDescription = "abc";
        String planId="36387";
        CreateNewLink createNewLink=new CreateNewLink().buildRequest(merchant,"SUBSCRIPTION_LINK","10",linkDescription,planId);
        JsonPath jsonPath=createNewLink.execute().jsonPath();
        System.out.println(jsonPath);
        String paymentLink=jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"None");
        linkPaymentLoginPage.waitUntilLoads();
        linkPaymentLoginPage.linkSubsContactMerchant().assertNotVisible();
    }

    @Owner("VIDHI")
    @Feature("PGP-61332")
    @Parameters("theme")
    @Test(description = "Verify the redirect time on UI when Pref REDIRECTION_TIME_PL is 100 and redirection URL is being passed")
    public void verifyRedirectTimeOnUI_WhenPrefRedirectionTimePL_100(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant = Constants.MerchantType.LINK_SUBS_MID.getId();
        String redirectionUrl = "https://fs.getquickride.com/financialserver/paytmpaymentlinksuccess.do";
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(merchant, redirectionUrl);
        JsonPath jsonPath = createNewLink.execute().jsonPath();
        String paymentLink = jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        String redirectTimeText = cashierPage.redirectTimeOnLinkStatusPage().getText();
        System.out.println("Redirect Time: "+redirectTimeText);
        // Extract numeric value from the text (in case it contains non-numeric characters)
        int redirectTime = Integer.parseInt(redirectTimeText.replaceAll("[^0-9]", ""));
        System.out.println("Redirect Time:------------------------------------- "+redirectTime);
        Assertions.assertThat(redirectTime).isGreaterThan(20).isLessThanOrEqualTo(100);
    }
    
    @Owner("VIDHI")
    @Feature("PGP-61332")
    @Parameters("theme")
    @Test(description = "Verify the REDIRECTION_TIMEOUT=100 in APP_DATA when Pref REDIRECTION_TIME_PL is 100 and redirection URL is being passed")
    public void verify_PrefRedirectionTimePL_100_onAPP_DATA(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant = Constants.MerchantType.LINK_SUBS_MID.getId();
        String redirectionUrl = "https://fs.getquickride.com/financialserver/paytmpaymentlinksuccess.do";
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(merchant, redirectionUrl);
        JsonPath jsonPath = createNewLink.execute().jsonPath();
        String paymentLink = jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        String orderIdText = cashierPage.OrderIdLinkstatus().getText();
        // Extract only the numeric order ID value (e.g., "OrderID:202601282206400070" -> "202601282206400070")
        String orderId = orderIdText.replaceAll("[^0-9]", "");
        System.out.println("Order ID: --------------------------- "+orderId);
        
        // Grep logs with order ID and logger message from THEIA
        String theiaLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, "JsonObj served to  linkPaymentRedirect html page");
        System.out.println("THEIA Logs: --------------------------- "+theiaLogs);
        Assertions.assertThat(theiaLogs).contains(orderId);
        Assertions.assertThat(theiaLogs).contains("\"REDIRECTION_TIMEOUT\":\"100\"");

    }

    @Owner("VIDHI")
    @Feature("PGP-61332")
    @Parameters("theme")
    @Test(description = "Verify the redirect time on UI when Pref REDIRECTION_TIME_PL is not enabled on mid and redirection URL is being passed")
    public void verifyRedirectTimeOnUI_WhenPrefRedirectionTimePL_NotEnabled(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        String merchant = Constants.MerchantType.SUBS_UI_TEXT.getId();
        String redirectionUrl = "https://fs.getquickride.com/financialserver/paytmpaymentlinksuccess.do";
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(merchant, redirectionUrl);
        JsonPath jsonPath = createNewLink.execute().jsonPath();
        String paymentLink = jsonPath.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        String redirectTimeText = cashierPage.redirectTimeOnLinkStatusPage().getText();
        System.out.println("Redirect Time: "+redirectTimeText);
        // Extract numeric value from the text (in case it contains non-numeric characters)
        int redirectTime = Integer.parseInt(redirectTimeText.replaceAll("[^0-9]", ""));
        System.out.println("Redirect Time:------------------------------------- "+redirectTime);
        Assertions.assertThat(redirectTime).isGreaterThan(0).isLessThanOrEqualTo(20);
    }

    @Owner("VIDHI")
    @Feature("PG-1176")
    @Parameters("theme")
    @Test(description = "Verify the UPIPUSH paymode in FPO response in Link txn when supportOnline = TRUE in LPV response | FF4J flag - enable.theia.upi.push.channel.filter - ON")
    public void verifyUPIPUSH_paymode_FPO_supportOnline_true_inLPV(
            @Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        String mid = Constants.MerchantType.PAYMENT_LINKS_MID_UPI.getId();

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath createResponse = createNewLink.execute().jsonPath();
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"), CREATE_LINK_SUCCESS_STATUS);

        String paymentLink = createResponse.getString("body.longUrl");
        String linkId = createResponse.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String theiaLogsByLinkId = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                linkId);
        Assertions.assertThat(theiaLogsByLinkId).as("THEIA_REQ_RESP logs containing linkId").isNotBlank();

        String orderId = extractOrderIdFromTheiaLogs(
                theiaLogsByLinkId,
                "grep by linkId " + linkId);

        String initiateTxnResponseLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                orderId,
                Constants.NativeAPIResourcePath.INIT_TXN,
                "RESPONSE");
        Assertions.assertThat(initiateTxnResponseLogs).as("initiateTransaction RESPONSE logs").isNotBlank();

        String txnToken = extractTxnTokenFromInitiateTxnResponseLogs(initiateTxnResponseLogs);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO =
                new FetchPaymentOptionsDTO.Builder(txnToken).build();
        Response fetchPayOptionsResp = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO).execute();
        Assertions.assertThat(fetchPayOptionsResp.getStatusCode()).as("fetchPaymentOptions HTTP status").isEqualTo(200);
        JsonPath fpoJson = fetchPayOptionsResp.jsonPath();
        Assertions.assertThat(fpoJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fpoJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(
                fpoJson.getString(
                        "body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode"))
                .contains("UPIPUSH");

        String litePayviewConsultRespLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.payment_option_facade,
                orderId,
                "CHECKOUT_LITE_PAYVIEW_CONSULT",
                "RESPONSE");
        Assertions.assertThat(litePayviewConsultRespLogs)
                .as("CHECKOUT_LITE_PAYVIEW_CONSULT RESPONSE on payment_option_facade").isNotBlank();
        Assertions.assertThat(
                litePayviewConsultRespLogs.contains("supportOnline=true")
                        || litePayviewConsultRespLogs.contains("\"supportOnline\":true"))
                .as("supportOnline=true (or JSON) expected in lite payview consult RESPONSE logs")
                .isTrue();
    }

    @Owner("VIDHI")
    @Feature("PG-1176")
    @Parameters("theme")
    @Test(description = "Verify the UPIPUSH paymode in FPO response in Link txn when supportOnline = false in LPV response | FF4J flag - enable.theia.upi.push.channel.filter - ON")
    public void verifyUPIPUSH_paymode_FPO_supportOnline_FALSE_inLPV(
            @Optional("checkoutjs_web_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        String mid = Constants.MerchantType.UPI_PTAB_MID.getId();

        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath createResponse = createNewLink.execute().jsonPath();
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
        Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"), CREATE_LINK_SUCCESS_STATUS);

        String paymentLink = createResponse.getString("body.longUrl");
        String linkId = createResponse.getString("body.linkId");

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink, "None");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        String theiaLogsByLinkId = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                linkId);
        Assertions.assertThat(theiaLogsByLinkId).as("THEIA_REQ_RESP logs containing linkId").isNotBlank();

        String orderId = extractOrderIdFromTheiaLogs(
                theiaLogsByLinkId,
                "grep by linkId " + linkId);

        String initiateTxnResponseLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                orderId,
                Constants.NativeAPIResourcePath.INIT_TXN,
                "RESPONSE");
        Assertions.assertThat(initiateTxnResponseLogs).as("initiateTransaction RESPONSE logs").isNotBlank();

        String txnToken = extractTxnTokenFromInitiateTxnResponseLogs(initiateTxnResponseLogs);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO =
                new FetchPaymentOptionsDTO.Builder(txnToken).build();
        Response fetchPayOptionsResp = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO).execute();
        Assertions.assertThat(fetchPayOptionsResp.getStatusCode()).as("fetchPaymentOptions HTTP status").isEqualTo(200);
        JsonPath fpoJson = fetchPayOptionsResp.jsonPath();
        Assertions.assertThat(fpoJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fpoJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String bhimUpiChannelCode;
        try {
            bhimUpiChannelCode = fpoJson.getString(
                    "body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode");
        } catch (Exception e) {
            bhimUpiChannelCode = null;
        }
        Assertions.assertThat(bhimUpiChannelCode == null || !bhimUpiChannelCode.contains("UPIPUSH"))
                .as("FPO: BHIM UPI channelCode must not contain UPIPUSH for UPI_PTAB_MID")
                .isTrue();

        String litePayviewConsultRespLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.payment_option_facade,
                orderId,
                "CHECKOUT_LITE_PAYVIEW_CONSULT",
                "RESPONSE");
        Assertions.assertThat(litePayviewConsultRespLogs)
                .as("CHECKOUT_LITE_PAYVIEW_CONSULT RESPONSE on payment_option_facade").isNotBlank();
        Assertions.assertThat(
                !(litePayviewConsultRespLogs.contains("supportOnline=true")
                        || litePayviewConsultRespLogs.contains("\"supportOnline\":true")))
                .as("supportOnline=true must not appear in lite payview consult RESPONSE logs for UPI_PTAB_MID")
                .isTrue();
    }

    private static final Pattern ORDER_ID_IN_JSON = Pattern.compile("\"ORDER_ID\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ORDER_ID_CAMEL_IN_JSON = Pattern.compile("\"orderId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TXN_TOKEN_IN_JSON = Pattern.compile("\"txnToken\"\\s*:\\s*\"([^\"]+)\"");

    private static String extractOrderIdFromTheiaLogs(String logs, String contextLabel) {
        Matcher upper = ORDER_ID_IN_JSON.matcher(logs);
        if (upper.find()) {
            return upper.group(1);
        }
        Matcher camel = ORDER_ID_CAMEL_IN_JSON.matcher(logs);
        Assert.assertTrue(camel.find(), "orderId / ORDER_ID not found in Theia logs: " + contextLabel);
        return camel.group(1);
    }

    private static String extractTxnTokenFromInitiateTxnResponseLogs(String logs) {
        Matcher m = TXN_TOKEN_IN_JSON.matcher(logs);
        Assert.assertTrue(m.find(), "txnToken not found in /theia/api/v1/initiateTransaction RESPONSE logs");
        return m.group(1);
    }

}
