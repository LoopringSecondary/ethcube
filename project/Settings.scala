import sbt._
import Keys._

import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
// import com.typesafe.sbt.packager.Keys._
// import sbtdocker._
// import sbtdocker.DockerKeys._


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


  lazy val dockerSettings: Seq[Setting[_]] = Seq(
    dockerBaseImage := "openjdk"
    // packageName in Docker := 
    
    // dockerfile in docker := {
    //   val appDir = stage.value
    //   val targetDir = "/app"

    //   new Dockerfile {
    //     from("java:8u111-jre") // 这里还是默认的openjdk 不知道为什么
    //     entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    //     expose(20552)
    //     // container(containerId)
    //     copy(appDir, targetDir, chown = "daemon:daemon")
    //   }
    // }

  )


}