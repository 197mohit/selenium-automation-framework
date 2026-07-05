package merchantCreationScript;

import com.paytm.utils.merchant.util.PGPUtil;

/**
 * Created by anjukumari on 21/01/19
 */
public class ValidateChecksumSendToMerchant {
    public void test(){
        PGPUtil util = new PGPUtil();
        util.validateChecksumFromPaytmString
                ("WALLET|1708147|INR|WALLET|HNA8RH22934202716700|PARCEL424164|PPI|01|Txn Success|TXN_SUCCESS|2.00|2019-01-18 19:08:01.0|20190118111212800110168239500446667|", "IApsOx#%0MZts6a@","bTcVFfKPIMOHimwqTMSbYR/05qKe7m98F/5dSXGdtTNVHF75xgAjFLJuqWFQVWYnuWbfN0EH3P99qcK+j4KgCXDIHSLtPAM5VbJyRtE9AwE=");
    }

}
