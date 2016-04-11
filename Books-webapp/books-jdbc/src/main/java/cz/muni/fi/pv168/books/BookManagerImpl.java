package cz.muni.fi.pv168.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookManagerImpl implements BookManager {

    final static Logger log = LoggerFactory.getLogger(BookManagerImpl.class);
    private final DataSource dataSource;

    public BookManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Book> getAllBooks() throws BookException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("select * from books")) {
                ResultSet rs = st.executeQuery();
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String author = rs.getString("author");
                    books.add(new Book(id, name, author));
                }
                log.debug("getting all {} books",books.size());
                return books;
            }
        } catch (SQLException e) {
            log.error("cannot select books", e);
            throw new BookException("database select failed", e);
        }
    }

    @Override
    public void createBook(Book book) throws BookException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("insert into books (name,author) values (?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                st.setString(1, book.getName());
                st.setString(2, book.getAuthor());
                st.executeUpdate();
                ResultSet keys = st.getGeneratedKeys();
                if (keys.next()) {
                    book.setId(keys.getLong(1));
                }
                log.debug("created book {}",book);
            }
        } catch (SQLException e) {
            log.error("cannot insert book", e);
            throw new BookException("database insert failed", e);
        }
    }

    @Override
    public Book getBookById(Long id) throws BookException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("select * from books where id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Long nid = rs.getLong("id");
                    String name = rs.getString("name");
                    String author = rs.getString("author");
                    return new Book(nid, name, author);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("cannot select books", e);
            throw new BookException("database select failed", e);
        }
    }

    @Override
    public void updateBook(Book book) throws BookException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("update books set name=?, author=? where id=?")) {
                st.setString(1, book.getName());
                st.setString(2, book.getAuthor());
                st.setLong(3, book.getId());
                int n = st.executeUpdate();
                if (n == 0) {
                    throw new BookException("not updated book with id " + book.getId(), null);
                }
                if (n > 1) {
                    throw new BookException("more than 1 book with id " + book.getId(), null);
                }
                log.debug("updated book {}",book);
            }
        } catch (SQLException e) {
            log.error("cannot update books", e);
            throw new BookException("database update failed", e);
        }
    }

    @Override
    public void deleteBook(Long id) throws BookException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("delete from books where id=?")) {
                st.setLong(1, id);
                int n = st.executeUpdate();
                if (n == 0) {
                    throw new BookException("not deleted book with id " + id, null);
                }
                log.debug("deleted book {}",id);
            }
        } catch (SQLException e) {
            log.error("cannot delete book", e);
            throw new BookException("database delete failed", e);
        }
    }
}
