Use naviDB;

CREATE TABLE IF NOT EXISTS users (
	email VARCHAR(90) NOT NULL,
	username VARCHAR(40) NOT NULL,
	password CHAR(64) NOT NULL,
	salt CHAR(64) NOT NULL,
	alive TINYINT(1), 
	is_facebook TINYINT(1),
	verified TINYINT(1),
	PRIMARY KEY (email),
	CONSTRAINT User_Email_unique UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS attractions (
	attractionid INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(256),
	address VARCHAR(256),
	photoURI VARCHAR(256),
	blurbURI VARCHAR(256),
	price INT,
	purchase CHAR(1),
	duration INT,
	latitude DECIMAL(11),
	longitude DECIMAL(11),
	description VARCHAR(128),
	phone_number VARCHAR(45),
	PRIMARY KEY (attractionid)
);

CREATE TABLE IF NOT EXISTS itineraries (
	itineraryid INT NOT NULL AUTO_INCREMENT,
	totalcost DECIMAL(10,2),
	duration INT,
	title VARCHAR(256),
	authorid VARCHAR(90),
	PRIMARY KEY (itineraryid),
	FOREIGN KEY (authorid) REFERENCES users(email) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS activities (
	activityid INT NOT NULL AUTO_INCREMENT,
	starttime DATETIME,
	endtime DATETIME,
	itineraryid INT,
	attractionid INT NOT NULL,
	PRIMARY KEY (activityid),
	FOREIGN KEY (attractionid) REFERENCES attractions(attractionid) ON DELETE CASCADE,
	FOREIGN KEY (itineraryid) REFERENCES itineraries(itineraryid) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cuisines (
	cuisineid INT,
	PRIMARY KEY (cuisineid)
);

CREATE TABLE IF NOT EXISTS atmospheres (
	atmosphereid INT,
	PRIMARY KEY (atmosphereid)
);

CREATE TABLE IF NOT EXISTS diets (
	dietid INT,
	PRIMARY KEY (dietid)
);

CREATE TABLE IF NOT EXISTS package_types (
	typeid TINYINT(1),
	item_name VARCHAR(16),
	PRIMARY KEY (typeid)
);

CREATE TABLE IF NOT EXISTS restaurants (
	restaurantid INT NOT NULL AUTO_INCREMENT,
	cuisineid INT,
	RankOfLife INT,
	atmosphereid INT,
	dietid INT,
	attractionid INT,
	PRIMARY KEY (restaurantid),
	FOREIGN KEY (attractionid) REFERENCES attractions(attractionid) ON DELETE CASCADE,
	FOREIGN KEY (cuisineid) REFERENCES cuisines(cuisineid) ON DELETE CASCADE,
	FOREIGN KEY (atmosphereid) REFERENCES atmospheres(atmosphereid) ON DELETE CASCADE,
	FOREIGN KEY (dietid) REFERENCES diets(dietid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pointofinterests (
	pointofinterestsid INT NOT NULL AUTO_INCREMENT,
	typeid INT,
	activityLevel INT,
	culturalauthenticity TINYINT(1),
	PRIMARY KEY (pointofinterestsid),
	FOREIGN KEY (typeid) REFERENCES types(typeid) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS preference_questions (
  preference_type INT,
  preference_question VARCHAR(256),
  PRIMARY KEY (preference_type)
);

CREATE TABLE IF NOT EXISTS tags (
	tag VARCHAR(64) NOT NULL,
	counter INT DEFAULT 0,
	PRIMARY KEY (tag)
);

CREATE TABLE IF NOT EXISTS preferences (
	preference VARCHAR(32) NOT NULL,
	counter INT DEFAULT 0,
	photoURL VARCHAR(32),
	preference_type INT,
	PRIMARY KEY (preference, preference_type),
  FOREIGN KEY (preference_type) REFERENCES preference_questions(preference_type) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS userspreferences (
	email VARCHAR(40) NOT NULL,
	preference VARCHAR(32) NOT NULL,
	preference_type INT NOT NULL,
	FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (preference) REFERENCES preferences(preference) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (preference_type) REFERENCES preferences(preference_type) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT pk_userpreference PRIMARY KEY (email,preference,preference_type)
);

CREATE TABLE IF NOT EXISTS attractionspreferences (
	attractionid INT,
	preference VARCHAR(10),
	UNIQUE KEY (attractionid,preference),
	FOREIGN KEY (attractionid) REFERENCES attractions(attractionid) ON DELETE CASCADE,
	FOREIGN KEY (preference) REFERENCES preferences(preference) ON DELETE CASCADE,
	CONSTRAINT pk_attractionpreference PRIMARY KEY (attractionid,preference)
);

CREATE TABLE IF NOT EXISTS attractionstags (
	attractionid INT,
	tag VARCHAR(10),
	UNIQUE KEY (attractionid,tag),
	FOREIGN KEY (attractionid) REFERENCES attractions(attractionid) ON DELETE CASCADE,
	FOREIGN KEY (tag) REFERENCES tags(tag) ON DELETE CASCADE,
	CONSTRAINT pk_attractiontag PRIMARY KEY (tag,attractionid)
);

CREATE TABLE IF NOT EXISTS yelp_categories (
	yelp_category VARCHAR(64) NOT NULL,
	PRIMARY KEY (yelp_category)
);

CREATE TABLE IF NOT EXISTS yelp_preference_category (
	yelp_category VARCHAR(64) NOT NULL, 
	preference VARCHAR(32) NOT NULL,
	FOREIGN KEY (preference) REFERENCES preferences(preference) ON DELETE CASCADE ON UPDATE CASCADE ,
	FOREIGN KEY (yelp_category) REFERENCES yelp_categories(yelp_category) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS yelp_filters (
	category VARCHAR(64) NOT NULL,
	PRIMARY KEY (category)
);

CREATE TABLE IF NOT EXISTS package_itinerary_map (
	itineraryid INT,
	typeid INT,
	position INT,
	attractionid INT NULL,
	FOREIGN KEY (itineraryid) REFERENCES itineraries(itineraryid) ON DELETE CASCADE,
	FOREIGN KEY (attractionid) REFERENCES attractions(attractionid) ON DELETE CASCADE,
	FOREIGN KEY (typeid) REFERENCES package_types(typeid) ON DELETE CASCADE ON UPDATE CASCADE
);
