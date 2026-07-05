package com.paytm.dto.NativeDTO.InitTxn;

public class ShippingInfo {
    private ChargeAmount chargeAmount;
    private String lastName;
    private String trackingNo;
    private String countryName;
    private String merchantShippingId;
    private String cityName;
    private String address1;
    private String address2;
    private String email;
    private String zipCode;
    private String stateName;
    private String carrier;
    private String firstName;
    private String mobileNo;
    private String shipmentDate;
    private String shipmentFrom;
    private String shipmentTo;
    private String consignerName;
    private String consignerAddress;
    private String deliveryAgent;
    private String packingMode;

    public ShippingInfo() {
        this.chargeAmount = new ChargeAmount();
        this.lastName = "Li";
        this.trackingNo = "646431431322332133";
        this.countryName = "JP";
        this.merchantShippingId = "564314314574327545";
        this.cityName = "Atlanta";
        this.address1 = "137 W San Bernardino";
        this.address2 = "4114 Sepulveda";
        this.email = "abc@gmail.com";
        this.zipCode = "310001";
        this.stateName = "GA";
        this.carrier = "Federal Express";
        this.firstName = "Jim";
        this.mobileNo = "13765443223";
    }

    public ChargeAmount getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(ChargeAmount chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getMerchantShippingId() {
        return merchantShippingId;
    }

    public void setMerchantShippingId(String merchantShippingId) {
        this.merchantShippingId = merchantShippingId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(String shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public String getShipmentFrom() {
        return shipmentFrom;
    }

    public void setShipmentFrom(String shipmentFrom) {
        this.shipmentFrom = shipmentFrom;
    }

    public String getShipmentTo() {
        return shipmentTo;
    }

    public void setShipmentTo(String shipmentTo) {
        this.shipmentTo = shipmentTo;
    }

    public String getConsignerName() {
        return consignerName;
    }

    public void setConsignerName(String consignerName) {
        this.consignerName = consignerName;
    }

    public String getConsignerAddress() {
        return consignerAddress;
    }

    public void setConsignerAddress(String consignerAddress) {
        this.consignerAddress = consignerAddress;
    }

    public String getDeliveryAgent() {
        return deliveryAgent;
    }

    public void setDeliveryAgent(String deliveryAgent) {
        this.deliveryAgent = deliveryAgent;
    }

    public String getPackingMode() {
        return packingMode;
    }

    public void setPackingMode(String packingMode) {
        this.packingMode = packingMode;
    }

    /** Cross-border export sample shipping extensions (dates, consigner, packing). */
    public void applyCrossBorderExportShippingOverrides() {
        this.shipmentDate = "20260325";
        this.shipmentFrom = "Ohio";
        this.shipmentTo = "Delhi";
        this.consignerName = "James";
        this.consignerAddress = "137 W San Bernardino";
        this.deliveryAgent = "Test";
        this.packingMode = "Carton";
    }

    @Override
    public String toString() {
        return "ClassPojo [chargeAmount = " + chargeAmount + ", lastName = " + lastName + ", trackingNo = " + trackingNo + ", countryName = " + countryName + ", merchantShippingId = " + merchantShippingId + ", cityName = " + cityName + ", address1 = " + address1 + ", address2 = " + address2 + ", email = " + email + ", zipCode = " + zipCode + ", stateName = " + stateName + ", carrier = " + carrier + ", firstName = " + firstName + ", mobileNo = " + mobileNo + "]";
    }
}
