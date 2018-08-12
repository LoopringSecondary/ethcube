import sbt._
import Keys._
import sbtdocker._


object Settings {

	lazy val basicSettings: Seq[Setting[_]] = Seq(
    name := "eth",
    organization := Globals.organization,
    version := Globals.version,
    scalaVersion := Globals.scalaVersion,
    autoScalaLibrary := false,
    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
    // resolvers ++= Resolvers.repositories,
    javacOptions := Seq( //"-source", Globals.jvmVersion,
    //"-target", Globals.jvmVersion
    ),
    scalacOptions := Seq(
      "-encoding", "utf8",
      "-g:vars",
      "-unchecked",
      "-deprecation",
      "-Yresolve-term-conflict:package"),
    fork in Test := false,
    parallelExecution in Test := false,
    // publishArtifact in (Compile, packageSrc) := false,
    // publishArtifact in (Compile, packageDoc) := false,
    shellPrompt in ThisBuild := { state => "sbt (%s)> ".format(Project.extract(state).currentProject.id) })


  // lazy val dockerSettings: Seq[Setting[_]] = Seq(
  //   dockerfile in Docker := {
  //     val appDir = stage.value
  //     val targetDir = "/app"

  //     new Dockerfile {
  //       from("openjdk:8-jre")
  //       entryPoint(s"$targetDir/bin/${executableScriptName.value}")
  //       copy(appDir, targetDir)
  //     }
  //   }, 
  //   buildOptions in Docker := BuildOptions(cache = false))


}