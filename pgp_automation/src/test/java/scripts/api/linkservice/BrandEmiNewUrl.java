package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
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
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

public class BrandEmiNewUrl extends PGPBaseTest {

    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }


    @Owner("Himanshu Arora")
    @Feature("PGP-50457")
    @Test(description = "Verify new key emiLinkPayment in payment summary url when ff4j flag is on emi.link.web.payment.enable.")
    public void BrandEmiNewUrl_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");

       LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
       linkPaymentLoginPage.openLink(paymentLink,"FIXED");

        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"PaymentSummaryData");
        Assertions.assertThat(linkServiceLogs).contains("emiLinkPayment");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-56478")
    @Test(description = "Verify the EMI DC paymode on clicking EDC EMI payment link when ff4j flag theia.addEmiDcForEdcLinkPayments is ON ")
    public void EDC_EMIDC_Paymode_EdcLink(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK","DEBIT_CARD");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        System.out.println(paymentLink);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        linkPaymentLoginPage.waitUntilLoads();
        linkPaymentLoginPage.procedToConvertEMI().click();
        String paymodeOnUI=linkPaymentLoginPage.EDCEMI_Paymode().getText();
        Assertions.assertThat(paymodeOnUI).contains("Debit Card");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-56478")
    @Test(description = "Verify the EMI CC paymode on clicking EDC EMI payment link when ff4j flag theia.addEmiDcForEdcLinkPayments is ON ")
    public void EDC_EMICC_Paymode_EdcLink(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"EDCLINK","CREDIT_CARD");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        System.out.println(paymentLink);
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLink(paymentLink,"FIXED");
        linkPaymentLoginPage.waitUntilLoads();
        linkPaymentLoginPage.procedToConvertEMI().click();
        linkPaymentLoginPage.waitUntilLoads();
        String paymodeOnUI=linkPaymentLoginPage.EDCEMI_Paymode().getText();
        Assertions.assertThat(paymodeOnUI).contains("Credit Card");
    }

}
