package com.example.domain

import doobie.util.fragment.Fragment

import java.text.SimpleDateFormat
import io.circe.*

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
  given Decoder[SortType] = Decoder.decodeString.map(fromString)

  val issueDate: Ordering[LoanData] = {
    val format = new SimpleDateFormat("MMM-yyyy")
    Ordering.by(loan => loan.issuedDate.map(format.parse))
  }

  val default: Ordering[LoanData]    = Ordering.by(_.id)
  val loanAmount: Ordering[LoanData] = Ordering.by(loan => loan.loanAmount.map(value => -value))
  val grade: Ordering[LoanData]      =
    Ordering.Tuple2[Option[String], Option[String]].on(loan => (loan.grade, loan.subGrade))
  val ficoLow: Ordering[LoanData]    = Ordering.by(_.ficoRangeLow)
  val ficoHigh: Ordering[LoanData]   = Ordering.by(_.ficoRangeHigh)

  def fromString(s: String): SortType = s match {
    case IssuedDate.value => IssuedDate
    case LoanAmount.value => LoanAmount
    case Grade.value      => Grade
    case FicoLow.value    => FicoLow
    case FicoHigh.value   => FicoHigh
    case _                => Default
  }

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
        case SortType.Default    => SortType.default
        case SortType.IssuedDate => SortType.issueDate
        case SortType.LoanAmount => SortType.loanAmount
        case SortType.Grade      => SortType.grade
        case SortType.FicoLow    => SortType.ficoLow
        case SortType.FicoHigh   => SortType.ficoHigh
      }
  }
}
