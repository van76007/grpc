package grpc.scala.server

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.pki.pem.{DERPrivateKeyLoader, PEMDecoder}
import com.datastax.driver.core.{HostDistance, PoolingOptions}
import com.typesafe.config.ConfigFactory
import grpc.scala.server.cql.{CQLConfiguration, CQLDriver, CQLSession, ConsistentLevel}
import grpc.scala.server.service.QueryScyllaServiceImpl
import www.grpc.proto.QueryScyllaHandler

import java.net.InetSocketAddress
import java.security.cert.{Certificate, CertificateFactory}
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object GreeterServer {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = akka.actor.typed.ActorSystem[Nothing](akka.actor.typed.javadsl.Behaviors.empty, "GreeterServer", conf)
    new GreeterServer(system).run()
  }
}

class GreeterServer(system: akka.actor.typed.ActorSystem[_]) {
  implicit val sys = system
  implicit val ec: ExecutionContext = system.executionContext
  // If test client from the same machine as server, use "127.0.0.1"
  // If test client from the different machine, use IP e.g. ifconfig | grep eth0 "192.168.1.34"
  val SERVER_IP = "192.168.1.34"

  def buildDriver(system: akka.actor.ActorSystem) = {
    val config = CQLConfiguration(
      contactPoints = List(
        new InetSocketAddress("127.0.0.1", 9042),
        new InetSocketAddress("127.0.0.1", 9043),
        new InetSocketAddress("127.0.0.1", 9044)),
      user = "cassandra",
      password = "cassandra",
      consistencyLevel = ConsistentLevel.ONE)
    // Optimize Scylla performance
    val poolingOptions = new PoolingOptions()
      .setMaxQueueSize(2048) // To fix error: Pool is busy (no available connection and queue reach its max size 256)
      .setCoreConnectionsPerHost(HostDistance.LOCAL, 4)
      .setMaxConnectionsPerHost(HostDistance.LOCAL, 4)
      .setNewConnectionThreshold(HostDistance.LOCAL,1024)
      .setPoolTimeoutMillis(5000)
    // val poolingOptions = new PoolingOptions()

    val session = new CQLSession(config, poolingOptions)
    new CQLDriver(session, system)
  }

  def run(): Future[Http.ServerBinding] = {
    val ss = akka.actor.ActorSystem("ActorSystem")
    val driver = buildDriver(ss)
    val service: HttpRequest => Future[HttpResponse] =
      QueryScyllaHandler.withServerReflection(new QueryScyllaServiceImpl(driver, system))

    val bound: Future[Http.ServerBinding] = Http(system)
      .newServerAt(interface = SERVER_IP, port = 8090)
      // .enableHttps(serverHttpContext)
      .bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println("gRPC server bound to {}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        println("Failed to bind gRPC endpoint, terminating system", ex)
        system.terminate()
    }

    bound
  }
  /*
  private def serverHttpContext: HttpsConnectionContext = {
    val privateKey =
      DERPrivateKeyLoader.load(PEMDecoder.decode(readPrivateKeyPem()))
    val fact = CertificateFactory.getInstance("X.509")
    val cer = fact.generateCertificate(
      classOf[GreeterServer].getResourceAsStream("/certs/server1.pem")
    )
    val ks = KeyStore.getInstance("PKCS12")
    ks.load(null)
    ks.setKeyEntry(
      "private",
      privateKey,
      new Array[Char](0),
      Array[Certificate](cer)
    )
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, null)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
    ConnectionContext.https(context)
  }

  private def readPrivateKeyPem(): String = Source.fromResource("certs/server1.key").mkString
   */
}
