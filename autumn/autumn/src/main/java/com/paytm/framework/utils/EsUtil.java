package com.paytm.framework.utils;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Author:NikunjKumar.
 * Date: 2019/05/03.
 */

public class EsUtil {

    private static EsUtil esUtil;
    private static TransportClient tclient = null;
    private static BulkProcessor staticBulkProcessor = null;

    private EsUtil() {
    }


    public static synchronized EsUtil getInstance() {
        if (esUtil == null) {
            esUtil = new EsUtil();
        }
        return esUtil;
    }

    //eHosts: Comma Seperated Value of Hosts along with post, For eg: 192.168.1.41:9300,192.168.1.42:9300,192.168.1.43:9300
    public synchronized TransportClient getClient(String eHosts, String clusterName) throws UnknownHostException {
        if (tclient == null) {
            String EsHosts = eHosts;
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName)
                    //.put("tclient.transport.sniff", true)
                    .build();

            tclient = new PreBuiltTransportClient(settings);
            String[] nodes = EsHosts.split(",");
            for (String node : nodes) {
                if (node.length() > 0) {
                    String[] hostPort = node.split(":");
                    tclient.addTransportAddress(
                            new TransportAddress(
                                    InetAddress.getByName(hostPort[0]), Integer.parseInt(hostPort[1])));
                }
            }
        }
        return tclient;
    }

    public synchronized SearchHits executeSearchQuery(String eHosts,int from, int size, String clusterName,QueryBuilder queryBuilder, String... index) throws UnknownHostException {
        tclient = this.getClient(eHosts, clusterName);
        SearchRequestBuilder searchRequestBuilder = tclient.prepareSearch(index);
        SearchResponse searchResponse = searchRequestBuilder.
               /* setQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("@timestamp"))
                        .should(QueryBuilders.termQuery("id", id))
                        .should(QueryBuilders.)
                        .should(QueryBuilders.prefixQuery("content", content)))*/
                setQuery(queryBuilder)
                .setFrom(from).setSize(size).setExplain(true).execute().actionGet();
        return searchResponse.getHits();

    }
}