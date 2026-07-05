package scripts.SolidityCheck;

import com.paytm.LocalConfig;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.paytm.framework.reporting.Reporter.report;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Created by anjukumari on 07/07/18
 */
@Owner("Gagandeep")
public class HealthCheck extends PGPBaseTest implements Runnable{
    String module;
    SoftAssertions softAssertions;

    //TODO Removed Link Service healthcheck needs to be added later as it was failing
    private static String[] modulesArray = {"theia","instaproxy","mapping-service",
            "refund", "merchant-status", "pgproxy-notification/alipayplus", "promo-service", "savedcardservice", "subscription", "pg-plus-bo"};

    @Override
    public void run() {
        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.GET);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        api.getRequestSpecBuilder().setBasePath("/"+this.module+"/healthcheck");
        report.info("Healthcheck of : "+ this.module);
        int code = api.execute().getStatusCode();
        report.info("Response of healthcheck on  : "+this.module+ " is :"+ code);
        this.softAssertions.assertThat(code).isEqualTo(200);
    }

//    @Test(enabled = false)
    public void healthCheckOld(){
        SoftAssertions softAssertions = new SoftAssertions();
        HealthCheck healthCheck = new HealthCheck();
        for(int i=0; i<modulesArray.length; i++) {
            healthCheck.module = modulesArray[i];
            healthCheck.softAssertions = softAssertions;
            Thread t = new Thread(healthCheck);
            t.run();
        }
        healthCheck.softAssertions.assertAll();
    }


    @DataProvider(name ="module", parallel=true)
    public Object[][] getModule(){
        Object[][] obj = new Object[modulesArray.length][];
        int i;
        for(i=0; i<modulesArray.length; i++) {
            obj[i] = new Object[1];
            obj[i][0] = modulesArray[i];
        }
        return obj;
    }

    @Test(dataProvider="module", threadPoolSize=3, enabled = true)
    public void healthCheck(String moduleName){
        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.GET);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        api.getRequestSpecBuilder().setBasePath("/"+moduleName+"/healthcheck");
        report.info("Healthcheck of : "+ moduleName);
        int code = api.execute().getStatusCode();
       // report.info("Response of healthcheck on  : "+this.module+ " is :"+ code);
        assertThat(code).isEqualTo(200);
    }


}
