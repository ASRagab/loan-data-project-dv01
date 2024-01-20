package com.example.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import java.time.LocalDateTime

class HealthRoutes[F[_]: Monad] private extends Http4sDsl[F] {

  private val routes = HttpRoutes.of[F] { case GET -> Root / "health" =>
    Ok(s"The local time is ${LocalDateTime.now()}")
  }

  val router: HttpRoutes[F] = Router(
    "/" -> routes
  )
}

object HealthRoutes {
  def apply[F[_]: Monad]: HealthRoutes[F] = new HealthRoutes[F]
}
