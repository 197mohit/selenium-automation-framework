package scripts;

import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Step;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Created by nikunjkumar on 28/12/17.
 */
public class Dummy extends PGPBaseTest {

    @Step
    void sampleStep() {

    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test()
    public void test2() {
        sampleStep();
        System.out.println("FAIL");
        throw new RuntimeException("FAIL");
    }

    @Test
    public void test3() {
        sampleStep();
        System.out.println("SKIPPED");
        throw new SkipException("bye-bye");
    }

    @Test()
    public void test4() {
        System.out.println("FAIL");
        sampleStep();
        throw new RuntimeException("FAIL");
    }

    @Test()
    public void test5() {
        sampleStep();
        System.out.println("FAIL");
        throw new RuntimeException("FAIL");
    }

    @Test(description = "this is test description")
    public void test6() {
        sampleStep();
        System.out.println("PASS");
    }

    @Test
    public void test7() {
        sampleStep();
        throw new AssertionError("test case issue");
    }

    @Test()
    public void test8() {
        sampleStep();
    }
}
