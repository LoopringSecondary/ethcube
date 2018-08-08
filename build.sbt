
import sbt._
import Keys._
import Settings._
import Dependencies._

lazy val proto = (project in file("proto"))
  .settings(
    libraryDependencies ++= scalapbDependency,

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )


lazy val common = (project in file("common"))
  .dependsOn(proto)
  .settings(
    libraryDependencies ++= commonDependency
  )

lazy val worker = (project in file("worker"))
  .dependsOn(common)
  .settings(
    libraryDependencies ++= akkaDependency ++ socketDependency
  )


lazy val root = (project in file("root"))
  .dependsOn(common, worker)
  .settings(
    libraryDependencies ++= akkaDependency
  )

lazy val all = (project in file("."))
  .aggregate(proto, common, root, worker)
  .settings(
    basicSettings,
    update / aggregate := false
  )