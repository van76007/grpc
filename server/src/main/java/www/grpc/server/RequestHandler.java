package www.grpc.server;

import io.grpc.stub.StreamObserver;
import www.grpc.cql.CQLDriver;
import www.grpc.proto.Scyllaquery;
import www.grpc.proto.Scyllaquery.SearchKey;
import www.grpc.proto.Scyllaquery.Values;

public class RequestHandler {
    private final StreamObserver<Scyllaquery.Values> responseObserver;
    private final ExceptionHandler exceptionHandler;
    private final CQLDriver driver;

    public RequestHandler(StreamObserver<Values> responseObserver, ExceptionHandler exceptionHandler, CQLDriver driver) {
        this.responseObserver = responseObserver;
        this.exceptionHandler = exceptionHandler;
        this.driver = driver;
    }

    public void hanle(SearchKey request) {
        driver.executeQueryOnExecutorThenConvert(request.getKey()).whenComplete(
            (response, error) -> {
                if (error != null) {
                    exceptionHandler.handleException(error);
                } else {
                    setSuccess(response);
                }
            });
    }

    private void setSuccess(Values response) {
        // r.getSet(0, ByteBuffer.class).stream()).collect(Collectors.toSet())
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
