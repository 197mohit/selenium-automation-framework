package com.paytm.utils.merchant.limit;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.policy.Policy;
import com.google.gson.JsonObject;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.utils.AerospikeUtil;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import org.luaj.vm2.ast.Str;
import org.testng.Assert;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anjukumari on 16/02/18
 */
public class MerchantVelocityLimit {


    private static final String VELOCITY_NAMESPACE = "pg-transient-expiry";
    private static final String VELOCITY_SETS_NAME = "pgpVelocityLimits";

    public enum LIMITDURATION {

        DAY, WEEK, MONTH;
    }

    public enum LIMIT_TYPE_DB {
        MAXAMTPERTXN, MAXAMTPERDAY, MAXAMTPERWEEK, MAXAMTPERMON, MAXTXNPERDAY, MAXTXNPERWEEK, MAXTXNPERMON
    }

    public enum LIMITTYPE {AMOUNT, COUNT}

    public enum OPERATION {ADD, SUBTRACT, DELETE}


    private String checkCurrentLimitValue(String mid, LIMITDURATION duration, String limitType, String host, int port) {
        AerospikeClient client = AerospikeUtil.getInstance().getConnection(host, port);
        Key key = new Key(VELOCITY_NAMESPACE, VELOCITY_SETS_NAME, getVelocityLimitKey(mid, duration));
        try {
            String currentLimitValue = client.get(new Policy(), key).bins.get(limitType.toString().toLowerCase()).toString();
            return currentLimitValue;
        }catch (NullPointerException e){
            return "0";
        }
    }


    public void editLimitInAerospike(LIMITTYPE limittype, LIMITDURATION duration, String mid, String aerospikeHost, int aerospikePort) {
        String getLimitFromDB = "select * from ENTITY_TXN_LIMITS etl,ENTITY_INFO ef,LOOKUP_DATA  ld where etl.ENTITY_ID=ef.ID and ef.MID='" + mid + "' and etl.LIMITS_IDENTIFIER=ld.LOOKUP_ID and ld.NAME='PER_MID';\n";
        String Current = checkCurrentLimitValue(mid, duration, limittype.toString(), aerospikeHost, aerospikePort);
        List StandardLimitList = DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, getLimitFromDB);
        LIMIT_TYPE_DB limit_type_db = null;
        String amount = null;
        if (limittype.equals(LIMITTYPE.AMOUNT)) {

            switch (duration) {
                case WEEK:
                    limit_type_db = LIMIT_TYPE_DB.MAXAMTPERWEEK;
                    break;
                case DAY:
                    limit_type_db = LIMIT_TYPE_DB.MAXAMTPERDAY;
                    break;
                case MONTH:
                    limit_type_db = LIMIT_TYPE_DB.MAXAMTPERMON;
                    break;

            }
            Double StandardLimitAmount = ((HashMap<String, Double>) StandardLimitList.get(0)).get(limit_type_db);
            Double finalCurrentAmount = Double.parseDouble(Current) / 100;
            Double amount1 = StandardLimitAmount > finalCurrentAmount ? (StandardLimitAmount - finalCurrentAmount) : 0.0;
            amount = amount1.toString();

        } else if (limittype.equals(LIMITTYPE.COUNT)) {
            switch (duration) {
                case MONTH:
                    limit_type_db = LIMIT_TYPE_DB.MAXTXNPERMON;
                    break;
                case WEEK:
                    limit_type_db = LIMIT_TYPE_DB.MAXTXNPERWEEK;
                    break;
                case DAY:
                    limit_type_db = LIMIT_TYPE_DB.MAXTXNPERDAY;
                    break;
            }
            Long StandardLimitCount = ((HashMap<String, Long>) StandardLimitList.get(0)).get(limit_type_db);
            Long finalCurrentCount = Long.valueOf(Current);
            Long amount2 = StandardLimitCount > finalCurrentCount ? (StandardLimitCount - finalCurrentCount) : 0;
            amount = amount2.toString();
        }

        JsonObject json = createJsonBody(limittype, duration, mid, amount.toString(), OPERATION.ADD);
        new changeLimit(json).execute();
    }

    public void breachLimitAmount(String mid, LIMITDURATION duration){
        String updateValue = null;
        String key = "select ENTITY_TXN_ID   from  ENTITY_TXN_LIMITS where ENTITY_ID = ( select ID from ENTITY_INFO where mid='" + mid + "' ) and LIMITS_IDENTIFIER=(select LOOKUP_ID from LOOKUP_DATA where VALUE='PER_MID');";
        String ENTITY_TXN_ID =  DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, key).get(0).get("ENTITY_TXN_ID").toString();
        switch (duration){
            case DAY: updateValue = "MAXAMTPERDAY=500,  MAXAMTPERWEEK=1000,  MAXAMTPERMON=1500";
            break;
            case WEEK: updateValue = "MAXAMTPERDAY=1500,  MAXAMTPERWEEK=500,  MAXAMTPERMON=1000";
            break;
            case MONTH:updateValue = "MAXAMTPERDAY=1500,  MAXAMTPERWEEK=1000,  MAXAMTPERMON=500";
        }
        String updateLimit = "update ENTITY_TXN_LIMITS set "+updateValue+" where ENTITY_TXN_ID = "+ENTITY_TXN_ID+";";
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, updateLimit);
        RedisUtil.getInstance().getConnection(Constants.PG_REDIS_URI).flushAll();
    }

    public void breachLimitCount(String mid, LIMITDURATION duration){
        String updateValue = null;
        String key = "select ENTITY_TXN_ID   from  ENTITY_TXN_LIMITS where ENTITY_ID = ( select ID from ENTITY_INFO where mid='" + mid + "' ) and LIMITS_IDENTIFIER=(select LOOKUP_ID from LOOKUP_DATA where VALUE='PER_MID');";
        String ENTITY_TXN_ID =  DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, key).get(0).get("ENTITY_TXN_ID").toString();
        switch (duration){
            case DAY: updateValue = "MAXAMTPERDAY=50,  MAXAMTPERWEEK=100,  MAXAMTPERMON=150";
            break;
            case WEEK:updateValue = "MAXAMTPERDAY=150,  MAXAMTPERWEEK=50,  MAXAMTPERMON=100";
            break;
            case MONTH:updateValue = "MAXAMTPERDAY=50,  MAXAMTPERWEEK=150,  MAXAMTPERMON=50";
        }
        String updateLimit = "update ENTITY_TXN_LIMITS set "+updateValue+" where ENTITY_TXN_ID = "+ENTITY_TXN_ID+";";
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, updateLimit);
        RedisUtil.getInstance().getConnection(Constants.PG_REDIS_URI).flushAll();
    }


    public void resetLimit(String mid, LIMITTYPE limittype) {
        JsonObject jsonBody = createJsonBody(limittype, LIMITDURATION.MONTH, mid, "1.0", OPERATION.DELETE);
        Response response = new changeLimit(jsonBody).execute();
        boolean value = response.jsonPath().get("response.limitBreached");
        Assert.assertEquals(value,false);
        String key = "select ENTITY_TXN_ID  from  ENTITY_TXN_LIMITS where ENTITY_ID = ( select ID from ENTITY_INFO where mid='"+mid+"' ) and LIMITS_IDENTIFIER=(select LOOKUP_ID from LOOKUP_DATA where VALUE='PER_MID');";
        String ENTITY_TXN_ID =  DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, key).get(0).get("ENTITY_TXN_ID").toString();
        String resetLimit = "update ENTITY_TXN_LIMITS set MAXAMTPERDAY=5000,  MAXAMTPERWEEK=10000,  MAXAMTPERMON=15000 where ENTITY_TXN_ID = "+ENTITY_TXN_ID+";";
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, resetLimit);
        RedisUtil.getInstance().getConnection(Constants.PG_REDIS_URI).flushAll();

    }


    public Response checkLimit(String mid){
        checkLimit limit = new checkLimit(mid);
        Response response = limit.execute();
        return response;
    }


    public boolean checkLimitBreached(String mid) {
        checkLimit limit = new checkLimit(mid);
        Response response = limit.execute();
        System.out.println(response.toString());
        boolean value = response.jsonPath().get("response.limitsConfigured");
        return value;
    }

    private String getLimitDurations(String duration) {
        Date date = new Date();
        String durationWithPrefix = null;
        int i=0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (duration.equals("DAY")) {
            i = cal.get(Calendar.DAY_OF_MONTH);
        } else if (duration.equals("WEEK")) {
           i = cal.get(Calendar.WEEK_OF_MONTH);

        } else if (duration.equals("MONTH")) {
            i = cal.get(Calendar.MONTH);// 0 INDEXED MONTH LATER INCREMENTE WITH 1
            i++;
        }
        durationWithPrefix = duration + i;
        return durationWithPrefix;

    }

    private JsonObject createJsonBody(LIMITTYPE limitType, LIMITDURATION limitDuration, String mid, String amount, OPERATION operationType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        String currentDate = dateFormat.format(new Date());
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("txnType", "ANY");
        jsonBody.addProperty("limitType", limitType.name());
        jsonBody.addProperty("limitDuration", limitDuration.toString());
        jsonBody.addProperty("mid", mid);
        jsonBody.addProperty("operationValue", amount );
        jsonBody.addProperty("operation", operationType.name());
        jsonBody.addProperty("date", currentDate);
        return jsonBody;
    }


    private String getVelocityLimitKey(String mid, LIMITDURATION duration) {
        String PREFIX = "vel";
        String DELIMITER = "/";
        String TxnType = "ANY";
        String durationWithPrefix = getLimitDurations(duration.toString());
        StringBuilder buildKey = new StringBuilder();
        buildKey.append(PREFIX).append(DELIMITER)
                .append(mid).append(DELIMITER)
                .append(durationWithPrefix).append(DELIMITER)
                .append(TxnType);
        return buildKey.toString();
    }


}

class checkLimit extends BaseApi {
    public checkLimit(String mid) {
        String jsonBody = "{ \"mid\":\"" + mid + "\" }";
        setMethod(BaseApi.MethodType.POST);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setBasePath("/velocity/checklimit");
        requestSpecBuilder.setBaseUri(Constants.ADMIN_SERVER_ADDRESS);
        requestSpecBuilder.setContentType("application/json");
        requestSpecBuilder.setBody(jsonBody);
    }
}

class changeLimit extends BaseApi {
    public changeLimit(JsonObject jsonBody) {
        setMethod(BaseApi.MethodType.POST);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setBasePath("/velocity/changelimit");
        requestSpecBuilder.setBaseUri(Constants.ADMIN_SERVER_ADDRESS);
        requestSpecBuilder.setContentType("application/json");
        requestSpecBuilder.setBody(jsonBody.toString());
    }
}



