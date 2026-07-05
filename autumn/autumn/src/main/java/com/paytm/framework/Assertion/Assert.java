package com.paytm.framework.Assertion;

import com.paytm.framework.reporting.Reporter;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.assertj.core.api.PredicateAssert;
import org.assertj.core.util.CheckReturnValue;

import java.util.function.Predicate;

/**
 * @author ankuragarwal
 * Dated: 10/10/2018
 */
@CheckReturnValue
public class Assert {


    /**
     * Create assertion for {@link Predicate}.
     *
     * @param actual the actual value.
     * @param <T> the type of the value contained in the {@link Predicate}.
     * @return the created assertion object.
     *
     * @since 3.5.0
     */
    public static <T> PredicateAssert<T> assertThat(Predicate<T> actual) {
        Reporter.report.info("");
        return AssertionsForInterfaceTypes.assertThat(actual);
    }


}
