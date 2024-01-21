package com.example.graphql

import caliban.*
import caliban.interop.cats.implicits.*
import cats.effect.Async
import cats.effect.std.Dispatcher
import com.example.repository.LoanDataRepo
import models.{*, given}
import org.typelevel.log4cats.Logger
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter

class Api[F[_]: Async: Dispatcher: Logger] private (repo: LoanDataRepo[F]) {
  private val queries = Queries[F](args => repo.findBy(args))
  val graphql         = graphQL(RootResolver(queries))
}

object Api {
  def apply[F[_]: Async: Dispatcher: Logger](repo: LoanDataRepo[F]): Api[F] = new Api(repo)
}
