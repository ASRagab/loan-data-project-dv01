package com.example.config

import cats.MonadThrow
import cats.implicits.*
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object syntax {

  extension (source: ConfigSource)
    def loadF[F[_]: MonadThrow, A: ConfigReader: ClassTag]: F[A] =
      MonadThrow[F].pure(source.load[A]).flatMap {
        case Left(e)  => MonadThrow[F].raiseError(ConfigReaderException(e))
        case Right(a) => MonadThrow[F].pure(a)
      }
}
