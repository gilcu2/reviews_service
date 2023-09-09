import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import Model.Review

class Repository(transactor: Transactor[IO]) {

  def getReview(id: Int): Stream[IO, Review] =
    getQuery( sql"SELECT * FROM review where id = $id")

  private def getQuery(query: Fragment) =
    query
      .query[Review]
      .stream
      .transact(transactor)

}
