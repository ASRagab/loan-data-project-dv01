package com.example.services

import cats.effect.{Async, Resource}
import com.example.config.*
import doobie.hikari.HikariTransactor

object Database {
  def make[F[_]: Async](config: DbConfig): Resource[F, HikariTransactor[F]] =
    HikariTransactor.fromHikariConfig(config.toHikariConfig)

}
