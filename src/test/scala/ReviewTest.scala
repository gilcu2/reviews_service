import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import Model.Review
import io.circe.parser.decode
import io.circe.generic.auto._, io.circe.syntax._

class ReviewTest extends AnyFlatSpec with GivenWhenThen {

  "A Review" should "deserialize from some fields" in {
    Given("a json string")
    val json_string =
      """
        |{
        |  "airport_name":"FRA",
        |  "title": "title",
        |   "author": "author",
        |   "content": "content"
        |}
        |""".stripMargin

    And("the expected value")
    val expected = Review(airport_name = "FRA", title = "title",
      author = "author", content = "content")

    When("deserialize")
    val deserialized = decode[Review](json_string)

    Then("must be right")
    assert(deserialized.isRight)

    And("must be the expected")
    assert(deserialized.toOption.get == expected)

  }

}
