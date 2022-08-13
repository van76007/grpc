package www.grpc.cql;

/**
 * The consistency level of read queries to Scylla.
 */
public enum ConsistencyLevel {
    /**
     * Waits for a response from single replica
     * in the local data center.
     */
    LOCAL_ONE,
    /**
     * Waits for a response from single replica.
     */
    ONE,
    /**
     * Waits for responses from two replicas.
     */
    TWO,
    /**
     * Waits for responses from three replicas.
     */
    THREE,
    /**
     * Waits for responses from a quorum of replicas
     * in the same DC as the coordinator.
     * <p>
     * Local quorum is defined as:
     * <code>dataCenterReplicationFactor / 2 + 1</code>,
     * where <code>dataCenterReplicationFactor</code> is the
     * configured replication factor for the datacenter of
     * the coordinator node.
     */
    LOCAL_QUORUM,
    /**
     * Waits for responses from a quorum of replicas.
     * <p>
     * Quorum is defined as:
     * <code>(dc1ReplicationFactor + dc2ReplicationFactor + ...) / 2 + 1</code>,
     * where <code>dc1ReplicationFactor</code>, <code>dc2ReplicationFactor</code>, ... are the configured
     * replication factors for all data centers.
     */
    QUORUM,
    /**
     * Waits for responses from all replicas.
     */
    ALL
}
