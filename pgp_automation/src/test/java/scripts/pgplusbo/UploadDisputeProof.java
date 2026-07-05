/**
 * @desc This class is used to run test cases for sharing proof for disputes
 */


package scripts.pgplusbo;

import com.paytm.ServerConfigProvider;
import com.paytm.api.PgPlusBO.CreateDispute;
import com.paytm.api.PgPlusBO.UploadProof;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.ResponseSpecification;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Date;

public class UploadDisputeProof extends PGPBaseTest {

    /**
     * @desc This variable is used to store SSO token
     */
    private ThreadLocal<String> ssoToken = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store externalSerialNo
     */
    private ThreadLocal<String> externalSerialNo = new ThreadLocal<String>();


    /**
     * @desc This variable is used to store referenceNo
     */
    private ThreadLocal<String> referenceNo = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store orderID
     */
    private ThreadLocal<String> orderId = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store nid
     */
    private ThreadLocal<String> mid = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store requestTimestamp
     */
    private ThreadLocal<String> requestTimestamp = new ThreadLocal<String>();

    /**
     * @desc This variable is used to store Transaction ID
     */
    private ThreadLocal<String> acquirementId = new ThreadLocal<String>();


    /**
     * @throws Exception
     * @desc This function is used to generate SSO Token
     */
    private void generateSsoToken() throws Exception {
        User user1 = userManager.getForRead(PGPBaseTest.Label.BASIC);
        ssoToken.set(user1.ssoToken());
    }

    /**
     * @param amount
     * @param merchantType
     * @param requestType
     * @param theme
     * @throws Exception
     * @desc THis function is used to perform enhance transaction
     */
    private void ehanceTransaction(String amount, Constants.MerchantType merchantType, String requestType, String theme) throws Exception {
        generateSsoToken();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setTXN_AMOUNT(amount)
                .setSSO_TOKEN(ssoToken.get())
                .setREQUEST_TYPE(requestType)
                .build();
        orderId.set(orderDTO.getORDER_ID());
        mid.set(orderDTO.getMID());
        String getAccquirementIdCommand = "grep " + orderId.get() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER' | grep 'RESPONSE'";
        System.out.println("getAccquirementIdCommand = " + getAccquirementIdCommand);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String getAccquirementId = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, getAccquirementIdCommand), s -> !"".equals(s));
        System.out.println("Facade logs = " + getAccquirementId);
        JsonPath jsonPath = new JsonPath(getAccquirementId);
        acquirementId.set(jsonPath.getString("RESPONSE.response.body.acquirementId"));
        cashierPage.payBy(Constants.PayMode.CC);
        getUploadProofData();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(amount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        referenceNo.set(txnStatus.txnStatusResponse.BANKTXNID);
        System.out.println("Reference number = " + referenceNo.get());
    }

    /**
     * @desc This function is used to get externalSerialNo
     */
    private void getUploadProofData() throws Exception {
        String cmdTheiaFacadeLogs = "grep " + acquirementId.get() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_INQUIRE_WITH_ACQ_ID' | grep 'RESPONSE'";
        System.out.println("cmdTheiaFacadeLogs = " + cmdTheiaFacadeLogs);
        String theiaFacadeLogs = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdTheiaFacadeLogs), s -> !"".equals(s));
        System.out.println("Facade logs = " + theiaFacadeLogs);
        JsonPath jsonPath = new JsonPath(theiaFacadeLogs);
        String extendedInfo = jsonPath.getString("RESPONSE.response.body.paymentViews[0].payOptionInfos[0].extendInfo");
        System.out.println("extendedInfo = " + extendedInfo);
        JsonPath extendedInfoJsonPath = new JsonPath(extendedInfo);
        externalSerialNo.set(extendedInfoJsonPath.get("externalSerialNo"));
        System.out.println("Exterial Serial Number = " + externalSerialNo.get());
    }

    /**
     * @desc This function is used to get current timestamp
     */
    private void getCurrentTimeStamp() {
        requestTimestamp.set(String.valueOf(System.currentTimeMillis()));
        System.out.println("Current time stamp = " + requestTimestamp.get());
    }

    /**
     * @param expectedResultCodeId
     * @param expectedResultCode
     * @desc This function is used to apply assertion
     */
    private ResponseSpecification applyAssertions(String expectedResultCodeId, String expectedResultCode) {
        return new ResponseSpecBuilder()
                .expectBody("body.resultInfo.resultCodeId", Matchers.equalTo(expectedResultCodeId))
                .expectBody("body.resultInfo.resultCode", Matchers.equalTo(expectedResultCode))
                .build();
    }

    @Test(description = "Upload proof in Png file format")
    public void uploadProofDataPngFileformat() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("1001", "UPLOADED_SUCCESS"));
    }

    @Test(description = "Upload proof in pdf file format")
    public void uploadProofDataPdfFileformat() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.pdf", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("1001", "UPLOADED_SUCCESS"));
    }

    @Test(description = "Verify POD_ALREADY_UPLOADED status")
    public void documentAlreadyUploaded() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly, disputeId,
                requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND");
        uploadProof.addMultipartData("src/test/resources/uploadProof/sample.pdf", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("1001", "UPLOADED_SUCCESS"));
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4006", "POD_ALREADY_UPLOADED"));
    }

    @Test(description = "Upload proof in Zip format")
    public void uploadProofDataZipFileformat() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(),
                "ORDER_IN_TRANSIT", "DEFEND");
        uploadProof.addMultipartData("src/test/resources/uploadProof/sample.zip", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("1001", "UPLOADED_SUCCESS"));
    }

    @Test(description = "Verify FILE_NOT_SUPPORTED status")
    public void uploadProofDataInvalidFileformat() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(),
                "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/demo.jpeg", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4009", "FILE_NOT_SUPPORTED"));
    }

    @Test(description = "Verify INVALID_SIGNATURE status")
    public void uploadDocumentInvalidSignature() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(),
                "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 1);
        uploadProof.getRequestSpecBuilder().addMultiPart("signature", "5678998765434567");
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4001", "INVALID_SIGNATURE"));
    }

    @Test(description = "Verify MANDATORY_PARAM_MISSING status")
    public void uploadDocumentMandetoryParamMissing() throws Exception {
        getCurrentTimeStamp();
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                null, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4002", "MANDATORY_PARAM_MISSING"));
    }

    @Test(description = "Verify DISPUTE_ID_NOT_FOUND status")
    public void uploadDocumentInvalidDisputeId() throws Exception {
        getCurrentTimeStamp();
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                "2345654345654567", requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.pdf", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4004", "DISPUTE_ID_NOT_FOUND"));
    }


    @Test(description = "Verify MAX_FILES_EXCEEDED status")
    public void uploadProofMaxFilesExceed() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 7);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4007", "MAX_FILES_EXCEEDED"));
    }


    @Test(description = "Verify SYSTEM_ERROR status")
    public void uploadDocumentSystemError() throws Exception {
        getCurrentTimeStamp();
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 1);
        uploadProof.getRequestSpecBuilder().addMultiPart("files", "src/test/resources/uploadProof/sample.png");
        uploadProof.execute()
                .then()
                .spec(applyAssertions("5001", "SYSTEM_ERROR"));
    }

    @Test(description = "Verify REQUEST_PARAMS_INVALID status")
    public void uploadDocumentRequestedParamsInvalid() throws Exception {
        requestTimestamp.set("2021-06-13 00:23:01.375");
        ehanceTransaction("100", Constants.MerchantType.PGOnly, "DEFAULT", "enhancedweb");
        CreateDispute createDispute = new CreateDispute(externalSerialNo.get(), referenceNo.get());
        String disputeId = createDispute.execute()
                .then()
                .body("result.disputeId", Matchers.notNullValue())
                .extract().jsonPath().getString("result.disputeId");
        UploadProof uploadProof = new UploadProof(Constants.MerchantType.PGOnly,
                disputeId, requestTimestamp.get(), "ORDER_IN_TRANSIT", "DEFEND")
                .addMultipartData("src/test/resources/uploadProof/sample.png", 1);
        uploadProof.execute()
                .then()
                .spec(applyAssertions("4003", "REQUEST_PARAMS_INVALID"));
    }
}
