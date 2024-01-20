package com.example.logging

import cats.MonadError
import org.typelevel.log4cats.Logger
import cats.implicits.*

object syntax {
  extension [F[_], E, A](fa: F[A])(using me: MonadError[F, E], logger: Logger[F])
    def log(success: A => String, error: E => String): F[A] = fa.attemptTap {
      case Right(value) => logger.info(success(value))
      case Left(value)  => logger.error(error(value))
    }

    def logError(error: E => String): F[A] = fa.attemptTap {
      case Left(value) => logger.error(error(value))
      case _           => me.unit
    }
}
