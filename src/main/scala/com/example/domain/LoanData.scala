package com.example.domain

import doobie.Read
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

final case class LoanData(
    id: Long,
    loanAmount: Option[Int],
    fundedAmount: Option[Int],
    term: Option[String],
    interestRate: Option[String],
    grade: Option[String],
    subGrade: Option[String],
    employeeTitle: Option[String],
    homeOwnership: Option[String],
    issuedDate: Option[String],
    loanStatus: Option[String],
    zipCode: Option[String],
    stateAddress: Option[String],
    ficoRangeLow: Option[Int],
    ficoRangeHigh: Option[Int]
)

object LoanData {
  val list: List[LoanData] = List.empty

  given Decoder[LoanData] = deriveDecoder[LoanData]
  given Encoder[LoanData] = deriveEncoder[LoanData]

  given Read[LoanData] = Read[
    (
        Long,
        Option[Int],
        Option[Int],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[Int],
        Option[Int]
    )
  ].map {
    case (
          id,
          loanAmount,
          fundedAmount,
          term,
          interestRate,
          grade,
          subGrade,
          employeeTitle,
          homeOwnership,
          issuedDate,
          loanStatus,
          zipCode,
          stateAddress,
          ficoRangeLow,
          ficoRangeHigh
        ) =>
      LoanData(
        id,
        loanAmount,
        fundedAmount,
        term,
        interestRate,
        grade,
        subGrade,
        employeeTitle,
        homeOwnership,
        issuedDate,
        loanStatus,
        zipCode,
        stateAddress,
        ficoRangeLow,
        ficoRangeHigh
      )
  }
}
