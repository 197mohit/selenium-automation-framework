package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.ui.element.RadioButton;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.KYCPage;
import io.qameta.allure.Owner;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Tarun")
public class KYC extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();


    @Parameters({"theme"})
    @Test(description = "Verify for basic user with AddMoney flow and wallet limit api details and verify user is navigated to KYC document page", groups = "smoke")
    public void validateKYCDocumentPageVisibility(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       // PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.CC);

        Assert.assertTrue(kycPage.kycPageFooter().getText().contains(KYCPage.KYC_FOOTER),"KYC page is not getting opened for : " + user.mobNo());

       }


    @Parameters({"theme"})
    @Test(description = "Verify voter id number detail limit on KYC Document page",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateVoterIDNumberLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney,theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");


        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idNumber().getAttribute("maxLength"),"-1","Limit is set");

    }



    @Parameters({"theme"})
    @Test(description = "Validate Voter ID Name Limit not set",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateVoterIDNameLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idName().getAttribute("maxLength"),"-1","Limit is set");
    }


    @Parameters({"theme"})
    @Test(description = "Validate incorrect voter id details and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterIncorrectVoterIDDetails(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.INCORRECT_NUMBER);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.INCORRECT_VOTERID_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter voter id number and leave name field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterVoterIDNumberButEmptyName(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type(KYCPage.EMPTY);
        kycPage.idNumber().type("123456789012");
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NAME_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter voter id name and leave number field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterVoterIDNameButEmptyNumber(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.EMPTY);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_VOTERID_NUMBER_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Verify PAN Card number detail limit on KYC Document page",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validatePANCARDNumberLimit(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney,theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idNumber().getAttribute("maxLength"),"-1","Limit is set");

    }

    @Parameters({"theme"})
    @Test(description = "Validate Pan Card Name Limit not set",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validatePANCARDNameLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idName().getAttribute("maxLength"),"-1","Limit is set");
    }


    @Parameters({"theme"})
    @Test(description = "Validate incorrect pan card details and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterIncorrectPANCARDDetails(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.INCORRECT_NUMBER);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.INCORRECT_PANCARD_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter PAN Card number and leave name field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterPANCARDNumberButEmptyName(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Voter_ID.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type(KYCPage.EMPTY);
        kycPage.idNumber().type("123456789012");
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NAME_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Enter PAN Card name and leave number field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterPANCARDNameButEmptyNumber(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("TARUN");
        kycPage.idNumber().type(KYCPage.EMPTY);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_PANCARD_NUMBER_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Verify Driving License number detail limit on KYC Document page",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateDrivingLicenseNumberLimit(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney,theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Driving_License.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idNumber().getAttribute("maxLength"),"-1","Limit is set");

    }

    @Parameters({"theme"})
    @Test(description = "Validate Driving License Name Limit not set",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateDrivingLicenseNameLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Driving_License.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idName().getAttribute("maxLength"),"-1","Limit is set");
    }


    @Parameters({"theme"})
    @Test(description = "Validate incorrect driving license details and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterIncorrectDrivingLicenseDetails(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Driving_License.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.INCORRECT_NUMBER);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.INCORRECT_DRIVINGLICENSE_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter Driving License number and leave name field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterDrivingLicenseNumberButEmptyName(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Driving_License.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type(KYCPage.EMPTY);
        kycPage.idNumber().type("123456789012");
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NAME_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Enter Driving License name and leave number field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterDrivingLicenseNameButEmptyNumber(@Optional("enhancedweb_revamp") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Driving_License.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("TARUN");
        kycPage.idNumber().type(KYCPage.EMPTY);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_DRIVINGLICENSE_NUMBER_ERROR_MESSAGE,"Error message is incorrect");
    }





    @Parameters({"theme"})
    @Test(description = "Verify Passport number detail limit on KYC Document page",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validatePassportNumberLimit(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney,theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Passport.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idNumber().getAttribute("maxLength"),"-1","Limit is set");

    }

    @Parameters({"theme"})
    @Test(description = "Validate Passport Name Limit not set",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validatePassportNameLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Passport.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idName().getAttribute("maxLength"),"-1","Limit is set");
    }


    @Parameters({"theme"})
    @Test(description = "Validate incorrect passport details and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterIncorrectPassportDetails(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Passport.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.INCORRECT_NUMBER);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.INCORRECT_PASSPORT_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter Driving License number and leave name field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterPassportNumberButEmptyName(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Passport.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type(KYCPage.EMPTY);
        kycPage.idNumber().type("123456789012");
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NAME_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Enter Passport name and leave number field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterPassportNameButEmptyNumber(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.Passport.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("TARUN");
        kycPage.idNumber().type(KYCPage.EMPTY);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_PASSPORT_NUMBER_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Verify NREGA Job Card number detail limit on KYC Document page",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateNREGAJobCardNumberLimit(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney,theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.NREGA_JOB_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idNumber().getAttribute("maxLength"),"-1","Limit is set");

    }

    @Parameters({"theme"})
    @Test(description = "Validate NREGA Job Card Name Limit not set",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void validateNREGAJobCardLimitNotSet(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.NREGA_JOB_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        Assert.assertEquals(kycPage.idName().getAttribute("maxLength"),"-1","Limit is set");
    }


    @Parameters({"theme"})
    @Test(description = "Validate incorrect NREGA Job Card details and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterIncorrectNREGAJobCardDetails(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.NREGA_JOB_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("Tarun");
        kycPage.idNumber().type(KYCPage.INCORRECT_NUMBER);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.INCORRECT_NREGAJOBCARD_ERROR_MESSAGE,"Error message is incorrect");
    }

    @Parameters({"theme"})
    @Test(description = "Enter NREGA Job Card number and leave name field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterNREGAJobCardNumberButEmptyName(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.NREGA_JOB_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type(KYCPage.EMPTY);
        kycPage.idNumber().type("123456789012");
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NAME_ERROR_MESSAGE,"Error message is incorrect");
    }


    @Parameters({"theme"})
    @Test(description = "Enter NREGA Job Card name and leave number field blank and submit",dependsOnMethods = "validateKYCDocumentPageVisibility")
    public void enterNREGAJobCardNameButEmptyNumber(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        RadioButton idRadioBtn = kycPage.idRadioButton(KYCPage.allIDDetails.NREGA_JOB_Card.toString());
        idRadioBtn.select();
        Assert.assertTrue(idRadioBtn.isSelected(),idRadioBtn + " is not selectable");
        kycPage.idName().type("TARUN");
        kycPage.idNumber().type(KYCPage.EMPTY);
        kycPage.submit();

        Assert.assertEquals(kycPage.errorMessage().getText(),KYCPage.EMPTY_NREGAJOBCARD_NUMBER_ERROR_MESSAGE,"Error message is incorrect");
    }
}


