import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest._
import org.scalatest.matchers.should.Matchers

object maths {

  def ioSum(io1: IO[Int], io2: IO[Int]): IO[Int] = for {
    i1 <- io1
    i2 <- io2
  } yield i1+i2

}

class IOTest extends flatspec.AsyncFlatSpec with AsyncIOSpec with
  GivenWhenThen with Matchers {

  "iosum" should "sum the IO[Int]" in {
    Given("IO ints")
    val io1 = IO(1)
    val io2 = IO(2)

    When("sum")
    val io_sum = maths.ioSum(io1,io2)

    Then("Value is the expected")
    io_sum.asserting(_ shouldBe 3)

  }

}
