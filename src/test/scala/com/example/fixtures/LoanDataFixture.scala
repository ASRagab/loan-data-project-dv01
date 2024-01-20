package com.example.fixtures

import com.example.domain.{LoanData, LoanDataFilters}
import io.circe.*
import io.circe.parser.*

import java.time.YearMonth
import scala.util.Try

trait LoanDataFixture {
  protected def parseLocalDate(dt: String): Option[YearMonth] =
    Try(YearMonth.parse(dt, LoanDataFilters.issuedDateFormat)).toOption

  lazy val rawList =
    """
      |[{
      |"id": 125313459,
      |"loanAmount": 10500,
      |"fundedAmount": 10500,
      |"term": " 36 months",
      |"interestRate": "5.32%",
      |"grade": "A",
      |"subGrade": "A1",
      |"employeeTitle": "Carman",
      |"homeOwnership": "MORTGAGE",
      |"issuedDate": "Dec-2017",
      |"loanStatus": "Fully Paid",
      |"zipCode": "310xx",
      |"stateAddress": "GA",
      |"ficoRangeLow": "755",
      |"ficoRangeHigh": "759"
      |},
      |{
      |"id": 124457048,
      |"loanAmount": 30000,
      |"fundedAmount": 30000,
      |"term": " 36 months",
      |"interestRate": "5.32%",
      |"grade": "A",
      |"subGrade": "A1",
      |"employeeTitle": "Director 1",
      |"homeOwnership": "MORTGAGE",
      |"issuedDate": "Nov-2017",
      |"loanStatus": "Current",
      |"zipCode": "333xx",
      |"stateAddress": "FL",
      |"ficoRangeLow": "810",
      |"ficoRangeHigh": "814"
      |},
      |{
      |"id": 125623807,
      |"loanAmount": 20000,
      |"fundedAmount": 20000,
      |"term": " 36 months",
      |"interestRate": "5.32%",
      |"grade": "C",
      |"subGrade": "C1",
      |"employeeTitle": "Manager, Accounting Service Management",
      |"homeOwnership": "MORTGAGE",
      |"issuedDate": "Dec-2019",
      |"loanStatus": "Fully Paid",
      |"zipCode": "633xx",
      |"stateAddress": "MO",
      |"ficoRangeLow": "705",
      |"ficoRangeHigh": "709"
      |},
      |{
      |"id": 125317723,
      |"loanAmount": 6000,
      |"fundedAmount": 6000,
      |"term": " 36 months",
      |"interestRate": "5.32%",
      |"grade": "B",
      |"subGrade": "B1",
      |"employeeTitle": "COUNSELOR ",
      |"homeOwnership": "MORTGAGE",
      |"issuedDate": "Dec-2016",
      |"loanStatus": "Fully Paid",
      |"zipCode": "919xx",
      |"stateAddress": "CA",
      |"ficoRangeLow": "805",
      |"ficoRangeHigh": "809"
      |}]
      |""".stripMargin

  lazy val loanList: Vector[LoanData] = decode[Vector[LoanData]](rawList).getOrElse(Vector.empty)

  lazy val loanDataFiltersJson =
    """
      |{ "size": 100, "issuedDate": "Dec-2017", "grade": "A", "minFico": 500 }
      |""".stripMargin

  lazy val filters = decode[LoanDataFilters](loanDataFiltersJson)
}
