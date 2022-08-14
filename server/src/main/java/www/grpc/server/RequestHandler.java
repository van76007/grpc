package www.grpc.server;

import io.grpc.stub.StreamObserver;
import www.grpc.cql.CQLDriver;
import www.grpc.proto.Scyllaquery;
import www.grpc.proto.Scyllaquery.Request;
import www.grpc.proto.Scyllaquery.Response;

public class RequestHandler {
    private final StreamObserver<Scyllaquery.Response> responseObserver;
    private final ExceptionHandler exceptionHandler;
    private final CQLDriver driver;

    public RequestHandler(StreamObserver<Response> responseObserver, ExceptionHandler exceptionHandler, CQLDriver driver) {
        this.responseObserver = responseObserver;
        this.exceptionHandler = exceptionHandler;
        this.driver = driver;
    }

    public void hanle(Request request) {
        driver.executeQueryOnExecutorThenConvert(request).whenComplete(
            (response, error) -> {
                if (error != null) {
                    exceptionHandler.handleException(error);
                } else {
                    setSuccess(response);
                }
            });
    }

    private void setSuccess(Response response) {
        // r.getSet(0, ByteBuffer.class).stream()).collect(Collectors.toSet())
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
