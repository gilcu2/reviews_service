import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import Model.Review


class RepositoryTest extends CatsEffectSuite {

  test("put and get Review") {
    for {
      xa <- DB.transactor()
      _ <- DB.initialize(xa)
      repository <- IO.pure(new Repository(xa))
      review <- IO.pure(Review(airport_name = "FRA"))
      put_result <- repository.putReview(review)
      get_result <- repository.getReview(put_result.id.get)
    } yield {
      assertEquals(get_result.toOption.get.airport_name, review.airport_name)
    }
  }

}
