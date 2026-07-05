package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by anjukumari on 16/11/18
 */
public class PeonResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("CURRENCY")
    private String cURRENCY;
    @JsonProperty("GATEWAYNAME")
    private String gATEWAYNAME;
    @JsonProperty("RESPMSG")
    private String rESPMSG;
    @JsonProperty("BANKNAME")
    private String bANKNAME;
    @JsonProperty("PAYMENTMODE")
    private String pAYMENTMODE;
    @JsonProperty("CUSTID")
    private String cUSTID;
    @JsonProperty("MID")
    private String mID;
    @JsonProperty("MERC_UNQ_REF")
    public String MERC_UNQ_REF;
    @JsonProperty("RRN")
    public String RRN;
    @JsonProperty("AUTHCODE")
    public String AUTHCODE;
    @JsonProperty("RESPCODE")
    private String rESPCODE;
    @JsonProperty("TXNID")
    private String tXNID;
    @JsonProperty("TXNAMOUNT")
    private String tXNAMOUNT;
    @JsonProperty("ORDERID")
    private String oRDERID;
    @JsonProperty("STATUS")
    private String sTATUS;
    @JsonProperty("BANKTXNID")
    private String bANKTXNID;
    @JsonProperty("TXNDATETIME")
    private String tXNDATETIME;
    @JsonProperty("TXNDATE")
    private String tXNDATE;
    @JsonProperty("PROMO_CAMP_ID")
    private String PROMO_CAMP_ID;
    @JsonProperty("PROMO_STATUS")
    private String PROMO_STATUS;
    @JsonProperty("PROMO_RESPCODE")
    private String PROMO_RESPCODE;
    @JsonProperty("cardIndexNo")
    private String cardIndexNo;
    @JsonProperty("CHILDTXNLIST")
    private String childTxnString;
    @JsonProperty("PAYMENT_PROMO_CHECKOUT_DATA")
    private String PAYMENT_PROMO_CHECKOUT_DATA;
    @JsonProperty("emiSubventionInfo")
    private String EMI_SUBVENTION_INFO;
    @JsonProperty("PAYABLE_AMOUNT")
    public String PAYABLE_AMOUNT;
    @JsonProperty("CHECKSUMHASH")
    private String CHECKSUMHASH;
    @JsonProperty("cardScheme")
    private String cardScheme;
    @JsonProperty("BIN")
    private String BIN;
    @JsonProperty("LASTFOURDIGITS")
    private String LASTFOURDIGITS;
    @JsonProperty("APAYMODE")
    private String APAYMODE;
    @JsonProperty("ABKTRNTIME")
    private String ABKTRNTIME;
    @JsonProperty("BKTRNID")
    private String BKTRNID;
    @JsonProperty("BKTRNTIME")
    private String BKTRNTIME;
    @JsonProperty("ABKTRNMSG")
    private String ABKTRNMSG;
    @JsonProperty("BKTRNMSG")
    private String BKTRNMSG;
    @JsonProperty("ABKCD")
    private String ABKCD;
    @JsonProperty("TOTAMT")
    private String TOTAMT;
    @JsonProperty("CHLNREFNO")
    private String CHLNREFNO;
    @JsonProperty("ABKTRNSTS")
    private String ABKTRNSTS;
    @JsonProperty("BKTRNSTS")
    private String BKTRNSTS;
    @JsonProperty("MERCHANTCD")
    private String MERCHANTCD;
    @JsonProperty("ABKTRNID")
    private String ABKTRNID;
    @JsonProperty("CHECKSUM")
    private String CHECKSUM;
    @JsonProperty("RESULTCODE")
    private String RESULTCODE;

    public String getRESULTCODE() { return RESULTCODE; }

    public PeonResponse setRESULTCODE(String RESULTCODE) {
        this.RESULTCODE = RESULTCODE;
        return this;
    }

    public String getChildTxnString() {
        return childTxnString;
    }

    public PeonResponse setChildTxnString(String childTxnString) {
        this.childTxnString = childTxnString;
        return this;
    }

    @JsonProperty("PAYMENT_PROMO_CHECKOUT_DATA")
    public String getPaymentPromoCheckoutData() {
        return PAYMENT_PROMO_CHECKOUT_DATA;
    }

    @JsonProperty("emiSubventionInfo")
    public String getEmiSubventionInfo() {
        return EMI_SUBVENTION_INFO;
    }

    @JsonProperty("PAYABLE_AMOUNT")
    public String getPAYABLE_AMOUNT() {
        return PAYABLE_AMOUNT;
    }

    @JsonProperty("MERC_UNQ_REF")
    public String getMERC_UNQ_REF() {
        return MERC_UNQ_REF;
    }

    public PeonResponse(){

    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public PeonResponse setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
        return this;
    }

    public String getCHECKSUMHASH() {
        return CHECKSUMHASH;
    }


    public PeonResponse setCHECKSUMHASH(String CHECKSUMHASH) {
        this.CHECKSUMHASH = CHECKSUMHASH;
        return this;
    }


    public String getPROMO_RESPCODE() {
        return PROMO_RESPCODE;
    }

    public PeonResponse setPROMO_RESPCODE(String PROMO_RESPCODE) {
        this.PROMO_RESPCODE = PROMO_RESPCODE;
        return this;
    }



    @JsonProperty("CURRENCY")
    public String getCURRENCY() {
        return cURRENCY;
    }

    @JsonProperty("CURRENCY")
    public void setCURRENCY(String cURRENCY) {
        this.cURRENCY = cURRENCY;
    }

    @JsonProperty("GATEWAYNAME")
    public String getGATEWAYNAME() {
        return gATEWAYNAME;
    }

    @JsonProperty("GATEWAYNAME")
    public void setGATEWAYNAME(String gATEWAYNAME) {
        this.gATEWAYNAME = gATEWAYNAME;
    }

    @JsonProperty("RESPMSG")
    public String getRESPMSG() {
        return rESPMSG;
    }

    @JsonProperty("RESPMSG")
    public void setRESPMSG(String rESPMSG) {
        this.rESPMSG = rESPMSG;
    }

    @JsonProperty("BANKNAME")
    public String getBANKNAME() {
        return bANKNAME;
    }

    @JsonProperty("BANKNAME")
    public void setBANKNAME(String bANKNAME) {
        this.bANKNAME = bANKNAME;
    }

    @JsonProperty("PAYMENTMODE")
    public String getPAYMENTMODE() {
        return pAYMENTMODE;
    }

    @JsonProperty("PAYMENTMODE")
    public void setPAYMENTMODE(String pAYMENTMODE) {
        this.pAYMENTMODE = pAYMENTMODE;
    }

    @JsonProperty("CUSTID")
    public String getCUSTID() {
        return cUSTID;
    }

    @JsonProperty("CUSTID")
    public void setCUSTID(String cUSTID) {
        this.cUSTID = cUSTID;
    }

    @JsonProperty("MID")
    public String getMID() {
        return mID;
    }

    @JsonProperty("MID")
    public void setMID(String mID) {
        this.mID = mID;
    }

    @JsonProperty("MERC_UNQ_REF")
    public void setMERCUNQREF(String MERC_UNQ_REF) {
        this.MERC_UNQ_REF = MERC_UNQ_REF;
    }

    @JsonProperty("RESPCODE")
    public String getRESPCODE() {
        return rESPCODE;
    }

    @JsonProperty("RESPCODE")
    public void setRESPCODE(String rESPCODE) {
        this.rESPCODE = rESPCODE;
    }

    @JsonProperty("TXNID")
    public String getTXNID() {
        return tXNID;
    }

    @JsonProperty("TXNID")
    public void setTXNID(String tXNID) {
        this.tXNID = tXNID;
    }

    @JsonProperty("TXNAMOUNT")
    public String getTXNAMOUNT() {
        return tXNAMOUNT;
    }

    @JsonProperty("TXNAMOUNT")
    public void setTXNAMOUNT(String tXNAMOUNT) {
        this.tXNAMOUNT = tXNAMOUNT;
    }

    @JsonProperty("ORDERID")
    public String getORDERID() {
        return oRDERID;
    }

    @JsonProperty("ORDERID")
    public void setORDERID(String oRDERID) {
        this.oRDERID = oRDERID;
    }

    @JsonProperty("STATUS")
    public String getSTATUS() {
        return sTATUS;
    }

    @JsonProperty("STATUS")
    public void setSTATUS(String sTATUS) {
        this.sTATUS = sTATUS;
    }

    @JsonProperty("BANKTXNID")
    public String getBANKTXNID() {
        return bANKTXNID;
    }

    @JsonProperty("BANKTXNID")
    public void setBANKTXNID(String bANKTXNID) {
        this.bANKTXNID = bANKTXNID;
    }

    @JsonProperty("TXNDATETIME")
    public String getTXNDATETIME() {
        return tXNDATETIME;
    }

    @JsonProperty("TXNDATETIME")
    public void setTXNDATETIME(String tXNDATETIME) {
        this.tXNDATETIME = tXNDATETIME;
    }

    @JsonProperty("TXNDATE")
    public String getTXNDATE() {
        return tXNDATE;
    }

    @JsonProperty("TXNDATE")
    public void setTXNDATE(String tXNDATE) {
        this.tXNDATE = tXNDATE;
    }

    @JsonProperty("AUTHCODE")
    public String getAUTHCODE() {
        return AUTHCODE;
    }

    @JsonProperty("RRN")
    public String getRRN() {
        return RRN;
    }

    public String getPROMO_CAMP_ID() {
        return PROMO_CAMP_ID;
    }

    public PeonResponse setPROMO_CAMP_ID(String PROMO_CAMP_ID) {
        this.PROMO_CAMP_ID = PROMO_CAMP_ID;
        return this;
    }

    public String getPROMO_STATUS() {
        return PROMO_STATUS;
    }

    public PeonResponse setPROMO_STATUS(String PROMO_STATUS) {
        this.PROMO_STATUS = PROMO_STATUS;
        return this;
    }

    public String getAPAYMODE() {
        return APAYMODE;
    }

    public String getABKTRNTIME() {
        return ABKTRNTIME;
    }

    public String getBKTRNID() {
        return BKTRNID;
    }

    public String getBKTRNTIME() {
        return BKTRNTIME;
    }

    public String getABKTRNMSG() {
        return ABKTRNMSG;
    }

    public String getBKTRNMSG() {
        return BKTRNMSG;
    }

    public String getABKCD() {
        return ABKCD;
    }

    public String getTOTAMT() {
        return TOTAMT;
    }

    public String getCHLNREFNO() {
        return CHLNREFNO;
    }

    public String getABKTRNSTS() {
        return ABKTRNSTS;
    }

    public String getBKTRNSTS() {
        return BKTRNSTS;
    }

    public String getMERCHANTCD() {
        return MERCHANTCD;
    }

    public String getABKTRNID() {
        return ABKTRNID;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChildTxn {
        public String TXNID;
        public String PAYMENTMODE;
        public String TXNAMOUNT;
        public String GATEWAYNAME;
        public String BANKTXNID;
        public String BANKNAME;
        public String STATUS;



        public String getTXNID() {
            return TXNID;
        }

        public String getPAYMENTMODE() {
            return PAYMENTMODE;
        }

        public String getTXNAMOUNT() {
            return TXNAMOUNT;
        }

        public String getGATEWAYNAME() {
            return GATEWAYNAME;
        }

        public String getBANKTXNID() {
            return BANKTXNID;
        }

        public String getBANKNAME() {
            return BANKNAME;
        }

        public String getSTATUS() {
            return STATUS;
        }



    }
}
