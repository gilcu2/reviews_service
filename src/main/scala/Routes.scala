import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import Model._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
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
      for {
        getResult <- repository.getAllStats()
        response <- reviewResult(getResult)
      } yield response
  }

  private def reviewResult(result: Either[ReviewNotFoundError.type, Review]) = {
    result match {
      case Left(ReviewNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }

}
