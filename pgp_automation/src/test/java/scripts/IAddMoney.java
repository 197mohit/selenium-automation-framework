package scripts;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Tarun")
@Feature("PGP-19696")
public interface IAddMoney {

             @Test(description = "Full KYC Flow : To validate when more than 10000 Rs txn is done by CC for adding money to Wallet")
             @Parameters({"theme"})
             abstract void validateFullKYCWalletCC(@Optional("enhancedweb") String theme) throws Exception;

            @Test(description = "Full KYC Flow : To validate when more than 10000 Rs txn is done by Saved CC for adding money to Wallet")
            @Parameters({"theme"})
            abstract void validateFullKYCWalletSavedCC(@Optional("enhancedweb") String theme) throws Exception;

             @Test(description = "MIN KYC Flow - To validate when less than 10000 Rs txn is done by CC for adding money to Wallet")
             @Parameters({"theme"})
             abstract void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb") String theme) throws Exception;

            @Test(description = "MIN KYC Flow - To validate when more than 10000 Rs txn is done by CC for adding money to Wallet")
            @Parameters({"theme"})
            abstract void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "NO KYC Flow - To validate when less than 10000 Rs txn is done by CC for adding money to Wallet")
             @Parameters({"theme"})
             abstract void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "NO KYC Flow - To validate when more than 10000 Rs txn is done by CC for adding money to Wallet")
             @Parameters({"theme"})
             abstract void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "Full KYC Flow : To validate when more than 10000 Rs txn is done by CC for adding money to GV")
             @Parameters({"theme"})
              abstract void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "Full KYC Flow : To validate when more than 10000 Rs txn is done by Saved CC for adding money to GV")
             @Parameters({"theme"})
             abstract void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "MIN KYC Flow - To validate when less than 10000 Rs txn is done by CC for adding money to GV")
             @Parameters({"theme"})
             abstract void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "MIN KYC Flow - To validate when more than 10000 Rs txn is done by CC for adding money to GV")
             @Parameters({"theme"})
             abstract void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception;

             @Test(description = "NO KYC Flow - To validate when more than 10000 Rs txn is done by CC for adding money to GV")
             @Parameters({"theme"})
             abstract void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception;

             //Negative Case

            @Test(description = "Full KYC Flow : To validate success txn when more than 10000 Rs txn is done by DC for adding money to Wallet")
            @Parameters({"theme"})
            abstract void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception;

            @Test(description = "MIN KYC Flow : To validate success txn when more than 10000 Rs txn is done by NB for adding money to Wallet")
            @Parameters({"theme"})
            abstract void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception;

            @Test(description = "No KYC Flow : To validate success txn when more than 10000 Rs txn is done by DC for adding money to Wallet")
            @Parameters({"theme"})
            abstract void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception;

 }
