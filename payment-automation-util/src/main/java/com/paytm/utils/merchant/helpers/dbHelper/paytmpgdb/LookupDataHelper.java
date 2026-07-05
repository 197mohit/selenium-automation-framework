package com.paytm.utils.merchant.helpers.dbHelper.paytmpgdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.DatabaseDTO.paytmpgdb.LookupDataDTO;
import com.paytm.utils.merchant.util.DbQueriesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class LookupDataHelper {

    private String query;
    private List<Map<String, Object>> resultList;
    private List<LookupDataDTO> lookupDataDTOS;

    public LookupDataHelper(String query) {
        this.query = query;
        resultList = DbQueriesUtil.selectFromPaytmPGDB(query);
        if(resultList.isEmpty()) {
            resultList = Collections.emptyList();
        }
    }

    public List<LookupDataDTO> getLookupDataDTOS() {
        if(!resultList.isEmpty()) {
            lookupDataDTOS = new ArrayList<>();
            for(Map<String, Object> map : resultList) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                LookupDataDTO lookupDataDTO = mapper.convertValue(map, LookupDataDTO.class);
                lookupDataDTOS.add(lookupDataDTO);
            }
            return lookupDataDTOS;
        }
        return Collections.emptyList();
    }

    public LookupDataDTO getResult(int index) {
        if(!resultList.isEmpty()) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            LookupDataDTO lookupDataDTO = mapper.convertValue(resultList.get(index), LookupDataDTO.class);
            return lookupDataDTO;
        }
        return null;
    }

}
