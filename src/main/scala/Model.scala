object Model {
  case class Review(id: Option[Long]=None,
                    airport_name: String="",
                    link: String="",
                    title: String="",
                    author: String="",
                    author_country: String="",
                    date: String="",
                    content: String="",
                    experience_airport: String="",
                    date_visit: String="",
                    type_traveller: String="",
                    overall_rating: Int=0,
                    queuing_rating: Int=0,
                    terminal_cleanliness_rating: Int=0,
                    terminal_seating_rating: Int=0,
                    terminal_signs_rating: Int=0,
                    food_beverages_rating: Int=0,
                    airport_shopping_rating: Int=0,
                    wifi_connectivity_rating: Int=0,
                    airport_staff_rating: Int=0,
                    recommended: Boolean=false
                   )

  case object ReviewNotFoundError

}
