import sbt._

object Dependencies {

  lazy val logbackVersion = "1.2.3"

  lazy val json4sVersion = "3.5.4"

	lazy val commonDependency = Seq(
		"org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-access" % logbackVersion,
    "com.google.inject" % "guice" % "4.2.0",
    "net.codingwell" %% "scala-guice" % "4.2.1",
    "org.json4s" %% "json4s-native" % json4sVersion,
		"org.json4s" %% "json4s-jackson" % json4sVersion, 
		"org.json4s" %% "json4s-ext" % json4sVersion,
		"de.heikoseeberger" %% "akka-http-json4s" % "1.21.0"
	
	)

	lazy val akkaVersion = "2.5.14"

	lazy val akkaDependency = Seq(
		// "io.github.shogowada" %% "scala-json-rpc" % "0.9.3",
		"com.typesafe.akka" %% "akka-actor" % akkaVersion,
		"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
		"com.typesafe.akka" %% "akka-remote" % akkaVersion,
		"com.typesafe.akka" %% "akka-stream" % akkaVersion,
		"com.typesafe.akka" %% "akka-contrib" % akkaVersion,
		"com.typesafe.akka" %% "akka-cluster" % akkaVersion,
		"com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
		"com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
		"com.typesafe.akka" %% "akka-http" % "10.1.3"
	)

	lazy val socketDependency = Seq(
		"com.github.jnr" % "jnr-unixsocket" % "0.19"
	)


	lazy val scalapbDependency = Seq(
		"com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
	)
}	