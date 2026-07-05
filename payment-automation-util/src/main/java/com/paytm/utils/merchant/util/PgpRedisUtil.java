package com.paytm.utils.merchant.util;

import com.paytm.framework.utils.RedisUtil;
import com.paytm.pgplus.cache.redis.codec.SerializedObjectCodec;

/**
 * Created by ankuragarwal on 27/9/18
 */
public class PgpRedisUtil {

    static SerializedObjectCodec serializedObjectCodec = new SerializedObjectCodec();

    public static synchronized Object getRedisKey(String redisUrl, String key) {
        byte[] keyArray = serializedObjectCodec.encodeKeyToByteArray(key);
        byte[] value = RedisUtil.getInstance().get(redisUrl, keyArray);
        Object var = serializedObjectCodec.decodeValue(value);
        return var;
    }

}
