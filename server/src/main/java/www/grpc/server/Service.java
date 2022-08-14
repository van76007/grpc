package www.grpc.server;

import www.grpc.concurrent.SynchronizedStreamObserver;
import www.grpc.cql.CQLDriver;
import www.grpc.proto.QueryScyllaGrpc;
import www.grpc.proto.Scyllaquery.Request;
import www.grpc.proto.Scyllaquery.Response;
import io.grpc.stub.StreamObserver;

public class Service extends QueryScyllaGrpc.QueryScyllaImplBase {
    private final CQLDriver driver;

    public Service(CQLDriver driver) {
        this.driver = driver;
    }

    @Override
    public void executeQuery(Request request, StreamObserver<Response> responseObserver) {
        // io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteQueryMethod(), responseObserver);
        SynchronizedStreamObserver<Response> synchronizedStreamObserver = new SynchronizedStreamObserver<>(responseObserver);
        ExceptionHandler exceptionHandler = new ExceptionHandler(synchronizedStreamObserver);
        new RequestHandler(synchronizedStreamObserver, exceptionHandler, driver).hanle(request);
    }
}
