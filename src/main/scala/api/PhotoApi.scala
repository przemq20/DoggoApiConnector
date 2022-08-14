package api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpHeader, HttpRequest }
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._
trait PhotoApi {
  val config: Config = ConfigFactory.load()
  val token:  Option[String]
  val url:    String;
  val prefix: String

  def getEnvVar(variable: String): String = s"$prefix.$variable".replace(".", "_")

  def getConfString(variable: String): Option[String] = {
    val envVar: String = getEnvVar(variable)
    if (System.getenv(envVar) == null) {
      if (config.getConfig(s"$prefix").hasPath(s"$variable"))
        Some(config.getString(s"$prefix.$variable"))
      else None
    } else {
      Some(System.getenv(envVar))
    }
  }

  def getConfigStringList(variable: String): List[String] = {
    val envVar = getEnvVar(variable)
    if (System.getenv(envVar) == null) {
      if (config.getConfig(s"$prefix").hasPath(s"$variable"))
        config.getStringList(s"$prefix.$variable").asScala.toList
      else Nil
    } else {
      List(System.getenv(envVar))
    }
  }

  def headers: Seq[HttpHeader] =
    Seq(
//    RawHeader("Content-type", "application/json"),
      RawHeader("Charset", "UTF-8")
    ) ++ (if (token.isDefined) Seq(RawHeader("X-Auth-Token", token.get)) else Seq.empty)

  def getPhotoUrl: Future[String]
  def getPhoto(implicit actorSystem: ActorSystem[Any], executionContext: ExecutionContext): Future[Array[Byte]] = {
    val photoUrl = getPhotoUrl
    val request  = photoUrl.flatMap(url => Http().singleRequest(HttpRequest(uri = url).withHeaders(headers)))
    val response = request.flatMap(resp => Unmarshal(resp.entity).to[Array[Byte]])

    response
  }

}
