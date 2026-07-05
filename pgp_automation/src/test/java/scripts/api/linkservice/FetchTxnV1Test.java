package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchTransactionV1;
import com.paytm.api.linkAPI.LinkHelper;
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
import java.util.Map;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40336")
public class FetchTxnV1Test extends PGPBaseTest {

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
    @Test(dataProvider = "Dataset",description="verify successful FetchTransactionV1 response when mid is null for FIXED,INVOICE,GENERIC link.")
    public void FetchTransactionV1_01(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionV1 fetchTransactionV1=new FetchTransactionV1().buildRequest(null,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionV1.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson2.getMap("head");
        Map<String,String> objectBody= withDrawJson2.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForFetchTxnV1Response(objectHead);
        linkHelper.containsExactlyInBodyForFetchTxnV1Response(objectBody);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TXNV1_NULLMID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TXNV1_NULLMID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful FetchTransactionV1 response when link id is null  for FIXED,INVOICE,GENERIC link.")
    public void FetchTransactionV1_02(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionV1 fetchTransactionV1=new FetchTransactionV1().buildRequest(mid,null,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionV1.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson2.getMap("head");
        Map<String,String> objectBody= withDrawJson2.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForFetchTxnV1Response(objectHead);
        linkHelper.containsExactlyInBodyForFetchTxnV1Response(objectBody);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TXNV1_NULL_LINKID);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TXNV1_NULL_LINKID_CODE);
    }

    @Owner("Himanshu Arora")
    @Test(dataProvider = "Dataset",description="verify successful FetchTransactionV1 response for date difference for FIXED,INVOICE,GENERIC link.")
    public void FetchTransactionV1_03(String linkType) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,linkType , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        List<String>dates=dateFetchTransaction(35);
        FetchTransactionV1 fetchTransactionV1=new FetchTransactionV1().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionV1.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson2.getMap("head");
        Map<String,String> objectBody= withDrawJson2.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForFetchTxnV1Response(objectHead);
        linkHelper.containsExactlyInBodyForFetchTxnV1Response(objectBody);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TXNV1_DATE);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TXNV1_DATE_CODE);
    }

    @Owner("Himanshu Arora")
    @Parameters({"theme"})
    @Test(description="verify successful FetchTransactionV1 response for FIXED link.")
    public void FetchTransactionV1_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED" , "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Integer linkId = withDrawJson1.getInt("body.linkId");
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByNBForNewFlow();
        cashierPage.waitUntilLoads();
        List<String>dates=dateFetchTransaction(5);
        FetchTransactionV1 fetchTransactionV1=new FetchTransactionV1().buildRequest(mid,linkId,dates.get(0),dates.get((1)));
        JsonPath withDrawJson2 = fetchTransactionV1.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson2.getMap("head");
        Map<String,String> objectBody= withDrawJson2.getMap("body");
        LinkHelper linkHelper=new LinkHelper();
        linkHelper.containsExactlyInHeadForFetchTxnV1Response(objectHead);
        linkHelper.containsExactlyInBodyForFetchTxnV1Response(objectBody);
        List<String> orders=new ArrayList<>();
        orders=withDrawJson2.getList("body.orders");
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultMessage"),FETCH_TXNV1_SUCCESS);
        Assert.assertEquals(withDrawJson2.getString("body.resultInfo.resultCode"),FETCH_TXNV1_SUCCESS_CODE);
        for(int i=0;i<orders.size();i++) {
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].txnId")).isNotNull();
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].orderId")).isNotNull();
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].customerName")).isNotNull();
            Assertions.assertThat(withDrawJson2.getString("body.orders[\" + i + \"].customerPhoneNumber")).isNotNull();
        }

    }



}
