package com.paytm.framework.conditions;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface Function {
    BooleanSupplier apply(Object... objects);
}
