package scripts.api.linkservice;

import com.paytm.api.linkAPI.LinksReportDownload;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.ServerConfigProvider;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
@Feature("PGP-37787")
public class LinkReportDownloadApi extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForRead(Label.LINK);
        mid = merchant;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String searchEndDate= CommonHelpers.addDays(sdf.format(new Date()),"dd/MM/yyyy",5);
    String searchStartDate = sdf.format(new Date());
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ReportDownloadId is not null in ReportDownload api response for only fixed link")
    public void LinksReportDownload_001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate.toString());
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();

        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultMessage")).isEqualTo("Request Successfully Processed");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.reportDownloadId")).isNotNull();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ReportDownloadId is not null in ReportDownload api response for only Generic link")
    public void LinksReportDownload_002(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("GENERIC");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultMessage")).isEqualTo("Request Successfully Processed");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.reportDownloadId")).isNotNull();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ReportDownloadId is not null in ReportDownload api response for only INVOICE link")
    public void LinksReportDownload_003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("INVOICE");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();

        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultMessage")).isEqualTo("Request Successfully Processed");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.reportDownloadId")).isNotNull();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify ReportDownloadId is not null in reportDownload api response for only FIXED,GENERIC,INVOICE links")
    public void LinksReportDownload_004(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("INVOICE");
        linkType.add("GENERIC");
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJsonInvoice = createNewLink.execute().jsonPath();
        createNewLink.buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJsonGeneric = createNewLink.execute().jsonPath();
        createNewLink.buildRequest(mid,"FIXED","200");
        JsonPath withDrawJsonFixed = createNewLink.execute().jsonPath();

        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultMessage")).isEqualTo("Request Successfully Processed");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJsonLinkReport.getString("body.reportDownloadId")).isNotNull();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify LinksReportDownload Api Request Body for only INVOICE link")
    public void LinksReportDownload_005(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("INVOICE");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"INVOICE","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Donwload links report API Request");

        Assertions.assertThat(linkServiceLogs).contains("LinksReportRequestBody")
                .contains("linkType");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify LinksReportDownload Api Request Body for only FIXED link")
    public void LinksReportDownload_006(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        List<String> linkType = new ArrayList<>();
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Donwload links report API Request");

        Assertions.assertThat(linkServiceLogs).contains("LinksReportRequestBody")
                .contains("linkType");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify LinksReportDownload Api Request Body only GENERIC link")
    public void LinksReportDownload_007(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("GENERIC");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"GENERIC","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Donwload links report API Request");

        Assertions.assertThat(linkServiceLogs).contains("LinksReportRequestBody")
                .contains("linkType");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify LinksReportDownload Api Request Body only FIXED link")
    public void LinksReportDownload_008(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"Donwload links report response LinksReportResponse");

        Assertions.assertThat(linkServiceLogs).contains("LinksReportResponseBody")
                .contains("reportDownloadId");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify reportDownloadId is not null in reportDownload api response for only INVOICE link")
    public void LinksReportDownload_009(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"reportDownloadId");

        Assertions.assertThat(linkServiceLogs).contains("\"resultMessage\":\"Request Successfully Processed\"")
                .contains("\"resultStatus\":\"SUCCESS\"")
                .contains("reportDownloadId");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify reportDownloadId is not null in reportDownload api response for only INVOICE link")
    public void LinksReportDownload_0010(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        linkType.add("FIXED");
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        String linkServiceLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.link_service,"https://pgp-qa.s3.ap-south-1.amazonaws.com/%2Fqa7/link_reports/");

         Assertions.assertThat(linkServiceLogs).contains("\"status\":\"SUCCESS\"")
                .contains("\"fileUrl\"")
                .contains("https://pgp-qa.s3.ap-south-1.amazonaws.com/%2Fqa7/link_reports/");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify reportDownloadId is not null in reportDownload api response for only INVOICE link")
    public void LinksReportDownload_0011(@Optional("enhancedweb_revamp") String theme) throws Exception {
        List<String> linkType = new ArrayList<>();
        setUserAndMId(Constants.MerchantType.LINK_PGONLY.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid,"FIXED","200");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        LinksReportDownload linksReportDownload = new LinksReportDownload();
        linksReportDownload.buildRequest(mid,linkType,searchStartDate,searchEndDate);
        JsonPath withDrawJsonLinkReport = linksReportDownload.execute().jsonPath();
        Assert.assertEquals(withDrawJsonLinkReport.getString("body.resultInfo.resultMessage"),"Empty link type.");

    }

}
