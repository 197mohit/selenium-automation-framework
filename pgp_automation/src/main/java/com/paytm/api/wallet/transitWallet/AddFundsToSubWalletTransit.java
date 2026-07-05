package com.paytm.api.wallet.transitWallet;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class AddFundsToSubWalletTransit extends BaseApi {

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"merchantGuid\": \"125fd26c-4d98-11e2-b20c-e89a8ff309ea\",\n" +
            "        \"merchantOrderId\": \"{ORDERID}\",\n" +
            "        \"merchantSubWalletGuid\": \"AB4874FE-79E0-4C8A-8683-D9C063F99D8B\",\n" +
            "        \"salesWalletGuid\": \"\",\n" +
            "        \"payeeEmailId\": \"\",\n" +
            "        \"payeeSsoId\": \"\",\n" +
            "        \"payeePhoneNumber\": \"{MOBILE}\",\n" +
            "        \"amount\": 1,\n" +
            "        \"currencyCode\": \"INR\",\n" +
            "        \"userSubWalletType\": \"TRANSIT_BLOCK\" ,\n" +
            "        \"comment\":\"\"},\n" +
            "    \"metadata\": \"Testing\",\n" +
            "    \"ipAddress\": \"127.0.0.1\",\n" +
            "    \"platformName\": \"PayTM\",\n" +
            "    \"operationType\": \"SALES_TO_USER_CREDIT\"\n" +
            "}";


    public String getRequest()
    {
        return request;
    }

    public AddFundsToSubWalletTransit setRequest(String orderId, String amount, String cardNo, String mid, String mobile)
    {
        this.request= request
                .replace("{ORDERID}",orderId)
                .replace("{AMOUNT}",amount)
                .replace("{CARDNO}",cardNo)
                .replace("{MID}",mid)
                .replace("{MOBILE}",mobile)
        ;

        return this;
    }

    public AddFundsToSubWalletTransit(String orderId, String amount, String cardNo, Constants.MerchantType mid, User user)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath(Constants.WalletAPIResourcePath.TRANSIT_ADDFUNDSTOSUBWALLET);
        setRequest(orderId,amount,cardNo,mid.getId(),user.mobNo());

        getRequestSpecBuilder().setBody(getRequest());
    }

}
