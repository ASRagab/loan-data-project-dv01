package com.example.routes

import cats.effect.kernel.Concurrent
import cats.implicits.*
import com.example.domain.{LoanDataFilters, SortType}
import com.example.repository.LoanDataRepo
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.server.Router
import org.http4s.{HttpRoutes, QueryParamDecoder, Request, Response}
import org.typelevel.log4cats.Logger

class LoanRoutes[F[_]: Concurrent: Logger] private (repo: LoanDataRepo[F]) extends RequestValidationDsl[F] {

  given QueryParamDecoder[SortType] = QueryParamDecoder[String].map(str => SortType.fromString(str))
  private object SortTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[SortType]("sortType")

  // POST /api/loans
  val loanRoute = HttpRoutes.of[F] { case req @ POST -> Root / "loans" :? SortTypeQueryParamMatcher(sortType) =>
    req.validate[LoanDataFilters](LoanDataFilters.validator) { filters =>
      val updated = filters.withSortType(sortType.getOrElse(SortType.Default))
      for {
        loans <- repo.findBy(updated)
        resp  <- Ok(loans.map(_.asJson).asJson)
      } yield resp
    }
  }

  val router: HttpRoutes[F] = Router("/api" -> loanRoute)

}

object LoanRoutes {

  def apply[F[_]: Concurrent: Logger](repo: LoanDataRepo[F]): LoanRoutes[F] = new LoanRoutes[F](repo)
}
