
import sbt._
import Keys._
import Settings._
import Dependencies._

lazy val proto = (project in file("proto"))
  .settings(
    // libraryDependencies ++= commonDependency
    // other settings

    // enablePlugins(AkkaGrpcPlugin)
    // baseDirectory.value

    // inConfig(Compile)(Seq(
    //   PB.protoSources += baseDirectory.value / "protobuf"
    // ))
  )


lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= commonDependency
    // other settings
  )

lazy val worker = (project in file("worker"))
  .dependsOn(common)
  .settings(
    libraryDependencies ++= jsonRPCDependency
    // other settings
  )


lazy val root = (project in file("root"))
  .dependsOn(common)
  .settings(
    libraryDependencies ++= jsonRPCDependency
    // other settings
  )


// lazy val all = Project(id = "hello", base = file("."))
//   .aggregate(root, worker, common)
//   .settings(
//     basicSettings,
//     update / aggregate := false
//     // other settings
//   )

lazy val all = (project in file("."))
  .aggregate(root, worker, common)
  .settings(
    basicSettings,
    update / aggregate := false
    // other settings
  )