package com.paytm.dto.saveCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.asserts.SoftAssert;

/**
 * Created by anjukumari on 22/08/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveCardResponse {
    private int responseCode;
    private String responseMessage;
    private Object cardId;
    private String userId;
    private String cardScheme;
    private Long updated_on;
    private Long created_on;
    private int status;
    private String firstSixDigit;
    private String lastFourDigit;
    private String cardNum;
    private String cardNumber;
    private String cardType;
    private String expiryDate;
    @JsonProperty("mId")
    private String mid;
    @JsonProperty("custId")
    private String custId;

    public String getMid() {
        return mid;
    }

    public SaveCardResponse setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getCustId() {
        return custId;
    }

    public SaveCardResponse setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    private SoftAssert softAssert = new SoftAssert();


    public SaveCardResponse() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public SaveCardResponse setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }


    public SaveCardResponse setSoftAssert(SoftAssert softAssert) {
        this.softAssert = softAssert;
        return this;
    }

    public SaveCardResponse(int responseCode, String responseMessage, Object cardId, String userId, String cardType, String cardScheme) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.cardId = cardId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardScheme = cardScheme;
    }

    public int getResponseCode() {
        return responseCode;
    }


    public SaveCardResponse setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public SaveCardResponse setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    public Object getCardId() {
        return cardId;
    }

    public SaveCardResponse setCardId(Object cardId) {
        this.cardId = cardId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SaveCardResponse setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getCardType() {
        return cardType;
    }

    public SaveCardResponse setCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public SaveCardResponse setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
        return this;
    }

    public Long getUpdated_on() {
        return updated_on;
    }

    public SaveCardResponse setUpdated_on(Long updated_on) {
        this.updated_on = updated_on;
        return this;
    }

    public Long getCreated_on() {
        return created_on;
    }

    public SaveCardResponse setCreated_on(Long created_on) {
        this.created_on = created_on;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public SaveCardResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getFirstSixDigit() {
        return firstSixDigit;
    }

    public SaveCardResponse setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
        return this;
    }

    public String getLastFourDigit() {
        return lastFourDigit;
    }

    public SaveCardResponse setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
        return this;
    }

    public String getCardNum() {
        return cardNum;
    }

    public SaveCardResponse setCardNum(String cardNum) {
        this.cardNum = cardNum;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public SaveCardResponse setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    @Override
    public String toString() {
        return "SaveCardResponse{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", cardId=" + cardId +
                ", userId='" + userId + '\'' +
                ", cardScheme='" + cardScheme + '\'' +
                ", updated_on=" + updated_on +
                ", created_on=" + created_on +
                ", status=" + status +
                ", firstSixDigit='" + firstSixDigit + '\'' +
                ", lastFourDigit='" + lastFourDigit + '\'' +
                ", cardNum='" + cardNum + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardType='" + cardType + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", mid='" + mid + '\'' +
                ", custId='" + custId + '\'' +
                ", softAssert=" + softAssert +
                '}';
    }

    public SaveCardResponse validateCardNumber(String cardNumber){
        this.softAssert.assertEquals(cardNumber, this.cardNumber);
        return this;
    }


    public SaveCardResponse validateCardNum(String cardNum){
        this.softAssert.assertEquals(cardNum, this.cardNum);
        return this;
    }

    public SaveCardResponse validateCardId(Object cardId){
        this.softAssert.assertEquals(cardId.toString(), this.cardId.toString());
        return this;
    }

    public SaveCardResponse validateCardType(String cardType){
        this.softAssert.assertEquals(cardType, this.cardType);
        return this;
    }

    public SaveCardResponse validateExpiry(String Exp){
        this.softAssert.assertEquals(Exp, this.expiryDate);
        return this;
    }

    public SaveCardResponse validateFirstSixDigit(String val){
        this.softAssert.assertEquals(val, this.firstSixDigit);
        return this;
    }

    public SaveCardResponse validateLastFour(String val){
        this.softAssert.assertEquals(val, this.lastFourDigit);
        return this;
    }

    public SaveCardResponse validateStatus(int val){
        this.softAssert.assertEquals(val, this.status);
        return this;
    }

    public SaveCardResponse validateUserId(String val){
        this.softAssert.assertEquals(val, this.userId);
        return this;
    }

    public SaveCardResponse validateCardScheme(String val){
        this.softAssert.assertEquals(val, this.cardScheme);
        return this;
    }

    public SaveCardResponse validateMid(String val){
        this.softAssert.assertEquals(val, this.mid);
        return this;
    }

    public SaveCardResponse validateCustId(String val){
        this.softAssert.assertEquals(val, this.custId);
        return this;
    }

    public SaveCardResponse assertAll(){
        this.softAssert.assertAll();
        return this;
    }
}
