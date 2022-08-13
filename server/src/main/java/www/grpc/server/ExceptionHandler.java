package www.grpc.server;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionException;

public class ExceptionHandler {
  private final StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver;

  public ExceptionHandler(StreamObserver<www.grpc.proto.Scyllaquery.Values> responseObserver) {
    this.responseObserver = responseObserver;
  }

  public void handleException(Throwable throwable) {
    if (throwable instanceof CompletionException) {
      handleException(throwable.getCause());
    } else if (throwable instanceof StatusException || throwable instanceof StatusRuntimeException) {
      onError(null, throwable, null);
    } else {
      Status status = Status.UNKNOWN.withDescription(throwable.getMessage()).withCause(throwable);
      onError(status, status.asRuntimeException(), null);
    }
  }

  protected void onError(
      @Nullable Status status, @Nonnull Throwable throwable, @Nullable Metadata trailer) {
    if (status == null) {
      responseObserver.onError(throwable);
      return;
    }
    status = status.withDescription(throwable.getMessage()).withCause(throwable);
    responseObserver.onError(
        trailer != null ? status.asRuntimeException(trailer) : status.asRuntimeException());
  }
}
