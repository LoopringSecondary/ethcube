
import sbt._
import Keys._
import Settings._
import Dependencies._

lazy val proto = (project in file("proto"))
  // .enablePlugins(AkkaGrpcPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
      // "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion

    ),
    // libraryDependencies ++= commonDependency
    // other settings

    // enablePlugins(AkkaGrpcPlugin)
    // baseDirectory.value

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )

    // PB.protoSources in Compile := Seq(
    //   // baseDirectory.value
    //   baseDirectory.value
    // ),
    // PB.targets in Compile := Seq(
    //   scalapb.gen() -> (sourceManaged in Compile).value
    // )

    // scalapb.gen() -> (sourceManaged in Compile).value

    // PB.protoSources in Compile := {
    //   println(sourceDirectory.value)
    //   Seq(
    //   // baseDirectory.value
    //     sourceDirectory.value / "main" / "proto"
    //   )
    // },
    // PB.targets in Compile := Seq(
    //   scalapb.gen() -> (sourceManaged in Compile).value
    // )

    // PB.targets in Compile := Seq(
    //   scalapb.gen()       -> (sourceManaged in Compile).value,
    //   RpcLibCodeGenerator -> (sourceManaged in Compile).value
    // )
  )


lazy val common = (project in file("common"))
  .dependsOn(proto)
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
  .aggregate(proto, common, root, worker)
  .settings(
    basicSettings,
    update / aggregate := false
    // other settings
  )