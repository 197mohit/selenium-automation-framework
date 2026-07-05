package com.paytm.framework.conditions;

import java.util.function.BooleanSupplier;

import static java.text.MessageFormat.format;

public interface Condition extends BooleanSupplier {

    default BooleanSupplier not() {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return !Condition.this.getAsBoolean();
            }

            @Override
            public String toString() {
                return format("not: {0}", Condition.this.toString());
            }
        };
    }

    default boolean asBoolean() {
        return this.getAsBoolean();
    }
}
