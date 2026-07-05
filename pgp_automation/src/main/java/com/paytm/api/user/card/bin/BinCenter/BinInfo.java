package com.paytm.api.user.card.bin.BinCenter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BinInfo {
    private String binMin;
    private String binMax;
    private String source;
    @JsonProperty("isBinCoftEligible")
    private boolean isBinCoftEligible;
    private String cardScheme;
    private String cardType;
    private String institutionId;
    private String countryCode;
    @JsonProperty("isEmiEligible")
    private boolean isEmiEligible;
    private String cardBin;
    private boolean prepaid;
    private boolean corporateCard;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isAltTokenizationEligible;

    public static class BinInfoBuilder {
        private String binMin;
        private String binMax;
        private String source;
        @JsonProperty("isBinCoftEligible")
        private boolean isBinCoftEligible;
        private String cardScheme;
        private String cardType;
        private String institutionId;
        private String countryCode;
        @JsonProperty("isEmiEligible")
        private boolean isEmiEligible;
        private String cardBin;
        private boolean prepaid;
        private boolean corporateCard;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean isAltTokenizationEligible;

        private BinInfoBuilder() {
        }

        public static BinInfo.BinInfoBuilder builder() {
            return new BinInfo.BinInfoBuilder();
        }

        public BinInfo.BinInfoBuilder setBinMin(String BinMin) {
            this.binMin = BinMin;
            return this;
        }
        public BinInfo.BinInfoBuilder setBinMax(String BinMax) {
            this.binMax = BinMax;
            return this;
        }

        public BinInfo.BinInfoBuilder setSource(String Source) {
            this.source = Source;
            return this;
        }
        public BinInfo.BinInfoBuilder setIsBinCoftEligible(boolean IsBinCoftEligible) {
            this.isBinCoftEligible = IsBinCoftEligible;
            return this;
        }
        public BinInfo.BinInfoBuilder setCardScheme(String CardScheme) {
            this.cardScheme = CardScheme;
            return this;
        }
        public BinInfo.BinInfoBuilder setCardType(String CardType) {
            this.cardType = CardType;
            return this;
        }
        public BinInfo.BinInfoBuilder setInstitutionId(String InstitutionId) {
            this.institutionId = InstitutionId;
            return this;
        }
        public BinInfo.BinInfoBuilder setCountryCode(String CountryCode) {
            this.countryCode = CountryCode;
            return this;
        }
        public BinInfo.BinInfoBuilder setIsEmiEligible(boolean IsEmiEligible) {
            this.isEmiEligible = IsEmiEligible;
            return this;
        }
        public BinInfo.BinInfoBuilder setCardBin(String CardBin) {
            this.cardBin = CardBin;
            return this;
        }
        public BinInfo.BinInfoBuilder setPrepaid(boolean Prepaid) {
            this.prepaid = Prepaid;
            return this;
        }
        public BinInfo.BinInfoBuilder setCorporateCard(boolean CorporateCard) {
            this.corporateCard = CorporateCard;
            return this;
        }

        public BinInfo.BinInfoBuilder setIsAltTokenizationEligible(Boolean isAltTokenizationEligible) {
            this.isAltTokenizationEligible = isAltTokenizationEligible;
            return this;
        }

        public BinInfo build() {
            BinInfo BinInfo = new BinInfo();
            BinInfo.binMin = this.binMin;
            BinInfo.binMax = this.binMax;
            BinInfo.source= this.source;
            BinInfo.isBinCoftEligible = this.isBinCoftEligible;
            BinInfo.cardScheme = this.cardScheme;
            BinInfo.cardType = this.cardType;
            BinInfo.institutionId = this.institutionId;
            BinInfo.countryCode = this.countryCode;
            BinInfo.isEmiEligible = this.isEmiEligible;
            BinInfo.cardBin = this.cardBin;
            BinInfo.prepaid = this.prepaid;
            BinInfo.corporateCard = this.corporateCard;
            if (this.isAltTokenizationEligible != null) {
                BinInfo.isAltTokenizationEligible = this.isAltTokenizationEligible;
            }
            return BinInfo;
        }
    }

}
