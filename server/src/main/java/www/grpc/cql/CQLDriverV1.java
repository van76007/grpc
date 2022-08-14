package www.grpc.cql;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import www.grpc.concurrent.ConcurrencyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * To execute this function
 * session.getDriverSession().executeAsync(statement.bind("").setConsistencyLevel(session.getConsistencyLevel()))
 */
public class CQLDriverV1 {
    private final CQLSession session;
    private PreparedStatement statement;
    private final String selectQuery = "select value from keyspace where key = ?";

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // private Executor executor = MoreExecutors.directExecutor();

    public CQLDriverV1(CQLSession session) {
        this.session = session;
        prepareStatement();
    }

    public void stop() {
        session.close();
        ConcurrencyUtils.shutdownAndAwaitTermination(executor);
    }

    /**
     * Usage: The entire session runs only 1 statement
     */
    public void prepareStatement() {
        this.statement = session.getDriverSession().prepare(selectQuery);
    }

    /**
     * Usage: Each request might be a new statement.
     *        Chain futures: Create prepared statment -> execute it async
     * @param query
     * @return
     */
    public CompletableFuture<PreparedStatement> prepareStatementAsync(String query) {
        return ConcurrencyUtils.convertToCompletableFuture(session.getDriverSession().prepareAsync(query), executor);
    }

    public CompletableFuture<Collection<Row>> executeQuery(String key) {
        CompletableFuture<Collection<Row>> result = new CompletableFuture<>();
        ResultSetFuture future = session.getDriverSession()
                .executeAsync(this.statement.bind(key).setConsistencyLevel(session.getConsistencyLevel()));
        // TODO: Try MoreExecutors.directExecutor()?
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
                // TODO: Not to use MoreExecutors.directExecutor()?
                Futures.addCallback(rs.fetchMoreResults(), new FutureCallback<ResultSet>() {
                    @Override
                    public void onSuccess(ResultSet rsNew) {
                        fetchRowsAsync(rsNew, alreadyFetched, result);
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        result.completeExceptionally(t);
                    }
                }, MoreExecutors.directExecutor());
            }
        } else {
            for (int i = 0; i < availableWithoutFetching; i++) {
                alreadyFetched.add(rs.one());
            }
            fetchRowsAsync(rs, alreadyFetched, result);
        }
    }
}
