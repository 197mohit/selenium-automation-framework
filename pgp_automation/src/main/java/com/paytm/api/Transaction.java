package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by deepakkumar on 8/2/18.
 */
public class Transaction extends BaseApi {

    private String TXNID;
    private String BANKTXNID;
    private String ORDERID;
    private String TXNAMOUNT;
    private String STATUS;
    private String TXNTYPE;
    private String GATEWAYNAME;
    private String RESPCODE;
    private String RESPMSG;
    private String BANKNAME;
    private String MID;
    private String PAYMENTMODE;
    private String REFUNDAMT;
    private Date TXNDATE;
    private String SUBS_ID;
    private String MERC_UNQ_REF;

    public static String NOT_PRESENT = "NOT_PRESENT";
    public static String EMPTY = "EMPTY";
    public static String NON_EMPTY = "NON_EMPTY";
    private static String STATUS_TXN_SUCCESS = "TXN_SUCCESS";
    private static String STATUS_TXN_FAILURE = "TXN_FAILURE";
    private static String RESPMSG_TXN_SUCCESS = "Txn Successful.";
    private static String RESPMSG_TXN_FAILURE = "ORDER IS CLOSE.";
    private static String RESPCODE_FAILURE = "810";
    private static String RESPCODE_SUCCESS = "01";
    private static String TXNTYPE_SALE = "SALE";
    private static String TXNTYPE_ADDMONEY = "ADDMONEY";

    private Transaction(String TXNID, String BANKTXNID, String ORDERID, String TXNAMOUNT, String STATUS, String TXNTYPE, String GATEWAYNAME, String RESPCODE, String RESPMSG, String BANKNAME, String MID, String PAYMENTMODE, String REFUNDAMT, Date TXNDATE, String SUBS_ID, String MERC_UNQ_REF) {
        this.TXNID = TXNID;
        this.BANKTXNID = BANKTXNID;
        this.ORDERID = ORDERID;
        this.TXNAMOUNT = TXNAMOUNT;
        this.STATUS = STATUS;
        this.TXNTYPE = TXNTYPE;
        this.GATEWAYNAME = GATEWAYNAME;
        this.RESPCODE = RESPCODE;
        this.RESPMSG = RESPMSG;
        this.BANKNAME = BANKNAME;
        this.MID = MID;
        this.PAYMENTMODE = PAYMENTMODE;
        this.REFUNDAMT = REFUNDAMT;
        this.TXNDATE = TXNDATE;
        this.SUBS_ID = SUBS_ID;
        this.MERC_UNQ_REF = MERC_UNQ_REF;

        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.TXNSTATUS);
        getRequestSpecBuilder().addParam("JsonData", "{\"MID\":\"" + MID + "\",\"ORDERID\":\"" + ORDERID + "\"}");
    }

    public static Transaction AddMoneyMP_Success(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return PGOnly_Success(orderDTO, GATEWAYNAME, BANKNAME, PAYMENTMODE);
    }

    public static Transaction AddMoneyMP_Failure(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return PGOnly_Failure(orderDTO, GATEWAYNAME, BANKNAME, PAYMENTMODE);
    }

    public static Transaction AddMoneyUber_Success(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return new Transaction(NON_EMPTY, NON_EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_SUCCESS, TXNTYPE_ADDMONEY, GATEWAYNAME, RESPCODE_SUCCESS, RESPMSG_TXN_SUCCESS, BANKNAME, orderDTO.getMID(), PAYMENTMODE, "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }

    public static Transaction AddMoneyUber_Failure(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return new Transaction(NON_EMPTY, NON_EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_FAILURE, TXNTYPE_ADDMONEY, GATEWAYNAME, RESPCODE_FAILURE, RESPMSG_TXN_FAILURE, BANKNAME, orderDTO.getMID(), PAYMENTMODE, "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }

    public static Transaction PGOnly_Success(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return new Transaction(NON_EMPTY, NON_EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_SUCCESS, TXNTYPE_SALE, GATEWAYNAME, RESPCODE_SUCCESS, RESPMSG_TXN_SUCCESS, BANKNAME, orderDTO.getMID(), PAYMENTMODE, "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }

    public static Transaction PGOnly_Failure(OrderDTO orderDTO, String GATEWAYNAME, String BANKNAME, String PAYMENTMODE) {
        return new Transaction(NON_EMPTY, NON_EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_FAILURE, TXNTYPE_SALE, GATEWAYNAME, RESPCODE_FAILURE, RESPMSG_TXN_FAILURE, BANKNAME, orderDTO.getMID(), PAYMENTMODE, "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }

    public static Transaction AddnPay_Success(OrderDTO orderDTO) {
        return new Transaction(NON_EMPTY, EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_SUCCESS, TXNTYPE_SALE, "WALLET", RESPCODE_SUCCESS, RESPMSG_TXN_SUCCESS, "WALLET", orderDTO.getMID(), "PPI", "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }

    public static Transaction AddnPay_Failure(OrderDTO orderDTO) {
        return new Transaction(NON_EMPTY, EMPTY, orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), STATUS_TXN_FAILURE, TXNTYPE_SALE, "WALLET", RESPCODE_FAILURE, RESPMSG_TXN_FAILURE, "WALLET", orderDTO.getMID(), "PPI", "0.00", new Date(), NOT_PRESENT, NOT_PRESENT);
    }


    public void verify() {
        JsonPath response = executeTxnStatusAPIUntilPending().jsonPath();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(new TXNID(TXNID).equals(response.get("TXNID"))).isTrue();
        soft.assertThat(new BANKTXNID(BANKTXNID).equals(response.get("BANKTXNID"))).isTrue();
        soft.assertThat(new ORDERID(ORDERID).equals(response.get("ORDERID"))).isTrue();
        soft.assertThat(new TXNAMOUNT(TXNAMOUNT).equals(response.get("TXNAMOUNT"))).isTrue();
        soft.assertThat(new STATUS(STATUS).equals(response.get("STATUS"))).isTrue();
        soft.assertThat(new TXNTYPE(TXNTYPE).equals(response.get("TXNTYPE"))).isTrue();
        soft.assertThat(new GATEWAYNAME(GATEWAYNAME).equals(response.get("GATEWAYNAME"))).isTrue();
        soft.assertThat(new RESPCODE(RESPCODE).equals(response.get("RESPCODE"))).isTrue();
        soft.assertThat(new RESPMSG(RESPMSG).equals(response.get("RESPMSG"))).isTrue();
        soft.assertThat(new BANKNAME(BANKNAME).equals(response.get("BANKNAME"))).isTrue();
        soft.assertThat(new MID(MID).equals(response.get("MID"))).isTrue();
        soft.assertThat(new PAYMENTMODE(PAYMENTMODE).equals(response.get("PAYMENTMODE"))).isTrue();
        soft.assertThat(new REFUNDAMT(REFUNDAMT).equals(response.get("REFUNDAMT"))).isTrue();
        soft.assertThat(new TXNDATE(TXNDATE).equals(response.get("TXNDATE"))).isTrue();//need to think abt it
        soft.assertThat(new SUBS_ID(SUBS_ID).equals(response.get("SUBS_ID"))).isTrue();
        soft.assertThat(new MERC_UNQ_REF(MERC_UNQ_REF).equals(response.get("MERC_UNQ_REF"))).isTrue();
        soft.assertAll();

    }

    private Response executeTxnStatusAPIUntilPending() {
        final String STATUS = "PENDING";
        String txnStatus = null;

        Response response = execute();
        txnStatus = response.jsonPath().get("STATUS").toString();

        if (txnStatus != null && !txnStatus.toUpperCase().equals(STATUS)) {

        } else {

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < 4; i++) {
                response = execute();
                txnStatus = response.jsonPath().get("STATUS").toString();
                if (txnStatus != null && !txnStatus.toUpperCase().equals(STATUS)) {
                    break;
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }
        }

        Assertions.assertThat(txnStatus).as("STATUS").isNotEqualTo(STATUS);
        return response;
    }

    private class MID extends TxnAttribute {
        private String value;

        public MID(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class TXNID extends TxnAttribute {
        private String value;

        public TXNID(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class BANKTXNID extends TxnAttribute {
        private String value;

        public BANKTXNID(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class ORDERID extends TxnAttribute {
        private String value;

        public ORDERID(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class TXNAMOUNT extends TxnAttribute {
        private String value;

        public TXNAMOUNT(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class STATUS extends TxnAttribute {
        private String value;


        public STATUS(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class TXNTYPE extends TxnAttribute {
        private String value;


        public TXNTYPE(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class GATEWAYNAME extends TxnAttribute {
        private String value;


        public GATEWAYNAME(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class RESPCODE extends TxnAttribute {
        private String value;


        public RESPCODE(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class RESPMSG extends TxnAttribute {
        private String value;


        public RESPMSG(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class BANKNAME extends TxnAttribute {
        private String value;


        public BANKNAME(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class PAYMENTMODE extends TxnAttribute {
        private String value;


        public PAYMENTMODE(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class REFUNDAMT extends TxnAttribute {
        private String value;


        public REFUNDAMT(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            boolean b = super.equals(value);
            double v = Double.valueOf(this.value) - Double.valueOf(value.toString());
            return b || v == 0;
        }
    }

    private class TXNDATE {
        private Object value;


        public TXNDATE(Object value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            boolean b = super.equals(value);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedExpectedDate = sdf.format(this.value);
            Date actualDate;
            try {
                actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(value.toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String formattedActualDate = sdf.format(actualDate);
            return b || formattedExpectedDate.equals(formattedActualDate);
        }
    }

    private class SUBS_ID extends TxnAttribute {
        private String value;


        public SUBS_ID(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class MERC_UNQ_REF extends TxnAttribute {
        private String value;


        public MERC_UNQ_REF(String value) {
            super(value);
            this.value = value;
        }

        @Override
        public boolean equals(Object value) {
            return super.equals(value);
        }
    }

    private class TxnAttribute {
        private String value;

        public TxnAttribute(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            boolean b = false;
            if (value == NON_EMPTY) {
                b = !obj.toString().isEmpty();
            } else if (value == NOT_PRESENT) {
                b = obj == null;
            } else if (value == EMPTY) {
                b = obj.toString().isEmpty();
            } else {
                b = this.value.equals(obj.toString());
            }
            return b;
        }
    }

}
