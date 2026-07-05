/*
package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.utils.merchant.limit.MerchantVelocityLimit;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


public class Velocity extends PGPBaseTest {

    //need to check for all the themes

    @Parameters({"theme"})
    @Test(description = "Verify successfull txn if merchant limit is configured but not breached and theia has enabled velocity check")
    public void VerifySuccessfulTxnWhenMerchantLimitNotBreached(@Optional("merchant") String theme) throws Exception {
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        String mid = Constants.MerchantType.HYBPEON.getId();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).withFailMessage("Cashier page not displayed.").containsIgnoringCase("Paytm Secure Online Payment Gateway");
        }catch (Exception e){

        }
        finally{
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);

        }
    }

    @Parameters({"theme"})//done
    @Test(description = "Verify Fail txn if merchant monthly Amount limit is breached and theia has enabled velocity check")
    public void VelocityMonthAmountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        SoftAssertions soft = new SoftAssertions();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
            merchantLimit.breachLimitAmount(mid, MerchantVelocityLimit.LIMITDURATION.MONTH);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).withFailMessage("Merchant limit breached msg not displayed. ").containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify Fail txn if merchant Day Amount limit is breached and theia has enabled velocity check")
    public void VelocityDayAmountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
            merchantLimit.breachLimitAmount(mid, MerchantVelocityLimit.LIMITDURATION.DAY);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).withFailMessage("Merchant limit breached msg not displayed. ").containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify Fail txn if merchant Week Amount limit is breached and theia has enabled velocity check")
    public void VelocityWeekAmountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
            merchantLimit.breachLimitAmount(mid, MerchantVelocityLimit.LIMITDURATION.WEEK);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).withFailMessage("Merchant limit breached msg not displayed. ").containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.AMOUNT);
        }
    }


    //Velocity limit check for count is not yet implemented on theia
    @Parameters({"theme"})
    @Test(description = "Verify Fail txn if merchant Month Count limit is breached and theia has enabled velocity check")
    public void VelocityMonthCountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        SoftAssertions soft = new SoftAssertions();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
            merchantLimit.breachLimitCount(mid, MerchantVelocityLimit.LIMITDURATION.MONTH);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify Fail txn if merchant Day Count limit is breached and theia has enabled velocity check")
    public void VelocityDayCountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        SoftAssertions soft = new SoftAssertions();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();

            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
            merchantLimit.breachLimitCount(mid, MerchantVelocityLimit.LIMITDURATION.DAY);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify Fail txn if merchant Month Count limit is breached and theia has enabled velocity check")
    public void VelocityWeekCountLimitReached(@Optional("merchant") String theme) throws Exception {
        String mid = Constants.MerchantType.HYBPEON.getId();
        MerchantVelocityLimit merchantLimit = new MerchantVelocityLimit();
        SoftAssertions soft = new SoftAssertions();
        try {
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HYBPEON, theme)
                    .setTXN_AMOUNT("600")
                    .build();
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
            merchantLimit.breachLimitCount(mid, MerchantVelocityLimit.LIMITDURATION.WEEK);
            String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
            Assertions.assertThat(response).containsIgnoringCase("Your merchant has reached the monthly limit of accepting payments. Please contact your merchant");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            merchantLimit.resetLimit(mid, MerchantVelocityLimit.LIMITTYPE.COUNT);
        }
    }
}
*/
