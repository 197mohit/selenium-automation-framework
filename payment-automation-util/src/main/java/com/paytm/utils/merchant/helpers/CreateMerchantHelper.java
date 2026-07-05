package com.paytm.utils.merchant.helpers;

import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.api.GetMerchantKey;
import com.paytm.utils.merchant.merchant.*;
import io.qameta.allure.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMerchantHelper {

    private static Map<String, List> newMerchants = new HashMap<>();
    private static NewContract contract;

    public static Map<String, List> getNewMerchants () {
        return newMerchants;
    }

    @Step("{0}")
    public static boolean validateMerchant(String merchantType, String mid) {
        return triggerCURL(mid);
    }

    @Step
    private static boolean triggerCURL(String mid) {
        GetMerchantHelper getMerchantHelper = new GetMerchantHelper();
        getMerchantHelper.fetchMetchantDetails(mid, 10, 5);
        try {
            return getMerchantHelper.getResponse().getStatusCode() == 200;
        } catch (AssertionError e) {
            return false;
        }

    }

    @Step("{0}")
    public static void createMerchant(String merchantType) {
        switch (merchantType) {
            case "PGOnly":
                MERCHANT_PGOnly();
                break;
            case "AddMoney":
                MERCHANT_AddMoney();
                break;
            case "AddMoneyMP":
                MERCHANT_AddMoneyMP();
                break;
            case "AddnPay":
                MERCHANT_AddnPay();
                break;
            case "WalletOnly":
                MERCHANT_WalletOnly();
                break;
            case "Hybrid":
                MERCHANT_Hybrid();
                break;
            case "COD":
                MERCHANT_COD();
                break;
            case "PaytmExpress_Hybrid_Onus":
                MERCHANT_PaytmExpress_Hybrid_Onus();
                break;
            case "PGOnly_Retry":
                MERCHANT_PGOnly_Retry();
                break;
            case "Hybrid_Retry":
                MERCHANT_Hybrid_Retry();
                break;
            case "AddnPay_Retry":
                MERCHANT_AddnPay_Retry();
                break;
            case "Seamless_Hybrid_Onus":
                MERCHANT_Seamless_Hybrid_Onus();
                break;
            case "Seamless_Hybrid_Offus":
                MERCHANT_Seamless_Hybrid_Offus();
                break;
            case "Subscription_PGOnly":
                MERCHANT_Subscription_PGOnly();
                break;
            case "SUBSCRIPTION_PGONLY_RETRY":
                MERCHANT_SUBSCRIPTION_PGONLY_RETRY();
                break;
            case "SUBSCRIPTION_PPI":
                MERCHANT_SUBSCRIPTION_PPI();
                break;
            case "SUBSCRIPTION_PPI_RETRY":
                MERCHANT_SUBSCRIPTION_PPI_RETRY();
                break;
            default:
                Reporter.report.error(new RuntimeException("Merchant Type not supported for creation").getMessage());
                break;
        }
    }

    private static ArrayList<Object> getList(Object... listObjects) {
        ArrayList<Object> list = new ArrayList<>();
        for(Object obj : listObjects)
            list.add(obj);
        return list;
    }

    private static void create(NewContract contract, String merchantType) {
        String mid = contract.create();
        String entityId = contract.getEntityIdFromMid(mid);
        String key = GetMerchantKey.getKey(entityId);

        newMerchants.put(merchantType, getList(mid, key));
    }

    private static void MERCHANT_PGOnly() {
        contract = new NewContract(

                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.UPI_PUSH(),
                Bank.IciciUPI(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall());

        create(contract, "PGOnly");
    }

    private static void MERCHANT_AddMoney() {

        contract = new NewContract(
                Merchant.AddnPay(Merchant.ConvFeeType.DEFAULT, Merchant.ADD_MONEY),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall());

        create(contract, "AddMoney");
    }

    private static void MERCHANT_AddMoneyMP() {

    }

    private static void MERCHANT_AddnPay() {

        contract = new NewContract(
                Merchant.AddnPay(Merchant.ConvFeeType.DEFAULT, Merchant.ADD_MONEY,
                        Merchant.SUBSCRIBE, Merchant.RENEW_SUBSCRIPTION),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall());

        create(contract, "AddnPay");
    }

    private static void MERCHANT_WalletOnly() {
        contract = new NewContract(
                Merchant.WalletOnly(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                DefaultCommission.SimplePercent(1),
                Velocity.Overall()
        );

        create(contract, "WalletOnly");
    }

    private static void MERCHANT_Hybrid() {
        contract = new NewContract(
                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall());

        create(contract, "Hybrid");
    }

    private static void MERCHANT_COD() {
        contract = new NewContract(
                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                Bank.COD(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall());

        create(contract, "COD");
    }

    private static void MERCHANT_PaytmExpress_Hybrid_Onus() {
        contract = new NewContract(
                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT, Merchant.PAYTM_EXPRESS),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
        );

        create(contract, "PaytmExpress_Hybrid_Onus");
    }

    private static void MERCHANT_PGOnly_Retry() {
        contract = new NewContract(
                Merchant.Hybrid(3, Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
        );
        create(contract, "PGOnly_Retry");
    }

    private static void MERCHANT_Hybrid_Retry() {
        contract = new NewContract(
                Merchant.Hybrid(3, Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
        );
        create(contract, "Hybrid_Retry");
    }

    private static void MERCHANT_AddnPay_Retry() {
        contract = new NewContract(
                Merchant.AddnPay(3, Merchant.ConvFeeType.DEFAULT,
                        Merchant.ADD_MONEY, Merchant.SUBSCRIBE, Merchant.RENEW_SUBSCRIPTION),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
        );
        create(contract, "AddnPay_Retry");
    }

    private static void MERCHANT_Seamless_Hybrid_Onus() {
        contract = new NewContract(
                Merchant.AddnPay(Merchant.ConvFeeType.DEFAULT,
                        Merchant.ADD_MONEY, Merchant.SEAMLESS),
                Docs.Default(),
                Urls.Default(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.HDFC_NB(),
                Bank.IciciNB(),
                Bank.PPBL(),
                Bank.Wallet(),
                Bank.PostPaid(),
                Bank.UPI_PUSH(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
        );
    }

    private static void MERCHANT_Seamless_Hybrid_Offus() {

    }

    private static void MERCHANT_Subscription_PGOnly() {

    }

    private static void MERCHANT_SUBSCRIPTION_PGONLY_RETRY() {

    }

    private static void MERCHANT_SUBSCRIPTION_PPI() {

    }

    private static void MERCHANT_SUBSCRIPTION_PPI_RETRY() {

    }

}
