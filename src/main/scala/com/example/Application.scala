package com.example

import cats.effect.*
import com.example.api.Api
import com.example.config.AppConfig
import com.example.config.syntax.*
import com.example.repository.LoanDataPostgresRepo
import com.example.services.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

object Application extends IOApp.Simple {
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  def runServer(config: AppConfig): Resource[IO, Server] =
    for {
      xa     <- Database.make[IO](config.db)
      cache  <- Cache.make[IO](config.cache)
      repo    = LoanDataPostgresRepo[IO](xa, cache)
      api     = Api[IO](repo)
      server <- EmberServerBuilder
                  .default[IO]
                  .withHost(config.ember.host)
                  .withPort(config.ember.port)
                  .withIdleTimeout(config.ember.idleTimeout)
                  .withShutdownTimeout(config.ember.shutdownTimeout)
                  .withHttpApp(api.endpoints.orNotFound)
                  .build

    } yield server

  override def run: IO[Unit] =
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap { config =>
        runServer(config)
          .use(_ => Logger[IO].info(s"Server started at ${config.ember.host}:${config.ember.port}") *> IO.never)
          .onError(ex => Logger[IO].error(ex)("Server error"))
      }

}
