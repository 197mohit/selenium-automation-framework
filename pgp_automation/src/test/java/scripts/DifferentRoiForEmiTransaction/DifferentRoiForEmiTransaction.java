package scripts.DifferentRoiForEmiTransaction;

import com.paytm.LocalConfig;
import java.util.stream.Stream;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.nativeAPI.GetEMIDetails;
import com.paytm.appconstants.Constants;

import com.paytm.base.test.PGPBaseTest;

import com.paytm.dto.NativeDTO.getEMIDetails.request.Body;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.Head;

import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantPreferenceInfo;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;

import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PGPUtil;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Owner("Rahul Gulati")
@Feature("PGP-30748")
public class DifferentRoiForEmiTransaction extends PGPBaseTest {

    String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";

    private final static ResponseSpecification paramSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("head", Matchers.notNullValue())
            .expectBody("head.responseTimestamp", Matchers.notNullValue())
            .expectBody("head.version", Matchers.notNullValue())
            .expectBody("body", Matchers.notNullValue())
            .expectBody("body.emiDetails", Matchers.notNullValue())
            .expectBody("body.brandEmiDetails", Matchers.notNullValue())
            .expectBody("body.resultInfo", Matchers.notNullValue())
            .build();

    private final static ResponseSpecification resultSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", IsEqual.equalTo("S"))
            .expectBody("resultCode", IsEqual.equalTo("0000"))
            .expectBody("resultMsg", IsEqual.equalTo("Success"))
            .build();

    private final static ResponseSpecification emiDetailSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.emiDetails")
            .expectBody("channelCode", Matchers.notNullValue())
            .expectBody("channelName", Matchers.notNullValue())
            .expectBody("emiType", Matchers.notNullValue())
            .expectBody("iconUrl", Matchers.notNullValue())
            .expectBody("emiChannelInfos", Matchers.notNullValue())
            .expectBody("multiItemEmiSupported", Matchers.notNullValue())
            .build();


    private final static ResponseSpecification emiChannelInfoSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.emiDetails[0].emiChannelInfos")
            .expectBody("emiId", Matchers.notNullValue())
            .expectBody("planId", Matchers.notNullValue())
            .expectBody("interestRate", Matchers.notNullValue())
            .expectBody("ofMonths", Matchers.notNullValue())
            .expectBody("minAmount.currency", Matchers.notNullValue())
            .expectBody("minAmount.value", Matchers.notNullValue())
            .expectBody("maxAmount.currency", Matchers.notNullValue())
            .expectBody("maxAmount.value", Matchers.notNullValue())
            .build();

    private final static ResponseSpecification brandEmiDetailSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.brandEmiDetails[0]")
            .expectBody("brandCode", Matchers.notNullValue())
            .expectBody("brandEmiDetailInfo", Matchers.notNullValue())
            .build();

    private final static ResponseSpecification brandEmiChannelInfoSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .rootPath("body.brandEmiDetails[0].brandEmiDetailInfo[0].emiChannelInfos[0]")
            .expectBody("emiId", Matchers.notNullValue())
            .expectBody("planId", Matchers.notNullValue())
            .expectBody("interestRate", Matchers.notNullValue())
            .expectBody("ofMonths", Matchers.notNullValue())
            .expectBody("minAmount.currency", Matchers.notNullValue())
            .expectBody("minAmount.value", Matchers.notNullValue())
            .expectBody("maxAmount.currency", Matchers.notNullValue())
            .expectBody("maxAmount.value", Matchers.notNullValue())
            .build();

    private final static ResponseSpecification ff4jDisable = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body", Matchers.notNullValue())
            .expectBody("body.emiDetails", Matchers.notNullValue())
            .expectBody("body.brandEmiDetails", Matchers.nullValue())
            .expectBody("body.resultInfo", Matchers.notNullValue())
            .build();

    private static String createChecksum(String merchantKey,String body) {
        String checksum = "";

        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String GET_EMI_DETAILS(String brandCode, String bankCode, String columnName) {
        return "SELECT " + columnName + " FROM PAYTMPGDB.EMI_BRAND_MASTER INNER JOIN BANK_MASTER ON EMI_BRAND_MASTER.BANK = BANK_MASTER.BANK_ID WHERE\n\t EMI_BRAND_MASTER.STATUS=9376503 AND BANK_MASTER.BANK_CODE='" + bankCode + "' AND EMI_BRAND_MASTER.BRAND_CODE ='" + brandCode + "'";
    }
    private static List<String> queryDb(String bankCode, String col,String brandCode) {
        List<String> resultList = new ArrayList();
        List<Map<String, Object>> valueList = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, GET_EMI_DETAILS(brandCode,bankCode,col));
        if (valueList.isEmpty()) {
            Reporter.report.info("No EMI Details found for this " + brandCode, new Object[0]);
            return null;
        } else {
            for(int i = 0; i < valueList.size(); ++i) {
                String var9 = ((Map)valueList.get(i)).get(col).toString();
                resultList.add(var9);
            }

            return resultList;
        }
    }

    private static ResponseSpecification emiDetailsSchema(int index,String bankCode, String brandCode) {
        ResponseSpecification verficationSchema = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .rootPath("body.brandEmiDetails["+index+"].brandEmiDetailInfo.find {it.channelCode =='" + bankCode + "'}.emiChannelInfos")
                .expectBody("interestRate", containsInAnyOrder(queryDb(bankCode, "INTEREST",brandCode).toArray()))
                .expectBody("ofMonths", containsInAnyOrder(queryDb(bankCode,  "MONTH",brandCode).toArray()))
                .expectBody("minAmount.value", containsInAnyOrder(queryDb(bankCode,  "MIN_AMT",brandCode).toArray()))
                .expectBody("maxAmount.value", containsInAnyOrder(queryDb(bankCode,  "MAX_AMT",brandCode).toArray()))
                .build();

        return verficationSchema;
    }

    @Test(priority = 1,description = "verify Mid is mandatory parameter")
    public void verifyMidIsMandatory()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"]}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM")
        .deleteContext("body.mid");
        Response response = request.execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("U");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("resultCode mismatch").isEqualToIgnoringCase("00000900");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("resultMsg mismatch").isEqualToIgnoringCase("System error");

    }

    @Test(priority = 1,description = "verify brandCode is not mandatory parameter")
    public void verifyBrandCodeIsNotMandatory()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM")
                .deleteContext("body.brandCode");
        Response response = request.execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg Mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");

    }

    @Test(priority = 1,description = "verify schemas")
    public void verifySchemas()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM");
        Response response = request.execute();
        response.then()
                .spec(paramSchema)
                .spec(resultSchema)
                .spec(emiDetailSchema)
                .spec(emiChannelInfoSchema)
                .spec(brandEmiDetailSchema)
                .spec(brandEmiChannelInfoSchema);


    }

    @Test(priority = 1,description = "verify Emis from Db")
    public void verifyAllEmisFromDb()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM");
        Response response = request.execute();
        response.then()
                .spec(emiDetailsSchema(0,"HDFC","LG"))
                .spec(emiDetailsSchema(0,"HDFE","LG"))
                .spec(emiDetailsSchema(0,"ICICI","LG"))
                .spec(emiDetailsSchema(0,"ICIE","LG"))
                ;


    }

    @Test(priority = 1,description = "verify default is returned if no brand is passed")
    public void verifyDefaultIfNoBrandIsPassed() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM")
        .deleteContext("body.brandCode");
        Response response = request.execute();
        Assertions.assertThat(response.jsonPath().getList("body.brandEmiDetails").size()).as("").isEqualTo(1);

        response.then()
                .spec(emiDetailsSchema(0,"HDFC","DEFAULT"))
                ;


    }

    @Test(priority = 1,description = "verify multiple brands are returned")
    public void verifyMultipleBrandsAreReturned() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\",\"DEFAULT\"],\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        brandCodes.add("DEFAULT");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM");
        Response response = request.execute();
        Assertions.assertThat(response.jsonPath().getList("body.brandEmiDetails").size()).as("").isEqualTo(2);

        response.then()
                .spec(emiDetailsSchema(0,"HDFC","DEFAULT"))
                .spec(emiDetailsSchema(1,"ICICI","LG"))
        ;
  }

    @Test(priority = 1,description = "verify emiID is same ")
    public void verifyEmiIdIsSame()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("mid", mid);

        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid)
                        .setProductCode("51051000100000000001")
                        .setBrandCode(brandCodes));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.tokenType","CHECKSUM");
        Response response = request.execute();
        ArrayList<String>  emiIdICICI= response.jsonPath().get("body.emiDetails.find {it.channelCode =='ICICI'}.emiChannelInfos.emiId");
        ArrayList<String>  emiIdHDFC= response.jsonPath().get("body.emiDetails.find {it.channelCode =='HDFC'}.emiChannelInfos.emiId");
        ArrayList<String>  emiIdHDFE= response.jsonPath().get("body.emiDetails.find {it.channelCode =='HDFE'}.emiChannelInfos.emiId");
        ArrayList<String> emiIdICICI1= response.jsonPath().get("body.brandEmiDetails[0].brandEmiDetailInfo.find {it.channelCode =='ICICI'}.emiChannelInfos.emiId");
        ArrayList<String> emiIdHDFC1= response.jsonPath().get("body.brandEmiDetails[0].brandEmiDetailInfo.find {it.channelCode =='HDFC'}.emiChannelInfos.emiId");
        ArrayList<String> emiIdHDFE1= response.jsonPath().get("body.brandEmiDetails[0].brandEmiDetailInfo.find {it.channelCode =='HDFE'}.emiChannelInfos.emiId");
        MatcherAssert.assertThat(emiIdICICI, is(emiIdICICI1));
        Boolean test = null;
        if(emiIdHDFC==null) {
            test = emiIdHDFC1.stream().allMatch(str ->str.equalsIgnoreCase("null"));
            Assert.assertTrue(test);
        }
        else{
            MatcherAssert.assertThat(emiIdHDFC, is(emiIdHDFC1));
        }
        if(emiIdHDFE==null) {
            test = emiIdHDFE1.stream().allMatch(str ->str.equalsIgnoreCase("null"));
            Assert.assertTrue(test);
        }
        else{
            MatcherAssert.assertThat(emiIdHDFE, is(emiIdHDFE1));
        }



    }



//    @Test(enabled = false,priority = 5,description = "verify after disabling ff4j flag")        // theia.brandEmi.feature is enabled in ALL merchants
    public void verifyAfterDisablingFF4J()  {
        try{
            FF4JFlags.disable("theia.brandEmi.feature");
            String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();;
            String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
            ArrayList<String> brandCodes = new ArrayList<>();
            brandCodes.add("LG");
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("mid", mid);

            String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
            GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                    .setHead(new Head(signature))
                    .setBody(new Body(mid)
                            .setProductCode("51051000100000000001")
                            .setBrandCode(brandCodes));
            GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
            request.setContext("head.tokenType","CHECKSUM");
            Response response = request.execute();

            response
                    .then()
                    .spec(resultSchema)
                    .spec(ff4jDisable);
        }
        finally {
            FF4JFlags.enable("theia.brandEmi.feature");
        }

    }

    @Test(priority = 6,description = "verify after removing preference on merchant")
    public void verifyAfterRemovingPreference()  {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        try{
            MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                    new MerchantAddPreferenceInfoReq.Builder(mid, "SUBVENTED_EMI_RATE","ACTIVE","N")
                            .build();

            MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
            merchantAddPreferenceInfo.execute();

            String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
            ArrayList<String> brandCodes = new ArrayList<>();
            brandCodes.add("LG");
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("mid", mid);

            String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
            GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                    .setHead(new Head(signature))
                    .setBody(new Body(mid)
                            .setProductCode("51051000100000000001")
                            .setBrandCode(brandCodes));
            GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
            request.setContext("head.tokenType","CHECKSUM");
            Response response = request.execute();

            response
                    .then()
                    .spec(resultSchema)
                    .spec(ff4jDisable);
        }
        finally {
            MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReqEnable =
                    new MerchantAddPreferenceInfoReq.Builder(mid, "SUBVENTED_EMI_RATE","ACTIVE","Y")
                            .build();

            MerchantAddPreferenceInfo merchantAddPreferenceInfoEnable = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReqEnable);
            merchantAddPreferenceInfoEnable.execute();
        }

    }



//    @Test(description = "check DTO's")
//    public void checkDto() {
//
//        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
//        String body = "{\"productCode\":\"51051000100000000001\",\"brandCode\":[\"LG\"],\"mid\":\"BStgBi76007014560704\"}";
//        ArrayList<String> brandCodes = new ArrayList<>();
//        brandCodes.add("LG");
//        Map<String, String> requestMap = new HashMap<>();
//        requestMap.put("mid", mid);
//
//        String signature = createChecksum(Constants.MerchantType.DIFFERENT_ROI_EMI.getKey(), body);
//        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
//                .setHead(new Head(signature))
//                .setBody(new Body(mid)
//                        .setProductCode("51051000100000000001")
//                        .setBrandCode(brandCodes));
//        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
//        request.setContext("head.tokenType","CHECKSUM");
//        Response response = request.execute();
//
//        int statusCode = response.statusCode();
//        Assertions.assertThat(statusCode).as("Status Code is: "+statusCode).isEqualTo(200);
//        JsonPath jsonPath = response.jsonPath();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.putAll(jsonPath.get());
//
//        ObjectMapper mapper = new ObjectMapper();
////        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        BrandEmiDetailInfoDTO brandEmiDetailInfoDTO = null;
//        try {
//            brandEmiDetailInfoDTO = mapper.readValue(jsonObject.toJSONString(), BrandEmiDetailInfoDTO.class);
//        } catch (IOException e) {
//            Assertions.fail("Change in Json", e);
//        }
//
//    }

    @AfterClass(alwaysRun = true)
    public void enableFF4j(){
        FF4JFlags.enable("theia.brandEmi.feature");
    }

}
