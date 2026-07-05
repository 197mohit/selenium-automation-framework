package com.paytm.framework.grpc;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.paytm.framework.utils.PollingPredicate_V2;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * * @param <Req>  grpc Request class send to server
 * * @param <Resp> grpc Response class receive from server
 * * @param <T> grpc api class name extends {@link GrpcRequest_V2Base}
 *
 * @author ankuragarwal
 *
 * GrpcRequest_V2Base represents template of grpc api
 */

public abstract class GrpcRequest_V2Base<Req, Resp, T extends GrpcRequest_V2Base> implements IGrpcApiRequest_V2 {

    private static final Map<String, ManagedChannel> channelMap = new HashMap<>();
    private final String REQUEST_TEMPLATE_PATH_PREFIX = "grpcApi/request/";
    private LinkedHashMap<String, Object> updateContextMap = new LinkedHashMap<>();
    private LinkedList<String> deleteContextMap = new LinkedList<>();
    private DocumentContext baseContext = null;
    private boolean isRequestLoaded = false;
    private static Logger LOGGER = LoggerFactory.getLogger(GrpcRequest_V2Base.class);

    public abstract Integer servicePort();

    public abstract String serviceHost();

    public abstract String requestTemplatePath();

    public final Req build(Class<Req> reqClazz) {
        loadRequest();
        updateRequestContext();
        return toRequest(reqClazz);
    }

    public final Req build(Req request, Class<Req> reqClass) {
        loadRequest(request);
        updateRequestContext();
        return toRequest(reqClass);
    }

    public T setContext(String jsonPath, Object value) {
        this.updateContextMap.put(jsonPath, value);
        return (T) this;
    }

    public T deleteContext(String jsonPath) {
        this.deleteContextMap.add(jsonPath);
        return (T) this;
    }

    private T loadRequest() {
        loadTemplate();
        return (T) this;
    }

    private T loadRequest(Req request) {
        try {
            String req = toJson((MessageOrBuilder) request);
            loadTemplate(req);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            assert false : e;
        }
        return (T) this;
    }

    private String toJson(MessageOrBuilder messageOrBuilder) throws InvalidProtocolBufferException {
        return JsonFormat.printer().includingDefaultValueFields().print(messageOrBuilder);
    }

    private Req toRequest(Class<Req> reqClazz) {
        if (baseContext != null) {
            AbstractMessage.Builder builder = null;
            try {

                builder = (AbstractMessage.Builder) reqClazz.getMethod("newBuilder").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                assert false : e;
            }
            String json = baseContext.jsonString();
            try {
                JsonFormat.parser().merge(json, builder);
                return (Req) builder.build();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                assert false : e;
            }
        } else {
            assert false : "baseContext is null";
        }
        return null;
    }

    private void loadTemplate(String req) {
        if (!isRequestLoaded) {
            if (null != req && !req.isEmpty()) {
                baseContext = JsonPath.parse(req);
                isRequestLoaded = true;
            } else {
                LOGGER.error("message request cannot be empty");
                assert false : "message request cannot be empty";
            }
        }
    }

    private void loadTemplate() {
        if (!isRequestLoaded) {
            String reqTmplt = requestTemplatePath();
            if (null != reqTmplt && !reqTmplt.isEmpty()) {
                String requestPath = REQUEST_TEMPLATE_PATH_PREFIX + reqTmplt;
                if (!requestPath.contains("reportportal"))
                    LOGGER.info("loading request: " + requestPath);
                ClassLoader cl = getClass().getClassLoader();
                URL resource = cl.getResource(requestPath);
                try {
                    baseContext = JsonPath.parse(resource);
                    isRequestLoaded = true;
                } catch (IOException e) {
                    LOGGER.error("Error occurred while loading request: " + requestPath, e);
                }
            } else {
                LOGGER.error("requestTemplatePath is empty or null");
                assert false : "requestTemplatePath is empty or null";
            }
        }
    }

    private void updateRequestContext() {
        if (baseContext == null) {
            LOGGER.info("No need to set context when baseTemplate not provided");
        } else {
            updateContextMap.forEach((k, v) -> {
                String[] path = k.split("\\.");
                if (path != null && path.length > 0) {
                    StringJoiner trgtPath = new StringJoiner(".", "$.", "");
                    for (int i = 0; i < path.length - 1; i++) {
                        trgtPath.add(path[i]);
                    }
                    if (v instanceof List) {
                        JSONArray array = getContextValAsJSONArray(v);
                        if (trgtPath.toString().equalsIgnoreCase("$."))
                            baseContext.put("$", path[path.length - 1], v);
                        else
                            baseContext.put(trgtPath.toString(), path[path.length - 1], array);
                    } else if (getClassName(v).contains("com.paytm") && !v.getClass().isEnum()) {
                        JSONObject object = getContextValAsJSONObject(v);
                        if (trgtPath.toString().equalsIgnoreCase("$."))
                            baseContext.put("$", path[path.length - 1], v);
                        else
                            baseContext.put(trgtPath.toString(), path[path.length - 1], object);
                    } else if (trgtPath.toString().equalsIgnoreCase("$.")) {
                        baseContext.put("$", path[path.length - 1], v);
                    } else
                        baseContext.put(trgtPath.toString(), path[path.length - 1], v);
                }
            });
            deleteContextMap.forEach(p -> {
                String[] path = p.split("\\.");
                if (path != null && path.length > 0) {
                    StringJoiner trgtPath = new StringJoiner(".", "$.", "");
                    for (int i = 0; i < path.length - 1; i++) {
                        trgtPath.add(path[i]);
                    }
                    baseContext.delete(trgtPath.toString() + "." + path[path.length - 1]);
                }
            });
            updateContextMap.clear();
            deleteContextMap.clear();
        }
    }

    private JSONArray getContextValAsJSONArray(Object obj) {
        JSONArray array = new JSONArray();
        ((List) obj).forEach(i -> {
            array.add(getContextValAsJSONObject(i));
        });
        return array;
    }

    private JSONObject getContextValAsJSONObject(Object obj) {
        assert obj != null : "input value cannot be null";
        try {
            String sJson = JsonFormat.printer().print((MessageOrBuilder) obj);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(sJson);
        } catch (InvalidProtocolBufferException | ParseException e) {
            e.printStackTrace();
            LOGGER.error("error converting message", e);
        }
        return null;
    }

    private String getClassName(Object obj) {
        try {
            return obj.getClass().getName();
        } catch (Exception e) {
            return "";
        }
    }

    public Resp withPolling(String methodName, Req request, Predicate<Resp> respPredicate) {
        return withPolling(methodName, request, respPredicate, Duration.ofMinutes(2), Duration.ofSeconds(2));
    }

    public Resp withPolling(String methodName, Req request, Predicate<Resp> respPredicate, Duration MAX_TIME, Duration POLL_TIME) {
        Callable<Resp> respCallable = getCallable(methodName, request);
        Resp resp = new PollingPredicate_V2<Resp>(respCallable, respPredicate, MAX_TIME, POLL_TIME)
                .evaluate();
        assert null != resp : "predicate condition not met successfully";
        return resp;
    }

    private Callable<Resp> getCallable(String methodName, Req req) {
        return new Callable<Resp>() {
            @Override
            public Resp call() throws Exception {
                try {
                    Method method = getMethod(methodName, req);
                    Resp resp = (Resp) method.invoke(getStub(), req);
                    return resp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Method getMethod(String methodName, Req req) throws NoSuchMethodException {
        return this.getStub().getClass().getMethod(methodName, req.getClass());
    }

    protected ManagedChannel getChannel() {
        String channelKey = getChannelKey();
        if (channelMap.containsKey(channelKey)) {
            ManagedChannel channel = channelMap.get(channelKey);
            if (isActiveChannel(channel))
                return channel;
            else {
                channelMap.remove(channelKey);
                ManagedChannel channel1 = initChannel();
                channelMap.put(channelKey, channel1);
            }
        } else {
            ManagedChannel channel1 = initChannel();
            channelMap.put(channelKey, channel1);
        }
        return channelMap.get(channelKey);
    }

    private boolean isActiveChannel(ManagedChannel channel) {
        if (channel.isShutdown())
            return false;
        return true;
    }

    private ManagedChannel initChannel() {
        GRPCInterceptor_V2 interceptor = new GRPCInterceptor_V2();
        LOGGER.info(String.format("Initiating channel for serviceHost: %s , servicePort: %s", serviceHost(), servicePort()));
        return ManagedChannelBuilder
                .forAddress(serviceHost(), servicePort())
                .usePlaintext()
                .intercept(interceptor)
                .build();
    }

    private String getChannelKey() {
        String host = serviceHost();
        int port = servicePort();
        return host + "_" + port;
    }


}
