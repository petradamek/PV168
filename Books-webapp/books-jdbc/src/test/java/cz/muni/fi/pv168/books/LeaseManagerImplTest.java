package cz.muni.fi.pv168.books;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class) //Spring se zúčastní unit testů
@ContextConfiguration(classes = {MySpringTestConfig.class}) //konfigurace je ve třídě MySpringTestConfig
@Transactional //každý test poběží ve vlastní transakci, která bude na konci rollbackována
public class LeaseManagerImplTest {

    @Autowired //Spring injektuje odkaz na implementaci
    private CustomerManager customerManager;
    @Autowired
    private BookManager bookManager;
    @Autowired
    private LeaseManager leaseManager;

    @Test
    public void testCreateLease() throws Exception {
        Book book = bookManager.getBookById(1l);
        Customer customer = customerManager.getCustomerById(1L);
        Date now = new Date();
        GregorianCalendar gc = new GregorianCalendar(2013, Calendar.DECEMBER, 10);
        Lease lease = new Lease(null, book, customer, now, gc.getTime(), null);
        leaseManager.createLease(lease);
        Assert.assertThat(lease.getId(), notNullValue());
    }

    @Test
    public void testGetLeasesForCustomer() throws Exception {
        List<Lease> leaseList = leaseManager.getLeasesForCustomer(customerManager.getCustomerById(1L));
        assertThat("number of all leases", leaseList.size(), is(1));
    }

    @Test
    public void testAvailableBooks() throws Exception {
        List<Book> availableBooks = leaseManager.getAvailableBooks();
        assertThat(availableBooks.get(0).getId(),equalTo(2L));
    }
}
