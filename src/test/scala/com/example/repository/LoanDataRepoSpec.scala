package com.example.repository

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import com.example.domain.{LoanData, LoanDataFilters, SortType}
import com.example.fixtures.{LoanDataFixture, RepoFixture}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.YearMonth

class LoanDataRepoSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with RepoFixture with LoanDataFixture {

  override val initScript: String = "sql/init.sql"

  behavior of "LoanDataRepo"

  it should "return loan data in default order with all issued dates occurring after Jan-2014" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo     <- LoanDataPostgresRepo[IO](xa, cache)
        loanData <- repo.findBy(
                      LoanDataFilters(
                        Some(LoanDataFilters.Default.size),
                        LoanDataFilters.Default.sortType,
                        Some(YearMonth.of(2014, 1)),
                        None,
                        None
                      )
                    )
      } yield {
        loanData should have size LoanDataFilters.Default.size
        loanData
          .forall(_.issuedDate.forall(parseLocalDate(_).exists(_.compareTo(YearMonth.of(2014, 1)) >= 0))) shouldBe true
      }
    }
  }

  it should "return loan data in order of loan amount high to low with all grade A loans" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo     <- LoanDataPostgresRepo[IO](xa, cache)
        loanData <- repo.findBy(LoanDataFilters(Some(5), Some(SortType.LoanAmount), None, Some("A"), None))
      } yield {
        loanData should have size 5
        loanData.head.loanAmount shouldBe Some(40000)
        loanData.last.loanAmount shouldBe Some(10000)
        loanData.forall(_.grade.exists(_ >= "A")) shouldBe true
      }
    }
  }

  it should "return loan data in order of issued_date most recent to least recent and min fico above 660" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo     <- LoanDataPostgresRepo[IO](xa, cache)
        loanData <- repo.findBy(LoanDataFilters(Some(5), Some(SortType.IssuedDate), None, None, Some(661)))
      } yield {
        loanData should have size 5
        loanData.head.issuedDate shouldBe Some("Dec-2022")
        loanData.last.issuedDate shouldBe Some("Dec-2018")
      }
    }
  }

  it should "return loan data in order of grade and subgrade" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo     <- LoanDataPostgresRepo[IO](xa, cache)
        loanData <- repo.findBy(LoanDataFilters(Some(5), Some(SortType.Grade), None, None, None))
      } yield {
        loanData should have size 5
        loanData.head.grade shouldBe Some("A")
        loanData.head.subGrade shouldBe Some("A1")
        loanData.last.grade shouldBe Some("B")
        loanData.last.subGrade shouldBe Some("B2")
      }
    }
  }

  it should "it should cache the loan data" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo           <- LoanDataPostgresRepo[IO](xa, cache)
        loanData       <- repo.findBy(LoanDataFilters.Default)
        cachedLoanData <- cache.get[LoanDataFilters, LoanData](LoanDataFilters.Default)
      } yield {
        loanData should have size 10
        cachedLoanData.map(_.size) shouldBe Some(10)
      }
    }
  }

  it should "invalidate the cache after the provided ttl" in {
    Resource.both(transactor, cache).use { case (xa, cache) =>
      for {
        repo           <- LoanDataPostgresRepo[IO](xa, cache)
        loanData       <- repo.findBy(LoanDataFilters.Default)
        _              <- cache.get[LoanDataFilters, LoanData](LoanDataFilters.Default) *> IO.sleep(cache.ttl)
        cachedLoanData <- cache.get[LoanDataFilters, LoanData](LoanDataFilters.Default)
      } yield {
        loanData should have size 10
        cachedLoanData shouldBe None
      }
    }
  }

}
