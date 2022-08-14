package api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpHeader, HttpRequest }
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{ Config, ConfigFactory }
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

class TheDogApi(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) extends PhotoApi {
  override val config: Config = ConfigFactory.load()
  val className:       String = getClass.getName.split('.').last
  println(className)

  val prefix = s"${config.getString("App.name")}.$className" //ApiConnector.TheDogApi
  override val token: Option[String] = None

  override val url: String = conf("environment.url")

  val headers: Seq[HttpHeader] = Seq(
    RawHeader("Content-type", "application/json"),
    RawHeader("Charset", "UTF-8")
  ) ++ (if (token.isDefined) Seq(RawHeader("X-Auth-Token", token.get)) else Seq.empty)

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
