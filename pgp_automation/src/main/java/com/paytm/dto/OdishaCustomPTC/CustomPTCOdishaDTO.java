package com.paytm.dto.OdishaCustomPTC;

import com.paytm.appconstants.Constants;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CustomPTCOdishaDTO {


    String MERCHANTCD;
    String CHLNREFNO;
    String GOVTAMT;
    String AGID1;
    String AGIDAMT1;
    String AGID2;
    String AGIDAMT2;
    String AGID3;
    String AGIDAMT3;
    String AGID4;
    String AGIDAMT4;
    String AGID5;
    String AGIDAMT5;
    String AGID6;
    String AGIDAMT6;
    String TOTAMT;
    String PAYEENM;
    String PAYMODE;
    String RU;
    String CheckSum = "";
    String MerchantKey;
    String PIPE_SEPARATED_PAYLOAD = "";


    public CustomPTCOdishaDTO(Builder builder) {

        this.MERCHANTCD = builder.MERCHANTCD;
        this.CHLNREFNO = builder.CHLNREFNO;
        this.GOVTAMT = builder.GOVTAMT;
        this.AGID1 = builder.AGID1;
        this.AGIDAMT1 = builder.AGIDAMT1;
        this.AGID2 = builder.AGID2;
        this.AGIDAMT2 = builder.AGIDAMT2;
        this.AGID3 = builder.AGID3;
        this.AGIDAMT3 = builder.AGIDAMT3;
        this.AGID4 = builder.AGID4;
        this.AGIDAMT4 = builder.AGIDAMT4;
        this.AGID5 = builder.AGID5;
        this.AGIDAMT5 = builder.AGIDAMT5;
        this.AGID6 = builder.AGID6;
        this.AGIDAMT6 = builder.AGIDAMT6;
        this.TOTAMT = builder.TOTAMT;
        this.PAYEENM = builder.PAYEENM;
        this.PAYMODE = builder.PAYMODE;
        this.RU = builder.RU;
        this.MerchantKey = builder.MerchantKey;
    }

    private static byte[] encrypt(final byte[] plainText, final byte[] secret) throws Exception {
        final SecretKeySpec secretKey = new SecretKeySpec(secret, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainText);
    }

    private static String getBase64String(final byte[] byteArray) throws Exception {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    private static byte[] genHmac(final byte[] data, final byte[] key) throws Exception {
        final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data);
    }

    public String getMERCHANTCD() {
        return MERCHANTCD;
    }

    public String getCHLNREFNO() {
        return CHLNREFNO;
    }

    public String getGOVTAMT() {
        return GOVTAMT;
    }

    public String getAGID1() {
        return AGID1;
    }

    public String getAGIDAMT1() {
        return AGIDAMT1;
    }

    public String getAGID2() {
        return AGID2;
    }

    public String getAGIDAMT2() {
        return AGIDAMT2;
    }

    public String getAGID3() {
        return AGID3;
    }

    public String getAGIDAMT3() {
        return AGIDAMT3;
    }

    public String getAGID4() {
        return AGID4;
    }

    public String getAGIDAMT4() {
        return AGIDAMT4;
    }

    public String getAGID5() {
        return AGID5;
    }

    public String getAGIDAMT5() {
        return AGIDAMT5;
    }

    public String getAGID6() {
        return AGID6;
    }

    public String getAGIDAMT6() {
        return AGIDAMT6;
    }

    public String getTOTAMT() {
        return TOTAMT;
    }

    public String getPAYEENM() {
        return PAYEENM;
    }

    public String getPAYMODE() {
        return PAYMODE;
    }

    public String getRU() {
        return RU;
    }

    public String getCheckSum() {
        return CheckSum;
    }

    public String generateChecksum() throws Exception {

        String PIPE_SEPARATED_PAYLOAD =
                MERCHANTCD + "|" + CHLNREFNO + "|" + GOVTAMT + "|" + AGID1 + "|" + AGIDAMT1 + "|" + AGID2 + "|" + AGIDAMT2 + "|" + AGID3 + "|" + AGIDAMT3 + "|" + AGID4 + "|" + AGIDAMT4 + "|" + AGID5 + "|" + AGIDAMT5 + "|" + AGID6 + "|" + AGIDAMT6 + "|" + TOTAMT + "|" + PAYEENM + "|" + PAYMODE + "|" + RU;

        byte[] dataArr = PIPE_SEPARATED_PAYLOAD.getBytes();
        byte[] keyData = MerchantKey.getBytes();
        byte[] response = genHmac(dataArr, keyData);
        String datachkPlain = PIPE_SEPARATED_PAYLOAD + "|" + getBase64String(response);
        byte[] datachkPlainArr = datachkPlain.getBytes();
        byte[] cypherText = encrypt(datachkPlainArr, keyData);
        CheckSum = getBase64String(cypherText);
        return CheckSum;


    }

    public String decodeChecksum() throws Exception {

        byte[] dataArr = PIPE_SEPARATED_PAYLOAD.getBytes();
        byte[] keyData = MerchantKey.getBytes();
        byte[] response = genHmac(dataArr, keyData);
        String datachkPlain = PIPE_SEPARATED_PAYLOAD + "|" + getBase64String(response);
        byte[] datachkPlainArr = datachkPlain.getBytes();
        byte[] cypherText = encrypt(datachkPlainArr, keyData);
        return getBase64String(cypherText);


    }

    public static class Builder {


        String MERCHANTCD = "";
        String CHLNREFNO = "";
        String GOVTAMT = "";
        String AGID1 = "";
        String AGIDAMT1 = "";
        String AGID2 = "";
        String AGIDAMT2 = "";
        String AGID3 = "";
        String AGIDAMT3 = "";
        String AGID4 = "";
        String AGIDAMT4 = "";
        String AGID5 = "";
        String AGIDAMT5 = "";
        String AGID6 = "";
        String AGIDAMT6 = "";
        String TOTAMT = "";
        String PAYEENM = "Gagandeep Singh";
        String PAYMODE = "";
        String RU = "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse";
        String MerchantKey = "";


        public Builder(Constants.MerchantType MERCHANTCD, String CHLNREFNO, String TOTAMT, String PAYMODE) {
            this.MERCHANTCD = MERCHANTCD.getId();
            this.CHLNREFNO = CHLNREFNO;
            this.TOTAMT = TOTAMT;
            this.MerchantKey = MERCHANTCD.getKey();
            this.PAYMODE = PAYMODE;
        }


        public Builder setMERCHANTCD(String MERCHANTCD) {
            this.MERCHANTCD = MERCHANTCD;
            return this;
        }


        public Builder setCHLNREFNO(String CHLNREFNO) {
            this.CHLNREFNO = CHLNREFNO;
            return this;
        }

        public Builder setGOVTAMT(String GOVTAMT) {
            this.GOVTAMT = GOVTAMT;
            return this;
        }


        public Builder setAGID1(String AGID1) {
            this.AGID1 = AGID1;
            return this;
        }


        public Builder setAGIDAMT1(String AGIDAMT1) {
            this.AGIDAMT1 = AGIDAMT1;
            return this;
        }


        public Builder setAGID2(String AGID2) {
            this.AGID2 = AGID2;
            return this;
        }


        public Builder setAGIDAMT2(String AGIDAMT2) {
            this.AGIDAMT2 = AGIDAMT2;
            return this;
        }


        public Builder setAGID3(String AGID3) {
            this.AGID3 = AGID3;
            return this;
        }


        public Builder setAGIDAMT3(String AGIDAMT3) {
            this.AGIDAMT3 = AGIDAMT3;
            return this;
        }


        public Builder setAGID4(String AGID4) {
            this.AGID4 = AGID4;
            return this;
        }


        public Builder setAGIDAMT4(String AGIDAMT4) {
            this.AGIDAMT4 = AGIDAMT4;
            return this;
        }


        public Builder setAGID5(String AGID5) {
            this.AGID5 = AGID5;
            return this;
        }


        public Builder setAGIDAMT5(String AGIDAMT5) {
            this.AGIDAMT5 = AGIDAMT5;
            return this;
        }


        public Builder setAGID6(String AGID6) {
            this.AGID6 = AGID6;
            return this;
        }

        public Builder setAGIDAMT6(String AGIDAMT6) {
            this.AGIDAMT6 = AGIDAMT6;
            return this;
        }


        public Builder setTOTAMT(String TOTAMT) {
            this.TOTAMT = TOTAMT;
            return this;
        }


        public Builder setPAYEENM(String PAYEENM) {
            this.PAYEENM = PAYEENM;
            return this;
        }

        public Builder setPAYMODE(String PAYMODE) {
            this.PAYMODE = PAYMODE;
            return this;
        }

        public void setRU(String RU) {
            this.RU = RU;
        }

        public CustomPTCOdishaDTO build() {
            if (AGIDAMT1.equals("") && AGIDAMT2.equals("") && AGIDAMT3.equals("") && AGIDAMT4.equals("") && AGIDAMT5.equals("") && AGIDAMT6.equals(""))
                GOVTAMT = TOTAMT;

            switch (PAYMODE) {
                case "NB":
                    PAYMODE = "N";
                    break;
                case "DC":
                    PAYMODE = "D";
                    break;
                case "UPI":
                    PAYMODE = "U";
                    break;
                case "PPI":
                    PAYMODE = "W";
                    break;
            }
            return new CustomPTCOdishaDTO(this);
        }

    }
}
