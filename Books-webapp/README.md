# PV168 - web application

Example web applications for the PV168 course.

It has two modules:
* **books-jdbc** - JDBC implementation of a library
* **books-web** - web application written using Servlets, JSP and JSTL

Run the following commands to run it from the command line:
```
mvn install
cd books-web
mvn cargo:run
```

then visit the URL [http://localhost:8080/books-web/](http://localhost:8080/books-web/)
