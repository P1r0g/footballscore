package edu.rutmiit.demo.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.66.0)",
    comments = "Source: match_analytics.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MatchAnalyticsGrpc {

  private MatchAnalyticsGrpc() {}

  public static final java.lang.String SERVICE_NAME = "matchanalytics.MatchAnalytics";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<edu.rutmiit.demo.grpc.AnalyzeMatchRequest,
      edu.rutmiit.demo.grpc.MatchAnalysisResponse> getAnalyzeMatchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AnalyzeMatch",
      requestType = edu.rutmiit.demo.grpc.AnalyzeMatchRequest.class,
      responseType = edu.rutmiit.demo.grpc.MatchAnalysisResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<edu.rutmiit.demo.grpc.AnalyzeMatchRequest,
      edu.rutmiit.demo.grpc.MatchAnalysisResponse> getAnalyzeMatchMethod() {
    io.grpc.MethodDescriptor<edu.rutmiit.demo.grpc.AnalyzeMatchRequest, edu.rutmiit.demo.grpc.MatchAnalysisResponse> getAnalyzeMatchMethod;
    if ((getAnalyzeMatchMethod = MatchAnalyticsGrpc.getAnalyzeMatchMethod) == null) {
      synchronized (MatchAnalyticsGrpc.class) {
        if ((getAnalyzeMatchMethod = MatchAnalyticsGrpc.getAnalyzeMatchMethod) == null) {
          MatchAnalyticsGrpc.getAnalyzeMatchMethod = getAnalyzeMatchMethod =
              io.grpc.MethodDescriptor.<edu.rutmiit.demo.grpc.AnalyzeMatchRequest, edu.rutmiit.demo.grpc.MatchAnalysisResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AnalyzeMatch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.rutmiit.demo.grpc.AnalyzeMatchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.rutmiit.demo.grpc.MatchAnalysisResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MatchAnalyticsMethodDescriptorSupplier("AnalyzeMatch"))
              .build();
        }
      }
    }
    return getAnalyzeMatchMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MatchAnalyticsStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsStub>() {
        @java.lang.Override
        public MatchAnalyticsStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchAnalyticsStub(channel, callOptions);
        }
      };
    return MatchAnalyticsStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MatchAnalyticsBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsBlockingStub>() {
        @java.lang.Override
        public MatchAnalyticsBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchAnalyticsBlockingStub(channel, callOptions);
        }
      };
    return MatchAnalyticsBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MatchAnalyticsFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchAnalyticsFutureStub>() {
        @java.lang.Override
        public MatchAnalyticsFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchAnalyticsFutureStub(channel, callOptions);
        }
      };
    return MatchAnalyticsFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void analyzeMatch(edu.rutmiit.demo.grpc.AnalyzeMatchRequest request,
        io.grpc.stub.StreamObserver<edu.rutmiit.demo.grpc.MatchAnalysisResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAnalyzeMatchMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MatchAnalytics.
   */
  public static abstract class MatchAnalyticsImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MatchAnalyticsGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MatchAnalytics.
   */
  public static final class MatchAnalyticsStub
      extends io.grpc.stub.AbstractAsyncStub<MatchAnalyticsStub> {
    private MatchAnalyticsStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchAnalyticsStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchAnalyticsStub(channel, callOptions);
    }

    /**
     */
    public void analyzeMatch(edu.rutmiit.demo.grpc.AnalyzeMatchRequest request,
        io.grpc.stub.StreamObserver<edu.rutmiit.demo.grpc.MatchAnalysisResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAnalyzeMatchMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MatchAnalytics.
   */
  public static final class MatchAnalyticsBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MatchAnalyticsBlockingStub> {
    private MatchAnalyticsBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchAnalyticsBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchAnalyticsBlockingStub(channel, callOptions);
    }

    /**
     */
    public edu.rutmiit.demo.grpc.MatchAnalysisResponse analyzeMatch(edu.rutmiit.demo.grpc.AnalyzeMatchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAnalyzeMatchMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MatchAnalytics.
   */
  public static final class MatchAnalyticsFutureStub
      extends io.grpc.stub.AbstractFutureStub<MatchAnalyticsFutureStub> {
    private MatchAnalyticsFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchAnalyticsFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchAnalyticsFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<edu.rutmiit.demo.grpc.MatchAnalysisResponse> analyzeMatch(
        edu.rutmiit.demo.grpc.AnalyzeMatchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAnalyzeMatchMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ANALYZE_MATCH = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ANALYZE_MATCH:
          serviceImpl.analyzeMatch((edu.rutmiit.demo.grpc.AnalyzeMatchRequest) request,
              (io.grpc.stub.StreamObserver<edu.rutmiit.demo.grpc.MatchAnalysisResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getAnalyzeMatchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              edu.rutmiit.demo.grpc.AnalyzeMatchRequest,
              edu.rutmiit.demo.grpc.MatchAnalysisResponse>(
                service, METHODID_ANALYZE_MATCH)))
        .build();
  }

  private static abstract class MatchAnalyticsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MatchAnalyticsBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return edu.rutmiit.demo.grpc.MatchAnalyticsOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MatchAnalytics");
    }
  }

  private static final class MatchAnalyticsFileDescriptorSupplier
      extends MatchAnalyticsBaseDescriptorSupplier {
    MatchAnalyticsFileDescriptorSupplier() {}
  }

  private static final class MatchAnalyticsMethodDescriptorSupplier
      extends MatchAnalyticsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MatchAnalyticsMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MatchAnalyticsGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MatchAnalyticsFileDescriptorSupplier())
              .addMethod(getAnalyzeMatchMethod())
              .build();
        }
      }
    }
    return result;
  }
}
