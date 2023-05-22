DROP DATABASE IF EXISTS retail;
DROP SCHEMA IF EXISTS retail CASCADE;

CREATE DATABASE retail;
CREATE SCHEMA retail;

SET search_path TO retail;

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
        street_number varchar(50)
);

CREATE TABLE store_type (
    id SERIAL PRIMARY KEY,
    name varchar(20)
);

CREATE TABLE store (
    id SERIAL PRIMARY KEY,
    store_type_id INT,
    location_id INT
);

CREATE TABLE product (
    id SERIAL UNIQUE,
    product_type_id INT,
    name varchar(100),
    price varchar(10)
);

CREATE TABLE inventory (
    id SERIAL,
    store_id INT,
    product_id INT,
    quantity INT,
    CONSTRAINT unique_product_store UNIQUE (product_id, store_id)
);