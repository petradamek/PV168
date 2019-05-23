# PV168 - web application

Example applications for the PV168 course.

It has three modules:
* **books-jdbc** - JDBC implementation of a library
* **books-web** - web application written using Servlets, JSP and JSTL
* **book-swing** - applicatin with Swing GUI

Run the following commands to run the web application from the command line:
```
mvn install
cd books-web
mvn cargo:run
```

then visit the URL [http://localhost:8080/books-web/](http://localhost:8080/books-web/)

Run the following commands to run the GUI application from the command line:
```
mvn install
cd books-swing
mvn exec:java
```

