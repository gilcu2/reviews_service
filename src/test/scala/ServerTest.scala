import Model.Review
import cats.effect.IO
import cats.effect.{ExitCode, IO}
import cats.effect.unsafe.IORuntime
import io.circe.Json
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory
import org.scalatest._
import io.circe.literal._
import io.circe.generic.auto._, io.circe.syntax._

class ServerTest extends flatspec.AnyFlatSpec with Matchers
  with GivenWhenThen with BeforeAndAfterAll with Eventually {
  private lazy val client = BlazeClientBuilder[IO].resource

  private val rootUrl = s"http://localhost:8080/api"

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  private val logger = LoggerFactory.getLogger(classOf[ServerTest])

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)),
      interval = scaled(Span(100, Millis)))

  override def beforeAll(): Unit = {
    Server.create().unsafeRunAsync(resultHandler)
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
    val jsonGet = client.use(_.expect[Json](requestPost)).unsafeRunSync()
    val resultGet = jsonGet.as[Review].toOption.get

    Then("result is the expected")
    assert(resultGet.airport_name == expected.airport_name)
  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit = {
    result.left.foreach(t => logger.error("Executing the http server failed", t))
  }
}
