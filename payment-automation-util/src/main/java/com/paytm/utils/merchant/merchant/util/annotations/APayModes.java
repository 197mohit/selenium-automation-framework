package com.paytm.utils.merchant.merchant.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface APayModes {
    //value in ['true', 'false', 'any']
    String cc() default "any";

    String dc() default "any";

    String ppbl() default "any";

    String nb() default "any";

    String upi() default "any";

    String cod() default "any";

    String emi() default "any";
}
