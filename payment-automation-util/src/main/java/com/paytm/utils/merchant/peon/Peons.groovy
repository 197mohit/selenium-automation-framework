package com.paytm.utils.merchant.peon

import com.paytm.utils.merchant.GList

class Peons implements GList<Peon>{

    String orderId;
    public final List<Peon> peons = [new EncPeon(orderId), new TxnPeon(orderId)]

    Peons(String orderId) {
        this.orderId = orderId
    }

    Peon getAt(String type) {
        assert type in ['encpeon', 'txnpeon']
        this.find {it.name == type}
    }

    @Override
    Iterator<Peon> iterator() {
        new Iterator<Peon>() {
            private def list = peons;
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Peon next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Peon> c) {
        return false
    }

    @Override
    boolean removeAll(Collection<?> c) {
        return false
    }
}
