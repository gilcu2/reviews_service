import cats.data.Kleisli
import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import doobie.hikari.HikariTransactor
import org.http4s.{Request, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling

object Server {

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def create(): IO[ExitCode] = {
    resources().use(create)
  }

  private def create(resources: Resources): IO[ExitCode] = {
    for {
      _ <- DB.initialize(resources.transactor)
      repository = new Repository(resources.transactor)
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(withErrorLogging(repository)).build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }

  private def withErrorLogging(repository: Repository): Kleisli[IO, Request[IO], Response[IO]] =
    ErrorHandling.Recover.total(
    ErrorAction.log(
      new Routes(repository).routes.orNotFound,
      messageFailureLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t),
      serviceErrorLogAction = (t, msg) =>
        IO.println(msg) >>
          IO.println(t)
    )
  )

  private def resources(): Resource[IO, Resources] = {
    for {
      transactor <- DB.transactor()
    } yield Resources(transactor)
  }

  private case class Resources(transactor: HikariTransactor[IO])

}
