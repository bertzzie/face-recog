package models

/**
  * Created by alex on 5/29/16.
  */
case class FaceScores(anger: Double,
                      contempt: Double,
                      disgust: Double,
                      fear: Double,
                      happiness: Double,
                      neutral: Double,
                      sadness: Double,
                      surprise: Double) {
  def bestScore = {
    Map(
      "anger" -> anger, "contempt" -> contempt, "disgust" -> disgust, "fear" -> fear,
      "happiness" -> happiness, "neutral" -> neutral, "sadness" -> sadness, "surprise" -> surprise
    ).maxBy(_._2)._1
  }
}
