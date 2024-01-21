package com.example.domain

import cats.implicits.*
import doobie.util.fragment.Fragment
import io.circe.*

import java.text.SimpleDateFormat
import scala.util.Try

enum SortType(val value: String) {
  case Default    extends SortType("default")
  case IssuedDate extends SortType("issuedDate")
  case LoanAmount extends SortType("loanAmount")
  case Grade      extends SortType("grade")
  case FicoLow    extends SortType("ficoLow")
  case FicoHigh   extends SortType("ficoHigh")
}

object SortType {
  given Encoder[SortType] = Encoder.encodeString.contramap[SortType](sortType => sortType.value)
  given Decoder[SortType] = Decoder.decodeString.emap(fromStringEither)

  def fromStringEither(s: String): Either[String, SortType] = Try {
    s match {
      case IssuedDate.value => IssuedDate
      case LoanAmount.value => LoanAmount
      case Grade.value      => Grade
      case FicoLow.value    => FicoLow
      case FicoHigh.value   => FicoHigh
      case Default.value    => Default
      case _                => throw new IllegalArgumentException(s"Invalid sort type: $s")
    }
  }.toEither.leftMap(_.getMessage)

  extension (sortType: SortType) {
    def toFragment: Fragment =
      sortType match {
        case SortType.Default    => Fragment.empty
        case SortType.IssuedDate => Fragment.const("ORDER BY TO_DATE(issued_date, 'Mon-YYYY') DESC")
        case SortType.LoanAmount => Fragment.const("ORDER BY loan_amount DESC")
        case SortType.Grade      => Fragment.const("ORDER BY grade, sub_grade")
        case SortType.FicoLow    => Fragment.const("ORDER BY fico_range_low")
        case SortType.FicoHigh   => Fragment.const("ORDER BY fico_range_high DESC")
      }

    def toOrdering: Ordering[LoanData] =
      sortType match {
        case SortType.Default    => Ordering.by(_.id)
        case SortType.IssuedDate => Ordering.by(loan => loan.issuedDate.map(new SimpleDateFormat("MMM-yyyy").parse))
        case SortType.LoanAmount => Ordering.by(loan => loan.loanAmount.map(value => -value))
        case SortType.Grade      => Ordering.Tuple2[Option[String], Option[String]].on(loan => (loan.grade, loan.subGrade))
        case SortType.FicoLow    => Ordering.by(_.ficoRangeLow)
        case SortType.FicoHigh   => Ordering.by(_.ficoRangeHigh)
      }
  }
}
