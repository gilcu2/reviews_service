import Model.{AirportReviewCount, Review}
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import debug._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers

class RepositoryComposeResourceTest extends flatspec.AsyncFlatSpec with AsyncIOSpec
  with  GivenWhenThen with Matchers {

  def makeDB(t:HikariTransactor[IO]): Resource[IO, Unit] =Resource
    .make(DB.initialize(t).debug_thread)(
      _ => IO("releasing DB").debug_thread.void
    )


  val transactor: Resource[IO, HikariTransactor[IO]] =for {
    t <- DB.transactor()
    _ <- makeDB(t)
  } yield t


  "Repository" should "create and retrieve a Review" in {
    Given("Review")
    val review = Review(airport_name = "FRA", title = "title",
      author = "author", content = "content")

    When("save to repo and load result")
    val r: IO[Review] = transactor.use(t =>
      for {
        _ <- IO.println("begin create and retrieve a Review")
        repository = new Repository(t)
        returned <- repository.createReview(review).debug_thread
        saved <- repository.getReview(returned.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.airport_name shouldBe review.airport_name).debug_thread

  }

  "Server" should "retrieve All Reviews Statistics" in {
    Given("Some reviews in the DB")
    val reviews = Array(
      Review(airport_name = "as1", title = "t1", author = "a1", content = "c1"),
      Review(airport_name = "as1", title = "t2", author = "a1", content = "c1"),
      Review(airport_name = "as2", title = "t3", author = "a1", content = "c1")
    )

    And("The expected values")
    val expected = List(
      AirportReviewCount("as1", 2),
      AirportReviewCount("as2", 1)
    )

    When("save reviews to repo and get stats")
    val r: IO[List[AirportReviewCount]] = transactor.use(t =>
      for {
        _ <- IO.println("begin retrieve All Reviews Statistics")
        repository = new Repository(t)
        _ <- repository.createReview(reviews(0))
        _ <- repository.createReview(reviews(1))
        _ <- repository.createReview(reviews(2))
        stats <- repository.getAllStats("as").compile.toList
      } yield stats )

    Then("results are the expected")
    r.asserting(_ shouldBe expected)

  }

}
