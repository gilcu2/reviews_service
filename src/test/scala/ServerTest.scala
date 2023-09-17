import Model.{AirportReview, AirportReviewCount, AirportStats, Review}
import cats.effect.IO
import cats.effect.{ExitCode, IO}
import cats.effect.unsafe.IORuntime
import config.Config
import io.circe.Json
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.slf4j.LoggerFactory
import org.scalatest._
import io.circe.literal._
import io.circe.generic.auto._
import io.circe.syntax._

class ServerTest extends flatspec.AnyFlatSpec with Matchers
  with GivenWhenThen with BeforeAndAfterAll with Eventually {
  private lazy val client = BlazeClientBuilder[IO].resource

  private val configFile = "test.conf"

  private lazy val config = Config.load(configFile).use(config => IO.pure(config)).unsafeRunSync()

  private lazy val rootUrl = s"http://${config.server.host}:${config.server.port}/api"

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  private val logger = LoggerFactory.getLogger(classOf[ServerTest])

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)),
      interval = scaled(Span(100, Millis)))

  override def beforeAll(): Unit = {
    Server.create(configFile).unsafeRunAsync(resultHandler)
    eventually {
      client.use(_.statusFromUri(Uri.unsafeFromString(s"$rootUrl/hello"))).unsafeRunSync() shouldBe Status.Ok
    }
    ()
  }

  "Server" should "create and retrieve a Review" in {
    Given("Review json")
    val json_string =
      json"""
        {
          "airport_name":"FRA",
          "title": "title",
          "author": "author",
          "content": "content"
        }
        """

    And("The expected result")
    val expected = Review(airport_name = "FRA", title = "title",
      author = "author", content = "content")

    When("Post the Review")
    val requestPost = Request[IO](method = Method.POST,
      uri = Uri.unsafeFromString(s"$rootUrl/review"))
      .withEntity(json_string)
    val jsonPost = client.use(_.expect[Json](requestPost)).unsafeRunSync()
    val resultPost = jsonPost.as[Review].toOption.get

    Then("result is the expected")
    assert(resultPost.airport_name == expected.airport_name)

    And("Given the id")
    val id = resultPost.id.get

    When("Get the the Review")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(s"$rootUrl/review/$id"))
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[Review].toOption.get

    Then("result is the expected")
    assert(resultGet.airport_name == expected.airport_name)
  }

  "Server" should "retrieve All Reviews Statistics" in {
    Given("Some reviews in the DB")
    val reviews = List(
      Review(airport_name = "a1", title = "t1", author = "a1", content = "c1"),
      Review(airport_name = "a1", title = "t2", author = "a1", content = "c1"),
      Review(airport_name = "a2", title = "t3", author = "a1", content = "c1")
    )
    reviews.foreach(r => {
      val r_json = r.asJson
      val requestPost = Request[IO](method = Method.POST,
        uri = Uri.unsafeFromString(s"$rootUrl/review"))
        .withEntity(r_json)
      client.use(_.expect[Json](requestPost)).unsafeRunSync()
    })

    And("The expected values")
    val expected = List(
      AirportReviewCount("a1", 2),
      AirportReviewCount("a2", 1)
    )

    When("retrieve the stats")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(s"$rootUrl/all/stats"))
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[List[AirportReviewCount]].toOption.get

    Then("results are the expected")
    assert(expected.toSet.subsetOf(resultGet.toSet))

  }

  "Server" should "retrieve Review Airport Statistics" in {
    Given("Some reviews in the DB")
    val reviews = List(
      Review(airport_name = "a3", title = "t1", author = "a1", content = "c1",
        overall_rating = Some(6), recommended = Some(1)),
      Review(airport_name = "a3", title = "t2", author = "a1", content = "c1",
        overall_rating = Some(4), recommended = Some(1)),
      Review(airport_name = "a3", title = "t3", author = "a1", content = "c1",
        overall_rating = Some(2), recommended = Some(0))
    )
    reviews.foreach(r => {
      val r_json = r.asJson
      val requestPost = Request[IO](method = Method.POST,
        uri = Uri.unsafeFromString(s"$rootUrl/review"))
        .withEntity(r_json)
      client.use(_.expect[Json](requestPost)).unsafeRunSync()
    })

    And("The expected values")
    val expected = AirportStats("a3", 3, 4.0, 2)

    When("retrieve the stats")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(s"$rootUrl/a3/stats"))
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[AirportStats].toOption.get

    Then("results are the expected")
    assert(resultGet == expected)

  }

  "Server" should "retrieve Reviews of given airport" in {
    Given("airport name")
    val airport_name = "a5"

    And("Some reviews of this airport in the DB")
    val reviews = List(
      Review(airport_name = airport_name, title = "t1", author = "a1", content = "c1",
        overall_rating = Some(6), recommended = Some(1),
        date = Option("1/1/23"), author_country = Option("de")
      ),
      Review(airport_name = airport_name, title = "t2", author = "a2", content = "c1",
        overall_rating = Some(5), recommended = Some(1),
        date = Option("1/1/23"), author_country = Option("de")
      )
    )
    reviews.foreach(r => {
      val r_json = r.asJson
      val requestPost = Request[IO](method = Method.POST,
        uri = Uri.unsafeFromString(s"$rootUrl/review"))
        .withEntity(r_json)
      client.use(_.expect[Json](requestPost)).unsafeRunSync()
    })

    And("The expected values")
    val expected = List(
      AirportReview(airport_name, 6, "1/1/23", "c1", "a1", "de"),
      AirportReview(airport_name, 5, "1/1/23", "c1", "a2", "de")
    )

    When("retrieve the reviews")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(s"$rootUrl/$airport_name/reviews"))
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[List[AirportReview]].toOption.get

    Then("results are the expected")
    assert(resultGet.toSet == expected.toSet)

  }

  "Server" should "retrieve Reviews of given airport with minimum overall rating" in {
    Given("airport name and minimum overall rating")
    val airport_name = "a6"
    val minimum_overall_rating = 6

    And("Some reviews of this airport in the DB")
    val reviews = List(
      Review(airport_name = airport_name, title = "t1", author = "a1", content = "c1",
        overall_rating = Some(6), recommended = Some(1),
        date = Option("1/1/23"), author_country = Option("de")
      ),
      Review(airport_name = airport_name, title = "t2", author = "a2", content = "c1",
        overall_rating = Some(5), recommended = Some(1),
        date = Option("1/1/23"), author_country = Option("de")
      ),
      Review(airport_name = airport_name, title = "t3", author = "a2", content = "c1",
        overall_rating = Some(7), recommended = Some(1),
        date = Option("1/1/23"), author_country = Option("de")
      )
    )
    reviews.foreach(r => {
      val r_json = r.asJson
      val requestPost = Request[IO](method = Method.POST,
        uri = Uri.unsafeFromString(s"$rootUrl/review"))
        .withEntity(r_json)
      client.use(_.expect[Json](requestPost)).unsafeRunSync()
    })

    And("The expected values")
    val expected = List(
      AirportReview(airport_name, 6, "1/1/23", "c1", "a1", "de"),
      AirportReview(airport_name, 7, "1/1/23", "c1", "a2", "de")
    )

    When("retrieve the reviews")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(
        s"$rootUrl/$airport_name/reviews?overall_rating=$minimum_overall_rating"
      )
    )
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[List[AirportReview]].toOption.get

    Then("results are the expected")
    assert(resultGet.toSet == expected.toSet)

  }


  "Server" should "create a Review with the given airport name" in {
    Given("Review json")
    val json_string =
      json"""
        {
          "airport_name":"MUN",
          "title": "title",
          "author": "author",
          "content": "content"
        }
        """

    And("another airport name")
    val airport_name = "BER"

    And("The expected result")
    val expected = Review(airport_name = "BER", title = "title",
      author = "author", content = "content")

    When("Post the Review")
    val requestPost = Request[IO](method = Method.POST,
      uri = Uri.unsafeFromString(s"$rootUrl/$airport_name/review"))
      .withEntity(json_string)
    val jsonPost = client.use(_.expect[Json](requestPost)).unsafeRunSync()
    val resultPost = jsonPost.as[Review].toOption.get

    Then("result is the expected")
    assert(resultPost.airport_name == expected.airport_name)

    And("Given the id")
    val id = resultPost.id.get

    When("Get the the Review")
    val requestGet = Request[IO](method = Method.GET,
      uri = Uri.unsafeFromString(s"$rootUrl/review/$id"))
    val jsonGet = client.use(_.expect[Json](requestGet)).unsafeRunSync()
    val resultGet = jsonGet.as[Review].toOption.get

    Then("result is the expected")
    assert(resultGet.airport_name == expected.airport_name)
  }


  private def resultHandler(result: Either[Throwable, ExitCode]): Unit = {
    result.left.foreach(t => logger.error("Executing the http server failed", t))
  }
}
