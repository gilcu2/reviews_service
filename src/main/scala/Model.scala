object Model {
  case class Review(id: Option[Long]=None,
                    airport_name: String,
                    link: Option[String]=None,
                    title: String,
                    author: String,
                    author_country: Option[String]=None,
                    date: Option[String]=None,
                    content: String,
                    experience_airport: Option[String]=None,
                    date_visit: Option[String]=None,
                    type_traveller: Option[String]=None,
                    overall_rating: Option[Int]=None,
                    queuing_rating: Option[Int]=None,
                    terminal_cleanliness_rating: Option[Int]=None,
                    terminal_seating_rating: Option[Int]=None,
                    terminal_signs_rating: Option[Int]=None,
                    food_beverages_rating: Option[Int]=None,
                    airport_shopping_rating: Option[Int]=None,
                    wifi_connectivity_rating: Option[Int]=None,
                    airport_staff_rating: Option[Int]=None,
                    recommended: Option[Int]=None,
                   )

  case object ReviewNotFoundError

  case class AirportReviewCount(airport_name: String,review_count:Int)
  case class AirportStats(airport_name: String,review_count:Int,
                          average_overall_rating:Double,recomended_count:Int)

  case object AirportNotFoundError

  case class AirportReview(airport_name: String,overall_rating:Int,
                           date:String,content:String,
                           author:String,author_country:String)

}
