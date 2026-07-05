package com.paytm.base.test;

import com.paytm.framework.datareader.DataReaderUtil;
import org.testng.SkipException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by deepakkumar on 28/12/17.
 */
public class MerchantManager {

    private static Map<String, Long> merchantMap = new HashMap<String, Long>();
    private static Map<String, String> merchantDetails = new HashMap<>();

    static {
        String[][] merchants = DataReaderUtil.readCSV("merchants.csv", "merchants");
        for (String merchant[] : merchants) {
            String mid = merchant[0];
            String key = mid.replace(mid.split("_")[0] + "_", "");
            mid = mid.replace("_" + key, "");
            MerchantManager.merchantDetails.put(mid, key);
            MerchantManager.merchantMap.put(mid, (long) - 1);
        }
    }


    public static String getMerchant() {
        Set<String> keys = merchantMap.keySet();
        Long threadId = Thread.currentThread().getId();
        for (String key : keys) {
            if (merchantMap.get(key).equals(threadId)) {
                return key;
            }
        }

        while (true) {
            for (String key : keys) {
                if (merchantMap.get(key) < 0) {
                    merchantMap.put(key, Thread.currentThread().getId());
                    return key;
                }
            }
            throw new SkipException("No free merchant...");
        }
    }

    public static String getMerchantKey(String mid) {
        return merchantDetails.get(mid);
    }

}
