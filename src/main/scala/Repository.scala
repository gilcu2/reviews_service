import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import Model._

class Repository(transactor: Transactor[IO]) {

  def createReview(review: Review): IO[Review] = {
    val airport_name = review.airport_name
    val sql_query =
      sql"""
    INSERT INTO review (
          airport_name,
          link,
          title,
          author,
          author_country,
          date,
          content,
          experience_airport,
          date_visit,
          type_traveller,
          overall_rating,
          queuing_rating,
          terminal_cleanliness_rating,
          terminal_seating_rating,
          terminal_signs_rating,
          food_beverages_rating,
          airport_shopping_rating,
          wifi_connectivity_rating,
          airport_staff_rating,
          recommended
    )
    VALUES (
      $airport_name,
      ${review.link},
      ${review.title},
      ${review.author},
      ${review.author_country},
      ${review.date},
      ${review.content},
      ${review.experience_airport},
      ${review.date_visit},
      ${review.type_traveller},
      ${review.overall_rating},
      ${review.queuing_rating},
      ${review.terminal_cleanliness_rating},
      ${review.terminal_seating_rating},
      ${review.terminal_signs_rating},
      ${review.food_beverages_rating},
      ${review.airport_shopping_rating},
      ${review.wifi_connectivity_rating},
      ${review.airport_staff_rating},
      ${review.recommended}
    );
    """
    IO.println("Before update")
    val sql_query_updated = sql_query
      .update
    IO.println("After update")
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => review.copy(id = Some(id))
      }
  }

  def getReview(id: Long): IO[Either[ReviewNotFoundError.type, Review]] = {
    sql"SELECT * FROM review WHERE id = $id"
      .query[Review].option.transact(transactor).map {
        case Some(review) => Right(review)
        case None => Left(ReviewNotFoundError)
      }
  }

  def getAllStats: Stream[IO, AirportReviewCount] = {
    sql"""
         SELECT airport_name, count(*) as review_count
         FROM review
         GROUP BY airport_name
      """
      .query[AirportReviewCount].stream.transact(transactor)
  }

  def getAirportStats(airport_name: String)
  : IO[Either[AirportNotFoundError.type, AirportStats]] = {
    sql"""
         SELECT
            airport_name,
            count(*) as review_count,
            AVG(overall_rating) as average_overall_rating,
            SUM(recommended)
         FROM review
         WHERE airport_name = $airport_name
      """
      .query[AirportStats].option.transact(transactor).map {
        case Some(stat) => Right(stat)
        case None => Left(AirportNotFoundError)
      }
  }

  def getAirportReviews(airport_name: String, maybe_minimum_overall_rating: Option[Double] = None): Stream[IO, AirportReview] = {
    val minimum_overall_rating=maybe_minimum_overall_rating.getOrElse(0.0)
    sql"""
         SELECT
            airport_name,
            overall_rating,
            date,
            content,
            author,
            author_country
         FROM review
         WHERE airport_name = $airport_name AND overall_rating >= $minimum_overall_rating
      """
      .query[AirportReview].stream.transact(transactor)
  }

}
