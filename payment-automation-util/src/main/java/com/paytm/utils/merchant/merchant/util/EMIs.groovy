package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList

class EMIs implements GList<EMI> {

    private final Merchant m
    private List<EMI> emis

    EMIs(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<EMI> iterator() {
        return new Iterator<EMI>() {
            private List<EMI> list = emis ?: (emis = {
                String query = """
                SELECT mlm.ID, mlm.INTEREST, mlm.MIN_AMT, mlm.BANK_ID, mlm.MONTH, mlm.MAX_AMT, mlm.PAY_MODE, bm.BANK_ID, bm.BANK_CODE, bm.BANK_DISPLAY_NAME FROM PAYTMPGDB.MBID_LIMIT_MAPPING mlm 
                INNER JOIN PAYTMPGDB.BANK_MASTER bm 
                ON mlm.BANK_ID = bm.BANK_ID 
                WHERE mlm.ENTITY_ID = (SELECT ID FROM PAYTMPGDB.ENTITY_INFO WHERE MID = '$m.id') AND mlm.STATUS = '9376503' AND bm.STATUS = '9376503';
"""
                def banks = m.acquirings.findAll { it.enabled == true }.bank
                DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, query)
                        .findAll { it.get('BANK_CODE').toLowerCase() in banks }
                        .collect {
                    new EMI(it.get('ID') as String, it.get('BANK_ID') as String, it.get('BANK_CODE') as String, it.get('BANK_DISPLAY_NAME') as String, it.get('INTEREST') as Double, it.get('MONTH') as Integer, it.get('MIN_AMT') as Double, it.get('MAX_AMT') as Double, it.get('BANK_CODE') == 'ZEST' ? 'nbfc' : ['EMI': 'cc', 'EMI_DC': 'dc'][it.get('PAY_MODE')])
                }
            }())
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            EMI next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends EMI> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
