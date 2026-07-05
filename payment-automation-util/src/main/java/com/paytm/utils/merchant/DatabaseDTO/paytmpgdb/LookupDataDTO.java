package com.paytm.utils.merchant.DatabaseDTO.paytmpgdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class LookupDataDTO {

    @JsonProperty("LOOKUP_ID")
    private Object lookupId;
    @JsonProperty("CATEGORY")
    private Object category;
    @JsonProperty("SUB_CATEGORY")
    private Object subCategory;
    @JsonProperty("NAME")
    private Object name;
    @JsonProperty("VALUE")
    private Object value;
    @JsonProperty("STATUS")
    private Object status;
    @JsonProperty("DESCRIPTION")
    private Object description;
    @JsonProperty("CREATED_BY")
    private Object createdBy;
    @JsonProperty("CREATED_DATE")
    private Object createdDate;
    @JsonProperty("MODIFIED_BY")
    private Object modifiedBy;
    @JsonProperty("MODIFIED_DATE")
    private Object modifiedDate;

    public Object getLookupId() {
        return lookupId;
    }

    public Object getCategory() {
        return category;
    }

    public Object getSubCategory() {
        return subCategory;
    }

    public Object getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Object getStatus() {
        return status;
    }

    public Object getDescription() {
        return description;
    }

    public Object getCreatedBy() {
        return createdBy;
    }

    public Object getCreatedDate() {
        return createdDate;
    }

    public Object getModifiedBy() {
        return modifiedBy;
    }

    public Object getModifiedDate() {
        return modifiedDate;
    }

    public void setLookupId(Object lookupId) {
        this.lookupId = lookupId;
    }

    public void setCategory(Object category) {
        this.category = category;
    }

    public void setSubCategory(Object subCategory) {
        this.subCategory = subCategory;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public void setCreatedBy(Object createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(Object createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedBy(Object modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setModifiedDate(Object modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
