package www.grpc.server;

import www.grpc.cql.CQLDriver;

public class Service {
    private final CQLDriver driver;

    public Service(CQLDriver driver) {
        this.driver = driver;
    }
}
