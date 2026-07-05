package com.paytm.api;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;

    public class CreateSubscription {
    private String txnToken, subsId;
    private InitTxnResponseDTO initTxnResponse;
    private OrderDTO orderDTO;
    private Constants.MerchantType merchant;
    private InitTxnDTO initTxnDTO;
    private TxnStatus txnStatus;
    private String subsStartDate;
    private String aquirementId;
    private String subsStatus;
    private String savedCardId;
    private String appInvokeDevice;

    public String getTxnToken() {
        return txnToken;
    }

    public String getSubsId() {
        return subsId;
    }

    public TxnStatus getTxnStatus() {
        return txnStatus;
    }

    public String getSubsStartDate() {
        return subsStartDate;
    }

    public String getAquirementId() {
        return aquirementId;
    }

    public String getSubsStatus() {
        return subsStatus;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public String getAppInvokeDevice(){return appInvokeDevice;}

    public CreateSubscription(InitTxnDTO initTxnDTO, Constants.MerchantType merchantType) {
        this.initTxnDTO = initTxnDTO;
        this.merchant = merchantType;
    }

    @Step("Create Subscription")
    public CreateSubscription paymethodType(PayMethodType payMethodType, PaymentDTO... paymentDTO) {
        this.initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        if(!this.initTxnResponse.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"))
            throw new SkipException("Initiate transaction response is not success");
        this.txnToken = initTxnResponse.getBody().getTxnToken();
        this.subsId = initTxnResponse.getBody().getSubscriptionId();
        PaymentDTO paymentDTO1 = new PaymentDTO();
        if(paymentDTO.length != 0)
            paymentDTO1 = paymentDTO[0];
        switch (payMethodType) {
            case CREDIT_CARD:
                this.orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, payMethodType, subsId, paymentDTO1)
                        .setAUTH_MODE("3D")
                        .build();
                break;
            case DEBIT_CARD:
                this.orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, payMethodType, subsId, paymentDTO1)
                        .setAUTH_MODE("otp")
                        .build();
                break;
            case BALANCE:
                this.orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, payMethodType, subsId)
                        .setAUTH_MODE("USRPWD")
                        .build();
                break;
            case ADDANDPAY:
                this.orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD, subsId, paymentDTO1)
                        .setAUTH_MODE("3D")
                        .setPaymentFlow(payMethodType.toString())
                        .build();
                break;

            case PPBL:
                this.orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PPBL, subsId, paymentDTO1)
                        .setAUTH_MODE("MPIN")
                        .setMpin(paymentDTO1.getPasscode())
                        .build();
                break;
            default:
        }
        return this;
    }



    public CreateSubscription fetchPayOptions(Constants.MerchantType merchant, String orderId)
    {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(this.txnToken, merchant.getId(), orderId);
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getSubscriptionDetail().getSubsId())
                  .as("Subscription Details are not correct")
                 .isEqualTo(subsId);

        return this;
    }

    @Step
    public CreateSubscription pay() {
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        this.txnStatus = PGPHelpers.getTxnStatus(this.merchant.getId(), this.orderDTO.getORDER_ID());
        if(!this.txnStatus.getResponse().getSTATUS().equalsIgnoreCase("TXN_SUCCESS"))
            throw new SkipException("Transaction is not successful");
        this.subsStartDate = PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID());
        this.aquirementId = PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID());
        this.subsStatus = PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID());
        this.savedCardId = PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID());
        return this;
    }

        @Step
        public CreateSubscription pay(String blankSubsId) {
            CheckoutPage checkoutPage = new CheckoutPage();
            orderDTO.setSUBSCRIPTION_ID("");//blank subsid
            checkoutPage.createNativeOrder(orderDTO, false);
            this.txnStatus = PGPHelpers.getTxnStatus(this.merchant.getId(), this.orderDTO.getORDER_ID());
            if(!this.txnStatus.getResponse().getSTATUS().equalsIgnoreCase("TXN_SUCCESS"))
                throw new SkipException("Transaction is not successful");
            this.subsStartDate = PGPHelpers.executeUntilSubsContractNotFound("subcription_start_date", subsId, orderDTO.getORDER_ID());
            this.aquirementId = PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID());
            this.subsStatus = PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID());
            this.savedCardId = PGPHelpers.executeUntilSubsContractNotFound("saved_card_id", subsId, orderDTO.getORDER_ID());
            Assertions.assertThat(this.subsStartDate).isNotEmpty();
            Assertions.assertThat(this.aquirementId).isNotEmpty().isNotNull();
            Assertions.assertThat(this.subsStatus).isNotEmpty().isEqualTo("ACTIVE");
            Assertions.assertThat(this.savedCardId).isNotEmpty();
            return this;
        }




    }
