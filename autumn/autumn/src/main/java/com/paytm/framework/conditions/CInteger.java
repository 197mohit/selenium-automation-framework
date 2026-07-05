package com.paytm.framework.conditions;

public class CInteger extends CObject {

    private final Integer integer;

    public CInteger(Integer integer) {
        super(integer);
        this.integer = integer;
    }

    public Integer getValue() {
        return this.integer;
    }

    public Condition equals(int expected) {
        return super.equalsTo(expected);
    }

    public Integer asType(Class<Integer> aClass) {
        if (aClass.equals(Integer.class)) return this.integer;
        else throw new UnsupportedOperationException("unable to cast");
    }


}
