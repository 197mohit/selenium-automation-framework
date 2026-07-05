package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchTransactionApi;
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

@Feature("PGP-40279")
public class FetchTransactionApiTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
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

    public List<String> dateFetchTransaction(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful FetchTransactionApi link response when mid is null for FIXED,GENERIC,INVOICE link.")
    public void fetchTransaction_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(null,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_NULLMID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_NULLMID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful FetchTransactionApi link response when linkid is null for FIXED,GENERIC,INVOICE link.")
    public void fetchTransaction_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest( mid,null,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_NULL_LINKID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful FetchTransactionApi link response for FIXED,GENERIC,INVOICE link.")
    public void fetchTransaction_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_SUCCESS_CODE);
        Assert.assertEquals(withDrawJson2.getString("body.customerContact.customerName"),"nirottam");
        Assert.assertEquals(withDrawJson2.getString("body.customerContact.customerEmail"),"nirottam.singh@paytm.com");
        Assert.assertEquals(withDrawJson2.getString("body.customerContact.customerMobile"),"7014107741");
    }


    @Owner("Himanshu Arora")
    @Test(description = "verify successful FetchTransactionApi link response for PAYMENT_BUTTON link.")
    public void fetchTransaction_04() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_SUCCESS_CODE);
    }


    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description = "verify successful FetchTransactionApi link response when date difference is more than 31 days for FIXED,GENERIC,INVOICE link.")
    public void fetchTransaction_05(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, linkType, "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(35);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_DATE_MSG);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_DATE_CODE);
    }

    @Owner("Himanshu Arora")
    @Parameters({"theme"})
    @Test(description = "verify successful FetchTransactionApi link response for FIXED,GENERIC,INVOICE link.")
    public void fetchTransaction_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionApi fetchTransactionApi=new FetchTransactionApi().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionApi.execute().jsonPath();
        List<String> orders=new ArrayList<>();
        orders=withDrawJson2.getList("body.orders");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TRANSACTION_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TRANSACTION_SUCCESS_CODE);
        for(int i=0;i<orders.size();i++) {
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].txnId")).isNotNull();
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].orderId")).isNotNull();
        }

    }


}
