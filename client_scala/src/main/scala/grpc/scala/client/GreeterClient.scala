package grpc.scala.client
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import www.grpc.proto.{QueryScyllaClient, Request}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object GreeterClient {
  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("GrpcClient")
    implicit val ec = sys.dispatcher

    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8090).withTls(false)
    val client: QueryScyllaClient = QueryScyllaClient(clientSettings)

    runSingleRequestReplyExample()

    def runSingleRequestReplyExample(): Unit = {
      sys.log.info("Performing request")
      val reply = client.executeQuery(Request(key = "0", start = System.nanoTime(), uuid = java.util.UUID.randomUUID.toString))
      reply.onComplete {
        case Success(msg) =>
          println(s"got single reply: $msg")
        case Failure(e) =>
          println(s"Error sayHello: $e")
      }
    }
  }
}
