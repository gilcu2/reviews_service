import cats.effect.IO
import org.http4s._

class RoutesTest extends munit.Http4sHttpRoutesSuite {
  override val routes = Routes.routes

  test(GET(uri"hello" / "Jose")) { response =>
    assertIO(response.as[String], "Hi Jose")
  }

}
