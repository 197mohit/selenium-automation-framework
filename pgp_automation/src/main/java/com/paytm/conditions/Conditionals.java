package com.paytm.conditions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;

public interface Conditionals {

    Predicate<String> validDate = new Predicate<String>() {
        @Override
        public boolean test(String s) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedExpectedDate = sdf.format(new Date());
            String formattedActualDate = null;
            try {
                formattedActualDate = sdf.format(sdf.parse(s));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return Objects.equals(formattedActualDate, formattedExpectedDate);
        }

        @Override
        public String toString() {
            return "date is valid";
        }
    };
}
