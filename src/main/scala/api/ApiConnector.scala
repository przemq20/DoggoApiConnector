package api

import akka.actor.typed.ActorSystem

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

class ApiConnector(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) {
  val apiList: List[PhotoApi] = List(
    new DogCeo(),
    new TheDogApi(),
    new RandomWoof()
  )

  val random = new Random()
  def randomApi: PhotoApi = apiList(random.nextInt(apiList.length))

  def getPhotoUrl(format: Option[String] = None, breed: Option[String] = None): Future[String] = {
    def getPhotoUrlRec(format: String, breed: Option[String] = None): Future[String] = {
      randomApi
        .getPhotoUrl(breed)
        .flatMap(
          s =>
            s.split('.').last match {
              case photoFormat if photoFormat == format => Future(s)
              case _                                    => getPhotoUrlRec(format, breed)
            }
        )
    }

    if (format.isDefined) {
      getPhotoUrlRec(format.get.toLowerCase, breed)
    } else {
      randomApi.getPhotoUrl(breed)
    }
  }

  def getPhoto: Future[Array[Byte]] = {
    randomApi.getPhoto
  }

  def getPhotoByUrl(photoUrl: String): Future[Array[Byte]] = {
    randomApi.getPhotoByUrl(photoUrl)
  }
}
