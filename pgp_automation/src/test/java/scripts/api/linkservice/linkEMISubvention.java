package scripts.api.linkservice;

import com.paytm.api.linkAPI.FetchLinkApi;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class linkEMISubvention extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",5);
    String fromdate = sdf.format(new Date());
    List<String> items = new ArrayList<>();
    List<String> categoryList = new ArrayList<>();
    void fillItems(){
        items.add("51");
        items.add("true");
        items.add("true");
        items.add("1");
        items.add("321067334");
        items.add("1000");
        items.add("saumsung1");
        categoryList.add("34634883");
        items.add(categoryList.get(0));
        items.add("m12");
        items.add("1152435");
        items.add("1");
    }

    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error msg for generic link for emi subvention")
    public void EMISubvention_001(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("3000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"GENERIC","3000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_LINK_TYPE_VALIDATION);
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error msg for INVOICE link for emi subvention")
    public void EMISubvention_002(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("3000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"INVOICE","3000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_LINK_TYPE_VALIDATION);
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error msg for FIXED link When subvention amount is greater than link amount")
    public void EMISubvention_003(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("3000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","300");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(AMOUNT_EMI_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Creation When subvention amount is equal to link amount")
    public void EMISubvention_004(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("2000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(CREATE_LINK_SUCCESS);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Creation When link amount is greater than subvention amount")
    public void EMISubvention_005(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("1000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(CREATE_LINK_SUCCESS);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Product Id is null for Fixed Link")
    public void EMISubvention_006(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(4,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_PRODUCTID_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Brand Id is null for Fixed Link")
    public void EMISubvention_007(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(6,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_BRANDID_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Quantity is null for Fixed Link")
    public void EMISubvention_008(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(3,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_QUANTITY_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Price is null for Fixed Link")
    public void EMISubvention_009(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_PRICE_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Category List is null for Fixed Link")
    public void EMISubvention_0010(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(7,"");
        copyItems.set(5,"2000");
        List<String>Category = new ArrayList<>();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_CATEGORYLIST_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Id  is null for Fixed Link")
    public void EMISubvention_0011(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(10,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_ID_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Creation when verticalId,isEmiEnabled,isPhysical,merchantId,model are null ")
    public void EMISubvention_0012(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(0,"");
        copyItems.set(1,"");
        copyItems.set(2,"");
        copyItems.set(8,"");
        copyItems.set(9,"");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(CREATE_LINK_SUCCESS);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Creation When price is equal to link Amount for FIXED Link")
    public void EMISubvention_0013(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(CREATE_LINK_SUCCESS);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Price is less than link amount for FIXED link")
    public void EMISubvention_0014(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"1000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_ITEM_PRICE_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg When Price is greater than link amount for FIXED link")
    public void EMISubvention_0015(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_ITEM_PRICE_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg Subvention Amount and Items Based Subvention Both passed together")
    public void EMISubvention_0016(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("1000","3454","true",items);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(ALL_EMI_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg For Generic Link Type And Item Based Subvention")
    public void EMISubvention_0017(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",items);
        createNewLink.buildRequest(mid,"GENERIC","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_LINK_TYPE_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg For INVOICE Link Type And Item Based Subvention")
    public void EMISubvention_0018(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",items);
        createNewLink.buildRequest(mid,"INVOICE","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_LINK_TYPE_VALIDATION);

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg For INVOICE Link Type And Item Based Subvention")
    public void EMISubvention_0019(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",items);
        createNewLink.buildRequest(mid,"INVOICE","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_LINK_TYPE_VALIDATION);
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Fetch For Fixed link and Amount based Subvention")
    public void EMISubvention_0020(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        List<String> copyItems = new ArrayList<>();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("1000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId= withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi = new FetchLinkApi().buildRequest(mid,linkId,toDate,fromdate);
        JsonPath withDrawJsonFetchLink = fetchLinkApi.execute().jsonPath();
        String simplifiedSubvention = withDrawJsonFetchLink.getString("body.links[0].simplifiedSubvention");
        Assertions.assertThat(simplifiedSubvention).isNotNull();
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Fetch and simplifiedSubvention Object in Response For Fixed link and Amount based Subvention")
    public void EMISubvention_0021(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("1000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId= withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi = new FetchLinkApi().buildRequest(mid,linkId,toDate,fromdate);
        JsonPath withDrawJsonFetchLink = fetchLinkApi.execute().jsonPath();
        String subventionAmount = withDrawJsonFetchLink.getString("body.links[0].simplifiedSubvention.subventionAmount");
        Assertions.assertThat(subventionAmount).isEqualTo("1000.0");
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Successfull link Fetch For Fixed link and Item based Subvention")
    public void EMISubvention_0022(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId= withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi = new FetchLinkApi().buildRequest(mid,linkId,toDate,fromdate);
        JsonPath withDrawJsonFetchLink = fetchLinkApi.execute().jsonPath();
        String simplifiedSubvention = withDrawJsonFetchLink.getString("body.links[0].simplifiedSubvention");
        Assertions.assertThat(simplifiedSubvention).isNotNull();
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Item Array in Successfull link fetch response")
    public void EMISubvention_0023(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId= withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi = new FetchLinkApi().buildRequest(mid,linkId,toDate,fromdate);
        JsonPath withDrawJsonFetchLink = fetchLinkApi.execute().jsonPath();
        List<String> items = withDrawJsonFetchLink.getList("body.links[0].simplifiedSubvention.items");
        Assertions.assertThat(items).isNotNull();
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify simplifiedsubvention object details in logs")
    public void EMISubvention_0024(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("1000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request ::");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(linkServiceLogs).contains("simplifiedSubvention")
        .contains("customerId=3454")
        .contains("subventionAmount=1000.0")
        .contains("selectPlanOnCashierPage=true");
        softAssertions.assertAll();

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify simplifiedsubvention object details in logs")
    public void EMISubvention_0025(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        List<String> copyItems = new ArrayList<>();
        CreateNewLink createNewLink = new CreateNewLink("1000","","",copyItems);
        createNewLink.buildRequest(mid,"FIXED","1000");
        createNewLink.deleteContext("body.customerId");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,linkId,"Payload for initiate transaction request ::");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(linkServiceLogs).contains("simplifiedSubvention")
       .contains("customerId=LINK_CUSTOMER_1234").contains("subventionAmount=1000.0")
        .contains("selectPlanOnCashierPage=true");
        softAssertions.assertAll();

    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify simplifiedsubvention object details in logs for Item Based Subvention")
    public void EMISubvention_0026(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(1,"");
        copyItems.set(2,"");
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","","",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String grepcmd = "grep \"" + "\" /paytm/logs/linkService.log  | " +
                "grep \"" + linkId + "\" | grep \"Payload for initiate transaction request\"";
        String linkServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.LINKSERVICE, grepcmd);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(linkServiceLogs).contains("simplifiedSubvention")
        .contains("customerId=LINK_CUSTOMER_1234")
        .contains("price=2000.0")
        .contains("quantity=1")
        .contains("verticalId=51")
        .contains("isEmiEnabled=true")
        .contains("isPhysical=true")
        .contains("merchantId=testli61258254741921")
        .contains("model=m12")
        .contains("categoryList=[34634883]")
        .contains("brandId=saumsung1")
       .contains("productId=321067334")
        .contains("id=1")
        .contains("selectPlanOnCashierPage=true");
        softAssertions.assertAll();
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error msg When Merchnt does not have Link Subvention Prefrence Active")
    public void EMISubvention_0027(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        List<String> copyItems = new ArrayList<>();
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("3000","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","3000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_SUBVENTION_PREF_VALIDATION);
    }
    @Owner("Nirottam")
    @Feature("PGP-38398")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg For Item Based Subvention When merchant does not have subvention Pref for Fixed Link Type")
    public void EMISubvention_0028(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        fillItems();
        List<String> copyItems = new ArrayList<>(items);
        copyItems.set(5,"2000");
        setUserAndMId(Constants.MerchantType.LINK_SKIPLOGIN.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink("","3454","true",copyItems);
        createNewLink.buildRequest(mid,"FIXED","2000");
        createNewLink.deleteContext("body.customerId");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultMessage")).isEqualTo(EMI_SUBVENTION_PREF_VALIDATION);
    }
}
