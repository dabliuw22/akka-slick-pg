name := "akka-slick-pg"

version := "0.1"

scalaVersion := "2.13.1"

val akkaParent = "com.typesafe.akka"
val akkaVersion = "2.5.23"
val lightbendParent = "com.lightbend.akka"
val lightbendVersion = "1.1.2"
val postgreSqlParent = "org.postgresql"
val postgreSqlVersion = "9.4.1212"
val scalaLoggingParent = "com.typesafe.scala-logging"
val scalaLoggingVersion = "3.9.2"
val logbackParent = "ch.qos.logback"
val logbackVersion = "1.2.3"
val scalaTestParent = "org.scalatest"
val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  akkaParent %% "akka-stream" % akkaVersion,
  lightbendParent %% "akka-stream-alpakka-slick" % lightbendVersion,
  postgreSqlParent % "postgresql" % postgreSqlVersion,
  scalaLoggingParent %% "scala-logging" % scalaLoggingVersion,
  logbackParent % "logback-classic" % logbackVersion,

  scalaTestParent %% "scalatest" % scalaTestVersion % Test,
  akkaParent %% "akka-stream-testkit" % akkaVersion % Test,
  akkaParent %% "akka-testkit" % akkaVersion % Test
)
