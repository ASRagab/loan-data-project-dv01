package com.example.routes

import cats.MonadThrow
import cats.data.EitherT
import cats.implicits.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, Request, Response}
import org.typelevel.log4cats.Logger

trait RequestValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F] {
  extension (request: Request[F])
    def validate[A](validator: A => F[Either[FailureResponse, A]])(handler: A => F[Response[F]])(using
        decoder: EntityDecoder[F, A]
    ): F[Response[F]] = {
      val result = for {
        entity    <- request
                       .attemptAs[A]
                       .leftSemiflatMap(failure =>
                         Logger[F].error(failure)("Failed to decode request body") *> BadRequest(
                           FailureResponse(s"Failed to decode request body ${failure.getMessage}").asJson
                         )
                       )
        validated <- EitherT(validator(entity)).leftSemiflatMap(failure => BadRequest(failure.asJson))
        response  <- EitherT.liftF(handler(validated))
      } yield response

      result.value.map(_.merge)
    }
}

case class FailureResponse(error: String)

object FailureResponse {
  given Encoder[FailureResponse] = deriveEncoder[FailureResponse]

  given Decoder[FailureResponse] = deriveDecoder[FailureResponse]

}
