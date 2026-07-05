package scripts.SolidityCheck;

import com.paytm.LocalConfig;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.utils.DatabaseUtil;
import io.qameta.allure.Owner;

/**
 * Created by anjukumari on 20/06/18
 */
@Owner("Gagandeep")
public class BankDetailsQueries extends PGPBaseTest{
    String  bankName;
    String[]  payMethodList;
    String[] channelList;
    String bankId;
    String channelId;
    String payMethodId;

    String sql_update_query;
    String sql_query;
    String sql_query_web_response;
    String channelId_query;
    String bankId_query;
    String payMethodId_query;


    BankDetailsQueries(){

        sql_update_query="update BANK_URL_INFO set {?}='{?}'"
                +" where BANK_ID='{?}' " + " and PAY_METHOD_ID='{?}' and CHANNEL_ID='{?}'";

        sql_query="select BANK_ID,CHANNEL_ID,PAY_METHOD_ID,WEB_PAY_URL,WEB_RESPONSE_URL,REFUND_URL " +
                "from BANK_URL_INFO where" + " BANK_ID='{?}' and PAY_METHOD_ID='{?}' and CHANNEL_ID='{?}'";

        sql_query_web_response="select WEB_RESPONSE_URL " +
                "from BANK_URL_INFO where" + " BANK_ID='{?}' and PAY_METHOD_ID='{?}' and CHANNEL_ID='{?}'";

        channelId_query = "select ID from LOOKUP_DATA where NAME = '{?}'";
        bankId_query = "select BANK_ID from BANK_MASTER where BANK_CODE ='{?}'";
        payMethodId_query = "select ID from LOOKUP_DATA where NAME ='{?}'";

    }

    public static String getUpdatedStaring(String ...args){
        for(int i=1;i<=args.length-1;i++){
            args[0]  = args[0].replaceFirst("\\{\\?\\}",args[i]);
        }
        return args[0];

    }


    public void setBankDetails(String channel, String bank, String payMethod) {
        try {
            payMethodId = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(payMethodId_query, payMethod)).get(0).get("ID").toString();
            channelId = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(channelId_query, channel)).get(0).get("ID").toString();
            bankId = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bankId_query, bankName)).get(0).get("BANK_ID").toString();
        }catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Data not found in DB for channel:"+ channel+", bank:"+ bank+ " and payMethod:"+payMethod);
        }

    }





}
