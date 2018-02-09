name := "cryptocheck"

version := "0.1"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.8"
val akkaHttpVersion = "10.1.0-RC1"
val scalaTestV = "3.0.4"
val circeVersion = "0.9.1"
val akkaHttpJson = "1.20.0-RC1"
val catsVersion = "1.0.1"



libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,

  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "org.scalatest" %% "scalatest" % scalaTestV % Test,

  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJson,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.typelevel" %% "cats-core" % catsVersion
)

resolvers += Resolver.bintrayRepo("hseeberger", "maven")
