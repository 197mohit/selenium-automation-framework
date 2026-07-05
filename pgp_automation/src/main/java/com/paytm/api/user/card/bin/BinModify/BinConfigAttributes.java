package com.paytm.api.user.card.bin.BinModify;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;


@ToString
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BinConfigAttributes{

    private String INDIAN;
 //   private String ZERO_SUCCESS_RATE;
    private String PREPAID_CARD;
    private String CORPORATE_CARD;


    public static class BinConfigAttributesBuilder {
        private String INDIAN = "true";
  //      private String ZERO_SUCCESS_RATE = "false";
        private String PREPAID_CARD;
        private String CORPORATE_CARD;

        private BinConfigAttributesBuilder() {
        }

        public static BinConfigAttributesBuilder builder() {
            return new BinConfigAttributesBuilder();
        }

        public BinConfigAttributesBuilder setINDIAN(String INDIAN) {
            this.INDIAN = INDIAN;
            return this;
        }
/*
        public BinConfigAttributesBuilder setZERO_SUCCESS_RATE(String ZERO_SUCCESS_RATE) {
            this.ZERO_SUCCESS_RATE = ZERO_SUCCESS_RATE;
            return this;
        }

 */
        public BinConfigAttributesBuilder setPREPAID_CARD(String PREPAID_CARD) {
            this.PREPAID_CARD = PREPAID_CARD;
            return this;
        }

        public BinConfigAttributesBuilder setCORPORATE_CARD(String CORPORATE_CARD) {
            this.CORPORATE_CARD = CORPORATE_CARD;
            return this;
        }

        public BinConfigAttributes build() {
            BinConfigAttributes binConfigAttributes = new BinConfigAttributes();
            binConfigAttributes.PREPAID_CARD = this.PREPAID_CARD;
            binConfigAttributes.INDIAN = this.INDIAN;
    //        binConfigAttributes.ZERO_SUCCESS_RATE = this.ZERO_SUCCESS_RATE;
            binConfigAttributes.CORPORATE_CARD = this.CORPORATE_CARD;
            return binConfigAttributes;
        }
    }
}

