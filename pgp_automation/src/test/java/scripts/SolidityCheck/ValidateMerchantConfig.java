package scripts.SolidityCheck;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.UtilConstants;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.ContractDetails;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO.EmiConfigInfos;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import com.paytm.utils.merchant.util.Acquiring;
import com.paytm.utils.merchant.util.EmiUtils;
import com.paytm.utils.merchant.util.MerchantPrefType;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Owner("Gagandeep")
public class ValidateMerchantConfig extends PGPBaseTest {

    public GetMerchantHelper merchantHelper;
    public Acquiring acquiring;
    public SoftAssertions softly = new SoftAssertions();
    public EmiUtils emiUtils;
    public MerchExtendedInfo merchExtendedInfo;

    @Test(description = "Verfication of PG only merchant acquirings")
    public void validatePGOnlyMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.PGOnly.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.PGOnly, 3);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);

        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            String prodName = contractDetails.getContractBasic().getProductName();
            if (prodName.equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD,
                        PayMethodType.NET_BANKING);
                status = true;
            }
        }
        if (status == false) {
            this.softly.fail("Default Contract not found for mid: " + mid);
        }
        acquiring.AssertAll();
    }

    @Test(description = "Verify Add Money merchant acquirings")
    public void validateAddMoneyMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.AddMoney.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.AddMoney, 3);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.CREDIT_CARD,
                        PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING,
                        PayMethodType.BALANCE);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        verifyMerchantPreference(MerchantPrefType.ADD_MONEY_ENABLED);
        acquiring.AssertAll();
    }

    @Test(description = "Verify Add Money MP merchant acquiring")
    public void validateAddMoneyMpMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.AddMoneyMP.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.AddMoneyMP, 3);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD,
                PayMethodType.NET_BANKING, PayMethodType.UPI);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD,
                        PayMethodType.NET_BANKING, PayMethodType.UPI);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        acquiring.AssertAll();
    }

    @Test(description = "Verify Add and Pay merchant acquirings")
    public void validateAddNPayMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.AddnPay.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.AddnPay, 3);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD,
                PayMethodType.NET_BANKING, PayMethodType.UPI);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiring(PayMethodType.NET_BANKING, UtilConstants.BankName.ICICI);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD, PayMethodType.CREDIT_CARD,
                        PayMethodType.NET_BANKING, PayMethodType.BALANCE);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        verifyMerchantPreference(MerchantPrefType.ADD_MONEY_ENABLED);
        acquiring.AssertAll();
    }

    @Test(description = "Verify Wallet Only merchant acquirings")
    public void validateWalletOnlyMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.WalletOnly.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.WalletOnly, 3);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.BALANCE);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        acquiring.AssertAll();
    }

    @Test(description = "Verify Hybrid merchant acquiring")
    public void validateHybridMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.Hybrid.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.Hybrid, 3);
        verifyMerchantPreference(MerchantPrefType.HYBRID_ALLOWED);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.BALANCE,
                        PayMethodType.NET_BANKING, PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD,
                        PayMethodType.HYBRID_PAYMENT);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING,
                PayMethodType.DEBIT_CARD);
        acquiring.verifyAcquiring(PayMethodType.NET_BANKING, UtilConstants.BankName.ICICI).
                verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC).
                verifyAcquiring(PayMethodType.DEBIT_CARD, UtilConstants.BankName.HDFC).
                AssertAll();
    }

    @Test(description = "Verify COD merchant acquiring and details")
    public void validateCODMerchant() {
        boolean status = false;
        String mid = Constants.MerchantType.COD.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.COD, 3);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.COD);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.COD);
        acquiring.verifyAcquiring(PayMethodType.COD, UtilConstants.BankName.CODMOCK).
                AssertAll();
    }

    @Test(description = "Verify PaytmExpress_Hybrid_Onus merchant Acquiring and details")
    public void validatePaythExpressHybridMerchant() {
        boolean status = false;
        boolean seamlessStatus = false;
        String mid = Constants.MerchantType.PaytmExpress_Hybrid_Onus.getId();
        fetchMerchantFromAlipay(Constants.MerchantType.PaytmExpress_Hybrid_Onus, 3);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.BALANCE, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.HYBRID_PAYMENT);
                status = true;
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("SeamlessPaymentAcquiringProd")) {
                seamlessStatus = true;
                Reporter.report.info("SeamlessPaymentAcquiringProd Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.NET_BANKING,
                        PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD);
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("SeamlessPaymentAcquiringProd Contract not found for mid: " + mid);
        acquiring.verifyAcquiringByPaymethod(PayMethodType.NET_BANKING,
                PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD).
                verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC).
                AssertAll();
    }

    @Test(description = "Verify EMI merchant Acquiring and details")
    public void validateEmiMerchant() {
        String mid = Constants.MerchantType.EMI.getId();
        boolean status = false;
        fetchMerchantFromAlipay(Constants.MerchantType.EMI, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC);
        List<EmiConfigInfos> emiConfigInfosList = emiUtils.getEmiConfigByBankName(EmiUtils.EmiBank.HDFC);
        if (emiConfigInfosList.size() != 0) {
            Reporter.report.info(EmiUtils.EmiBank.HDFC.toString() + " emi is available for mid: " + mid);
        }

        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.EMI, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        emiUtils.AssertAll();

    }

    @Test(description = "Verify PGOnly_Retry merchant Acquiring and details")
    public void validatePGOnly_RetryMerchant() {
        String mid = Constants.MerchantType.PGOnly_Retry.getId();
        boolean status = false;
        fetchMerchantFromAlipay(Constants.MerchantType.PGOnly_Retry, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        int retryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if (retryCount > 0)
            Reporter.report.info("Retry count is greater than 0 for mid: " + mid);
        else
            this.softly.fail("Retry count is 0 for merchant mid: " + mid + ". Unable to perform retry test");
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        this.softly.assertAll();
    }

    @Test(description = "Verify Hybrid_Retry merchant Acquiring and details")
    public void validateHybrid_RetryMerchant() {
        String mid = Constants.MerchantType.Hybrid_Retry.getId();
        boolean status = false;
        fetchMerchantFromAlipay(Constants.MerchantType.Hybrid_Retry, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        int retryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if (retryCount > 0)
            Reporter.report.info("Retry count is greater than 0 for mid: " + mid);
        else
            this.softly.fail("Retry count is 0 for merchant mid: " + mid + ". Unable to perform retry test");
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.HYBRID_PAYMENT);
                status = true;
            }
        }
        verifyMerchantPreference(MerchantPrefType.HYBRID_ALLOWED);
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        this.softly.assertAll();
    }

    @Test(description = "Verify AddnPay_Retry merchant Acquiring and details")
    public void validateAddnPay_RetryMerchant() {
        String mid = Constants.MerchantType.AddnPay_Retry.getId();
        boolean status = false;
        fetchMerchantFromAlipay(Constants.MerchantType.AddnPay_Retry, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        int retryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if (retryCount > 0)
            Reporter.report.info("Retry count is greater than 0 for mid: " + mid);
        else
            this.softly.fail("Retry count is 0 for merchant mid: " + mid + ". Unable to perform retry test");
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.BALANCE);
                status = true;
                if (contractDetails.getProductCondition().getExtendInfo().equalsIgnoreCase("{\"isSupportAddPay\":\"Y\"}"))
                    Reporter.report.info("Add & Pay is supported for StandardDirectPayAcquiringProd contract: " + contractDetails.getProductCondition().getExtendInfo());
                else
                    this.softly.fail("Add & Pay is not supported for defaul contract: " + contractDetails.getProductCondition().getExtendInfo());
            }

        }
        verifyMerchantPreference(MerchantPrefType.ADD_MONEY_ENABLED);
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        this.softly.assertAll();
    }

    //    @Test(description = "Verify Seamless_Hybrid_Onus merchant Acquiring and details")
    public void validateSeamless_Hybrid_OnusMerchant() {
        String mid = Constants.MerchantType.Seamless_Hybrid_Onus.getId();
        boolean status = false;
        fetchMerchantFromAlipay(Constants.MerchantType.Seamless_Hybrid_Onus, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.BALANCE);
                status = true;
                if (contractDetails.getProductCondition().getExtendInfo().equalsIgnoreCase("{\"isSupportAddPay\":\"Y\"}"))
                    Reporter.report.info("Add & Pay is supported for StandardDirectPayAcquiringProd contract: " + contractDetails.getProductCondition().getExtendInfo());
                else
                    this.softly.fail("Add & Pay is not supported for defaul contract: " + contractDetails.getProductCondition().getExtendInfo());
            }

        }
        verifyMerchantPreference(MerchantPrefType.ADD_MONEY_ENABLED);
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);

        this.softly.assertAll();
    }

    @Test(description = "Verify Seamless_Hybrid_Offus merchant Acquiring and details")
    public void validateSeamless_Hybrid_OffusMerchant() {
        String mid = Constants.MerchantType.Seamless_Hybrid_Offus.getId();
        boolean status = false, seamlessStatus = false;
        fetchMerchantFromAlipay(Constants.MerchantType.Seamless_Hybrid_Offus, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.BALANCE, PayMethodType.HYBRID_PAYMENT);
                status = true;
                if (contractDetails.getProductCondition().getExtendInfo().equalsIgnoreCase("{\"isSupportHybridPayment\":\"Y\"}"))
                    Reporter.report.info("Hybrid is supported for StandardDirectPayAcquiringProd contract: " + contractDetails.getProductCondition().getExtendInfo());
                else
                    this.softly.fail("Hybrid is not supported for defaul contract: " + contractDetails.getProductCondition().getExtendInfo());
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("SeamlessPaymentAcquiringProd")) {
                Reporter.report.info("SeamlessPaymentAcquiringProd Contract found for mid: " + mid);
                seamlessStatus = true;
            }

        }
        verifyMerchantPreference(MerchantPrefType.HYBRID_ALLOWED);
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("SeamlessPaymentAcquiringProd Contract not found for mid: " + mid);
        this.softly.assertAll();
    }

    @Test(description = "Verify Subscription_PGOnly merchant Acquiring and details")
    public void validateSubscription_PGOnlyMerchant() {
        String mid = Constants.MerchantType.Subscription_PGOnly.getId();
        boolean status = false, seamlessStatus = false;
        fetchMerchantFromAlipay(Constants.MerchantType.Subscription_PGOnly, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("RecurringAcquiringProd")) {
                Reporter.report.info("RecurringAcquiringProd Contract found for mid: " + mid);
                seamlessStatus = true;
            }

        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("RecurringAcquiringProd Contract not found for mid: " + mid);
        this.softly.assertAll();
    }

    @Test(description = "Verify SUBSCRIPTION_PGONLY_RETRY merchant Acquiring and details")
    public void validate_SUBSCRIPTION_PGONLY_RETRY_Merchant() {
        String mid = Constants.MerchantType.SUBSCRIPTION_PGONLY_RETRY.getId();
        boolean status = false, seamlessStatus = false;
        fetchMerchantFromAlipay(Constants.MerchantType.SUBSCRIPTION_PGONLY_RETRY, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        int retryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if (retryCount > 0)
            Reporter.report.info("Retry count is greater than 0 for mid: " + mid);
        else
            this.softly.fail("Retry count is 0 for merchant mid: " + mid + ". Unable to perform retry test");
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("RecurringAcquiringProd")) {
                Reporter.report.info("RecurringAcquiringProd Contract found for mid: " + mid);
                seamlessStatus = true;
            }

        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("RecurringAcquiringProd Contract not found for mid: " + mid);
        this.softly.assertAll();
    }

    @Test(description = "Verify SUBSCRIPTION_PPI merchant Acquiring and details")
    public void validate_SUBSCRIPTION_PPI_Merchant() {
        String mid = Constants.MerchantType.SUBSCRIPTION_PPI.getId();
        boolean status = false, seamlessStatus = false;
        fetchMerchantFromAlipay(Constants.MerchantType.SUBSCRIPTION_PPI, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("RecurringAcquiringProd")) {
                Reporter.report.info("RecurringAcquiringProd Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.BALANCE);
                seamlessStatus = true;
            }

        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("RecurringAcquiringProd Contract not found for mid: " + mid);
        this.softly.assertAll();
    }

    @Test(description = "Verify SUBSCRIPTION_PPI_RETRY merchant Acquiring and details")
    public void validate_SUBSCRIPTION_PPI_RETRY_Merchant() {
        String mid = Constants.MerchantType.SUBSCRIPTION_PPI_RETRY.getId();
        boolean status = false, seamlessStatus = false;
        fetchMerchantFromAlipay(Constants.MerchantType.SUBSCRIPTION_PPI_RETRY, 3);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiringByPaymethod(PayMethodType.CREDIT_CARD, PayMethodType.DEBIT_CARD, PayMethodType.NET_BANKING);
        int retryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if (retryCount > 0)
            Reporter.report.info("Retry count is greater than 0 for mid: " + mid);
        else
            this.softly.fail("Retry count is 0 for merchant mid: " + mid + ". Unable to perform retry test");

        for (ContractDetails contractDetails : merchantHelper.getAllContracts()) {
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("StandardDirectPayAcquiringProd")) {
                Reporter.report.info("Default Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING);
                status = true;
            }
            if (contractDetails.getContractBasic().getProductName().
                    equalsIgnoreCase("RecurringAcquiringProd")) {
                Reporter.report.info("RecurringAcquiringProd Contract found for mid: " + mid);
                acquiring.verifyPaymethods(contractDetails, PayMethodType.DEBIT_CARD,
                        PayMethodType.CREDIT_CARD, PayMethodType.NET_BANKING, PayMethodType.BALANCE);
                seamlessStatus = true;
            }

        }
        if (!status)
            this.softly.fail("Default Contract not found for mid: " + mid);
        if (!seamlessStatus)
            this.softly.fail("RecurringAcquiringProd Contract not found for mid: " + mid);
        this.softly.assertAll();
    }




    @Test(description = "Verify Scan N Pay Description Offline Text")
    public void validateOfflineDescText() {
        fetchMerchantFromAlipay(Constants.MerchantType.PGOnly, 3);

        verifyMerchantPreference(MerchantPrefType.OFFLINE_SNP_DES_TXT);
    }


    @Test(description = "Verify Scan N Pay Description Offline Flag")
    public void validateOfflineDescFlag() {
        fetchMerchantFromAlipay(Constants.MerchantType.PGOnly, 3);
        verifyMerchantPreference(MerchantPrefType.OFFLINE_SNP_DES_TXT);
    }





//================================================================================

    private void verifyWalletAcquiring(List<ContractDetails> contractDetails, String... s) {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(s));
        for (ContractDetails contractDetail : contractDetails) {
            String prodName = contractDetail.getContractBasic().getProductName();
            if (arrayList.contains(prodName))
                acquiring.verifyWalletAcquiring(contractDetail);
        }
    }

    private void verifyMerchantPreference(MerchantPrefType merchantPrefType) {
        String value = merchantHelper.getMerchantPrefValue(merchantPrefType, merchantHelper.getMerchantPrefInfo());
        if (value.equals("Y"))
            Reporter.report.info(merchantPrefType.toString() + " prefence type is ACTIVE");
        else
            this.softly.fail(merchantPrefType.toString() + " is not enabled");
    }

    public void fetchMerchantFromAlipay(Constants.MerchantType merchantType, int retry) {
        String mid = merchantType.getId();
        merchantHelper = new GetMerchantHelper();
        merchantHelper.fetchMetchantDetails(mid, retry, 15);
        acquiring = new Acquiring(merchantHelper, true);
        emiUtils = new EmiUtils(merchantHelper, softly);
        merchExtendedInfo = merchantHelper.getMerchantExtendedInfo();
    }
}
