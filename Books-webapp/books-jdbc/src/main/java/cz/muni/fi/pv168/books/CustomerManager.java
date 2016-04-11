package cz.muni.fi.pv168.books;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface CustomerManager {

    void deleteCustomer(long id);

    void updateCustomer(Customer customer);

    void createCustomer(Customer customer);

    List<Customer> getAllCustomers();

    Customer getCustomerById(long id);

}
