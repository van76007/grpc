package www.grpc.cql;

import com.google.common.base.Preconditions;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CQLConfiguration {
    private static final int DEFAULT_PORT = 9042;
    private static final ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = ConsistencyLevel.QUORUM;

    public final List<InetSocketAddress> contactPoints;
    public final String user;
    public final String password;
    private final ConsistencyLevel consistencyLevel;
    private final String localDCName;

    private CQLConfiguration(List<InetSocketAddress> contactPoints,
                             String user, String password, ConsistencyLevel consistencyLevel,
                             String localDCName) {
        this.contactPoints = Preconditions.checkNotNull(contactPoints);
        Preconditions.checkArgument(!contactPoints.isEmpty());

        this.user = user;
        this.password = password;
        // Either someone did not provide credentials
        // or provided user-password pair.
        Preconditions.checkArgument((this.user == null && this.password == null)
                || (this.user != null && this.password != null));

        this.consistencyLevel = Preconditions.checkNotNull(consistencyLevel);
        this.localDCName = localDCName;
    }

    /**
     * Returns the configured consistency level.
     * <p>
     * This consistency level is used in read queries to the
     * CDC log table. The queries to system tables, such
     * as <code>system_distributed.cdc_streams_descriptions_v2</code> do
     * not respect this configuration option.
     *
     * @return configured consistency level.
     */
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Returns the name of the configured local datacenter.
     * <p>
     * This local datacenter name will be used to setup
     * the connection to Scylla to prioritize sending requests to
     * the nodes in the local datacenter. If this parameter
     * was not configured, this method returns <code>null</code>.
     *
     * @return the name of configured local datacenter or
     *         <code>null</code> if it was not configured.
     */
    public String getLocalDCName() {
        return localDCName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<InetSocketAddress> contactPoints = new ArrayList<>();
        private String user = null;
        private String password = null;
        private ConsistencyLevel consistencyLevel = DEFAULT_CONSISTENCY_LEVEL;
        private String localDCName = null;

        public Builder addContactPoint(InetSocketAddress contactPoint) {
            Preconditions.checkNotNull(contactPoint);
            contactPoints.add(contactPoint);
            return this;
        }

        public Builder addContactPoints(Collection<InetSocketAddress> addedContactPoints) {
            for (InetSocketAddress contactPoint : addedContactPoints) {
                this.addContactPoint(contactPoint);
            }
            return this;
        }

        public Builder addContactPoint(String host, int port) {
            Preconditions.checkNotNull(host);
            Preconditions.checkArgument(port > 0 && port < 65536);
            return addContactPoint(new InetSocketAddress(host, port));
        }

        public Builder addContactPoint(String host) {
            return addContactPoint(host, DEFAULT_PORT);
        }

        public Builder withCredentials(String user, String password) {
            this.user = Preconditions.checkNotNull(user);
            this.password = Preconditions.checkNotNull(password);
            return this;
        }

        /**
         * Sets the consistency level of CDC table read queries.
         * <p>
         * This consistency level is used only for read queries
         * to the CDC log table. The queries to system tables, such
         * as <code>system_distributed.cdc_streams_descriptions_v2</code> do
         * not respect this configuration option.
         *
         * @param consistencyLevel consistency level to set.
         * @return a reference to this builder.
         */
        public Builder withConsistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = Preconditions.checkNotNull(consistencyLevel);
            return this;
        }

        /**
         * Sets the name of local datacenter.
         * <p>
         * This local datacenter name will be used to setup
         * the connection to Scylla to prioritize sending requests to
         * the nodes in the local datacenter.
         *
         * @param localDCName the name of local datacenter to set.
         * @return a reference to this builder.
         */
        public Builder withLocalDCName(String localDCName) {
            this.localDCName = Preconditions.checkNotNull(localDCName);
            return this;
        }

        public CQLConfiguration build() {
            return new CQLConfiguration(contactPoints, user, password, consistencyLevel, localDCName);
        }
    }
}
