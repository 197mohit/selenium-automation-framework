package scripts.UI;

import com.paytm.api.UpiPredicate;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test;

import static io.restassured.RestAssured.given;


public class upiUITests extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final UpiConsent upiConsent = new UpiConsent();
    private final Constants.MerchantType merchantPG = Constants.MerchantType.UPI_CONSENT_PG;
    private final Constants.MerchantType merchantHyb = Constants.MerchantType.UPI_CONSENT_PG;
    private final Constants.MerchantType merchantAdd = Constants.MerchantType.UPI_CONSENT_ADD;
    private final String condition = "\"{request -> request.getParameter('cust-id')=='1000130288'}\"";
    private final String upiProfileData = "\"status\": \"SUCCESS\",\n" +
            "        \"seqNo\": \"9800907760114504bf4756b9606a62cepgpsandbox101paytmlocal\",\n" +
            "        \"respMessage\": \"\",\n" +
            "        \"respCode\": \"0\",\n" +
            "        \"respDetails\": {\n" +
            "            \"profileDetail\": {\n" +
            "                \"vpaDetails\": [\n" +
            "                    {\n" +
            "                        \"name\": \"ankitarora26@paytm\",\n" +
            "                        \"defaultCreditAccRefId\": \"10673\",\n" +
            "                        \"defaultDebitAccRefId\": \"10673\",\n" +
            "                        \"isPrimary\": true\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"bankAccounts\": [\n" +
            "                    {\n" +
            "                        \"bank\": \"Mypsp2\",\n" +
            "                        \"ifsc\": \"AABF0876543\",\n" +
            "                        \"accRefId\": \"10673\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0125\",\n" +
            "                        \"accountType\": \"UOD\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"P2M\",\n" +
            "                        \"warningMessage\": \"Unsecured Overdraft Account can only be used to make payments to merchants\",\n" +
            "                        \"pgBankCode\": \"PPBL\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"100000\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https://static.paytmbank.com/upi/images/bank-logo/500004.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"MYPSP\",\n" +
            "                        \"ifsc\": \"AABC0876543\",\n" +
            "                        \"accRefId\": \"10679\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0127\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"ATMPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"HDFC\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500001.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"Mybank\",\n" +
            "                        \"ifsc\": \"AABD0876543\",\n" +
            "                        \"accRefId\": \"10761\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0123\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"N\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"NHAI\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"RED\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"The Bank is experiencing downtime.Please select another payment option\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500007.png\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"profileStatus\": \"ACTIVE\",\n" +
            "                \"upiLinkedMobileNumber\": \"919999161601\",\n" +
            "                \"isDeviceBinded\": false\n" +
            "            },\n" +
            "            \"metaDetails\": {\n" +
            "                \"banksDown\": [],\n" +
            "                \"npciHealthCategory\": \"GREEN\",\n" +
            "                \"npciHealthMsg\": \"\",\n" +
            "                \"txnAction\": null\n" +
            "            }\n" +
            "        }";

    /* UPI DAILY TXN LIMIT TC */

    @Test(description = "Verify that the amount limit breach message is displayed for PG only merchant, also verify the msg")
    @Parameters({"theme"})
    public void upiAmountBreach1(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);

        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantPG.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantPG.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String limit = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[1].bankMetaData.perTxnLimit");
        String upiAmount = limit + "1";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_PG, theme)
                .setTXN_AMOUNT(upiAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);
        upiConsent.selectUpiPush();
        String limitmsg = upiConsent.getLimitMsg();
        Assertions.assertThat(limitmsg.contains("UPI transaction limit for this bank is Rs" + limit));

    }

    @Test(description = "Verify that the amount limit msg is not displayed for a hybrid merchant when wallet is checked")
    @Parameters({"theme"})
    public void upiAmountBreach2(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantHyb.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantHyb.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String limit = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0].bankMetaData.perTxnLimit");
        Assert.assertEquals(limit, "1,00,000");
        Double upiAmount = 100001.00;

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_HYB, theme)
                .setTXN_AMOUNT(String.valueOf(upiAmount))
                .setSSO_TOKEN(user.ssoToken()).build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        upiConsent.selectUpiPush();
        String limitmsg = upiConsent.getLimitMsg();
        Assertions.assertThat(limitmsg.contains("false"));

    }

    @Test(description = "Verify that the amount limit msg is displayed for a hybrid merchant when wallet is unchecked")
    @Parameters({"theme"})
    public void upiAmountBreach3(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantHyb.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantHyb.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String limit = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0].bankMetaData.perTxnLimit");
        Assert.assertEquals(limit, "1,00,000");
        Double upiAmount = 100001.00;

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_HYB, theme)
                .setTXN_AMOUNT(String.valueOf(upiAmount))
                .setSSO_TOKEN(user.ssoToken()).build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        upiConsent.selectUpiPush();
        upiConsent.uncheckWallet();
        String limitmsg = upiConsent.getLimitMsg();
        Assertions.assertThat(limitmsg.contains("UPI transaction limit for this bank is Rs" + limit));
    }

    @Test(description = "Verify that when addnpay amount is less than the limit for upi then limit breach msg should not be displayed")
    @Parameters({"theme"})
    public void upiAmountBreach4(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantAdd.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantAdd.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String limit = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0].bankMetaData.perTxnLimit");
        Assert.assertEquals(limit, "1,00,000");
        Double upiAmount = 100001.00;

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_ADD, theme)
                .setTXN_AMOUNT(String.valueOf(upiAmount))
                .setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00 );
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        upiConsent.selectUpiPush();
        String limitmsg = upiConsent.getLimitMsg();
        Assertions.assertThat(limitmsg.contains("false"));

    }


    @Test(description = "Verify that when addnpay amount is greater than the limit for upi then limit breach msg should be displayed")
    @Parameters({"theme"})
    public void upiAmountBreach5(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantAdd.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantAdd.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String limit = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[1].bankMetaData.perTxnLimit");
        String upiAmount = limit + "1";

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_ADD, theme)
                .setTXN_AMOUNT(upiAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, (double) 1);
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        upiConsent.selectUpiPush();
        String limitmsg = upiConsent.getLimitMsg();
        Assertions.assertThat(limitmsg.contains("UPI transaction limit for this bank is Rs" + limit));
    }

    /* UPI CONSENT TC - To be automated on APP*/


    /* UPI BANK HEALTH AND NPCI HEALTH TC*/


    private final String upiNpciData = "\"status\": \"SUCCESS\",\n" +
            "        \"seqNo\": \"9800907760114504bf4756b9606a62cepgpsandbox101paytmlocal\",\n" +
            "        \"respMessage\": \"\",\n" +
            "        \"respCode\": \"0\",\n" +
            "        \"respDetails\": {\n" +
            "            \"profileDetail\": {\n" +
            "                \"vpaDetails\": [\n" +
            "                    {\n" +
            "                        \"name\": \"ankitarora26@paytm\",\n" +
            "                        \"defaultCreditAccRefId\": \"10673\",\n" +
            "                        \"defaultDebitAccRefId\": \"10673\",\n" +
            "                        \"isPrimary\": true\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"bankAccounts\": [\n" +
            "                    {\n" +
            "                        \"bank\": \"Mypsp2\",\n" +
            "                        \"ifsc\": \"AABF0876543\",\n" +
            "                        \"accRefId\": \"10673\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0125\",\n" +
            "                        \"accountType\": \"UOD\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"P2M\",\n" +
            "                        \"warningMessage\": \"Unsecured Overdraft Account can only be used to make payments to merchants\",\n" +
            "                        \"pgBankCode\": \"PPBL\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"100000\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https://static.paytmbank.com/upi/images/bank-logo/500004.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"MYPSP\",\n" +
            "                        \"ifsc\": \"AABC0876543\",\n" +
            "                        \"accRefId\": \"10679\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0127\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"ATMPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"HDFC\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"100\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"YELLOW\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500001.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"Mybank\",\n" +
            "                        \"ifsc\": \"AABD0876543\",\n" +
            "                        \"accRefId\": \"10761\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0123\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"N\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"NHAI\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"RED\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500007.png\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"profileStatus\": \"ACTIVE\",\n" +
            "                \"upiLinkedMobileNumber\": \"919999161601\",\n" +
            "                \"isDeviceBinded\": false\n" +
            "            },\n" +
            "            \"metaDetails\": {\n" +
            "                \"banksDown\": [],\n" +
            "                \"npciHealthCategory\": \"RED\",\n" +
            "                \"npciHealthMsg\": \"The Bank is experiencing downtime.Please select another payment option\",\n" +
            "                \"txnAction\": null\n" +
            "            }\n" +
            "        }";


    @Test(description = "Verify that all UPI paymodes are disabled when npcihealth that is returned is RED , also verify the npciHealth down message on UI")
    @Parameters({"theme"})
    public void npcihealth1(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiNpciData);
        upiPredicate.execute();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantPG.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantPG.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String npciHealthCategory = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.metaDetails.npciHealthCategory");
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_PG, theme)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        String upiEnabled = upiConsent.isUpiEnabled();
        String npciDownMsg = upiConsent.npciDownMsg();
        Assertions.assertThat(upiEnabled.contains("false"));
        Assertions.assertThat(npciHealthCategory.contains("RED"));
        Assertions.assertThat(npciDownMsg.contains("The Bank is experiencing downtime.Please select another payment option"));
    }

    @Test(description = "Verify that all UPI paymodes are enabled when npcihealth that is returned is GREEN")
    @Parameters({"theme"})
    public void npcihealth2(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantPG.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantPG.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String npciHealthCategory = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.metaDetails.npciHealthCategory");
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_PG, theme)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        String upiEnabled = upiConsent.isUpiEnabled();
        Assertions.assertThat(upiEnabled.contains("true"));
        Assertions.assertThat(npciHealthCategory.contains("GREEN"));
    }

    @Test(description = "Verify that when a bank health is down then only that bank is disabled, other banks and upi collect section should be enabled; also verify the bank health down message")
    @Parameters({"theme"})
    public void npcihealth3(@Optional("enhancedweb") String theme) throws Exception {

        User user=userManager.getForRead(Label.UPICONSENT);
        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantPG.getId())
                .setToken(user.ssoToken())
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchantPG.getId()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        String bankHealth = fetchPaymentOptResponse.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[2].bankMetaData.bankHealth.category");
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CONSENT_PG, theme)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme);

        String bankDown = upiConsent.isBankDown();
        String bankDownMsg = upiConsent.bankDownMsg();
        Assertions.assertThat(bankDown.contains("false"));
        Assertions.assertThat(bankHealth.contains("RED"));
        Assertions.assertThat(bankDownMsg.contains("The Bank is experiencing downtime.Please select another payment option"));
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine For Standerd Checkout Flow")
    public void UPICreditLineEligibilityStanderdCheckout(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String errorMsg = "This merchant is not accepting Credit Line on UPI.";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CREDITLINE_ELIGIBLE, theme)
                .setTXN_AMOUNT("20.00").build();
        System.out.println(orderDTO.getORDER_ID());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeErrorMsg(errorMsg);
        }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine And PPIWallet For Standerd Checkout Flow")
    public void UPICreditLineAndWalletEligibilityStanderdCheckout(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String errorMsg = "This merchant is not accepting Wallet, Credit Line on UPI.";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CREDITLINEANDWALLET_ELIGIBLE, theme)
                .setTXN_AMOUNT("20.00").build();
        System.out.println(orderDTO.getORDER_ID());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeErrorMsg(errorMsg);
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Parameters({"theme"})
    @Test(description = "Verify Eligibility ErrorMsg Of UPI CreditLine Which is Pick form locale For Standerd Checkout Flow")
    public void UPICreditLineEligibilityFromLocaleStanderdCheckout(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String errorMsg = "This merchant is not accepting Credit Line on UPI.";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_CREDITLINE_LOCALE, theme)
                .setTXN_AMOUNT("20.00").build();
        System.out.println(orderDTO.getORDER_ID());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.validateUpiSubPayModeErrorMsg(errorMsg);
    }
}
