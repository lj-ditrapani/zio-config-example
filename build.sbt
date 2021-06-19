lazy val root = (project in file(".")).settings(
  name := "zio-config",
  version := "0.1",
  scalaVersion := "2.13.5",
  organization := "info.ditrapani",
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
)

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % dependencies.zioV,
  "dev.zio" %% "zio-config" % dependencies.zioConfigV,
  "dev.zio" %% "zio-config-magnolia" % dependencies.zioConfigV,
)

wartremoverWarnings ++= Warts.allBut(
  Wart.Equals,
  Wart.NonUnitStatements,
  Wart.Throw,
  Wart.AsInstanceOf,
  Wart.StringPlusAny,
)

lazy val dependencies =
  new {
    val zioV = "1.0.9"
    val zioConfigV = "1.0.6"
  }
