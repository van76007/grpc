package www.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientRunner {
    private static final long DURATION_SECONDS = 100;

    /**
     * NUM_CONCURRENCY = 1 -> Did 441.73333333333335 RPCs/s but most of the time delay is 1/2 ms
     * NUM_CONCURRENCY = 100 -> Did 5044.233333333334 RPCs/s but most of the time delay is 10 ms
     */
    private static final int NUM_CONCURRENCY = 100;

    // Use e.g. "dns:///192.168.1.35:8090" if server running on another machine
    private static final String GRPC_SERVER = "dns:///192.168.1.35:8090";

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

        channel = ManagedChannelBuilder.forTarget(GRPC_SERVER).usePlaintext().build();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            AtomicBoolean done = new AtomicBoolean();
            KvClient client = new KvClient(channel, NUM_CONCURRENCY);
            System.out.println("Starting");
            scheduler.schedule(() -> done.set(true), DURATION_SECONDS, TimeUnit.SECONDS);
            client.doClientWork(done);
            long requestCount = client.getRpcCount();
            double qps = (double) requestCount / DURATION_SECONDS;
            System.out.println("Completed " + qps + " RPCs/s");

        } finally {
            scheduler.shutdownNow();
            channel.shutdownNow();
        }
    }
}
