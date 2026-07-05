package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "mName",
        "mLogo",
        "mcc"
})

public class OrderAdditionalInfo {

/*    public OrderAdditionalInfo(String mid,String mName,String mLogo,String mcc)
    {
        this.mid = mid;
        this.mName = mName;
        this.mLogo = mLogo;
        this.mcc = mcc;
    }*/

    @JsonProperty("mid")
    private String mid;

    @JsonProperty("mName")
    private String mName;

    @JsonProperty("mLogo")
    private String mLogo;

    @JsonProperty("mcc")
    private String mcc;


    @JsonProperty("mid")
    public String getMID()
    {return mid;}

    @JsonProperty("mid")
    public OrderAdditionalInfo setMID(String mid)
    {this.mid = mid;
    return this;}

    @JsonProperty("mName")
    public String getMName()
    {return mName;}

    @JsonProperty("mName")
    public OrderAdditionalInfo setMName(String mName)
    {this.mName = mName;
    return this;}

    @JsonProperty("mLogo")
    public String getmLogo()
    {return mLogo;}

    @JsonProperty("mLogo")
    public OrderAdditionalInfo setMLogo(String mLogo)
    {this.mLogo = mLogo;
    return this;}

    @JsonProperty("mcc")
    public String getMcc()
    {return mcc;}

    @JsonProperty("mcc")
    public OrderAdditionalInfo setMcc(String mcc)
    {this.mcc = mcc;
        return this;}


}
