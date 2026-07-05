package com.paytm.utils.merchant.merchant;

import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import org.fest.assertions.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by deepakkumar on 7/3/18.
 */
public class SingleEventMigration implements Migration {

    private final String mid;

    public SingleEventMigration(String mid) {
        this.mid = mid;
    }

    @Override
    public void waitTillCompletion() {
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

}
