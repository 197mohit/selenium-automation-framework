package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PG2LogsValidationHelper extends BaseApi {
    String gte;
    String lte;

    public  void getTimeStamp() {
        String currentDate= Instant.now().toString();
        Instant instant = Instant.parse(currentDate);
       gte = instant.minus(5, ChronoUnit.MINUTES).toString();
       lte=instant.plus(5, ChronoUnit.MINUTES).toString();
    }

    public static class setKibanaIndex {
        public static final String THEIA_FACADE = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-theia-facade-*";
        public static final String THEIA_PRIMARY = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-theia*";
        public static final String MAPPING_SERVICE = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-mapping-service-*";
        //public static final String INSTAPROXY = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-instaproxy-*";
        public static final String INSTAPROXY = LocalConfig.ENV_NAME.toLowerCase()+"-instaproxy-service*";
        public static final String MERCHANT_STATUS = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-merchant-status*";
        public static final String PG_PROXY_NOTIFICATION = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-pgproxy-notification-*";
        public static final String SUBSCRIPTION = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-subscription-*";
        public static final String BILLPROXY = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-billproxy*";
        public static final String SAVEDCARDSERVICE = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-savedcardservice-*";
        public static final String SAVEDCARDSERVICE_FACADE = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-savedcardservice-facade-*";
        public static final String NOTIFICATION_QUEUE_HANDLER = LocalConfig.ENV_NAME.toLowerCase()+"-notification*";
        public static final String COMMUNICATION_GATEWAY = LocalConfig.ENV_NAME.toLowerCase()+"-communicationgateway*";
        public static final String LINKSERVICE = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-link-service*";
        public static final String LINKSERVICE_EVENT = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-link-event*";
        public static final String LINK_EXCHANGE= "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-link-exchange*";
        public static final String PAYMENT_OPTION_FACADE= LocalConfig.ENV_NAME.toLowerCase()+"-payment-option-facade*";
        public static final String PAYMENT_OPTION=LocalConfig.ENV_NAME.toLowerCase()+"-payment-option*";

        public static final String UPI_PSP_PROCESSOR = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-upi_psp_processor*";
        public static final String DOWN_STEAM_REQUEST_RESPONSE = LocalConfig.ENV_NAME.toLowerCase()+"-request_response_upi_psp_processor*";
        public static final String THEIA_REQ_RESP = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-theia_request_response*";
        public static final String REFUND_SERVICE_LOGS = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-refund-*";
        public static final String REFUND_FACADE_LOGS = "qa-"+LocalConfig.ENV_NAME.toLowerCase()+"-refund_facade*";
    }
    public static class setEnvService {
        public static final String theia_facade = "THEIA_FACADE";
        public static final String theia = "THEIA_PRIMARY";
        public static final String mapping_service = "MAPPING_SERVICE";
        public static final String instaproxy = "INSTAPROXY";
        public static final String merchant_status = "MERCHANT_STATUS";
        public static final String pgproxy_notification = "PG_PROXY_NOTIFICATION";
        public static final String subscription = "SUBSCRIPTION";
        public static final String billproxy = "BILLPROXY";
        public static final String savedcard_service = "SAVEDCARDSERVICE";
        public static final String savedcard_service_facade = "SAVEDCARDSERVICE_FACADE";
        public static final String notification_Queue_handler = "NOTIFICATION_QUEUE_HANDLER";
        public static final String communication_Gateway = "COMMUNICATION_GATEWAY";
        public static final String link_service = "LINKSERVICE";
        public static final String link_service_event = "LINKSERVICE_EVENT";
        public static final String link_exchange= "LINK_EXCHANGE";
        public static final String payment_option_facade= "PAYMENT_OPTION_FACADE";
        public static final String payment_option="PAYMENT_OPTION";
        public static final String UPI_PSP_PROCESSOR = "UPI_PSP_PROCESSOR";
        public static final String DOWN_STEAM_REQUEST_RESPONSE = "DOWN_STEAM_REQUEST_RESPONSE";
        public static final String THEIA_REQ_RESP = "THEIA_REQ_RESP";
        public static final String REFUND_SERVICE_LOGS = "REFUND_SERVICE_LOGS";
        public static final String REFUND_FACADE_LOGS = "REFUND_FACADE_LOGS";


    }

   String ESRequest= "{\n" +
           "    \"batch\": [\n" +
           "        {\n" +
           "            \"request\": {\n" +
           "                \"params\": {\n" +
           "                    \"index\": \"qa-ite-theia-facade-*\",\n" +
           "                    \"body\": {\n" +
           "                        \"sort\": [\n" +
           "                            {\n" +
           "                                \"@timestamp\": {\n" +
           "                                    \"order\": \"desc\",\n" +
           "                                    \"unmapped_type\": \"boolean\"\n" +
           "                                }\n" +
           "                            }\n" +
           "                        ],\n" +
           "                        \"fields\": [\n" +
           "                            {\n" +
           "                                \"field\": \"*\",\n" +
           "                                \"include_unmapped\": \"true\"\n" +
           "                            },\n" +
           "                            {\n" +
           "                                \"field\": \"@timestamp\",\n" +
           "                                \"format\": \"strict_date_optional_time\"\n" +
           "                            }\n" +
           "                        ],\n" +
           "                        \"size\": 5000,\n" +
           "                        \"version\": true,\n" +
           "                        \"script_fields\": {},\n" +
           "                        \"stored_fields\": [\n" +
           "                            \"*\"\n" +
           "                        ],\n" +
           "                        \"runtime_mappings\": {},\n" +
           "                        \"_source\": false,\n" +
           "                        \"query\": {\n" +
           "                            \"bool\": {\n" +
           "                                \"must\": [],\n" +
           "                                \"filter\": [\n" +
           "                                    {\n" +
           "                                        \"multi_match\": {\n" +
           "                                            \"type\": \"phrase\",\n" +
           "                                            \"query\": \"71fcc0a1ec714a49849a747bb7c5c38e\",\n" +
           "                                            \"lenient\": true\n" +
           "                                        }\n" +
           "                                    },\n" +
           "                                    {\n" +
           "                                        \"range\": {\n" +
           "                                            \"@timestamp\": {\n" +
           "                                                \"format\": \"strict_date_optional_time\",\n" +
           "                                                \"gte\": \"2023-05-22T05:00:12.040Z\",\n" +
           "                                                \"lte\": \"2023-05-22T15:00:12.040Z\"\n" +
           "                                            }\n" +
           "                                        }\n" +
           "                                    }\n" +
           "                                ],\n" +
           "                                \"should\": [],\n" +
           "                                \"must_not\": []\n" +
           "                            }\n" +
           "                        },\n" +
           "                        \"highlight\": {\n" +
           "                            \"pre_tags\": [\n" +
           "                                \"@kibana-highlighted-field@\"\n" +
           "                            ],\n" +
           "                            \"post_tags\": [\n" +
           "                                \"@/kibana-highlighted-field@\"\n" +
           "                            ],\n" +
           "                            \"fields\": {\n" +
           "                                \"*\": {}\n" +
           "                            },\n" +
           "                            \"fragment_size\": 2147483647\n" +
           "                        }\n" +
           "                    },\n" +
           "                    \"track_total_hits\": false,\n" +
           "                    \"preference\": 1684740311909\n" +
           "                }\n" +
           "            },\n" +
           "            \"options\": {\n" +
           "                \"isRestore\": false,\n" +
           "                \"strategy\": \"ese\",\n" +
           "                \"isStored\": false,\n" +
           "                \"executionContext\": {\n" +
           "                    \"type\": \"application\",\n" +
           "                    \"name\": \"discover\",\n" +
           "                    \"description\": \"fetch documents\",\n" +
           "                    \"url\": \"/app/discover\",\n" +
           "                    \"id\": \"\"\n" +
           "                }\n" +
           "            }\n" +
           "        },\n" +
           "        {\n" +
           "            \"request\": {\n" +
           "                \"params\": {\n" +
           "                    \"index\": \"qa-ite-theia*\",\n" +
           "                    \"body\": {\n" +
           "                        \"size\": 0,\n" +
           "                        \"aggs\": {\n" +
           "                            \"2\": {\n" +
           "                                \"date_histogram\": {\n" +
           "                                    \"field\": \"@timestamp\",\n" +
           "                                    \"fixed_interval\": \"30s\",\n" +
           "                                    \"time_zone\": \"Asia/Calcutta\",\n" +
           "                                    \"min_doc_count\": 1\n" +
           "                                }\n" +
           "                            }\n" +
           "                        },\n" +
           "                        \"script_fields\": {},\n" +
           "                        \"stored_fields\": [\n" +
           "                            \"*\"\n" +
           "                        ],\n" +
           "                        \"runtime_mappings\": {},\n" +
           "                        \"query\": {\n" +
           "                            \"bool\": {\n" +
           "                                \"must\": [],\n" +
           "                                \"filter\": [\n" +
           "                                    {\n" +
           "                                        \"multi_match\": {\n" +
           "                                            \"type\": \"phrase\",\n" +
           "                                            \"query\": \"71fcc0a1ec714a49849a747bb7c5c38e\",\n" +
           "                                            \"lenient\": true\n" +
           "                                        }\n" +
           "                                    },\n" +
           "                                    {\n" +
           "                                        \"range\": {\n" +
           "                                            \"@timestamp\": {\n" +
           "                                                \"format\": \"strict_date_optional_time\",\n" +
           "                                                \"gte\": \"2023-05-22T05:00:12.040Z\",\n" +
           "                                                \"lte\": \"2023-05-22T15:00:12.040Z\"\n" +
           "                                            }\n" +
           "                                        }\n" +
           "                                    }\n" +
           "                                ],\n" +
           "                                \"should\": [],\n" +
           "                                \"must_not\": []\n" +
           "                            }\n" +
           "                        }\n" +
           "                    },\n" +
           "                    \"track_total_hits\": true\n" +
           "                   \n" +
           "                }\n" +
           "            },\n" +
           "            \"options\": {\n" +
           "             \n" +
           "                \"isRestore\": false,\n" +
           "                \"strategy\": \"ese\",\n" +
           "                \"isStored\": false,\n" +
           "                \"executionContext\": {\n" +
           "                    \"type\": \"application\",\n" +
           "                    \"name\": \"discover\",\n" +
           "                    \"description\": \"fetch chart data and total hits\",\n" +
           "                    \"url\": \"/app/discover\",\n" +
           "                    \"id\": \"\"\n" +
           "                }\n" +
           "            }\n" +
           "        }\n" +
           "    ]\n" +
           "}";
    String ESRequestWithLogger="{\n" +
            "    \"batch\": [\n" +
            "        {\n" +
            "            \"request\": {\n" +
            "                \"params\": {\n" +
            "                    \"index\": \"qa-ite-theia-facade-*\",\n" +
            "                    \"body\": {\n" +
            "                        \"sort\": [\n" +
            "                            {\n" +
            "                                \"@timestamp\": {\n" +
            "                                    \"order\": \"desc\",\n" +
            "                                    \"unmapped_type\": \"boolean\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"fields\": [\n" +
            "                            {\n" +
            "                                \"field\": \"*\",\n" +
            "                                \"include_unmapped\": \"true\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"field\": \"@timestamp\",\n" +
            "                                \"format\": \"strict_date_optional_time\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"size\": 5000,\n" +
            "                        \"version\": true,\n" +
            "                        \"script_fields\": {},\n" +
            "                        \"stored_fields\": [\n" +
            "                            \"*\"\n" +
            "                        ],\n" +
            "                        \"runtime_mappings\": {},\n" +
            "                        \"_source\": false,\n" +
            "                        \"query\": {\n" +
            "                            \"bool\": {\n" +
            "                                \"must\": [],\n" +
            "                                \"filter\": [\n" +
            "                                    {\n" +
            "                                        \"bool\": {\n" +
            "                                            \"filter\": [\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"phrase\",\n" +
            "                                                        \"query\": \"c6f16bf30ea64eeb9b6ada004b89dc7a\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"phrase\",\n" +
            "                                                        \"query\": \"LITEPAYVIEW_CONSULT\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                }\n" +
            "                                            ]\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"range\": {\n" +
            "                                            \"@timestamp\": {\n" +
            "                                                \"format\": \"strict_date_optional_time\",\n" +
            "                                                \"gte\": \"2023-05-25T12:43:38.267Z\",\n" +
            "                                                \"lte\": \"2023-05-25T12:58:38.267Z\"\n" +
            "                                            }\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"should\": [],\n" +
            "                                \"must_not\": []\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"highlight\": {\n" +
            "                            \"pre_tags\": [\n" +
            "                                \"@kibana-highlighted-field@\"\n" +
            "                            ],\n" +
            "                            \"post_tags\": [\n" +
            "                                \"@/kibana-highlighted-field@\"\n" +
            "                            ],\n" +
            "                            \"fields\": {\n" +
            "                                \"*\": {}\n" +
            "                            },\n" +
            "                            \"fragment_size\": 2147483647\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"track_total_hits\": false,\n" +
            "                    \"preference\": 1685019502227\n" +
            "                }\n" +
            "            },\n" +
            "            \"options\": {\n" +
            "                \"sessionId\": \"31560006-37e0-439e-8188-f34cb7eb142e\",\n" +
            "                \"isRestore\": false,\n" +
            "                \"strategy\": \"ese\",\n" +
            "                \"isStored\": false,\n" +
            "                \"executionContext\": {\n" +
            "                    \"type\": \"application\",\n" +
            "                    \"name\": \"discover\",\n" +
            "                    \"description\": \"fetch documents\",\n" +
            "                    \"url\": \"/app/discover\",\n" +
            "                    \"id\": \"\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"request\": {\n" +
            "                \"params\": {\n" +
            "                    \"index\": \"qa-ite-theia-facade-*\",\n" +
            "                    \"body\": {\n" +
            "                        \"size\": 0,\n" +
            "                        \"aggs\": {\n" +
            "                            \"2\": {\n" +
            "                                \"date_histogram\": {\n" +
            "                                    \"field\": \"@timestamp\",\n" +
            "                                    \"fixed_interval\": \"30s\",\n" +
            "                                    \"time_zone\": \"Asia/Calcutta\",\n" +
            "                                    \"min_doc_count\": 1\n" +
            "                                }\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"script_fields\": {},\n" +
            "                        \"stored_fields\": [\n" +
            "                            \"*\"\n" +
            "                        ],\n" +
            "                        \"runtime_mappings\": {},\n" +
            "                        \"query\": {\n" +
            "                            \"bool\": {\n" +
            "                                \"must\": [],\n" +
            "                                \"filter\": [\n" +
            "                                    {\n" +
            "                                        \"bool\": {\n" +
            "                                            \"filter\": [\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"phrase\",\n" +
            "                                                        \"query\": \"c6f16bf30ea64eeb9b6ada004b89dc7a\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"phrase\",\n" +
            "                                                        \"query\": \"LITEPAYVIEW_CONSULT\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                }\n" +
            "                                            ]\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"range\": {\n" +
            "                                            \"@timestamp\": {\n" +
            "                                                \"format\": \"strict_date_optional_time\",\n" +
            "                                                \"gte\": \"2023-05-25T12:43:38.267Z\",\n" +
            "                                                \"lte\": \"2023-05-25T12:58:38.267Z\"\n" +
            "                                            }\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"should\": [],\n" +
            "                                \"must_not\": []\n" +
            "                            }\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"track_total_hits\": true,\n" +
            "                    \"preference\": 1685019502227\n" +
            "                }\n" +
            "            },\n" +
            "            \"options\": {\n" +
            "                \"sessionId\": \"31560006-37e0-439e-8188-f34cb7eb142e\",\n" +
            "                \"isRestore\": false,\n" +
            "                \"strategy\": \"ese\",\n" +
            "                \"isStored\": false,\n" +
            "                \"executionContext\": {\n" +
            "                    \"type\": \"application\",\n" +
            "                    \"name\": \"discover\",\n" +
            "                    \"description\": \"fetch chart data and total hits\",\n" +
            "                    \"url\": \"/app/discover\",\n" +
            "                    \"id\": \"\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    String ESRequestWithLoggerAndType="{\n" +
            "    \"batch\": [\n" +
            "        {\n" +
            "            \"request\": {\n" +
            "                \"params\": {\n" +
            "                    \"index\": \"qa-ite-theia-facade-*\",\n" +
            "                    \"body\": {\n" +
            "                        \"sort\": [\n" +
            "                            {\n" +
            "                                \"@timestamp\": {\n" +
            "                                    \"order\": \"desc\",\n" +
            "                                    \"unmapped_type\": \"boolean\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"fields\": [\n" +
            "                            {\n" +
            "                                \"field\": \"*\",\n" +
            "                                \"include_unmapped\": \"true\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"field\": \"@timestamp\",\n" +
            "                                \"format\": \"strict_date_optional_time\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"size\": 5000,\n" +
            "                        \"version\": true,\n" +
            "                        \"script_fields\": {},\n" +
            "                        \"stored_fields\": [\n" +
            "                            \"*\"\n" +
            "                        ],\n" +
            "                        \"runtime_mappings\": {},\n" +
            "                        \"_source\": false,\n" +
            "                        \"query\": {\n" +
            "                            \"bool\": {\n" +
            "                                \"must\": [],\n" +
            "                                \"filter\": [\n" +
            "                                    {\n" +
            "                                        \"bool\": {\n" +
            "                                            \"filter\": [\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"1919b6b79d0d42e4abdb08a996f930bd\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"PAYMENT_CASHIER_PAYRESULT_QUERY\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"REQUEST\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                }\n" +
            "                                            ]\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"range\": {\n" +
            "                                            \"@timestamp\": {\n" +
            "                                                \"format\": \"strict_date_optional_time\",\n" +
            "                                                \"gte\": \"2023-09-13T12:22:19.838Z\",\n" +
            "                                                \"lte\": \"2023-09-14T12:22:19.838Z\"\n" +
            "                                            }\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"should\": [],\n" +
            "                                \"must_not\": [\n" +
            "                                    {\n" +
            "                                        \"match_phrase\": {\n" +
            "                                            \"@timestamp\": \"2023-09-12T13:51:49.119Z\"\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"highlight\": {\n" +
            "                            \"pre_tags\": [\n" +
            "                                \"@kibana-highlighted-field@\"\n" +
            "                            ],\n" +
            "                            \"post_tags\": [\n" +
            "                                \"@/kibana-highlighted-field@\"\n" +
            "                            ],\n" +
            "                            \"fields\": {\n" +
            "                                \"*\": {}\n" +
            "                            },\n" +
            "                            \"fragment_size\": 2147483647\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"track_total_hits\": false,\n" +
            "                    \"preference\": 1694440019496\n" +
            "                }\n" +
            "            },\n" +
            "            \"options\": {\n" +
            "                \"sessionId\": \"3eee8290-8580-4090-804e-5d8cdbafae95\",\n" +
            "                \"isRestore\": false,\n" +
            "                \"strategy\": \"ese\",\n" +
            "                \"isStored\": false,\n" +
            "                \"executionContext\": {\n" +
            "                    \"type\": \"application\",\n" +
            "                    \"name\": \"discover\",\n" +
            "                    \"description\": \"fetch documents\",\n" +
            "                    \"url\": \"/app/discover\",\n" +
            "                    \"id\": \"\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"request\": {\n" +
            "                \"params\": {\n" +
            "                    \"index\": \"qa-ite-theia-facade-*\",\n" +
            "                    \"body\": {\n" +
            "                        \"size\": 0,\n" +
            "                        \"aggs\": {\n" +
            "                            \"2\": {\n" +
            "                                \"date_histogram\": {\n" +
            "                                    \"field\": \"@timestamp\",\n" +
            "                                    \"fixed_interval\": \"30m\",\n" +
            "                                    \"time_zone\": \"Asia/Calcutta\",\n" +
            "                                    \"min_doc_count\": 1\n" +
            "                                }\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"script_fields\": {},\n" +
            "                        \"stored_fields\": [\n" +
            "                            \"*\"\n" +
            "                        ],\n" +
            "                        \"runtime_mappings\": {},\n" +
            "                        \"query\": {\n" +
            "                            \"bool\": {\n" +
            "                                \"must\": [],\n" +
            "                                \"filter\": [\n" +
            "                                    {\n" +
            "                                        \"bool\": {\n" +
            "                                            \"filter\": [\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"1919b6b79d0d42e4abdb08a996f930bd\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"PAYMENT_CASHIER_PAYRESULT_QUERY\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"multi_match\": {\n" +
            "                                                        \"type\": \"best_fields\",\n" +
            "                                                        \"query\": \"REQUEST\",\n" +
            "                                                        \"lenient\": true\n" +
            "                                                    }\n" +
            "                                                }\n" +
            "                                            ]\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"range\": {\n" +
            "                                            \"@timestamp\": {\n" +
            "                                                \"format\": \"strict_date_optional_time\",\n" +
            "                                                \"gte\": \"2023-09-13T12:22:19.838Z\",\n" +
            "                                                \"lte\": \"2023-09-14T12:22:19.838Z\"\n" +
            "                                            }\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"should\": [],\n" +
            "                                \"must_not\": [\n" +
            "                                    {\n" +
            "                                        \"match_phrase\": {\n" +
            "                                            \"@timestamp\": \"2023-09-12T13:51:49.119Z\"\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"track_total_hits\": true,\n" +
            "                    \"preference\": 1694440019496\n" +
            "                }\n" +
            "            },\n" +
            "            \"options\": {\n" +
            "                \"sessionId\": \"3eee8290-8580-4090-804e-5d8cdbafae95\",\n" +
            "                \"isRestore\": false,\n" +
            "                \"strategy\": \"ese\",\n" +
            "                \"isStored\": false,\n" +
            "                \"executionContext\": {\n" +
            "                    \"type\": \"application\",\n" +
            "                    \"name\": \"discover\",\n" +
            "                    \"description\": \"fetch chart data and total hits\",\n" +
            "                    \"url\": \"/app/discover\",\n" +
            "                    \"id\": \"\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public PG2LogsValidationHelper() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.ES_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.ES);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().addHeader("Origin",Constants.KibanaIP.KIBANA_ORIGIN);
        getRequestSpecBuilder().addHeader("Referer",Constants.KibanaIP.KIBANA_REFERER);
        getRequestSpecBuilder().addHeader("kbn-version",Constants.KibanaIP.KIBANA_VERSION);
    }
    public PG2LogsValidationHelper(String requestLogger) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.ES_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.ES);
        getRequestSpecBuilder().setBody(getRequestWithLogger());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().addHeader("Origin",Constants.KibanaIP.KIBANA_ORIGIN);
        getRequestSpecBuilder().addHeader("Referer",Constants.KibanaIP.KIBANA_REFERER);
        getRequestSpecBuilder().addHeader("kbn-version",Constants.KibanaIP.KIBANA_VERSION);
    }

    public PG2LogsValidationHelper(String requestLogger,String type) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.ES_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.ES);
        getRequestSpecBuilder().setBody(getRequestWithLoggerAndType());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().addHeader("Origin",Constants.KibanaIP.KIBANA_ORIGIN);
        getRequestSpecBuilder().addHeader("Referer",Constants.KibanaIP.KIBANA_REFERER);
        getRequestSpecBuilder().addHeader("kbn-version",Constants.KibanaIP.KIBANA_VERSION);
    }

    public void buildRequest(String logfile, String query){
        getTimeStamp();
        setContext("batch[0].request.params.index",setLogFile(logfile));
        setContext("batch[1].request.params.index",setLogFile(logfile));
        setContext("batch[0].request.params.body.query.bool.filter[0].multi_match.query",query);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
        setContext("batch[1].request.params.body.query.bool.filter[0].multi_match.query",query);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
    }

    public void buildNewRequest(String serviceName, String query){
        getTimeStamp();
        setContext("batch[0].request.params.index",serviceName);
        setContext("batch[1].request.params.index",serviceName);
        setContext("batch[0].request.params.body.query.bool.filter[0].multi_match.query",query);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
        setContext("batch[1].request.params.body.query.bool.filter[0].multi_match.query",query);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
    }
    public void buildNewRequest(String serviceName, String query, String requestLogger){
        getTimeStamp();
        setContext("batch[0].request.params.index",serviceName);
        setContext("batch[1].request.params.index",serviceName);
        setContext("batch[0].request.params.body.query.bool.filter[0].bool.filter[0].multi_match.query",query);
        setContext("batch[0].request.params.body.query.bool.filter[0].bool.filter[1].multi_match.query",requestLogger);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
        setContext("batch[1].request.params.body.query.bool.filter[0].bool.filter[0].multi_match.query",query);
        setContext("batch[1].request.params.body.query.bool.filter[0].bool.filter[1].multi_match.query",requestLogger);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
    }

    public void buildNewRequestWithMultipleParams(String serviceName, String query, String requestLogger,String type){
        getTimeStamp();
        setContext("batch[0].request.params.index",serviceName);
        setContext("batch[1].request.params.index",serviceName);
        setContext("batch[0].request.params.body.query.bool.filter[0].bool.filter[0].multi_match.query",query);
        setContext("batch[0].request.params.body.query.bool.filter[0].bool.filter[1].multi_match.query",requestLogger);
        setContext("batch[0].request.params.body.query.bool.filter[0].bool.filter[2].multi_match.query",type);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[0].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
        setContext("batch[1].request.params.body.query.bool.filter[0].bool.filter[0].multi_match.query",query);
        setContext("batch[1].request.params.body.query.bool.filter[0].bool.filter[1].multi_match.query",requestLogger);
        setContext("batch[1].request.params.body.query.bool.filter[0].bool.filter[2].multi_match.query",type);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].gte",gte);
        setContext("batch[1].request.params.body.query.bool.filter[1].range[\"@timestamp\"].lte",lte);
    }

    public String getRequest(){
        return ESRequest;
    }
    public String getRequestWithLogger() { return ESRequestWithLogger; }

    public String getRequestWithLoggerAndType() { return ESRequestWithLoggerAndType; }


    public String executeESRequest(PG2LogsValidationHelper pg2LogsValidationHelper) throws InterruptedException {
        int TIMEOUT=5;
        int i=0;
        String response="";
        while(i<TIMEOUT){
            response=pg2LogsValidationHelper.execute().asString();
           if(response.contains("_index")){
               break;
           }
            Thread.sleep(50000);
           i=i+1;

        }
        String jsonFormattedString= response.replaceAll("\\\\", "");
        System.out.println("jsonFormattedString is----"+jsonFormattedString);
        return jsonFormattedString;
    }

    public static String getOrderId(String grepcmd){
        StringBuilder s = new StringBuilder();
        String orderId="";
        String path="/paytm/logs/";
        int firstIndexOfPath=grepcmd.indexOf(path);
        char c= '"';
        int firstIndex=grepcmd.indexOf(c);
        int i=firstIndex+1;
        int j=firstIndexOfPath;
        while(Character.isLetterOrDigit(grepcmd.charAt(j))==false){
            j=j-1;
        }

        while(Character.isDigit(grepcmd.charAt(j))==true || Character.isLetter(grepcmd.charAt(j))==true){
            orderId=orderId+grepcmd.charAt(j);
            j=j-1;
        }
        s.append(orderId);
        s.reverse();
        String ss=s.toString();
        return ss;
    }

    public static String getLogFile(String grepcmd){
        String logFile="";
        String path="/paytm/logs/";
        int firstIndex=grepcmd.indexOf(path);
        System.out.println("firstIndex is---"+firstIndex);
        int i=firstIndex+12;
        while( i<grepcmd.length() ){
            if(grepcmd.charAt(i)=='|'){
                break;
            }
            logFile=logFile+grepcmd.charAt(i);
            i=i+1;
        }
        return logFile;
    }
    public String setLogFile(String logFile){
        String logFileName="";

        if(logFile.contains("theia_facade.log")){
            logFileName= setKibanaIndex.THEIA_FACADE;
        }
        else if(logFile.contains("theia.log")){
            logFileName=setKibanaIndex.THEIA_PRIMARY;
        }
        else if(logFile.contains("mapping-service.log")){
            logFileName=setKibanaIndex.MAPPING_SERVICE;
        }
        else if(logFile.contains("instaproxy.log")){
            logFileName=setKibanaIndex.INSTAPROXY;
        }
        else if(logFile.contains("merchant-status.log")){
            logFileName=setKibanaIndex.MERCHANT_STATUS;
        }
        else if(logFile.contains("savedcardservice.log")){
            logFileName=setKibanaIndex.SAVEDCARDSERVICE;
        }
        else if(logFile.contains("billproxy.log")){
            logFileName=setKibanaIndex.BILLPROXY;
        }
        else if(logFile.contains("subscription.log")){
            logFileName=setKibanaIndex.SUBSCRIPTION;
        }
        else if(logFile.contains("pgproxy-notification.log")){
            logFileName=setKibanaIndex.PG_PROXY_NOTIFICATION;
        }
        else if(logFile.contains("notificationQueueHandler.log")){
            logFileName=setKibanaIndex.NOTIFICATION_QUEUE_HANDLER;
        }
        else if(logFile.contains("communicationGateway.log")){
            logFileName=setKibanaIndex.COMMUNICATION_GATEWAY;
        }
        else if(logFile.contains("payment-option-facade.log")){
            logFileName=setKibanaIndex.PAYMENT_OPTION_FACADE;
        }
        else if(logFile.contains("payment-option.log")){
            logFileName=setKibanaIndex.PAYMENT_OPTION;
        }
        else if(logFile.contains("refund.log")){
            logFileName=setKibanaIndex.REFUND_SERVICE_LOGS;
        }
        else if(logFile.contains("refund_facade.log")){
            logFileName=setKibanaIndex.REFUND_FACADE_LOGS;
        }
        else if(logFile.contains("down_steam_request_response.log")) {
            logFileName=setKibanaIndex.DOWN_STEAM_REQUEST_RESPONSE;
        }
        System.out.println("servicename is---"+logFileName);
        return logFileName;
    }

    public static String getKeyParameterValueFromLogs(String key,String logs){
        int idx=logs.indexOf(key);
        String requiredString=logs.substring(idx);
        int idx1=requiredString.indexOf(",");
        String rrString=requiredString.substring(0,idx1);
        String keysAndValues[]=rrString.split(":");
        String value=keysAndValues[1];
       int begin=0;
       int end=value.length();
       if(value.charAt(0)=='"'){
           begin=1;
       }
        if(value.charAt(value.length()-1)=='"'){
            end=value.length()-1;
        }
        if(value.charAt(value.length()-1)=='}' && value.charAt(value.length()-2)=='"'){
            end=value.length()-2;
        }

        String finalValue=value.substring(begin,end);
           return finalValue;
    }
}
