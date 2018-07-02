scalaVersion in ThisBuild := "2.12.6"

val commonSettings = Seq(
  name := "pact-lagom",
  version := "0.1-SNAPSHOT",
  organization := "com.github.erip"
)

lazy val `pact-lagom` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5",
      "com.lightbend.lagom" %% "lagom-scaladsl-testkit" % "1.4.6",
      "com.typesafe.play" %% "play-json" % "2.6.7",
      "com.typesafe.play" %% "play-ws" % "2.6.7"
    )
  )