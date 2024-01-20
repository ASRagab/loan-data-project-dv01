package com.example.routes

import cats.effect.kernel.Concurrent
import cats.implicits.*
import com.example.domain.LoanDataFilters
import com.example.repository.LoanDataRepo
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response}
import org.typelevel.log4cats.Logger

class LoanRoutes[F[_]: Concurrent: Logger] private (repo: LoanDataRepo[F]) extends RequestValidationDsl[F] {

  // POST /api/loans
  val loanRoute = HttpRoutes.of[F] { case req @ POST -> Root / "loans" =>
    req.validate[LoanDataFilters](LoanDataFilters.validator) { filters =>
      for {
        loans <- repo.findBy(filters)
        resp  <- Ok(loans.map(_.asJson).asJson)
      } yield resp
    }
  }

  val router: HttpRoutes[F] = Router("/api" -> loanRoute)

}

object LoanRoutes {

  def apply[F[_]: Concurrent: Logger](repo: LoanDataRepo[F]): LoanRoutes[F] = new LoanRoutes[F](repo)
}
