package api

import akka.actor.typed.ActorSystem

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

class ApiConnector(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) {
  val random = new Random()
  val apiList: List[PhotoApi] = List(
    new DogCeo(),
    new TheDogApi(),
    new RandomWoof()
  )

  def randomApi(breedSupport: Boolean = false): PhotoApi = {
    if (breedSupport)
      apiList(random.nextInt(apiList.length))
    else
      randomApiWithBreedsSupport
  }

  @tailrec
  final def randomApiWithBreedsSupport: PhotoApi = {
    val api = apiList(random.nextInt(apiList.length))

    if (api.breedsSupported)
      api
    else
      randomApiWithBreedsSupport
  }

  def getPhotoUrl(format: Option[String] = None, breed: Option[String] = None): Future[String] = {
    def getPhotoUrlRec(format: String, breed: Option[String] = None): Future[String] = {
      randomApi(breed.isDefined)
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
      randomApi(breed.isDefined).getPhotoUrl(breed)
    }
  }

  def getPhoto: Future[Array[Byte]] = {
    randomApi().getPhoto
  }

  def getPhotoByUrl(photoUrl: String): Future[Array[Byte]] = {
    randomApi().getPhotoByUrl(photoUrl)
  }
}
