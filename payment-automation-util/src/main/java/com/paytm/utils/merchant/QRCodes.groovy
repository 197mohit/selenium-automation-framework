package com.paytm.utils.merchant

import com.paytm.utils.merchant.merchant.util.QRCode

trait QRCodes implements GListV1<QRCode> {

    @Override
    List<QRCode> removeAll(Collection<?> c) {
        c.every { it.setEnabled(false) } ? c : []
    }
}