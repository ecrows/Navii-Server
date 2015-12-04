Use naviDB;

CREATE TABLE IF NOT EXISTS users (
	user_id INT NOT NULL AUTO_INCREMENT,
	username VARCHAR(16) NOT NULL,
	password CHAR(64) NOT NULL,
	salt CHAR(64) NOT NULL,
  	is_facebook TINYINT(1),
	PRIMARY KEY (user_id),
	UNIQUE(username)
);

CREATE TABLE IF NOT EXISTS attractions (
	attractionid INT NOT NULL AUTO_INCREMENT,	
	name VARCHAR(256),
	location VARCHAR(256),
	photoURI VARCHAR(256),
	blurbURI VARCHAR(256),
	price INT,
  purchase CHAR(1),
	duration INT,
	PRIMARY KEY (attractionid)
);

CREATE TABLE IF NOT EXISTS itineraries (
	itineraryid INT NOT NULL AUTO_INCREMENT,
	totalcost DECIMAL(10,2),
	duration INT,
    description VARCHAR(256),
	authorid INT,
	PRIMARY KEY (itineraryid),
	FOREIGN KEY (authorid) REFERENCES users(userid) ON DELETE SET NULL
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

CREATE TABLE IF NOT EXISTS types (
	typeid INT,
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

CREATE TABLE IF NOT EXISTS preferences (
	preference VARCHAR(10) NOT NULL,
    counter INT DEFAULT 0,
    photoURL VARCHAR(32),
    PRIMARY KEY (preference)
);

CREATE TABLE IF NOT EXISTS tags (
	tag VARCHAR(10) NOT NULL,
    counter INT DEFAULT 0,
    PRIMARY KEY (tag)
);

CREATE TABLE IF NOT EXISTS userspreferences (
	username VARCHAR(16) NOT NULL,
    preference VARCHAR(10) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (preference) REFERENCES preferences(preference) ON DELETE CASCADE,
    CONSTRAINT pk_userpreference PRIMARY KEY (username,preference)
);

CREATE TABLE IF NOT EXISTS itinerariespreferences (
	itineraryid INT,
    preference VARCHAR(10),
    UNIQUE KEY (itineraryid,preference),
    FOREIGN KEY (itineraryid) REFERENCES itineraries(itineraryid) ON DELETE CASCADE,
    FOREIGN KEY (preference) REFERENCES preferences(preference) ON DELETE CASCADE,
    CONSTRAINT pk_itinerarypreference PRIMARY KEY (itineraryid,preference)
);


CREATE TABLE IF NOT EXISTS itinerariestags (
	itineraryid INT,
    tag VARCHAR(10),
    UNIQUE KEY (itineraryid,tag),
    FOREIGN KEY (itineraryid) REFERENCES itineraries(itineraryid) ON DELETE CASCADE,
    FOREIGN KEY (tag) REFERENCES tags(tag) ON DELETE CASCADE,
    CONSTRAINT pk_itinerarytag PRIMARY KEY (tag,itineraryid)
);
