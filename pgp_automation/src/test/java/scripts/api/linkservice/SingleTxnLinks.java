package scripts.api.linkservice;

import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.ArchiveLink;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchLinkApi;
import com.paytm.api.linkAPI.LinkHelper;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.ARCHIVE_LINK_SUCCESS_CODE;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class SingleTxnLinks extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }


    @Owner("Himanshu Arora")
    @Test(description = "Verify singleTransactionOnly True for Generic link error case")
    public void singleTxnLink_01() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultCode"),SINGLETXN_GENERIC_LINK_CODE);
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_GENERIC_LINK);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify order id should not be more than 50 characters")
    public void singleTxnLink_02() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.linkOrderId","11111111111222222222233333333334444444444555555555566");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_ORDERID);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify order id should not be other then regex.")
    public void singleTxnLink_03() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.linkOrderId","11111111&");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_ORDERID_REGEX);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify singleTransactionOnly --> True & max payment allowed more than 1 error case")
    public void singleTxnLink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.singleTransactionOnly",true);
        createNewLink.setContext("body.maxPaymentsAllowed","10");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_MAXPAYMENT);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify singleTransactionOnly --> True for Payment Button error case")
    public void singleTxnLink_05() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_GENERIC_LINK);
    }


    @Owner("Himanshu Arora")
    @Test(description = "Verify singleTransactionOnly --> True for partial payment error case")
    public void singleTxnLink_06() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        createNewLink.setContext("body.singleTransactionOnly",true);
        createNewLink.setContext("body.partialPayment",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_PARTIAL);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify we are getting error msg for offline links for  single txn links. ")
    public void singleTxnLink_07() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_OFFLINE_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "PAYMENT_BUTTON", "200");
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        Assert.assertEquals(withDrawJson1.getString("body.resultInfo.resultMessage"),SINGLETXN_OLDLINKS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link.")
    public void singleTxnLink_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for Invoice link.")
    public void singleTxnLink_09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link when orderid is passed in request.")
    public void singleTxnLink_10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for Invoice link when orderid is passed in request.")
    public void singleTxnLink_11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link when both singleTransactionOnly=true and orderid is passed in request.")
    public void singleTxnLink_12(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.singleTransactionOnly",true);
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for Invoice link when both singleTransactionOnly=true and orderid is passed in request.")
    public void singleTxnLink_13(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_TXN_PG2.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        createNewLink.setContext("body.singleTransactionOnly",true);
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }



    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link when pref SINGLE_TXN_ATTEMPT_ON_LINK is true.")
    public void singleTxnLink_14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.SINGLETXN_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for Invoice link when pref SINGLE_TXN_ATTEMPT_ON_LINK is true.")
    public void singleTxnLink_15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.SINGLETXN_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link when pref SINGLE_TXN_ATTEMPT_ON_LINK is true & order id is also passed.")
    public void singleTxnLink_16(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.SINGLETXN_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for Invoice link when pref SINGLE_TXN_ATTEMPT_ON_LINK is true & order id is also passed.")
    public void singleTxnLink_17(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.SINGLETXN_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "2");
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }

    @Owner("Himanshu Arora")
    @Test(description = "verify single txn link with deferOrderCreation=true for fixed link when pref SINGLE_TXN_ATTEMPT_ON_LINK is true & order id is also passed and singleTransactionOnly=true.")
    public void singleTxnLink_18(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.SINGLETXN_LINK.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
        createNewLink.setContext("body.linkOrderId", CommonHelpers.generateOrderId());
        createNewLink.setContext("body.singleTransactionOnly",true);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.SuccessfullScreen().waitUntilVisible();
        Assertions.assertThat(cashierPage.SuccessfullScreen().getText()).contains("Paid Successfully");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request");
        Assertions.assertThat(linkServiceLogs).contains("deferOrderCreation=true");

    }











}
