package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.SummaryLink;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40290")
public class SummaryLinkApiTest extends PGPBaseTest {

    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    @DataProvider(name = "Dataset")
    public Object[][] linkTypeSet()
    {
        return new Object[][]
                {
                        {"FIXED"},
                        {"GENERIC"},
                        {"INVOICE"}
                };
    }

    public List<String> dateSummaryLink(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful SummaryLink response for FIXED,INVOICE,GENERIC link.")
    public void summaryLink_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        SummaryLink summaryLink =new SummaryLink().buildRequest(mid,"14/07/2022","18/07/2022");
        JsonPath withDrawJson2 = summaryLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SUMMARY_LINK_SUCCESS_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SUMMARY_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful SummaryLink response when mid is null for FIXED,INVOICE,GENERIC link.")
    public void summaryLink_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        SummaryLink summaryLink =new SummaryLink().buildRequest(null,"14/07/2022","18/07/2022");
        JsonPath withDrawJson2 = summaryLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SUMMARY_LINK_NULL_MID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SUMMARY_LINK_NULL_MID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful SummaryLink response for date difference FIXED,INVOICE,GENERIC link.")
    public void summaryLink_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        SummaryLink summaryLink =new SummaryLink().buildRequest(mid,"14/07/2022","18/09/2022");
        JsonPath withDrawJson2 = summaryLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SUMMARY_LINK_DATE_ISSUE_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SUMMARY_LINK_DATE_ISSUE_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(description = "verify successful SummaryLink response for PAYMENT_BUTTON link.")
    public void summaryLink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        SummaryLink summaryLink =new SummaryLink().buildRequest(mid,"14/07/2022","30/07/2022");
        JsonPath withDrawJson2 = summaryLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SUMMARY_LINK_SUCCESS_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SUMMARY_LINK_SUCCESS_CODE);
    }

    @Owner("Himanshu Arora")
    @Parameters({"theme"})
    @Test(description="verify successful SummaryLink response for FIXED link.")
    public void summaryLink_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED" , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        List<String>dates=dateSummaryLink(5);
        SummaryLink summaryLink =new SummaryLink().buildRequest(mid,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = summaryLink.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),SUMMARY_LINK_SUCCESS_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),SUMMARY_LINK_SUCCESS_CODE);
        Assertions.assertThat(withDrawJson2.getString("body.invoiceSummary.totalCount")).isNotNull();
        Assertions.assertThat(withDrawJson2.getString("body.invoiceSummary.totalAmount")).isNotNull();
    }



}
