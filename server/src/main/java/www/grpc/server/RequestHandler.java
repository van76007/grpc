package www.grpc.server;

import com.datastax.driver.core.Row;
import io.grpc.stub.StreamObserver;
import www.grpc.cql.CQLDriverV2;
import www.grpc.proto.Scyllaquery;
import www.grpc.proto.Scyllaquery.SearchKey;
import www.grpc.proto.Scyllaquery.Values;

import java.util.Collection;

public class RequestHandler {
    private final StreamObserver<Scyllaquery.Values> responseObserver;
    private final ExceptionHandler exceptionHandler;
    private final CQLDriverV2 driver;

    public RequestHandler(StreamObserver<Values> responseObserver, ExceptionHandler exceptionHandler, CQLDriverV2 driver) {
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
