package com.paytm.utils.merchant.ff4j.annotations;

import java.lang.annotation.*;

@Repeatable(FF4JFeature.List.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FF4JFeature {

    String name();

    boolean enabled();

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FF4JFeature[] value();
    }
}