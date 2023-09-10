CREATE TABLE review (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  airport_name VARCHAR,
  link VARCHAR,
  title VARCHAR,
  author VARCHAR,
  author_country VARCHAR,
  date VARCHAR,
  content VARCHAR,
  experience_airport VARCHAR,
  date_visit VARCHAR,
  type_traveller VARCHAR,
  overall_rating INTEGER,
  queuing_rating INTEGER,
  terminal_cleanliness_rating INTEGER,
  terminal_seating_rating INTEGER,
  terminal_signs_rating INTEGER,
  food_beverages_rating INTEGER,
  airport_shopping_rating INTEGER,
  wifi_connectivity_rating INTEGER,
  airport_staff_rating INTEGER,
  recommended INTEGER
);

--INSERT INTO review (
--    id,airport_name,title,author,content
--) values (
--    1,'airport_name','title','author','content'
--);


