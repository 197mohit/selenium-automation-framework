package com.paytm.framework.reportportal.service;

import com.epam.reportportal.annotations.UniqueID;
import com.epam.reportportal.annotations.attribute.Attributes;
import com.epam.reportportal.listeners.Statuses;
import com.epam.reportportal.testng.util.internal.LimitedSizeConcurrentHashMap;
import com.epam.reportportal.utils.AttributeParser;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.framework.reportportal.api.FinishTestItemRequest;
import com.paytm.framework.reportportal.api.LogItemRequest;
import com.paytm.framework.reportportal.api.StartChildTestItemRequest;
import com.paytm.framework.reportportal.api.StartTestItemRequest;
import com.paytm.framework.reportportal.contants.ItemStatusEnum;
import com.paytm.framework.reportportal.contants.TestAttributeKeys;
import com.paytm.framework.reportportal.contants.TestMethodTypeEnum;
import com.paytm.framework.reportportal.service.dto.Attribute;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import com.paytm.framework.reportportal.service.dto.TestParameter;
import com.paytm.framework.reportportal.service.launchManager.LaunchHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.*;
import org.testng.annotations.Parameters;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.IParameterInfo;
import rp.com.google.common.base.Throwables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static rp.com.google.common.base.Throwables.getStackTraceAsString;

/**
 * @author ankuragarwal
 */
/*
    Known Issues:
    1. When complete launch is retried, then new entry of test method will be generated
        instead of updating existing testMethod data. This will impact final test case counts.
    2.
 */

public class RPTestNGService implements RPTestNGServiceImpl {
    private static final int MAXIMUM_HISTORY_SIZE = 10000;
    private static final Predicate<StackTraceElement> IS_RETRY_ELEMENT = e -> "org.testng.internal.TestInvoker".equals(e.getClassName())
            && "retryFailed".equals(e.getMethodName());
    private static final Predicate<StackTraceElement[]> IS_RETRY = eList -> Arrays.stream(eList).anyMatch(IS_RETRY_ELEMENT);
    private final Map<Object, Boolean> RETRY_STATUS_TRACKER = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);
    private final Map<ITestNGMethod, Boolean> RETRY_STATUS_TRACKER_V2 = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);       //added for retry issue fix
    private final Map<Object, String> RETRY_STATUS_TRACKER_ID = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);
    private final Map<ITestNGMethod, String> RETRY_STATUS_TRACKER_ID_V2 = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);     //added for retry issue fix
    private final Map<Object, Boolean> SKIPPED_STATUS_TRACKER = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);
    private final Map<Object, String> SKIPPED_STATUS_TRACKER_ID = new LimitedSizeConcurrentHashMap<>(MAXIMUM_HISTORY_SIZE);
    private final Map<Object, Queue<Pair<String, FinishTestItemRequest>>> BEFORE_METHOD_TRACKER = new ConcurrentHashMap<>();
    private AtomicBoolean isLaunchFailed = new AtomicBoolean();
    //    private String launchId;
    private LaunchInfo launchInfo = LaunchInfo.getInstance();

    private static Set<ITestResult> getTestResults(IResultMap rm) {
        return ofNullable(rm).map(IResultMap::getAllResults).orElse(Collections.emptySet());
    }


    /**
     * Start current launch
     */
    @Override
    public void startLaunch() {
        try {
            LaunchHandler launchHandler = LoadFactory.getLaunchHandler();
            this.launchInfo.setLaunchId(launchHandler.startLaunch());
            Runtime.getRuntime().addShutdownHook(
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LoadFactory.getLaunchHandler().finishLaunch(launchInfo.getLaunchId(), ItemStatusEnum.SKIPPED.name());
                        }
                    })
            );
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Finish current launch
     */
    @Override
    public void finishLaunch() {
        try {
            LaunchHandler launchHandler = LoadFactory.getLaunchHandler();
            launchHandler.finishLaunch(this.launchInfo.getLaunchId(), this.launchInfo.getLaunchStatus());
        } catch (Exception e) {
            System.err.println(e);
//            com.paytm.framework.reporting.Reporter.report.info("Exception occurred while finishLaunch", e);
        }
    }

    /**
     * Start test suite event handler
     *
     * @param suite TestNG's suite
     */
    @Override
    public void startTestSuite(ISuite suite) {
        try {
            StartTestItemRequest rq = new StartTestItemRequest();
            rq.setContext("name", suite.getName());
            rq.setContext("startTime", Calendar.getInstance().getTime());
            rq.setContext("type", "SUITE");
            rq.setContext("launchUuid", this.launchInfo.getLaunchId());
            String item = rq.execute()
                    .jsonPath()
                    .getString("id");
            suite.setAttribute(RP_ID, item);
            suite.setAttribute(RP_SUITE_ID, item);
            this.launchInfo.setItemUuid(item);
        } catch (Exception e) {
            System.err.println(e);
//            com.paytm.framework.reporting.Reporter.report.info("Exception occurred while startTestSuite", e);
        }
    }

    /**
     * Finish test suite event handler
     *
     * @param suite TestNG's suite
     */
    @Override
    public void finishTestSuite(ISuite suite) {
        try {
            String rpId = getAttribute(suite, RP_ID);
            if (null != rpId) {
                FinishTestItemRequest rq = new FinishTestItemRequest(rpId);
                rq.setContext("endTime", Calendar.getInstance().getTime());
                rq.setContext("status", getSuiteStatus(suite));
                rq.setContext("launchUuid", this.launchInfo.getLaunchId());
                rq.execute();
                suite.removeAttribute(RP_ID);
                suite.removeAttribute(RP_SUITE_ID);
                this.launchInfo.setItemUuid("");
            }
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while finishTestSuite", e);
        }
    }

    /**
     * Start test event handler
     *
     * @param testContext TestNG's test context
     */
    @Override
    public void startTest(ITestContext testContext) {
        try {
            if (hasMethodsToRun(testContext)) {
                StartChildTestItemRequest rq = new StartChildTestItemRequest(this.getAttribute(testContext.getSuite(), RP_ID));

                rq.setContext("name", testContext.getName());
                rq.setContext("startTime", testContext.getStartDate());
                rq.setContext("type", "TEST");
                rq.setContext("launchUuid", this.launchInfo.getLaunchId());
                if (!"true".equalsIgnoreCase(ReporterConfig.RP_RERUN))
                    rq.deleteContext("retry");

                String testID = rq.execute()
                        .jsonPath().getString("id");
                testContext.setAttribute(RP_ID, testID);
                this.launchInfo.setItemUuid(testID);
            } else {
                System.out.println("startTest_hasMethodsToRun_else_called + " + testContext.getCurrentXmlTest().getName());
            }
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while startTest", e);
        }
    }

    /**
     * Finish test event handler
     *
     * @param testContext TestNG's test context
     */
    @Override
    public void finishTest(ITestContext testContext) {
        try {
            if (hasMethodsToRun(testContext)) {
                FinishTestItemRequest rq = new FinishTestItemRequest(this.getAttribute(testContext, RP_ID));
                rq.setContext("endTime", testContext.getEndDate());
                String status = isTestPassed(testContext) ? ItemStatusEnum.PASSED.name() : ItemStatusEnum.FAILED.name();
                rq.setContext("status", status);
                rq.setContext("launchUuid", this.launchInfo.getLaunchId());
                rq.execute();
                // Cleanup
                Set<ITestResult> results = new HashSet<>();
                results.addAll(getTestResults(testContext.getFailedButWithinSuccessPercentageTests()));
                results.addAll(getTestResults(testContext.getFailedConfigurations()));
                results.addAll(getTestResults(testContext.getFailedTests()));
                results.addAll(getTestResults(testContext.getSkippedTests()));
                results.addAll(getTestResults(testContext.getSkippedConfigurations()));
                results.addAll(getTestResults(testContext.getPassedConfigurations()));
                results.addAll(getTestResults(testContext.getPassedTests()));
                results.stream().map(ITestResult::getInstance).filter(Objects::nonNull).collect(Collectors.toSet()).forEach(i -> {
                    RETRY_STATUS_TRACKER.remove(i);
                    RETRY_STATUS_TRACKER_ID.remove(i);
                    SKIPPED_STATUS_TRACKER.remove(i);
                    SKIPPED_STATUS_TRACKER_ID.remove(i);
                });
                results.stream().map(ITestResult::getMethod).filter(Objects::nonNull).collect(Collectors.toSet()).forEach(i -> {
                    RETRY_STATUS_TRACKER_V2.remove(i);
                    RETRY_STATUS_TRACKER_ID_V2.remove(i);
                });
                this.launchInfo.setItemUuid("");
            } else {
                System.out.println("finishTest_hasMethodsToRun_else_called + " + testContext.getCurrentXmlTest().getName());
            }
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while finishTest", e);
        }
    }

    /**
     * Start test method event handler
     *
     * @param testResult TestNG's test result
     */
    @Override
    public void startTestMethod(ITestResult testResult) {
        try {
            TestMethodTypeEnum methodType = ofNullable(TestMethodTypeEnum.getStepType(testResult.getMethod())).orElse(TestMethodTypeEnum.STEP);
            testResult.setAttribute(RP_METHOD_TYPE, methodType);
            String codeRef = testResult.getMethod().getQualifiedName();
            StartChildTestItemRequest rq = new StartChildTestItemRequest(getAttribute(testResult.getTestContext(), RP_ID));
            rq.setContext("name", createStepName(testResult));
            rq.setContext("codeRef", codeRef);
//        rq.setContext("testCaseId", ofNullable(getTestCaseId(codeRef, testResult)).map(TestCaseIdEntry::getId).orElse(null));
//        rq.setContext("attributes", createStepAttributes(testResult));  TODO: As @Attribute annotation will not be used by end user
            String desc = getTestDescription(testResult);
            rq.setContext("description", desc);
            rq.setContext("uniqueId", extractUniqueID(testResult));
            rq.setContext("startTime", new Date(testResult.getStartMillis()));
            rq.setContext("type", methodType.toString());
            if (!methodType.equals(TestMethodTypeEnum.STEP))
                rq.deleteContext("retry");
            boolean retry = isRetry(testResult);
            if (retry) {
                rq.setContext("retry", Boolean.TRUE);
            } else {
                rq.setContext("retry", Boolean.FALSE);
            }

            rq.setContext("launchUuid", this.launchInfo.getLaunchId());
            rq.setContext("attributes", getTestAttributes(testResult));
            rq.setContext("parameters", createDataProviderParameters(testResult));
            String stepMaybe = rq.execute().jsonPath().getString("id");

            testResult.setAttribute(RP_ID, stepMaybe);
            testResult.setAttribute(RP_TEST_DESC, desc);
            this.launchInfo.setItemUuid(stepMaybe);
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while startTestMethod", e);
        }
    }

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see Statuses
     * @deprecated
     */
    @Override
    @Deprecated
    public void finishTestMethod(String status, ITestResult testResult) {

    }

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see ItemStatusEnum
     */
    @Override
    public void finishTestMethod(ItemStatusEnum status, ITestResult testResult) {
        try {
            String itemId = getAttribute(testResult, RP_ID);

            if (ItemStatusEnum.SKIPPED == status) {
                if (!testResult.wasRetried() && null == itemId) {
                    startTestMethod(testResult);
                    itemId = getAttribute(testResult, RP_ID); // if we started new test method we need to get new item ID
                }
//            createSkippedSteps(testResult);
            }
            FinishTestItemRequest rq = new FinishTestItemRequest(itemId);
            rq.setContext("endTime", new Date(testResult.getEndMillis()));
            rq.setContext("status", status);

            TestMethodTypeEnum type = getAttribute(testResult, RP_METHOD_TYPE);
            Object instance = testResult.getInstance();

            // TestNG does not repeat before methods if an after method fails during retries. But reports them as skipped.
            // Mark before methods as not an issue if it is not a culprit.
            if (instance != null) {
                if (ItemStatusEnum.FAILED == status && TestMethodTypeEnum.BEFORE_METHOD == type) {
                    SKIPPED_STATUS_TRACKER.put(instance, Boolean.TRUE);
                }
                if (ItemStatusEnum.SKIPPED == status && (SKIPPED_STATUS_TRACKER.containsKey(instance) || (TestMethodTypeEnum.BEFORE_METHOD == type
                        && getAttribute(testResult, RP_RETRY) != null))) {
//                rq.setIssue(NOT_ISSUE); TODO: code for Issue
                }
            }

            processFinishRetryFlag(testResult, rq);
            String desc = null == getAttribute(testResult, RP_TEST_DESC) ? "" : getAttribute(testResult, RP_TEST_DESC);
            if (testResult.getThrowable() != null) {
                String failedLogs = "```error\n"
                        + Throwables.getStackTraceAsString(testResult.getThrowable())
                        + "\n```";
                desc = desc + System.lineSeparator() + failedLogs;
                rq.setContext("description", desc);
            }
            rq.setContext("launchUuid", itemId);
            rq.execute();
            this.launchInfo.setItemUuid("");
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while finishTestMethod", e);
        }
    }

    /**
     * Start configuration method(any before of after method)
     *
     * @param testResult TestNG's test result
     */
    @Override
    public void startConfiguration(ITestResult testResult) {
        try {
            TestMethodTypeEnum type = TestMethodTypeEnum.getStepType(testResult.getMethod());
            testResult.setAttribute(RP_METHOD_TYPE, type);
            String parentId = getConfigParent(testResult, type);
            BaseApi rq = null;
            if (null == parentId) {
                rq = new StartTestItemRequest();
            } else {
                rq = new StartChildTestItemRequest(parentId);
            }
            rq.setContext("name", testResult.getMethod().getMethodName());
            rq.setContext("codeRef", testResult.getMethod().getQualifiedName());
            rq.setContext("description", testResult.getMethod().getDescription());
            rq.setContext("startTime", new Date(testResult.getStartMillis()));
            rq.setContext("type", type == null ? null : type.toString());
            rq.setContext("launchUuid", this.launchInfo.getLaunchId());
            rq.deleteContext("retry");
            boolean retry = isRetry(testResult);
            if (retry) {
                rq.setContext("retry", Boolean.TRUE);
                testResult.setAttribute(RP_RETRY, Boolean.TRUE);
            }
            String itemID = rq.execute()
                    .jsonPath()
                    .getString("id");
            testResult.setAttribute(RP_ID, itemID);
            this.launchInfo.setItemUuid(itemID);
        } catch (Exception e) {
            System.err.println(e);
//            Reporter.report.info("Exception occurred while startConfiguration", e);
        }
    }

    @Override
    public void sendReportPortalMsg(ITestResult testResult) {
        try {
            LogItemRequest rq = new LogItemRequest();
            rq.setContext("itemUuid", LaunchInfo.getInstance().getItemUuid());
            rq.setContext("time", Calendar.getInstance().getTime());
            rq.setContext("level", "ERROR");
            if (testResult.getThrowable() != null)
                rq.setContext("message", getStackTraceAsString(testResult.getThrowable()));
            else
                rq.setContext("message", "Test has failed without exception");
            rq.execute();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // ***************** HELPER METHODS ********************************

    protected <T> T getAttribute(IAttributes attributes, String attribute) {
        return (T) attributes.getAttribute(attribute);
    }

    protected String getSuiteStatus(ISuite suite) {
        Collection<ISuiteResult> suiteResults = suite.getResults().values();
        ItemStatusEnum suiteStatus = ItemStatusEnum.PASSED;
        for (ISuiteResult suiteResult : suiteResults) {
            if (!(isTestPassed(suiteResult.getTestContext()))) {
                suiteStatus = ItemStatusEnum.FAILED;
                break;
            }
        }
        // if at least one suite failed launch should be failed
        isLaunchFailed.compareAndSet(false, suiteStatus == ItemStatusEnum.FAILED);
        return suiteStatus.name();
    }

    protected boolean isTestPassed(ITestContext testContext) {
        return testContext.getFailedTests().size() == 0 && testContext.getFailedConfigurations().size() == 0
                && testContext.getSkippedConfigurations().size() == 0 && (testContext.getSkippedTests().size() == 0
                || testContext.getSkippedTests()
                .getAllResults()
                .stream()
                .allMatch(e -> (boolean) ofNullable(getAttribute(e, RP_RETRY)).orElse(Boolean.FALSE)));
    }

    /**
     * Checks if test suite has any methods to run.
     * It can be useful with writing test with "groups".
     * So there could be created a test suite that has some methods but doesn't fit
     * the condition of a group. Such suite should be ignored for rp.
     *
     * @param testContext Test context
     * @return True if item has any tests to run
     */
    private boolean hasMethodsToRun(ITestContext testContext) {
        return null != testContext && null != testContext.getAllTestMethods() && 0 != testContext.getAllTestMethods().length;
    }

    /**
     * Extension point to customize test step name
     *
     * @param testResult TestNG's testResult context
     * @return Test/Step Name being sent to ReportPortal
     */
    protected String createStepName(ITestResult testResult) {
        String testClassName = testResult.getTestClass().getName();
        return testClassName + "." + testResult.getMethod().getMethodName();
    }

    protected Set<ItemAttributesRQ> createStepAttributes(ITestResult testResult) {
        Attributes attributesAnnotation = getMethodAnnotation(Attributes.class, testResult);
        if (attributesAnnotation != null) {
            return AttributeParser.retrieveAttributes(attributesAnnotation);
        }
        return null;
    }

    /**
     * Returns method annotation by specified annotation class from
     * TestNG Method or null if the method does not contain
     * such annotation.
     *
     * @param annotation Annotation class to find
     * @param testResult Where to find
     * @return {@link Annotation} or null if doesn't exists
     */
    private <T extends Annotation> T getMethodAnnotation(Class<T> annotation, ITestResult testResult) {
        Method method = getMethod(testResult);
        if (null != method) {
            return method.getAnnotation(annotation);
        }
        return null;
    }

    private <T extends Annotation> T getClassAnnotation(Class<T> annotation, ITestResult testResult) {
        Class className = getClass(testResult);
        if (null != className) {
            return (T) className.getAnnotation(annotation);
        }
        return null;
    }

    @Nullable
    private Class getClass(@Nonnull ITestResult testResult) {
        return ofNullable(testResult.getMethod()).map(ITestNGMethod::getRealClass)
                .orElse(null);
    }

    @Nullable
    private Method getMethod(@Nonnull ITestResult testResult) {
        return ofNullable(testResult.getMethod()).map(ITestNGMethod::getConstructorOrMethod)
                .map(ConstructorOrMethod::getMethod)
                .orElse(null);
    }

    private String getTestDescription(ITestResult testResult) {
        String description = testResult.getMethod().getDescription();
        if (testResult.getParameters().length != 0) {
            description = description + System.lineSeparator();
            description = description + Arrays.asList(testResult.getParameters()).toString();
        }
        return description;
    }

    /**
     * Returns test item ID from annotation if it provided.
     * <br><br>
     * If No UniqueId is provided by the user then
     * it will generate UUID for the newly executed tests and same UUID will be used for retried test cases.
     * <br><br>
     * In case completed launch retried then user UniqueId will be returned if available, else null will be returned.
     *
     * @param testResult Where to find
     * @return test item ID or null
     */
    private String extractUniqueID(ITestResult testResult) {
        UniqueID itemUniqueID = getMethodAnnotation(UniqueID.class, testResult);
        if (itemUniqueID == null)
            if (!"true".equalsIgnoreCase(ReporterConfig.RP_RERUN)) {
                boolean retry = isRetry(testResult);
                if (!retry) {
                    String test_uid = UUID.randomUUID().toString();
                    testResult.setAttribute(RP_TEST_UID, test_uid);
                    return test_uid;
                } else {
//                    Object instance = testResult.getInstance();
//                    String ext_test_uid = RETRY_STATUS_TRACKER_ID.get(instance);
                    ITestNGMethod iTestNGMethod = testResult.getMethod();
                    String ext_test_uid = RETRY_STATUS_TRACKER_ID_V2.get(iTestNGMethod);
                    testResult.setAttribute(RP_TEST_UID, ext_test_uid);
                    if (ext_test_uid != null && !ext_test_uid.isEmpty())
                        return ext_test_uid;
                }
            }
        return itemUniqueID != null ? itemUniqueID.value() : null;
    }

    private boolean isRetry(ITestResult testResult) {
        if (testResult.wasRetried()) {
            return true;
        }
//        Object instance = testResult.getInstance();
//        if (instance != null && RETRY_STATUS_TRACKER.containsKey(instance)) {
//            return true;
//        }

        ITestNGMethod iTestNGMethod = testResult.getMethod();
        if (iTestNGMethod != null && RETRY_STATUS_TRACKER_V2.containsKey(iTestNGMethod))
            return true;
        return IS_RETRY.test(Thread.currentThread().getStackTrace());
    }

    private List<Attribute> getTestAttributes(ITestResult testResult) {
        List<Attribute> l = new ArrayList<>();

        // lookup for dataprovider parameters as attributes
        try {
            Parameters testngParameters = this.getMethodAnnotation(Parameters.class, testResult);
            if (null != testngParameters) {
                String[] paramKeys = testngParameters.value();
                Object[] paramValue = testResult.getParameters();
                if (paramKeys.length == paramValue.length)
                    for (int i = 0; i < paramKeys.length; i++) {
                        String key = (null != paramKeys[i] && !"".equals(paramKeys[i])) ? paramKeys[i] : "EMPTY_KEY";
                        String value = (null != paramValue[i] && !"".equals(paramValue[i].toString())) ? paramValue[i].toString() : "EMPTY_VALUE";
                        l.add(new Attribute().withSystem(false).withKey(key).withValue(value));
                    }
            } else {
                Parameter[] parameters = testResult.getMethod().getConstructorOrMethod().getMethod().getParameters();
                Object[] paramValue = testResult.getParameters();
                if (parameters.length == paramValue.length) {
                    for (int i = 0; i < parameters.length; i++) {
                        String key = (null != parameters[i] && !"".equals(parameters[i].getName())) ? parameters[i].getName() : "EMPTY_KEY";
                        String value = (null != paramValue[i] && !"".equals(paramValue[i].toString())) ? paramValue[i].toString() : "EMPTY_VALUE";
                        l.add(new Attribute().withSystem(false).withKey(key).withValue(value));
                    }
                }
            }

        } catch (Exception e) {
        }

        // lookup for custom owner Annotation
        try {
            Owner classOwnerAnnotation = getClassAnnotation(Owner.class, testResult);
            Owner methodOwnerAnnotation = getMethodAnnotation(Owner.class, testResult);
            getCustomOwnerList(l, classOwnerAnnotation);
            getCustomOwnerList(l, methodOwnerAnnotation);
        } catch (Exception e) {
        }

        // lookup for allure owner Annotation
        try {
            io.qameta.allure.Owner classOwnerAnnotation = getClassAnnotation(io.qameta.allure.Owner.class, testResult);
            io.qameta.allure.Owner methodOwnerAnnotation = getMethodAnnotation(io.qameta.allure.Owner.class, testResult);
            getAllureOwnerList(l, classOwnerAnnotation);
            getAllureOwnerList(l, methodOwnerAnnotation);
        } catch (Exception e) {
        }

        //lookup for factoryAttributes
        try {
            getFactoryAttributes(l, testResult);
        } catch (Exception e) {
        }
        return l;
    }

    private void getFactoryAttributes(List<Attribute> l, ITestResult iTestResult) throws IllegalAccessException {
        IParameterInfo parameterInfo = iTestResult.getMethod().getFactoryMethodParamsInfo();
        Object instance = parameterInfo.getInstance();
        int instanceId = instance.hashCode();

        Field[] fields = instance.getClass().getDeclaredFields();
        if (fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Object fieldObj = field.get(instance);
                Attribute attribute = new Attribute();
                switch (getObjectType(fieldObj)) {
                    case "UNKNOWN":
                        attribute.withValue(String.valueOf(instanceId));
                        break;
                    default:
                        attribute.withValue(String.valueOf(fieldObj));
                        break;
                }
                attribute.withSystem(false).withKey(name);

                l.add(attribute);
            }
        } else {
            Attribute attribute = new Attribute()
                    .withKey("arg0")
                    .withSystem(false)
                    .withValue(String.valueOf(instanceId));
            l.add(attribute);
            return;
        }
    }

    public String getObjectType(Object obj) {
        if (null == obj)
            return "UNKNOWN";
        if (obj instanceof String)
            return "String";
        if (obj instanceof Integer)
            return "Integer";
        if (obj instanceof Long)
            return "Long";
        if (obj instanceof Double)
            return "Double";
        if (obj instanceof Float)
            return "Float";
        if (obj instanceof Boolean)
            return "Boolean";
        if (obj instanceof Character)
            return "Character";
        if (obj instanceof Short)
            return "Short";
        return "UNKNOWN";
    }

    private void getAllureOwnerList(List<Attribute> l, io.qameta.allure.Owner owner) {
        String ownerKeyName = TestAttributeKeys.owner.name();
        if (null != owner) {
            Optional<Attribute> attributeOptional = l.stream().filter(item ->
                    item.getKey().equalsIgnoreCase(ownerKeyName) && item.getValue().equalsIgnoreCase(owner.value())
            ).findFirst();
            if (attributeOptional.isPresent())
                return;
            l.add(new Attribute().withKey(ownerKeyName).withValue(owner.value()));
        }
    }

    private void getCustomOwnerList(List<Attribute> l, Owner owner) {
        String ownerKeyName = TestAttributeKeys.owner.name();
        if (null != owner) {
            for (String name : owner.value()) {
                Optional<Attribute> attributeOptional = l.stream().filter(item ->
                        item.getKey().equalsIgnoreCase(ownerKeyName) && item.getValue().equalsIgnoreCase(name)
                ).findFirst();
                if (attributeOptional.isPresent())
                    continue;
                l.add(new Attribute().withKey(ownerKeyName).withValue(name));
            }
        }
    }

    // Not in use, as it is creating issue with history in factory approach
    private List<TestParameter> createFactoryParameters(ITestResult testResult) throws IllegalAccessException {
        List<TestParameter> testParameters = new ArrayList<>();
        IParameterInfo parameterInfo = testResult.getMethod().getFactoryMethodParamsInfo();
        Object instance = parameterInfo.getInstance();
        int instanceId = instance.hashCode();

        Field[] fields = instance.getClass().getDeclaredFields();
        if (fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Object fieldObj = field.get(instance);
                TestParameter parameter = new TestParameter();
                switch (getObjectType(fieldObj)) {
                    case "UNKNOWN":
                        parameter.withValue(String.valueOf(instanceId));
                        break;
                    default:
                        parameter.withValue(String.valueOf(fieldObj));
                        break;
                }
                parameter.withKey(name);

                testParameters.add(parameter);
            }
        } else {
            TestParameter parameter = new TestParameter()
                    .withKey("arg0")
                    .withValue(String.valueOf(instanceId));
            testParameters.add(parameter);
        }
        return testParameters;
    }

    private List<TestParameter> createDataProviderParameters(ITestResult testResult) {
        List<TestParameter> l = new ArrayList<>();
        try {
            Parameters testngParameters = this.getMethodAnnotation(Parameters.class, testResult);
            if (null != testngParameters) {
                String[] paramKeys = testngParameters.value();
                Object[] paramValue = testResult.getParameters();
                if (paramKeys.length == paramValue.length)
                    for (int i = 0; i < paramKeys.length; i++) {
                        l.add(new TestParameter().withValue(paramValue[i].toString()).withKey(paramKeys[i]));
                    }
            } else {
                Parameter[] parameters = testResult.getMethod().getConstructorOrMethod().getMethod().getParameters();
                Object[] paramValue = testResult.getParameters();
                if (parameters.length == paramValue.length) {
                    for (int i = 0; i < parameters.length; i++) {
                        l.add(new TestParameter().withValue(paramValue[i].toString()).withKey(parameters[i].getName()));
                    }
                }
            }

        } catch (Exception e) {
        }
//        try {
//            l.addAll(createFactoryParameters(testResult));
//        } catch (Exception e) {
//        }
        return l;
    }

    private void processFinishRetryFlag(ITestResult testResult, FinishTestItemRequest rq) {
        Object instance = testResult.getInstance();
        ITestNGMethod iTestNGMethod = testResult.getMethod();
//        if (instance != null && !ItemStatusEnum.SKIPPED.equals(rq.getContext().get("status"))) {
//            // Remove retry flag if an item passed
//            RETRY_STATUS_TRACKER.remove(instance);
//        }
        if (iTestNGMethod != null && !ItemStatusEnum.SKIPPED.equals(rq.getContext().get("status"))) {
            // Remove retry flag if an item passed
            RETRY_STATUS_TRACKER_V2.remove(iTestNGMethod);
        }

        TestMethodTypeEnum type = getAttribute(testResult, RP_METHOD_TYPE);

        boolean isRetried = testResult.wasRetried();
        if (TestMethodTypeEnum.STEP == type && getAttribute(testResult, RP_RETRY) == null && isRetried) {
//            RETRY_STATUS_TRACKER.put(instance, Boolean.TRUE);
            RETRY_STATUS_TRACKER_V2.put(iTestNGMethod, Boolean.TRUE);
            String test_uid = getAttribute(testResult, RP_TEST_UID);
            rq.setContext("retry", Boolean.TRUE);
            if (null != test_uid) {
                RETRY_STATUS_TRACKER_ID.put(instance, test_uid);
                RETRY_STATUS_TRACKER_ID_V2.put(iTestNGMethod, test_uid);
            }
//            rq.setIssue(NOT_ISSUE); TODO: code for Issue
        }
        if (isRetried) {
            testResult.setAttribute(RP_RETRY, Boolean.TRUE);
        }

        // Save before method finish requests to update them with a retry flag in case of main test method failed
        if (instance != null) {
            if (TestMethodTypeEnum.BEFORE_METHOD == type && getAttribute(testResult, RP_RETRY) == null) {
                String itemId = getAttribute(testResult, RP_ID);
                BEFORE_METHOD_TRACKER.computeIfAbsent(instance, i -> new ConcurrentLinkedQueue<>()).add(Pair.of(itemId, rq));
            } else {
                Queue<Pair<String, FinishTestItemRequest>> beforeFinish = BEFORE_METHOD_TRACKER.remove(instance);
                if (beforeFinish != null && isRetried) {
                    beforeFinish.stream().filter(e -> e.getValue().getContext().get("retry") == null || !Boolean.getBoolean(e.getValue().getContext().get("retry").toString())).forEach(e -> {
                        FinishTestItemRequest f = e.getValue();
                        f.setContext("retry", true);
                        f.setContext("launchUuid", e.getKey());
                        f.execute();
                    });
                }
            }
        }
    }

    String getConfigParent(ITestResult testResult, TestMethodTypeEnum type) {
        String parentId;
        if (TestMethodTypeEnum.BEFORE_SUITE.equals(type) || TestMethodTypeEnum.AFTER_SUITE.equals(type)) {
            parentId = getAttribute(testResult.getTestContext().getSuite(), RP_ID);
        } else {
            parentId = getAttribute(testResult.getTestContext(), RP_ID);
        }
        return parentId;
    }
}
