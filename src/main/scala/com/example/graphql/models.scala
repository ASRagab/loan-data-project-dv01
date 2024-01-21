package com.example.graphql

import caliban.CalibanError.ExecutionError
import caliban.Value.StringValue
import caliban.interop.cats.implicits.*
import caliban.schema.Annotations.GQLDescription
import caliban.schema.{ArgBuilder, Schema}
import cats.effect.*
import cats.effect.std.Dispatcher
import cats.implicits.*
import com.example.domain.*
import org.typelevel.log4cats.Logger

import java.time.YearMonth
import scala.util.Try

object models {
  case class Queries[F[_]](
      @GQLDescription("Return loan data according to the provided filters")
      loanData: LoanDataFilters => F[Vector[LoanData]]
  )

  given ArgBuilder[YearMonth] = {
    case StringValue(value) =>
      lazy val error = (ex: Throwable) =>
        Left(ExecutionError(s"Invalid YearMonth format: $value", innerThrowable = Some(ex)))
      Try(YearMonth.parse(value, LoanDataFilters.issuedDateFormat)).fold(error, Right(_))
    case other              => Left(ExecutionError(s"Invalid YearMonth format $other"))
  }

  given ArgBuilder[SortType] = ArgBuilder.gen

  given Schema[Any, YearMonth] = Schema.stringSchema.contramap(ym => ym.format(LoanDataFilters.issuedDateFormat))
  given Schema[Any, SortType]  = Schema.gen

  given ArgBuilder[LoanDataFilters] = ArgBuilder.gen

  given Schema[Any, LoanDataFilters]                                      = Schema.gen
  given Schema[Any, LoanData]                                             = Schema.gen
  given queries[F[_]: Async: Dispatcher: Logger]: Schema[Any, Queries[F]] = Schema.gen

}
