package com.paytm.utils.merchant.merchant.util

class Refunds {

    private final String mId
    private final String mKey
    private final String orderId
    private List<String> refIds = []

    Refunds(String mId, String mKey, String orderId) {
        this.mId = mId
        this.mKey = mKey
        this.orderId = orderId
    }

    boolean removeAll() {
        !refIds.any { !refIds.remove(it) }
    }

    boolean plus(String refId) {
        this.add(refId)
    }

    boolean add(String refId) {
        refIds.add(refId)
    }

    Refund getAt(int index) {
        new Refund(mId, mKey, orderId, refIds[index])
    }

    Refund get(int index) {
        this[index]
    }

}
