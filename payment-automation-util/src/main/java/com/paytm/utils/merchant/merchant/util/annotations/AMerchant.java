package com.paytm.utils.merchant.merchant.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AMerchant {
    //value in ['true', 'false', 'any']
    String hybrid() default "any";

    String addnpay() default "any";

    String saveCardDetails() default "any";

    String peon() default "any";

    String onus() default "any";

    String migrated() default "true";

    String expired() default "false";

    String checksum() default "true";

    String pcf() default "false";

    int retry() default 0;

    APayModes paymodes() default @APayModes;

    boolean edit() default false;
}
