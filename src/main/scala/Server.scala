import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import doobie.hikari.HikariTransactor
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

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
        .withHttpApp(new Routes(repository).routes.orNotFound).build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }

  private def resources(): Resource[IO, Resources] = {
    for {
      transactor <- DB.transactor()
    } yield Resources(transactor)
  }

  case class Resources(transactor: HikariTransactor[IO])

}
