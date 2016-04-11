-- schema-javadb.sql
-- DDL commands for JavaDB/Derby
CREATE TABLE books (
  id     INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name   VARCHAR(70),
  author VARCHAR(45)
);

CREATE TABLE customers (
  id       INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  fullname VARCHAR(50),
  address  VARCHAR(150),
  phone    VARCHAR(20),
  email    VARCHAR(50)
);

CREATE TABLE leases (
  id          INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  bookId      INT REFERENCES books (id) ON DELETE CASCADE,
  customerId  INT REFERENCES customers (id) ON DELETE CASCADE,
  startDate   DATE,
  expectedEnd DATE,
  realEnd     DATE
);