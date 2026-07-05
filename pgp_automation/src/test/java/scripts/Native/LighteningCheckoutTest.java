// AI-Generated: 2025-09-12 - Test class creation
package scripts.Native;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.LighteningCheckoutPage;

import io.qameta.allure.Feature;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Lightening Checkout Test class for testing lightening checkout functionality
 */

@Feature("Lightening Checkout")
@Owners(author = "AI Assistant", qa = "Puspa")
public class LighteningCheckoutTest extends PGPBaseTest {

    // AI-Generated: 2025-09-12 - Page object initialization
    private final LighteningCheckoutPage lighteningCheckoutPage = new LighteningCheckoutPage();

    void loginUser(String mobileNumber, String otp)
    {
        lighteningCheckoutPage.mobileNumberInput().sendKeys(mobileNumber);
        lighteningCheckoutPage.proceedButtonByText().click();
        lighteningCheckoutPage.enterOtp().sendKeys(otp);
        lighteningCheckoutPage.enterOtp().click(); 
    }

    @Parameters({"theme"})
    @Feature("PAPR-6624")
    @Test(description = "Verify lightening checkout page loads successfully", groups = "lightening")
    public void verifyLoginUserFlow(@Optional("lightening_web_revamp") String theme) throws Exception {
        // AI-Generated: 2025-09-12 - Test method implementation
        lighteningCheckoutPage.loadMerchantConfig(theme);
        
        // AI-Generated: 2025-09-12 - Check if phone field is visible before entering number
        if (lighteningCheckoutPage.mobileNumberInput().isDisplayed()) {

         loginUser("7480053111","888888");
            
        }
        
        
       
    }


}
