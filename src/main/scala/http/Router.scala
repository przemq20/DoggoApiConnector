package http

import akka.http.scaladsl.model._
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
        parameters("format".optional, "breed".optional) { (format, breed) =>
          get {
            onComplete(api.getPhotoUrl(format, breed)) {
              case Success(value) => complete(value)
              case Failure(exception) => complete(exception)
            }
          }
        }
      }
    } ~
      pathPrefix("photo") {
        pathEndOrSingleSlash {
          parameters("format".optional, "breed".optional) { (format, breed) =>
            get {
              onComplete(api.getPhotoUrl(format, breed)) {
                case Success(photoUrl) =>
                  onComplete(api.getPhotoByUrl(photoUrl)) {
                    case Success(byteArray) =>
                      val contentType: ContentType = photoUrl.split('.').last.toLowerCase match {
                        case "jpg" | "jpeg" => MediaTypes.`image/jpeg`
                        case "png" => MediaTypes.`image/png`
                        case "mp4" => MediaTypes.`video/mp4`
                        case "gif" => MediaTypes.`image/gif`
                        case _ => MediaTypes.`image/jpeg`
                      }
                      complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(contentType, byteArray)))
                    case Failure(exception) => complete(exception)
                  }
                case Failure(exception) => complete(exception)
              }
            }
          }
        }
      }
}
