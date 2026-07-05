package com.paytm.utils.merchant

import com.paytm.framework.conditions.CList
import com.paytm.framework.conditions.CString
import com.paytm.framework.conditions.Condition
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.util.PGPUtil

class Peon {

    private Map map

    Peon(Map map) {
        this.map = map
    }

    CString bankName() { this['BANKNAME'] }

    CString bankTxnId() { this['BANKTXNID'] }

    CString currency() { this['CURRENCY'] }

    CString custId() { this['CUSTID'] }

    CString gatewayName() { this['GATEWAYNAME'] }

    CString mercUnqRef() { this['MERC_UNQ_REF'] }

    CString mId() { this['MID'] }

    CString orderId() { this['ORDERID'] }

    CString payMode() { this['PAYMENTMODE'] }

    CString respCode() { this['RESPCODE'] }

    CString respMsg() { this['RESPMSG'] }

    CString status() { this['STATUS'] }

    CString Comments() { this['comments'] }

    CString maskedCustomerMobileNumber() { this['Masked_customer_mobile_number'] }

    CString txnType() { this['TXNTYPE'] }

    CString udf3() { this['udf_3'] }

    CString udf2() { this['udf_2'] }

    CString udf1() { this['udf_1'] }

    CString refundAmt() { this['REFUNDAMT'] }

    CString txnAmt() {
        CString txnAmt = this['TXNAMOUNT']
        return new CString(txnAmt as String) {
            private CString self = this

            @Override
            Condition equals(String string) {
                return new Condition() {
                    @Override
                    boolean getAsBoolean() {
                        Objects.equals((txnAmt as String) as double, string as double)
                    }

                    @Override
                    String toString() {
                        "$self equals $string"
                    }
                }
            }

            @Override
            String toString() {
                txnAmt.toString()
            }
        }
    }

    CString txnDate() { this['TXNDATE'] }

    CString vpa() { this['VPA'] }

    CString txnDateTime() { this['TXNDATETIME'] }

    CString txnId() { this['TXNID'] }

    CString checksumHash() { this['CHECKSUMHASH'] }

    CString promoRespCode() { this['PROMO_RESPCODE'] }

    CString promoCampId() { this['PROMO_CAMP_ID'] }

    CString promoStatus() { this['PROMO_STATUS'] }

    CString childTxnList() { this['CHILDTXNLIST'] }

    CString additionalParam() { this['ADDITIONAL_PARAM'] }

    CString bin() { this['BIN'] }

    CString lastFourDigits() { this['LASTFOURDIGITS'] }

    CString cardScheme() { this['cardScheme'] }

    CString cardIndexNo() { this['cardIndexNo'] }

    CString cardHash() { this['cardHash'] }

    CString prepaidCard() { this['prepaidCard'] }

    CString subsId() { this['SUBS_ID'] }

    CString feeRateFactors() { this['feeRateFactors'] }


    CString maskedEcomToken() { this['MASKED_ECOMTOKEN'] }

    CString riskInfo(){this['riskInfo']}

    CString resultCode(){ this['RESULTCODE']}

    Condition isChecksumValid() {
        this.isChecksumValid(new Merchant(this.mId().value).key)
    }

    Condition isChecksumValid(String mKey) {
        boolean isValid = PGPUtil.isChecksumValid(mKey, this.map as TreeMap, this.checksumHash().value)
        new Condition() {
            @Override
            boolean getAsBoolean() {
                return isValid
            }

            @Override
            String toString() {
                'is checksum valid'
            }
        }
    }

    CString getAt(String attribute) {
        return new CString(map[attribute]) {
            @Override
            String toString() {
                "peon $attribute(${this.getValue()})"
            }
        }
    }

    CList<String> keys() {
        return new CList<String>(map.keySet().toList()) {
            @Override
            String toString() {
                "peon-attributes(${this.getValue()})"
            }
        }
    }

    def asType(Class<?> aClass) {
        if (aClass == Map.class) return map
        else throw new UnsupportedOperationException()
    }

    @Override
    String toString() {
        map as String
    }
}
