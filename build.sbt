
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


  // lazy val dockerSettings: Seq[Setting[_]] = Seq(
    // dockerfile in Docker := {
    //   val appDir = stage.value
    //   val targetDir = "/app"

  //     new Dockerfile {
  //       from("openjdk:8-jre")
  //       entryPoint(s"$targetDir/bin/${executableScriptName.value}")
  //       copy(appDir, targetDir)
  //     }
  //   }, 
  //   buildOptions in Docker := BuildOptions(cache = false))


lazy val worker = (project in file("worker"))
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .dependsOn(common)
  .settings(
    // dockerSettings,
    dockerfile in Docker := {

      val appDir = stage.value
      val targetDir = "/app"

      new Dockerfile {
        from("java:8u111-jre")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir)
      }
    },
    libraryDependencies ++= akkaDependency ++ socketDependency
  )


lazy val root = (project in file("root"))
  .dependsOn(common)
  .settings(
    libraryDependencies ++= akkaDependency
  )

lazy val all = (project in file("."))
  .aggregate(proto, common, root, worker)
  .settings(
    basicSettings,
    update / aggregate := false
  )