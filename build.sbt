
import sbt._
import sbt.Keys._
import Settings._
import Dependencies._


lazy val all = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    basicSettings,

    packageSettings,

    dockerSettings,

    libraryDependencies ++= commonDependency ++ akkaDependency ++ socketDependency
    // update / aggregate := false
  )

