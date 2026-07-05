package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.RadioButton;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class KYCPage extends BasePage {

    public KYCPage() {
        super("kyc-page");
    }

    public static final String KYC_FOOTER= "minimum KYC";

    //============================incorrect-data=================================================//

    public static final String EMPTY ="";
    public static final String INCORRECT_NUMBER = "$$$$$$";

    //============================error-messages=================================================//
    public static final String EMPTY_NAME_ERROR_MESSAGE = "Name field cannot be blank";

    public static final String INCORRECT_VOTERID_ERROR_MESSAGE = "Voter ID number not valid";
    public static final String EMPTY_VOTERID_NUMBER_ERROR_MESSAGE = "Voter Id number cannot be empty";

    public static final String INCORRECT_PANCARD_ERROR_MESSAGE = "PAN Card number not valid";
    public static final String EMPTY_PANCARD_NUMBER_ERROR_MESSAGE = "Pan Card number cannot be empty";//TODO

    public static final String INCORRECT_DRIVINGLICENSE_ERROR_MESSAGE = "Driving License number not valid";
    public static final String EMPTY_DRIVINGLICENSE_NUMBER_ERROR_MESSAGE = "Driving License number cannot be empty";

    public static final String INCORRECT_PASSPORT_ERROR_MESSAGE = "Passport number not valid";
    public static final String EMPTY_PASSPORT_NUMBER_ERROR_MESSAGE = "Passport number cannot be empty";

    public static final String INCORRECT_NREGAJOBCARD_ERROR_MESSAGE = "NREGA Job Card number not valid";
    public static final String EMPTY_NREGAJOBCARD_NUMBER_ERROR_MESSAGE = "NREGA Job Card number cannot be empty";


    public UIElement submit_btn()
    {
       return new UIElement(By.xpath("//span[text()='Submit']"),getPageName(),"submit-btn");

    }

    public UIElement submitBtnNew()
    {
        return new UIElement(By.cssSelector(".submitBtn"),getPageName(),"submit-btnNew");
    }

    public void submit()
    {
        submit_btn().click();
        pause(1);
    }

    public RadioButton idRadioButton(String idProof)
    {   
        return new RadioButton(By.xpath("//span[text()='" + idProof.trim() + "']/parent::div//input"),getPageName(),idProof + " radio-btn");
    }

    public String getSelectedId()
    {
           String value = DriverManager.getDriver().findElement(By.cssSelector(".custom-radio:not(.fl) input:checked ")).getAttribute("value");

           switch (value) {

               case "voter":
                   value = allIDDetails.Voter_ID.toString();
                   break;
               case "pan":
                   value = allIDDetails.Pan_Card.toString();
                   break;
               case "dl" :
                   value = allIDDetails.Driving_License.toString();
                   break;
               case "passport":
                   value = allIDDetails.Passport.toString();
                   break;
               case "nrega_job":
                   value = allIDDetails.NREGA_JOB_Card.toString();
                   break;

           }
         return value;
    }



    public TextBox idNumber()
    {
        return new TextBox(By.xpath("//span[text()='Enter "+getSelectedId()+" Number']/parent::label/preceding-sibling::input"),getPageName(),getSelectedId() + "number field");
    }

    public TextBox idName()
    {
        return new TextBox(By.xpath("//span[text()='Enter Name as per "+getSelectedId()+"']/parent::label/preceding-sibling::input"),getPageName(),getSelectedId() + "name field");

    }

    public UIElement errorMessage()
    {
        return new UIElement(By.xpath("//div[@class='_1HbL  _11dj o-h fs12 mt5']"),getPageName(),"error-msg");
    }

    public UIElement kycPageFooter()
    {
            return new UIElement(By.xpath("//span[contains(text(),'minimum KYC.')]"), getPageName(), "kyc-page-title");
    }

    public enum allIDDetails{
        Voter_ID("Voter ID"),
        Pan_Card("PAN Card"),
        Driving_License("Driving License"),
        Passport("Passport"),
        NREGA_JOB_Card("NREGA Job Card");

        private final String uiField;
        allIDDetails(String uiField)
        {
            this.uiField = uiField;
        }
        @Override
        public String toString() {
            return uiField;
        }
    }

}

