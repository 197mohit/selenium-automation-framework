package com.paytm.dto.mappingService.response;



/**
 * @author rajeshkumar
 *
 */
public class EntityBankDetailsResponse {
	
	
	private String bankId;
	private String bankName;
	private String bankCode;
	private String bankDisplayName;
	private String bankKey;
	private String alipayBankCode;
	private String bankWebLogo;
	private String bankWapLogo;
	boolean status;
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getBankDisplayName() {
		return bankDisplayName;
	}
	public void setBankDisplayName(String bankDisplayName) {
		this.bankDisplayName = bankDisplayName;
	}
	public String getBankKey() {
		return bankKey;
	}
	public void setBankKey(String bankKey) {
		this.bankKey = bankKey;
	}
	public String getAlipayBankCode() {
		return alipayBankCode;
	}
	public void setAlipayBankCode(String alipayBankCode) {
		this.alipayBankCode = alipayBankCode;
	}
	public String getBankWebLogo() {
		return bankWebLogo;
	}
	public void setBankWebLogo(String bankWebLogo) {
		this.bankWebLogo = bankWebLogo;
	}
	public String getBankWapLogo() {
		return bankWapLogo;
	}
	public void setBankWapLogo(String bankWapLogo) {
		this.bankWapLogo = bankWapLogo;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
	

}
