package www.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientRunner {
    private static final long DURATION_SECONDS = 60;

    /**
     * NUM_CONCURRENCY = 1 -> Did 441.73333333333335 RPCs/s but most of the time delay is 1/2 ms
     * NUM_CONCURRENCY = 100 -> Did 5044.233333333334 RPCs/s but most of the time delay is 10 ms
     */
    private static final int NUM_CONCURRENCY = 100;
    private ManagedChannel channel;

    public static void main(String[] args) {
        ClientRunner runner = new ClientRunner();
        try {
            runner.runClient();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runClient() throws InterruptedException {
        if (channel != null) {
            throw new IllegalStateException("Already started");
        }

        channel = ManagedChannelBuilder.forTarget("dns:///localhost:8090").usePlaintext().build();
        // channel = NettyChannelBuilder.forAddress("localhost", 8090).usePlaintext(true).build();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            AtomicBoolean done = new AtomicBoolean();
            KvClient client = new KvClient(channel, NUM_CONCURRENCY);
            System.out.println("Starting");
            scheduler.schedule(() -> done.set(true), DURATION_SECONDS, TimeUnit.SECONDS);
            client.doClientWork(done);
            long requestCount = client.getRpcCount();
            double qps = (double) requestCount / DURATION_SECONDS;
            System.out.println("Did " + qps + " RPCs/s");

        } finally {
            scheduler.shutdownNow();
            channel.shutdownNow();
        }
    }
}
