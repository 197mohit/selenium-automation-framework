package scripts.RetryPayModes;

import com.paytm.RetryPaymode.RetryPaymentMode;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import io.qameta.allure.Owner;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Owner("Gagandeep")
public class TestRetryPayModes extends PGPBaseTest implements ITest {


    private static final String KEY = "theme";
    public String theme = null;
    private RetryPaymentMode retryPaymentMode = new RetryPaymentMode();
    private ThreadLocal<String> testName = new ThreadLocal<>();


    @Override
    public String getTestName() {
        return testName.get();
    }

    @BeforeMethod
    public void beforeClass(ITestContext context, Method method,ITestResult testResult, Object[] testData) {
        try {
            if (testData.length > 0) {
                testName.set(method.getName() + "_" + "FirstPayMode-" + testData[0] + " SecondPayMode-" + testData[1]);
                context.setAttribute("testName", testName.get());
            } else
                context.setAttribute("testName", method.getName());

            theme = context.getCurrentXmlTest().getParameter(KEY);
            theme = theme == null ? "enhancedweb_revamp" : theme;
            System.err.println("theme = " + theme);
        } catch (Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }


    @Test(dataProvider = "MatrixOfPayModes", description = "validate Retry Payment modes")
    public void validatePaymentRetry(String firstPaymenthod, String secondPayMethod) throws Exception {

        System.out.println("FirstPayMode-" + firstPaymenthod);
        System.out.println("SecondPayMode-" + secondPayMethod);
        OrderDTO orderDTO = retryPaymentMode.FirstPayMode(firstPaymenthod, theme);
        retryPaymentMode.RetryPayMode(secondPayMethod, theme, orderDTO, firstPaymenthod);
        System.out.println();
    }

    @DataProvider(name = "MatrixOfPayModes", parallel = true)
    public Object[][] getDataFromDataprovider() {


      //  String FirstMode[] = {"CC", "DC", "NB", "UPI", "PPBL", "EMI", "EMI_DC", "SAVEDCARD", "EMI_SAVEDCARD", "ADDNPAY", "HYBRID", "DIRECT_BANK", "ZESTMONEY"};
      //  String RetryMode[] = {"CC", "DC", "WALLET", "NB", "PPBL", "UPI", "EMI", "EMI_DC", "COD", "SAVEDCARD", "POSTPAID", "EMI_SAVEDCARD","ZESTMONEY"};
        String FirstMode[] = {"CC", "DC", "NB", "UPI", "PPBL", "SAVEDCARD", "ADDNPAY" };
        String RetryMode[] = {"CC", "DC", "WALLET", "NB", "PPBL", "UPI","SAVEDCARD", "POSTPAID"};

        /*String FirstMode[] = {"ADDNPAY"};
        String RetryMode[] = {"EMI_SAVEDCARD"};*/
        List<List<String>> allPossibleConnection = new ArrayList<>();
       // List<List<String>> impossibleRetryConnection = Arrays.asList(Arrays.asList("ADDNPAY", "WALLET"), Arrays.asList("HYBRID", "WALLET"),Arrays.asList("ADDNPAY", "UPI"),Arrays.asList("ADDNPAY", "POSTPAID"),
       //         Arrays.asList("ADDNPAY", "ZESTMONEY"),Arrays.asList("ADDNPAY", "EMI_DC"), Arrays.asList("ADDNPAY", "EMI"), Arrays.asList("ADDNPAY", "EMI_SAVEDCARD"), Arrays.asList("ADDNPAY", "COD"), Arrays.asList("HYBRID", "COD"));
        List<List<String>> impossibleRetryConnection = Arrays.asList(Arrays.asList("ADDNPAY", "WALLET"),Arrays.asList("ADDNPAY", "UPI"),Arrays.asList("ADDNPAY", "POSTPAID") );

        for (String s : FirstMode) {
            for (String s1 : RetryMode) {
                allPossibleConnection.add(Arrays.asList(s, s1));
            }
        }
        allPossibleConnection.removeAll(impossibleRetryConnection);
        String[][] arr = new String[allPossibleConnection.size()][2];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < 2; j++) {
                arr[i][j] = allPossibleConnection.get(i).get(j);
            }
        }
        return arr;
    }

    @DataProvider(name = "MatrixOfDebugPayModes", parallel = false)
    public Object[][] getDebugDataFromDataprovider() {
        String FirstMode[] = {"SAVEDCARD"};
        String RetryMode[] = {"UPI"};
        /*String FirstMode[] = {"ADDNPAY"};
        String RetryMode[] = {"EMI_SAVEDCARD"};*/
        List<List<String>> allPossibleConnection = new ArrayList<>();
        List<List<String>> impossibleRetryConnection = Arrays.asList(Arrays.asList("ADDNPAY", "WALLET"), Arrays.asList("HYBRID", "WALLET"),Arrays.asList("ADDNPAY", "UPI"),Arrays.asList("ADDNPAY", "POSTPAID"),
                Arrays.asList("ADDNPAY", "ZESTMONEY"),Arrays.asList("ADDNPAY", "EMI_DC"), Arrays.asList("ADDNPAY", "EMI"), Arrays.asList("ADDNPAY", "EMI_SAVEDCARD"), Arrays.asList("ADDNPAY", "COD"), Arrays.asList("HYBRID", "COD"));
        for (String s : FirstMode) {
            for (String s1 : RetryMode) {
                allPossibleConnection.add(Arrays.asList(s, s1));
            }
        }
        allPossibleConnection.removeAll(impossibleRetryConnection);
        String[][] arr = new String[allPossibleConnection.size()][2];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < 2; j++) {
                arr[i][j] = allPossibleConnection.get(i).get(j);
            }
        }
        return arr;
    }
}
