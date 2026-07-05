
package com.paytm.dto.NativeDTO.InitTxn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ref1",
        "ref2",
        "ref3",
        "ref4",
        "ref5",
        "ref6",
        "ref7",
        "ref8",
        "ref9",
        "ref10",
        "ref11",
        "ref12"
})
public class AdditionalInfo implements Serializable {

    @JsonProperty("ref1")
    private String ref1;
    @JsonProperty("ref2")
    private String ref2;
    @JsonProperty("ref3")
    private String ref3;
    @JsonProperty("ref4")
    private String ref4;
    @JsonProperty("ref5")
    private String ref5;
    @JsonProperty("ref6")
    private String ref6;
    @JsonProperty("ref7")
    private String ref7;
    @JsonProperty("ref8")
    private String ref8;
    @JsonProperty("ref9")
    private String ref9;
    @JsonProperty("ref10")
    private String ref10;
    @JsonProperty("ref11")
    private String ref11;
    @JsonProperty("ref12")
    private String ref12;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7783736183013740032L;

    @JsonProperty("ref1")
    public String getRef1() {
        return ref1;
    }

    @JsonProperty("ref1")
    public AdditionalInfo setRef1(String ref1) {
        this.ref1 = ref1;
        return this;
    }

    @JsonProperty("ref2")
    public String getRef2() {
        return ref2;
    }

    @JsonProperty("ref2")
    public AdditionalInfo setRef2(String ref2) {
        this.ref2 = ref2;
        return this;
    }

    @JsonProperty("ref3")
    public String getRef3() {
        return ref3;
    }

    @JsonProperty("ref3")
    public AdditionalInfo setRef3(String ref3) {
        this.ref3 = ref3;
        return this;
    }

    @JsonProperty("ref4")
    public String getRef4() {
        return ref4;
    }

    @JsonProperty("ref4")
    public AdditionalInfo setRef4(String ref4) {
        this.ref4 = ref4;
        return this;
    }

    @JsonProperty("ref5")
    public String getRef5() {
        return ref5;
    }

    @JsonProperty("ref5")
    public AdditionalInfo setRef5(String ref5) {
        this.ref5 = ref5;
        return this;
    }

    @JsonProperty("ref6")
    public String getRef6() {
        return ref6;
    }

    @JsonProperty("ref6")
    public AdditionalInfo setRef6(String ref6) {
        this.ref6 = ref6;
        return this;
    }

    @JsonProperty("ref7")
    public String getRef7() {
        return ref7;
    }

    @JsonProperty("ref7")
    public AdditionalInfo setRef7(String ref7) {
        this.ref7 = ref7;
        return this;
    }

    @JsonProperty("ref8")
    public String getRef8() {
        return ref8;
    }

    @JsonProperty("ref8")
    public AdditionalInfo setRef8(String ref8) {
        this.ref8 = ref8;
        return this;
    }

    @JsonProperty("ref9")
    public String getRef9() {
        return ref9;
    }

    @JsonProperty("ref9")
    public AdditionalInfo setRef9(String ref9) {
        this.ref9 = ref9;
        return this;
    }

    @JsonProperty("ref10")
    public String getRef10() {
        return ref10;
    }

    @JsonProperty("ref10")
    public AdditionalInfo setRef10(String ref10) {
        this.ref10 = ref10;
        return this;
    }

    @JsonProperty("ref11")
    public String getRef11() {
        return ref11;
    }

    @JsonProperty("ref11")
    public AdditionalInfo setRef11(String ref11) {
        this.ref11 = ref11;
        return this;
    }

    @JsonProperty("ref12")
    public String getRef12() {
        return ref12;
    }

    @JsonProperty("ref12")
    public AdditionalInfo setRef12(String ref12) {
        this.ref12 = ref12;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public AdditionalInfo() {
        this.ref1 = "ref1";
        this.ref2 = "ref2";
        this.ref3 = "ref3";
        this.ref4 = "ref4";
        this.ref5 = "ref5";
        this.ref6 = "ref6";
        this.ref7 = "ref7";
        this.ref8 = "ref8";
        this.ref9 = "ref9";
        this.ref10 = "ref10";
        this.ref11 = "ref11";
        this.ref12 = "ref12";
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ref1", ref1).append("ref2", ref2).append("ref3", ref3).append("ref4", ref4).append("ref5", ref5).append("ref6", ref6).append("ref7", ref7).append("ref8", ref8).append("ref9", ref9).append("ref10", ref10).append("ref11", ref11).append("ref12", ref12).append("additionalProperties", additionalProperties).toString();
    }

}