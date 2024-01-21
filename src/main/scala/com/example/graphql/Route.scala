package com.example.graphql

import caliban.*
import caliban.interop.tapir.{HttpInterpreter, WebSocketInterpreter}
import cats.data.Kleisli
import cats.effect.Async
import cats.effect.std.Dispatcher
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import zio.Runtime

class Route[F[_]: Async: Dispatcher: Logger] private (
    interpreter: GraphQLInterpreter[Any, CalibanError],
    wsBuilder: WebSocketBuilder2[F]
)(using runtime: Runtime[Any]) {
  import sttp.tapir.json.circe.{*, given}

  val routes: HttpRoutes[F] = Router(
    "/api/graphql" -> CORS.policy(Http4sAdapter.makeHttpServiceF[F, Any, CalibanError](HttpInterpreter(interpreter))),
    "/ws/graphql"  -> CORS.policy(
      Http4sAdapter.makeWebSocketServiceF[F, Any, CalibanError](wsBuilder, WebSocketInterpreter(interpreter))
    ),
    "/graphiql"    -> Kleisli.liftF(StaticFile.fromResource[F]("/graphiql.html", None))
  )
}

object Route {
  def apply[F[_]: Async: Dispatcher: Logger](
      interpreter: GraphQLInterpreter[Any, CalibanError],
      wsBuilder: WebSocketBuilder2[F]
  )(using runtime: Runtime[Any]): Route[F] = new Route[F](interpreter, wsBuilder)
}
