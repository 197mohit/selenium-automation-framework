package com.paytm.utils.merchant.user.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AUser {
    //value -> {"true", "false", "any"}
    String premium() default "any";

    String ppbl() default "any";

    String paytmcc() default "any";

    String kyc() default "true";

    String emidc() default "any";

    ASubWallets subWallets() default @ASubWallets;

    boolean edit() default false;
}
