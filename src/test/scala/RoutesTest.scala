import cats.effect.IO
import org.http4s._

class RoutesTest extends munit.Http4sHttpRoutesSuite {
  override val routes: HttpRoutes[IO] = Routes.routes

  test(GET(uri"hello" / "Jose")).alias("Say hello to Jose") { response =>
    assertIO(response.as[String], "Hi Jose")
  }

}
