/**
  * Created by alex on 5/29/16.
  */
package object models {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val formatFaceScore: Format[FaceScores] = (
    (__ \ "anger").format[Double] and
    (__ \ "contempt").format[Double] and
    (__ \ "disgust").format[Double] and
    (__ \ "fear").format[Double] and
    (__ \ "happiness").format[Double] and
    (__ \ "neutral").format[Double] and
    (__ \ "sadness").format[Double] and
    (__ \ "surprise").format[Double]
  )(FaceScores.apply, unlift(FaceScores.unapply))

  implicit val formatFaceRectangle: Format[FaceRectangle] = (
    (__ \ "top").format[Double] and
    (__ \ "left").format[Double] and
    (__ \ "width").format[Double] and
    (__ \ "height").format[Double]
  )(FaceRectangle.apply, unlift(FaceRectangle.unapply))

  implicit val formatEmotionAPIResponse: Format[EmotionAPIResponse] = (
    (__ \ "faceRectangle").format[FaceRectangle] and
    (__ \ "scores").format[FaceScores]
  )(EmotionAPIResponse.apply, unlift(EmotionAPIResponse.unapply))

  implicit val formatFaceAPIResponse: Format[FaceAPIResponse] = (
    (__ \ "faceId").format[String] and
    (__ \ "faceRectangle").format[FaceRectangle]
  )(FaceAPIResponse.apply, unlift(FaceAPIResponse.unapply))
}
