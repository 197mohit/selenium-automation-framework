package scripts.api.merchant.migration;

import com.google.i18n.phonenumbers.Phonenumber;
import com.paytm.api.MappingService.GetMerchantAttributePreferenceInfo;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DirectSettlementOnEDCAcquirers extends merchantMigrationHelper {

    @Feature("PGP-25996")
    @Owner("Nirottam")
    @Test(description = "EDC MERCHANT send instantSettlement=false, feeOnHold=false for productCode 46,47 and 48 ")
    public void CreateMerchantWithSFPref_01() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,DEFAULT,CASH_POS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String>pcode=new ArrayList<>();
        pcode.add("51051000100000000046");
        pcode.add("51051000100000000047");
        pcode.add("51051000100000000048");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-25996")
    @Owner("Nirottam")
    @Test(description = "EDC MERCHANT send instantSettlement=false, feeOnHold=false for productCode 46 and 47 ")
    public void CreateMerchantWithSFPref_02() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String>pcode=new ArrayList<>();
        pcode.add("51051000100000000046");
        pcode.add("51051000100000000047");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-25996")
    @Owner("Nirottam")
    @Test(description = "EDC MERCHANT send instantSettlement=false, feeOnHold=false for productCode 46 and 48 ")
    public void CreateMerchantWithSFPref_03() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,CASH_POS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String>pcode=new ArrayList<>();
        pcode.add("51051000100000000046");
        pcode.add("51051000100000000048");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-25996")
    @Owner("Nirottam")
    @Test(description = "EDC MERCHANT send instantSettlement=false, feeOnHold=false for productCode 14 ")
    public void CreateMerchantWithSFPref_04() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String>pcode=new ArrayList<>();
        pcode.add("51051000100000000014");
        validateProductCodes(productsCodes,pcode);
    }

}


