import sbt._
import Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.MappingsHelper._

// import com.typesafe.sbt.SbtNativePackager.autoImport._

object Settings {

	lazy val basicSettings: Seq[Setting[_]] = Seq(
    name := Globals.name,
    organization := Globals.organization,
    version := Globals.version,
    scalaVersion := Globals.scalaVersion,
    autoScalaLibrary := false,
    // resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
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



  lazy val packageSettings: Seq[Setting[_]] = Seq(

    mappings in (Compile, packageBin) ~= {
      _.filterNot { case (file, _) => 
        (file.getName.endsWith("conf") && file.getName != "application.conf" && file.getName != "docker.conf") }
    },

    mappings in Universal += {
      val configFile = sys.props.getOrElse("env", default = "dev") + ".conf"
      ((resourceDirectory in Compile).value / configFile) -> s"conf/${configFile}"
    },

    mappings in Universal += {
      ((resourceDirectory in Compile).value / "logback.xml") -> "conf/logback.xml"
    },

    bashScriptExtraDefines ++= Seq("""addJava "-Xmx16G"""")

  )

  lazy val dockerSettings: Seq[Setting[_]] = Seq(

    dockerExposedPorts := Seq(9000),
    // 设置环境变量
    dockerEnvVars := Map(
      "env" -> sys.props.getOrElse("env", default = "docker")
    )
    
  )

}