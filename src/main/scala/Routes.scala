import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._


object Routes {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" => Ok("Hi")
    case GET -> Root / "hello" / name => Ok(s"Hi $name")
  }

}
