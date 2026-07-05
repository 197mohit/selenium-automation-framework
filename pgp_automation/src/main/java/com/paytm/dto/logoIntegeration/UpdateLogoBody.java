/**
 * @author : Samar Aswal
 * @desc : THis is a dto of a upload logo request body
 */

package com.paytm.dto.logoIntegeration;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateLogoBody {

    @SerializedName("logoUrl")
    @Expose
    private String logoUrl = "merchant/logo";
    @SerializedName("client")
    @Expose
    private String client = "web";
    @SerializedName("fileName")
    @Expose
    private String fileName="";
    @SerializedName("userId")
    @Expose
    private Integer userId = 8974;
    @SerializedName("subId")
    @Expose
    private String subId = "";

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

}