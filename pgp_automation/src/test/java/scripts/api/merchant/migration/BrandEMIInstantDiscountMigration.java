package scripts.api.merchant.migration;


import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.boss.staticPrefUpdateApi;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.CreateMerchantEDC;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BrandEMIInstantDiscountMigration extends merchantMigrationHelper {

    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Creating and checking Migration for Product Code 51051000100000000057")
    public void BrandEMIInstantDiscount_01() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,BRAND_EMI_DISCOUNT,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000057");
        softly.assertAll();
    }
    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Creating and checking Migration for Product Code 51051000100000000057 Supporting ONLINE_SETTLEMENT")
    public void BrandEMIInstantDiscount_02() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,BRAND_EMI_DISCOUNT,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setOnlineSettlement("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000057");
        softly.assertAll();
    }
    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Creating and checking Migration for Product Code 51051000100000000057 Supporting BUSINESS_WALLET")
    public void BrandEMIInstantDiscount_03() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("BRAND_EMI_DISCOUNT,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setBWManualType("BW_VIEW_ONLY")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000057");
        softly.assertAll();
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());

    }

    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Creating and checking Migration for Product Code 51051000100000000057 With Acquring Advanced Pay Deposite Method")
    public void BrandEMIInstantDiscount_04() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,BRAND_EMI_DISCOUNT,CASH_POS,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setPaymode("ADVANCE_DEPOSIT_ACCOUNT","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000057");
        softly.assertAll();
    }

    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Merchant should not be Supported  ny other Acquring except Advanced Pay Deposite Method for Product Code 51051000100000000057")
    public void BrandEMIInstantDiscount_05() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,BRAND_EMI_DISCOUNT,CASH_POS,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setPaymodes("CREDIT_CARD")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultMsg")).isEqualTo("TARGET_NOT_FOUND");
        softly.assertAll();
    }
    @Feature("PGP-32293")
    @Owner("Nirottam")
    @Test(description = "Merchant should not be Supported  ny other Acquring except Advanced Pay Deposite Method for Product Code 51051000100000000057 ")
    public void BrandEMIInstantDiscount_06() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,BRAND_EMI_DISCOUNT,CASH_POS,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)

                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.resultInfo.resultMsg")).isEqualTo("TARGET_NOT_FOUND");
        softly.assertAll();
    }


}
