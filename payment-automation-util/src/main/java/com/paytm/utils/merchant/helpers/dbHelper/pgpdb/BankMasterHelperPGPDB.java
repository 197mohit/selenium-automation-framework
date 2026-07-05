package com.paytm.utils.merchant.helpers.dbHelper.pgpdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.DatabaseDTO.pgpdb.BankMasterDTO;
import com.paytm.utils.merchant.util.DbQueriesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ankuragarwal on 12/10/18
 */
public class BankMasterHelperPGPDB {

    private String query;
    private List<Map<String, Object>> resultList;
    private List<BankMasterDTO> bankMasterList;

    public BankMasterHelperPGPDB(String query){
        this.query = query;
        resultList = DbQueriesUtil.selectFromPGPDB(query);
        if(resultList.isEmpty()) {
            resultList = Collections.emptyList();
        }
    }

    public List<BankMasterDTO> getResultList() {
        if(!resultList.isEmpty()) {
            bankMasterList = new ArrayList<>();
            for(Map<String, Object> map : resultList) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                BankMasterDTO bankMasterDTO = mapper.convertValue(map, BankMasterDTO.class);
                bankMasterList.add(bankMasterDTO);
            }
            return bankMasterList;
        }
        return Collections.emptyList();
    }

    public BankMasterDTO getResult(int index) {
        if(!resultList.isEmpty()) {

            Map<String, Object> result = resultList.get(index);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            BankMasterDTO bankMasterDTO = mapper.convertValue(result, BankMasterDTO.class);
            return bankMasterDTO;
        }
        return null;
    }
}
