object Model {
  case class Review(airport_name: String,
                    link: String,
                    title: String,
                    author: String,
                    author_country: String,
                    date: String,
                    content: String,
                    experience_airport: String,
                    date_visit: String,
                    type_traveller: String,
                    overall_rating: Int,
                    queuing_rating: Int,
                    terminal_cleanliness_rating: Int,
                    terminal_seating_rating: Int,
                    terminal_signs_rating: Int,
                    food_beverages_rating: Int,
                    airport_shopping_rating: Int,
                    wifi_connectivity_rating: Int,
                    airport_staff_rating: Int,
                    recommended: Boolean
                   )

}
