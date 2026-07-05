package com.paytm.framework.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.text.MessageFormat.format;

public class CList<E> extends CObject {

    private final List<E> list;

    public CList(List<E> list) {
        super(list);
        this.list = list;
    }

    public List<E> getValue() {
        return new ArrayList<>(this.list);
    }

    public Condition contains(E element) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return list.contains(element);
            }

            @Override
            public String toString() {
                return format("{0} contains {1}", CList.this, element);
            }
        };
    }

    @SafeVarargs
    public final Condition contains(E... elements) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return list.containsAll(Arrays.asList(elements));
            }

            @Override
            public String toString() {
                return format("{0} contains exactly {1}", CList.this, Arrays.asList(elements));
            }
        };
    }

    public Condition containsExactly(E... elements) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return list.containsAll(Arrays.asList(elements)) && list.size() == elements.length;
            }

            @Override
            public String toString() {
                return format("{0} contains exactly {1}", CList.this, Arrays.asList(elements));
            }
        };
    }

    public Condition equals(List<E> list) {
        return super.equalsTo(list);
    }

    public List<E> asType(Class<List> aClass) {
        if (aClass.equals(List.class)) return this.list;
        else throw new UnsupportedOperationException("unable to cast");
    }

}

