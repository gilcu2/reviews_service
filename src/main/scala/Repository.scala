import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import Model._

class Repository(transactor: Transactor[IO]) {

  def createTest(review: Review): IO[Review] = {
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
            ${review.airport_name},
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

            )
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => review.copy(id = Some(id))
      }
  }

  def createReview(review: Review): IO[Review] = {
    val airport_name=review.airport_name
    val sql_query=sql"""
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
    val sql_query_updated=sql_query
        .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => review.copy(id = Some(id))
      }
  }

  def getReview(id: Long): IO[Either[ReviewNotFoundError.type, Review]] = {
    sql"SELECT * FROM review WHERE id = $id"
      .query[Review].option.transact(transactor).map {
        case Some(todo) => Right(todo)
        case None => Left(ReviewNotFoundError)
      }
  }

}
