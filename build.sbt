organization := "com.hon"

name := "hon-sagashi-api"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.3"

val unusedWarnings = (
  "-Ywarn-unused" ::
  Nil
)

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
  case Some((2, v)) if v >= 11 => unusedWarnings
}.toList.flatten

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

scalacOptions ++= "-deprecation" :: "unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.10.0-M10"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-directives" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-specs2" % unfilteredVersion % "test",
  "com.google.cloud" % "google-cloud-datastore" % "1.104.0"
)
