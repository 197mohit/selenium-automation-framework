package com.paytm.utils.merchant.api.wallet;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.wallet.dto.WalletDTO;
import io.restassured.http.ContentType;

public class AddMoneyAPI extends BaseApi {


    public AddMoneyAPI(String ssoToken, WalletDTO body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.WALLET_HOST);
        getRequestSpecBuilder().setBasePath("/wallet-web/AddMoney");
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
        getRequestSpecBuilder().setBody(body);
    }

}
