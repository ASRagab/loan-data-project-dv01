package com.example.routes

import cats.Semigroup
import cats.effect.IO
import cats.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.example.domain.SortType.{Default, IssuedDate, LoanAmount, toOrdering}
import com.example.domain.{LoanData, LoanDataFilters, SortType}
import com.example.fixtures.LoanDataFixture
import com.example.repository.LoanDataRepo
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.http4s.{Method, Request}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.YearMonth

class LoanRoutesSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with LoanDataFixture {
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  val loanDataRepo = new LoanDataRepo[IO] {
    private given Semigroup[LoanData => Boolean] = (first: LoanData => Boolean, second: LoanData => Boolean) =>
      loanData => first(loanData) && second(loanData)

    override def findBy(filters: LoanDataFilters): IO[Vector[LoanData]] = {
      val LoanDataFilters(_, minIssuedDate, grade, minFico, _) = filters

      val minIssuedDateFilter = (data: LoanData) => {
        val maybe = for {
          issuedDate    <- data.issuedDate
          issuedDateYM  <- parseLocalDate(issuedDate)
          minIssuedDate <- minIssuedDate
        } yield minIssuedDate.compareTo(issuedDateYM) <= 0

        maybe.getOrElse(true)
      }

      val gradeFilter = (data: LoanData) => {
        val maybe = for {
          loanGrade   <- data.grade
          filterGrade <- grade
        } yield loanGrade >= filterGrade

        maybe.getOrElse(true)
      }

      val ficoFilter = (data: LoanData) => {
        val maybe = for {
          loanFico   <- data.ficoRangeLow
          filterFico <- minFico
        } yield loanFico >= filterFico

        maybe.getOrElse(true)
      }

      val allFilters = minIssuedDateFilter |+| gradeFilter |+| ficoFilter

      val filtered = loanList.take(filters.size).filter(allFilters)
      filters.sortType.map(sortType => filtered.sorted(toOrdering(sortType))).getOrElse(filtered).pure[IO]
    }
  }

  val loanRoutes = LoanRoutes[IO](loanDataRepo).router

  behavior of "LoanRoutes"

  it should "return a collection of loan data" in {
    for {
      response <- loanRoutes.orNotFound.run(
                    Request(method = Method.POST, uri = uri"/api/loans")
                      .withEntity(LoanDataFilters.Default)
                  )

      body <- response.as[Vector[LoanData]]
    } yield {
      response.status shouldBe Ok
      body shouldBe loanList.take(LoanDataFilters.Default.size).sorted(toOrdering(Default))
    }
  }

  it should "return a collection of loan data ordered based on size and sortType and dateIssued" in {
    for {
      response <-
        loanRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/api/loans?sortType=issuedDate")
            .withEntity(
              LoanDataFilters(2, Some(YearMonth.of(2017, 12)), None, None)
            )
        )
      body     <- response.as[Vector[LoanData]]
    } yield {
      response.status shouldBe Ok
      body shouldBe loanList
        .take(2)
        .filter(_.issuedDate.exists(dt => parseLocalDate(dt).exists(_.compareTo(YearMonth.of(2017, 12)) >= 0)))
        .sorted(toOrdering(IssuedDate))
    }
  }

  it should "return a collection of loan data ordered based on size and minFico if no sortType is requested" in {
    for {
      response <- loanRoutes.orNotFound.run(
                    Request(method = Method.POST, uri = uri"/api/loans")
                      .withEntity(LoanDataFilters(2, None, None, Some(750)))
                  )
      body     <- response.as[Vector[LoanData]]
    } yield {
      response.status shouldBe Ok
      body shouldBe loanList.take(2).filter(_.ficoRangeLow.exists(_ >= 750)).sorted(toOrdering(Default))
    }
  }

  it should "return a collection of loan data ordered based on sortType and grade if no size is requested" in {
    for {
      response <- loanRoutes.orNotFound.run(
                    Request(method = Method.POST, uri = uri"/api/loans?sortType=loanAmount")
                      .withEntity(LoanDataFilters(LoanDataFilters.Default.size, None, Some("A"), None))
                  )
      body     <- response.as[Vector[LoanData]]
    } yield {
      response.status shouldBe Ok
      body shouldBe loanList
        .take(LoanDataFilters.Default.size)
        .filter(_.grade.exists(_ >= "A"))
        .sorted(toOrdering(LoanAmount))
    }
  }

  it should "return bad request if size is less than 1" in {
    for {
      response <- loanRoutes.orNotFound.run(
                    Request(method = Method.POST, uri = uri"/api/loans")
                      .withEntity(LoanDataFilters(0, None, None, None))
                  )
    } yield {
      response.status shouldBe BadRequest
    }
  }

  it should "return bad request if fico is negative" in {
    for {
      response <- loanRoutes.orNotFound.run(
                    Request(method = Method.POST, uri = uri"/api/loans")
                      .withEntity(LoanDataFilters(LoanDataFilters.Default.size, None, None, Some(-1)))
                  )
    } yield {
      response.status shouldBe BadRequest
    }
  }
}
