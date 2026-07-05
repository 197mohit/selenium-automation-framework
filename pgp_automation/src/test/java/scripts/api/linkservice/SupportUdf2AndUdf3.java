package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.ExpireLink;
import com.paytm.api.linkAPI.GenerateQr;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

public class SupportUdf2AndUdf3 extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf3 is pass while creating order")
    public void createLink_1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudf");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudf");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf2 is pass while creating order")
    public void createLink_2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudf");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudf");

    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_3(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED CC txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_4(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_5(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_6(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED CC txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_7(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED DC txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_8(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf2 with special character and pass 400 character is pass while creating order")
    public void createLink_9(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudf3:,.90()uefksjdfneanfausdfjnfkerufanksnfsjgfbdjewaakbfxdeufbxnrjekaednkerbfaaefwkjhxnbejdwflwerjfbxdsjxfjaefjwelfxeejldsbfxubjrgbhkashdbfkbxejwbxbekwfjxkaelfrwerjxejjnxadwnjxewjfnjjnewfbfddgsjfcbnsaxhjsdabzkxahskzxbhewzhbsbbhdhzkshkwehfdhberfhdsajknjdknwjqjEWUIQFDQBEWBFHCBDSBJDJKBSJKAJKBDBJJBDFJBDSBJDBCJBJCBDJEJWBDBEWRFIYYEWBFBHDSBHBHFEBHFBEBKFEBJKFEWJBKJBKEWJBKJKBWEBJWFEBJEBJJBKEFWJBBJJKEW");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudf3:,.90()uefksjdfneanfausdfjnfkerufanksnfsjgfbdjewaakbfxdeufbxnrjekaednkerbfaaefwkjhxnbejdwflwerjfbxdsjxfjaefjwelfxeejldsbfxubjrgbhkashdbfkbxejwbxbekwfjxkaelfrwerjxejjnxadwnjxewjfnjjnewfbfddgsjfcbnsaxhjsdabzkxahskzxbhewzhbsbbhdhzkshkwehfdhberfhdsajknjdknwjqjEWUIQFDQBEWBFHCBDSBJDJKBSJKAJKBDBJJBDFJBDSBJDBCJBJCBDJEJWBDBEWRFIYYEWBFBHDSBHBHFEBHFBEBKFEBJKFEWJBKJBKEWJBKJKBWEBJWFEBJEBJJBKEFWJBBJJKEW");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudf3:,.90()uefksjdfneanfausdfjnfkerufanksnfsjgfbdjewaakbfxdeufbxnrjekaednkerbfaaefwkjhxnbejdwflwerjfbxdsjxfjaefjwelfxeejldsbfxubjrgbhkashdbfkbxejwbxbekwfjxkaelfrwerjxejjnxadwnjxewjfnjjnewfbfddgsjfcbnsaxhjsdabzkxahskzxbhewzhbsbbhdhzkshkwehfdhberfhdsajknjdknwjqjEWUIQFDQBEWBFHCBDSBJDJKBSJKAJKBDBJJBDFJBDSBJDBCJBJCBDJEJWBDBEWRFIYYEWBFBHDSBHBHFEBHFBEBKFEBJKFEWJBKJBKEWJBKJKBWEBJWFEBJEBJJBKEFWJBBJJKEW");

    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED NB txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED DC txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_12(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull FIXED CC txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_13(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify error messege for Fixed link when we pass more than 400 character in udf2")
    public void createLink_14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_2);
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),UDF_ERROR_MESSEGE);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify error messege for FIXED link when we pass more than 400 character in udf3")
    public void createLink_15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("FIXED", "udf1", "udf2", "udf3").buildRequest(mid, "FIXED", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_2);
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),UDF_ERROR_MESSEGE);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf3 is pass while creating order")
    public void createLink_16(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudf");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudf");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf2 is pass while creating order")
    public void createLink_17(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudf");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudf");

    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_18(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE CC txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_19(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf3 with special character is pass while creating order")
    public void createLink_20(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_21(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE CC txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_22(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE DC txn and vaildate udf2 with special character is pass while creating order")
    public void createLink_23(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2","dfghudfdshafk,:()");
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains("dfghudfdshafk,:()");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf2 with special character and pass 400 character is pass while creating order")
    public void createLink_24(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_25(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE NB txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_26(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE DC txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_27(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify successfull INVOICE CC txn and vaildate udf2 or udf3 with special character and pass 400 character is pass while creating order")
    public void createLink_28(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_1);
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_1);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4375512441465005");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("udf3");
        Assertions.assertThat(logs).contains(UDF_String_1);
        Assertions.assertThat(logs).contains("udf2");
        Assertions.assertThat(logs).contains(UDF_String_1);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify error messege for INVOICE link when we pass more than 400 character in udf2")
    public void createLink_29(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf2",UDF_String_2);
        createNewLink.deleteContext("body.additionalInfo.udf3");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),UDF_ERROR_MESSEGE);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-49139")
    @Parameters({"theme"})
    @Test(description = "verify error messege for INVOICE link when we pass more than 400 character in udf3")
    public void createLink_30(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("INVOICE", "udf1", "udf2", "udf3").buildRequest(mid, "INVOICE", "1");
        createNewLink.setContext("body.additionalInfo.udf3",UDF_String_2);
        createNewLink.deleteContext("body.additionalInfo.udf2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),UDF_ERROR_MESSEGE);
    }

}
