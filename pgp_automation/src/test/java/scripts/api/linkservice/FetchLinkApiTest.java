package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.linkAPI.FetchLinkApi;
import com.paytm.api.linkAPI.templateApis.FetchTemplate;
import com.paytm.api.linkAPI.templateApis.SaveUpdateTemplate;
import com.paytm.api.linkAPI.templateApis.deleteTemplate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.*;

@Feature("PGP-40042")
public class FetchLinkApiTest extends PGPBaseTest {
    User user;
    String mid;

    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    public void deleteFirsttemplate(){
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        deleteTemplate DeleteTemplate=new deleteTemplate().buildRequest(mid,templateId);
        DeleteTemplate.execute();
    }
    public List<String> dateFetchLink(int days){
        List<String> dates=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String toDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",days);
        String fromdate = sdf.format(new Date());
        dates.add(fromdate);
        dates.add(toDate);
        return dates;
    }
    @Owner("Himanshu Arora")
    @Test(description = "Verify Successfully Link Fetch Test for fixed link")
    public void Fetchlink_01() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify Successfully Link Fetch Test for Generic link")
    public void Fetchlink_02() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify Successfully Link Fetch Test for Invoice link")
    public void Fetchlink_03() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when to date is before from date for Invoice link")
    public void Fetchlink_04() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(-5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),TO_DATE_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when from date & to date are same for Invoice link.")
    public void Fetchlink_05() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }


    @Test(description = "Verify api response when to date is before from date for Generic link")
    public void Fetchlink_06() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(-5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),TO_DATE_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);
    }
    @Test(description = "Verify api response when to date is before from date for Fixed link")
    public void Fetchlink_07() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(-5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),TO_DATE_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when from date & to date are same for Generic link.")
    public void Fetchlink_08() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when from date & to date are same for Fixed link.")
    public void Fetchlink_09() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }


    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when mid is null for fixed link")
    public void Fetchlink_10() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(null,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),MID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is null for fixed link")
    public void Fetchlink_11() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,null,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when mid is null for GENERIC link")
    public void Fetchlink_12() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(null,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),MID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);

    }

    @Test(description = "Verify api response when mid is null for INVOICE link")
    public void Fetchlink_13() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(null,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),MID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_FAILED);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is null for GENERIC link")
    public void Fetchlink_14() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,null,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is null for INVOICE link")
    public void Fetchlink_15() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(0);
        fetchLinkApi.buildRequest(mid,null,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is wrong for Invoice link")
    public void Fetchlink_16() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "INVOICE", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        FetchLinkApi fetchLinkApi = new FetchLinkApi();
        String linkId = "1234567899999999999999";
        JsonPath fetchlinkresponse = fetchLinkApi.execute().jsonPath();
        List<String> dates = dateFetchLink(0);
        fetchLinkApi.buildRequest(mid, linkId, dates.get(1), dates.get((0)));
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"), LINKID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"), RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is wrong for GENERIC link")
    public void Fetchlink_17() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "GENERIC", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        FetchLinkApi fetchLinkApi = new FetchLinkApi();
        String linkId = "1234567899999999999999";
        JsonPath fetchlinkresponse = fetchLinkApi.execute().jsonPath();
        List<String> dates = dateFetchLink(0);
        fetchLinkApi.buildRequest(mid, linkId, dates.get(1), dates.get((0)));
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"), LINKID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"), RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response when link id is wrong for FIXED link")
    public void Fetchlink_18() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        FetchLinkApi fetchLinkApi = new FetchLinkApi();
        String linkId = "1234567899999999999999";
        JsonPath fetchlinkresponse = fetchLinkApi.execute().jsonPath();
        List<String> dates = dateFetchLink(0);
        fetchLinkApi.buildRequest(mid, linkId, dates.get(1), dates.get((0)));
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"), LINKID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"), RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response for template for Generic link")
    public void FetchlinkFortemplate_19() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.buildRequest(mid,"School form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId =withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response for template for INVOICE link")
    public void FetchlinkFortemplate_20() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.buildRequest(mid,"School form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId =withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response for template for FIXED link")
    public void FetchlinkFortemplate_21() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.buildRequest(mid,"School form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=fetchTemplateresponse.getString("body.templates[0].id");
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId =withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response for template when template id & link id is null for Generic link")
    public void FetchlinkFortemplate_22() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.buildRequest(mid,"School form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId=null;
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        fetchLinkApi.setContext("body.templateId","null");
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
        List<String> templatelink=new ArrayList<>();
        templatelink=fetchlinkresponse.getList("body.links");
        for(int i=0;i<templatelink.size();i++)
        {
            String templateIdInFetchLink=fetchlinkresponse.getString("body.links[\" + i + \"].templateId");
            String linkIdInFetchLink=fetchlinkresponse.getString("body.links[\" + i + \"].linkId");
            String templateNameInFetchLink=fetchlinkresponse.getString("body.links[\" + i + \"].templateName");
            Assertions.assertThat(templateIdInFetchLink).isNotNull();
            Assertions.assertThat(linkIdInFetchLink).isNotNull();
            Assertions.assertThat(templateNameInFetchLink).isNotNull();
            List<String>templateFields=new ArrayList<>();
            for(int j=0;j<templateFields.size();j++) {
                String fieldName=fetchlinkresponse.getString("body.links[\" + i + \"].fields[\" + j + \"]");
                Assertions.assertThat(fieldName).isNotNull();
            }

        }
    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify api response for template for wrong template id for Generic link")
    public void FetchlinkFortemplate_23() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY_OFFLINE.getId().toString());
        deleteFirsttemplate();
        SaveUpdateTemplate saveUpdateTemplate = new SaveUpdateTemplate();
        saveUpdateTemplate.buildRequest(mid,"School form");
        saveUpdateTemplate.execute();
        FetchTemplate fetchTemplate =new FetchTemplate();
        fetchTemplate.buildRequest(mid);
        JsonPath fetchTemplateresponse=fetchTemplate.execute().jsonPath();
        String templateId="1234999999";
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        createNewLink.setContext("body.templateId",templateId);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId =withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        fetchLinkApi.setContext("body.templateId","1239999999");
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"), LINKID_ERROR);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"), RESULTMSG_SUCCESS);

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-47918")
    @Test(description = "Verify Successfully Link Fetch Test for Payment Button link")
    public void Fetchlink_24() throws Exception {
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkPaymentBtn = new CreateNewLink().buildRequest(mid,"PAYMENT_BUTTON","200");
        createNewLinkPaymentBtn.deleteContext("body.sendSms");
        createNewLinkPaymentBtn.deleteContext("body.sendEmail");
        createNewLinkPaymentBtn.deleteContext("body.customerContact");
        JsonPath withDrawJson1 = createNewLinkPaymentBtn.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultMessage"),FETCH_LINK_SUCCESS);
        Assert.assertEquals(fetchlinkresponse.getString("body.resultInfo.resultStatus"),RESULTMSG_SUCCESS);
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-47918")
    @Test(description = "Verify Successfull Fetch link response in linkexchange logs.")
    public void Fetchlink_25() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,linkId);

        Assertions.assertThat(linkExchangeLogs).contains(linkId);
        Assertions.assertThat(linkExchangeLogs).contains("\"resultStatus\":\"SUCCESS\"");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultCode\":\"200\"");

    }

    @Owner("Himanshu Arora")
    @Feature("PGP-47918")
    @Test(description = "Verify Successfull Fetch link response in linkexchange logs when couponcode is passed with edcemi fields.")
    public void Fetchlink_26() throws Exception {
        setUserAndMId(Constants.MerchantType.EDC_LINK.getId().toString());
        CreateNewLink EdcLink=new CreateNewLink(mid,"HIMANSHU");
        JsonPath withDrawJson1 = EdcLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,linkId);

        Assertions.assertThat(linkExchangeLogs).contains(linkId);
        Assertions.assertThat(linkExchangeLogs).contains("couponCode");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultStatus\":\"SUCCESS\"");
        Assertions.assertThat(linkExchangeLogs).contains("\"resultCode\":\"200\"");

    }


    @Owner("Himanshu Arora")
    @Feature("PGP-47918")
    @Test(description = "Verify failure Fetch link response in linkexchange logs when link is not found.")
    public void Fetchlink_27() throws Exception {
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","20");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String linkId = withDrawJson1.getString("body.linkId");
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String>dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,"999999999",dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        String linkExchangeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_exchange,"999999999");

        Assertions.assertThat(linkExchangeLogs).contains("\"resultMessage\":\"No link found.\"");

    }





}
