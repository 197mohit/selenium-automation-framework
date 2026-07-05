package com.paytm.utils.merchant.user.wallet

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.util.exception.walletException.WalletException

import static io.restassured.RestAssured.given

class FoodWallet implements Wallet {

    private static final String DEFAULT_MERCHANT_GUID = "125FD26C-4D98-11E2-B20C-E89A8FF309EA"
    private static final String DEFAULT_MERCHANT_SUBWALLET_GUID = "6E05B037-21CC-4919-87C8-E531DD08E33D"

    private final User user

    FoodWallet(User user) {
        this.user = user
    }

    @Override
    String getName() {
        'food'
    }

    @Override
    void plus(double amt) {
        if (!user.editable) throw new UnsupportedOperationException()
        def body = """
{
  "request": {
    "merchantGuid": "$DEFAULT_MERCHANT_GUID",
    "merchantOrderId": "${System.currentTimeMillis()}",
    "merchantSubWalletGuid": "$DEFAULT_MERCHANT_SUBWALLET_GUID",
    "payeePhoneNumber": "$user.mobile",
    "amount": "$amt",
    "currencyCode": "INR",
    "userSubWalletType": "FOOD"
  },
  "metadata": "Testing",
  "ipAddress": "127.0.0.1",
  "platformName": "PayTM",
  "operationType": "SALES_TO_USER_CREDIT"
}
"""
        String statusCode = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .spec(reqSpec)
                .body(body)
                .when()
                .post("/wallet-web/addFundsToSubWallet").path('statusCode')
        if (!"SUCCESS".equalsIgnoreCase(statusCode)) throw new WalletException("unable to add amount to $this")
    }

    @Override
    void minus(double amt) {
        if (!user.editable) throw new UnsupportedOperationException()
        def body = """
{  
   "request":{  
      "skipRefill":true,
      "totalAmount": "$amt",
      "currencyCode":"INR",
      "merchantGuid":"$DEFAULT_MERCHANT_GUID",
      "industryType":"Retail",
      "merchantOrderId":"${System.currentTimeMillis()}",
      "pgTxnId":"${System.currentTimeMillis()}",
      "subWalletAmount":{  
         "FOOD":"$amt",
         "GIFT":"0",
         "TOLL":"0"
      }
   },
   "platformName":"PayTM",
   "ipAddress":"192.168.40.11",
   "operationType":"WITHDRAW_MONEY"
}
"""
        String statusCode = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .spec(reqSpec)
                .headers(['ssotoken' : user.tokens.collect().find { it.name == 'sso' }.id,
                          'is_admin' : 'false',
                          'tokentype': 'OAUTH'])
                .body(body)
                .post("/wallet-web/withdraw").path('statusCode')
        if (!"SUCCESS".equalsIgnoreCase(statusCode)) throw new WalletException("unable to remove amount from $this")
    }

    @Override
    WalletLimits getLimits() {
        return null
    }

    @Override
    String toString() {
        'food-wallet'
    }
}
