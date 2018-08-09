
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.5")

// addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.7.4"

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")