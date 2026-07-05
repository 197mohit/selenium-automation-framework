package scripts.api.mappingService.logoIntegrationApi;

import com.paytm.api.MappingService.LogoIntegration;
import com.paytm.framework.reporting.Owners;
import io.qameta.allure.Owner;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Samar Aswal")
public class UploadApi  {

    private final LogoIntegration logoIntegration = new LogoIntegration();
    private static final String merchantId = "SsEML469811569388287";

    @Test (priority = 1)
    public void uploadMerchant(){
        logoIntegration.uploadLogo("merchant", merchantId, "");
    }

    @Test (priority = 2)
    public void checkMerchant(){
        logoIntegration.getLogo("merchant", merchantId, "", "check");
    }

    @Test (priority = 3)
    public void fetchMerchant(){
        logoIntegration.getLogo("merchant", merchantId, "", "fetch");
    }

    @Test (priority = 4)
    public void deleteMerchant(){
        logoIntegration.deleteLogo("merchant", merchantId, "");
    }
}
