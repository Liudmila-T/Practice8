DROP DATABASE IF EXISTS practice8;

CREATE DATABASE practice8;

USE practice8;

CREATE TABLE users (
	id INT PRIMARY KEY auto_increment,
	login VARCHAR(15) UNIQUE NOT NULL
);

CREATE TABLE teams (
	id INT PRIMARY KEY auto_increment,
	name VARCHAR(15) UNIQUE NOT NULL
);

INSERT INTO users VALUES (DEFAULT, "ivanov");

INSERT INTO teams VALUES (1, "teamA");

SELECT * FROM users;
SELECT * FROM teams;


CREATE TABLE users_teams (
	user_id INT NOT NULL,
  team_id INT NOT NULL,
	PRIMARY KEY (user_id, team_id),
	FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
	FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);

SELECT * FROM users_teams;