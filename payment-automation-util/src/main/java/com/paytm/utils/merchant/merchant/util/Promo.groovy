package com.paytm.utils.merchant.merchant.util

import org.apache.commons.lang3.RandomStringUtils

class Promo implements Comparable<Promo> {

    final boolean expired
    final boolean used
    final boolean is8DigitBin
    final String name

    private static String randomName() {
        RandomStringUtils.random(6, (('a'..'z') + ('A'..'Z') + ('0'..'9')).join().toCharArray())
    }

    Promo(boolean expired, boolean used) {
        this(randomName(), expired, used, false)
    }

    Promo() {
        this(randomName(), false, false, false)
    }

    Promo(String name) {
        this(name, false, false, false)
    }

    Promo(boolean is8DigitBin) {
        this(randomName(), false, false, is8DigitBin)
    }

    Promo(String name, boolean expired, boolean used) {
        this(name, expired, used, false)
    }

    Promo(String name, boolean expired, boolean used, boolean is8DigitBin) {
        this.name = name
        this.expired = expired
        this.used = used
        this.is8DigitBin = is8DigitBin
    }

    @Override
    boolean equals(Object obj) {
        obj instanceof Promo && this.name == obj.name
    }

    @Override
    int compareTo(Promo promo) {
        double diff = this.offerAmt - promo.offerAmt
        return diff == 0 ? 0 : diff / diff.abs()
    }
}
