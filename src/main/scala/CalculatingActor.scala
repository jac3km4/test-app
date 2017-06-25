import akka.actor.{Actor, ActorRef, Props, SupervisorStrategy}

/**
  * Created by jacek on 26.06.17.
  */
object CalculatingActor {
  def props: Props = Props[CalculatingActor]

  sealed trait Result

  final case class Success(value: Double, sender: ActorRef) extends Result

  final case class Failure(reason: String) extends Result
}

final class CalculatingActor extends Actor {
  import context._

  override def supervisorStrategy: SupervisorStrategy =
    SupervisorStrategy.stoppingStrategy

  def handleFailure: Receive = {
    case fail: CalculatingActor.Failure =>
      parent ! fail
      stop(self)
  }

  override def receive: Receive = {
    case Lit(v) =>
      parent ! CalculatingActor.Success(v, self)
      stop(self)
    case Neg(v) =>
      val calc = actorOf(CalculatingActor.props)
      calc ! v
      become(awaitResult(calc, -_))
    case Add(l, r) =>
      become(runBinaryOp(l, r)(_ + _))
    case Sub(l, r) =>
      become(runBinaryOp(l, r)(_ - _))
    case Div(l, r) =>
      become(runBinaryOp(l, r) { (a, b) =>
        if (b == 0) {
          parent ! CalculatingActor.Failure("Division by zero")
          stop(self)
          0
        } else a / b
      })
    case Mul(l, r) =>
      become(runBinaryOp(l, r)(_ * _))
  }

  def runBinaryOp(left: Expr, right: Expr)(
      op: (Double, Double) => Double
  ): Receive = {
    val lcalc = actorOf(CalculatingActor.props)
    val rcalc = actorOf(CalculatingActor.props)
    lcalc ! left
    rcalc ! right
    awaitBinaryResults(lcalc, rcalc, op) orElse handleFailure
  }

  def awaitBinaryResults(
      left: ActorRef,
      right: ActorRef,
      op: (Double, Double) => Double
  ): Receive = {
    case CalculatingActor.Success(v, `left`) =>
      become {
        awaitResult(right, op(v, _)) orElse handleFailure
      }
    case CalculatingActor.Success(v, `right`) =>
      become {
        awaitResult(left, op(_, v)) orElse handleFailure
      }
  }

  def awaitResult(ref: ActorRef, op: Double => Double): Receive = {
    case CalculatingActor.Success(v, `ref`) =>
      parent ! CalculatingActor.Success(op(v), self)
      stop(self)
  }
}
