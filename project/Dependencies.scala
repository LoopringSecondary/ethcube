import sbt._

object Dependencies {

	
	lazy val slf4jVersion = "1.7.25"
  lazy val logbackVersion = "1.2.3"
	

	lazy val commonDependency = Seq(
		"org.slf4j" % "slf4j-api" % slf4jVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-access" % logbackVersion,
    "com.google.inject" % "guice" % "4.2.0",
    "net.codingwell" %% "scala-guice" % "4.2.1",
    "org.json4s" %% "json4s-native" % "3.5.4",
		"org.json4s" %% "json4s-jackson" % "3.5.4", 
		"de.heikoseeberger" %% "akka-http-json4s" % "1.21.0",
		"org.json4s" %% "json4s-native" % "3.5.4",
		"org.json4s" %% "json4s-jackson" % "3.5.4", 
		"org.json4s" %% "json4s-ext" % "3.5.4"
		// "org.web3j" % "core" % "3.4.0"
	)

	lazy val jsonRPCDependency = Seq(
		// "io.github.shogowada" %% "scala-json-rpc" % "0.9.3",
		"com.typesafe.akka" %% "akka-actor" % "2.5.14",
		"com.typesafe.akka" %% "akka-remote" % "2.5.14",
		"com.typesafe.akka" %% "akka-stream" % "2.5.14",
		"com.typesafe.akka" %% "akka-contrib" % "2.5.14",
		"com.typesafe.akka" %% "akka-cluster" % "2.5.14",
		"com.typesafe.akka" %% "akka-cluster-metrics" % "2.5.14",
		"com.typesafe.akka" %% "akka-http" % "10.1.3",
		// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
		
	)

}