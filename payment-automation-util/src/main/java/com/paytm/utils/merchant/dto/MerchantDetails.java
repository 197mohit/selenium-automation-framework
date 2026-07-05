
package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "MID",
        "REQUEST_ID",
        "USER_NAME",
        "ACCOUNT_FOR",
        "SOURCE_ID",
        "MERCHANT_TYPE",
        "OFFLINE_ENABLED",
        "PPI_LIMITED_MERCHANT",
        "BUSINESS_NAME",
        "CALLBACK_URL_ENABLED",
        "CUSTOM",
        "MERCHANT_NAME",
        "CURRENCY",
        "REFUND_TO_BANK_ENABLED",
        "STORE_CARD_DETAILS",
        "ADD_MONEY_ENABLE",
        "CHECKSUM_ENABLED",
        "NUMBER_OF_RETRY",
        "CATEGORY",
        "SUB_CATEGORY",
        "INDUSTRY_TYPE",
        "SIZE_OF_KEY",
        "WALLET_RECHARGE_OPT",
        "PROFILE_ID",
        "WALLET_ONLY_ENABLED",
        "EMAIL_ALERT",
        "AGGREGATOR_ID",
        "KYB_ID",
        "CONVENIENCE_FEE_TYPE",
        "VALID_FROM",
        "VALID_TO",
        "MULTI_SUPPORT",
        "HOW_MANY",
        "OCP",
        "CUSTOM_NAME",
        "REQUEST_NAME",
        "FIRST_NAME",
        "LAST_NAME",
        "MOBILE_NUMBER",
        "PHONE_NUMBER",
        "P_EMAIL",
        "MerchUniqRef",
        "S_FIRST_NAME",
        "S_EMAIL",
        "INVOICE_EMAIL",
        "ACCOUNT_PRIMARY",
        "CAN_EDIT_PMOBILE",
        "IS_SUB_USER",
        "ADDRESS1",
        "ADDRESS2",
        "ADDRESS3",
        "COUNTRY",
        "STATE",
        "CITY",
        "PIN",
        "SAME_AS_BUSINESS_ADDR",
        "COMMUNICATION_ADDRESS1",
        "COMMUNICATION_ADDRESS2",
        "COMMUNICATION_ADDRESS3",
        "COMMUNICATION_COUNTRY",
        "COMMUNICATION_STATE",
        "COMMUNICATION_CITY",
        "COMMUNICATION_PIN",
        "KYC_BANK_NAME",
        "KYC_BANK_ACCOUNT_HOLDER_NAME",
        "KYC_BANK_ACCOUNT_NO",
        "KYC_BUSINESS_PAN_NO",
        "KYC_AUTHORIZED_SIGNATORY_PAN_NO",
        "KYC_BUSINESS_IFSC_NO",
        "KYC_AUTHORIZED_SIGNATORY_NAME",
        "KYC_AUTHORIZED_SIGNATORY_PROOF_NO",
        "KYC_AUTHORIZED_SIGNATORY_ID_PROOF_NO",
        "COMM_STAT_SELECT",
        "EMAIL_MERCHANT",
        "EMAIL_CONSUMER",
        "REQUEST_TYPE",
        "SAP_CODE",
        "KYC_BUSINESS_GSTIN",
        "ENABLE_QR_TAG",
        "MERCHANT_QR_TAG"
})
public class MerchantDetails {

    @JsonProperty("MID")
    public String mID;
    @JsonProperty("REQUEST_ID")
    public String rEQUESTID;
    @JsonProperty("SOURCE_ID")
    public String sOURCEID;
    @JsonProperty("MERCHANT_TYPE")
    public String mERCHANTTYPE;
    @JsonProperty("OFFLINE_ENABLED")
    public Boolean oFFLINEENABLED;
    @JsonProperty("PPI_LIMITED_MERCHANT")
    public String pPILIMITEDMERCHANT;
    @JsonProperty("BUSINESS_NAME")
    public String bUSINESSNAME;
    @JsonProperty("CALLBACK_URL_ENABLED")
    public Boolean cALLBACKURLENABLED;
    @JsonProperty("CUSTOM")
    public String cUSTOM;
    @JsonProperty("MERCHANT_NAME")
    public String mERCHANTNAME;
    @JsonProperty("CURRENCY")
    public String cURRENCY;
    @JsonProperty("WEBSITE_NAME")
    public String wEBSITENAME;
    @JsonProperty("REFUND_TO_BANK_ENABLED")
    public Boolean rEFUNDTOBANKENABLED;
    @JsonProperty("STORE_CARD_DETAILS")
    public String sTORECARDDETAILS;
    @JsonProperty("ADD_MONEY_ENABLE")
    public Boolean aDDMONEYENABLE;
    @JsonProperty("HYBRID_TXN_ENABLE")
    public Boolean hybridEnabled;
    @JsonProperty("CHECKSUM_ENABLED")
    public Boolean cHECKSUMENABLED;
    @JsonProperty("NUMBER_OF_RETRY")
    public Integer nUMBEROFRETRY;
    @JsonProperty("CATEGORY")
    public String cATEGORY;
    @JsonProperty("SUB_CATEGORY")
    public String sUBCATEGORY;
    @JsonProperty("INDUSTRY_TYPE")
    public String iNDUSTRYTYPE;
    @JsonProperty("SIZE_OF_KEY")
    public Integer sIZEOFKEY;
    @JsonProperty("WALLET_RECHARGE_OPT")
    public String wALLETRECHARGEOPT;
    @JsonProperty("PROFILE_ID")
    public String pROFILEID;
    @JsonProperty("WALLET_ONLY_ENABLED")
    public Boolean wALLETONLYENABLED;
    @JsonProperty("EMAIL_ALERT")
    public Boolean eMAILALERT;
    @JsonProperty("AGGREGATOR_ID")
    public String aGGREGATORID;
    @JsonProperty("KYB_ID")
    public String kYBID;
    @JsonProperty("CONVENIENCE_FEE_TYPE")
    public String cONVENIENCEFEETYPE;
    @JsonProperty("VALID_FROM")
    public String vALIDFROM;
    @JsonProperty("VALID_TO")
    public String vALIDTO;
    @JsonProperty("MULTI_SUPPORT")
    public String mULTISUPPORT;
    @JsonProperty("HOW_MANY")
    public Integer hOWMANY;
    @JsonProperty("OCP")
    public Boolean oCP;
    @JsonProperty("CUSTOM_NAME")
    public String cUSTOMNAME;
    @JsonProperty("REQUEST_NAME")
    public String rEQUESTNAME;
    @JsonProperty("FIRST_NAME")
    public String fIRSTNAME;
    @JsonProperty("LAST_NAME")
    public String lASTNAME;
    @JsonProperty("USER_NAME")
    public String uSERNAME;
    @JsonProperty("MOBILE_NUMBER")
    public String mOBILENUMBER;
    @JsonProperty("PHONE_NUMBER")
    public String pHONENUMBER;
    @JsonProperty("P_EMAIL")
    public String pEMAIL;
    @JsonProperty("MerchUniqRef")
    public String merchUniqRef;
    @JsonProperty("S_FIRST_NAME")
    public String sFIRSTNAME;
    @JsonProperty("S_EMAIL")
    public String sEMAIL;
    @JsonProperty("INVOICE_EMAIL")
    public String iNVOICEEMAIL;
    @JsonProperty("ACCOUNT_FOR")
    public String aCCOUNTFOR;
    @JsonProperty("ACCOUNT_PRIMARY")
    public String aCCOUNTPRIMARY;
    @JsonProperty("CAN_EDIT_PMOBILE")
    public String cANEDITPMOBILE;
    @JsonProperty("IS_SUB_USER")
    public Boolean iSSUBUSER;
    @JsonProperty("ADDRESS1")
    public String aDDRESS1;
    @JsonProperty("ADDRESS2")
    public String aDDRESS2;
    @JsonProperty("ADDRESS3")
    public String aDDRESS3;
    @JsonProperty("COUNTRY")
    public String cOUNTRY;
    @JsonProperty("STATE")
    public String sTATE;
    @JsonProperty("CITY")
    public String cITY;
    @JsonProperty("PIN")
    public String pIN;
    @JsonProperty("SAME_AS_BUSINESS_ADDR")
    public Boolean sAMEASBUSINESSADDR;
    @JsonProperty("COMMUNICATION_ADDRESS1")
    public String cOMMUNICATIONADDRESS1;
    @JsonProperty("COMMUNICATION_ADDRESS2")
    public String cOMMUNICATIONADDRESS2;
    @JsonProperty("COMMUNICATION_ADDRESS3")
    public String cOMMUNICATIONADDRESS3;
    @JsonProperty("COMMUNICATION_COUNTRY")
    public String cOMMUNICATIONCOUNTRY;
    @JsonProperty("COMMUNICATION_STATE")
    public String cOMMUNICATIONSTATE;
    @JsonProperty("COMMUNICATION_CITY")
    public String cOMMUNICATIONCITY;
    @JsonProperty("COMMUNICATION_PIN")
    public String cOMMUNICATIONPIN;
    @JsonProperty("KYC_BANK_NAME")
    public String kYCBANKNAME;
    @JsonProperty("KYC_BANK_ACCOUNT_HOLDER_NAME")
    public String kYCBANKACCOUNTHOLDERNAME;
    @JsonProperty("KYC_BANK_ACCOUNT_NO")
    public String kYCBANKACCOUNTNO;
    @JsonProperty("KYC_BUSINESS_PAN_NO")
    public String kYCBUSINESSPANNO;
    @JsonProperty("KYC_AUTHORIZED_SIGNATORY_PAN_NO")
    public String kYCAUTHORIZEDSIGNATORYPANNO;
    @JsonProperty("KYC_BUSINESS_IFSC_NO")
    public String kYCBUSINESSIFSCNO;
    @JsonProperty("KYC_AUTHORIZED_SIGNATORY_NAME")
    public String kYCAUTHORIZEDSIGNATORYNAME;
    @JsonProperty("KYC_AUTHORIZED_SIGNATORY_PROOF_NO")
    public String kYCAUTHORIZEDSIGNATORYPROOFNO;
    @JsonProperty("KYC_AUTHORIZED_SIGNATORY_ID_PROOF_NO")
    public String kYCAUTHORIZEDSIGNATORYIDPROOFNO;
    @JsonProperty("COMM_STAT_SELECT")
    public String cOMMSTATSELECT;
    @JsonProperty("EMAIL_MERCHANT")
    public Boolean eMAILMERCHANT;
    @JsonProperty("EMAIL_CONSUMER")
    public Boolean eMAILCONSUMER;
    @JsonProperty("REQUEST_TYPE")
    public String rEQUESTTYPE;
    @JsonProperty("KYC_BUSINESS_GSTIN")
    public String kYCBUSINESSGSTIN;
    @JsonProperty("SAP_CODE")
    public String sapCode;
    @JsonProperty("ENABLE_QR_TAG")
    public String enableQRTag;

    public String getEnableQRTag() {
        return enableQRTag;
    }

    public MerchantDetails setEnableQRTag(String enableQRTag) {
        this.enableQRTag = enableQRTag;
        return this;
    }

    public String getSapCode() {
        return sapCode;
    }

    public MerchantDetails setSapCode(String sapCode) {
        this.sapCode = sapCode;
        return this;
    }

    public String getmID() {
        return mID;
    }

    public MerchantDetails setmID(String mID) {
        this.mID = mID;
        return this;
    }

    public String getrEQUESTID() {
        return rEQUESTID;
    }

    public MerchantDetails setrEQUESTID(String rEQUESTID) {
        this.rEQUESTID = rEQUESTID;
        return this;
    }

    public String getsOURCEID() {
        return sOURCEID;
    }

    public MerchantDetails setsOURCEID(String sOURCEID) {
        this.sOURCEID = sOURCEID;
        return this;
    }

    public String getmERCHANTTYPE() {
        return mERCHANTTYPE;
    }

    public MerchantDetails setmERCHANTTYPE(String mERCHANTTYPE) {
        this.mERCHANTTYPE = mERCHANTTYPE;
        return this;
    }

    public Boolean getoFFLINEENABLED() {
        return oFFLINEENABLED;
    }

    public MerchantDetails setoFFLINEENABLED(Boolean oFFLINEENABLED) {
        this.oFFLINEENABLED = oFFLINEENABLED;
        return this;
    }

    public String getpPILIMITEDMERCHANT() {
        return pPILIMITEDMERCHANT;
    }

    public MerchantDetails setpPILIMITEDMERCHANT(String pPILIMITEDMERCHANT) {
        this.pPILIMITEDMERCHANT = pPILIMITEDMERCHANT;
        return this;
    }

    public String getbUSINESSNAME() {
        return bUSINESSNAME;
    }

    public MerchantDetails setbUSINESSNAME(String bUSINESSNAME) {
        this.bUSINESSNAME = bUSINESSNAME;
        return this;
    }

    public Boolean iscALLBACKURLENABLED() {
        return cALLBACKURLENABLED;
    }

    public MerchantDetails setcALLBACKURLENABLED(Boolean cALLBACKURLENABLED) {
        this.cALLBACKURLENABLED = cALLBACKURLENABLED;
        return this;
    }

    public String getcUSTOM() {
        return cUSTOM;
    }

    public MerchantDetails setcUSTOM(String cUSTOM) {
        this.cUSTOM = cUSTOM;
        return this;
    }

    public String getmERCHANTNAME() {
        return mERCHANTNAME;
    }

    public MerchantDetails setmERCHANTNAME(String mERCHANTNAME) {
        this.mERCHANTNAME = mERCHANTNAME;
        return this;
    }

    public String getcURRENCY() {
        return cURRENCY;
    }

    public MerchantDetails setcURRENCY(String cURRENCY) {
        this.cURRENCY = cURRENCY;
        return this;
    }

    public String getwEBSITENAME() {
        return wEBSITENAME;
    }

    public MerchantDetails setwEBSITENAME(String wEBSITENAME) {
        this.wEBSITENAME = wEBSITENAME;
        return this;
    }

    public Boolean isrEFUNDTOBANKENABLED() {
        return rEFUNDTOBANKENABLED;
    }

    public MerchantDetails setrEFUNDTOBANKENABLED(Boolean rEFUNDTOBANKENABLED) {
        this.rEFUNDTOBANKENABLED = rEFUNDTOBANKENABLED;
        return this;
    }

    public String getsTORECARDDETAILS() {
        return sTORECARDDETAILS;
    }

    public MerchantDetails setsTORECARDDETAILS(String sTORECARDDETAILS) {
        this.sTORECARDDETAILS = sTORECARDDETAILS;
        return this;
    }

    public Boolean isaDDMONEYENABLE() {
        return aDDMONEYENABLE;
    }

    public MerchantDetails setaDDMONEYENABLE(Boolean aDDMONEYENABLE) {
        this.aDDMONEYENABLE = aDDMONEYENABLE;
        return this;
    }

    public Boolean iscHECKSUMENABLED() {
        return cHECKSUMENABLED;
    }

    public MerchantDetails setcHECKSUMENABLED(Boolean cHECKSUMENABLED) {
        this.cHECKSUMENABLED = cHECKSUMENABLED;
        return this;
    }

    public Integer getnUMBEROFRETRY() {
        return nUMBEROFRETRY;
    }

    public MerchantDetails setnUMBEROFRETRY(Integer nUMBEROFRETRY) {
        this.nUMBEROFRETRY = nUMBEROFRETRY;
        return this;
    }

    public String getcATEGORY() {
        return cATEGORY;
    }

    public MerchantDetails setcATEGORY(String cATEGORY) {
        this.cATEGORY = cATEGORY;
        return this;
    }

    public String getsUBCATEGORY() {
        return sUBCATEGORY;
    }

    public MerchantDetails setsUBCATEGORY(String sUBCATEGORY) {
        this.sUBCATEGORY = sUBCATEGORY;
        return this;
    }

    public String getiNDUSTRYTYPE() {
        return iNDUSTRYTYPE;
    }

    public MerchantDetails setiNDUSTRYTYPE(String iNDUSTRYTYPE) {
        this.iNDUSTRYTYPE = iNDUSTRYTYPE;
        return this;
    }

    public Integer getsIZEOFKEY() {
        return sIZEOFKEY;
    }

    public MerchantDetails setsIZEOFKEY(Integer sIZEOFKEY) {
        this.sIZEOFKEY = sIZEOFKEY;
        return this;
    }

    public String getwALLETRECHARGEOPT() {
        return wALLETRECHARGEOPT;
    }

    public MerchantDetails setwALLETRECHARGEOPT(String wALLETRECHARGEOPT) {
        this.wALLETRECHARGEOPT = wALLETRECHARGEOPT;
        return this;
    }

    public String getpROFILEID() {
        return pROFILEID;
    }

    public MerchantDetails setpROFILEID(String pROFILEID) {
        this.pROFILEID = pROFILEID;
        return this;
    }

    public Boolean getwALLETONLYENABLED() {
        return wALLETONLYENABLED;
    }

    public MerchantDetails setwALLETONLYENABLED(Boolean wALLETONLYENABLED) {
        this.wALLETONLYENABLED = wALLETONLYENABLED;
        return this;
    }

    public Boolean geteMAILALERT() {
        return eMAILALERT;
    }

    public MerchantDetails seteMAILALERT(Boolean eMAILALERT) {
        this.eMAILALERT = eMAILALERT;
        return this;
    }

    public String getaGGREGATORID() {
        return aGGREGATORID;
    }

    public MerchantDetails setaGGREGATORID(String aGGREGATORID) {
        this.aGGREGATORID = aGGREGATORID;
        return this;
    }

    public String getkYBID() {
        return kYBID;
    }

    public MerchantDetails setkYBID(String kYBID) {
        this.kYBID = kYBID;
        return this;
    }

    public String getcONVENIENCEFEETYPE() {
        return cONVENIENCEFEETYPE;
    }

    public MerchantDetails setcONVENIENCEFEETYPE(String cONVENIENCEFEETYPE) {
        this.cONVENIENCEFEETYPE = cONVENIENCEFEETYPE;
        return this;
    }

    public String getvALIDFROM() {
        return vALIDFROM;
    }

    public MerchantDetails setvALIDFROM(String vALIDFROM) {
        this.vALIDFROM = vALIDFROM;
        return this;
    }

    public String getvALIDTO() {
        return vALIDTO;
    }

    public MerchantDetails setvALIDTO(String vALIDTO) {
        this.vALIDTO = vALIDTO;
        return this;
    }

    public String getmULTISUPPORT() {
        return mULTISUPPORT;
    }

    public MerchantDetails setmULTISUPPORT(String mULTISUPPORT) {
        this.mULTISUPPORT = mULTISUPPORT;
        return this;
    }

    public Integer gethOWMANY() {
        return hOWMANY;
    }

    public MerchantDetails sethOWMANY(Integer hOWMANY) {
        this.hOWMANY = hOWMANY;
        return this;
    }

    public Boolean isoCP() {
        return oCP;
    }

    public MerchantDetails setoCP(Boolean oCP) {
        this.oCP = oCP;
        return this;
    }

    public String getcUSTOMNAME() {
        return cUSTOMNAME;
    }

    public MerchantDetails setcUSTOMNAME(String cUSTOMNAME) {
        this.cUSTOMNAME = cUSTOMNAME;
        return this;
    }

    public String getrEQUESTNAME() {
        return rEQUESTNAME;
    }

    public MerchantDetails setrEQUESTNAME(String rEQUESTNAME) {
        this.rEQUESTNAME = rEQUESTNAME;
        return this;
    }

    public String getfIRSTNAME() {
        return fIRSTNAME;
    }

    public MerchantDetails setfIRSTNAME(String fIRSTNAME) {
        this.fIRSTNAME = fIRSTNAME;
        return this;
    }

    public String getlASTNAME() {
        return lASTNAME;
    }

    public MerchantDetails setlASTNAME(String lASTNAME) {
        this.lASTNAME = lASTNAME;
        return this;
    }

    public String getuSERNAME() {
        return uSERNAME;
    }

    public MerchantDetails setuSERNAME(String uSERNAME) {
        this.uSERNAME = uSERNAME;
        return this;
    }

    public String getmOBILENUMBER() {
        return mOBILENUMBER;
    }

    public MerchantDetails setmOBILENUMBER(String mOBILENUMBER) {
        this.mOBILENUMBER = mOBILENUMBER;
        return this;
    }

    public String getpHONENUMBER() {
        return pHONENUMBER;
    }

    public MerchantDetails setpHONENUMBER(String pHONENUMBER) {
        this.pHONENUMBER = pHONENUMBER;
        return this;
    }

    public String getpEMAIL() {
        return pEMAIL;
    }

    public MerchantDetails setpEMAIL(String pEMAIL) {
        this.pEMAIL = pEMAIL;
        return this;
    }

    public String getMerchUniqRef() {
        return merchUniqRef;
    }

    public MerchantDetails setMerchUniqRef(String merchUniqRef) {
        this.merchUniqRef = merchUniqRef;
        return this;
    }

    public String getsFIRSTNAME() {
        return sFIRSTNAME;
    }

    public MerchantDetails setsFIRSTNAME(String sFIRSTNAME) {
        this.sFIRSTNAME = sFIRSTNAME;
        return this;
    }

    public String getsEMAIL() {
        return sEMAIL;
    }

    public MerchantDetails setsEMAIL(String sEMAIL) {
        this.sEMAIL = sEMAIL;
        return this;
    }

    public String getiNVOICEEMAIL() {
        return iNVOICEEMAIL;
    }

    public MerchantDetails setiNVOICEEMAIL(String iNVOICEEMAIL) {
        this.iNVOICEEMAIL = iNVOICEEMAIL;
        return this;
    }

    public String getaCCOUNTFOR() {
        return aCCOUNTFOR;
    }

    public MerchantDetails setaCCOUNTFOR(String aCCOUNTFOR) {
        this.aCCOUNTFOR = aCCOUNTFOR;
        return this;
    }

    public String getaCCOUNTPRIMARY() {
        return aCCOUNTPRIMARY;
    }

    public MerchantDetails setaCCOUNTPRIMARY(String aCCOUNTPRIMARY) {
        this.aCCOUNTPRIMARY = aCCOUNTPRIMARY;
        return this;
    }

    public String getcANEDITPMOBILE() {
        return cANEDITPMOBILE;
    }

    public MerchantDetails setcANEDITPMOBILE(String cANEDITPMOBILE) {
        this.cANEDITPMOBILE = cANEDITPMOBILE;
        return this;
    }

    public Boolean getiSSUBUSER() {
        return iSSUBUSER;
    }

    public MerchantDetails setiSSUBUSER(Boolean iSSUBUSER) {
        this.iSSUBUSER = iSSUBUSER;
        return this;
    }

    public String getaDDRESS1() {
        return aDDRESS1;
    }

    public MerchantDetails setaDDRESS1(String aDDRESS1) {
        this.aDDRESS1 = aDDRESS1;
        return this;
    }

    public String getaDDRESS2() {
        return aDDRESS2;
    }

    public MerchantDetails setaDDRESS2(String aDDRESS2) {
        this.aDDRESS2 = aDDRESS2;
        return this;
    }

    public String getaDDRESS3() {
        return aDDRESS3;
    }

    public MerchantDetails setaDDRESS3(String aDDRESS3) {
        this.aDDRESS3 = aDDRESS3;
        return this;
    }

    public String getcOUNTRY() {
        return cOUNTRY;
    }

    public MerchantDetails setcOUNTRY(String cOUNTRY) {
        this.cOUNTRY = cOUNTRY;
        return this;
    }

    public String getsTATE() {
        return sTATE;
    }

    public MerchantDetails setsTATE(String sTATE) {
        this.sTATE = sTATE;
        return this;
    }

    public String getcITY() {
        return cITY;
    }

    public MerchantDetails setcITY(String cITY) {
        this.cITY = cITY;
        return this;
    }

    public String getpIN() {
        return pIN;
    }

    public MerchantDetails setpIN(String pIN) {
        this.pIN = pIN;
        return this;
    }

    public Boolean getsAMEASBUSINESSADDR() {
        return sAMEASBUSINESSADDR;
    }

    public MerchantDetails setsAMEASBUSINESSADDR(Boolean sAMEASBUSINESSADDR) {
        this.sAMEASBUSINESSADDR = sAMEASBUSINESSADDR;
        return this;
    }

    public String getcOMMUNICATIONADDRESS1() {
        return cOMMUNICATIONADDRESS1;
    }

    public MerchantDetails setcOMMUNICATIONADDRESS1(String cOMMUNICATIONADDRESS1) {
        this.cOMMUNICATIONADDRESS1 = cOMMUNICATIONADDRESS1;
        return this;
    }

    public String getcOMMUNICATIONADDRESS2() {
        return cOMMUNICATIONADDRESS2;
    }

    public MerchantDetails setcOMMUNICATIONADDRESS2(String cOMMUNICATIONADDRESS2) {
        this.cOMMUNICATIONADDRESS2 = cOMMUNICATIONADDRESS2;
        return this;
    }

    public String getcOMMUNICATIONADDRESS3() {
        return cOMMUNICATIONADDRESS3;
    }

    public MerchantDetails setcOMMUNICATIONADDRESS3(String cOMMUNICATIONADDRESS3) {
        this.cOMMUNICATIONADDRESS3 = cOMMUNICATIONADDRESS3;
        return this;
    }

    public String getcOMMUNICATIONCOUNTRY() {
        return cOMMUNICATIONCOUNTRY;
    }

    public MerchantDetails setcOMMUNICATIONCOUNTRY(String cOMMUNICATIONCOUNTRY) {
        this.cOMMUNICATIONCOUNTRY = cOMMUNICATIONCOUNTRY;
        return this;
    }

    public String getcOMMUNICATIONSTATE() {
        return cOMMUNICATIONSTATE;
    }

    public MerchantDetails setcOMMUNICATIONSTATE(String cOMMUNICATIONSTATE) {
        this.cOMMUNICATIONSTATE = cOMMUNICATIONSTATE;
        return this;
    }

    public String getcOMMUNICATIONCITY() {
        return cOMMUNICATIONCITY;
    }

    public MerchantDetails setcOMMUNICATIONCITY(String cOMMUNICATIONCITY) {
        this.cOMMUNICATIONCITY = cOMMUNICATIONCITY;
        return this;
    }

    public String getcOMMUNICATIONPIN() {
        return cOMMUNICATIONPIN;
    }

    public MerchantDetails setcOMMUNICATIONPIN(String cOMMUNICATIONPIN) {
        this.cOMMUNICATIONPIN = cOMMUNICATIONPIN;
        return this;
    }

    public String getkYCBANKNAME() {
        return kYCBANKNAME;
    }

    public MerchantDetails setkYCBANKNAME(String kYCBANKNAME) {
        this.kYCBANKNAME = kYCBANKNAME;
        return this;
    }

    public String getkYCBANKACCOUNTHOLDERNAME() {
        return kYCBANKACCOUNTHOLDERNAME;
    }

    public MerchantDetails setkYCBANKACCOUNTHOLDERNAME(String kYCBANKACCOUNTHOLDERNAME) {
        this.kYCBANKACCOUNTHOLDERNAME = kYCBANKACCOUNTHOLDERNAME;
        return this;
    }

    public String getkYCBANKACCOUNTNO() {
        return kYCBANKACCOUNTNO;
    }

    public MerchantDetails setkYCBANKACCOUNTNO(String kYCBANKACCOUNTNO) {
        this.kYCBANKACCOUNTNO = kYCBANKACCOUNTNO;
        return this;
    }

    public String getkYCBUSINESSPANNO() {
        return kYCBUSINESSPANNO;
    }

    public MerchantDetails setkYCBUSINESSPANNO(String kYCBUSINESSPANNO) {
        this.kYCBUSINESSPANNO = kYCBUSINESSPANNO;
        return this;
    }

    public String getkYCAUTHORIZEDSIGNATORYPANNO() {
        return kYCAUTHORIZEDSIGNATORYPANNO;
    }

    public MerchantDetails setkYCAUTHORIZEDSIGNATORYPANNO(String kYCAUTHORIZEDSIGNATORYPANNO) {
        this.kYCAUTHORIZEDSIGNATORYPANNO = kYCAUTHORIZEDSIGNATORYPANNO;
        return this;
    }

    public String getkYCBUSINESSIFSCNO() {
        return kYCBUSINESSIFSCNO;
    }

    public MerchantDetails setkYCBUSINESSIFSCNO(String kYCBUSINESSIFSCNO) {
        this.kYCBUSINESSIFSCNO = kYCBUSINESSIFSCNO;
        return this;
    }

    public String getkYCAUTHORIZEDSIGNATORYNAME() {
        return kYCAUTHORIZEDSIGNATORYNAME;
    }

    public MerchantDetails setkYCAUTHORIZEDSIGNATORYNAME(String kYCAUTHORIZEDSIGNATORYNAME) {
        this.kYCAUTHORIZEDSIGNATORYNAME = kYCAUTHORIZEDSIGNATORYNAME;
        return this;
    }

    public String getkYCAUTHORIZEDSIGNATORYPROOFNO() {
        return kYCAUTHORIZEDSIGNATORYPROOFNO;
    }

    public MerchantDetails setkYCAUTHORIZEDSIGNATORYPROOFNO(String kYCAUTHORIZEDSIGNATORYPROOFNO) {
        this.kYCAUTHORIZEDSIGNATORYPROOFNO = kYCAUTHORIZEDSIGNATORYPROOFNO;
        return this;
    }

    public String getkYCAUTHORIZEDSIGNATORYIDPROOFNO() {
        return kYCAUTHORIZEDSIGNATORYIDPROOFNO;
    }

    public MerchantDetails setkYCAUTHORIZEDSIGNATORYIDPROOFNO(String kYCAUTHORIZEDSIGNATORYIDPROOFNO) {
        this.kYCAUTHORIZEDSIGNATORYIDPROOFNO = kYCAUTHORIZEDSIGNATORYIDPROOFNO;
        return this;
    }

    public String getcOMMSTATSELECT() {
        return cOMMSTATSELECT;
    }

    public MerchantDetails setcOMMSTATSELECT(String cOMMSTATSELECT) {
        this.cOMMSTATSELECT = cOMMSTATSELECT;
        return this;
    }

    public Boolean iseMAILMERCHANT() {
        return eMAILMERCHANT;
    }

    public MerchantDetails seteMAILMERCHANT(Boolean eMAILMERCHANT) {
        this.eMAILMERCHANT = eMAILMERCHANT;
        return this;
    }

    public Boolean iseMAILCONSUMER() {
        return eMAILCONSUMER;
    }

    public MerchantDetails seteMAILCONSUMER(Boolean eMAILCONSUMER) {
        this.eMAILCONSUMER = eMAILCONSUMER;
        return this;
    }

    public String getrEQUESTTYPE() {
        return rEQUESTTYPE;
    }

    public MerchantDetails setrEQUESTTYPE(String rEQUESTTYPE) {
        this.rEQUESTTYPE = rEQUESTTYPE;
        return this;
    }

    public String getkYCBUSINESSGSTIN() {
        return kYCBUSINESSGSTIN;
    }

    public MerchantDetails setkYCBUSINESSGSTIN(String kYCBUSINESSGSTIN) {
        this.kYCBUSINESSGSTIN = kYCBUSINESSGSTIN;
        return this;
    }


    public Boolean isHybridEnabled() {
        return hybridEnabled;
    }

    public MerchantDetails setHybridEnabled(Boolean hybridEnabled) {
        this.hybridEnabled = hybridEnabled;
        return this;
    }
}