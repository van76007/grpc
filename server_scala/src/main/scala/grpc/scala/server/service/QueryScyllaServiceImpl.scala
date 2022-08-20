package grpc.scala.server.service

import akka.actor.typed.ActorSystem
import grpc.scala.server.cql.{CQLConfiguration, CQLDriver}
import www.grpc.proto.{QueryScylla, Request, Response}

import scala.concurrent.Future

class QueryScyllaServiceImpl(driver: CQLDriver, system: ActorSystem[_]) extends QueryScylla {
  private implicit val sys: ActorSystem[_] = system

  override def executeQuery(in: Request): Future[Response] = {
    driver.queryThenConvert(in)
  }
}
