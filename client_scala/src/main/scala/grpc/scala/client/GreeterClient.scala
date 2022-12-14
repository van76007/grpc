package grpc.scala.client
import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, Cancellable}
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import www.grpc.proto.{QueryScyllaClient, Request}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object GreeterClient {
  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("GrpcClient")
    implicit val ec = sys.dispatcher
    val NUM_CONCURRENT_REQ = 200 // 200 is too much


    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8090).withTls(false)
    val client: QueryScyllaClient = QueryScyllaClient(clientSettings)

    runV3()

    def runV1(): Unit = {
      sys.scheduler.scheduleWithFixedDelay(1.milliseconds, 1.milliseconds) { () => {
          sys.log.info("Performing request")
          val reply = client.executeQuery(Request(key = "0", start = System.nanoTime(), uuid = java.util.UUID.randomUUID.toString))
          reply.onComplete {
            case Success(resp) =>
              val delay = (System.nanoTime() - resp.start) / 1000000
              println(s"got single reply after $delay ms")
            case Failure(e) =>
              println(s"Error sayHello: $e")
          }
        }
      }
    }

    def generateLoad() = Seq.fill(NUM_CONCURRENT_REQ)(Random.nextInt(2))

    def runV2(): Unit = {
      sys.scheduler.scheduleWithFixedDelay(1.milliseconds, 1.milliseconds) { () => {
        Future.sequence(generateLoad().map { k =>
          val req = Request(key = s"$k", start = System.nanoTime(), uuid = java.util.UUID.randomUUID.toString)
          client.executeQuery(
            req
          ).transformWith {
            case Success(resp) =>
              val delay = (System.nanoTime() - resp.start) / 1000000
              println(s"after $delay ms got reply")
              Future(resp)
            case Failure(e) =>
              e.printStackTrace()
              val delay = (System.nanoTime() - req.start) / 1000000
              println(s"after $delay ms got error: $e")
              Future.failed(e)
          }
        })
      }}
    }

    def runV3(): Unit = {
      val DURATION_SECONDS = 30
      val rpcOK = new RpcCounter()
      val rpcKO = new RpcCounter()

      val scheduleRepeatedly: Cancellable = sys.scheduler.scheduleWithFixedDelay(1.milliseconds, 1.milliseconds) { () => {
        Future.sequence(generateLoad().map { k =>
          val req = Request(key = s"$k", start = System.nanoTime(), uuid = java.util.UUID.randomUUID.toString)
          client.executeQuery(
            req
          ).transformWith {
            case Success(resp) =>
              val delay = (System.nanoTime() - resp.start) / 1000000
              println(s"after $delay ms got reply")
              rpcOK.inc()
              Future(resp)
            case Failure(e) =>
              e.printStackTrace()
              val delay = (System.nanoTime() - req.start) / 1000000
              println(s"after $delay ms got error: $e")
              rpcKO.inc()
              Future.failed(e)
          }
        })
      }}

      sys.scheduler.scheduleOnce(DURATION_SECONDS seconds) {
        scheduleRepeatedly.cancel() // cancel repeated schedule
        println(s"DONE RPC = ${(rpcOK.get() + rpcKO.get()) / DURATION_SECONDS} rps")
        println(s"BAD VS GOOD = ${rpcKO.get() * 100 / (rpcOK.get() + rpcKO.get())}")
      }
    }
  }
}
