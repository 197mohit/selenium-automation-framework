package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Table;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.utils.merchant.util.PGPUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.*;


import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ResponsePage extends BasePage {

    private static final String responseTableXpath = "body > div:nth-child(3) > center:nth-child(3) > table";
    private static final String respMsgXpath = "//table/tbody//*[text()=\"RESPMSG\"]/../td[2]";
    private static final String statusXpath = "//table/tbody//*[text()=\"STATUS\"]/../td[2]";
    private static final String subsIdXpath = "//table/tbody//*[text()=\"SUBS_ID\"]/../td[2]";
    private static final String responseStatus = "//table/tbody//*[text()=\"RESPONSE_STATUS\"]/../td[2]";
    private static final String txnIdXpath = "//table/tbody//*[text()=\"TXNID\"]/../td[2]";
    private static final String respCodeXpath = "//table/tbody//*[text()=\"RESPCODE\"]/../td[2]";
    private static final String midXpath = "//table/tbody//*[text()=\"MID\"]/../td[2]";
    private static final String orderIdXpath = "//table/tbody//*[text()=\"ORDERID\"]/../td[2]";
    private static final String txnAmountXpath = "//table/tbody//*[text()=\"TXNAMOUNT\"]/../td[2]";
    private static final String txnDateXpath = "//table/tbody//*[text()=\"TXNDATE\"]/../td[2]";
    private static final String bankTxnIdXpath = "//table/tbody//*[text()=\"BANKTXNID\"]/../td[2]";
    private static final String currencyXpath = "//table/tbody//*[text()=\"CURRENCY\"]/../td[2]";
    private static final String paymentModeXpath = "//table/tbody//*[text()=\"PAYMENTMODE\"]/../td[2]";
    private static final String childTxnXpath = "//table/tbody//*[text()=\"CHILDTXNLIST\"]/../td[2]";
    private static final String promoCampId = "//table/tbody//*[text()=\"PROMO_CAMP_ID\"]/../td[2]";
    private static final String promoResponseCode = "//table/tbody//*[text()=\"PROMO_RESPCODE\"]/../td[2]";
    private static final String promoStatus = "//table/tbody//*[text()=\"PROMO_STATUS\"]/../td[2]";
    private static final String errorMessage = "//table/tbody//*[text()=\"errorMessage\"]/../td[2]";
    private static final String errorCode = "//table/tbody//*[text()=\"errorCode\"]/../td[2]";
    private static final String cardIndexNo = "//table/tbody//*[text()=\"cardIndexNo\"]/../td[2]";
    private static final String bankNameXpath = "//table/tbody//*[text()=\"BANKNAME\"]/../td[2]";
    private static final String gatewayNameXpath = "//table/tbody//*[text()=\"GATEWAYNAME\"]/../td[2]";
    private static final String allParameters = "//table/tbody/tr/td[1]";
    private static final String lastDigitXpath = "//table/tbody//*[text()=\"LASTFOURDIGITS\"]/../td[2]";
    private static final String binXpath = "//table/tbody//*[text()=\"BIN\"]/../td[2]";
    private static final String ENC_DATA = "//table/tbody//*[text()=\"ENC_DATA\"]/../td[2]";
    private static final String mercUnqRefXpath = "//table/tbody//*[text()=\"MERC_UNQ_REF\"]/../td[2]";
    private static final String chargeAmountXpath ="//table/tbody//*[text()=\"CHARGEAMOUNT\"]/../td[2]";
    private static final String vpa = "//table/tbody//*[text()=\"VPA\"]/../td[2]";
    private static final String prepaidCard = "//table/tbody//*[text()=\"PREPAIDCARD\"]/../td[2]";
    private static final String splitSettlementInfo = "//table/tbody//*[text()=\"splitSettlementInfo\"]/../td[2]";
    private static final String is_accepted = "//table/tbody//*[text()=\"IS_ACCEPTED\"]/../td[2]";
    private static final String mandateType = "//table/tbody//*[text()=\"MANDATE_TYPE\"]/../td[2]";
    private static final String AcceptedRefNO = "//table/tbody//*[text()=\"ACCEPTED_REF_NO\"]/../td[2]";
    private static final String riskInfo = "//table/tbody//*[text()=\"riskInfo\"]/../td[2]";
    private static final String udf = "//table/tbody//*[text()=\"UDF_2\"]/../td[2]";
    private static final String txnpaidtime = "//table/tbody//*[text()=\"TXNPAIDTIME\"]/../td[2]";


    private Set<String> parameters = new ConcurrentSkipListSet<>();
    private SoftAssertions softly = new SoftAssertions();

    public ResponsePage() {
        super("Response Page");
        this.pageURL = LocalConfig.PGP_RESP_HOST + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH;
    }

    public SoftAssertions getSoftly() {
        return this.softly;
    }

    public void verifyIsResponsePage() {
        Assertions.assertThat(DriverManager.getDriver().getCurrentUrl()).isEqualToIgnoringCase(this.pageURL);
    }

    public ResponsePage validateCheckSum(String merchantKey) {
        Set<String> keys = DriverManager.getDriver().findElements(By.xpath(allParameters)).stream().map(WebElement::getText).collect(Collectors.toSet());
        this.softly.assertThat(keys).as("checksumhash is not present").contains(ResponseUIField.CHECKSUM.toString().toUpperCase());
        AtomicReference<String> checksum = new AtomicReference<>("");
        TreeMap<String, String> map = new TreeMap<>();
        keys.forEach(key -> {
            String value = ((JavascriptExecutor) DriverManager.getDriver()).executeScript("return document.evaluate(`//table/tbody//*[text()='" + key + "']/../td[2]`, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerHTML;").toString().trim();
            if ("CHECKSUMHASH".equalsIgnoreCase(key)) {
                checksum.set(value.replaceAll(" ", "+"));
            } else {
                map.put(key, value);
            }
        });
        parameters.add(ResponseUIField.CHECKSUM.toString().toUpperCase());
        this.softly.assertThat(PGPUtil.isChecksumValid(merchantKey, map, checksum.get())).as("checksum is not valid").isTrue();
        return this;
    }

    public ResponsePage validateResponsePageParameters() {


        Set<String> actualParameters = DriverManager.getDriver().findElements(By.xpath(allParameters)).stream().map(WebElement::getText).collect(Collectors.toSet());
        Set<String> expectedParameters = new HashSet<>(parameters);

        for (String expected : expectedParameters) {
            if (!actualParameters.add(expected.toUpperCase())) {
                actualParameters.remove(expected.toUpperCase());
            }
        }

        this.softly.assertThat(actualParameters).as("Validation failed for : " + actualParameters).isEmpty();
        return this;

    }


    public Table tableResponse() {
        return new Table(By.cssSelector(responseTableXpath), getPageName(), "tableResponse");
    }

    public UIElement textRespMsg() {
        return new UIElement(By.xpath(respMsgXpath), getPageName(), "textRespMsg");
    }

    public UIElement textStatus() {
        return new UIElement(By.xpath(statusXpath), getPageName(), "textStatus");
    }

    public UIElement textSUBSID() {
        return new UIElement(By.xpath(subsIdXpath), getPageName(), "textSUBSID");
    }

    public UIElement textRespStatus() {
        return new UIElement(By.xpath(responseStatus), getPageName(), "responseStatus");
    }

    public UIElement textTxnID() {
        return new UIElement(By.xpath(txnIdXpath), getPageName(), "textTxnID");
    }

    public UIElement textRespCode() {
        return new UIElement(By.xpath(respCodeXpath), getPageName(), "textRespCode");
    }

    public UIElement textMID() {
        return new UIElement(By.xpath(midXpath), getPageName(), "textMID");
    }

    public UIElement textOrderID() {
        return new UIElement(By.xpath(orderIdXpath), getPageName(), "textOrderID");
    }

    public UIElement textTXNAMOUNT() {
        return new UIElement(By.xpath(txnAmountXpath), getPageName(), "textTXNAMOUNT");
    }

    public UIElement textTxnDate() {
        return new UIElement(By.xpath(txnDateXpath), getPageName(), "textTxnDate");
    }

    public UIElement textBankTxnID() {
        return new UIElement(By.xpath(bankTxnIdXpath), getPageName(), "textBankTxnID");
    }

    public UIElement textCurrency() {
        return new UIElement(By.xpath(currencyXpath), getPageName(), "textCurrency");
    }

    public UIElement textErrorMessage() {
        return new UIElement(By.xpath(errorMessage), getPageName(), "textErrorMessage");
    }

    public UIElement textErrorCode() {
        return new UIElement(By.xpath(errorCode), getPageName(), "textErrorCode");
    }

    public UIElement textPromoCampId() {
        return new UIElement(By.xpath(promoCampId), getPageName(), "textPromoCampId");
    }

    public UIElement textPromoRespcode() {
        return new UIElement(By.xpath(promoResponseCode), getPageName(), "textPromoResponseCode");
    }

    public UIElement textPromoStatus() {
        return new UIElement(By.xpath(promoStatus), getPageName(), "textPromoStatus");
    }

    public UIElement textPaymentMode() {
        return new UIElement(By.xpath(paymentModeXpath), getPageName(), "textPaymentMode");
    }

    public String getRespMsg() {
        String script = "return   document.evaluate('" + respMsgXpath + "', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerHTML;";
        return executeJavaScript(script).toString();
    }


    @Override
    public void waitUntilLoads() {
        try {
            WebDriver driver = DriverManager.getCurrentWebDriver();
            Awaitility.await().atMost(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS).until(() -> driver.getCurrentUrl().contains("/MerchantSite/bankResponse"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("expected response-page to be loaded but it didn't");
//            throw new RuntimeException("expected response-page to be loaded but it didn't");
        }
    }

    public void waitUntilLoads(String maxTime) {
        try {
            WebDriver driver = DriverManager.getCurrentWebDriver();
            Awaitility.await().atMost(Long.parseLong(maxTime), TimeUnit.SECONDS).until(() -> driver.getCurrentUrl().contains("/MerchantSite/bankResponse"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("expected response-page to be loaded but it didn't");
//            throw new RuntimeException("expected response-page to be loaded but it didn't");
        }
    }

    public String getResponseUIFieldValue(ResponseUIField responseUIField) {
        String xpath;
        switch (responseUIField) {
            case RESP_CODE:
                xpath = respCodeXpath;
                break;
            case RESP_MSG:
                xpath = respMsgXpath;
                break;
            case STATUS:
                xpath = statusXpath;
                break;
            case TXN_ID:
                xpath = txnIdXpath;
                break;
            case TXN_AMOUNT:
                xpath = txnAmountXpath;
                break;
            case TXN_DATE:
                xpath = txnDateXpath;
                break;
            case BANK_TXN_ID:
                xpath = bankTxnIdXpath;
                break;
            case CURRENCY:
                xpath = currencyXpath;
                break;
            case SUBS_ID:
                xpath = subsIdXpath;
                break;
            case RESPONSE_STATUS:
                xpath = responseStatus;
                break;
            case ORDER_ID:
                xpath = orderIdXpath;
                break;
            case PAYMENT_MODE:
                xpath = paymentModeXpath;
                break;
            case MID:
                xpath = midXpath;
                break;
            case CARD_INDEX_NO:
                xpath = cardIndexNo;
                break;
            case GATEWAY_NAME:
                xpath = gatewayNameXpath;
                break;

            case BANK_NAME:
                xpath = bankNameXpath;
                break;

            case CHILD_TXN:
                xpath = childTxnXpath;
                break;

            case LASTFOURDIGITS:
                xpath = lastDigitXpath;
                break;

            case CARDBIN:
                xpath = binXpath;
                break;
            case ENC_DATA:
                xpath = ENC_DATA;
                break;
            case MERC_UNQ_REF:
                xpath = mercUnqRefXpath;
                break;
            case CHARGEAMOUNT:
                xpath = chargeAmountXpath;
                break;
            case VPA:
                xpath = vpa;
                break;
            case PREPAID_CARD:
                xpath = prepaidCard;
                break;
            case SPLIT_SETTLEMENT_INFO:
                xpath = splitSettlementInfo;
                break;
            case IS_ACCEPTED:
                xpath = is_accepted;
                break;
            case MANDATE_TYPE:
                xpath = mandateType;
                break;
            case ACCEPTED_REF_NO:
                xpath = AcceptedRefNO;
                break;
            case RISK_INFO:
                xpath = riskInfo;
                break;
            case UDF:
                xpath = udf;
                break;
            case TXN_PAID_TIME:
                xpath = txnpaidtime;
                break;
            default:
                throw new RuntimeException("Incorrect Response UI Field");
        }
        String script = "return   document.evaluate('" + xpath + "', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerHTML;";
        try{
            return executeJavaScript(script).toString().trim();
        }
        catch (JavascriptException exception){
            return FIELD_NOT_AVAILABLE_IN_UI;
        }
    }

    private static final String FIELD_NOT_AVAILABLE_IN_UI = "FIELD_NOT_AVAILABLE_IN_UI";

    private void validate(String fieldName, Object actual, Constants.ValidationType validationType) {

        parameters.add(fieldName);

        if (validationType == Constants.ValidationType.NON_EMPTY) {
            this.softly.assertThat(actual.toString()).as(fieldName).isNotEmpty();
        } else if (validationType == Constants.ValidationType.NOT_PRESENT) {
            this.softly.assertThat(actual).as(fieldName).isNull();
        } else if (validationType == Constants.ValidationType.EMPTY) {
            this.softly.assertThat(actual.toString()).as(fieldName).isEmpty();
        } else {
            throw new RuntimeException("Incorrect validation type: " + validationType);
        }
    }

    public void validateExpectedAndActual(String fieldName, String actualOrderId, String expectedOrderid) {
        if(FIELD_NOT_AVAILABLE_IN_UI.equals(actualOrderId))
            this.softly.fail(fieldName+ " field not available in UI");
        else {
            parameters.add(fieldName);
            this.softly.assertThat(actualOrderId).as(fieldName + " mismatch").isEqualToIgnoringCase(expectedOrderid);
        }

    }
    public void validateExpectedAndActualMultiple(String fieldName, String actualOrderId, String expectedOrderid) {
        if(FIELD_NOT_AVAILABLE_IN_UI.equals(actualOrderId))
            this.softly.fail(fieldName+ " field not available in UI");
        else {
            parameters.add(fieldName);
            if (actualOrderId.equals("Transaction Success")){ this.softly.assertThat(actualOrderId).as(fieldName + " mismatch").isEqualToIgnoringCase("Transaction Success");}

            else
            {this.softly.assertThat(actualOrderId).as(fieldName + " mismatch").isEqualToIgnoringCase(expectedOrderid);}
        }

    }

    public void validateExpectedAndActual(String fieldName, Double actualOrderId, Double expectedOrderid) {
        if(FIELD_NOT_AVAILABLE_IN_UI.equals(actualOrderId))
            this.softly.fail(fieldName+ " field not available in UI");
        else {
            parameters.add(fieldName);
            this.softly.assertThat(actualOrderId).as(fieldName + " mismatch").isEqualByComparingTo(expectedOrderid);
        }
    }

    public ResponsePage validateRespMsg(String expectedRespMsg) {
        //validateExpectedAndActual(ResponseUIField.RESP_MSG.toString(), getResponseUIFieldValue(ResponseUIField.RESP_MSG), expectedRespMsg);
        validateExpectedAndActualMultiple(ResponseUIField.RESP_MSG.toString(), getResponseUIFieldValue(ResponseUIField.RESP_MSG), expectedRespMsg);
        return this;

    }

    public ResponsePage validateRespMsgContains(String expectedRespMsg) {
        validateExpectedAndActual(ResponseUIField.RESP_MSG.toString(), getResponseUIFieldValue(ResponseUIField.RESP_MSG), expectedRespMsg);
//        this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.RESP_MSG)).as(ResponseUIField.RESP_MSG.toString()).containsIgnoringCase(expectedRespMsg);
        return this;
    }

    public ResponsePage validateChargeAmount(String expectedChargeAmount) {
        validateExpectedAndActual(ResponseUIField.CHARGEAMOUNT.toString(), getResponseUIFieldValue(ResponseUIField.CHARGEAMOUNT), expectedChargeAmount);
        return this;
    }

    public ResponsePage validateStatus(String expectedStatus) {
        validateExpectedAndActual(ResponseUIField.STATUS.toString(), getResponseUIFieldValue(ResponseUIField.STATUS), expectedStatus);
        return this;
    }

    public ResponsePage validateUDF(String udf) {
        validateExpectedAndActual(ResponseUIField.UDF.toString(), getResponseUIFieldValue(ResponseUIField.UDF), udf);
        return this;
    }


    public ResponsePage validateSubsId(String expectedSubsId) {
        validateExpectedAndActual(ResponseUIField.SUBS_ID.toString(), getResponseUIFieldValue(ResponseUIField.SUBS_ID), expectedSubsId);
//        this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.SUBS_ID)).as(ResponseUIField.SUBS_ID.toString()).isEqualToIgnoringCase(expectedSubsId);
        return this;
    }

    public ResponsePage validateResponseStatus(String expectedRespStatus) {
        validateExpectedAndActual(ResponseUIField.RESPONSE_STATUS.toString(), getResponseUIFieldValue(ResponseUIField.RESPONSE_STATUS), expectedRespStatus);
//        this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.RESPONSE_STATUS)).as(ResponseUIField.RESPONSE_STATUS.toString()).isEqualToIgnoringCase(expectedRespStatus);
        return this;
    }

    public ResponsePage validateTxnId(String expectedTxnId) {
        validateExpectedAndActual(ResponseUIField.TXN_ID.toString(), getResponseUIFieldValue(ResponseUIField.TXN_ID), expectedTxnId);
//        this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.TXN_ID)).as(ResponseUIField.TXN_ID.toString()).isEqualToIgnoringCase(expectedTxnId);
        return this;
    }

    public ResponsePage validateMid(String expectedMid) {
        validateExpectedAndActual(ResponseUIField.MID.toString(), getResponseUIFieldValue(ResponseUIField.MID), expectedMid);
        return this;
    }


    public ResponsePage validateGatewayName(String expectedGatewayName) {
        validateExpectedAndActual(ResponseUIField.GATEWAY_NAME.toString(), getResponseUIFieldValue(ResponseUIField.GATEWAY_NAME), expectedGatewayName);
        return this;
    }
    public ResponsePage validateVPA(String vpa) {
        validateExpectedAndActual(ResponseUIField.VPA.toString(), getResponseUIFieldValue(ResponseUIField.VPA), vpa);
        return this;
    }

    public ResponsePage validateMandateType(String mandateType) {
        validateExpectedAndActual(ResponseUIField.MANDATE_TYPE.toString(), getResponseUIFieldValue(ResponseUIField.MANDATE_TYPE), mandateType);
        return this;
    }

    public ResponsePage validateIsAccepted(String isAccepted) {
        validateExpectedAndActual(ResponseUIField.IS_ACCEPTED.toString(), getResponseUIFieldValue(ResponseUIField.IS_ACCEPTED), isAccepted);
        return this;
    }


    public ResponsePage validateBankName(String expectedBankName) {

        if(FIELD_NOT_AVAILABLE_IN_UI.equals(getResponseUIFieldValue(ResponseUIField.BANK_NAME)))
            this.softly.fail(ResponseUIField.BANK_NAME.toString()+ " field not available in UI");
        else {
            parameters.add(ResponseUIField.BANK_NAME.toString());
            this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.BANK_NAME)).as(ResponseUIField.BANK_NAME.toString() + " mismatch").contains(expectedBankName);
        }
//        validateExpectedAndActual(ResponseUIField.BANK_NAME.toString(), getResponseUIFieldValue(ResponseUIField.BANK_NAME), expectedBankName);
        return this;
    }

    public ResponsePage validatePrepaidCard(String expectedValue) {
        validateExpectedAndActual(ResponseUIField.PREPAID_CARD.toString(), getResponseUIFieldValue(ResponseUIField.PREPAID_CARD), expectedValue);
        return this;
    }


    public ResponsePage validateChildTxnsPresent() {
        parameters.add(ResponseUIField.CHILD_TXN.toString().toUpperCase());
        return this;
    }

    public ResponsePage validateRespCode(String expectedRespCode) {
        validateExpectedAndActual(ResponseUIField.RESP_CODE.toString(), getResponseUIFieldValue(ResponseUIField.RESP_CODE), expectedRespCode);
        return this;
    }

    public ResponsePage validateOrderId(String expectedOrderId) {
        validateExpectedAndActual(ResponseUIField.ORDER_ID.toString(), getResponseUIFieldValue(ResponseUIField.ORDER_ID), expectedOrderId);
        return this;
    }

    public ResponsePage validateTxnAmount(String expectedTxnAmount) {

//        this.softly.assertThat(Double.parseDouble(getResponseUIFieldValue(ResponseUIField.TXN_AMOUNT)) == Double.parseDouble(expectedTxnAmount)).as("TXNAMOUNT");       //TODO: assertion is not correct
        validateExpectedAndActual(ResponseUIField.TXN_AMOUNT.toString(), Double.parseDouble(getResponseUIFieldValue(ResponseUIField.TXN_AMOUNT)), Double.parseDouble((expectedTxnAmount)));

        return this;
    }

    public ResponsePage validateTxnDate(Date expectedTxnDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedExpectedDate = sdf.format(expectedTxnDate);
        String formattedActualDate = null;
        try {
            formattedActualDate = sdf.format(sdf.parse(getResponseUIFieldValue(ResponseUIField.TXN_DATE)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        validateExpectedAndActual(ResponseUIField.TXN_DATE.toString(), formattedActualDate, formattedExpectedDate);
        return this;
    }
    public ResponsePage validateTxnPaidTime(Date expectedTxnDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedExpectedDate = sdf.format(expectedTxnDate);
        String formattedActualDate = null;
        try {
            formattedActualDate = sdf.format(sdf.parse(getResponseUIFieldValue(ResponseUIField.TXN_PAID_TIME)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        validateExpectedAndActual(ResponseUIField.TXN_PAID_TIME.toString(), formattedActualDate, formattedExpectedDate);
        return this;
    }

    public ResponsePage validateBankTxnId(String expectedBankTxnId) {
        validateExpectedAndActual(ResponseUIField.BANK_TXN_ID.toString(), getResponseUIFieldValue(ResponseUIField.BANK_TXN_ID), expectedBankTxnId);
//        this.softly.assertThat(getResponseUIFieldValue(ResponseUIField.BANK_TXN_ID)).as(ResponseUIField.BANK_TXN_ID.toString()).isEqualToIgnoringCase(expectedBankTxnId);
        return this;
    }

    public ResponsePage validateCurrency(String expectedCurrency) {
        validateExpectedAndActual(ResponseUIField.CURRENCY.toString(), getResponseUIFieldValue(ResponseUIField.CURRENCY), expectedCurrency);
        return this;
    }

    public ResponsePage validatePaymentMode(String expectedPaymentMode) {
        if ("PAYTM_DIGITAL_CREDIT".equals(expectedPaymentMode)) {
            expectedPaymentMode = "Paytm Postpaid";
        }
        validateExpectedAndActual(ResponseUIField.PAYMENT_MODE.toString(), getResponseUIFieldValue(ResponseUIField.PAYMENT_MODE), expectedPaymentMode);
        return this;
    }

    public ResponsePage validateRespMsg(Constants.ValidationType validationType) {
        validate(ResponseUIField.RESP_MSG.toString(), getResponseUIFieldValue(ResponseUIField.RESP_MSG), validationType);
        return this;
    }

    public ResponsePage validateStatus(Constants.ValidationType validationType) {
        validate(ResponseUIField.STATUS.toString(), getResponseUIFieldValue(ResponseUIField.STATUS), validationType);
        return this;
    }

    public ResponsePage validateSubsId(Constants.ValidationType validationType) {
        validate(ResponseUIField.SUBS_ID.toString(), getResponseUIFieldValue(ResponseUIField.SUBS_ID), validationType);
        return this;
    }

    public ResponsePage validateENC_DATA(Constants.ValidationType validationType) {
        validate(ResponseUIField.ENC_DATA.toString(), getResponseUIFieldValue(ResponseUIField.ENC_DATA), validationType);
        return this;
    }

    public ResponsePage validateTxnId(Constants.ValidationType validationType) {
        validate(ResponseUIField.TXN_ID.toString(), getResponseUIFieldValue(ResponseUIField.TXN_ID), validationType);
        return this;
    }

    public ResponsePage validateAcceptedRefNo(Constants.ValidationType validationType) {
        validate(ResponseUIField.ACCEPTED_REF_NO.toString(), getResponseUIFieldValue(ResponseUIField.ACCEPTED_REF_NO), validationType);
        return this;
    }


    public ResponsePage validateMid(Constants.ValidationType validationType) {
        validate(ResponseUIField.MID.toString(), getResponseUIFieldValue(ResponseUIField.MID), validationType);
        return this;
    }

    public ResponsePage validateRespCode(Constants.ValidationType validationType) {
        validate(ResponseUIField.RESP_CODE.toString(), getResponseUIFieldValue(ResponseUIField.RESP_CODE), validationType);
        return this;
    }

    public ResponsePage validateOrderId(Constants.ValidationType validationType) {
        validate(ResponseUIField.ORDER_ID.toString(), getResponseUIFieldValue(ResponseUIField.ORDER_ID), validationType);
        return this;
    }

    public ResponsePage validateTxnAmount(Constants.ValidationType validationType) {
        validate(ResponseUIField.TXN_AMOUNT.toString(), getResponseUIFieldValue(ResponseUIField.TXN_AMOUNT), validationType);
        return this;
    }

    public ResponsePage validateTxnDate(Constants.ValidationType validationType) {
        validate(ResponseUIField.TXN_DATE.toString(), getResponseUIFieldValue(ResponseUIField.TXN_DATE), validationType);
        return this;
    }

    public ResponsePage validateBankTxnId(Constants.ValidationType validationType) {
        validate(ResponseUIField.BANK_TXN_ID.toString(), getResponseUIFieldValue(ResponseUIField.BANK_TXN_ID), validationType);
        return this;
    }

    public ResponsePage validateRiskInfo(Constants.ValidationType validationType) {
        validate(ResponseUIField.RISK_INFO.toString(), getResponseUIFieldValue(ResponseUIField.RISK_INFO), validationType);
        return this;
    }

    public ResponsePage validateCurrency(Constants.ValidationType validationType) {
        validate(ResponseUIField.CURRENCY.toString(), getResponseUIFieldValue(ResponseUIField.CURRENCY), validationType);
        return this;
    }

    public ResponsePage validatePaymentCode(Constants.ValidationType validationType) {
        validate(ResponseUIField.PAYMENT_MODE.toString(), getResponseUIFieldValue(ResponseUIField.PAYMENT_MODE), validationType);
        return this;
    }

    public ResponsePage validateCardIndexNo(Constants.ValidationType validationType) {
        validate(ResponseUIField.CARD_INDEX_NO.toString(), getResponseUIFieldValue(ResponseUIField.CARD_INDEX_NO), validationType);
        return this;
    }

    public ResponsePage validateLastFourDigits(String expectedLastFourDigits) {
        validateExpectedAndActual(ResponseUIField.LASTFOURDIGITS.toString(), getResponseUIFieldValue(ResponseUIField.LASTFOURDIGITS), expectedLastFourDigits);
        return this;
    }

    public ResponsePage validateCardBin(String expectedCardBin) {
        validateExpectedAndActual(ResponseUIField.CARDBIN.toString(),getResponseUIFieldValue(ResponseUIField.CARDBIN), expectedCardBin);
        return this;
    }

    public ResponsePage validateMERC_UNQ_REF(String expectedMercUnqReq) {
        validateExpectedAndActual(ResponseUIField.MERC_UNQ_REF.toString(), getResponseUIFieldValue(ResponseUIField.MERC_UNQ_REF), expectedMercUnqReq);
        return this;
    }

    public ResponsePage validateSplitSettlementInfoWithEscape()
    {
        String splitResponse = getResponseUIFieldValue(ResponseUIField.SPLIT_SETTLEMENT_INFO);
        this.softly.assertThat(splitResponse).contains("\\");
        return this;
    }

    public ResponsePage validateSplitSettlementInfoWithEscape(Boolean isSplitTransaction)
    {
        if(isSplitTransaction) {
            String splitResponse = getResponseUIFieldValue(ResponseUIField.SPLIT_SETTLEMENT_INFO);
            this.softly.assertThat(splitResponse).contains("\\");
        }
        return this;
    }

    public ResponsePage validateSplitSettlementInfoWithoutEscape()
    {
        String splitResponse = getResponseUIFieldValue(ResponseUIField.SPLIT_SETTLEMENT_INFO);
        this.softly.assertThat(splitResponse).doesNotContain("\\");
        return this;

    }

    public ResponsePage validateSplitSettlementInfo(String splitSettlementInfo)
    {
        String splitResponse = getResponseUIFieldValue(ResponseUIField.SPLIT_SETTLEMENT_INFO);
        this.softly.assertThat(splitResponse).contains(splitSettlementInfo);
        return this;
    }


    public ResponsePage assertAll() {
        this.softly.assertAll();
        return this;
    }

    public enum ResponseUIField {
        CHARGEAMOUNT("ChargeAmount"),
        RESP_MSG("RespMsg"),
        STATUS("Status"),
        SUBS_ID("SubsId"),
        RESPONSE_STATUS("RespStatus"),
        TXN_ID("TxnId"),
        MID("Mid"),
        RESP_CODE("RespCode"),
        ORDER_ID("OrderId"),
        TXN_AMOUNT("TxnAmount"),
        TXN_DATE("TxnDate"),
        BANK_TXN_ID("BankTxnId"),
        CURRENCY("Currency"),
        PAYMENT_MODE("PaymentMode"),
        PROMO_CAMP_ID(""),
        CARD_INDEX_NO(""),
        GATEWAY_NAME("GatewayName"),
        BANK_NAME("BankName"),
        CHILD_TXN("CHILDTXNLIST"),
        CHECKSUM("ChecksumHash"),
        LASTFOURDIGITS("LASTFOURDIGITS"),
        CARDBIN("BIN"),
        ENC_DATA("ENC_DATA"),
        VPA("VPA"),
        MERC_UNQ_REF("MERC_UNQ_REF"),
        PREPAID_CARD("PREPAIDCARD"),
        SPLIT_SETTLEMENT_INFO("splitSettlementInfo"),
        IS_ACCEPTED("IS_ACCEPTED"),
        ACCEPTED_REF_NO("ACCEPTED_REF_NO"),
        MANDATE_TYPE("MANDATE_TYPE"),
        RISK_INFO("riskInfo"),
        UDF("udf"),
        TXN_PAID_TIME("txn_paid_time");
        private final String uiField;

        ResponseUIField(String uiField) {
            this.uiField = uiField;
        }

        @Override
        public String toString() {
            return uiField;
        }


    }

}
