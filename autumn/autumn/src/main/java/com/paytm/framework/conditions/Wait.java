package com.paytm.framework.conditions;

import com.paytm.framework.reporting.Reporter;

import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;

public class Wait {

    private final IntFunction<Integer> pollingFunction;
    private final int pollingCount;
    private final long timeUnitInMillis;

    public Wait(IntFunction<Integer> pollingFunction, int pollingCount, long timeUnitInMillis) {
        this.pollingFunction = pollingFunction;
        this.pollingCount = pollingCount;
        this.timeUnitInMillis = timeUnitInMillis;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Reporter.report.error("Couldn't sleep "+e.getMessage());
        }
    }

    public BooleanSupplier apply(BooleanSupplier condition) {
        for (int i = 0; i < pollingCount; i++) {
            sleep(pollingFunction.apply(i) * timeUnitInMillis);
            if (condition.getAsBoolean()) {
                break;
            }
        }
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return condition.getAsBoolean();
            }

            @Override
            public String toString() {
                return Wait.this.toString();
            }
        };
    }

    public BooleanSupplier call(BooleanSupplier condition) {
        return this.apply(condition);
    }
}
