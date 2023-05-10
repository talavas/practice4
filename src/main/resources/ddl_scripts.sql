DROP DATABASE IF EXISTS retail;
DROP SCHEMA IF EXISTS retail CASCADE;

CREATE DATABASE retail;
CREATE SCHEMA retail;

CREATE TABLE product_type (
    id SERIAL PRIMARY KEY,
    name varchar(100)
);

CREATE TABLE city (
    id SERIAL PRIMARY KEY,
    name varchar(50)
);

CREATE TABLE location (
        id SERIAL PRIMARY KEY,
        city_id INT,
        street_name varchar(50),
        street_number varchar(50),
        CONSTRAINT fk_city FOREIGN KEY (city_id) REFERENCES city (id)
);

CREATE TABLE store_type (
    id SERIAL PRIMARY KEY,
    name varchar(20)
);

CREATE TABLE store (
    id SERIAL PRIMARY KEY,
    store_type_id INT,
    location_id INT,
    CONSTRAINT fk_store_type FOREIGN KEY (store_type_id) REFERENCES store_type (id),
    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES location (id)

);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    product_type_id INT,
    name varchar(100),
    price float,
    CONSTRAINT fk_product_type FOREIGN KEY (product_type_id) REFERENCES product_type(id)
);

CREATE TABLE inventory (
    id SERIAL PRIMARY KEY,
    store_id INT,
    product_id INT,
    quantity INT,
    CONSTRAINT fk_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (id)
);