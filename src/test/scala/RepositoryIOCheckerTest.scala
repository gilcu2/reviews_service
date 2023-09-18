import Model.Review
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import debug._
import doobie._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import doobie.scalatest.IOChecker
//
//class RepositoryIOCheckerTest extends flatspec.AnyFlatSpec with IOChecker with
//  GivenWhenThen with Matchers {
//
//  val transactor = Transactor.fromDriverManager[IO](
//    driver = "org.h2.Driver",
//    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
//    user = "sa",
//    password = "",
//    logHandler = None
//  )
//  val repository=new Repository(transactor)
//
//  "Repository" should "create and retrieve a Review" in {
//    Given("Review")
//    val review= Review(airport_name = "FRA", title = "title",
//      author = "author", content = "content")
//
//    When("save to repo and load result")
//    check(repository.createReview(review))
//
//  }
//
//}
