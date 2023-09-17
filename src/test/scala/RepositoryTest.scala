import Model.Review
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import debug._

class RepositoryTest extends flatspec.AsyncFlatSpec with AsyncIOSpec with
  GivenWhenThen with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()
  val repository_io: IO[Repository] = transactor.use(t =>
  for {
    _ <- DB.initialize(t).debug_thread
    r = new Repository(t)
  } yield r
  )

  "Repository" should "create and retrieve a Review" in {
    Given("Review")
    val review= Review(airport_name = "FRA", title = "title",
      author = "author", content = "content")

    When("save to repo and load result")
    val loaded_io=for {
      repository <- repository_io.debug_thread
      returned <- repository.createReview(review).debug_thread
      saved <- repository.getReview(returned.id.get).debug_thread
    } yield saved.toOption.get

    Then("Value is the expected")
    loaded_io.asserting(_.airport_name shouldBe review.airport_name).debug_thread

  }

}
