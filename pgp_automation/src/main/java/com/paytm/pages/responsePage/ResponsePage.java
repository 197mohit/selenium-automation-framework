package com.paytm.pages.responsePage;

import com.paytm.framework.conditions.CList;
import com.paytm.framework.conditions.CString;
import com.paytm.framework.conditions.Condition;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.WebPage;
import com.paytm.utils.merchant.util.PGPUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;


public class ResponsePage extends WebPage {

    public CString get(String attribute) {
        String text = Optional.ofNullable(((JavascriptExecutor) DriverManager.getDriver()).executeScript("var val = document.evaluate(`//table/tbody//*[text()='" + attribute + "']/../td[2]`, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; return val !== null ? val.innerHTML : null")).orElse("null").toString().trim();
        return new CString(text) {
            private final CString self = this;

            @Override
            public String toString() {
                return format(ResponsePage.this + " " + "{0}({1})", attribute, text);
            }

            @Override
            public Condition equals(double expected) {
                return new Condition() {
                    @Override
                    public boolean getAsBoolean() {
                        return text != null && Double.valueOf(text) - expected == 0;
                    }

                    @Override
                    public String toString() {
                        return format("{0} equals {1}", self, expected);
                    }
                };
            }
        };
    }

    public Condition hasValidChecksum(String merchantKey) {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                List<String> keys = DriverManager.getDriver().findElements(By.xpath("//table/tbody/tr/td[1]")).stream().map(WebElement::getText).collect(Collectors.toList());
                AtomicReference<String> checksum = new AtomicReference<>("");
                TreeMap<String, String> map = new TreeMap<>();
                keys.forEach(key -> {
                    String value = ResponsePage.this.get(key).getValue();
                    if (Attribute.CHECKSUMHASH.equalsIgnoreCase(key)) {
                        checksum.set(value.replaceAll(" ", "+"));
                    } else {
                        map.put(key, value);
                    }
                });
                return PGPUtil.isChecksumValid(merchantKey, map, checksum.get());
            }

            @Override
            public String toString() {
                return format("{0} has valid checksum for merchantKey({1})", ResponsePage.this, merchantKey);
            }
        };
    }

    public CList<String> keys() {
        List<String> keys = DriverManager.getDriver().findElements(By.xpath("//table/tbody/tr/td[1]")).stream().map(WebElement::getText).collect(Collectors.toList());
        return new CList<String>(keys) {
            @Override
            public String toString() {
                return "merchant-callback-attributes(" + this.getValue() + ")";
            }
        };
    }

    @Override
    public Condition hasLoaded() {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return ResponsePage.super.hasLoaded().getAsBoolean() && DriverManager.getDriver().getCurrentUrl().contains("/MerchantSite/bankResponse");
            }

            @Override
            public String toString() {
                return ResponsePage.super.hasLoaded().toString();
            }
        };
    }

    @Override
    public String toString() {
        return "response-page";
    }

    public interface Attribute {
        String MID = "MID";
        String RESPMSG = "RESPMSG";
        String STATUS = "STATUS";
        String SUBS_ID = "SUBS_ID";
        String TXNID = "TXNID";
        String RESPCODE = "RESPCODE";
        String ORDERID = "ORDERID";
        String TXNAMOUNT = "TXNAMOUNT";
        String TXNDATE = "TXNDATE";
        String BANKTXNID = "BANKTXNID";
        String CURRENCY = "CURRENCY";
        String PROMO_CAMP_ID = "PROMO_CAMP_ID";
        String PROMO_RESPCODE = "PROMO_RESPCODE";
        String PROMO_STATUS = "PROMO_STATUS";
        String PAYMENTMODE = "PAYMENTMODE";
        String CHECKSUMHASH = "CHECKSUMHASH";
        String CHARGEAMOUNT = "CHARGEAMOUNT";
        String BIN = "BIN";
        String LAST_FOUR_DIGITS = "LASTFOURDIGITS";
        String CARD_SCHEME = "cardScheme";
        String CHILD_TXN_LIST = "CHILDTXNLIST";
        String PREPAID_CARD = "PREPAIDCARD";
        String GATEWAY_NAME = "GATEWAYNAME";
        String CARD_INDEX_NO = "cardIndexNo";
        String CARD_HASH = "cardHash";
        String SPLIT_SETTLEMENT_INFO = "splitSettlementInfo";
    }
}
