import sbt.*

object Dependencies {
  lazy val circeVersion               = "0.14.5"
  lazy val catsEffectVersion          = "3.5.0"
  lazy val http4sVersion              = "0.23.19"
  lazy val doobieVersion              = "1.0.0-RC5"
  lazy val pureConfigVersion          = "0.17.2"
  lazy val log4catsVersion            = "2.5.0"
  lazy val scalaTestVersion           = "3.2.15"
  lazy val scalaTestCatsEffectVersion = "1.4.0"
  lazy val testContainerVersion       = "1.17.6"
  lazy val logbackVersion             = "1.4.7"
  lazy val slf4jVersion               = "2.0.5"
  lazy val redis4catsVersion          = "1.5.2"
  lazy val calibanVersion             = "2.5.1"
  lazy val tapirVersion               = "1.2.10"

  lazy val applicationDependencies =
    Seq(
      "org.typelevel"               %% "cats-effect"                   % catsEffectVersion,
      "org.http4s"                  %% "http4s-dsl"                    % http4sVersion,
      "org.http4s"                  %% "http4s-ember-server"           % http4sVersion,
      "org.http4s"                  %% "http4s-circe"                  % http4sVersion,
      "io.circe"                    %% "circe-generic"                 % circeVersion,
      "io.circe"                    %% "circe-core"                    % circeVersion,
      "io.circe"                    %% "circe-parser"                  % circeVersion,
      "dev.profunktor"              %% "redis4cats-effects"            % redis4catsVersion,
      "dev.profunktor"              %% "redis4cats-core"               % redis4catsVersion,
      "dev.profunktor"              %% "redis4cats-log4cats"           % redis4catsVersion,
      "org.tpolecat"                %% "doobie-core"                   % doobieVersion,
      "org.tpolecat"                %% "doobie-hikari"                 % doobieVersion,
      "org.tpolecat"                %% "doobie-postgres"               % doobieVersion,
      "org.tpolecat"                %% "doobie-scalatest"              % doobieVersion              % Test,
      "com.github.ghostdogpr"       %% "caliban"                       % calibanVersion,
      "com.github.ghostdogpr"       %% "caliban-http4s"                % calibanVersion,
      "com.github.ghostdogpr"       %% "caliban-cats"                  % calibanVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"              % tapirVersion,
      "com.github.pureconfig"       %% "pureconfig-core"               % pureConfigVersion,
      "org.typelevel"               %% "log4cats-slf4j"                % log4catsVersion,
      "org.typelevel"               %% "log4cats-core"                 % log4catsVersion,
      "ch.qos.logback"               % "logback-classic"               % logbackVersion,
      "org.scalatest"               %% "scalatest"                     % scalaTestVersion           % Test,
      "org.typelevel"               %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      "org.testcontainers"           % "testcontainers"                % testContainerVersion       % Test,
      "org.testcontainers"           % "postgresql"                    % testContainerVersion       % Test,
      "com.redis"                    % "testcontainers-redis"          % "2.0.1"                    % Test
    )
}
