package scripts;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.utils.RedisClusterUtil;
import com.paytm.framework.utils.RedisUtil;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Epic(Constants.Sprint.SPRINT32_1)
@Feature("PGP-21455")
@Owner("Deepak")
public class RedisKeysTest extends PGPBaseTest {

    @Step("Validating for Key: {0}")
    private void key(String keyName){}

    @Test(description = "Verify PG REDIS KEYS after completion of suite")
    public void t1() {
        SoftAssertions softly = new SoftAssertions();
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI);
        Set<String> keySet = jedis.keys("*");
        Map<String, Boolean> m = keySet.stream().collect(Collectors.toMap(Function.identity(), Boolean::new));
        m.replaceAll((k, v) -> v = false);
        int count = 1;
        while (m.values().contains(false)) {
            System.out.println("Attempt: " + count++);
            m.forEach((k, val) -> {
                if (val.equals(false)) {
                    Long ttl = null;
                    try {
                        ttl = jedis.ttl(k);
                        m.put(k, true);
                        key(k);
                        softly.assertThat(ttl).as("Key name: \"" + k + "\" value is \"-1\"").isNotEqualTo(-1);
                    } catch (JedisConnectionException ex) {
                        m.put(k, false);
                    }
                }
            });
            if (count == 5)
                break;
        }
        softly.assertAll();
    }

    @Test(description = "Verify SESSION REDIS KEYS after completion of suite")
    public void t2() {
        SoftAssertions softly = new SoftAssertions();
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI);
        Set<String> keySet = jedis.keys("*");
        Map<String, Boolean> m = keySet.stream().collect(Collectors.toMap(Function.identity(), Boolean::new));
        m.replaceAll((k, v) -> v = false);
        int count = 1;
        while (m.values().contains(false)) {
            System.out.println("Attempt: " + count++);
            m.forEach((k, val) -> {
                if (val.equals(false)) {
                    Long ttl = null;
                    try {
                        ttl = jedis.ttl(k);
                        m.put(k, true);
                        key(k);
                        softly.assertThat(ttl).as("Key name: \"" + k + "\" value is \"-1\"").isNotEqualTo(-1);
                    } catch (JedisConnectionException ex) {
                        m.put(k, false);
                    }
                }
            });
            if (count == 5)
                break;
        }
        softly.assertAll();
    }

    @Test(description = "Verify CLUSTER REDIS KEYS after completion of suite")
    public void t3() {
        SoftAssertions softly = new SoftAssertions();
        StatefulRedisClusterConnection<String, String> connection = RedisClusterUtil
                .getInstance(LocalConfig.PG_REDIS_CLUSTER_URI, LocalConfig.PG_REDIS_CLUSTER_PASS).getConnection();
        List<String> keys = connection.sync().keys("*");
        Map<String, Boolean> m = keys.stream().collect(Collectors.toMap(Function.identity(), Boolean::new));
        m.replaceAll((k, v) -> v = false);
        int count = 1;
        while (m.containsValue(false)) {
            System.out.println("Attempt: " + count++);
            m.forEach((k, val) -> {
                if (val.equals(false)) {
                    Long ttl = null;
                    try {
                        ttl = connection.sync().ttl(k);
                        m.put(k, true);
                        key(k);
                        softly.assertThat(ttl).as("Key name: \"" + k + "\" value is \"-1\"").isNotEqualTo(-1);
                    } catch (Exception ex) {
                        m.put(k, false);
                    }
                }
            });
            if (count == 5)
                break;
        }
        softly.assertAll();
    }

    @Test(description = "Verify ttl of key in SESSION_REDIS should not be greater than 15 minutes")
    public void t4() {
        SoftAssertions softly = new SoftAssertions();
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI);
        Set<String> keySet = jedis.keys("*");
        Map<String, Boolean> m = keySet.stream().collect(Collectors.toMap(Function.identity(), Boolean::new));
        m.replaceAll((k, v) -> v = false);
        int count = 1;
        while(m.containsValue(false)) {
            System.out.println("Attempt: " + count++);
            m.forEach((k,v) -> {
                if(v.equals(false)) {
                    Long ttl = null;
                    try{
                        ttl = jedis.ttl(k);
                        m.put(k, true);
                        key(k);
                        softly.assertThat(ttl)
                                .as("Key name: '"+k+"' ttl is: '"+ttl+"'")
                                .isLessThanOrEqualTo(900000);
                    } catch (Exception ex) {
                        m.put(k, false);
                    }
                }
            });
            if (count == 5)
                break;
        }
        softly.assertAll();
    }

}