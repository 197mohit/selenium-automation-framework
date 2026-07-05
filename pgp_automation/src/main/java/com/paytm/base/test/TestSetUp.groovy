package com.paytm.base.test

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter
import com.paytm.framework.utils.resourceManager.ResourcePool
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.MUser
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.user.annotations.AUsers
import groovy.json.JsonSlurper
import io.restassured.builder.RequestSpecBuilder
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite

import java.lang.reflect.Method
import java.util.function.Predicate

import static io.restassured.RestAssured.given
import static com.paytm.LocalConfig.QR_HOST

class TestSetUp extends PGPBaseTest {

    private static final ThreadLocal<List<MUser>> MUSERS = ThreadLocal.withInitial({ [] })
    private static final ThreadLocal<List<com.paytm.utils.merchant.user.User>> USERS = ThreadLocal.withInitial({ [] })
    protected static final ThreadLocal<List<Merchant>> MERCHANTS = ThreadLocal.withInitial({ [] })
    static ResourcePool<MUser> usersDataPool
    private static ResourcePool<Merchant> merchantsList
    final def reqSpec = {
        given(
                new RequestSpecBuilder()
                        .addFilter(new RequestResponseLoggingFilter())
                        .setConfig(new CurlLoggingRestAssuredConfigBuilder().build())
                        .build()
        )
    }

    com.paytm.utils.merchant.user.User user() {
        user(0)
    }

    com.paytm.utils.merchant.user.User user(int idx) {
        def list = USERS.get()
        list.size() > idx ? list[idx] : null
    }

    Merchant m() {
        m(0)
    }

    Merchant m(int idx) {
        def list = MERCHANTS.get()
        list.size() > idx ? list[idx] : null
    }

    @BeforeSuite
    void setUsers() {
        def users = new JsonSlurper().parse(ClassLoader.systemClassLoader.getResource('users.json'))
        usersDataPool = new ResourcePool<>(users.collect {
            it as MUser
        }, new Wait({ n -> n > 0 ? 5 : 0 }, 25, 10**3))
    }

    @BeforeSuite
    void setMerchants() {
        def merchants = new JsonSlurper().parse(ClassLoader.systemClassLoader.getResource('merchants.json')).unique()
        TestSetUp.merchantsList = new ResourcePool<>(merchants.collect {
            new Merchant(it[0], it[1], false)
        }, new Wait({ n -> n > 0 ? 5 : 0 }, 25, 10**3))
    }

    @BeforeMethod
    void setUserPool(Method m, ITestResult testResult) {
        try {
            def annotations = m.declaredAnnotations.findAll { it instanceof AUser || it instanceof AUsers }
            if (annotations.empty) return
            if (annotations.size() > 1) throw new SkipException("Method cannot have both @AUser and @AUsers annotations")
            List<AUser> userAnnotations = annotations[0] instanceof AUser ? annotations : annotations[0].value()
            if (userAnnotations.empty) throw new SkipException("@Users cannot be empty")
            userAnnotations.collectEntries {
                [({ AUser aUser, MUser mUser -> mUser.matches(aUser) }.curry(it)), it]
            }
                    .collectEntries { Predicate<MUser> predicate, AUser annotation  ->
                        MUser mUser = usersDataPool.find(annotation.edit(), predicate)
                        com.paytm.utils.merchant.user.User user = new com.paytm.utils.merchant.user.User(mUser.mobile, mUser.password, annotation.edit())
                        [(mUser): user]
                    }
                    .each {
                        MUSERS.get().add(it.key)
                        USERS.get().add(it.value)
                    }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(m.getName(), e));
        }
    }

    @BeforeMethod
    void setMerchant(Method m, ITestResult testResult) {
        try {
            m.declaredAnnotations.findAll {
                it instanceof com.paytm.utils.merchant.merchant.util.annotations.Merchant || it instanceof Merchants
            }.with {
                it.empty ? null : it
            }?.tap {
                if (it.size() > 1) throw new SkipException("Method cannot have both @Merchant and @Merchants annotations")
            }?.with {
                it[0] instanceof com.paytm.utils.merchant.merchant.util.annotations.Merchant ? it : it[0].value()
            }?.tap {
                if ((it as List).empty) throw new SkipException("@Merchants cannot be empty")
            }?.collect { ma ->
                if (ma.value()) {
                    def predicate = ma.value().newInstance(null, null)
                    return TestSetUp.merchantsList.find(ma.edit(), predicate)?.tap { it.editable = ma.edit() }
                } else return null
            }?.each {
                MERCHANTS.get().add(it)
            }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(m.getName(), e));
        }
    }

    @AfterMethod(alwaysRun = true)
    void releaseUser() {
        usersDataPool.addAll(MUSERS.get())
        USERS.set([])
        MUSERS.set([])
    }

    @AfterMethod(alwaysRun = true)
    void releaseMerchant() {
        merchantsList.addAll(MERCHANTS.get())
        MERCHANTS.set([])
    }

}
