package com.paytm.base.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.paytm.LocalConfig;
import com.paytm.api.MerchantCallbackAPI;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.core.wrapper.PGJedisCluster;
import com.paytm.framework.conditions.HardAssertion;
import com.paytm.framework.conditions.Wait;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.listenerDecorators.DefaultListener;
import com.paytm.framework.reporting.reports.AllureReport;
import com.paytm.framework.reporting.reports.MultiReport;
import com.paytm.framework.reporting.reports.PortalReport;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.ServerUtil;
import com.paytm.framework.utils.resourcePool.*;
import com.paytm.listeners.ExecutionListener;
import com.paytm.listeners.FailureHandlingListener;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.Results;
import com.paytm.utils.merchant.user.Cards;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.io.*;
import java.lang.reflect.Method;
import java.security.Permission;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.paytm.LocalConfig.PG_REDIS_CLUSTER_PASS;
import static com.paytm.LocalConfig.PROFILE;
import static com.paytm.framework.reporting.Reporter.report;
import com.aventstack.extentreports.ExtentReports;
import com.paytm.framework.reporting.listeners.RPTestListener;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.IExecutionListener;
import org.testng.annotations.*;

@Listeners({DefaultListener.class, FailureHandlingListener.class, ExecutionListener.class})
public class PGPBaseTest extends BaseTest {

    protected HardAssertion assertion = new HardAssertion();
    //TODO: for verification purpose
    protected Wait pageWait = new Wait(value -> 3, 10, 1000);
    protected Wait peonWait = new Wait(n -> n == 0 ? 0 : 5, 10, 1000);
    protected Wait txnStatusWait = new Wait(n -> n == 0 ? 0 : 3, 10, 1000);
    protected Wait refundStatusWait = new Wait(n -> n == 0 ? 0 : 3, 10, 1000);
    protected Peons peons = new Peons();
    protected Cards cards = new Cards();
    protected Results results = new Results();
    public DecimalFormat format = new DecimalFormat("0.00");
    public final static Set<HostAndPort> staticRedis_nodes = new HashSet<>();
    public final static Set<HostAndPort> sessionRedis_nodes = new HashSet<>();
    public final static Set<HostAndPort> transactionRedis_nodes = new HashSet<>();
    public final static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();


    private static JedisCluster SESSION_REDIS_CLUSTER = null;
    public static JedisCluster STATIC_REDIS_CLUSTER = null;
    private static PGJedisCluster TRANSACTIONAL_REDIS_CLUSTER = null;
    protected ValidatableResponse merchantCallback(String orderId) {
        return Awaitility.await("merchant-callback-wait")
                .pollDelay(Duration.FIVE_SECONDS)
                .pollInterval(Duration.TWO_SECONDS)
                .atMost(Duration.ONE_MINUTE)
                .until(() -> new MerchantCallbackAPI(orderId).execute(), response -> response.statusCode() == 200)
                .then()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json-schemas/merchant-callback-schema.json"));
    }
    protected ValidatableResponse getTxnStatus(String mId, String orderId) {
        return Awaitility.await("get-txn-status-wait")
                .pollInterval(Duration.TWO_SECONDS)
                .atMost(Duration.ONE_MINUTE)
                .until(() -> new TxnStatus(mId, orderId).execute(), response -> response.statusCode() == 200 && !"PENDING".equals(response.getBody().jsonPath().getString("STATUS")))
                .then();
    }
    protected static JedisCluster SESSION_REDIS_CLUSTER() {
        if (SESSION_REDIS_CLUSTER == null) SESSION_REDIS_CLUSTER = new JedisCluster(sessionRedis_nodes, 5 * 1000, 5 * 1000, 3, PG_REDIS_CLUSTER_PASS, poolConfig);
        return SESSION_REDIS_CLUSTER;
    }
    static {
        Arrays.stream(
                LocalConfig.SESSION_REDIS_CLUSTER_URI
                        .replace("redis-cluster://", "")
                        .split(",")
        )
                .map(hostAndPort -> hostAndPort.split(":"))
                .forEach(hostAndPort -> sessionRedis_nodes.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]))));
    }

    // TODO: test code for VM crash issue
    static {
        final SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if( perm.getName().startsWith("exitVM") ) {
                    throw new RuntimeException("Something called exit ") ;
                }
            }
        };
//        System.setSecurityManager(securityManager);
    }


    protected static JedisCluster STATIC_REDIS_CLUSTER() {
        if (STATIC_REDIS_CLUSTER == null) STATIC_REDIS_CLUSTER = new JedisCluster(staticRedis_nodes, 5 * 1000, 5 * 1000, 3, PG_REDIS_CLUSTER_PASS, poolConfig);
        return STATIC_REDIS_CLUSTER;
    }
    static {
        Arrays.stream(
                LocalConfig.STATIC_REDIS_CLUSTER_URI
                        .replace("redis-cluster://", "")
                        .split(",")
        )
                .map(hostAndPort -> hostAndPort.split(":"))
                .forEach(hostAndPort -> staticRedis_nodes.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]))));
    }

    public static PGJedisCluster TRANSACTIONAL_REDIS_CLUSTER(){
        if(TRANSACTIONAL_REDIS_CLUSTER == null) TRANSACTIONAL_REDIS_CLUSTER = new PGJedisCluster(transactionRedis_nodes, 5*1000, 5*1000, 3, PG_REDIS_CLUSTER_PASS, poolConfig);
        return TRANSACTIONAL_REDIS_CLUSTER;
    }
    static {
        Arrays.stream(
                LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI
                    .replace("redis-cluster://", "")
                    .split(",")
        )
                .map(hostport -> hostport.split(":"))
                .forEach(hostport -> transactionRedis_nodes.add(new HostAndPort(hostport[0], Integer.parseInt(hostport[1]))));
    }

    @BeforeMethod
    public void setBrowserBeforeMethods(Method method, ITestResult testResult) {
        try {
//              this.setBrowserBeforeMethods("CHROME", "LINUX", "Nexus 5", "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML,like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25");
        }
        catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }



    static {
        //TODO: once system is stable and tested we can remove AllureReport() object
        report = new MultiReport(new AllureReport(), new PortalReport());
        //TODO: once logger is fixed then again uncomment this code.
//        RestAssured.filters(new ResponseLoggingFilter(),new RequestLoggingFilter());

    }

    private static PrioritySetter prioritySetter = new CountBasedPrioritySetter();
    private static ResourcePool resourcePool = new CustomResourcePool(6, TimeUnit.MINUTES);
    public static ResourceManager<User, Label> userManager = new CustomResourceManager(resourcePool, prioritySetter);
    public static Map<String, Map<String, Object>> MERCHANT_MAP = new HashMap<>();
    public static List<String> result = new ArrayList<>();
    private static ChannelExec channelExec;
    public enum Label {
        CLOSEORDER,
        BASIC,
        POSTPAID,
        IVR,
        PPBL,
        NOPPBL,
        UPIPUSH,
        NOPOSTPAID,
        FOODWALLET,
        PAYTMCCDISABLED,
        GIFTWALLET,
        LOGIN,
        EMIDC,
        SAVEDVPA,
        NONEMIDC,
        RETRYPAYMODE,
        RISKACCEPT,
        ADDMONEYREJECTED,
        RISKREJECT,
        VPAENABLED,
        VPACHECKED,
        POSTPAIDONBOARDING,
        AUTOLOGIN,
        ADVANCEDEPOSIT,
        ADVANCEDEPOSITLOWBALANCEUSER,
        BASICTOKYC,
        INVALIDMOBNO,
        RISKVERIFY,
        MINKYCEXPIRED,
        MGV,
        NOMGV,
        LIMIT,
        PRIORITY,
        LOYALTY,
        UPICONSENT,
        IMPS,
        EDC,
        EXPIREDMGV,
        SINGLECLICKENROLLCARD,
        SINGLECLICKDENROLLCARD,
        SAVECARDMIGRATION,
        SMSNOTSENT,
        UPIPUSHPG2,
        UPIPG2FF4JCONFIGUSER,
         LINK,
        AUTOLINK,
        DEACTIVATEDUSER,
        LOYALTYPOINT,
        USERPAYMODEPOSTPAID,
        COFT,
        NEWWALLETUSER,
        WALLETLIMIT,
        CREDITFREEZE,
        DEBITFREEZE,
        CREDITDEBITFREEZE,
        LINKZEROWALLET,
        PG2WALLETUSER,
        PG2POSTPAIDUSER,
        ZEROWALLET,
        STORECASH,
        NOKYC,
        MINKYC,
        MINKYC1,
        UPILITECC,
        USERWITHNAMEMORETHAN60CHARACTER,
        WALLETBALANCE,
        INACTIVEWALLET,
        FROZENWALLET,
        INACTIVEWALLETZEROBAL,
        BINTXN3,
        LOGIN_STRIP_MID,
        RISKREFUNDUSER,
        OFFERS,
        EMIDCELIGIBLE,
        UNREGISTEREDUSER;
       @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * triggered from @{@link ExecutionListener}
     *
     */
    public static void executeLogCheckPrerequisites() {
        if(LocalConfig.INITIATE_LOGCHECK) {
            try {
                ServerUtil serverutil = new ServerUtil();
                Session session = serverutil.getSession("10.142.51.188:deployerA:deployer@4578");
                System.out.println("beforesuite:: " + "executeLogCheckPrerequisites");
                channelExec = serverutil.getChannel(session, "exec");
                InputStream in = channelExec.getInputStream();
                channelExec.setCommand("sh /paytm/script/new-scripts/pre.sh");
                channelExec.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                    System.out.println("verifyresult" + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * triggered from @{@link ExecutionListener}
     */
    public static void getMerchants() {
        Map<String, Object> map = DataReaderUtil.readYML("merchant.yaml");
        for (String key : map.keySet()) {
            Map<String, Object> merchantDetails = new HashMap<>();
            ArrayList<Map> list = (ArrayList) map.get(key);
            for (Map tempMap : list) {
                merchantDetails.putAll(tempMap);
            }
            MERCHANT_MAP.put(key, merchantDetails);
        }
    }

    /**
     * triggered from @{@link ExecutionListener}
     */
    public static void setupUsers() {
        String[][] users = DataReaderUtil.readCSV("users.csv", "users");
        for (String[] user : users) {
            String[] labelsString = user[2].split("_");
            List<Label> labelsEnum = new ArrayList<>();
            for (String labelString : labelsString) {
                labelsEnum.add(Label.valueOf(labelString.toUpperCase()));
            }
            userManager.add(new CachedUser(user[0], user[1]), labelsEnum);
        }

    }

    /**
     * triggered from @{@link ExecutionListener}
     */
    public static void setEnvironmentDetails() {
        String basePath = System.getProperty("user.dir");
        try{
            OutputStream out = new FileOutputStream(basePath + "/environment.properties");
            Properties properties = new Properties();
            properties.setProperty("Branch", System.getProperty("TESTING_BRANCH", ""));
            properties.setProperty("Environment", System.getProperty("currentProfile", ""));
            properties.setProperty("Browser", System.getProperty("BROWSER", ""));
            properties.setProperty("Execution_Reason", System.getProperty("EXECUTION_REASON", ""));
            properties.setProperty("Execution_StartTime", String.valueOf(new Date()));
            properties.store(out, "");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * triggered from @{@link ExecutionListener}
     */
    public static void savedCardsDataReset() {
        if(LocalConfig.PERFORM_SAVEDCARD_RESET)
            Arrays.asList(
                    "DELETE FROM PGPDB.SAVED_BIN_INFO;",
                    "DELETE FROM PGPDB.SAVED_MID_CARD_INFO;",
                    "DELETE FROM PGPDB.SAVED_CARD_INFO;"
            ).forEach(query -> {
                DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, query);
            });
    }

    /**
     * triggered from @{@link ExecutionListener}
     */
    public static void FF4JFlags() {
        final List<String> ENABLED_FLAGS = Arrays.asList(
                FF4JFeatures.FETCH_SAVED_CARD_FROM_PLATFORM_FOR_MID_CUSTID,
                FF4JFeatures.FETCH_SAVED_CARD_FROM_PLATFORM_FOR_USERID,
                FF4JFeatures.SAVE_CARD_AT_PLATFORM_ON_MID_CUSTID,
                FF4JFeatures.SAVE_CARD_AT_PLATFORM_ON_USERID,
                FF4JFeatures.THEIA_SEND_CIN_AND_8BIN_HASH_TO_PROMO,
                FF4JFeatures.QUERY_NON_SENSITIVE_FOR_CCBILL_PAYMENT,
                FF4JFeatures.BILLPROXY_SHORT_CIRCUIT_READ_FROM_REDIS_CACHE,
                FF4JFeatures.BILLPROXY_SHORT_CIRCUIT_UPDATE_IN_BILL_PAYMENT,
                FF4JFeatures.THEIA_GETMERCHANT_API_URL_INFO,
                FF4JFeatures.THEIA_UIMICROSERVICE_ENHANCEDFLOW_FEATURE,
                FF4JFeatures.THEIA_UIMICROSERVICE_GVCONSENTFLOW_FEATURE,
                FF4JFeatures.THEIA_UIMICROSERVICE_RISKFLOW_FEATURE,
                FF4JFeatures.THEIA_V1_TXN_STATUS_UPI_POLLING,
                FF4JFeatures.THEIA_ADD_MONEY_SOURCE_CONSULT_ENABLE,
                FF4JFeatures.THEIA_BLACKLIST_LPV_ACCESS_TOKEN,
                FF4JFeatures.CREATE_ORDER_IN_INTTXN,
                FF4JFeatures.PREPAID_CARD,
                FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID_CUSTID,
                FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID,
                FF4JFeatures.RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_MIDCUSTID,
                FF4JFeatures.RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_USERID,
                FF4JFeatures.SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_MID_CUSTID
                );
        final List<String> DISABLED_FLAGS = Arrays.asList(
                FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_USERID,
                FF4JFeatures.SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_USERID,
                FF4JFeatures.THEIA_FILTER_PLATFORM_SAVED_ASSETS,
                FF4JFeatures.BLOCKING_FILTER_SAVED_ASSETS_FROM_PLATFORM_FOR_SUBS,
                FF4JFeatures.SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_USERID,
                FF4JFeatures.SC_FETCH_FROM_PLATFORM_REPLACE_ASTRICK_TO_FEATURE,
                FF4JFeatures.SC_FETCH_FROM_PLATFORM_PERCENTAGE_FEATURE,
                FF4JFeatures.SC_PLATFORMSAVEDCARDUSERID
        );
        if(LocalConfig.PERFORM_FF4j_FLAGSETUP) {
            ENABLED_FLAGS.forEach(
                    flag -> {
                        FF4JFlags.enable(flag);
                    }
            );
            DISABLED_FLAGS.forEach(
                    flag -> {
                        FF4JFlags.disable(flag);
                    }
            );
            try {
                Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * triggered from: @{@link ExecutionListener}
     */
    public static void copyProperties() {
        String basePath = System.getProperty("user.dir");
        File categorySourcePath = new File(basePath + "/categories.json");
        File categoryDestPath = new File(basePath + "/allure-results/categories.json");
        File envSourcePath = new File(basePath + "/environment.properties");
        File envDestPath = new File(basePath + "/allure-results/environment.properties");
        try {
            FileUtils.copyFile(categorySourcePath, categoryDestPath);
            FileUtils.copyFile(envSourcePath, envDestPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @BeforeSuite(alwaysRun = true)
    public void clearRedis(ITestContext ctx) {
        if(PROFILE.equalsIgnoreCase("automation")
                &&(ctx.getCurrentXmlTest().getSuite().getName().equalsIgnoreCase("Functional_Regression"))) {
            PreRequisite.execute();
        }
    }


    @AfterMethod(alwaysRun = true)
    public void releaseUser() {
        userManager.release();
    }

    protected double convenienceFeeCalculatorOld(double txnAmount, double percentCommission, double flatCommission, String paymentMode) {
        double serviceTax;
        double convenienceCharges;
        if (percentCommission == 0) {
            serviceTax = txnAmount <= 2000 && paymentMode.equalsIgnoreCase("CC") ? 0.0 : 0.18 * flatCommission;
            convenienceCharges = flatCommission + serviceTax;
        } else if (flatCommission == 0) {
            serviceTax = txnAmount <= 2000 && paymentMode.equalsIgnoreCase("CC") ? 0.0 : 0.18 * percentCommission * txnAmount * 0.01;
            convenienceCharges = 0.01 * txnAmount * percentCommission + serviceTax;
        } else {
            serviceTax = txnAmount <= 2000 && paymentMode.equalsIgnoreCase("CC") ? 0.0 : 0.18 * (flatCommission + 0.01 * percentCommission * txnAmount);
            convenienceCharges = (flatCommission + 0.01 * percentCommission * txnAmount) + serviceTax;
        }
        return CommonHelpers.doubleHalfUpConvertor(convenienceCharges);
    }

    protected double convenienceFeeCalculator(double txnAmount, double percentCommission, double flatCommission, String paymentMode) {
        double serviceTax;
        double convenienceCharges;
        if (percentCommission == 0) {
            serviceTax = 0.18 * flatCommission;
            convenienceCharges = flatCommission + serviceTax;
        } else if (flatCommission == 0) {
            serviceTax = 0.18 * percentCommission * txnAmount * 0.01;
            convenienceCharges = 0.01 * txnAmount * percentCommission + serviceTax;
        } else {
            serviceTax = 0.18 * (flatCommission + 0.01 * percentCommission * txnAmount);
            convenienceCharges = (flatCommission + 0.01 * percentCommission * txnAmount) + serviceTax;
        }
        return CommonHelpers.doubleHalfUpConvertor(convenienceCharges);
    }

}
