package com.example

import caliban.interop.cats.implicits.*
import cats.effect.*
import cats.effect.std.Dispatcher
import com.example.api.Api
import com.example.config.syntax.*
import com.example.config.{AppConfig, ServerType}
import com.example.graphql.{Route => GraphQLRoute, Api => GraphQLApi}
import com.example.repository.LoanDataPostgresRepo
import com.example.services.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

object Application extends IOApp.Simple {
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  given zio.Runtime[Any] = zio.Runtime.default

  private def runServer(config: AppConfig): Resource[IO, Server] =
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

  private def runGraphQLServer(config: AppConfig): Resource[IO, Server] =
    Dispatcher.parallel[IO].flatMap { implicit dispatcher =>
      for {
        xa          <- Database.make[IO](config.db)
        cache       <- Cache.make[IO](config.cache)
        repo         = LoanDataPostgresRepo[IO](xa, cache)
        graphql      = GraphQLApi[IO](repo).graphql
        interpreter <- graphql.interpreterAsync[IO].toResource
        server      <- EmberServerBuilder
                         .default[IO]
                         .withHost(config.ember.host)
                         .withPort(config.ember.port)
                         .withIdleTimeout(config.ember.idleTimeout)
                         .withShutdownTimeout(config.ember.shutdownTimeout)
                         .withHttpWebSocketApp(wsBuilder => GraphQLRoute[IO](interpreter, wsBuilder).routes.orNotFound)
                         .build

      } yield server
    }

  override def run: IO[Unit] =
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap { config =>
        lazy val server = if (config.serverType == ServerType.Rest) runServer(config) else runGraphQLServer(config)

        server
          .use(_ => Logger[IO].info(s"Server started at ${config.ember.host}:${config.ember.port}") *> IO.never)
          .onError(ex => Logger[IO].error(ex)("Server error"))
      }

}
