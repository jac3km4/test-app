name := "parallel-calc"

version := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xlint",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-opt-warnings",
  "-unchecked",
  "-opt:l:classpath"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.8",
  "de.heikoseeberger" %% "akka-http-circe" % "1.17.0",
  "io.circe" %% "circe-generic" % "0.8.0",
  "com.lihaoyi" %% "fastparse" % "0.4.3",
  // test
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.8" % Test
)
