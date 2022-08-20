package grpc.scala.server.cql

import akka.actor.ActorSystem
import akka.actor.typed.DispatcherSelector
import com.datastax.driver.core.{PreparedStatement, ResultSet, Row}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.typesafe.config.ConfigFactory
import www.grpc.proto.{Request, Response}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}

class CQLDriver(val cqlSession: CQLSession, system: ActorSystem) {
  implicit val executionContext = system.dispatchers.lookup("fork-join-dispatcher")

  lazy val selectQuery = "select value from demo.history where key = ?"
  val statement: PreparedStatement = cqlSession.session.prepare(selectQuery)

  def queryThenConvert(request: Request): Future[Response] = {
    val p = Promise[ResultSet]
    val start = System.nanoTime()
    val guavaFuture = cqlSession.session
      .executeAsync(statement.bind(request.key).setConsistencyLevel(cqlSession.getConsistencyLevel()))

    Futures.addCallback(guavaFuture, new FutureCallback[ResultSet] {
      def onFailure(t: Throwable) {
        p.failure(t)
      }

      def onSuccess(result: ResultSet) {
        p.success(result)
      }
    }, executionContext)

    p.future.flatMap { rs =>
      val alreadyFetched = ListBuffer.empty[Row]
      val p = Promise[Seq[Row]]
      fetchAllRecords(rs, alreadyFetched, p).future.map(convertToResponse(_, request, start))
    }
  }

  def fetchAllRecords(rs: ResultSet, alreadyFetched: ListBuffer[Row], p: Promise[Seq[Row]]): Promise[Seq[Row]] = {
    val availableWithoutFetching: Int = rs.getAvailableWithoutFetching
    if (availableWithoutFetching > 0) {
      val result = ListBuffer.empty[Row]
      for (_ <- 0 until availableWithoutFetching) {
        result.addOne(rs.one())
      }
      p.success(result.toList)
    } else {
      if (rs.isFullyFetched) {
        p.success(alreadyFetched.toList)
      } else {
        Futures.addCallback(rs.fetchMoreResults(), new FutureCallback[ResultSet]() {
          override def onSuccess(rsNew: ResultSet): Unit = {
            fetchAllRecords(rsNew, alreadyFetched, p)
          }
          override def onFailure(t: Throwable): Unit = {
            p.failure(t)
          }
        }, executionContext)
      }
      p
    }
  }

  def convertToResponse(rows: Seq[Row], request: Request, start: Long): Response = {
    val delay = (System.nanoTime() - start) / 1000000
    new Response()
      .withStart(request.start)
      .addAllValues(rows.map(r => r.getString(0)))
      .addMetrics(request.uuid)
      .addMetrics(String.valueOf(delay))
  }
}
