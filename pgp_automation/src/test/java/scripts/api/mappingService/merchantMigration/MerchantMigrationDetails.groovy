package scripts.api.mappingService.merchantMigration

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.Acquiring
import com.paytm.utils.merchant.merchant.util.Preference
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.api.MappingService.MappingAlipayApi.getContractDetails
import static com.paytm.api.MappingService.MappingAlipayApi.getQueryEmi
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_MIGRATION_DETAILS
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class MerchantMigrationDetails extends TestSetUp {


    private final acquiringMap = ['nb': 'NET_BANKING', 'upi': 'UPI', 'cc': 'CREDIT_CARD', 'dc': 'DEBIT_CARD', 'emi': 'EMI', 'pdc': 'PAYTM_DIGITAL_CREDIT', 'emidc': 'EMI_DC', 'cod': 'MP_COD']
    private final gateways = ['hdfc', 'icici', 'icie', 'icio', 'icicidirect', 'codmock', 'paytmcc', 'axis', 'ppbl', 'amex', 'bajajfn', 'iciciidebit', 'icicipay', 'sbifss', 'bobfss', 'hdfo', 'hddo', 'citidirect']

    private final Map prefMap = [
            'STORE CARD DETAILS'               : 'save-card',
            'ADD_MONEY_ENABLED'                : 'addnpay',
            'HYBRID_ALLOWED'                   : 'hybrid',
            'CHECKSUM_ENABLED'                 : 'checksum',
            'WalletOnlyMerchant'               : 'wallet-only',
            'offlineMerchant'                  : 'offline',
            'AUTO_DEBIT'                       : 'auto-debit',
            'REFUND_DISABLE'                   : 'disable-refund',
            'PCF_FEE_INFO'                     : 'pcf-fee-info',
            'ONE_CLICK_SUPPORTED'              : 'one-click-supported',
            'nativeJsonRequest'                : 'native-json-request',
            'LOYALTY_VOUCHER_MANAGEMENT'       : 'mlv',
            'INSTANTREFUND_IMPS'               : 'imps-instant-refund',
            'INSTANTREFUND_VPA'                : 'vpa-instant-refund',
            'IS_VPA_ACCOUNT_VALIDATION_ALLOWED': 'vpa-account-validation',
            'BIN_IN_RESPONSE'                  : 'bin-in-response',
    ]

    private List<Preference> preferences

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(MERCHANT_MIGRATION_DETAILS)
                        .build()
        )
    }

    @Merchant
    @Test
    void 'test acquirings present on Merchant'() {

        def resp
        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then().extract().jsonPath().get('MERCHANT-ACQUIRING-INFO')['acquiringConfigInfos']
                    .collect {
                        new Acquiring(it.recordId, acquiringMap.collectEntries
                        { [(it.value): it.key] }[it.payMethod], it.serviceInstId.toLowerCase(), it.enableStatus as boolean)
                    }
        }
        VALIDATION:
        {
            assert resp == m().acquirings
        }
    }

    @Merchant
    @Test
    void 'test extendedInfo of Merchant'() {

        def resp
        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then().defaultParser(Parser.JSON).extract().
                    jsonPath().get('MERCHANT-EXTENDED-INFO.extendedInfo')
        }
        VALIDATION:
        {
            assert resp == m().extendedInfo
        }
    }

    @Merchant
    @Test
    void 'test merchantPreferenceInfos of Merchant'() {
        List<Preference> prefList

        API:
        {
            prefList = preferences ?: (preferences = {
                def respMerPref = req().pathParam('mid', m().getId())
                        .get().then().defaultParser(Parser.JSON).extract().jsonPath().get('MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos').collect {
                    prefMap[it.prefType] != null ? [(prefMap[it.prefType]): ['Y': true, 'YES': true, 'NO': false, 'N': false, 'DISABLED': false][it.prefValue]] : null
                }.findAll().collectEntries { [(it.keySet()[0]): it.values()[0]] }


                prefMap.collect { it.value }.collectEntries { [(it): (respMerPref[it] ?: false)] }.collect {
                    new Preference(it.key, it.value)
                }
            }())
        }
        VALIDATION:
        {
            assert prefList == m().preferences
        }
    }


    @Merchant
    @Test
    void 'test emi Config Infos Merchant'() {

        def resp
        def query

        QUERY:
        {
            query = getQueryEmi(m().alipayId)
        }

        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then().defaultParser(Parser.JSON)
                    .extract().jsonPath().get("MERCHANT-EMI-INFO.emiConfigInfos.issuingBank")
        }

        VALIDATION:
        {
            assert resp == query
        }

    }


    @Merchant
    @Test
    void 'test paymethods details for all contracts present on Migration Api'() {

        def query
        def resp
        def fetchContract
        List contracts = m().contracts.collect { it.id }

        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then().defaultParser(Parser.JSON)
                    .extract().jsonPath()
        }

        contracts.each {

            QUERY:
            {
                query = getContractDetails(it)['productCondition']['payMethods']
            }
            FETCH_CONTRACT:
            {
                fetchContract = resp.get('CONTRACT-DETAIL-' + it)['productCondition']['payMethods']

            }
            VALIDATION:
            {
                assert fetchContract == query
            }
        }

    }


    @Merchant
    @Test
    void 'test Contract Basic details for all contracts present on Migration Api'() {

        def query
        def resp
        def fetchContract
        List contracts = m().contracts.collect { it.id }

        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then()
        }

        contracts.each {

            QUERY:
            {
                query = getContractDetails(it)['contractBasic'] as Map
            }
            FETCH_CONTRACT:
            {
                fetchContract = resp.root('CONTRACT-DETAIL-' + it + '.contractBasic')

            }
            VALIDATION:
            {
                resp.body('contractStatus', equalTo(query.get('contractStatus')),
                        "productName", equalTo(query.get('productName')),
                        'merchantId', equalTo(query.get('merchantId')),
                        'contractId', equalTo(query.get('contractId')))
            }
        }

    }


    @Merchant
    @Test
    void 'test refund details for all contract present on Merchant Migration Api'() {

        def query
        def resp
        def fetchContract
        List contracts = m().contracts.collect { it.id }

        API:
        {
            resp = req().pathParam('mid', m().getId())
                    .get().then().defaultParser(Parser.JSON)
                    .extract().jsonPath()
        }

        contracts.each {

            QUERY:
            {
                query = getContractDetails(it)['productCondition']['refundOptions']
            }
            FETCH_CONTRACT:
            {
                fetchContract = resp.get('CONTRACT-DETAIL-' + it)['productCondition']['refundOptions']
            }
            VALIDATION:
            {
                assert fetchContract == query
            }


        }
    }


}