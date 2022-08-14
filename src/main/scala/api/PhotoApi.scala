package api

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
trait PhotoApi {
  val config: Config
  val token:  Option[String]
  val url:    String
  val prefix: String
  def getPhotoUrl: Future[String]
  def getPhoto:    Future[Array[Byte]]
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

  def headers: Seq[HttpHeader] = Seq(
//    RawHeader("Content-type", "application/json"),
    RawHeader("Charset", "UTF-8")
  ) ++ (if (token.isDefined) Seq(RawHeader("X-Auth-Token", token.get)) else Seq.empty)
}
