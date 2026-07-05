package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Preferences implements GList<Preference> {

    private final Merchant m
    private final Map map = [
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
            'BANK_TRANSFER_ENABLED'            : 'bank-transfer',
            'DYNAMIC_CHARGE_TARGET'            : 'dynamic-charge-target',
            'CHECK_UPI_ACCOUNT_EXISTS'         : 'check-upi-account-exists',
            'LOCALE_ENABLE'                    : 'locale-enabled',
            'ADDANDPAY_WITH_UPI_COLLECT'       : 'addnpay-with-upi-collect',
    ]
    private List<Preference> preferences

    Preferences(Merchant m) {
        this.m = m
    }

    Preference saveCard() { this['save-card'] }

    Preference getSaveCard() { this['save-card'] }

    Preference addnpay() { this['addnpay'] }

    Preference getAddnpay() { this['addnpay'] }

    Preference hybrid() { this['hybrid'] }

    Preference getHybrid() { this['hybrid'] }

    Preference checksum() { this['checksum'] }

    Preference getChecksum() { this['checksum'] }

    Preference walletOnly() { this['wallet-only'] }

    Preference getWalletOnly() { this['wallet-only'] }

    Preference offline() { this['offline'] }

    Preference getOffline() { this['offline'] }

    Preference autoDebit() { this['auto-debit'] }

    Preference getAutoDebit() { this['auto-debit'] }

    Preference disableRefund() { this['disable-refund'] }

    Preference getDisableRefund() { this['disable-refund'] }

    Preference pcfFeeInfo() { this['pcf-fee-info'] }

    Preference getPcfFeeInfo() { this['pcf-fee-info'] }

    Preference getOneClickSupported() { this['one-click-supported'] }

    Preference getNativeJsonRequest() { this['native-json-request'] }

    Preference getMlv() { this['mlv'] }

    Preference getImpsInstantRefund() { this['imps-instant-refund'] }

    Preference getVpaInstantRefund() { this['vpa-instant-refund'] }

    Preference getVpaAccountValidation() { this['vpa-account-validation'] }

    Preference getBinInResponse() { this['bin-in-response'] }

    Preference getBankTransfer() { this['bank-transfer'] }

    Preference getDynamicChargeTarget() { this['dynamic-charge-target'] }

    Preference getCheckUPIAccountExists() { this['check-upi-account-exists'] }

    Preference isLocaleEnabled() { this['locale-enabled'] }

    Preference isAddNPayWithUPICollect() { this['addnpay-with-upi-collect'] }

    Preference getAt(String preference) {
        this.find { it.name == preference }
    }

    @Override
    Iterator<Preference> iterator() {
        return new Iterator<Preference>() {
            List<Preference> list = preferences ?: (preferences = {
                def preferences = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/merchant/get/preference/info/$m.id").get().path('merchantPreferenceInfos').collect {
                    Preferences.this.map[it.prefType] != null ? [(Preferences.this.map[it.prefType]): ['Y': true, 'YES': true, 'NO': false, 'N': false, 'DISABLED': false][it.prefValue]] : null
                }.findAll().collectEntries { [(it.keySet()[0]): it.values()[0]] }
                map.collect { it.value }.collectEntries { [(it): (preferences[it] ?: false)] }.collect {
                    new Preference(it.key, it.value)
                }
            }())
            int index = 0

            @Override
            boolean hasNext() {
                return list.size() > index
            }

            @Override
            Preference next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Preference> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        preferences = null
        def root = [
                merchantId             : m.id,
                merchantPreferenceInfos: c.collect { Preference preference ->
                    [
                            prefType  : map.collectEntries { [(it.value): it.key] }[preference.name],
                            prefStatus: 'ACTIVE',
                            prefValue : 'Y'
                    ]
                }
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/mapping-service/merchant/add/preference/info/').body(root).post().path('response.resultStatus') == 'S'
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        preferences = null
        def root = [
                merchantId             : m.id,
                merchantPreferenceInfos: c.collect { Preference preference ->
                    [
                            prefType  : map.collectEntries { [(it.value): it.key] }[preference.name],
                            prefStatus: 'ACTIVE',
                            prefValue : 'N'
                    ]
                }
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/mapping-service/merchant/add/preference/info/').body(root).post().path('response.resultStatus') == 'S'
    }
}
