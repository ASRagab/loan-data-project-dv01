package com.example.services

import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import com.example.config.CacheConfig
import dev.profunktor.redis4cats.log4cats.*
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.*

trait Cache[F[_]] {
  def get[T: Encoder, A: Decoder](key: T): F[Option[Vector[A]]]
  def set[T: Encoder, A: Encoder](key: T, value: Vector[A]): F[Unit]
  def ttl: FiniteDuration

}

final class RedisCache[F[_]: Async: Logger](
    redis: RedisCommands[F, String, String],
    cacheTimeout: FiniteDuration,
    override val ttl: FiniteDuration
) extends Cache[F] {
  override def get[T: Encoder, A: Decoder](key: T): F[Option[Vector[A]]] =
    for {
      value  <- redis.get(key.asJson.noSpacesSortKeys).timeout(cacheTimeout)
      result <- value.map(decode[Vector[A]](_).toOption.pure[F]).getOrElse(none[Vector[A]].pure[F])
    } yield result

  override def set[T: Encoder, A: Encoder](key: T, value: Vector[A]): F[Unit] =
    redis
      .setEx(key.asJson.noSpacesSortKeys, value.asJson.noSpaces, expiresIn = ttl)
      .timeout(cacheTimeout)
      .void
}

object Cache {
  private lazy val DefaultTtl: FiniteDuration = 30.minutes

  def make[F[_]: Async: Logger](cacheConfig: CacheConfig): Resource[F, Cache[F]] =
    for {
      redis <- Redis[F].utf8(s"redis://:${cacheConfig.password}@${cacheConfig.host}:${cacheConfig.port}")
    } yield new RedisCache[F](redis, cacheConfig.cacheTimeout, DefaultTtl)

  def makeTest[F[_]: Async: Logger](uri: String): Resource[F, Cache[F]] =
    for {
      redis <- Redis[F].utf8(uri)
    } yield new RedisCache[F](redis, cacheTimeout = 2.seconds, ttl = 2.seconds)
}
