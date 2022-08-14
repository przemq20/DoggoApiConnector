package http

import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.ApiConnector

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class Router(api: ApiConnector) {
  implicit val timeout: FiniteDuration = 5.seconds

  val routes: Route =
    pathPrefix("url") {
      pathEndOrSingleSlash {
        get {
          onComplete(api.getPhotoUrl) {
            case Success(value)     => complete(value)
            case Failure(exception) => complete(exception)
          }
        }
      }
    } ~
      pathPrefix("photo") {
        pathEndOrSingleSlash {
          get {
            onComplete(api.getPhoto) {
              case Success(value)     => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(MediaTypes.`image/jpeg`, value)))
              case Failure(exception) => complete(exception)
            }
          }
        }
      }
}
