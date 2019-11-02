CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

INSERT INTO users(name) VALUES('username1');
INSERT INTO users(name) VALUES('username2');
INSERT INTO users(name) VALUES('username3');
INSERT INTO users(name) VALUES('username4');
INSERT INTO users(name) VALUES('username5');
INSERT INTO users(name) VALUES('username6');
INSERT INTO users(name) VALUES('username7');
INSERT INTO users(name) VALUES('username8');

INSERT INTO items(name) VALUES('items1');
INSERT INTO items(name) VALUES('items2');
INSERT INTO items(name) VALUES('items3');
INSERT INTO items(name) VALUES('items4');
INSERT INTO items(name) VALUES('items5');
INSERT INTO items(name) VALUES('items6');
INSERT INTO items(name) VALUES('items7');
INSERT INTO items(name) VALUES('items8');