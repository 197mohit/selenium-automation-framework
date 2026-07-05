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
public class MultiEventMigration implements Migration {

    private final String mid;
    private final int noOfEvents;

    public MultiEventMigration(String mid, int noOfEvents) {
        this.mid = mid;
        this.noOfEvents = noOfEvents;
    }

    @Override
    public void waitTillCompletion() {
        try {
            String query = "SELECT STATUS FROM MIGRATION_MID WHERE MID='" + mid + "' ORDER BY CREATED_ON DESC LIMIT " + noOfEvents + ";";
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                int nonMigratedEventsCount = 0;
                Thread.sleep(5000);
                result = DatabaseUtil.getInstance().executeSelectQuery(Constants.PG_DB_CONNECTION, query);
                for (int j = 0; j < result.size(); j++) {
                    if (!result.get(j).get("STATUS").toString().equalsIgnoreCase("MIGRATED") && !result.get(j).get("STATUS").toString().equalsIgnoreCase("REDUNDANT")) {
                        nonMigratedEventsCount++;
                    }
                    System.out.println(result.get(j).get("STATUS"));
                }
                if (nonMigratedEventsCount == 0) {
                    break;
                }
            }
            for (int i = 0; i < result.size(); i++) {
                Assertions.assertThat(result.get(i).get("STATUS").toString()).as("Merchant migration failed").isIn("MIGRATED", "REDUNDANT");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
