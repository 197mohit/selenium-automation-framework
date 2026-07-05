package com.paytm.framework.utils;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.Policy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 14/02/18
 */
public class AerospikeUtil {

    private static AerospikeUtil aerospikeConnection;
    private static Map<String, AerospikeClient> aerospikeConnectionMap = new HashMap<>();

    private AerospikeUtil(){
    }

    public static synchronized AerospikeUtil getInstance(){
        if(aerospikeConnection == null){
            aerospikeConnection = new AerospikeUtil();
        }
        return aerospikeConnection;
    }

    public synchronized AerospikeClient getConnection(String host, int port){
        String connectionUrl = host+":"+port;
        AerospikeClient connectionClient = aerospikeConnectionMap.get(connectionUrl);
        if(connectionClient == null  ){
            connectionClient = new AerospikeClient(host,port);
            aerospikeConnectionMap.put(connectionUrl,connectionClient);
        }else{
            if(!connectionClient.isConnected()){
                aerospikeConnectionMap.remove(connectionUrl);
                connectionClient = new AerospikeClient(host,port);
                aerospikeConnectionMap.put(connectionUrl,connectionClient);
            }
        }
        return connectionClient;
    }
    //ToDo
    public Record getRecord(String namespace, String set, String primaryKey, String bin){
        Key key = new Key(namespace,set,primaryKey);
        AerospikeClient client = AerospikeUtil.getInstance().getConnection("10.144.18.121",3000);
        Record record = client.                     get(new Policy(), key);
        String res = record.bins.get(bin).toString();
        System.out.println(res);
        return record;
    }





}
