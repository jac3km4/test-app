/**
  * Created by jacek on 26.06.17.
  */
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import fastparse.core.Parsed
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.io.StdIn
import akka.pattern.ask

import scala.concurrent.duration._

object Server {

  final case class Expression(expression: String)

  final case class Response(result: Either[String, Double])

  private implicit val system = ActorSystem("parallel-calculator")
  private implicit val materializer = ActorMaterializer()
  private implicit val executionContext = system.dispatcher

  private val expressionActor = system.actorOf(ExpressionActor.props)

  val route: Route =
    path("evaluate") {
      post {
        entity(as[Expression]) { expr =>
          val result =
            parse(expr.expression) match {
              case Parsed.Success(v, i) if i == expr.expression.length =>
                expressionActor
                  .?(v)(10.seconds)
                  .mapTo[ExpressionActor.Result]
                  .map(_.toEither)
              case Parsed.Success(_, _) =>
                Future.successful(Left("Couldn't parse the entire expression"))
              case Parsed.Failure(_, i, _) =>
                Future.successful(Left(s"Failed to parse at index $i"))
            }

          complete(result.map(Response))
        }
      }
    }

  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(
      s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
