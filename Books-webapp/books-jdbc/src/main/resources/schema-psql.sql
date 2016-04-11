  -- schema-psql.sql
  -- DDL commands for PostgreSQL
  DROP TABLE IF EXISTS leases;
  DROP TABLE IF EXISTS customers;
  DROP TABLE IF EXISTS books;

 CREATE TABLE books (
   id     SERIAL PRIMARY KEY,
   name   VARCHAR,
   author VARCHAR);

 CREATE TABLE customers (
   id       SERIAL PRIMARY KEY,
   fullname VARCHAR,
   address  VARCHAR,
   phone    VARCHAR,
   email    VARCHAR);

 CREATE TABLE leases (
   id          SERIAL PRIMARY KEY,
   bookId      INT REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE,
   customerId  INT REFERENCES customers(id) ON DELETE CASCADE ON UPDATE CASCADE,
   startDate   DATE,
   expectedEnd DATE,
   realEnd     DATE);