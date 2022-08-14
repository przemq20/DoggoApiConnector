package api

import com.typesafe.config.Config

import scala.concurrent.Future
trait PhotoApi {
  val config: Config
  val token:  Option[String]
  val url:    String
  val prefix: String
  def getPhotoUrl: Future[String]
  def getPhoto: Future[Array[Byte]]

  def conf(variable: String): String = {
    val envVar = s"$prefix.$variable".replace(".", "_")
    if (System.getenv(envVar) == null) config.getString(s"$prefix.$variable") else System.getenv(envVar)
  }
}
