package cz.muni.fi.pv168.books;

import java.util.List;

public interface LeaseManager {

    List<Lease> getLeasesForCustomer(Customer c);

    void createLease(Lease lease);

    List<Book> getAvailableBooks();

}
