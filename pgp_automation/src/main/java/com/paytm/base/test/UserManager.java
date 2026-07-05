package com.paytm.base.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserManager {

    public static Map<String, Long> userMap = new HashMap<String, Long>();

    public static String getUser() {
        Set<String> keys = userMap.keySet();
        Long threadId = Thread.currentThread().getId();
        for (String key : keys) {
            if (userMap.get(key).equals(threadId)) {
                return key;
            }
        }

        while (true) {
            for (String key : keys) {
                if (userMap.get(key) < 0) {
                    userMap.put(key, Thread.currentThread().getId());
                    return key;
                }
            }
            throw new RuntimeException("No free user...");
        }
    }

    public static String getNewUser() {
        Set<String> keys = userMap.keySet();
        while (true) {
            for (String key : keys) {
                if (userMap.get(key) < 0) {
                    userMap.put(key, Thread.currentThread().getId());
                    return key;
                }
            }
            throw new RuntimeException("No free user...");
        }
    }


    public static void releaseUser() {
        Long threadId = Thread.currentThread().getId();
        Set<String> keys = userMap.keySet();
        for (String key : keys) {
            if (userMap.get(key).equals(threadId)) {
                userMap.put(key, (long) -1);
                break;
            }
        }
    }

}
