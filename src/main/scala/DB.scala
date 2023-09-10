import cats.effect._
import doobie.hikari.HikariTransactor
import com.zaxxer.hikari.HikariConfig
import org.flywaydb.core.Flyway

object DB {
  def transactor(): Resource[IO, HikariTransactor[IO]] =
    for {
      hikariConfig <- Resource.pure {
        val config = new HikariConfig()
        config.setDriverClassName("org.h2.Driver")
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        config.setUsername("sa")
        config.setPassword("")
        config
      }
      xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
    } yield xa

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
      }
    }
  }

}
