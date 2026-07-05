package com.paytm.utils.merchant.merchant;

/**
 * Created by rahulkumar on Mar,2018
 */
public class NewMerchantContractWithMigration  implements MerchantContract{
    private final MerchantContract contract;
    NewMerchantContractWithMigration(){
        this.contract=new NewContract();
    }

    @Override
    public String create() {
        String mid = contract.create();
        new SingleEventMigration(mid).waitTillCompletion();
        return mid;
    }

    @Override
    public String createWithoutDBcheck() {
        String mid = contract.create();
        new SingleEventMigration(mid).waitTillCompletion();
        return mid;
    }
}
