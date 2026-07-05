package scripts;

import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EDCQR extends PGPBaseTest {


    Constants.MerchantType edcQR = Constants.MerchantType.EDCQR;

    //Only DC is coming in cashPos Product Code, Surbhi to confirm
    @Test(description = "To test migration is successful with EDC fee with 51051000100000000048 productCode for EDC merchant for DC PayMode")
    public void cashPosDC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "DEBIT_CARD")
                .getList("CONTRACT-DETAIL-2020092951051010016800213474886.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains(
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");

    }

    @Test(description = "To test migration is successful with EDC fee with 51051000100000000001 Default productCode for EDC merchant for DC PayMode")
    public void defaultEDCDC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "DEBIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212666880.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains(
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");

    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000001 Default productCode for EDC merchant for EMI PayMode")
    public void defaultEDCEMI() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "EMI")
                .getList("CONTRACT-DETAIL-2020092851051010016800212666880.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).containsExactly("{\"EDC\":\"TRUE\"}");

    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000001 Default productCode for EDC merchant for CC PayMode")
    public void defaultEDCCC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "CREDIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212666880.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");

    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000014 EDCPosAcquiring productCode for EDC merchant for CC PayMode")
    public void edcPOSEDCCC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "CREDIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212667888.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");

    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000014 EDCPosAcquiring productCode for EDC merchant for DC PayMode")
    public void edcPOSEDCDC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "DEBIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212667888.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");

    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000014 EDCPosAcquiring productCode for EDC merchant for EMI PayMode")
    public void edcPOSEDCEMI() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "EMI")
                .getList("CONTRACT-DETAIL-2020092851051010016800212667888.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).containsExactly("{\"EDC\":\"TRUE\"}");
    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000047 productCode for EDC merchant for CC PayMode")
    public void edc47CC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "CREDIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212668886.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");
    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000047 productCode for EDC merchant for DC PayMode")
    public void edc47DC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "DEBIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212668886.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");
    }

    @Test(description = "To test migration is successful with EDC fee with 51051000100000000047 productCode for EDC merchant for EMI PayMode")
    public void edc47EMI() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "EMI")
                .getList("CONTRACT-DETAIL-2020092851051010016800212668886.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).containsExactly("{\"EDC\":\"TRUE\"}");

    }



    @Test(description = "To test migration is successful with EDC fee with 51051000100000000046 productCode for EDC merchant for CC PayMode")
    public void edc46CC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "CREDIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212665882.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");
    }


    @Test(description = "To test migration is successful with EDC fee with 51051000100000000046 productCode for EDC merchant for DC PayMode")
    public void edc46DC() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "DEBIT_CARD")
                .getList("CONTRACT-DETAIL-2020092851051010016800212665882.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).contains
                ("{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"RUPAY\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MASTER\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"VISA\"}",
                        "{\"EDC\":\"TRUE\",\"FEE_DECISION_CARD_SCHEME\":\"MAESTRO\"}");
    }



    @Test(description = "To test migration is successful with EDC fee with 51051000100000000046 productCode for EDC merchant for EMI PayMode")
    public void edc46EMI() {
        MigrationDetails migrationDetails = new MigrationDetails(edcQR.getId());
        Response r = migrationDetails.execute();

        List<String> feeFactorsList = r.jsonPath().param("payMethod", "EMI")
                .getList("CONTRACT-DETAIL-2020092851051010016800212665882.productCondition.feeItems.payMethodFeeInfos[0]" +
                        ".findAll { payMethodFeeInfos -> payMethodFeeInfos.payMethod == payMethod}.feeRateFactors").stream().distinct().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());

        Assertions.assertThat(feeFactorsList).containsExactly("{\"EDC\":\"TRUE\"}");
    }



}
