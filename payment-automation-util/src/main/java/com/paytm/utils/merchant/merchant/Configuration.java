package com.paytm.utils.merchant.merchant;

import com.paytm.utils.merchant.dto.CreateMerchant;

/**
 * Created by deepakkumar on 26/11/17.
 */
public abstract class Configuration {

    abstract void apply(CreateMerchant merchantConfig);

    abstract void modify(String mid);

}
