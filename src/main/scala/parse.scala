/**
  * Created by jacek on 26.06.17.
  */
import fastparse.all._
import fastparse.core

import scala.annotation.tailrec

object parse {

  private def parseBinOp(l: Expr, op: String, r: Expr): Expr =
    op match {
      case "+" => Add(l, r)
      case "-" => Sub(l, r)
      case "/" => Div(l, r)
      case "*" => Mul(l, r)
    }

  @tailrec private def groupExprs(l: Expr, rem: List[(String, Expr)]): Expr =
    rem match {
      case ((op, r) :: xs) =>
        groupExprs(parseBinOp(l, op, r), xs)
      case Nil => l
    }

  val num: P[Expr] =
    P(CharIn('0' to '9').rep(1).! ~ ("." ~ CharIn('0' to '9').rep(1)).!.?)
      .map {
        case (i, r) =>
          val dec =
            if (i.isEmpty) 0
            else i.toInt
          Lit(dec + r.map(_.toDouble).getOrElse(0.0))
      }

  val parens: P[Expr] = P("(" ~/ lowPrioOp ~ ")")

  val factor: P[Expr] = P(parens | num)

  val neg: P[Expr] = P("-".!.? ~ factor)
    .map {
      case (Some("-"), e) => Neg(e)
      case (_, e) => e
    }

  val highPrioOp: P[Expr] = P(neg ~ (CharIn("*/").! ~/ neg).rep.map(_.toList))
    .map(Function.tupled(groupExprs))

  val lowPrioOp: P[Expr] =
    P(highPrioOp ~ (CharIn("+-").! ~/ highPrioOp).rep.map(_.toList))
      .map(Function.tupled(groupExprs))

  val expr: P[Expr] = P(lowPrioOp ~ End)

  def apply(str: String): core.Parsed[Expr, Char, String] =
    expr.parse(str)
}
