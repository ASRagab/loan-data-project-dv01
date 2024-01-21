package com.example.repository

import cats.effect.*
import cats.syntax.all.*
import com.example.domain.{LoanData, LoanDataFilters}
import com.example.services.Cache
import doobie.*
import doobie.implicits.*
import org.typelevel.log4cats.Logger

trait LoanDataRepo[F[_]] {
  def findBy(filters: LoanDataFilters): F[Vector[LoanData]]
}

class LoanDataPostgresRepo[F[_]: MonadCancelThrow: Logger] private (
    xa: Transactor[F],
    cache: Cache[F]
) extends LoanDataRepo[F] {

  private def findBySql(filters: LoanDataFilters): ConnectionIO[Vector[LoanData]] = {
    val selectFragment =
      fr"""
      SELECT 
        id,
        loan_amount,
        funded_amount,
        term,
        interest_rate,
        grade,
        sub_grade,
        employee_title,
        home_ownership,
        issued_date,
        loan_status,
        zip_code,
        state_address,
        fico_range_low,
        fico_range_high
     FROM loan_data
    """

    (selectFragment |+| filters.toFragment).query[LoanData].to[Vector]
  }

  override def findBy(filters: LoanDataFilters): F[Vector[LoanData]] =
    for {
      cached <- cache.get[LoanDataFilters, LoanData](filters)
      loans  <- cached.fold {
                  findBySql(filters)
                    .transact(xa)
                    .flatTap(result => cache.set(filters, result))
                    .onError(Logger[F].error(_)("Error while fetching loans"))
                }(_.pure[F])
    } yield loans
}

object LoanDataPostgresRepo {
  def apply[F[_]: MonadCancelThrow: Logger](
      xa: Transactor[F],
      cache: Cache[F]
  ): LoanDataRepo[F] =
    new LoanDataPostgresRepo[F](xa, cache)
}
