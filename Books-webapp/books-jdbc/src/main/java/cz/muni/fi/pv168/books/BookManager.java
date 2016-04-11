package cz.muni.fi.pv168.books;

import java.util.List;

public interface BookManager {

    List<Book> getAllBooks() throws BookException;

    void createBook(Book book) throws BookException;

    //atd.

    Book getBookById(Long id) throws BookException;

    void updateBook(Book book) throws BookException;

    void deleteBook(Long id) throws BookException;

}
