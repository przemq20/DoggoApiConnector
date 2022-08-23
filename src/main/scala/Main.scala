import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import api.ApiConnector
import http.Router

import scala.concurrent.ExecutionContext

object Main extends App {
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContext = system.executionContext

  val api = new ApiConnector()
  val router = new Router(api)
  val port = if (System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

  val serverSource = Http().newServerAt("localhost", port).bind(router.routes)
}
