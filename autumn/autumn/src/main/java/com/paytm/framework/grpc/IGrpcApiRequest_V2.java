package com.paytm.framework.grpc;

import io.grpc.stub.AbstractStub;

/**
 * @author ankuragarwal
 */
public interface IGrpcApiRequest_V2 {


    String serviceHost();

    Integer servicePort();

    AbstractStub getStub();

}
