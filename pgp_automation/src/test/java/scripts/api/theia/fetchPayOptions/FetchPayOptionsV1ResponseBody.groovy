package scripts.api.theia.fetchPayOptions

import groovy.json.JsonOutput

class FetchPayOptionsV1ResponseBody {
    Head head
    Body body

    @Override
    String toString() {
        JsonOutput.toJson(this)
    }

    static class AccountBalance {
        String currency
        String value
    }

    static class AddMoneyMerchantDetails {
        String merchantVpa
        Object merchantLogo
        String mcc
        String merchantName
    }

    static class AddMoneyPayOption {
        Object savedMandateBanks
        List<SavedInstrumentsItem> savedInstruments
        List<PaymentModesItem> paymentModes
        Object userProfileSarvatra
        Object activeSubscriptions
    }

    static class BalanceInfo {
        Object subWalletDetails
        boolean payerAccountExists
        AccountBalance accountBalance
    }

    static class Body {
        boolean zeroCostEmi
        String orderId
        boolean nativeJsonRequestSupported
        MerchantDetails merchantDetails
        boolean onTheFlyKYCRequired
        UserDetails userDetails
        ResultInfo resultInfo
        String oneClickMaxAmount
        AddMoneyPayOption addMoneyPayOption
        boolean addDescriptionMandatory
        MerchantPayOption merchantPayOption
        boolean walletOnly
        boolean pcfEnabled
        LoginInfo loginInfo
        MerchantLimitInfo merchantLimitInfo
        boolean activeMerchant
        String paymentFlow
        AddMoneyMerchantDetails addMoneyMerchantDetails
        Object extraParamsMap
    }

    static class CardDetails {
        String expiryDate
        String lastFourDigit
        boolean cvvRequired
        String firstSixDigit
        String cardId
        String cardType
        String cvvLength
        boolean indian
        String status
    }

    static class HasLowSuccess {
        String msg
        String status
    }

    static class Head {
        String responseTimestamp
        Object requestId
        String version
    }

    static class IsDisabled {
        Object msg
        String merchantAccept
        String userAccountExist
        String status
    }

    static class LoginInfo {
        boolean mobileNumberNonEditable
        boolean pgAutoLoginEnabled
        boolean userLoggedIn
    }

    static class MaxAmount {
        String currency
        String value
    }

    static class MerchantDetails {
        Object merchantVpa
        Object merchantLogo
        Object mcc
        String merchantName
    }

    static class MerchantLimitInfo {
        List<Object> merchantRemainingLimits
        List<String> excludedPaymodes
        String message
    }

    static class MerchantPayOption {
        Object savedMandateBanks
        List<SavedInstrumentsItem> savedInstruments
        List<PaymentModesItem> paymentModes
        Object userProfileSarvatra
        Object activeSubscriptions
    }

    static class MinAmount {
        String currency
        String value
    }

    static class PayChannelOptionsItem {
        HasLowSuccess hasLowSuccess
        boolean isHybridDisabled
        Object balanceInfo
        IsDisabled isDisabled
        String iconUrl
        MinAmount minAmount
        MaxAmount maxAmount
        String codHybridErrMsg
        String codMessage
        String channelCode
        String channelName
        String emiType
    }

    static class PaymentModesItem {
        List<PayChannelOptionsItem> payChannelOptions
        Object feeAmount
        Object totalTransactionAmount
        String paymentMode
        boolean isHybridDisabled
        String displayName
        boolean onboarding
        IsDisabled isDisabled
        Object taxAmount
        String priority
        boolean prepaidCardSupported
    }

    static class ResultInfo {
        String resultStatus
        String resultCode
        String resultMsg
    }

    static class SavedInstrumentsItem {
        boolean isHybridDisabled
        String displayName
        String priority
        boolean prepaidCard
        boolean oneClickSupported
        String issuingBank
        Object paymentOfferDetails
        HasLowSuccess hasLowSuccess
        CardDetails cardDetails
        boolean isEmiAvailable
        String channelName
        IsDisabled isDisabled
        String iconUrl
        List<String> authModes
        Object savedCardEmisubventionDetail
        String channelCode
        boolean isEmiHybridDisabled
    }

    static class UserDetails {
        boolean kyc
        String mobile
        boolean paytmCCEnabled
        String username
    }
}
