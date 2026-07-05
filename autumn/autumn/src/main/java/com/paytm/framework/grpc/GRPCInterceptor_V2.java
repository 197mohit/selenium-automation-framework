package com.paytm.framework.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ankuragarwal
 *
 * GRPCInterceptor_V2 holds logger for GRPC calls
 */
public class GRPCInterceptor_V2 implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GRPCInterceptor_V2.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        try {
                            String msg = JsonFormat.printer().includingDefaultValueFields().print((MessageOrBuilder) message);
                            String className = msg.getClass().getName();
                            LOGGER.info("MESSAGE FROM SERVER: " + className + System.lineSeparator() + msg);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            LOGGER.info("MESSAGE FROM SERVER: " + System.lineSeparator() + message);
                        }
                        super.onMessage(message);
                    }
                }, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                try {
                    String msg = JsonFormat.printer().includingDefaultValueFields().print((MessageOrBuilder) message);
                    String className = message.getClass().getName();
                    LOGGER.info("MESSAGE TO SERVER: " + className + System.lineSeparator() + msg);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    LOGGER.info("MESSAGE TO SERVER: " + System.lineSeparator() + message);
                }
                super.sendMessage(message);
            }
        };
    }
}
