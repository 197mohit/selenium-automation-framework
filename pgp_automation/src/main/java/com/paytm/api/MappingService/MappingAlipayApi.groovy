package com.paytm.api.MappingService

import com.paytm.base.test.TestSetUp
import groovy.json.JsonSlurper
import io.restassured.path.json.JsonPath
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.ALIPAY
import static io.restassured.RestAssured.given

class MappingAlipayApi extends TestSetUp {


    static JsonPath getMerchantProfile(String alipayId) {
        def root = [
                'request'  : [
                        'head': [
                                'clientId'    : '2016030715243903536806',
                                'clientSecret': 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                'function'    : 'alipayplus.merchant.profile.queryMerchantInfo',
                                'reserve'     : ['attr1': 'val1'],
                                'reqTime'     : DateTime.now() as String,
                                'accessToken' : '234567a',
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'version'     : '1.2',
                        ],
                        'body': [
                                'merchantQueryType': 'BY_MERCHANT_ID',
                                'merchantId'       : alipayId,
                        ],
                ],
                'signature': 'no-signature',
        ]
        given().baseUri(ALIPAY).basePath('alipayplus/merchant/profile/queryMerchantInfo.htm').body(root).post()
                .then().extract().jsonPath().setRoot('response.body.merchantInfo')
    }


    static getMerchantAttribute(String alipayId) {
        def root = [
                'request'  : [
                        'head': [
                                'clientId'    : '2016030715243903536806',
                                'clientSecret': 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                'function'    : 'alipayplus.merchant.attribute.query',
                                'reqTime'     : DateTime.now() as String,
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'version'     : '1.2',
                        ],
                        'body': [
                                'attributeKeys': ['merchantPreference'],
                                'merchantId'   : alipayId,
                        ],
                ],
                'signature': 'no-signature',
        ]
        new JsonSlurper().parseText(
                given().baseUri(ALIPAY).basePath('alipayplus/merchant/attribute/query.htm').body(root).post()
                        .then().extract().jsonPath().setRoot('response.body.attributeInfos.attributeValue').get())
    }


    static getQueryEmi(String alipayId) {
        def root = [
                'request'  : [
                        'head': [
                                'clientId'    : '2016030715243903536806',
                                'clientSecret': 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                'function'    : 'alipayplus.merchant.config.queryEmis',
                                'reqTime'     : DateTime.now() as String,
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'version'     : '1.2',
                        ],
                        'body': [
                                'attributeKeys': ['merchantPreference'],
                                'merchantId'   : alipayId,
                        ],
                ],
                'signature': 'no-signature',
        ]
        given().baseUri(ALIPAY).basePath('alipayplus/merchant/config/queryEmis.htm').body(root).post()
                .then().extract().jsonPath().setRoot('response.body.emiConfigInfos.issuingBank').get()
    }


    static getContractDetails(String contractId) {
        def root = [
                'request'  : [
                        'head': [
                                'clientId'    : '2016030715243903536806',
                                'clientSecret': 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                'function'    : 'alipayplus.boss.contract.details.query',
                                'reqTime'     : DateTime.now() as String,
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'version'     : 'fixed-a',
                        ],
                        'body': [
                                'attributeKeys': [],
                                'contractId'   : contractId,
                        ],
                ],
                'signature': 'no-signature',
        ]
        given().baseUri(ALIPAY).basePath('alipayplus/boss/contract/details/query.htm').body(root).post()
                .then().extract().jsonPath().setRoot('response.body').get()
    }


}

