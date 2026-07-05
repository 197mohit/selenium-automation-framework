package com.paytm.utils.merchant.user

import com.paytm.utils.merchant.GList

class Cards implements GList<Card> {

    @Override
    Iterator<Card> iterator() {
        return new Iterator<Card>() {
            List list = [
                    ['5160572570543655', '01', '2030', '123', 'master', 'high'],
                    ['5168441223630339', '01', '2030', '123', 'master', 'high'],
                    ['6799990100000000019', '01', '2030', '123', 'maestro', 'high'],
                    ['379863297651006', '01', '2030', '1234', 'amex', 'high'],
                    ['344185362360798', '01', '2030', '1234', 'amex', 'high'],
                    ['349921595143683', '01', '2030', '1234', 'amex', 'high'],
                    ['371341071385644', '01', '2030', '1234', 'amex', 'high'],
                    ['342309592209961', '01', '2030', '1234', 'amex', 'high'],
                    ['341546989495098', '01', '2030', '1234', 'amex', 'high'],
                    ['340348449008376', '01', '2030', '1234', 'amex', 'high'],
                    ['372406121390834', '01', '2030', '1234', 'amex', 'high'],
                    ['340516629746516', '01', '2030', '1234', 'amex', 'high'],
                    ['6073180505920479', '01', '2030', '123', 'rupay', 'high'],
                    ['30569309025904', '01', '2030', '123', 'diners', 'low'],
                    ['2030400200341578', '01', '2030', '123', 'bajajfn', 'high'],
                    ['4012888888881881', '01', '2030', '123', 'visa', 'high'],
                    ['4718650100010336', '01', '2030', '123', 'visa', 'high'],
                    ['6011858463797808', '01', '2030', '123', 'discover', 'high'],
                    ['4766413897814514', '01', '2030', '123', 'visa', 'high'],
            ].collect { it as Card }
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Card next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Card> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
