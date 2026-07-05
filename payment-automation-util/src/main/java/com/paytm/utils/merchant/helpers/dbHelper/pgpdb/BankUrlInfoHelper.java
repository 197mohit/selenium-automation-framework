package com.paytm.utils.merchant.helpers.dbHelper.pgpdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.DatabaseDTO.pgpdb.BankUrlInfoDTO;
import com.paytm.utils.merchant.util.DbQueriesUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class BankUrlInfoHelper {

    private String query;
    private List<Map<String, Object>> resultList;

    public BankUrlInfoHelper(String query) {
        this.query = query;
        resultList = DbQueriesUtil.selectFromPGPDB(query);
        if(resultList.isEmpty()) {
            resultList = Collections.emptyList();
        }
    }

    public BankUrlInfoDTO getResult(int index) {
        if(!resultList.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            BankUrlInfoDTO bankUrlInfoDTO = mapper.convertValue(resultList.get(index), BankUrlInfoDTO.class);
            return bankUrlInfoDTO;
        }

        return null;
    }

}
