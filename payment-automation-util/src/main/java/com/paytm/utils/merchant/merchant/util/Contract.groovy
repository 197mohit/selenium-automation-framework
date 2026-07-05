package com.paytm.utils.merchant.merchant.util

import groovy.transform.ToString


@ToString(includePackage = false, includeNames = true)
class Contract {

    final String id
    final String productCode
    final String productName
    final String contractStatus

        Contract(String id, String productCode, String productName, String contractStatus) {
        this.id = id
        this.productCode = productCode
        this.productName = productName
        this.contractStatus = contractStatus
    }

    @Override
    String toString() {
        "Contract(id:$id, productCode: $productCode., productName: $productName, contractStatus: $contractStatus)"
    }

}

