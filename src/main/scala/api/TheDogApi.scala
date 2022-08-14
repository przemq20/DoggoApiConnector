package api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class TheDogApi(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) extends PhotoApi {
  override val config: Config = ConfigFactory.load()
  val className:       String = getClass.getName.split('.').last

  val prefix = s"${config.getString("App.name")}.$className" //ApiConnector.TheDogApi
  override val token: Option[String] = getConfString("credentials.token")

  override val url: String = getConfString("environment.url").get

  def getPhotoUrl: Future[String] = {
    val request = Http().singleRequest(HttpRequest(uri = url).withHeaders(headers))
    val response = request
      .flatMap(resp => Unmarshal(resp.entity).to[String])
      .map(rawJson => rawJson.parseJson.asInstanceOf[JsArray].elements(0).asJsObject.fields("url").convertTo[String])

    response
  }

  override def getPhoto: Future[Array[Byte]] = {
    val photoUrl = getPhotoUrl
    val request  = photoUrl.flatMap(url => Http().singleRequest(HttpRequest(uri = url).withHeaders(headers)))
    val response = request.flatMap(resp => Unmarshal(resp.entity).to[Array[Byte]])

    response
  }
}
