import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import Model._
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.headers.`Content-Type`
import org.typelevel.log4cats.LoggerFactory

object OptionalOverallRatingParamMatcher extends OptionalQueryParamDecoderMatcher[Double]("overall_rating")



class Routes(repository: Repository) {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "hello" => Ok("Hi")

    case GET -> Root / "review" / LongVar(id) =>
      for {
        getResult <- repository.getReview(id)
        response <- reviewResult(getResult)
      } yield response

    case req@POST -> Root / "review" =>
      for {
        review <- req.decodeJson[Review]
        createdReview <- repository.createReview(review)
        response <- Created(createdReview.asJson)
      } yield response

    case GET -> Root / "all" / "stats" =>
      Ok(
        Stream("[")
          ++ repository.getAllStats.map(_.asJson.noSpaces).intersperse(",")
          ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / airport_name / "stats" =>
      for {
        getResult <- repository.getAirportStats(airport_name)
        response <- reviewStats(getResult)
      } yield response

    case GET -> Root / airport_name / "reviews" :? OptionalOverallRatingParamMatcher(overall_rating) =>
      Ok(
        Stream("[")
          ++ repository.getAirportReviews(airport_name,overall_rating)
            .map(_.asJson.noSpaces).intersperse(",")
          ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case req@POST -> Root / airport_name/ "review" =>
      for {
        review <- req.decodeJson[Review]
        updated_review = review.copy(airport_name = airport_name)
        createdReview <- repository.createReview(updated_review)
        response <- Created(createdReview.asJson)
      } yield response
  }

  private def reviewResult(result: Either[ReviewNotFoundError.type, Review]) = {
    result match {
      case Left(ReviewNotFoundError) => NotFound()
      case Right(review) => Ok(review.asJson)
    }
  }

  private def reviewStats(stats: Either[AirportNotFoundError.type, AirportStats]) = {
    stats match {
      case Left(AirportNotFoundError) => NotFound()
      case Right(airport_stats) => Ok(airport_stats.asJson)
    }
  }

}
