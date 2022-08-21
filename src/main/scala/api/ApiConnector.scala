package api

import akka.actor.typed.ActorSystem

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

class ApiConnector(implicit val actorSystem: ActorSystem[Any], implicit val executionContext: ExecutionContext) {
  val random     = new Random()
  val dogCeo     = new DogCeo()
  val theDogApi  = new TheDogApi()
  val randomWoof = new RandomWoof()

  val apiList: List[PhotoApi] = List(
    dogCeo,
    theDogApi,
    randomWoof
  )

  def getApi(breedSupport: Boolean = false, api: Option[PhotoApi] = None): PhotoApi = {
    if (api.isDefined) api.get
    else {
      if (breedSupport)
        apiList(random.nextInt(apiList.length))
      else
        randomApiWithBreedsSupport
    }
  }

  @tailrec
  final def randomApiWithBreedsSupport: PhotoApi = {
    val api = apiList(random.nextInt(apiList.length))

    if (api.breedsSupported)
      api
    else
      randomApiWithBreedsSupport
  }

  def getPhotoUrl(
    format:       Option[String] = None,
    breed:        Option[String] = None,
    preferredApi: Option[String] = None
  ): Future[String] = {

    def getPhotoUrlRec(format: String, breed: Option[String] = None, api: Option[PhotoApi] = None): Future[String] = {
      getApi(breed.isDefined, api)
        .getPhotoUrl(breed)
        .flatMap(
          s =>
            s.split('.').last match {
              case photoFormat if photoFormat == format => Future(s)
              case _                                    => getPhotoUrlRec(format, breed)
            }
        )
    }

    def getPhotoWithFormat(api: Option[PhotoApi]): Future[String] = {
      if (format.isDefined) {
        getPhotoUrlRec(format.get.toLowerCase, breed, api)
      } else {

        getApi(breed.isDefined, api).getPhotoUrl(breed)
      }
    }

    preferredApi match {
      case Some("TheDogApi") | Some("thedogapi") =>
        getPhotoWithFormat(Some(theDogApi))
      case Some("DogCeo") | Some("dogceo") =>
        getPhotoWithFormat(Some(dogCeo))
      case Some("RandomWoof") | Some("randomwoof") =>
        getPhotoWithFormat(Some(randomWoof))
      case _ =>
        getPhotoWithFormat(None)
    }
  }

  def getPhotoByUrl(photoUrl: String): Future[Array[Byte]] = {
    getApi().getPhotoByUrl(photoUrl)
  }
}
