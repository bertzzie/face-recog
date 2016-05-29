package controllers

import akka.actor.ActorSystem
import javax.inject._

import akka.util.Timeout
import models.{EmotionAPIResponse, FaceAPIResponse, FaceRectangle}
import play.api._
import play.api.libs.Files
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.{StreamedBody, WSClient}
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
 * run code after a delay.
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.
 */
@Singleton
class AsyncController @Inject()(actorSystem: ActorSystem, ws: WSClient)(implicit exec: ExecutionContext) extends Controller {
  def allowCrossOrigin = Action.async { request =>
    Future.successful {
      Ok.withHeaders(
        "Access-Control-Allow-Origin"  -> "*",
        "Access-Control-Allow-Methods" -> "POST",
        "Access-Control-Allow-Headers" -> "*",
        "Access-Control-Max-Age"       -> "1728000"
      )
    }
  }

  def analyzeFace = Action(parse.multipartFormData) { request =>
    request.body.file("face").map {
      case picture: MultipartFormData.FilePart[Files.TemporaryFile] =>
        val file = picture.ref.file

        val emotionAPIResponse = for {
          far <- faceAPI(file)
          ear <- emotionAPI(file, far.faceRectangle)
        } yield ear

        val timeout  = Timeout(120 seconds)
        val response = Await.result(emotionAPIResponse, 120 seconds)

        Ok(Json.obj("emotion" -> response.scores.bestScore, "face" -> response.faceRectangle))
          .withHeaders("Access-Control-Allow-Origin" -> "*")

      case _ => BadRequest("File unreadable").withHeaders("Access-Control-Allow-Origin" -> "*")
    }.getOrElse {
      BadRequest("Invalid file").withHeaders("Access-Control-Allow-Origin" -> "*")
    }
  }

  def faceAPI(file: java.io.File): Future[FaceAPIResponse] = {
    Logger.info("Calling Face API")
    ws.url("https://api.projectoxford.ai/face/v1.0/detect")
      .withHeaders(
        "Ocp-Apim-Subscription-Key" -> "85b7f914ef2d4525b6853376dd01fd77",
        "Content-Type"              -> "application/octet-stream"
      )
      .post(file)
      .map { response =>
        Logger.info("Face API returned:")
        Logger.info(response.json.toString)
        response.json.validate[Seq[FaceAPIResponse]] match {
          case success: JsSuccess[Seq[FaceAPIResponse]] => success.get.head
          case error: JsError                      => throw new Exception(error.errors.toString)
        }
      }
  }

  def emotionAPI(file: java.io.File, fr: FaceRectangle) = {
    val url = s"https://api.projectoxford.ai/emotion/v1.0/recognize?faceRectangles=${fr.left.toInt},${fr.top.toInt},${fr.width.toInt},${fr.height.toInt}"
    Logger.info("Calling emotion API with URL:")
    Logger.info(url)
    ws.url(url)
      .withHeaders(
        "Ocp-Apim-Subscription-Key" -> "5991c47b21b24974b5e96a9e8bba2add",
        "Content-Type"              -> "application/octet-stream"
      )
      .post(file)
      .map { response =>
        Logger.info("Face API returned:")
        Logger.info(response.json.toString)
        response.json.validate[Seq[EmotionAPIResponse]] match {
          case success: JsSuccess[Seq[EmotionAPIResponse]] => success.get.head
          case error: JsError => throw new Exception(error.errors.toString)
        }
      }
  }
}
