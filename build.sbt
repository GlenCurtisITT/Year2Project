name := """Year2ProjectHPMS"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  evolutions,
  cache,
  "org.mindrot" % "jbcrypt" % "0.3m",
  javaWs
)
