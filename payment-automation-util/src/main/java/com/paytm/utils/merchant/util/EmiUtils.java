package com.paytm.utils.merchant.util;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.MerchantEMI;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO.EmiConfigInfos;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import org.assertj.core.api.SoftAssertions;

import java.util.ArrayList;
import java.util.List;

public class EmiUtils {
    private GetMerchantHelper getMerchantHelper;
    private SoftAssertions softly = new SoftAssertions();
    private MerchantEMI merchantEMI;

    public EmiUtils(GetMerchantHelper getMerchantHelper) {
        this.getMerchantHelper = getMerchantHelper;
        merchantEMI = getMerchantHelper.getMerchantEmiInfo();
    }

    public EmiUtils (GetMerchantHelper getMerchantHelper, SoftAssertions softly) {
        this.getMerchantHelper = getMerchantHelper;
        merchantEMI = getMerchantHelper.getMerchantEmiInfo();
        this.softly = softly;
    }

    public List<EmiConfigInfos> getEmiConfigByBankName(EmiBank emiBank) {
        List<EmiConfigInfos> list = new ArrayList<>();
        boolean status=false;
        for(EmiConfigInfos emiConfigInfo : merchantEMI.getEmiConfigInfos()) {
            if(emiConfigInfo.getIssuingBank().getIssuingBankName().
                    equalsIgnoreCase(emiBank.toString())) {
                list.add(emiConfigInfo);
                status = true;
            }

        }
        if(!status)
            this.softly.fail(emiBank.toString()+" emi bank is not available in get merchant API");
        return list;
    }

    public enum EmiBank {
        YES("Yes Bank"),
        SCB("Standard Chartered Bank"),
        RATN("RBL Bank"),
        INDS("IndusInd Bank"),
        ICICI("ICICI Bank"),
        HSBC("HSBC Bank"),
        HDFC("HDFC Bank")

        ;
        private String bankName;

        EmiBank(String bankName) { this.bankName = bankName; }

        public String toString() { return this.bankName; }
    }

    public EmiUtils setSoftly(SoftAssertions softly) {
        this.softly = softly;
        return this;
    }

    public SoftAssertions getSoftly() {
        return softly;
    }

    public EmiUtils AssertAll() {
        this.softly.assertAll();
        return this;
    }
}
