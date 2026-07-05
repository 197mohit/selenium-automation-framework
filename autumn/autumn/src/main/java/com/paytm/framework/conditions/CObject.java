package com.paytm.framework.conditions;

import java.util.Objects;

import static java.text.MessageFormat.format;

public abstract class CObject {

    private final Object object;

    public CObject(Object object) {
        this.object = object;
    }

    public Object getValue() {
        return this.object;
    }

    protected Condition equalsTo(Object object) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return Objects.equals(CObject.this.object, object);
            }

            @Override
            public String toString() {
                return format("{0} equals {1}", CObject.this, object);
            }
        };
    }

    public Condition isNull() {
        return this.equalsTo(null);
    }

    public Condition equals(Function condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.object != null ? this.object.toString() : "null";
    }
}
