package com.paytm.utils.merchant.merchant.util

import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true, excludes = ['bank'])
class EMI {

    final String id
    final Bank bank
    final double minAmt
    final double maxAmt
    final int months
    final double interest
    final String type

    EMI(String id, String bankId, String bankCode, String bankName, double interest, int months, double minAmt, double maxAmt, String type) {
        this.id = id
        this.bank = new Bank(bankId, bankCode, bankName)
        this.interest = interest
        this.months = months
        this.minAmt = minAmt
        this.maxAmt = maxAmt
        this.type = type.toLowerCase()
    }

    @Override
    String toString() {
        "EMI(id:$id, bank: $bank.code, interest: $interest, months: $months, minAmt: $minAmt, maxAmt: $maxAmt, type: $type)"
    }

    private class Bank {
        final String id
        final String code
        final String name

        Bank(String id, String code, String name) {
            this.id = id
            this.code = code.toLowerCase()
            this.name = name.toLowerCase()
        }
    }
}
