import cats.effect._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Server.create()
}
