import sbt._
import Keys._
import Settings._
import Dependencies._

lazy val proto = (project in file("proto"))
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.github.jnr" % "jnr-unixsocket" % "0.18"),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value))

lazy val common = (project in file("common"))
  .dependsOn(proto)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    autoHeaderSettings,
    libraryDependencies ++= commonDependency)

lazy val worker = (project in file("worker"))
  .dependsOn(common)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    autoHeaderSettings,
    libraryDependencies ++= akkaDenepdencies)

lazy val root = (project in file("root"))
  .dependsOn(common)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    autoHeaderSettings,
    libraryDependencies ++= akkaDenepdencies)

lazy val ethcube = (project in file("."))
  .aggregate(root, worker)
  .settings(
    basicSettings,
    update / aggregate := false)