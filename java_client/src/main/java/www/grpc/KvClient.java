package www.grpc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import www.grpc.proto.Scyllaquery;
import www.grpc.proto.QueryScyllaGrpc;
import io.grpc.ManagedChannel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalTime;

public class KvClient {
    private final ManagedChannel channel;
    private final QueryScyllaGrpc.QueryScyllaFutureStub stub;
    private AtomicLong rpcCount = new AtomicLong();
    private Semaphore limiter;
    private final FileOutputStream fos;


    public KvClient(ManagedChannel channel, int numberOfConcurrentGrpc, String name) throws FileNotFoundException {
        this.channel = channel;
        this.limiter = new Semaphore(numberOfConcurrentGrpc);
        this.stub = QueryScyllaGrpc.newFutureStub(channel);
        this.fos = new FileOutputStream(name + ".csv", true);
    }

    public long getRpcCount() {
        return rpcCount.get();
    }

    public void doClientWork(AtomicBoolean done) throws InterruptedException {
        // QueryScyllaGrpc.QueryScyllaFutureStub stub = QueryScyllaGrpc.newFutureStub(channel);
        while(!done.get()) {
            // Call server
            limiter.acquire();
            Random rand = new Random();
            int r = rand.nextInt(3);
            long start = System.nanoTime();
            Scyllaquery.Request request = Scyllaquery.Request.newBuilder().setKey(String.valueOf(r)).setStart(start).build();
            ListenableFuture<Scyllaquery.Response> res = stub.executeQuery(request);

            res.addListener(() ->  {
                rpcCount.incrementAndGet();
                limiter.release();
            }, MoreExecutors.directExecutor());

            Futures.addCallback(res, new FutureCallback<Scyllaquery.Response>() {
                @Override
                public void onSuccess(Scyllaquery.Response response) {
                    long delay = (System.nanoTime() - response.getStart()) / 1000000;
                    System.out.println("Got response " + response.getValuesList().toString() + " delay " + delay + " ms");
                    try {
                        fos.write((delay + "\r\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    Status status = Status.fromThrowable(t);
                    System.out.println("On Failure " + status.getCode());
                }
            }, MoreExecutors.directExecutor());
        }
    }

    public void finish() throws IOException {
        this.fos.close();
    }
}
