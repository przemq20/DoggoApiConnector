import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import api.TheDogApi
import http.Router

import scala.concurrent.ExecutionContext

object Main extends App {
  implicit val system:           ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext: ExecutionContext = system.executionContext

  val api    = new TheDogApi()
  val router = new Router(api)

  val serverSource = Http().newServerAt("localhost", 8080).bind(router.routes)
}
