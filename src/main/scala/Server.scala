import cats.data.Kleisli
import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import doobie.hikari.HikariTransactor
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.{Request, Response}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import config.Config

object Server {

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def create(configFile: String = "application.conf"): IO[ExitCode] = {
    resources(configFile).use(create)
  }


  private def create(resources: Resources): IO[ExitCode] = {
    for {
      _ <- DB.initialize(resources.transactor)
      repository = new Repository(resources.transactor)
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(resources.config.server.port, resources.config.server.host)
        .withHttpApp(withErrorLogging(repository))
        .serve
        .compile
        .lastOrError
    } yield exitCode
  }

  private def withErrorLogging(repository: Repository):
  Kleisli[IO, Request[IO], Response[IO]] =
    ErrorHandling.Recover.total(
      ErrorAction.log(
        Router("/api" -> new Routes(repository).routes).orNotFound,
        messageFailureLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t),
        serviceErrorLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t)
      )
    )

  private def resources(configFile: String): Resource[IO, Resources] = {
    for {
      config <- Config.load(configFile)
      transactor <- DB.transactor()
    } yield Resources(transactor, config)
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)

}
