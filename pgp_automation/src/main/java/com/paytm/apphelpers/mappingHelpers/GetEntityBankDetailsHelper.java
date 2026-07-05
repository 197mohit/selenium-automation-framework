package com.paytm.apphelpers.mappingHelpers;

import com.paytm.api.MappingService.GetEntityBankDetails;
import com.paytm.dto.mappingService.response.EntityBankDetailsResponse;
import com.paytm.framework.Assertion.ValidateSoftly;

public class GetEntityBankDetailsHelper {

	private ValidateSoftly validateSoftly;
	private EntityBankDetailsResponse entityBankDetailsResponse;

	public GetEntityBankDetailsHelper(String bankCode) {

		validateSoftly = ValidateSoftly.getInstance();
		entityBankDetailsResponse = GetEntityBankDetails.executeEntityBankDetails(bankCode);
	}

	public GetEntityBankDetailsHelper validateBankId(String bankId) {
		this.validateSoftly.validate(entityBankDetailsResponse.getBankId(), "", "Validated Bank Id").isEqualTo(bankId);
		return this;
	}

	public GetEntityBankDetailsHelper validateBankName(String bankName) {
		this.validateSoftly.validate(entityBankDetailsResponse.getBankName(), "", "Validated Bank Name")
				.isEqualTo(bankName);
		return this;
	}

	public GetEntityBankDetailsHelper validateBankCode(String bankCode) {
		this.validateSoftly.validate(entityBankDetailsResponse.getBankCode(), "", "Validated Bank Code")
				.isEqualTo(bankCode);
		return this;
	}

	public GetEntityBankDetailsHelper validateBankDisplayName(String bankDisplayName) {
		this.validateSoftly.validate(entityBankDetailsResponse.getBankDisplayName(), "", "Validated Bank Display Name")
				.isEqualTo(bankDisplayName);
		return this;
	}

	public GetEntityBankDetailsHelper validateBankKey(String bankKey) {
		this.validateSoftly.validate(entityBankDetailsResponse.getBankKey(), "", "Validated Bank Key")
				.isEqualTo(bankKey);
		return this;
	}

	public GetEntityBankDetailsHelper validateAlipayBankCode(String alipayBankCode) {
		this.validateSoftly.validate(entityBankDetailsResponse.getAlipayBankCode(), "", "Validated Alipay Bank Code")
				.isEqualTo(alipayBankCode);
		return this;
	}

	public GetEntityBankDetailsHelper validateStatus(boolean status) {
		this.validateSoftly.validate(entityBankDetailsResponse.isStatus(), "", "Validated Status").isEqualTo(status);
		return this;
	}
	 public void assertAll() {
	        this.validateSoftly.assertAll();
	    }
}
