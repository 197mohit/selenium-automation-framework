package com.paytm.utils.merchant.merchant;

import com.paytm.utils.merchant.api.EditMerchantApi;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.DocsDetails;
import com.paytm.utils.merchant.dto.EditMerchant;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

/**
 * Created by deepakkumar on 11/12/17.
 */
public final class Docs extends Configuration {

    private DocsDetails config;

    public static Docs Default() {
        return new Docs("PAN CARD", "ID PROOF", "PASS PORT");
    }

    private Docs(String... docs) {
        this.config = new DocsDetails()
                .setDetailedList(docs);
    }

    @Override
    void apply(CreateMerchant merchantConfig) {
        merchantConfig
                .getCreateMerRequest()
                .setDocsDetails(this.config);
    }

    @Override
    void modify(String mid) {
        EditMerchant config =
                new EditMerchant()
                        .setAction("Save")
                        .setMid(mid)
                        .setDocsDetails(this.config);

        EditMerchantApi request = new EditMerchantApi(config);
        Response response = request.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
        Assertions.assertThat(jsonPath.getString("DATA.MSG")).isEqualToIgnoringCase("Merchant successfully edited");
    }
}
