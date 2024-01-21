package com.example.domain

import caliban.schema.Annotations.*
import cats.Applicative
import cats.implicits.*
import com.example.routes.FailureResponse
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

import java.time.format.DateTimeFormatter
import java.time.{Clock, YearMonth}

case class LoanDataFilters private (
    size: Int,
    minIssuedDate: Option[YearMonth],
    grade: Option[String],
    minFico: Option[Int],
    sortType: Option[SortType]
) { self =>
  def withSortType(s: SortType): LoanDataFilters = self.copy(sortType = Some(s))
}

object LoanDataFilters {
  val Default: LoanDataFilters = LoanDataFilters(10, None, None, None, Some(SortType.Default))

  def apply(
      size: Int,
      minIssuedDate: Option[YearMonth],
      grade: Option[String],
      minFico: Option[Int]
  ): LoanDataFilters =
    new LoanDataFilters(
      size,
      minIssuedDate,
      grade,
      minFico,
      None
    )

  def validator[F[_]: Applicative](loanDataFilters: LoanDataFilters): F[Either[FailureResponse, LoanDataFilters]] = {
    val LoanDataFilters(size, minIssuedDate, grade, minFico, _) = loanDataFilters

    val conditions: Boolean =
      List(
        Some(size > 0),
        grade.map(_.length <= 2),
        minIssuedDate.map(_.isBefore(YearMonth.now(Clock.systemUTC()))),
        minFico.map(fico => fico > 0 && fico < 850)
      ).flatten.forall(_ == true)

    // TODO: Use Validated instead of Either
    Either
      .cond(
        conditions,
        loanDataFilters,
        FailureResponse(
          "Insure size and fico are greater than zero, the issuedDate is before now, and the grade is two characters or less"
        )
      )
      .pure[F]
  }

  val issuedDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM-yyyy")

  given Encoder[YearMonth] = Encoder.encodeYearMonthWithFormatter(issuedDateFormat)
  given Decoder[YearMonth] = Decoder.decodeYearMonthWithFormatter(issuedDateFormat)

  given Decoder[LoanDataFilters] = deriveDecoder[LoanDataFilters]
  given Encoder[LoanDataFilters] = deriveEncoder[LoanDataFilters]

  extension (filters: LoanDataFilters) {
    def toFragment: Fragment = {
      val whereFragment = Fragments.whereAndOpt(
        filters.minIssuedDate.map(date =>
          fr"TO_DATE(issued_date, 'Mon-YYYY') >= TO_DATE(${date.format(issuedDateFormat)}, 'Mon-YYYY')"
        ),
        filters.grade.map(gr => fr"grade <= $gr"),
        filters.minFico.map(fico => fr"fico_range_low >= $fico")
      )
      
      val sortTypeFragment = filters.sortType.map(_.toFragment).getOrElse(SortType.Default.toFragment)
      val limitFragment    = fr"LIMIT ${filters.size}"

      whereFragment |+| sortTypeFragment |+| limitFragment
    }
  }
}
