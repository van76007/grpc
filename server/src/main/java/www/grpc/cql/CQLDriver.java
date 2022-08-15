package www.grpc.cql;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import www.grpc.concurrent.ConcurrencyUtils;
import www.grpc.proto.Scyllaquery;
import www.grpc.proto.Scyllaquery.Request;
import www.grpc.proto.Scyllaquery.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toCollection;

/**
 * To execute this function
 * session.getDriverSession().executeAsync(statement.bind("").setConsistencyLevel(session.getConsistencyLevel()))
 */
public class CQLDriver {
    private final CQLSession session;
    private PreparedStatement statement;
    private final String selectQuery = "select value from demo.history where key = ?";

    /*
    private ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
    */

    /*
    private ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    */

    private ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()*8);

    public CQLDriver(CQLSession session) {
        this.session = session;
        prepareStatement();
        System.out.println("Done prepare statement");
    }

    public void stop() {
        session.close();
        ConcurrencyUtils.shutdownAndAwaitTermination(executorService);
    }

    /**
     * Usage: The entire session runs only 1 statement
     */
    private void prepareStatement() {
        this.statement = session.getDriverSession().prepare(selectQuery);
    }

    /**
     * Usage: Each request might be a new statement.
     *        Chain futures: Create prepared statment -> execute it async
     * @param query
     * @return
     */
    private CompletableFuture<PreparedStatement> prepareStatementAsync(String query) {
        return ConcurrencyUtils.convertToCompletableFuture(session.getDriverSession().prepareAsync(query), executorService);
    }

    // This is not good version if calling executeQueryOnExecutorV1
    public CompletableFuture<Response> queryThenConvertV2(Request request) {
        return executeQueryOnExecutorV2(request.getKey()).thenApply(o ->
                Scyllaquery.Response.newBuilder()
                        .addAllValues(o.stream().map(r -> r.getString(0)).collect(toCollection(ArrayList::new)))
                        .setStart(request.getStart())
                        .build()

        );
    }

    public CompletableFuture<Response> queryThenConvert(Request request) {
        return executeQuery(request.getKey()).thenApply(o ->
                Scyllaquery.Response.newBuilder()
                        .addAllValues(o.stream().map(r -> r.getString(0)).collect(toCollection(ArrayList::new)))
                        .setStart(request.getStart())
                        .build()

        );
    }

    /**
     * This is not a good version as it is blocked on CQL session.execute
     * @param key
     * @return
     */
    protected CompletableFuture<Collection<Row>> executeQueryOnExecutorV1(String key) {
        CompletableFuture<Collection<Row>> result = new CompletableFuture<>();
        executorService.submit(
            () -> {
                try {
                    ResultSet rs = session.getDriverSession()
                            .execute(this.statement.bind(key).setConsistencyLevel(session.getConsistencyLevel()));
                    fetchRowsAsync(rs, new ArrayList<>(), result);
                } catch(Throwable t) {
                    result.completeExceptionally(t);
                }
            });
        return result;
    }

    protected CompletableFuture<Collection<Row>> executeQueryOnExecutorV2(String key) {
        CompletableFuture<Collection<Row>> result = new CompletableFuture<>();
        executorService.submit(
                () -> {
                    try {
                        ResultSetFuture future = session.getDriverSession()
                                .executeAsync(this.statement.bind(key).setConsistencyLevel(session.getConsistencyLevel()));
                        Futures.addCallback(future, new FutureCallback<ResultSet>() {
                            @Override
                            public void onSuccess(ResultSet rs) {
                                fetchRowsAsync(rs, new ArrayList<>(), result);
                            }
                            @Override
                            public void onFailure(Throwable t) {
                                result.completeExceptionally(t);
                            }
                        }, executorService); // MoreExecutors.directExecutor() or executor?

                    } catch(Throwable t) {
                        result.completeExceptionally(t);
                    }
                });
        return result;
    }

    public CompletableFuture<Collection<Row>> executeQuery(String key) {
        CompletableFuture<Collection<Row>> result = new CompletableFuture<>();
        ResultSetFuture future = session.getDriverSession()
                .executeAsync(this.statement.bind(key).setConsistencyLevel(session.getConsistencyLevel()));
        Futures.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet rs) {
                fetchRowsAsync(rs, new ArrayList<>(), result);
            }
            @Override
            public void onFailure(Throwable t) {
                result.completeExceptionally(t);
            }
        }, executorService); // MoreExecutors.directExecutor() or executor?
        return result;
    }

    private void fetchRowsAsync(ResultSet rs, Collection<Row> alreadyFetched,
                                CompletableFuture<Collection<Row>> result) {
        int availableWithoutFetching = rs.getAvailableWithoutFetching();
        if (availableWithoutFetching == 0) {
            if (rs.isFullyFetched()) {
                result.complete(alreadyFetched);
            } else {
                Futures.addCallback(rs.fetchMoreResults(), new FutureCallback<ResultSet>() {
                    @Override
                    public void onSuccess(ResultSet rsNew) {
                        fetchRowsAsync(rsNew, alreadyFetched, result);
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        result.completeExceptionally(t);
                    }
                }, executorService);
            }
        } else {
            for (int i = 0; i < availableWithoutFetching; i++) {
                alreadyFetched.add(rs.one());
            }
            fetchRowsAsync(rs, alreadyFetched, result);
        }
    }
}
