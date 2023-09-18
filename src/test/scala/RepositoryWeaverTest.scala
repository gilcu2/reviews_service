import weaver.IOSuite
import cats.effect._

object RepositoryWeaverTest extends IOSuite {

  type Res = Int

  def sharedResource : Resource[IO, Int] = Resource
    .make(
      IO(println("Making resource"))
        .as(123)
    )(n => IO(println(s"Closing resource $n")))

  test("test, but resource not visible"){
    IO(expect(123 == 123))
  }

  test("test with resource"){ n =>
    IO(expect(n == 123))
  }

  test("test with resource and a logger"){ (n, log) =>
    log.info("log was available") *>
      IO(expect(n == 123))
  }
}
