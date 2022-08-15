package www.grpc.server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import www.grpc.cql.CQLConfiguration;
import www.grpc.cql.CQLDriver;
import www.grpc.cql.CQLSession;
import www.grpc.cql.ConsistencyLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
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
        server = configureNettyServerBuilder()
                .addService(ProtoReflectionService.newInstance())
                .addService(buildService()).build();
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
            if (!server.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out while waiting for executor shutdown");
            }
        } catch (InterruptedException e) {
            System.out.println("Failed waiting for gRPC shutdown" + e);
        }
    }

    private CQLDriver buildDriver() {
        CQLConfiguration config = CQLConfiguration.builder()
                .addContactPoints(Arrays.asList(
                        new InetSocketAddress("localhost", 9042),
                        new InetSocketAddress("localhost", 9043),
                        new InetSocketAddress("localhost", 9044))
                )
                .withCredentials("cassandra", "cassandra")
                .withConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
                .build();
        CQLSession session = new CQLSession(config);
        return new CQLDriver(session);
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

        String value = System.getenv().getOrDefault("JVM_EXECUTOR_TYPE", "workStealing");
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
