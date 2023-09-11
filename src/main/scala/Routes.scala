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
        _ <- IO.println(s"Request: $review")
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

    case GET -> Root / airport_name / "reviews" =>
      Ok(
        Stream("[")
          ++ repository.getAirportReviews(airport_name)
            .map(_.asJson.noSpaces).intersperse(",")
          ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )
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
