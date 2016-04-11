package cz.muni.fi.pv168.books;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Lease {

    private Long id;
    private Book book;
    private Customer customer;
    private Date startdate;
    private Date expectedend;
    private Date realend;

    public Lease() {
    }

    public Lease(Long id, Book book, Customer customer, Date startdate, Date expectedend, Date realend) {
        this.id = id;
        this.book = book;
        this.customer = customer;
        this.startdate = startdate;
        this.expectedend = expectedend;
        this.realend = realend;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getExpectedend() {
        return expectedend;
    }

    public void setExpectedend(Date expectedend) {
        this.expectedend = expectedend;
    }

    public Date getRealend() {
        return realend;
    }

    public void setRealend(Date realend) {
        this.realend = realend;
    }

    @Override
    public String toString() {
        return "Lease{" +
                "id=" + id +
                ", book=" + book +
                ", customer=" + customer +
                ", startdate=" + startdate +
                ", expectedend=" + expectedend +
                ", realend=" + realend +
                '}';
    }
}
