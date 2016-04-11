package cz.muni.fi.pv168.books;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class) //Spring se zúčastní unit testů
@ContextConfiguration(classes = {MySpringTestConfig.class}) //konfigurace je ve třídě MySpringTestConfig
@Transactional //každý test poběží ve vlastní transakci, která bude na konci rollbackována
public class CustomerManagerImplTest {

    @Autowired //Spring injektuje odkaz na implementaci
    private CustomerManager customerManager;

    @Test
    public void testGetCustomerById() throws Exception {
        Customer c1 = customerManager.getCustomerById(1);
        assertThat(c1, is(notNullValue()));
        assertThat(c1.getFullname(), is(equalTo("Karel Čtvrtý")));
    }

    @Test
    public void testCreateCustomer() throws Exception {
        Customer c2 = new Customer(null, "Jan Novák", "Dlouhá 1", "603123456", "novak@gmail.com");
        customerManager.createCustomer(c2);
        assertThat("customer id", c2.getId(), notNullValue());
        Customer c3 = customerManager.getCustomerById(c2.getId());
        assertThat(c3, is(equalTo(c2)));
    }

    @Test
    public void testDeleteCustomer() throws Exception {
        customerManager.deleteCustomer(1);
        try {
            customerManager.getCustomerById(1);
            fail("customer 1 not deleted");
        } catch (EmptyResultDataAccessException e) {
            //no code
        }
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        Customer c1 = customerManager.getCustomerById(1);
        c1.setAddress("Krátká 3");
        c1.setPhone("2222");
        c1.setEmail("a@b.com");
        c1.setFullname("Pepa První");
        customerManager.updateCustomer(c1);
        Customer c2 = customerManager.getCustomerById(1);
        assertThat(c2, is(equalTo(c1)));
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        assertThat(customerManager.getAllCustomers(), hasItem(customerManager.getCustomerById(1)));
    }
}
