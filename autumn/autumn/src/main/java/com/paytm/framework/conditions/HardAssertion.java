package com.paytm.framework.conditions;

import java.util.function.BooleanSupplier;

public class HardAssertion {

    public void apply(BooleanSupplier condition) {
        if (!condition.getAsBoolean()) throw new AssertionError(condition);
    }

    public void call(BooleanSupplier condition) {
        this.apply(condition);
    }
}
