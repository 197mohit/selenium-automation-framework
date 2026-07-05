package scripts.DifferentRoiForEmiTransaction;


import com.paytm.LocalConfig;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.nativeAPI.GetEMIDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.Body;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.Head;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.apache.tools.ant.taskdefs.Get;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;

@Owner("Srinivas")
@Feature("PGP-38226")
public  class RoiForEmiTransaction extends PGPBaseTest {
    String signature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdWJ2ZW50aW9uIiwibWlkIjoicWE4UEcyNTMzNDY3OTA2Mjg5ODYifQ.Mgr3rNztkEL8M2UZTNQcWJ1WYT-ylyi6WtK4dIU2F2w";
    public static String GET_EMI_DETAILS(String brandCode, String bankCode, String columnName) {
        return "SELECT " + columnName + " FROM PAYTMPGDB.EMI_BRAND_MASTER INNER JOIN BANK_MASTER ON EMI_BRAND_MASTER.BANK = BANK_MASTER.BANK_ID WHERE\n\t EMI_BRAND_MASTER.STATUS=9376503 AND BANK_MASTER.BANK_CODE='" + bankCode + "' AND EMI_BRAND_MASTER.BRAND_CODE ='" + brandCode + "'";
    }
    private static List<String> queryDb(String bankCode, String col, String brandCode) {
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

    @Test(description = "Verify passing brand code and default parameter in the request body of getEmidetails API")
    public void veify_passing_brandcode_default()
    {
        String mid= Constants.MerchantType.EMI.getId();
        String body="\"body\":{\"mid\":\"qa8PG253346790628986\",\"brandCode\":[\"1703\",\"DEFAULT\"]}";
        ArrayList<String> brandcode=new ArrayList<>();
        brandcode.add("1703");
        brandcode.add("DEFAULT");
        Map<String,String> requestMap=new HashMap<>();
        requestMap.put("mid",mid);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid,brandcode));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.clientId","subvention");
        request.setContext("head.requestId","1234_111224344");
        Response response = request.execute();
        List<Object> brandEmiDetails= response.getBody().jsonPath().getList("body.brandEmiDetails");
        for(int i=0;i<brandEmiDetails.size();i++)
        {
            String brand=brandEmiDetails.get(i).toString();
            Assertions.assertThat(brand).contains("brandCode=1703");
        }
        for(int i=0;i<brandEmiDetails.size();i++) {
           List<Object> brandEmiDetailInfo = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "]");
           for (int j = 0; j < brandEmiDetailInfo.size(); j++) {
               List<Object> emiChannelInfos = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "].emiChannelInfos[" + j + "]");
               for (int k = 0; k < emiChannelInfos.size(); k++) {
                   String data = emiChannelInfos.get(k).toString();
                   if (data.contains("channelCode=AMEX"))
                   {
                       Assertions.assertThat(data).contains("interestRate=12.5");
                       Assertions.assertThat(data).contains("ofMonths=6");
                       Assertions.assertThat(data).contains("interestRate=18.0");
                       Assertions.assertThat(data).contains("ofMonths=3");
                   }
                  if (data.contains("channelCode=HDFC"))
                   {
                       Assertions.assertThat(data).contains("interestRate=14.0");
                       Assertions.assertThat(data).contains("ofMonths=6");
                       Assertions.assertThat(data).contains("interestRate=16.0");
                       Assertions.assertThat(data).contains("ofMonths=12");
                       Assertions.assertThat(data).contains("interestRate=5.5");
                       Assertions.assertThat(data).contains("ofMonths=3");
                       Assertions.assertThat(data).contains("interestRate=15.0");
                       Assertions.assertThat(data).contains("ofMonths=9");
                   }
                   if (data.contains("channelCode=BAJAJFN"))
                   {
                       Assertions.assertThat(data).contains("interestRate=12.5");
                       Assertions.assertThat(data).contains("ofMonths=3");
                       Assertions.assertThat(data).contains("interestRate=13.0");
                       Assertions.assertThat(data).contains("ofMonths=9");
                   }
                   if (data.contains("channelCode=ICICI"))
                   {
                       Assertions.assertThat(data).contains("interestRate=5.5");
                       Assertions.assertThat(data).contains("ofMonths=3");
                    }
               }
           }
       }
    }
    @Test(description = "Verify without passing the brand code and passing DEFAULT  in the request body of getEmidetails API")
    public void Verify_passing_default()
    {
        String mid= Constants.MerchantType.EMI.getId();
        String body="\"body\":{\"mid\":\"qa8PG253346790628986\",\"brandCode\":[\"DEFAULT\"]}";
        ArrayList<String> brandcode=new ArrayList<>();
        brandcode.add("DEFAULT");
        Map<String,String> requestMap=new HashMap<>();
        requestMap.put("mid",mid);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid,brandcode));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.clientId","subvention");
        request.setContext("head.requestId","1234_111224344");
        Response response = request.execute();
        List<Object> brandEmiDetails= response.getBody().jsonPath().getList("body.brandEmiDetails");
        for(int i=0;i<brandEmiDetails.size();i++)
        {
            String brand=brandEmiDetails.get(i).toString();
            Assertions.assertThat(brand).contains("brandCode=DEFAULT");
        }
        for(int i=0;i<brandEmiDetails.size();i++) {
            List<Object> brandEmiDetailInfo = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "]");
            for (int j = 0; j < brandEmiDetailInfo.size(); j++) {
                List<Object> emiChannelInfos = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "].emiChannelInfos[" + j + "]");
                for (int k = 0; k < emiChannelInfos.size(); k++) {
                    String data = emiChannelInfos.get(k).toString();
                    if (data.contains("channelCode=ICICI")) {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                      }
                    if(data.contains("channelCode=HDFC"))
                    {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                }
            }
        }
    }
    @Test(description = "Verify passing only the brand code in the request body of getEMIdetails API")
    public void Verify_passing_brandcode()
    {
        String mid= Constants.MerchantType.EMI.getId();
        String body="\"body\":{\"mid\":\"qa8PG253346790628986\",\"brandCode\":[\"1707\"]}";
        ArrayList<String> brandcode=new ArrayList<>();
        brandcode.add("1707");
        Map<String,String> requestMap=new HashMap<>();
        requestMap.put("mid",mid);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid,brandcode));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.clientId","subvention");
        request.setContext("head.requestId","1234_111224344");
        Response response = request.execute();
        List<Object> brandemidetails= response.getBody().jsonPath().getList("body.brandEmiDetails");
        for(int i=0;i<brandemidetails.size();i++)
        {
            String brand=brandemidetails.get(i).toString();
            Assertions.assertThat(brand).contains("brandCode=1707");
        }
        for(int i=0;i<brandemidetails.size();i++) {
            List<Object> brandEmiDetailInfo = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "]");
            for (int j = 0; j < brandEmiDetailInfo.size(); j++) {
                List<Object> emiChannelInfos = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "].emiChannelInfos[" + j + "]");
                for (int k = 0; k < emiChannelInfos.size(); k++) {
                    String data = emiChannelInfos.get(k).toString();
                    if (data.contains("channelCode=AMEX"))
                    {
                        Assertions.assertThat(data).contains("interestRate=12.5");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=18.0");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                    if (data.contains("channelCode=HDFC"))
                    {
                        Assertions.assertThat(data).contains("interestRate=14.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=16.0");
                        Assertions.assertThat(data).contains("ofMonths=12");
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                    if (data.contains("channelCode=BAJAJFN"))
                    {
                        Assertions.assertThat(data).contains("interestRate=12.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=13.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                    if (data.contains("channelCode=ICICI"))
                    {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                }
            }
        }
    }
    @Test(description = "Verify without passing the brand code and not the DEFAULT in the request body of getemidetails API")
    public void Verify_without_passing_brandcode_default()
    {
        String mid= Constants.MerchantType.EMI.getId();
        String body="\"body\":{\"mid\":\"qa8PG253346790628986\"}";
        ArrayList<String> brandcode=new ArrayList<>();
        Map<String,String> requestMap=new HashMap<>();
        requestMap.put("mid",mid);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature, mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid,brandcode));
        GetEMIDetails request=new GetEMIDetails(getEMIDetailsRequest, mid);
        request.setContext("head.clientId","subvention");
        request.setContext("head.requestId","1234_111224344");
        Response response = request.execute();
        List<Object> brandemidetails= response.getBody().jsonPath().getList("body.brandEmiDetails");
        for(int i=0;i<brandemidetails.size();i++)
        {
            String brand=brandemidetails.get(i).toString();
            Assertions.assertThat(brand).contains("brandCode=DEFAULT");
        }
        for(int i=0;i<brandemidetails.size();i++) {
            List<Object> brandEmiDetailInfo = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "]");
            for (int j = 0; j < brandEmiDetailInfo.size(); j++) {
                List<Object> emiChannelInfos = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "].emiChannelInfos[" + j + "]");
                for (int k = 0; k < emiChannelInfos.size(); k++) {
                    String data = emiChannelInfos.get(k).toString();
                    if (data.contains("channelCode=ICICI")) {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                    if(data.contains("channelCode=HDFC"))
                    {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                }
            }
        }
    }
    @Test(description = "Verify passing multiple brand codes in the request body of getemidetailsapi")
    public void Verify_multiple_brandcodes_passed_in_request()
    {
        String mid=Constants.MerchantType.EMI.getId();
        int[] brandCodes = {1707,1708};
        String body="\"body\":{\"mid\":\"qa8PG253346790628986\",\"brandCode\":[\"1707\",\"1708\",\"DEFAULT\"]}\n";
        ArrayList<String>brandcode=new ArrayList<>();
        brandcode.add("1707");
        brandcode.add("1708");
        brandcode.add("DEFAULT");
        Map<String,String> requestmap=new HashMap<>();
        requestmap.put("mid",mid);
        GetEMIDetailsRequest getEMIDetailsRequest=new GetEMIDetailsRequest(signature,mid)
                .setHead(new Head(signature))
                .setBody(new Body(mid,brandcode));
        GetEMIDetails req=new GetEMIDetails(getEMIDetailsRequest,mid);
        req.setContext("head.clientId","subvention");
        req.setContext("head.requestId","1234_111224344");
        Response response=req.execute();
        List<Object> brandemidetails= response.getBody().jsonPath().getList("body.brandEmiDetails");
        for(int i=0;i<brandemidetails.size();i++)
        {
            String brand = brandemidetails.get(i).toString();
            Assertions.assertThat(brand).contains("brandCode="+brandCodes[i]);
        }
        for(int i=0;i<brandemidetails.size();i++) {
            List<Object> brandEmiDetailInfo = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "]");
            for (int j = 0; j < brandEmiDetailInfo.size(); j++) {
                List<Object> emiChannelInfos = response.getBody().jsonPath().getList("body.brandEmiDetails.brandEmiDetailInfo[" + i + "].emiChannelInfos[" + j + "]");
                for (int k = 0; k < emiChannelInfos.size(); k++) {
                    String data = emiChannelInfos.get(k).toString();
                    if (data.contains("channelCode=AMEX"))
                    {
                        Assertions.assertThat(data).contains("interestRate=12.5");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=18.0");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                    if (data.contains("channelCode=HDFC"))
                    {
                        Assertions.assertThat(data).contains("interestRate=14.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=16.0");
                        Assertions.assertThat(data).contains("ofMonths=12");
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                    if (data.contains("channelCode=BAJAJFN"))
                    {
                        Assertions.assertThat(data).contains("interestRate=12.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=13.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                    if (data.contains("channelCode=ICICI"))
                    {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                    if(data.contains("brandCode: 1708")) {
                        Assertions.assertThat(data).contains("channelCode= AMEX");
                        Assertions.assertThat(data).contains("interestRate=19.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                    }
                    if(data.contains("channelCode=ICICI"))
                    {
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                    }
                    if (data.contains("channelCode=HDFC"))
                    {
                        Assertions.assertThat(data).contains("interestRate=14.0");
                        Assertions.assertThat(data).contains("ofMonths=6");
                        Assertions.assertThat(data).contains("interestRate=16.0");
                        Assertions.assertThat(data).contains("ofMonths=12");
                        Assertions.assertThat(data).contains("interestRate=5.5");
                        Assertions.assertThat(data).contains("ofMonths=3");
                        Assertions.assertThat(data).contains("interestRate=15.0");
                        Assertions.assertThat(data).contains("ofMonths=9");
                    }
                }
            }
        }
    }
}
