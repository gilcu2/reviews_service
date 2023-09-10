import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import Model.Review
import fs2.Stream


class RepositoryTest extends CatsEffectSuite {

  test("put and get Review") {
    for {
      xa <- Stream.eval(DB.transactor())
      _ <- Stream.eval(DB.initialize(xa))
      repository <- new Repository(xa)
      review <- IO.pure(Review(airport_name = "FRA"))
      put_result <- repository.putReview(review)
      get_result <- repository.getReview(put_result.id.get)
    } yield {
      assertEquals(get_result.toOption.get.airport_name, review.airport_name)
    }
  }

}
