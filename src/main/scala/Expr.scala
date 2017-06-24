/**
  * Created by jacek on 26.06.17.
  */
sealed trait Expr

final case class Lit(value: Double) extends Expr

final case class Add(l: Expr, r: Expr) extends Expr

final case class Sub(l: Expr, r: Expr) extends Expr

final case class Div(l: Expr, r: Expr) extends Expr

final case class Mul(l: Expr, r: Expr) extends Expr

final case class Neg(e: Expr) extends Expr