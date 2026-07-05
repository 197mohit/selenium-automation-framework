package com.paytm.utils.merchant.util;

import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ankuragarwal on 12/9/18
 */
public class DbQueriesUtil {

    private static String paytmpgDbUrl = Constants.PAYTMPG_DB_CONNECTION_URL;
    private static String pgpDbUrl = Constants.PGP_DB_CONNECTION_URL;

    /**
     *
     * @param query
     * @param columnName
     * @return
     */
    public static String selectFromPaytmPGDB(String query, String columnName) {

        List<Map<String, Object>> resultList = DatabaseUtil.getInstance().executeSelectQuery(paytmpgDbUrl, query);
        if (resultList.size() != 0) {
            Map<String, Object> resultMap = resultList.get(0);
            return resultMap.get(columnName).toString();
        } else
            Reporter.report.info("No result found for query '"+query+"' in PAYTMPGDB");
        return "";
    }





    /**
     *
     * @param query
     * @return Result list or empty list with log on report
     */
    public static List<Map<String, Object>> selectFromPaytmPGDB(String query) {
        List<Map<String, Object>> resultList = DatabaseUtil.getInstance().executeSelectQuery(paytmpgDbUrl, query);
        if (resultList.size() != 0)
            return resultList;
        else
            Reporter.report.info("No result found for query '"+query+"' in PAYTMPGDB");
        return Collections.emptyList();
    }

    public static String selectFromPGPDB(String query, String columnName) {
        List<Map<String, Object>> resultList = DatabaseUtil.getInstance().executeSelectQuery(pgpDbUrl, query);
        if (resultList.size() != 0) {
            Map<String, Object> resultMap = resultList.get(0);
            return resultMap.get(columnName).toString();
        } else
            Reporter.report.info("No result found for query '"+query+"' in PGPDB");
        return "";
    }

    /**
     *
     * @param query
     * @return Result list or empty list with log on reporting
     */
    public static List<Map<String, Object>> selectFromPGPDB(String query) {
        List<Map<String, Object>> resultList = DatabaseUtil.getInstance().executeSelectQuery(pgpDbUrl, query);
        if (resultList.size() != 0)
            return resultList;
        else
            Reporter.report.info("No result found for query '"+query+"' in PGPDB");
        return Collections.emptyList();
    }

}
