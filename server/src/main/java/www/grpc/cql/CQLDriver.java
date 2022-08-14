package www.grpc.cql;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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

    private ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // private Executor executor = MoreExecutors.directExecutor();

    public CQLDriver(CQLSession session) {
        this.session = session;
        prepareStatement();
        System.out.println("Done prepare statement");
    }

    public void stop() {
        session.close();
        ConcurrencyUtils.shutdownAndAwaitTermination(executorService);
        ConcurrencyUtils.shutdownAndAwaitTermination(executor);
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
        return ConcurrencyUtils.convertToCompletableFuture(session.getDriverSession().prepareAsync(query), executor);
    }

    public CompletableFuture<Response> executeQueryOnExecutorThenConvert(Request request) {
        return executeQueryOnExecutor(request.getKey()).thenApply(o ->
                Scyllaquery.Response.newBuilder()
                        .addAllValues(o.stream().map(r -> r.getString(0)).collect(toCollection(ArrayList::new)))
                        .setStart(request.getStart())
                        .build()

        );
    }

    protected CompletableFuture<Collection<Row>> executeQueryOnExecutor(String key) {
        CompletableFuture<Collection<Row>> result = new CompletableFuture<>();
        executorService.submit(
            () -> {
                try {
                    ResultSet rs = session.getDriverSession()
                            .execute(this.statement.bind(key).setConsistencyLevel(session.getConsistencyLevel()));
                    /*
                    Collection<Row> alreadyFetched = new ArrayList<>();
                    for (int i = 0; i < rs.getAvailableWithoutFetching(); i++) {
                        alreadyFetched.add(rs.one());
                    }
                    result.complete(alreadyFetched);
                     */
                    /*
                    Collection<Row> alreadyFetched = new ArrayList<>();
                    for (Row r : rs) {
                        alreadyFetched.add(r);
                    }
                    result.complete(alreadyFetched);
                     */
                    fetchRowsAsync(rs, new ArrayList<>(), result);
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
        // TODO: MoreExecutors.directExecutor() or executor or custom thread pool
        Futures.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet rs) {
                fetchRowsAsync(rs, new ArrayList<>(), result);
            }
            @Override
            public void onFailure(Throwable t) {
                result.completeExceptionally(t);
            }
        }, executor);
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
                }, executor);
            }
        } else {
            for (int i = 0; i < availableWithoutFetching; i++) {
                alreadyFetched.add(rs.one());
            }
            fetchRowsAsync(rs, alreadyFetched, result);
        }
    }
}
