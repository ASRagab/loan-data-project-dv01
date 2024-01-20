ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

addCommandAlias("preCommit", ";clean;compile;scalafmtAll;test")

lazy val root = (project in file("."))
  .settings(
    name                 := "dv01",
    libraryDependencies ++= Dependencies.applicationDependencies,
    Compile / run / fork := false
  )
