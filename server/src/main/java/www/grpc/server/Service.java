package www.grpc.server;

import www.grpc.concurrent.SynchronizedStreamObserver;
import www.grpc.cql.CQLDriverV2;
import www.grpc.proto.QueryScyllaGrpc;
import www.grpc.proto.Scyllaquery.SearchKey;
import www.grpc.proto.Scyllaquery.Values;
import io.grpc.stub.StreamObserver;

public class Service extends QueryScyllaGrpc.QueryScyllaImplBase {
    private final CQLDriverV2 driver;

    public Service(CQLDriverV2 driver) {
        this.driver = driver;
    }

    @Override
    public void executeQuery(SearchKey request, StreamObserver<Values> responseObserver) {
        // io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteQueryMethod(), responseObserver);
        SynchronizedStreamObserver<Values> synchronizedStreamObserver = new SynchronizedStreamObserver<>(responseObserver);
        ExceptionHandler exceptionHandler = new ExceptionHandler(synchronizedStreamObserver);
        new RequestHandler(synchronizedStreamObserver, exceptionHandler, driver).hanle(request);
    }
}
