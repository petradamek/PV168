package cz.muni.fi.pv168.books;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class) //Spring se zúčastní unit testů
@ContextConfiguration(classes = {MySpringTestConfig.class}) //konfigurace je ve třídě MySpringTestConfig
@Transactional //každý test poběží ve vlastní transakci, která bude na konci rollbackována
public class BookManagerImplTest {

    @Autowired
    private BookManager bookManager;

    @Test
    public void testGetAllBooks() throws Exception {
        List<Book> allBooks = bookManager.getAllBooks();
        Book book1 = bookManager.getBookById(1L);
        assertThat(allBooks, hasItem(book1));
        assertThat("number of all books", allBooks.size(), is(2));
    }

    @Test
    public void testCreateBook() throws Exception {
        Book b1 = new Book(null, "Egypťan Sinuhet", "Mika Waltari");
        bookManager.createBook(b1);
        assertThat("book id", b1.getId(), notNullValue());
        Book b2 = bookManager.getBookById(b1.getId());
        assertThat(b2, equalTo(b1));
    }

    @Test
    public void testGetBookById() throws Exception {
        Book book = bookManager.getBookById(1L);
        assertThat(book.getName(), is("Babička"));
    }

    @Test
    public void testUpdateBook() throws Exception {
        Book book = bookManager.getBookById(1L);
        book.setName("Dědeček");
        bookManager.updateBook(book);
        Book b2 = bookManager.getBookById(1L);
        assertThat(b2, equalTo(book));
    }

    @Test
    public void testDeleteBook() throws Exception {
        bookManager.deleteBook(1L);
        Book b2 = bookManager.getBookById(1L);
        assertThat(b2, is(nullValue()));
    }

}
