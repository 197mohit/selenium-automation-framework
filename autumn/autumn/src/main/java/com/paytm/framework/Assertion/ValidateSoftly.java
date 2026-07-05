package com.paytm.framework.Assertion;

import com.paytm.framework.reporting.Reporter;
import org.assertj.core.api.*;

import java.util.List;

/**
 * @author ankuragarwal
 * Date: 10/09/2018
 */
public class ValidateSoftly extends SoftAssertions {

    private SoftAssertions softly;

    private ValidateSoftly() {
        softly = new SoftAssertions();
    }

    public static ValidateSoftly getInstance() {
        return new ValidateSoftly();
    }

    public ProxyableObjectAssert<Object> validate(Object obj, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(obj).as(description);
    }

    public StringAssert validate(String expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public IntegerAssert validate(int expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public FloatAssert validate(Float expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public AbstractListAssert validate(List<?> expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public DoubleAssert validate(Double expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public BooleanAssert validate(boolean expected, String description, String validationMsg) {
        Reporter.report.info(validationMsg);
        return this.softly.assertThat(expected).as(description);
    }

    public void assertAll() {
        this.softly.assertAll();
    }
}
