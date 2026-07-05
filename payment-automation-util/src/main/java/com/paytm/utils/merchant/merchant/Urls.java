package com.paytm.utils.merchant.merchant;

import com.paytm.utils.merchant.api.EditMerchantApi;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.EditMerchant;
import com.paytm.utils.merchant.dto.UrlDetails;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

import java.util.Arrays;

/**
 * Created by deepakkumar on 11/12/17.
 */
public final class Urls extends Configuration {

    private String websiteName;
    private String requestUrl;
    private String responseUrl;
    private String peonUrl;
    private String imageName;

    private Urls(String websiteName, String requestUrl, String responseUrl, String peonUrl, String imageName) {
        this.websiteName = websiteName;
        this.requestUrl = requestUrl;
        this.responseUrl = responseUrl;
        this.peonUrl = peonUrl;
        this.imageName = imageName;
    }

    public static Urls Default() {
        return new Urls(
                "retail",
                "https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse",
                "https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse",
                "https://pgp-automation.paytm.in/mockbank/peon",
                "paytm_logo");
    }

    public Urls setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
        return this;
    }

    public Urls setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public Urls setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
        return this;
    }

    public Urls setPeonUrl(String peonUrl) {
        this.peonUrl = peonUrl;
        return this;
    }

    public Urls setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    @Override
    void apply(CreateMerchant merchantConfig) {
        merchantConfig
                .getCreateMerRequest()
                .getUrlDetails()
                .add(new UrlDetails()
                        .setWebsiteName(websiteName)
                        .setRequestUrl(requestUrl)
                        .setResponseUrl(responseUrl)
                        .setPeonUrl(peonUrl)
                        .setImageName(imageName));
    }

    @Override
    void modify(String mid) {
        EditMerchant config =
                new EditMerchant()
                        .setAction("Save")
                        .setMid(mid)
                        .setUrlDetails(Arrays.asList(
                                new UrlDetails()
                                        .setWebsiteName(websiteName)
                                        .setRequestUrl(requestUrl)
                                        .setResponseUrl(responseUrl)
                                        .setPeonUrl(peonUrl)
                                        .setImageName(imageName)));

        EditMerchantApi request = new EditMerchantApi(config);
        Response response = request.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
        Assertions.assertThat(jsonPath.getString("DATA.MSG")).isEqualToIgnoringCase("Merchant successfully edited");
    }
}
