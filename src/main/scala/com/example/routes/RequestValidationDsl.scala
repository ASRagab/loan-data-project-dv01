package com.example.routes

import cats.MonadThrow
import cats.implicits.*
import com.example.logging.syntax.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, Request, Response}
import org.typelevel.log4cats.Logger

trait RequestValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F] {
  extension (request: Request[F])
    def validate[A](validator: A => F[Either[FailureResponse, A]])(logic: A => F[Response[F]])(using
        decoder: EntityDecoder[F, A]
    ): F[Response[F]] =
      request
        .as[A]
        .logError(e => s"Failed to parse request: $e")
        .flatMap(validator)
        .flatMap {
          case Left(failure) => BadRequest(failure.asJson)
          case Right(valid)  => logic(valid)
        }
}

case class FailureResponse(error: String)

object FailureResponse {
  given Encoder[FailureResponse] = deriveEncoder[FailureResponse]

  given Decoder[FailureResponse] = deriveDecoder[FailureResponse]

}
