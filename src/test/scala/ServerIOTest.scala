//import Model.Review
//import Server.Resources
//import cats.effect._
//import cats.effect.testing.scalatest.AsyncIOSpec
//import config.Config
//import io.circe.Json
//import org.http4s.{Method, Request, Uri}
//import org.scalatest.matchers.should.Matchers
//import org.scalatest._
//import io.circe.literal._
//import io.circe.generic.auto._
//import io.circe.syntax._
//import org.http4s.client.Client
//
//class MySpec extends flatspec.AsyncFlatSpec with AsyncIOSpec with
//  GivenWhenThen with Matchers {
//
//  private val configFile = "test.conf"
//
//  def createApp(resources: Resources) = for {
//    _ <- DB.initialize(resources.transactor)
//    repository = new Repository(resources.transactor)
//    app <- new Routes(repository).routes.orNotFound
//  } yield app
//
//
//  "Server" should "create and retrieve a Review" in {
//    Given("Review json")
//    val json_string =
//      json"""
//        {
//          "airport_name":"FRA",
//          "title": "title",
//          "author": "author",
//          "content": "content"
//        }
//        """
//
//    And("The expected result")
//    val expected = Review(airport_name = "FRA", title = "title",
//      author = "author", content = "content")
//
//    When("Post the Review")
//    val requestPost = Request[IO](method = Method.POST,
//      uri = Uri.unsafeFromString(s"$rootUrl/review"))
//      .withEntity(json_string)
//    val jsonPost = client.use(_.expect[Json](requestPost)).unsafeRunSync()
//    val resultPost = jsonPost.as[Review].toOption.get
//
//    Then("result is the expected")
//    assert(resultPost.airport_name == expected.airport_name)
//
//    And("Given the id")
//    val id = resultPost.id.get
//
//    When("Get the the Review")
//    val requestGet = Request[IO](method = Method.GET,
//      uri = Uri.unsafeFromString(s"$rootUrl/review/$id"))
//    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
//    val resultGet = jsonGet.as[Review].toOption.get
//
//    Then("result is the expected")
//    assert(resultGet.airport_name == expected.airport_name)
//  }
//
//}