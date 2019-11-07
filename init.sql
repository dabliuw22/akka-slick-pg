CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    score DECIMAL(10, 2) NOT NULL,
    role_id INTEGER NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles(name) VALUES('USER_ROLE');
INSERT INTO roles(name) VALUES('ADMIN_ROLE');

INSERT INTO users(name, score, role_id) VALUES('username1', 10.5, 1);
INSERT INTO users(name, score, role_id) VALUES('username2', 14.5, 1);
INSERT INTO users(name, score, role_id) VALUES('username3', 20.0, 1);
INSERT INTO users(name, score, role_id) VALUES('username4', 22.2, 1);
INSERT INTO users(name, score, role_id) VALUES('username5', 10.5, 1);
INSERT INTO users(name, score, role_id) VALUES('username6', 9.5, 1);
INSERT INTO users(name, score, role_id) VALUES('username7', 8.5, 1);
INSERT INTO users(name, score, role_id) VALUES('username8', 12.5, 2);