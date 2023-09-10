import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import Model._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._


class Routes(repository: Repository) {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" => Ok("Hi")

    case GET -> Root / "hello" / name => Ok(s"Hi $name")

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
  }

  private def reviewResult(result: Either[ReviewNotFoundError.type, Review]) = {
    result match {
      case Left(ReviewNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }

}
