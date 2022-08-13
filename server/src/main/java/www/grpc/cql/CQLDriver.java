package www.grpc.cql;

import com.datastax.driver.core.PreparedStatement;

public class CQLDriver {
    private final CQLSession session;
    private PreparedStatement statement;

    public CQLDriver(CQLSession session) {
        this.session = session;
    }

    public void prepareStatement() {
        // Blocking build statement
    }

    public void prepareStatementAsync() {
        // Async build statement
    }
}
