package com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.ContractBasic;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.ContractRelation;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.ProductCondition;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.ResultInfo;

import java.util.List;

public class ContractDetails {

    private ResultInfo resultInfo;
    private ContractBasic contractBasic;
    private String contractTemplate;
    private ProductCondition productCondition;
    private List<ContractRelation> contractRelation;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public ContractBasic getContractBasic() {
        return contractBasic;
    }

    public String getContractTemplate() {
        return contractTemplate;
    }

    public ProductCondition getProductCondition() {
        return productCondition;
    }

    public List<ContractRelation> getContractRelation() {
        return contractRelation;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public void setContractBasic(ContractBasic contractBasic) {
        this.contractBasic = contractBasic;
    }

    public void setContractTemplate(String contractTemplate) {
        this.contractTemplate = contractTemplate;
    }

    public void setProductCondition(ProductCondition productCondition) {
        this.productCondition = productCondition;
    }

    public void setContractRelation(List<ContractRelation> contractRelations) {
        this.contractRelation = contractRelations;
    }
}

