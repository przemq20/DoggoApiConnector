package api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

class DogCeo(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) extends PhotoApi {
  val className: String = getClass.getName.split('.').last

  val prefix = s"${config.getString("App.name")}.$className" //ApiConnector.DogCeo
  override val token: Option[String] = getConfString("credentials.token")
  override val url:   String         = getConfString("environment.url").get + getConfString("environment.randomImage").get

  override def getPhotoUrl: Future[String] = {
    println(url)
    val breeds  = getConfStringList("environment.breeds")
    val request = Http().singleRequest(HttpRequest(uri = url).withHeaders(headers))
    val response = request
      .flatMap(resp => Unmarshal(resp.entity).to[String])
      .map(rawJson => rawJson.parseJson.asJsObject.fields("message").convertTo[String])

    response

  }
}
