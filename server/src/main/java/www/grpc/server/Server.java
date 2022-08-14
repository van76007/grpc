package www.grpc.server;
import io.grpc.netty.NettyServerBuilder;
import www.grpc.cql.CQLConfiguration;
import www.grpc.cql.CQLDriverV2;
import www.grpc.cql.CQLSession;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final static int SHUTDOWN_TIMEOUT_SECONDS = 60;
    private final io.grpc.Server server;

    public Server() {
        /*
        server =
                NettyServerBuilder.forAddress(new InetSocketAddress(listenAddress, port))
                        // `Persistence` operations are done asynchronously so there isn't a need for a separate
                        // thread pool for handling gRPC callbacks in `GrpcService`.
                        .directExecutor()
                        .intercept(new NewConnectionInterceptor(persistence, authenticationService))
                        .intercept(
                                new TaggingMetricCollectingServerInterceptor(
                                        metrics.getMeterRegistry(), grpcMetricsTagProvider))

                        .addService(new Service(persistence, executor))
                        .build();
                        */
        server = configureNettyServerBuilder().addService(buildService()).build();
    }

    public void start() {
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void stop() {
        try {
            server.shutdown();
            long timeoutMillis = TimeUnit.SECONDS.toMillis(SHUTDOWN_TIMEOUT_SECONDS);
            long start = System.currentTimeMillis();

            if (!server.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out while waiting for executor shutdown");
            }
        } catch (InterruptedException e) {
            System.out.println("Failed waiting for gRPC shutdown" + e);
        }
    }

    private CQLDriverV2 buildDriver() {
        CQLConfiguration config = CQLConfiguration.builder()
                .addContactPoint("localhost", 9042)
                .build();
        CQLSession session = new CQLSession(config);
        return new CQLDriverV2(session);
    }

    private Service buildService() {
        return new Service(buildDriver());
    }

    private static NettyServerBuilder configureNettyServerBuilder() {
        NettyServerBuilder sb = NettyServerBuilder.forPort(8090);

        String threads = System.getenv("JVM_EXECUTOR_THREADS");
        int i_threads = Runtime.getRuntime().availableProcessors();
        if (threads != null && !threads.isEmpty()) {
            i_threads = Integer.parseInt(threads);
        }

        // In principle, the number of threads should be equal to the number of CPUs
        i_threads = i_threads * 8;

        String value = System.getenv().getOrDefault("JVM_EXECUTOR_TYPE", "direct");
        System.out.println("Number of threads " + i_threads + " and executor style=" + value);

        if (Objects.equals(value, "direct")) {
            sb = sb.directExecutor();
        } else if (Objects.equals(value, "single")) {
            sb = sb.executor(Executors.newSingleThreadExecutor());
        } else if (Objects.equals(value, "fixed")) {
            sb = sb.executor(Executors.newFixedThreadPool(i_threads));
        } else if (Objects.equals(value, "workStealing")) {
            sb = sb.executor(Executors.newWorkStealingPool(i_threads));
        } else if (Objects.equals(value, "cached")) {
            sb = sb.executor(Executors.newCachedThreadPool());
        }
        return sb;
    }
}
