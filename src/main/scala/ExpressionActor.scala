import akka.actor.{Actor, ActorRef, Props}

/**
  * Created by jacek on 26.06.17.
  */
object ExpressionActor {
  def props: Props = Props[ExpressionActor]

  sealed trait Result {
    def toEither: Either[String, Double]
  }

  final case class Success(value: Double) extends Result {
    override def toEither: Either[String, Double] = Right(value)
  }

  final case class Failure(reason: String) extends Result {
    override def toEither: Either[String, Double] = Left(reason)
  }
}

final class ExpressionActor extends Actor {
  import context._

  override def receive: Receive = {
    case expr: Expr =>
      val calc = actorOf(CalculatingActor.props)
      calc ! expr
      become(calculating(sender(), calc), discardOld = false)
  }

  def calculating(sender: ActorRef, calc: ActorRef): Receive = {
    case CalculatingActor.Success(v, `calc`) =>
      sender ! ExpressionActor.Success(v)
      unbecome()
    case CalculatingActor.Failure(reason) =>
      sender ! ExpressionActor.Failure(reason)
      unbecome()
  }
}
