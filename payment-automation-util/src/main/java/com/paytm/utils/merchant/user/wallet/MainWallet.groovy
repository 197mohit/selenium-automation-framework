package com.paytm.utils.merchant.user.wallet

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.util.exception.walletException.WalletException
import io.restassured.path.json.JsonPath

import static io.restassured.RestAssured.given

class MainWallet implements Wallet {

    private static String DEFAULT_MERCHANT_GUID = "125FD26C-4D98-11E2-B20C-E89A8FF309EA"
    private final User user

    MainWallet(User user) {
        this.user = user
    }

    @Override
    String getName() {
        'main'
    }

    @Override
    void plus(double amt) {
        if (!user.editable) throw new UnsupportedOperationException()
        def body = """
{
  "request": {
    "merchantGuid": "$DEFAULT_MERCHANT_GUID",
    "merchantOrderId": "${System.currentTimeMillis()}",
    "pgTxnId": "${System.currentTimeMillis()}",
    "requestType": "WEB",
    "requestWithoutTxn": false,
    "skipRefill": false,
    "bankName": "HDFC Bank",
    "bankTxnId": "${System.currentTimeMillis()}",
    "destination": "MAIN",
    "paymentMethod": "CC",
    "paymentMode": "CC",
    "txnAmount": "$amt",
    "txnCurrency": "INR",
    "txnStatus": "Success"
  },
  "metadata": "INR",
  "ipAddress": "219.65.43.2",
  "platformName": "PayTM",
  "operationType": "ADD_MONEY_VIA_MERCHANT"
}
"""
        String statusCode = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .spec(this.reqSpec)
                .headers(["ssotoken": user.tokens.collect().find { it.name == 'sso' }.id])
                .body(body)
                .post("/wallet-web/AddMoney").path('statusCode')
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
         "FOOD":"0",
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
                .headers(['ssotoken' : user.tokens.collect().find { it.name == 'wallet' }.id,
                          'is_admin' : 'false',
                          'tokentype': 'OAUTH'])
                .body(body)
                .post('/wallet-web/withdraw').path('statusCode')
        if (!"SUCCESS".equalsIgnoreCase(statusCode)) throw new WalletException("unable to remove amount from $this")
    }

    @Override
    WalletLimits getLimits() {
        new WalletLimits() {

            @Override
            WalletLimit getAdd() {
                return new WalletLimit() {
                    @Override
                    void breach() {
                        if (!user.editable) throw new UnsupportedOperationException()
                        String query = "select config from limit_config where limit_name = \"WALLET_AGGREGATE_BALANCE_LIMIT\" and pan_verified='0' and wallet_rbi_type='PAYTM_PRIME_WALLET';"
                        String dbString = DatabaseUtil.getInstance().executeSelectQuery(Constants.WALLET_DB_CONNECTION_URL, query)
                                .get(0).toString()
                        String jsonString = dbString.split('=')[1].trim().replaceFirst('}', '')
                        JsonPath jPath = new JsonPath(jsonString)
                        String balance = jPath.getString("balance")
                        MainWallet.this.balance = (balance as double)
                    }

                    @Override
                    void reset() {
                        if (!user.editable) throw new UnsupportedOperationException()
                        MainWallet.this.balance = 0
                    }
                }
            }

            @Override
            WalletLimit getRemove() {
                return new WalletLimit() {
                    @Override
                    void breach() {
                        throw new UnsupportedOperationException()
                    }

                    @Override
                    void reset() {
                        throw new UnsupportedOperationException()
                    }
                }
            }

            @Override
            WalletLimit getQuery() {
                return new WalletLimit() {
                    @Override
                    void breach() {
                        throw new UnsupportedOperationException()
                    }

                    @Override
                    void reset() {
                        throw new UnsupportedOperationException()
                    }
                }
            }
        }
    }

    @Override
    String toString() {
        'main-wallet'
    }
}
