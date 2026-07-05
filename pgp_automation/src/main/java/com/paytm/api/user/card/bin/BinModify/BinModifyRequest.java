package com.paytm.api.user.card.bin.BinModify;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BinModifyRequest {
    private String bin;
    private String blocked;
    private String cardScheme;
    private String cardType;
    private String countryCode;
    private String institutionId;
    private BinConfigAttributes binConfigAttributes;
    private String source;

    public static class BinModifyApiBuilder {
        private String bin;
        private String blocked = "false";
        private String cardScheme;
        private String cardType;
        private String countryCode = "IN";
        private String institutionId;
        private BinConfigAttributes binConfigAttributes;
        private String source = "ADMIN";

        private BinModifyApiBuilder() {
        }

        public static BinModifyApiBuilder builder() {
            return new BinModifyApiBuilder();
        }

        public BinModifyApiBuilder setBin(String bin) {
            this.bin = bin;
            return this;
        }

        public BinModifyApiBuilder setBlocked(String blocked) {
            this.blocked = blocked;
            return this;
        }

        public BinModifyApiBuilder setCardScheme(String cardScheme) {
            this.cardScheme = cardScheme;
            return this;
        }

        public BinModifyApiBuilder setCardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public BinModifyApiBuilder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public BinModifyApiBuilder setInstitutionId(String institutionId) {
            this.institutionId = institutionId;
            return this;
        }

        public BinModifyApiBuilder setBinConfigAttributes(BinConfigAttributes binConfigAttributes) {
            this.binConfigAttributes = binConfigAttributes;
            return this;
        }

        public BinModifyApiBuilder setSource(String source) {
            this.source = source;
            return this;
        }

        public BinModifyRequest build() {
            BinModifyRequest binModifyRequest = new BinModifyRequest();
            binModifyRequest.institutionId = this.institutionId;
            binModifyRequest.binConfigAttributes = this.binConfigAttributes;
            binModifyRequest.bin = this.bin;
            binModifyRequest.countryCode = this.countryCode;
            binModifyRequest.blocked = this.blocked;
            binModifyRequest.cardScheme = this.cardScheme;
            binModifyRequest.cardType = this.cardType;
            binModifyRequest.source = this.source;
            return binModifyRequest;
        }
    }
}
