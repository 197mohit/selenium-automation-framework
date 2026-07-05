package com.paytm.api.linkAPI;

import com.google.gson.JsonObject;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.LinkPaymentLoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.*;

public class LinkHelper {
    public void containsExactlyInHeadForCreateLinkResponse(Map<String,String>createNewLinkResponse){
        HashMap<String,String> responseMap=new HashMap<>();
        responseMap.put("version","true");
        responseMap.put("timestamp","true");
        responseMap.put("channelId","true");
        responseMap.put("tokenType","true");
        responseMap.put("clientId","true");
        Iterator<String> keys = createNewLinkResponse.keySet().iterator();
        System.out.println("Head ==>"+createNewLinkResponse);
        while(keys.hasNext()) {
            String key = keys.next();
            if(responseMap.containsKey(key)==false){
                Assert.assertEquals(responseMap.get(key),"true");
            }

        }
    }
    public void containsExactlyInBodyForCreateLinkResponse( Map<String,String> createNewLinkResponse){
        HashMap<String,String> responseMap=new HashMap<>();
        responseMap.put("linkId","true");
        responseMap.put("linkType","true");
        responseMap.put("longUrl","true");
        responseMap.put("shortUrl","true");
        responseMap.put("amount","true");
        responseMap.put("expiryDate","true");
        responseMap.put("isActive","true");
        responseMap.put("merchantHtml","true");
        responseMap.put("createdDate","true");
        responseMap.put("notificationDetails","true");
        responseMap.put("resultInfo","true");
        System.out.println("Body ==>"+createNewLinkResponse);
        Iterator<String> keys = createNewLinkResponse.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(responseMap.containsKey(key)==false){
                Assert.assertEquals(responseMap.get(key),"true");
            }

        }

    }

    public void containsExactlyInHeadForFetchTxnV1Response(Map<String,String>FetchTxnV1Response){
        HashMap<String,String> responseMap=new HashMap<>();
        responseMap.put("version","true");
        responseMap.put("timestamp","true");
        responseMap.put("channelId","true");
        responseMap.put("tokenType","true");
        responseMap.put("clientId","true");
        Iterator<String> keys = FetchTxnV1Response.keySet().iterator();
        System.out.println("Head ==>"+FetchTxnV1Response);
        while(keys.hasNext()) {
            String key = keys.next();
            if(responseMap.containsKey(key)==false){
                Assert.assertEquals(responseMap.get(key),"true");
            }

        }
    }

    public void containsExactlyInBodyForFetchTxnV1Response( Map<String,String> FetchTxnV1ResponseResponse){
        HashMap<String,String> responseMap=new HashMap<>();
        responseMap.put("resultInfo","true");
        responseMap.put("merchantId","true");
        responseMap.put("merchantName","true");
        responseMap.put("orders","true");
        System.out.println("Body ==>"+FetchTxnV1ResponseResponse);
        Iterator<String> keys = FetchTxnV1ResponseResponse.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(responseMap.containsKey(key)==false){
                Assert.assertEquals(responseMap.get(key),"true");
            }

        }

    }
    public String getPaymentFormId(String paymentLink){
        int len = paymentLink.length();
        int idx = 0;
        for (int i = 0; i < len; i++) {
            if (paymentLink.charAt(i) == '_') {
                idx = i + 1;
                break;
            }
        }
        String paymentFormIdSubstring = paymentLink.substring(idx);
        String paymentFormId = "LINK_PAYMENT_FORM_";
        paymentFormId = paymentFormId.concat(paymentFormIdSubstring);
        return paymentFormId;
    }
    public UIElement LinkPaid(){
        return new UIElement(By.xpath("//b[text()='Link Paid']"),"Link Paid" );
    }
    public UIElement LinkPaidText(){
        return new UIElement(By.xpath("//p[@class='det m-mt-12']"),"Link Paid Paragraph Text" );
    }
    public UIElement InvoiceIsPaid(){
        return new UIElement(By.xpath("(//b[text()='Invoice already Paid'])[1]"),"Invoice already Paid" );
    }
    public  void verifyIsLinkPaid(String paymentLink) throws InterruptedException {
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.launchLoginPage(paymentLink);
        Thread.sleep(10000);
        if(!LinkPaid().isElementPresent()) {
            Assert.fail("Link paid is not present on UI");
        }
        Assert.assertEquals(LinkPaidText().getText(),"The Link is already Paid. Please reach out to the merchant in case of any query.");
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

    public void verifyFieldsInFetchLinkResponse(String linkId,String mid,String fieldName,String fieldExpectedValue){
        FetchLinkApi fetchLinkApi=new FetchLinkApi();
        List<String> dates=dateFetchLink(5);
        fetchLinkApi.buildRequest(mid,linkId,dates.get(1),dates.get((0)));
        JsonPath fetchlinkresponse=fetchLinkApi.execute().jsonPath();
        List<JsonObject> linksObjectInFetchLinkResponse=fetchlinkresponse.getList("body.links");
        int Size=linksObjectInFetchLinkResponse.size();
        for(int i=0;i<Size;i++){
            String linkIdInresponse=fetchlinkresponse.getString("body.links["+i+"].linkId");
            String linkTypeInresponse=fetchlinkresponse.getString("body.links["+i+"].linkType");
            if(linkIdInresponse.equals(linkId)){
                   if(linkTypeInresponse.equals("INVOICE")&& fieldName.equals("maxPaymentsAllowed")){
                       fieldExpectedValue=null;
                   }
                    String fieldActualValue = fetchlinkresponse.getString("body.links[" + i + "]." + fieldName + "");
                    Assert.assertEquals(fieldActualValue, fieldExpectedValue);
            }

        }
    }
    public  void setPrefOnMerchant(String mid, String prefName, String status, String prefValue){
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(mid,prefName,status,prefValue)
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        JsonPath response=merchantAddPreferenceInfo.execute().jsonPath();
        String actualResultStatus=response.getString("response.resultStatus");
        String actualMessaage=response.getString("response.messaage");
        Assert.assertEquals(actualResultStatus,"S");
        Assert.assertEquals(actualMessaage,"Success");
    }

    public void verifyInvoiceIsPaid(){
        if(InvoiceIsPaid().isElementPresent()){
            String actualText=InvoiceIsPaid().getText();
            Assert.assertEquals(actualText,"Invoice already Paid");
        }
    }

    public static boolean verifySuperRouterMerchantLongUrl(String longUrl){
        boolean isSuperRouterLongUrl=false;
        if(longUrl.contains("superRouterPayment")){
            isSuperRouterLongUrl=true;
        }
        return isSuperRouterLongUrl;
    }
    public static boolean verifySuperRouterMerchantLongUrlForInvoiceLink(String longUrl ) throws InterruptedException {
        LinkPaymentLoginPage linkPaymentLoginPage=new LinkPaymentLoginPage();
        linkPaymentLoginPage.launchLoginPage(longUrl);
        linkPaymentLoginPage.invoiceProceedToPayLink().click();
        Thread.sleep(5000);
        String currentUrl= DriverManager.getCurrentWebDriver().getCurrentUrl();
        boolean isSuperRouterLongUrl=false;
        if(currentUrl.contains("superRouterPayment")){
            isSuperRouterLongUrl=true;
        }
        return isSuperRouterLongUrl;
    }

}
