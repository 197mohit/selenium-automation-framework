package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.FetchVPADetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchVPADetails.FetchVPADetailsDTO;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import com.paytm.utils.merchant.util.PgpRedisUtil;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Owner("Deepak")
public class FetchVPADetails extends PGPBaseTest {

//    private static final String rediUrl = LocalConfig.PG_REDIS_URI;

    private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }

    @Test(description = "Verify success response of fetch VPA detail APIs when valid txn_token is passed.")
    public void TC_FVD_001() throws Exception {
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPACHECKED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder
                (user.ssoToken(), Constants.MerchantType.PPBLC_ONLY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String trxToken = jsonPath.getString("body.txnToken");
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder(trxToken).setChannelId(ChannelId).
                build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO, initTxnDTO);
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        CommonHelpers.assertCheck(fetchVPAJson, new Object[]{
                "body.resultInfo.resultStatus", "S",
                "body.resultInfo.resultCode", "0000",
                "body.resultInfo.resultMsg", "Success",
                "body.sarvatraUserProfile.status", "success"
        });
        Assert.assertFalse(fetchVPAJson.getList
                ("body.sarvatraUserProfile.response.vpaDetails.defaultCredit.credsAllowed.credsAllowedType").
                isEmpty());
        Assert.assertFalse(fetchVPAJson.getList
                ("body.sarvatraUserProfile.response.vpaDetails.defaultDebit.credsAllowed.credsAllowedType").
                isEmpty());
        Assertions.assertThat(fetchVPAJson.get("body.sarvatraUserProfile.response.vpaDetails.defaultCredit.invalidVpa")
                .toString().equals("false"));
        Assertions.assertThat(fetchVPAJson.get("body.sarvatraUserProfile.response.vpaDetails.defaultDebit.invalidVpa")
                .toString().equals("false"));
    }

    @Test(description="Verify failed response of fetch VPA detail when txn_token is passed blank")
    public void TC_FVD_002() throws Exception{
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder("").setChannelId(ChannelId).build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO,initTxnDTO);
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        CommonHelpers.assertCheck(fetchVPAJson,new Object[]{
                "body.resultInfo.resultStatus","F",
                "body.resultInfo.resultCode","1006",
                "body.resultInfo.resultMsg", "Your Session has expired."});
     Assertions.assertThat(fetchVPAJson.getList("body.sarvatraUserProfile.response.vpaDetails")).isNull();
    }


    @Test(description="Verify failed response of fetch VPA detail when txn_token is passed null")
    public void TC_FVD_003() throws Exception{
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder(null).setChannelId(ChannelId).build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO,initTxnDTO);
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        CommonHelpers.assertCheck(fetchVPAJson,new Object[]{
                "body.resultInfo.resultStatus","F",
                "body.resultInfo.resultCode","1006",
                "body.resultInfo.resultMsg", "Your Session has expired."});
        Assertions.assertThat(fetchVPAJson.getList("body.sarvatraUserProfile.response.vpaDetails")).isNull();
    }


    @Test(description="Verify failed response of fetch VPA detail when different MID is passed")
    public void TC_FVD_004() throws Exception{
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String trxToken = jsonPath.getString("body.txnToken");
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder(trxToken).setChannelId(ChannelId).build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO, Constants.MerchantType.PPBLC_ONLY.getId(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        CommonHelpers.assertCheck(fetchVPAJson,new Object[]{
                "body.resultInfo.resultStatus","F",
                "body.resultInfo.resultCode","2013",
                "body.resultInfo.resultMsg", "Mid in the query param doesn't match with the Mid send in the request"
        });
        Assertions.assertThat(fetchVPAJson.getList("body.sarvatraUserProfile.response.vpaDetails")).isNull();

    }

    @Test(description="Verify failed response of fetch VPA detail when different OrderId is passed")
    public void TC_FVD_005() throws Exception {
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String trxToken = jsonPath.getString("body.txnToken");
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder(trxToken).setChannelId(ChannelId).build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO,initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId().substring(0,5));
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        CommonHelpers.assertCheck(fetchVPAJson,new Object[]{
                "body.resultInfo.resultStatus","F",
                "body.resultInfo.resultCode","2014",
                "body.resultInfo.resultMsg", "OrderId in the query param doesn't match with the OrderId send in the request"
        });
        Assertions.assertThat(fetchVPAJson.getList("body.sarvatraUserProfile.response.vpaDetails")).isNull();

    }

    @Test(description = "Verify failed response of fetch VPA detail APIs when txn_token is deleted from Redis and passed .")
    public void TC_FVD_006() throws Exception {
        String ChannelId="WEB";
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder
                (user.ssoToken(), Constants.MerchantType.PPBLC_ONLY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String trxToken = jsonPath.getString("body.txnToken");
        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(initTxnDTO.getBody().getMid());
        String alipayId = dbResult.get("alipay_merchant_id").toString();
        String redisKey = "MID_A_P_" + alipayId;
//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null)
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
        FetchVPADetailsDTO fetchVPADetailsDTO = new FetchVPADetailsDTO.Builder(trxToken).setChannelId(ChannelId).
                build();
        FetchVPADetail fetchVPADetail = new FetchVPADetail(fetchVPADetailsDTO, initTxnDTO);
        JsonPath fetchVPAJson = fetchVPADetail.execute().jsonPath();
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchVPAJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assert.assertTrue(fetchVPADetail.execute().asString().contains("sarvatraVpa"));
    }

}
