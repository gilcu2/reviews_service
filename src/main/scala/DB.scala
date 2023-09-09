import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

object DB {
  def transactor(): IO[HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO]("org.h2.Driver",
      "jdbc:h2:mem:ch14;DB_CLOSE_DELAY=-1",
      "jose",
      ""
    )
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = new Flyway()
        flyWay.setLocations("classpath:db_migrations")
        flyWay.setDataSource(dataSource)
        flyWay.migrate()
      }
    }
  }
}
