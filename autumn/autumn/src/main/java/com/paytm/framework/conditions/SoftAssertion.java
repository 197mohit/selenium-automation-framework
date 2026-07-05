package com.paytm.framework.conditions;

import java.util.function.BooleanSupplier;

public class SoftAssertion {
    private StringBuilder assertionMsg = new StringBuilder();

    public void apply(BooleanSupplier... conditions) {
        for (BooleanSupplier condition : conditions) {
            if (!condition.getAsBoolean()) this.assertionMsg.append("\n").append(condition);
        }
    }

    public void add(BooleanSupplier condition) {
        this.apply(condition);
    }

    public void plus(BooleanSupplier condition) {
        this.add(condition);
    }

    public void eval() {
        if (!this.assertionMsg.toString().equals("")) throw new AssertionError(assertionMsg.toString());
    }

    public void call(BooleanSupplier condition) {
        this.apply(condition);
    }
}
