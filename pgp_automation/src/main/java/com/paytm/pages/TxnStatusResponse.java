package com.paytm.pages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.api.TxnStatus;
import org.assertj.core.api.Assertions;

public class TxnStatusResponse {

    @JsonProperty("TXNID")
    public String TXNID;
    @JsonProperty("errorCode")
    public String ERRORCODE;
    @JsonProperty("errorMessage")
    public String ERRORMESSAGE;
    @JsonProperty("chargeAmount")
    public String chargeAmount;
    @JsonProperty("BANKTXNID")
    public String BANKTXNID;
    @JsonProperty("ORDERID")
    public String ORDERID;
    @JsonProperty("TXNAMOUNT")
    public String TXNAMOUNT;
    @JsonProperty("STATUS")
    public String STATUS;
    @JsonProperty("TXNTYPE")
    public String TXNTYPE;
    @JsonProperty("GATEWAYNAME")
    public String GATEWAYNAME;
    @JsonProperty("RESPCODE")
    public String RESPCODE;
    @JsonProperty("RESPMSG")
    public String RESPMSG;
    @JsonProperty("BANKNAME")
    public String BANKNAME;
    @JsonProperty("MID")
    public String MID;
    @JsonProperty("PAYMENTMODE")
    public String PAYMENTMODE;
    @JsonProperty("REFUNDAMT")
    public String REFUNDAMT;
    @JsonProperty("PAYABLE_AMOUNT")
    public String PAYABLE_AMOUNT;
    @JsonProperty("PAYMENT_PROMO_CHECKOUT_DATA")
    public String PAYMENT_PROMO_CHECKOUT_DATA;
    @JsonProperty("emiSubventionInfo")
    public String EMI_SUBVENTION_INFO;
    @JsonProperty("TXNDATE")
    public String TXNDATE;
    @JsonProperty("SUBS_ID")
    public String SUBS_ID;
    @JsonProperty("splitSettlementInfo")
    public String splitSettlementInfo;
    @JsonProperty("PRN")
    public String PRN;
    @JsonProperty("COUNTRY_CODE")
    public String COUNTRY_CODE;

    @JsonProperty("binIrcId")
    public String binIrcId;

    @JsonProperty("BIN_IDENTIFIER")
    public String BIN_IDENTIFIER;

    @JsonProperty("riskInfo")
    public String riskInfo;

    @JsonProperty("currentTxnCount")
    public String currentTxnCount;

    @JsonProperty("BIN")
    public String BIN;
    @JsonProperty
    public String LASTFOURDIGITS;
    @JsonProperty("ADDITIONAL_PARAM")
    public String ADDITIONAL_PARAM;
    @JsonProperty
    public String cardScheme;
    @JsonProperty("cardHash")
    public String cardHash;
    @JsonIgnore
    public ChildTxn[] CHILDTXNLIST;
    @JsonProperty("MERC_UNQ_REF")
    public String MERC_UNQ_REF;
    @JsonProperty("cardIndexNo")
    public String cardIndexNo;
    @JsonProperty("maskedCardNo")
    public String maskedCardNo;
    @JsonProperty("bankResultInfo")
    public BankResultInfo bankResultInfo;
    @JsonProperty("ACCNUMVARSUCCESS")
    public String ACCNUMVARSUCCESS;
    @JsonProperty("VPA")
    public String VPA;
    @JsonProperty("PREPAIDCARD")
    public String PREPAID_CARD;
    @JsonProperty("UPI_MODE_SUB_TYPE")
    public String UPI_MODE_SUB_TYPE;
    /** API sends FINAL_PAYMENT_AMOUNT (same style as PAYABLE_AMOUNT, TXNAMOUNT). */
    @JsonProperty("FINALPAYMENTAMOUNT")
    @JsonAlias({"finalPaymentAmount"})
    public String finalPaymentAmount;

    public String getFinalPaymentAmount() {
        return finalPaymentAmount;
    }
    @JsonProperty("cardIndexNo")
    public String getCardIndexNo() {
        return cardIndexNo;
    }

    @JsonProperty("currentTxnCount")
    public String getCurrentTxnCount() {
        return currentTxnCount;
    }

    @JsonProperty("PRN")
    public String getPRN() {
        return PRN;
    }

    @JsonProperty("COUNTRY_CODE")
    public String getCOUNTRY_CODE(){return COUNTRY_CODE;}

    @JsonProperty("binIrcId")
    public String getBinIrcId(){return binIrcId;}

    @JsonProperty("BIN_IDENTIFIER")
    public String getBIN_IDENTIFIER(){return BIN_IDENTIFIER;}


    @JsonProperty("errorCode")
    public String getERRORCODE() {
        return ERRORCODE;
    }

    @JsonProperty("errorMessage")
    public String getERRORMESSAGE() {
        return ERRORMESSAGE;
    }
    @JsonProperty("chargeAmount")
    public String getChargeAmount() {
        return chargeAmount;
    }

    @JsonProperty("TXNID")
    public String getTXNID() {
        return TXNID;
    }

    @JsonProperty("ACCNUMVARSUCCESS")
    public String getACCNUMVARSUCCESS() {
        return ACCNUMVARSUCCESS;
    }

    @JsonProperty("BANKTXNID")
    public String getBANKTXNID() {
        return BANKTXNID;
    }
    @JsonProperty("BIN")
    public String getBIN() {
        return BIN;
    }
    @JsonProperty("LASTFOURDIGITS")
    public String getLASTFOURDIGITS(){
        return LASTFOURDIGITS;
    }

    @JsonProperty("cardScheme")
    public String getCardScheme(){
        return cardScheme;
    }

    @JsonProperty("ORDERID")
    public String getORDERID() {
        return ORDERID;
    }

    @JsonProperty("TXNAMOUNT")
    public String getTXNAMOUNT() {
        return TXNAMOUNT;
    }

    @JsonProperty("STATUS")
    public String getSTATUS() {
        return STATUS;
    }

    @JsonProperty("TXNTYPE")
    public String getTXNTYPE() {
        return TXNTYPE;
    }

    @JsonProperty("GATEWAYNAME")
    public String getGATEWAYNAME() {
        return GATEWAYNAME;
    }

    @JsonProperty("UPI_MODE_SUB_TYPE")
    public String getUPI_MODE_SUB_TYPE() {
        return UPI_MODE_SUB_TYPE;
    }

    @JsonProperty("RESPCODE")
    public String getRESPCODE() {
        return RESPCODE;
    }

    @JsonProperty("RESPMSG")
    public String getRESPMSG() {
        return RESPMSG;
    }

    @JsonProperty("BANKNAME")
    public String getBANKNAME() {
        return BANKNAME;
    }

    @JsonProperty("MID")
    public String getMID() {
        return MID;
    }

    @JsonProperty("PAYMENTMODE")
    public String getPAYMENTMODE() {
        return PAYMENTMODE;
    }

    @JsonProperty("REFUNDAMT")
    public String getREFUNDAMT() {
        return REFUNDAMT;
    }

    @JsonProperty("PAYABLE_AMOUNT")
    public String getPAYABLE_AMOUNT() {
        return PAYABLE_AMOUNT;
    }

    @JsonProperty("TXNDATE")
    public String getTXNDATE() {
        return TXNDATE;
    }

    @JsonProperty("SUBS_ID")
    public String getSUBS_ID() {
        return SUBS_ID;
    }

    @JsonProperty("splitSettlementInfo")
    public String getSplitSettlementInfo() {
        return splitSettlementInfo;
    }

    @JsonProperty("bankResultInfo")
    public void setBankResultInfo(BankResultInfo bankResultInfo) {
        this.bankResultInfo = bankResultInfo;
    }


    @JsonProperty("maskedCardNo")
    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    @JsonIgnore
    @JsonProperty("CHILDTXNLIST")
    public ChildTxn[] getChildTxns() {
        return CHILDTXNLIST;
    }

    @JsonProperty("PAYMENT_PROMO_CHECKOUT_DATA")
    public String getPaymentPromoCheckoutData() {
        return PAYMENT_PROMO_CHECKOUT_DATA;
    }

    @JsonProperty("emiSubventionInfo")
    public String getEmiSubventionInfo() {
        return EMI_SUBVENTION_INFO;
    }

    @JsonProperty("CHILDTXNLIST")
    public void setCHILDTXNLIST(ChildTxn[] CHILDTXNLIST) {
        this.CHILDTXNLIST = CHILDTXNLIST;
    }

    @JsonProperty("MERC_UNQ_REF")
    public String getMERC_UNQ_REF() {
        return MERC_UNQ_REF;
    }

    @JsonProperty("cardHash")
    public String getCardHash() {
        return cardHash;
    }

    @JsonProperty("VPA")
    public String getVPA() {
        return VPA;
    }

    @JsonProperty("PREPAIDCARD")
    public String getPrepaidCard() {
        return PREPAID_CARD;
    }

    @JsonProperty("ADDITIONAL_PARAM")
    public String getAdditionalParam() {
        return ADDITIONAL_PARAM;
    }

    public ChildTxn getChildTxnDetails(TxnStatus.ChildTxnType childTxnType) {
       if(CHILDTXNLIST==null)
       {  Assertions.fail("ChildTxn is empty");}
        if (childTxnType == TxnStatus.ChildTxnType.WALLET)
            return CHILDTXNLIST[1];
        else
            return CHILDTXNLIST[0];

   }

    @JsonProperty("riskInfo")
    public String getRiskInfo() {
        return riskInfo;
    }

    public static class ChildTxn {
        public String TXNID;
        public String PAYMENTMODE;
        public String TXNAMOUNT;
        public String GATEWAYNAME;
        public String BANKTXNID;
        public String BANKNAME;
        public String STATUS;
        public String cardIndexNo;
        public String maskedCardNo;
        public String LASTFOURDIGITS;
        public String BIN;
        public String cardScheme;

        @JsonProperty("cardIndexNo")
        public String getCardIndexNo() { return cardIndexNo; }

        @JsonProperty("maskedCardNo")
        public String getMaskedCardNo() { return maskedCardNo; }

        @JsonProperty("TXNID")
        public String getTXNID() {
            return TXNID;
        }

        @JsonProperty("PAYMENTMODE")
        public String getPAYMENTMODE() {
            return PAYMENTMODE;
        }

        @JsonProperty("TXNAMOUNT")
        public String getTXNAMOUNT() {
            return TXNAMOUNT;
        }

        @JsonProperty("GATEWAYNAME")
        public String getGATEWAYNAME() {
            return GATEWAYNAME;
        }

        @JsonProperty("BANKTXNID")
        public String getBANKTXNID() {
            return BANKTXNID;
        }

        @JsonProperty("BANKNAME")
        public String getBANKNAME() {
            return BANKNAME;
        }

        @JsonProperty("STATUS")
        public String getSTATUS() {
            return STATUS;
        }
        @JsonProperty("LASTFOURDIGITS")
        public String getLASTFOURDIGITS() {
            return LASTFOURDIGITS;
        }
        @JsonProperty("BIN")
        public String getBIN() {
            return BIN;
        }

        @JsonProperty("cardScheme")
        public String getCardScheme() {
            return cardScheme;
        }
    }
}