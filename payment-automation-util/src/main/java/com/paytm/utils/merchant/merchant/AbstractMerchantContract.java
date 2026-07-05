package com.paytm.utils.merchant.merchant;

import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.CreateMerchant;
import org.fest.assertions.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by deepakkumar on 26/11/17.
 */
public abstract class AbstractMerchantContract {

    String mid;
    Long entityId;
    CreateMerchant merchantConfig;

    private static final boolean EXPLICIT_MIGRATION_EVENT = Boolean.valueOf(Constants.EXPLICIT_MIGRATION_EVENT);


//    public void apply(Configuration config) {
//        config.apply(merchantConfig);
//    }

    void waitForMigration() {
        String query = "SELECT STATUS FROM MIGRATION_MID WHERE MID='" + this.mid + "' ORDER BY CREATED_ON DESC LIMIT 1;";
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, query);
            if (result.get(0).get("STATUS").toString().equalsIgnoreCase("MIGRATED")) {
                break;
            }
            System.out.println(result.get(0).get("STATUS"));
        }
        Assertions.assertThat(result.get(0).get("STATUS").toString()).as("Merchant migration failed").isEqualToIgnoringCase("MIGRATED");
    }

    void migrateMerchantInfo() {
        if (EXPLICIT_MIGRATION_EVENT) {
            String query = "INSERT INTO MIGRATION_MID (MID, ENTITY_ID, STATUS, CREATED_ON, MIGRATED_ON, OPERATION_TYPE)" +
                    " VALUES ('" + this.mid + "', " + entityId() + ",'PENDING', NOW(), NOW(), 'MERCHANT_UPDATE');";
            DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, query);
        }
    }

    private long entityId() {
        if (this.entityId != null) {
            return this.entityId;
        } else {
            String query = "SELECT ID FROM `ENTITY_INFO` WHERE MID='" + this.mid + "';";
            List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, query);
            if (result.size() == 0) {
                throw new RuntimeException("Record not found");
            }
            this.entityId = (Long) result.get(0).get("ID");
            if (this.entityId == null) {
                throw new RuntimeException("Entity Id is either null or empty: " + this.entityId);
            }
            return this.entityId;
        }
    }
}
