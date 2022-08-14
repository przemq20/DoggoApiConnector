package api

import akka.actor.typed.ActorSystem

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

class ApiConnector(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) {
  val apiList: List[PhotoApi] = List(
    new DogCeo(),
    new TheDogApi()
  )

  val random = new Random()
  def randomApi: PhotoApi = apiList(random.nextInt(apiList.length))

  def getPhotoUrl: Future[String] = {
    randomApi.getPhotoUrl
  }

  def getPhoto: Future[Array[Byte]] = {
    randomApi.getPhoto
  }
}
