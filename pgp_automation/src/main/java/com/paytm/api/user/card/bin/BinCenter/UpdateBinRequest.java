package com.paytm.api.user.card.bin.BinCenter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class UpdateBinRequest {
    private BinInfo binInfo;
    private List<Integer> digits;
    private String strategy;
    private String binUpdateSource;

    public static class UpdateBinApiBuilder {
        private BinInfo binInfo;
        private List<Integer> digits;
        private String strategy;
        private String binUpdateSource;

        private UpdateBinApiBuilder() {
        }

        public static UpdateBinApiBuilder builder() {
            return new UpdateBinApiBuilder();
        }

        public UpdateBinApiBuilder setBinInfo(BinInfo binInfo) {
            this.binInfo = binInfo;
            return this;
        }

        public UpdateBinApiBuilder setDigits(List<Integer> digits) {
            this.digits = digits;
            return this;
        }

        public UpdateBinApiBuilder setStrategy(String strategy) {
            this.strategy = strategy;
            return this;
        }

        public UpdateBinApiBuilder setBinUpdateSource(String binUpdateSource) {
            this.binUpdateSource = binUpdateSource;
            return this;
        }

        public UpdateBinRequest build() {
            UpdateBinRequest updateBinRequest = new UpdateBinRequest();
            updateBinRequest.binInfo = this.binInfo;
            updateBinRequest.digits = this.digits;
            updateBinRequest.strategy = this.strategy;
            updateBinRequest.binUpdateSource = this.binUpdateSource;
            return updateBinRequest;
        }
    }
}
