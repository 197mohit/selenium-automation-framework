package com.paytm.framework.reportportal.contants;

import com.paytm.framework.reportportal.api.GetExistingLaunchInfo;
import io.restassured.response.Response;

import java.util.Arrays;

import static com.paytm.framework.reportportal.ReporterConfig.*;

public class FeaturesEvaluator {

    public static FEATURE_VALUE FEATURE_ATLAS = FEATURE_VALUE.ENABLED;
    public static FEATURE_VALUE FEATURE_ATLAS_EXISTING_LAUNCH;
    public static FEATURE_VALUE FEATURE_GENERATE_FAILED_XML;

    static {
        evaluate();
    }

    private FeaturesEvaluator(){}

    private static void evaluate() {
        Arrays.asList(RP_ENDPOINT, RP_UUID, RP_LAUNCH, RP_PROJECT, AUTO_EXEC_HANDLER_URL)
                .forEach(i -> {
                    if (null == i || i.isEmpty()) {
                        FEATURE_ATLAS = FEATURE_VALUE.DISABLED;
                    }
                });

        if (!FEATURE_ATLAS.equals(FEATURE_VALUE.DISABLED)) {
            FEATURE_ATLAS = Boolean.valueOf(RP_ENABLE) ? FEATURE_VALUE.ENABLED : FEATURE_VALUE.DISABLED;
            if (Boolean.valueOf(RP_RERUN))
                FEATURE_ATLAS = !getRerunOfVal().isEmpty() ? FEATURE_VALUE.ENABLED : FEATURE_VALUE.DISABLED;
        }

        if (!FEATURE_ATLAS.equals(FEATURE_VALUE.DISABLED)) {
            FEATURE_ATLAS_EXISTING_LAUNCH = Boolean.valueOf(ALREADY_LAUNCHED) ? FEATURE_VALUE.ENABLED : FEATURE_VALUE.DISABLED;
        }


        FEATURE_GENERATE_FAILED_XML = Boolean.valueOf(GENERATE_FAILED_XML) ? FEATURE_VALUE.ENABLED : FEATURE_VALUE.DISABLED;

    }


    private static String getRerunOfVal() {
        if (!RP_RERUN_OF.isEmpty())
            return RP_RERUN_OF;
        Response r = new GetExistingLaunchInfo().execute();
        if (r.statusCode() == 200) {
            RP_RERUN_OF = r.jsonPath().getString("launch_id");
        }
        return RP_RERUN_OF;
    }


    public enum FEATURE_VALUE {
        ENABLED, DISABLED;
    }

}
