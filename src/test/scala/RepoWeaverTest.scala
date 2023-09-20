import BasicWeaverTest.expect
import Model.{AirportReviewCount, Review}
import cats.effect._
import weaver.IOSuite
import doobie.hikari.HikariTransactor
import com.zaxxer.hikari.HikariConfig
import de.lhns.doobie.flyway.Flyway
import debug._
import cats.syntax.all._

object RepoWeaverTest extends IOSuite {

  //  type Res = Int

  override type Res = HikariTransactor[IO]

  override def sharedResource: Resource[IO, Res] =
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
      _ <- Flyway(xa) { flyway =>
        for {
          _ <- flyway
            .migrate().debug_thread
        } yield ()
      }
    } yield xa

  test("test create and get review") { tr => {
    val review = Review(airport_name = "FRA", title = "title",
      author = "author", content = "content")
    val r = for {
      _ <- IO.println("begin put get test")
      repository = new Repository(tr)
      returned <- repository.createReview(review).debug_thread
      saved_result <- repository.getReview(returned.id.get).debug_thread
      saved = saved_result.toOption.get
      t1 = saved.airport_name == review.airport_name
      t2 = saved.id.isDefined
      r = expect(t1 && t2)
    } yield r

    r
  }
  }

  test("test get statistics") { tr => {
    //  Given("Some reviews in the DB")
    val reviews = List(
      Review(airport_name = "as1", title = "t1", author = "a1", content = "c1"),
      Review(airport_name = "as1", title = "t2", author = "a1", content = "c1"),
      Review(airport_name = "as2", title = "t3", author = "a1", content = "c1")
    )

    // And("The expected values")
    val expected = List(
      AirportReviewCount("as1", 2),
      AirportReviewCount("as2", 1)
    )

    // When("save reviews to repo and get stats")
    for {
      _ <- IO.println("begin retrieve All Reviews Statistics")
      repository = new Repository(tr)
      _ <- reviews.traverse(repository.createReview)
      stats <- repository.getAllStats("as").compile.toList
      r = expect(stats == expected)
    } yield r

  }
  }


}
