package cz.muni.fi.pv168.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaseManagerImpl implements LeaseManager {

    final static Logger log = LoggerFactory.getLogger(LeaseManagerImpl.class);

    private JdbcTemplate jdbc;
    private BookManager bookManager;
    private CustomerManager customerManager;

    public LeaseManagerImpl(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
    }

    public void setBookManager(BookManager bookManager) {
        this.bookManager = bookManager;
    }

    public void setCustomerManager(CustomerManager customerManager) {
        this.customerManager = customerManager;
    }

    @Override
    public List<Lease> getLeasesForCustomer(final Customer c) {
        return jdbc.query("SELECT * FROM leases WHERE customerId=?", new RowMapper<Lease>() {
            @Override
            public Lease mapRow(ResultSet rs, int rowNum) throws SQLException {
                long bookId = rs.getLong("bookId");
                Book book = null;
                try {
                    book = bookManager.getBookById(bookId);
                } catch (BookException e) {
                    log.error("cannot find book", e);
                }
                return new Lease(rs.getLong("id"), book, c, rs.getDate("startdate"), rs.getDate("expectedend"), rs.getDate("realend"));
            }
        },
                c.getId());
    }

    @Override
    public void createLease(Lease lease) {
        SimpleJdbcInsert insertLease = new SimpleJdbcInsert(jdbc).withTableName("leases").usingGeneratedKeyColumns("id");
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("bookId", lease.getBook().getId());
        parameters.put("customerId", lease.getCustomer().getId());
        parameters.put("startdate", lease.getStartdate());
        parameters.put("expectedend", lease.getExpectedend());
        parameters.put("realend", lease.getRealend());
        Number id = insertLease.executeAndReturnKey(parameters);
        lease.setId(id.longValue());
    }

    @Override
    public List<Book> getAvailableBooks() {
        List<Long> ids = jdbc.queryForList("SELECT id FROM books WHERE id NOT IN (SELECT bookid FROM leases WHERE realend is null)", Long.class);
        List<Book> books = new ArrayList<>(ids.size());
        for (Long id : ids) {
            try {
                books.add(bookManager.getBookById(id));
            } catch (BookException e) {
                log.error("book problem",e);
            }
        }
        return books;
    }
}
