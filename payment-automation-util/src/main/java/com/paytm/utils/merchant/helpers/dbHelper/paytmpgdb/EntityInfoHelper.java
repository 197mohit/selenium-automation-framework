package com.paytm.utils.merchant.helpers.dbHelper.paytmpgdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.DatabaseDTO.paytmpgdb.EntityInfoDTO;
import com.paytm.utils.merchant.util.DbQueriesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class EntityInfoHelper {

    private String query;
    private List<Map<String, Object>> resultList;
    private List<EntityInfoDTO> entityInfoDTOList;


    public EntityInfoHelper(String query) {
        this.query = query;
        resultList = DbQueriesUtil.selectFromPaytmPGDB(query);
        if(resultList.isEmpty()) {
            resultList = Collections.emptyList();
        }
    }

    public List<EntityInfoDTO> getEntityInfoDTOList(){
        if(!resultList.isEmpty()) {
            entityInfoDTOList = new ArrayList<>();
            for(Map<String, Object> map : resultList) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                EntityInfoDTO entityInfoDTO = mapper.convertValue(map, EntityInfoDTO.class);
                entityInfoDTOList.add(entityInfoDTO);
            }
            return entityInfoDTOList;
        }
        return Collections.emptyList();
    }

}
