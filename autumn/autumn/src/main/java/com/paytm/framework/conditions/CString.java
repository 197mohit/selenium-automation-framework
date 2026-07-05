package com.paytm.framework.conditions;

import java.util.Date;
import java.util.function.Predicate;

import static java.text.MessageFormat.format;

public class CString extends CObject {

    private final java.lang.String string;

    public CString(java.lang.String string) {
        super(string);
        this.string = string;
    }

    public String getValue() {
        return this.string;
    }

    public Condition equalsIgnoreCase(String string) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return CString.this.string == null ? string == null : CString.this.string.equalsIgnoreCase(string);
            }

            @Override
            public String toString() {
                return format("{0} is equals ignoring case to {1}", CString.this, string);
            }
        };
    }

    public Condition contains(String string) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return CString.this.string == null ? string == null : CString.this.string.contains(string);
            }

            @Override
            public String toString() {
                return format("{0} contains {1}", CString.this, string);
            }
        };
    }

    public Condition equals(Predicate<String> predicate) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return predicate.test(string);
            }

            @Override
            public String toString() {
                return format("{0} satisfies condition: {1}", CString.this, predicate);
            }
        };
    }

    public Condition equals(String string) {
        return super.equalsTo(string);
    }

    public Condition equals(Date date) {
        return super.equalsTo(date.toString());
    }

    public Condition equals(double expected) {
        return super.equalsTo(Double.toString(expected));
    }

    public Condition equals(int expected) {
        return super.equalsTo(Integer.toString(expected));
    }

    public String asType(Class<String> aClass) {
        if (aClass.equals(String.class)) return this.string;
        else throw new UnsupportedOperationException("unable to cast");
    }

}
