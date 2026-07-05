package com.paytm.utils.merchant.user.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ASubWallets {
    //value -> {"true", "false", "any"}
    String main() default "true";

    String food() default "any";

    String gift() default "any";
}
