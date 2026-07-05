package scripts.dataEnrichment;

import com.paytm.appconstants.Constants.Theme;

import com.paytm.dto.processTransactionV1.RiskExtendInfo;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;

public class DataEnrichmentValidations {

    public static void validateNativeRiskExtendInfoParameters(JsonPath jsonPath, RiskExtendInfo riskExtendInfo) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isEqualTo(riskExtendInfo.getOsType()).as("Incorrect osType value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.appVersion")).isEqualTo(riskExtendInfo.getAppVersion()).as("Incorrect appVersion value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceId")).isEqualTo(riskExtendInfo.getDeviceId()).as("Incorrect deviceId value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.latitude")).isEqualTo(riskExtendInfo.getUserLBSLatitude()).as("Incorrect latitude value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.longitude")).isEqualTo(riskExtendInfo.getUserLBSLongitude()).as("Incorrect longitude value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.userAgent")).isEqualTo(riskExtendInfo.getUserAgent()).as("Incorrect userAgent value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.screenResolution")).isEqualTo(riskExtendInfo.getScreenResolution()).as("Incorrect screenResolution value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceModel")).isEqualTo(riskExtendInfo.getDeviceModel()).as("Incorrect deviceModel value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isEqualTo(riskExtendInfo.getBrowserType()).as("Incorrect browserType value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserVersion")).isEqualTo(riskExtendInfo.getBrowserVersion()).as("Incorrect browserVersion value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceManufacturer")).isEqualTo(riskExtendInfo.getDeviceManufacturer()).as("Incorrect deviceManufacturer value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.language")).isEqualTo(riskExtendInfo.getLanguage()).as("Incorrect language value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceIMEI")).isEqualTo(riskExtendInfo.getDeviceIMEI()).as("Incorrect deviceIMEI value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.timeZone")).isEqualTo(riskExtendInfo.getTimeZone()).as("Incorrect timeZone value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.routerMac")).isEqualTo(riskExtendInfo.getRouterMac()).as("Incorrect routerMac value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.platform")).isEqualTo(riskExtendInfo.getPlatform()).as("Incorrect platform value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isNotNull().as("clientIp is null or not present");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osVersion")).isEqualTo(riskExtendInfo.getOsVersion()).as("Incorrect osVersion value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo(riskExtendInfo.getDeviceType()).as("Incorrect deviceType value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.hybridPlatform")).isEqualTo(riskExtendInfo.getHybridPlatform()).as("Incorrect hybridPlatform value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.hybridPlatformVersion")).isEqualTo(riskExtendInfo.getHybridPlatformVersion()).as("Incorrect hybridPlatformVersion value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.gender")).isEqualTo(riskExtendInfo.getGender()).as("Incorrect gender value");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isEqualTo(riskExtendInfo.getMerchantType().toUpperCase()).as("Incorrect merchantType value");

        //validating envInfo.extendInfo parameters
        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);
        softAssertions.assertThat(jsonPath1.getString("osType")).isEqualTo(riskExtendInfo.getOsType()).as("Incorrect osType value");
        softAssertions.assertThat(jsonPath1.getString("appVersion")).isEqualTo(riskExtendInfo.getAppVersion()).as("Incorrect appVersion value");
        softAssertions.assertThat(jsonPath1.getString("deviceId")).isEqualTo(riskExtendInfo.getDeviceId()).as("Incorrect deviceId value");
        softAssertions.assertThat(jsonPath1.getString("latitude")).isEqualTo(riskExtendInfo.getUserLBSLatitude()).as("Incorrect latitude value");
        softAssertions.assertThat(jsonPath1.getString("longitude")).isEqualTo(riskExtendInfo.getUserLBSLongitude()).as("Incorrect longitude value");
        softAssertions.assertThat(jsonPath1.getString("userAgent")).isEqualTo(riskExtendInfo.getUserAgent()).as("Incorrect userAgent value");
        softAssertions.assertThat(jsonPath1.getString("screenResolution")).isEqualTo(riskExtendInfo.getScreenResolution()).as("Incorrect screenResolution value");
        softAssertions.assertThat(jsonPath1.getString("deviceModel")).isEqualTo(riskExtendInfo.getDeviceModel()).as("Incorrect deviceModel value");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isEqualTo(riskExtendInfo.getBrowserType()).as("Incorrect browserType value");
        softAssertions.assertThat(jsonPath1.getString("browserVersion")).isEqualTo(riskExtendInfo.getBrowserVersion()).as("Incorrect browserVersion value");
        softAssertions.assertThat(jsonPath1.getString("deviceManufacturer")).isEqualTo(riskExtendInfo.getDeviceManufacturer()).as("Incorrect deviceManufacturer value");
        softAssertions.assertThat(jsonPath1.getString("language")).isEqualTo(riskExtendInfo.getLanguage()).as("Incorrect language value");
        softAssertions.assertThat(jsonPath1.getString("deviceIMEI")).isEqualTo(riskExtendInfo.getDeviceIMEI()).as("Incorrect deviceIMEI value");
        softAssertions.assertThat(jsonPath1.getString("timeZone")).isEqualTo(riskExtendInfo.getTimeZone()).as("Incorrect timeZone value");
        softAssertions.assertThat(jsonPath1.getString("routerMac")).isEqualTo(riskExtendInfo.getRouterMac()).as("Incorrect routerMac value");
        softAssertions.assertThat(jsonPath1.getString("platform")).isEqualTo(riskExtendInfo.getPlatform()).as("Incorrect platform value");
        softAssertions.assertThat(jsonPath1.getString("clientIp")).isNotNull().as("clientIp is null or not present");
        softAssertions.assertThat(jsonPath1.getString("osVersion")).isEqualTo(riskExtendInfo.getOsVersion()).as("Incorrect osVersion value");
        softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo(riskExtendInfo.getDeviceType()).as("Incorrect deviceType value");
        softAssertions.assertThat(jsonPath1.getString("hybridPlatform")).isEqualTo(riskExtendInfo.getHybridPlatform()).as("Incorrect hybridPlatform value");
        softAssertions.assertThat(jsonPath1.getString("hybridPlatformVersion")).isEqualTo(riskExtendInfo.getHybridPlatformVersion()).as("Incorrect hybridPlatformVersion value");
        softAssertions.assertThat(jsonPath1.getString("gender")).isEqualTo(riskExtendInfo.getGender()).as("Incorrect gender value");
        softAssertions.assertThat(jsonPath1.getString("merchantType")).isEqualTo(riskExtendInfo.getMerchantType().toUpperCase()).as("Incorrect merchantType value");

        //validating riskExtendInfo parameters
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath2.getString("appVersion")).isEqualTo(riskExtendInfo.getAppVersion()).as("Incorrect appVersion value");
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("versionCode")).isEqualTo(riskExtendInfo.getVersionCode()).as("Incorrect versionCode value");
        softAssertions.assertThat(jsonPath2.getString("isRooted")).isEqualTo(riskExtendInfo.getIsRooted()).as("Incorrect isRooted value");
        softAssertions.assertThat(jsonPath2.getString("businessFlow")).isEqualTo(riskExtendInfo.getBusinessFlow()).as("Incorrect businessFlow value");
        softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo(riskExtendInfo.getPlatform()).as("Incorrect platform value");
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo(riskExtendInfo.getOsType()).as("Incorrect osType value");
        softAssertions.assertThat(jsonPath2.getString("cookieId")).isEqualTo(riskExtendInfo.getCookieId()).as("Incorrect cookieId value");
        softAssertions.assertThat(jsonPath2.getString("operationType")).isEqualTo(riskExtendInfo.getOperationType()).as("Incorrect operationType value");
        softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo(riskExtendInfo.getChannelId()).as("Incorrect channelId value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");

        softAssertions.assertAll();
    }

    public static void validateEnhancedNativeRiskExtendInfoParameters(JsonPath jsonPath, String theme) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.userAgent")).isNotNull().as("userAgent not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.screenResolution")).isNotNull().as("screenResolution not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isNotNull().as("browserType not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserVersion")).isNotNull().as("browserVersion not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.language")).isEqualTo("en-US").as("language not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.timeZone")).isEqualTo("Asia/Calcutta").as("timeZone not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isNotNull().as("clientIp is null or not present");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osVersion")).isNotNull().as("osVersion not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isNotNull().as("merchantType not coming");
        if (theme.equals(Theme.ENHANCED_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.platform")).isEqualTo("WEB").as("platform not coming");
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo("Desktop").as("deviceType not coming");
        } else {
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.platform")).isEqualTo("mWeb").as("platform not coming");
            //below assertion needs to be fixed, JIRA : PGP-30465
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo("mobile").as("deviceType not coming");
        }

        //validating envInfo.extendInfo parameters
        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);
        softAssertions.assertThat(jsonPath1.getString("osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath1.getString("userAgent")).isNotNull().as("userAgent not coming");
        softAssertions.assertThat(jsonPath1.getString("screenResolution")).isNotNull().as("screenResolution not coming");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isNotNull().as("browserType not coming");
        softAssertions.assertThat(jsonPath1.getString("browserVersion")).isNotNull().as("browserVersion not coming");
        softAssertions.assertThat(jsonPath1.getString("language")).isEqualTo("en-US").as("language not coming");
        softAssertions.assertThat(jsonPath1.getString("timeZone")).isEqualTo("Asia/Calcutta").as("timeZone not coming");
        softAssertions.assertThat(jsonPath1.getString("clientIp")).isNotNull().as("clientIp not coming");
        softAssertions.assertThat(jsonPath1.getString("osVersion")).isNotNull().as("osVersion not coming");
        softAssertions.assertThat(jsonPath1.getString("merchantType")).isNotNull().as("merchantType not coming");
        if (theme.equals(Theme.ENHANCED_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath1.getString("platform")).isEqualTo("WEB").as("platform not coming");
            softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo("Desktop").as("deviceType not coming");
        } else {
            softAssertions.assertThat(jsonPath1.getString("platform")).isEqualTo("mWeb").as("platform not coming");
            //below assertion needs to be fixed, JIRA : PGP-30465
            softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo("mobile").as("deviceType not coming");
        }

        //validating riskExtendInfo parameters
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("businessFlow")).isEqualTo("STANDARD").as("Incorrect businessFlow value");
        softAssertions.assertThat(jsonPath2.getString("osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath2.getString("operationType")).isEqualTo("PAYMENT").as("Incorrect operationType value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        if (theme.equals(Theme.ENHANCED_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo("WEB").as("Incorrect platform value");
            softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo("WEB").as("Incorrect channelId value");
        } else {
            softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo("mWeb").as("Incorrect platform value");
            softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo("WAP").as("Incorrect channelId value");
        }
        softAssertions.assertAll();
    }

    public static void validateCheckoutJsRiskExtendInfoParameters(JsonPath jsonPath, String theme) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.userAgent")).isNotNull().as("userAgent not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.screenResolution")).isNotNull().as("screenResolution not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isNotNull().as("browserType not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserVersion")).isNotNull().as("browserVersion not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.language")).isEqualTo("en-US").as("language not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.timeZone")).isEqualTo("Asia/Calcutta").as("timeZone not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isNotNull().as("clientIp is null or not present");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osVersion")).isNotNull().as("osVersion not coming");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isNotNull().as("merchantType not coming");
        if (theme.equals(Theme.CHECKOUTJS_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.platform")).isEqualTo("WEB").as("platform not coming");
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo("Desktop").as("deviceType not coming");
        } else {
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.platform")).isEqualTo("mWeb").as("platform not coming");
            //below assertion needs to be fixed, JIRA : PGP-30465
            softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo("mobile").as("deviceType not coming");
        }

        //validating envInfo.extendInfo parameters
        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);
        softAssertions.assertThat(jsonPath1.getString("osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath1.getString("userAgent")).isNotNull().as("userAgent not coming");
        softAssertions.assertThat(jsonPath1.getString("screenResolution")).isNotNull().as("screenResolution not coming");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isNotNull().as("browserType not coming");
        softAssertions.assertThat(jsonPath1.getString("browserVersion")).isNotNull().as("browserVersion not coming");
        softAssertions.assertThat(jsonPath1.getString("language")).isEqualTo("en-US").as("language not coming");
        softAssertions.assertThat(jsonPath1.getString("timeZone")).isEqualTo("Asia/Calcutta").as("timeZone not coming");
        softAssertions.assertThat(jsonPath1.getString("clientIp")).isNotNull().as("clientIp not coming");
        softAssertions.assertThat(jsonPath1.getString("osVersion")).isNotNull().as("osVersion not coming");
        softAssertions.assertThat(jsonPath1.getString("merchantType")).isNotNull().as("merchantType not coming");
        if (theme.equals(Theme.CHECKOUTJS_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath1.getString("platform")).isEqualTo("WEB").as("platform not coming");
            softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo("Desktop").as("deviceType not coming");
        } else {
            softAssertions.assertThat(jsonPath1.getString("platform")).isEqualTo("mWeb").as("platform not coming");
            //below assertion needs to be fixed, JIRA : PGP-30465
            softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo("mobile").as("deviceType not coming");
        }

        //validating riskExtendInfo parameters
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("businessFlow")).isEqualTo("JS_CHECKOUT").as("Incorrect businessFlow value");
        softAssertions.assertThat(jsonPath2.getString("osType")).isNotNull().as("osType not coming");
        softAssertions.assertThat(jsonPath2.getString("operationType")).isEqualTo("PAYMENT").as("Incorrect operationType value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        if (theme.equals(Theme.CHECKOUTJS_WEB_REVAMP)) {
            softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo("WEB").as("Incorrect platform value");
            softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo("WEB").as("Incorrect channelId value");
        } else {
            softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo("mWeb").as("Incorrect platform value");
            softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo("WAP").as("Incorrect channelId value");
        }
        softAssertions.assertAll();
    }

    public static void validateLengthBreachOfParameters(JsonPath jsonPath, RiskExtendInfo riskExtendInfo) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating that parameters are not sent in envInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender", "merchantType");

        //parameters which will come even if length is breached as per the existing logic
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");

        //validating that parameters are not sent in envInfo.extendInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.extendInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender", "merchantType");

        //validating riskExtendInfo parameters, parameters will come even if length is breached as it works as a pass through
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath2.getString("appVersion")).isEqualTo(riskExtendInfo.getAppVersion()).as("Incorrect appVersion value");
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("versionCode")).isEqualTo(riskExtendInfo.getVersionCode()).as("Incorrect versionCode value");
        softAssertions.assertThat(jsonPath2.getString("isRooted")).isEqualTo(riskExtendInfo.getIsRooted()).as("Incorrect isRooted value");
        softAssertions.assertThat(jsonPath2.getString("businessFlow")).isEqualTo(riskExtendInfo.getBusinessFlow()).as("Incorrect businessFlow value");
        softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo(riskExtendInfo.getPlatform()).as("Incorrect platform value");
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo(riskExtendInfo.getOsType()).as("Incorrect osType value");
        softAssertions.assertThat(jsonPath2.getString("cookieId")).isEqualTo(riskExtendInfo.getCookieId()).as("Incorrect cookieId value");
        softAssertions.assertThat(jsonPath2.getString("operationType")).isEqualTo(riskExtendInfo.getOperationType()).as("Incorrect operationType value");
        softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo(riskExtendInfo.getChannelId()).as("Incorrect channelId value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");

        softAssertions.assertAll();
    }

    public static void validateBlankParameters(JsonPath jsonPath, RiskExtendInfo riskExtendInfo) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating that parameters are not sent in envInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender");

        //parameters which will come even if sent as blank as per the existing logic
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");
        //if merchantType does not come from client or come as null, theia derives the same
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isNotNull().as("merchantType not coming");

        //validating that parameters are not sent in envInfo.extendInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.extendInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        //if merchantType does not come from client or come as null, theia derives the same
        softAssertions.assertThat(jsonPath1.getString("merchantType")).isNotNull().as("merchantType not coming");

        //validating riskExtendInfo parameters, parameters will come even if sent as blank as it works as a pass through
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath2.getString("appVersion")).isEqualTo(riskExtendInfo.getAppVersion()).as("Incorrect appVersion value");
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("versionCode")).isEqualTo(riskExtendInfo.getVersionCode()).as("Incorrect versionCode value");
        softAssertions.assertThat(jsonPath2.getString("isRooted")).isEqualTo(riskExtendInfo.getIsRooted()).as("Incorrect isRooted value");
        softAssertions.assertThat(jsonPath2.getString("businessFlow")).isEqualTo(riskExtendInfo.getBusinessFlow()).as("Incorrect businessFlow value");
        softAssertions.assertThat(jsonPath2.getString("platform")).isEqualTo(riskExtendInfo.getPlatform()).as("Incorrect platform value");
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo(riskExtendInfo.getOsType()).as("Incorrect osType value");
        softAssertions.assertThat(jsonPath2.getString("cookieId")).isEqualTo(riskExtendInfo.getCookieId()).as("Incorrect cookieId value");
        softAssertions.assertThat(jsonPath2.getString("operationType")).isEqualTo(riskExtendInfo.getOperationType()).as("Incorrect operationType value");
        softAssertions.assertThat(jsonPath2.getString("channelId")).isEqualTo(riskExtendInfo.getChannelId()).as("Incorrect channelId value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");

        softAssertions.assertAll();
    }

    public static void validateExtraParameters(JsonPath jsonPath) {
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(risExtendInfo).as("scanType not coming").contains("scanType");
        softAssertions.assertThat(risExtendInfo).as("isContact not coming").contains("isContact");
        softAssertions.assertThat(risExtendInfo).as("otpReadFlag not coming").contains("otpReadFlag");
        softAssertions.assertThat(risExtendInfo).as("contactCreateTime not coming").contains("contactCreateTime");
        softAssertions.assertThat(risExtendInfo).as("displayName not coming").contains("displayName");
        softAssertions.assertThat(risExtendInfo).as("mode not coming").contains("mode");
        softAssertions.assertAll();
    }

    public static void validateBlankRiskExtendInfoScenario(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating that parameters are not sent in envInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender"); //Removed merchantType as it is being sent by default


        //parameters which will come even if sent as blank as per the existing logic
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");

        //validating that parameters are not sent in envInfo.extendInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.extendInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender"); //Removed merchantType as it is being sent by default

        //validating riskExtendInfo parameters, parameters will come even if riskExtendInfo is as these are theia derived parameters
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.riskExtendInfo")).doesNotContain("appVersion",
                "versionCode", "isRooted", "businessFlow", "platform", "osType", "cookieId", "operationType", "channelId");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");

        softAssertions.assertAll();
    }

    public static void validateOnlyOneRiskExtendInfoParameterSent(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating that parameters are not sent in envInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender");

        //parameters which will come even if not sent as per the existing logic
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isNotNull().as("osType not coming");
        //if merchantType does not come from client or come as null, theia derives the same
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isNotNull().as("merchantType not coming");

        //validating that parameters are not sent in envInfo.extendInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.extendInfo")).doesNotContain("deviceId", "latitude",
                "longitude", "appVersion", "userAgent", "screenResolution", "deviceModel", "deviceIMEI", "browserType", "browserVersion",
                "deviceManufacturer", "language", "timeZone", "routerMac", "platform", "deviceType", "osVersion", "hybridPlatform",
                "hybridPlatformVersion", "gender");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        //if merchantType does not come from client or come as null, theia derives the same
        softAssertions.assertThat(jsonPath1.getString("merchantType")).isNotNull().as("merchantType not coming");

        //validating riskExtendInfo theia derived parameters
        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.riskExtendInfo")).doesNotContain("appVersion",
                "versionCode", "isRooted", "businessFlow", "platform", "osType", "cookieId", "operationType", "channelId");
        softAssertions.assertThat(jsonPath2.getString("isOnlineMerchant")).isEqualTo("true").as("Incorrect isOnlineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("isOfflineMerchant")).isEqualTo("false").as("Incorrect isOfflineMerchant value");
        softAssertions.assertThat(jsonPath2.getString("requestType")).isEqualTo("NATIVE").as("Incorrect requestType value");

        softAssertions.assertAll();
    }

    public static void validateClientIpField(JsonPath jsonPath, String clientIp) {
        SoftAssertions softAssertions = new SoftAssertions();

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isEqualTo(clientIp).as("clientIp values does not match");

        //validating envInfo.extendInfo parameters
        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);
        softAssertions.assertThat(jsonPath1.getString("clientIp")).isEqualTo(clientIp).as("clientIp values does not match");

        softAssertions.assertAll();
    }

    public static void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerParametersWEB(JsonPath jsonPath, String browser) {
        SoftAssertions softAssertions = new SoftAssertions();

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isEqualTo("Windows").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isEqualTo(browser).as("browserType not coming/not matching");

        //validating envInfo.extendInfo parameters
        softAssertions.assertThat(jsonPath1.getString("osType")).isEqualTo("Windows").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isEqualTo(browser).as("browserType not coming/not matching");

        //validating riskExtendInfo parameters
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo("Windows").as("osType not coming/not matching");

        softAssertions.assertAll();
    }

    public static void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerParametersMobileWEB(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);


        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isEqualTo("Android").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isEqualTo("Chrome").as("browserType not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceModel")).isEqualTo("Nexus 5").as("deviceModel not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceManufacturer")).isEqualTo("LG").as("deviceManufacturer not coming/not matching");

        //validating envInfo.extendInfo parameters
        softAssertions.assertThat(jsonPath1.getString("osType")).isEqualTo("Android").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isEqualTo("Chrome").as("browserType not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("deviceModel")).isEqualTo("Nexus 5").as("deviceModel not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("deviceManufacturer")).isEqualTo("LG").as("deviceManufacturer not coming/not matching");

        //validating riskExtendInfo parameters
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo("Android").as("osType not coming/not matching");

        softAssertions.assertAll();
    }

    public static void validateOsTypeBrowserTypeDeviceModelDeviceManufacturerFieldsIOSMobileWeb(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        String risExtendInfo = jsonPath.getString("REQUEST.request.body.riskExtendInfo");
        JsonPath jsonPath2 = new JsonPath(risExtendInfo);

        //validating envInfo parameters
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.osType")).isEqualTo("iOS").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.browserType")).isEqualTo("Mobile Safari").as("browserType not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceModel")).isEqualTo("iPhone").as("deviceModel not coming/not matching");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceManufacturer")).isEqualTo("Apple").as("deviceManufacturer not coming/not matching");

        //validating envInfo.extendInfo parameters
        softAssertions.assertThat(jsonPath1.getString("osType")).isEqualTo("iOS").as("osType not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("browserType")).isEqualTo("Mobile Safari").as("browserType not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("deviceModel")).isEqualTo("iPhone").as("deviceModel not coming/not matching");
        softAssertions.assertThat(jsonPath1.getString("deviceManufacturer")).isEqualTo("Apple").as("deviceManufacturer not coming/not matching");

        //validating riskExtendInfo parameters
        softAssertions.assertThat(jsonPath2.getString("osType")).isEqualTo("iOS").as("osType not coming/not matching");

        softAssertions.assertAll();
    }

    public static void validateDeviceTypeFieldInTabletDevice(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        //validating in envInfo
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.deviceType")).isEqualTo("tablet").as("deviceType not coming/not matching");

        //validating in envInfo.extendInfo
        softAssertions.assertThat(jsonPath1.getString("deviceType")).isEqualTo("tablet").as("deviceType not coming/not matching");

        softAssertions.assertAll();
    }

    public static void validatePaymentTypeFieldInPaymentBizInfoInNativeFlow(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentType")).isEqualTo("ONE_TIME").as("Incorrect paymentType");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentFlow")).isEqualTo("DEFAULT").as("Incorrect paymentFlow");
        softAssertions.assertAll();
    }

    public static void validatePaymentTypeFieldInPaymentBizInfoInEnhancedNative(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentType")).isEqualTo("ONE_TIME").as("Incorrect paymentType");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.businessFlow")).isEqualTo("STANDARD").as("Incorrect businessFlow");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentFlow")).isEqualTo("DEFAULT").as("Incorrect paymentFlow");
        softAssertions.assertAll();
    }

    public static void validatePaymentTypeFieldInPaymentBizInfoInCheckoutJS(JsonPath jsonPath) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentType")).isEqualTo("ONE_TIME").as("Incorrect paymentType");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.businessFlow")).isEqualTo("JS_CHECKOUT").as("Incorrect businessFlow");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentFlow")).isEqualTo("DEFAULT").as("Incorrect paymentFlow");
        softAssertions.assertAll();
    }
}