package com.example.fixtures

import cats.effect.IO
import cats.effect.kernel.Resource
import com.example.services.Cache
import com.redis.testcontainers.RedisContainer
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.testcontainers.containers.PostgreSQLContainer
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait RepoFixture {
  given Logger[IO] = Slf4jLogger.getLogger[IO]
  def initScript: String

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScript)

      container.start()
      container
    }

    val release = (container: PostgreSQLContainer[Nothing]) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val redis: Resource[IO, RedisContainer] = {
    val acquire = IO {
      val container = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME)
      container.start()
      container
    }

    val release = (container: RedisContainer) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, HikariTransactor[IO]] = {
    for
      db <- postgres
      ce <- ExecutionContexts.fixedThreadPool[IO](1)
      xa <- HikariTransactor.newHikariTransactor[IO](
              db.getDriverClassName,
              db.getJdbcUrl,
              db.getUsername,
              db.getPassword,
              ce
            )
    yield xa
  }

  val cache =
    for {
      redis <- redis
      cache <- Cache.makeTest[IO](redis.getRedisURI)
    } yield cache
}
