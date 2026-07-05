package com.paytm.utils.merchant.util;

import com.paytm.utils.merchant.UtilConstants;

import java.util.HashMap;
import java.util.Map;

public class VerifyAcquiring {

    private Map<String, String> map = new HashMap<>();

    public static VerifyAcquiring getInstance() {
        return new VerifyAcquiring();
    }

    public Map<String, String> getMap() {
        return this.map;
    }

    public VerifyAcquiring validate(PayMethodType payMethodType, UtilConstants.BankName bankName) {
        setServiceInstanceId(map, bankName.toString());
        setPaymethod(map, payMethodType.toString());
        return this;
    }

    private void setServiceInstanceId(Map<String, String> map, String value) {
        map.put("serviceInstId", value);
    }

    private void setPaymethod(Map<String, String> map, String value) {
        map.put("payMethod", value);
    }


}
