package com.example.config

import com.comcast.ip4s.{Host, Port}
import com.zaxxer.hikari.HikariConfig
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.EnumConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

final case class AppConfig(ember: EmberConfig, db: DbConfig, cache: CacheConfig, serverType: ServerType)
    derives ConfigReader

final case class DbConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    threads: Int,
    connectionTimeout: FiniteDuration,
    maxLifetime: FiniteDuration
) derives ConfigReader

extension (dbConfig: DbConfig)
  def toHikariConfig: HikariConfig = {
    val config = new HikariConfig()
    config.setDriverClassName(dbConfig.driver)
    config.setJdbcUrl(dbConfig.url)
    config.setUsername(dbConfig.user)
    config.setPassword(dbConfig.password)
    config.setMaximumPoolSize(dbConfig.threads)
    config.setConnectionTimeout(dbConfig.connectionTimeout.toMillis)
    config.setMaxLifetime(dbConfig.maxLifetime.toMillis)
    config
  }

final case class CacheConfig(host: Host, port: Port, password: String, cacheTimeout: FiniteDuration)
    derives ConfigReader

final case class EmberConfig(host: Host, port: Port, idleTimeout: FiniteDuration, shutdownTimeout: FiniteDuration)
    derives ConfigReader

given ConfigReader[Host] = ConfigReader[String].emap { raw =>
  Host.fromString(raw).toRight(CannotConvert(raw, "Host", s"Invalid host: $raw"))
}

given ConfigReader[Port] = ConfigReader[Int].emap { raw =>
  Port.fromInt(raw).toRight(CannotConvert(raw.toString, "Port", s"Invalid port: $raw"))
}

enum ServerType derives EnumConfigReader {
  case Http
  case Graphql
}
