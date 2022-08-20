package grpc.scala.server.cql

import grpc.scala.server.cql.ConsistentLevel.ConsistentLevel

import java.net.InetSocketAddress

object ConsistentLevel extends Enumeration {
  type ConsistentLevel = Value
  val LOCAL_ONE, ONE, LOCAL_QUORUM, QUORUM, ALL = Value
}

case class CQLConfiguration(
                           port: Int = 9042,
                           contactPoints: List[InetSocketAddress],
                           user: String,
                           password: String,
                           consistencyLevel: ConsistentLevel
                           ) {
}
