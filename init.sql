CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    role_id INTEGER NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

INSERT INTO roles(name) VALUES('USER_ROLE');
INSERT INTO roles(name) VALUES('ADMIN_ROLE');

INSERT INTO users(name, role_id) VALUES('username1', 1);
INSERT INTO users(name, role_id) VALUES('username2', 1);
INSERT INTO users(name, role_id) VALUES('username3', 1);
INSERT INTO users(name, role_id) VALUES('username4', 1);
INSERT INTO users(name, role_id) VALUES('username5', 1);
INSERT INTO users(name, role_id) VALUES('username6', 1);
INSERT INTO users(name, role_id) VALUES('username7', 1);
INSERT INTO users(name, role_id) VALUES('username8', 2);

INSERT INTO items(name) VALUES('items1');
INSERT INTO items(name) VALUES('items2');
INSERT INTO items(name) VALUES('items3');
INSERT INTO items(name) VALUES('items4');
INSERT INTO items(name) VALUES('items5');
INSERT INTO items(name) VALUES('items6');
INSERT INTO items(name) VALUES('items7');
INSERT INTO items(name) VALUES('items8');