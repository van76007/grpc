package grpc.scala.server.cql

import com.datastax.driver.core._
import grpc.scala.server.cql.ConsistentLevel.{ALL, LOCAL_ONE, LOCAL_QUORUM, ONE, QUORUM}

import java.net.InetSocketAddress
import java.util

class CQLSession(val cqlConfiguration: CQLConfiguration, poolingOptions: PoolingOptions) {
  val session: Session = {
    val clusterBuilder = Cluster.builder
      .withProtocolVersion(ProtocolVersion.V4)
      .withCredentials(cqlConfiguration.user, cqlConfiguration.password)
      .withPoolingOptions(poolingOptions)
      // TODO: Pass from cqlConfiguration
      .addContactPointsWithPorts(util.Arrays.asList(
        new InetSocketAddress("localhost", 9042),
        new InetSocketAddress("localhost", 9043),
        new InetSocketAddress("localhost", 9044)))

    val driverCluster = clusterBuilder.build();
    driverCluster.connect()
  }

  def getConsistencyLevel() = {
    cqlConfiguration.consistencyLevel match {
      case LOCAL_ONE => ConsistencyLevel.LOCAL_ONE
      case ONE => ConsistencyLevel.ONE
      case QUORUM => ConsistencyLevel.QUORUM
      case LOCAL_QUORUM => ConsistencyLevel.LOCAL_QUORUM
      case ALL => ConsistencyLevel.ALL
    }
  }
}
