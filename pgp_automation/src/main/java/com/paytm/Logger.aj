package com.paytm;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.service.ExtentTestManager;
import com.paytm.base.test.User;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.TestStep;
import com.paytm.framework.reporting.Utility;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

public aspect Logger {

    pointcut testStepCut(): execution(@com.paytm.framework.reporting.TestStep * *.*(..));

    pointcut utilityCut(): execution(@com.paytm.framework.reporting.Utility * *.*(..));

    pointcut assertionCut(): execution(@com.paytm.framework.reporting.Assertion * *.*(..));

    pointcut testCaseCut(): execution(@org.testng.annotations.Test * *.*(..));

    pointcut orderCreated(): execution(* com.paytm.dto.OrderDTO.Builder.build());

    pointcut fetchUserForRead(): execution(* com.paytm.framework.utils.resourcePool.ResourceManager.getForRead(..));

    pointcut fetchUserForWrite(): execution(* com.paytm.framework.utils.resourcePool.ResourceManager.getForWrite(..));

    after() returning(com.paytm.dto.OrderDTO orderDTO): orderCreated() {
        ExtentTestManager.getTest().info(MarkupHelper.createCodeBlock(orderDTO.toString(), CodeLanguage.JSON));
        ExtentTestManager.getTest().info(MarkupHelper.createCodeBlock(orderDTO.asQuery()));
    }

    before(): testStepCut() {
        Annotation testStepAnnotation = ((MethodSignature) thisJoinPoint.getSignature()).getMethod().getAnnotation(TestStep.class);
        String testStepDescription = ((TestStep) testStepAnnotation).value();
        if (!testStepDescription.isEmpty()) {
            ExtentTestManager.getTest().pass(testStepDescription);
        }
    }

    before(): testCaseCut() {
        logOwners(thisJoinPoint);
    }

    after() returning(Object user): fetchUserForRead() || fetchUserForWrite() {
        ExtentTestManager.getTest().info("User fetched: " + ((User) user).mobNo());
    }

    before(): utilityCut() {
        Utility utility = ((MethodSignature) thisJoinPoint.getSignature()).getMethod().getAnnotation(Utility.class);
        Object[] args = thisJoinPoint.getArgs();
        String utilityDescription = "";
        for (int i = 0; i < args.length; i++) {
            String regex = "\\{" + i + "\\}";
            utilityDescription = utility.value()
                    .replaceAll(regex, args[i].toString());
        }
        if (utilityDescription.isEmpty()) {
            ExtentTestManager.getTest().info("Executing method: " + thisJoinPoint.getSignature().getName());
        } else {
            ExtentTestManager.getTest().info(utilityDescription);
        }
    }

    private static void logOwners(JoinPoint joinPoint) {
        Owners owners = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Owners.class);
        ExtentTest test = ExtentTestManager.getTest();
        if (owners == null) {
            test.warning("Feature Owners description not provided in Test Case");
        } else {
            if (!owners.author().isEmpty()) test.assignAuthor(owners.author());
            ExtentTest node = test.createNode("Feature Owners");
            if (owners.qa().isEmpty()) {
                node.warning("Functional Tester info not provided");
            } else {
                node.info("Functional Tester: " + owners.qa());
            }
            if (owners.author().isEmpty()) {
                node.warning("Automation Tester info not provided");
            } else {
                node.info("Automation Tester: " + owners.author());
            }
            if (owners.dev().isEmpty()) {
                node.warning("Developer info not provided");
            } else {
                node.info("Developer        : " + owners.dev());
            }
            if (owners.prod().isEmpty()) {
                node.warning("Product Manager info not provided");
            } else {
                node.info("Product Manager  : " + owners.prod());
            }
        }
    }

}
