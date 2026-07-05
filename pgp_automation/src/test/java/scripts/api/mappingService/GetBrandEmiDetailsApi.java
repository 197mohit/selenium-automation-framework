package scripts.api.mappingService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.MappingService.GetBrandEmiDetails;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;

import com.paytm.dto.mappingService.GetBrandEmiDetail.response.FetchBrandEmiDetailDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;


@Owner("Rahul Gulati")
@Feature("PGP-30749")
public class GetBrandEmiDetailsApi extends PGPBaseTest {

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

    private static ResponseSpecification emiDetailsSchema1(int index,String bankCode, String brandCode) {
        ResponseSpecification verficationSchema = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .rootPath("response.emiBrandSubventionPlans["+index+"].plans.find {it.bank =='" + bankCode + "'}")
                .expectBody("interest", Matchers.comparesEqualTo(Float.parseFloat(queryDb(bankCode, "INTEREST",brandCode).get(index))))
                .expectBody("month", Matchers.comparesEqualTo(Integer.parseInt(queryDb(bankCode,  "MONTH",brandCode).get(index))))
                .expectBody("minAmount",   Matchers.comparesEqualTo(Float.parseFloat(queryDb(bankCode,  "MIN_AMT",brandCode).get(index))))
                .expectBody("maxAmount", Matchers.comparesEqualTo(Float.parseFloat(queryDb(bankCode,  "MAX_AMT",brandCode).get(index))))
                .build();

        return verficationSchema;
    }

    private static ResponseSpecification emiDetailsSchema(String mid, String bankCode) {
        String backCodeDb = bankCode;
        ResponseSpecification verficationSchema = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .rootPath("response.emiBrandSubventionPlans[0].plans.find {it.bank =='" +bankCode+"'}")
                .expectBody("id", Matchers.comparesEqualTo(Integer.parseInt(PGPUtil.getEMIDetailsFromDB(mid, backCodeDb, "ID").get(0))))
                .build();

        return verficationSchema;
    }

    @Test(description = "verify id is returned from MBID_LIMIT_MAPPING")
    public void verifyId() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        Response response = new GetBrandEmiDetails(mid, "LG").execute();
        response.then()
                .statusCode(200)
                .spec(emiDetailsSchema(mid, "ICICI"))
                .spec(emiDetailsSchema(mid, "ICIE"));

    }

    @Test(description = "check id is null for hdfc")
    public void checkIdIsNullforHdfc() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        Response response = new GetBrandEmiDetails(mid, "LG").execute();
        response.then()
                .statusCode(200)
                .body("response.emiBrandSubventionPlans[0].plans.find {it.bank =='HDFC'}.id",
                        isEmptyOrNullString())
                .body("response.emiBrandSubventionPlans[0].plans.find {it.bank =='HDFE'}.id",
                        isEmptyOrNullString());

    }

    @Test(description = "verify plan id is combination of bank and month separated by pipe")
    public void verifyPlanId() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        Response response = new GetBrandEmiDetails(mid, "LG").execute();
        List<String> planId =response.jsonPath().get("response.emiBrandSubventionPlans.find {it.brandCode =='LG'}.plans.planId");
        List<String> bank =response.jsonPath().get("response.emiBrandSubventionPlans.find {it.brandCode =='LG'}.plans.bank");
        List<Integer> month =response.jsonPath().get("response.emiBrandSubventionPlans.find {it.brandCode =='LG'}.plans.month");
        for (int i=0 ; i<planId.size();i++){
            String bbank=bank.get(i);
            String mmonth=month.get(i).toString();

            System.out.println(bbank+"|"+mmonth);
            Assertions.assertThat((bbank+"|"+mmonth)).as("plan id mismatch")
                    .isEqualToIgnoringCase(planId.get(i));
        }


    }

    @Test(description = "verify multiple brand codes")
    public void verifyMultipleBrandCodes() throws Exception {
      String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
       ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Response response = new GetBrandEmiDetails(mid, "LG,DEFAULT").execute();
        Assertions.assertThat(response.jsonPath().getList("response.emiBrandSubventionPlans").size()).as("size mismatch").isEqualTo(2);
        response.then()
                .statusCode(200)
                .spec(emiDetailsSchema1(0,"ICICI", "LG"))
                .spec(emiDetailsSchema1(0,"HDFC", "LG"))
                .spec(emiDetailsSchema1(0,"HDFE", "LG"))
                .spec(emiDetailsSchema1(0,"ICIE", "LG"));
    }



    @Test(description = "verify default is returned is default is passed")
    public void verifyDefaultForDefaultBrand() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Response response = new GetBrandEmiDetails(mid, "DEFAULT").execute();
        Assertions.assertThat(response.jsonPath().getList("response.emiBrandSubventionPlans").size()).as("size mismatch").isEqualTo(1);
        response.then()
                .statusCode(200)
                .spec(emiDetailsSchema1(0,"HDFC", "DEFAULT"));

    }

    @Test(description = "verify default if no brand is passed")
    public void verifyDefaultIfNoBrandIsPassed() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Response response = new GetBrandEmiDetails(mid, "").execute();
        Assertions.assertThat(response.jsonPath().getList("response.emiBrandSubventionPlans").size()).as("size mismatch").isEqualTo(1);
        response.then()
                .statusCode(200)
                .spec(emiDetailsSchema1(0,"HDFC", "DEFAULT"));

    }

    @Test(description = "verify entry not avaiable for wrong brand")
    public void verifyEntryNotAvailable() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();
        Response response = new GetBrandEmiDetails(mid, "ABC").execute();
        Assertions.assertThat(response.jsonPath().get("paytmResultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("paytmResultInfo.resultCode").toString()).as("resultCode mismatch").isEqualToIgnoringCase("00001");
        Assertions.assertThat(response.jsonPath().get("paytmResultInfo.messaage").toString()).as("resultMsg mismatch").isEqualToIgnoringCase("Entry is not available ");


    }

    @Test(description = "check DTO's")
    public void VerifyDtoInResponse() {
        String mid = Constants.MerchantType.DIFFERENT_ROI_EMI.getId();

        ArrayList<String> brandCodes = new ArrayList<>();
        brandCodes.add("LG");
        Response response = new GetBrandEmiDetails(mid, "LG").execute();

        Assertions.assertThat(response.statusCode()).as("Status Code is: "+response.statusCode()).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchBrandEmiDetailDTO fetchBrandEmiDetailDTO = null;
        try {
            fetchBrandEmiDetailDTO = mapper.readValue(jsonObject.toJSONString(), FetchBrandEmiDetailDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in Json", e);
        }

    }



}
