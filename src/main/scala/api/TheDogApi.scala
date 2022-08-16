package api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

class TheDogApi(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) extends PhotoApi {
  val className: String = getClass.getName.split('.').last

  val prefix = s"${config.getString("App.name")}.$className" //ApiConnector.TheDogApi
  override val token: Option[String] = getConfString("credentials.token")
  override val url:   String         = getConfString("environment.url").get
  override val breedsSupported: Boolean = true

  override def getPhotoUrl(breedOpt: Option[String] = None): Future[String] = {
    val request = Http().singleRequest(HttpRequest(uri = url).withHeaders(headers))
    val response = request
      .flatMap(resp => Unmarshal(resp.entity).to[String])
      .map(rawJson => rawJson.parseJson.asInstanceOf[JsArray].elements(0).asJsObject.fields("url").convertTo[String])

    response
  }
}
