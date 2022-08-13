package www.grpc.proto;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.3.0)",
    comments = "Source: scyllaquery.proto")
public final class QueryScyllaGrpc {

  private QueryScyllaGrpc() {}

  public static final String SERVICE_NAME = "scyllaquery.QueryScylla";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<www.grpc.proto.Scyllaquery.SearchKey,
      www.grpc.proto.Scyllaquery.Values> METHOD_EXECUTE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "scyllaquery.QueryScylla", "ExecuteQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(www.grpc.proto.Scyllaquery.SearchKey.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(www.grpc.proto.Scyllaquery.Values.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static QueryScyllaStub newStub(io.grpc.Channel channel) {
    return new QueryScyllaStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static QueryScyllaBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new QueryScyllaBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static QueryScyllaFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new QueryScyllaFutureStub(channel);
  }

  /**
   */
  public static abstract class QueryScyllaImplBase implements io.grpc.BindableService {

    /**
     */
    public void executeQuery(www.grpc.proto.Scyllaquery.SearchKey request,
        io.grpc.stub.StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_EXECUTE_QUERY, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_EXECUTE_QUERY,
            asyncUnaryCall(
              new MethodHandlers<
                www.grpc.proto.Scyllaquery.SearchKey,
                www.grpc.proto.Scyllaquery.Values>(
                  this, METHODID_EXECUTE_QUERY)))
          .build();
    }
  }

  /**
   */
  public static final class QueryScyllaStub extends io.grpc.stub.AbstractStub<QueryScyllaStub> {
    private QueryScyllaStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryScyllaStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryScyllaStub(channel, callOptions);
    }

    /**
     */
    public void executeQuery(www.grpc.proto.Scyllaquery.SearchKey request,
        io.grpc.stub.StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_QUERY, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class QueryScyllaBlockingStub extends io.grpc.stub.AbstractStub<QueryScyllaBlockingStub> {
    private QueryScyllaBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryScyllaBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryScyllaBlockingStub(channel, callOptions);
    }

    /**
     */
    public www.grpc.proto.Scyllaquery.Values executeQuery(www.grpc.proto.Scyllaquery.SearchKey request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXECUTE_QUERY, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class QueryScyllaFutureStub extends io.grpc.stub.AbstractStub<QueryScyllaFutureStub> {
    private QueryScyllaFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryScyllaFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryScyllaFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryScyllaFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<www.grpc.proto.Scyllaquery.Values> executeQuery(
        www.grpc.proto.Scyllaquery.SearchKey request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_QUERY, getCallOptions()), request);
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

  private static final class QueryScyllaDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return www.grpc.proto.Scyllaquery.getDescriptor();
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
              .setSchemaDescriptor(new QueryScyllaDescriptorSupplier())
              .addMethod(METHOD_EXECUTE_QUERY)
              .build();
        }
      }
    }
    return result;
  }
}
