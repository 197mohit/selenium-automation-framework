package com.paytm.utils.merchant.util;

import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.UtilConstants;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.MerchantAcquiring;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.acquiringDTO.AcquiringConfigInfos;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.ContractDetails;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import org.assertj.core.api.SoftAssertions;

import java.util.*;

public class Acquiring {
    private GetMerchantHelper getMerchantHelper;
    private List<Map> acquiringList;
    private List<Map> acquiringListByUser = new ArrayList<>();
    private SoftAssertions softly = new SoftAssertions();
    static boolean status;
    MerchantAcquiring merchantAcquiring;

    public Acquiring(GetMerchantHelper getMerchantHelper, boolean enableStatus) {
        this.getMerchantHelper = getMerchantHelper;
        this.acquiringList = getMerchantHelper.getAcquiringsByEnabledStatus(enableStatus);
        merchantAcquiring = getMerchantHelper.getMerchantAcquiringInfo();
    }

    public Map iterateAcquirings(PayMethodType payMethodType, UtilConstants.BankName bankName) {

        for (Map node : acquiringList) {
            String payMethod = node.get("payMethod").toString();
            String serviceInstId = node.get("serviceInstId").toString();

            if (node.get("payMethod").equals(payMethodType.toString()) &&
                    node.get("serviceInstId").equals(bankName.toString())) {
                Reporter.report.info("Pay method: " + payMethodType.toString() + " and Bank Name: " + bankName.toString()
                        + " acquiring available in Get Merchant API");
                status = true;
                return node;
            }
        }
        status = false;

        return Collections.emptyMap();
    }

    public Acquiring verifyAcquiring(PayMethodType payMethodType, UtilConstants.BankName bankName) {
        iterateAcquirings(payMethodType, bankName);
        if (!status) {
            this.softly.fail("Pay method: " + payMethodType.toString() +
                    " and Bank Name: " + bankName.toString() + " not found in Get merchant API acquring list");

        }
        return this;
    }

    /**
     * Use this when user needs to verify Acquiring by minimum paymethod
     * @return
     */
    public Acquiring verifyAcquiringByPaymethod(PayMethodType... payMethodType) {
        List<PayMethodType> payMethodList = new ArrayList<>(Arrays.asList(payMethodType));
        List<String> paymethodsAPI = new ArrayList<>();
        for(AcquiringConfigInfos acquiringConfigInfo : merchantAcquiring.getAcquiringConfigInfos())
            paymethodsAPI.add(acquiringConfigInfo.getPayMethod());

        for(PayMethodType paymethod : payMethodList) {
            if(paymethodsAPI.contains(paymethod.toString()))
                Reporter.report.info(paymethod.toString() + " paymethod is available in Merchant acquirings");
            else
                this.softly.fail(paymethod.toString() + " paymethod is not available in Merchant acquirings");
        }
        return this;
    }

    /**
     * Use this when user needs to verify Acquiring by minimum bank
     * @return
     */
    public Acquiring verifyAcquiringByBankName(UtilConstants.BankName... bankNames) {
        List<UtilConstants.BankName> bankNameList = new ArrayList<>(Arrays.asList(bankNames));
        List<String> bankNameAPI = new ArrayList<>();
        for(AcquiringConfigInfos acquiringConfigInfo : merchantAcquiring.getAcquiringConfigInfos())
            bankNameAPI.add(acquiringConfigInfo.getServiceInstId());
        for(UtilConstants.BankName bankName : bankNameList) {
            if(bankNameAPI.contains(bankName.toString()))
                Reporter.report.info(bankName.toString()+" bankname is available in Merchant Acquirings");
            else
                this.softly.fail(bankName.toString()+" bankname is not available in Merchant Acquirings");
        }
        return this;
    }

    public Acquiring verifyWalletAcquiring(ContractDetails contractDetails) {
        List<String> paymethods = contractDetails.getProductCondition().getPayMethods();
        String productName = contractDetails.getContractBasic().getProductName();
        if(paymethods.contains("BALANCE"))
            Reporter.report.info("Wallet acquiring applied for CONTRACT: "+productName);
        else
            this.softly.fail("Wallet acquiring not found for CONTRACT: "+productName);
        return this;
    }

    public Acquiring verifyPaymethods(ContractDetails contractDetails, PayMethodType... payMethodTypes) {
        List<PayMethodType> paymethodList = new ArrayList<>(Arrays.asList(payMethodTypes));
        List<String> paymethodFromContract = contractDetails.getProductCondition().getPayMethods();
        String productName = contractDetails.getContractBasic().getProductName();
        for(PayMethodType payMethod : paymethodList) {
            if(paymethodFromContract.contains(payMethod.toString())) {
                Reporter.report.info(payMethod.toString() +" paymethod available in contract "+ productName);
            }
            else
                this.softly.fail(payMethod.toString()+" paymethod is not available in contract "+productName);
        }
        return this;
    }

    public void verifyAppliedAcquringOnly(VerifyAcquiring... verifyAcquirings) {
        boolean flag = false;
        boolean log = true;
        for (VerifyAcquiring verifyAcquiring : verifyAcquirings) {
            String serviceInstId = verifyAcquiring.getMap().get("serviceInstId");
            String paymethod = verifyAcquiring.getMap().get("payMethod");
            for (Map acquiringFromAPI : acquiringList) {

                if (acquiringFromAPI.containsValue(serviceInstId) && acquiringFromAPI.containsValue(paymethod)) {
                    Reporter.report.info("Pay method: " + paymethod + " and Bank Name: " + serviceInstId
                            + " acquiring available in Get Merchant API");
                    acquiringList.remove(acquiringFromAPI);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                log = false;
                this.softly.fail("Paymethod: "+paymethod+" and Bankname: "+serviceInstId+
                            "are not available in Get Merchant API");
            }
        }

        if (acquiringList.size() > 0 && log) {
            for (Map acquiringFromAPI : acquiringList) {
                String serviceInstId = acquiringFromAPI.get("serviceInstId").toString();
                String paymethod = acquiringFromAPI.get("payMethod").toString();
                this.softly.fail("Paymethod: " + paymethod + " and Bankname: " + serviceInstId +
                        " are available extra in Get merchant API");
            }
        }
        this.AssertAll();
    }

    public Acquiring setSoftly(SoftAssertions softly) {
        this.softly = softly;
        return this;
    }

    public SoftAssertions getSoftly() {
        return softly;
    }

    public Acquiring AssertAll() {
        this.softly.assertAll();
        return this;
    }

}
