package com.paytm.utils.merchant;

/**
 * Created by ankuragarwal on 28/9/18
 */
public class UtilConstants {

    public enum BankName{
        HDFC("HDFC"),
        ICICI("ICICI"),
        COD("COD"),
        WALLET("WALLET"),
        PAYTMCC("PAYTMCC"),
        PPBL("PPBL"),
        ICICI_DIRECT("ICICIDIRECT"),
        SBI("SBI"),
        AXIS("AXIS"),
        AMEX("AMEX"),
        CODMOCK("CODMOCK"),
        ICICIIDEBIT("ICICIIDEBIT"),
        SBIFSS("SBIFSS"),
        BAJAJFN("BAJAJFN"),
        ICICIPAY("ICICIPAY"),
        BOM("BOM"),
        HDFCPAY("HDFCPAY"),
        HDFCIDEBIT("HDFCIDEBIT"),
        RATN("RATN"),
        PNB("PNB"),
        ANDB("ANDB"),
        CITI("CITI"),
        BOBFSS("BOBFSS"),
        IDFC("IDFC"),
        PMCB("PMCB"),
        BOB("BOB"),
        PPBEX("PPBEX"),
        PPBLC("PPBLC"),
        ;
        private String bankName;

        BankName(String bankName) { this.bankName = bankName;}

        @Override
        public String toString() {
            return bankName;
        }
    }

    public enum PayMethod_DB {
        NB("NB"),
        CC("CC"),
        WEB("WEB"),
        WAP("WAP"),
        PPI("PPI"),
        IMPS("IMPS"),
        ATM("ATM"),
        COD("COD"),
        EMI("EMI"),
        UPI("UPI")
        ;

        private String payMethod;
        PayMethod_DB(String payMethod) { this.payMethod = payMethod; }

        @Override
        public String toString() {return payMethod;}
    }

    public enum LookUpData_Category {
        PAYMENT_MODE,
        AUTH_MODE,
        INDUSTRY_TYPE,
        FEE_IDENTIFIER,
        COMMISSION_IDENTIFIER,
        ALERT,
        REVENUE_TYPE,
        PAYMENT_OPTION,
        BASE_COMM_IDENTIFIER,
        AGGR_COMM_IDENTIFIER,
        CHANNEL
    }

    public enum LookUpData_Status {
        ACTIVE("9376503")
        ;

        private String status;
        LookUpData_Status(String status) { this.status = status; }

        @Override
        public String toString() {return status;}
    }

}
