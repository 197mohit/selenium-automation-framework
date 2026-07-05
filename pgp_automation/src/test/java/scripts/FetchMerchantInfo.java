package scripts;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.ValidateAndFetchMerchantInfoAPI;
import com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request.Body;
import com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request.Head;
import com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request.RequestBody;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.*;

@Owner("Deepak")
public class FetchMerchantInfo extends PGPBaseTest {

    @Test(description="Verify Successfull Merchant Info for Multiple Valid Mid")
    public void verifyMerchantInfowithValidMid() {
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String msg="";
        String RespCode="";
        List<String> mids = new ArrayList<String>();
        mids.add(Constants.MerchantType.AddMoney.getId());
        mids.add(Constants.MerchantType.PGOnly.getId());
        List<String> mid_resp = new ArrayList<>();
        try {
            map.put("mids",Constants.MerchantType.AddMoney.getId()+","+Constants.MerchantType.PGOnly.getId());
            jwttoken = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY);
            Body body = new Body();
            Head head = new Head();
            body.setMids(mids);
            head.setToken(jwttoken);
            RequestBody requestBody = new RequestBody() ;
            requestBody.setBody(body);
            requestBody.setHead(head);
            ValidateAndFetchMerchantInfoAPI infoAPI = new ValidateAndFetchMerchantInfoAPI(requestBody);
            JsonPath jsonPath = infoAPI.execute().jsonPath();
            if (!(jsonPath == null)) {
                msg = jsonPath.getString("body.resultInfo.resultMsg");
                RespCode = jsonPath.getString("body.resultInfo.resultCode");
                List<Map<String, String>> list = jsonPath.getList("body.merchantBaseInfoList");

                for (int i = 0; i <list.size() ; i++) {
                    mid_resp.add(list.get(i).get("mid"));
                }
                Collections.sort(mid_resp);
                Collections.sort(mids);
            }
            Assertions.assertThat(mid_resp).isEqualTo(mids);
            Assertions.assertThat(msg).isEqualTo("Success");
            Assertions.assertThat(RespCode).isEqualTo("00000");
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
    @Test(description="Invalid Mid Passed in Request Body")
    public void invalidMidMerchantInfo(){
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String msg="";
        String RespCode="";
        List<String> mids = new ArrayList<>();
        mids.add("INVALIDMID");
        try {
            map.put("mids", "INVALIDMID");
            jwttoken = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts,LocalConfig.JWT_KEY);
            Body body = new Body();
            Head head = new Head();
            body.setMids(mids);
            head.setToken(jwttoken);
            RequestBody requestBody = new RequestBody() ;
            requestBody.setBody(body);
            requestBody.setHead(head);
            ValidateAndFetchMerchantInfoAPI infoAPI = new ValidateAndFetchMerchantInfoAPI(requestBody);
            JsonPath jsonPath = infoAPI.execute().jsonPath();
            if (!(jsonPath == null)) {
                msg = jsonPath.getString("body.resultInfo.resultMsg");
                RespCode = jsonPath.getString("body.resultInfo.resultCode");

            }
            Assertions.assertThat(msg).isEqualTo("PROCESS FAIL");
            Assertions.assertThat(RespCode).isEqualTo("PROCESS_FAIL");
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Test(description="Verify Merchant Info with Single MID")
    public void verifyMerchantInfowithSingleValidMid() {
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String msg="";
        String RespCode="";
        List<String> mids = new ArrayList<>();
            List<String> mid_resp = new ArrayList<>();
        try {  mids.add(Constants.MerchantType.AddnPay.getId());
            map.put("mids", Constants.MerchantType.AddnPay.getId());
            jwttoken = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts,LocalConfig.JWT_KEY);
            Body body = new Body();
            Head head = new Head();
            body.setMids(mids);
            head.setToken(jwttoken);
            RequestBody requestBody = new RequestBody() ;
            requestBody.setBody(body);
            requestBody.setHead(head);
            ValidateAndFetchMerchantInfoAPI infoAPI = new ValidateAndFetchMerchantInfoAPI(requestBody);
            JsonPath jsonPath = infoAPI.execute().jsonPath();
            if (!(jsonPath == null)) {
                msg = jsonPath.getString("body.resultInfo.resultMsg");
                RespCode = jsonPath.getString("body.resultInfo.resultCode");
                List<Map<String, String>> list = jsonPath.getList("body.merchantBaseInfoList");

                for (int i = 0; i < list.size(); i++) {
                    mid_resp.add(list.get(i).get("mid")) ;
                }
                Collections.sort(mid_resp);
                Collections.sort(mids);

            }
            Assertions.assertThat(mid_resp).isEqualTo(mids);
            Assertions.assertThat(msg).isEqualTo("Success");
            Assertions.assertThat(RespCode).isEqualTo("00000");
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

}
