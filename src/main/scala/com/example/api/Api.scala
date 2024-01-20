package com.example.api

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import cats.implicits.*
import com.example.repository.LoanDataRepo
import com.example.routes.{HealthRoutes, LoanRoutes}
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class Api[F[_]: Concurrent: Logger] private (loanDataRepo: LoanDataRepo[F]) {

  private val loanRouter   = LoanRoutes[F](loanDataRepo).router
  private val healthRouter = HealthRoutes[F].router

  val endpoints: HttpRoutes[F] = Router(
    "/" -> (loanRouter <+> healthRouter)
  )
}

object Api {
  def apply[F[_]: Concurrent: Logger](loanDataRepo: LoanDataRepo[F]): Api[F] =
    new Api[F](loanDataRepo)
}
