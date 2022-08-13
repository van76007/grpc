package www.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.35.0)",
    comments = "Source: scyllaquery.proto")
public final class QueryScyllaGrpc {

  private QueryScyllaGrpc() {}

  public static final String SERVICE_NAME = "scyllaquery.QueryScylla";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<www.grpc.proto.Scyllaquery.SearchKey,
      www.grpc.proto.Scyllaquery.Values> getExecuteQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExecuteQuery",
      requestType = www.grpc.proto.Scyllaquery.SearchKey.class,
      responseType = www.grpc.proto.Scyllaquery.Values.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<www.grpc.proto.Scyllaquery.SearchKey,
      www.grpc.proto.Scyllaquery.Values> getExecuteQueryMethod() {
    io.grpc.MethodDescriptor<www.grpc.proto.Scyllaquery.SearchKey, www.grpc.proto.Scyllaquery.Values> getExecuteQueryMethod;
    if ((getExecuteQueryMethod = QueryScyllaGrpc.getExecuteQueryMethod) == null) {
      synchronized (QueryScyllaGrpc.class) {
        if ((getExecuteQueryMethod = QueryScyllaGrpc.getExecuteQueryMethod) == null) {
          QueryScyllaGrpc.getExecuteQueryMethod = getExecuteQueryMethod =
              io.grpc.MethodDescriptor.<www.grpc.proto.Scyllaquery.SearchKey, www.grpc.proto.Scyllaquery.Values>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExecuteQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  www.grpc.proto.Scyllaquery.SearchKey.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  www.grpc.proto.Scyllaquery.Values.getDefaultInstance()))
              .setSchemaDescriptor(new QueryScyllaMethodDescriptorSupplier("ExecuteQuery"))
              .build();
        }
      }
    }
    return getExecuteQueryMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static QueryScyllaStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryScyllaStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryScyllaStub>() {
        @java.lang.Override
        public QueryScyllaStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryScyllaStub(channel, callOptions);
        }
      };
    return QueryScyllaStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static QueryScyllaBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryScyllaBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryScyllaBlockingStub>() {
        @java.lang.Override
        public QueryScyllaBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryScyllaBlockingStub(channel, callOptions);
        }
      };
    return QueryScyllaBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static QueryScyllaFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryScyllaFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryScyllaFutureStub>() {
        @java.lang.Override
        public QueryScyllaFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryScyllaFutureStub(channel, callOptions);
        }
      };
    return QueryScyllaFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class QueryScyllaImplBase implements io.grpc.BindableService {

    /**
     */
    public void executeQuery(www.grpc.proto.Scyllaquery.SearchKey request,
        io.grpc.stub.StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteQueryMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getExecuteQueryMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                www.grpc.proto.Scyllaquery.SearchKey,
                www.grpc.proto.Scyllaquery.Values>(
                  this, METHODID_EXECUTE_QUERY)))
          .build();
    }
  }

  /**
   */
  public static final class QueryScyllaStub extends io.grpc.stub.AbstractAsyncStub<QueryScyllaStub> {
    private QueryScyllaStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryScyllaStub(channel, callOptions);
    }

    /**
     */
    public void executeQuery(www.grpc.proto.Scyllaquery.SearchKey request,
        io.grpc.stub.StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExecuteQueryMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class QueryScyllaBlockingStub extends io.grpc.stub.AbstractBlockingStub<QueryScyllaBlockingStub> {
    private QueryScyllaBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryScyllaBlockingStub(channel, callOptions);
    }

    /**
     */
    public www.grpc.proto.Scyllaquery.Values executeQuery(www.grpc.proto.Scyllaquery.SearchKey request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExecuteQueryMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class QueryScyllaFutureStub extends io.grpc.stub.AbstractFutureStub<QueryScyllaFutureStub> {
    private QueryScyllaFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryScyllaFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<www.grpc.proto.Scyllaquery.Values> executeQuery(
        www.grpc.proto.Scyllaquery.SearchKey request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExecuteQueryMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EXECUTE_QUERY = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final QueryScyllaImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(QueryScyllaImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EXECUTE_QUERY:
          serviceImpl.executeQuery((www.grpc.proto.Scyllaquery.SearchKey) request,
              (io.grpc.stub.StreamObserver<www.grpc.proto.Scyllaquery.Values>) responseObserver);
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

  private static abstract class QueryScyllaBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    QueryScyllaBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return www.grpc.proto.Scyllaquery.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("QueryScylla");
    }
  }

  private static final class QueryScyllaFileDescriptorSupplier
      extends QueryScyllaBaseDescriptorSupplier {
    QueryScyllaFileDescriptorSupplier() {}
  }

  private static final class QueryScyllaMethodDescriptorSupplier
      extends QueryScyllaBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    QueryScyllaMethodDescriptorSupplier(String methodName) {
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
      synchronized (QueryScyllaGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new QueryScyllaFileDescriptorSupplier())
              .addMethod(getExecuteQueryMethod())
              .build();
        }
      }
    }
    return result;
  }
}
