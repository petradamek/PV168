package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.books.Customer;
import cz.muni.fi.pv168.books.CustomerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Handles customer data to be displayed in the custmer table.
 */
public class CustomersTableModel extends AbstractTableModel {

    private final static Logger log = LoggerFactory.getLogger(CustomersTableModel.class);

    private List<Customer> customers = new ArrayList<>();
    private CustomerManager customerManager;
    private Random random = new Random();

    CustomersTableModel(CustomerManager customerManager) {
        this.customerManager = customerManager;
        loadCustomersFromDb();
    }

    /**
     * Returns the number of rows in the model.
     */
    @Override
    public int getRowCount() {
        return customers.size();
    }

    /**
     * Returns the number of columns in the model.
     */
    @Override
    public int getColumnCount() {
        return 5;
    }

    /**
     * Returns titles for columns.
     */
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Main.texts.getString("customers.table.column.id");
            case 1:
                return Main.texts.getString("customers.table.column.fullname");
            case 2:
                return Main.texts.getString("customers.table.column.email");
            case 3:
                return Main.texts.getString("customers.table.column.phone");
            case 4:
                return Main.texts.getString("customers.table.column.address");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    /**
     * Returns the value for the table cell at <code>columnIndex</code> and <code>rowIndex</code>.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Customer customer = customers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return customer.getId();
            case 1:
                return customer.getFullname();
            case 2:
                return customer.getEmail();
            case 3:
                return customer.getPhone();
            case 4:
                return customer.getAddress();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    /**
     * Adds a new customer to both the database and the list of customers in memory.
     * @param customer customer with id==null
     */
    void addCustomer(Customer customer) {
        //creates the customer in database
        customerManager.createCustomer(customer);
        //adds the customer to the list in memory
        customers.add(customer);
        //notifies the table to redraw the last row
        int lastRow = customers.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    /**
     * Gets Customer object representing the table row.
     */
    Customer getCustomerAtRow(int row) {
        return customers.get(row);
    }

    /**
     * Deletes the customer at the specified table row.
     * @param row index in list of customer in memory
     */
    void deleteCustomerAtRow(int row) {
        Customer customer = customers.get(row);
        log.debug("deleting row {} with customer {}",row, customer);
        customerManager.deleteCustomer(customer.getId());
        customers.remove(row);
        fireTableRowsDeleted(row,row);
    }

    /**
     * Updates customer in database and reloads all customers in memory.
     */
    void updateCustomer(Customer customer) {
        log.debug("updating customer {}", customer);
        customerManager.updateCustomer(customer);
        loadCustomersFromDb();
    }

    /**
     * Loads all customers from database to memory. Runs in a separate thread.
     */
    private void loadCustomersFromDb() {
        new SwingWorker<List<Customer>, Void>() {

            @Override
            protected List<Customer> doInBackground() {
                return customerManager.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    customers = get();
                    fireTableDataChanged();
                } catch (InterruptedException | ExecutionException ex) {
                    log.error("cannot load customers from database", ex);
                }
            }
        }.execute();
    }

    /**
     * Generates a new customer with random data.
     */
    void addRandomCustomer() {
        Customer customer = new Customer();
        customer.setFullname(rnd(FIRST_NAMES)+" "+rnd(LAST_NAMES));
        customer.setEmail(rnd(CONS)+rnd(VOWS)+rnd(CONS)+rnd(VOWS)+rnd(CONS)+random.nextInt(200)+"@"+rnd(DOMAINS));
        customer.setPhone("+420"+(random.nextInt(500000000)+500000000));
        customer.setAddress(rnd(STREETS)+" "+random.nextInt(300)+", "+rnd(TOWNS));
        addCustomer(customer);
    }
    private static final String[] FIRST_NAMES = { "Jan", "Jakub", "Martin", "Petr", "Pavel", "Michal", "Ondřej", "Aleš", "Boris", "Mirek"};
    private static final String[] LAST_NAMES = { "Novák", "Růžička", "Svoboda", "Procházka", "Černý", "Kučera", "Veselý", "Fiala", "Hájek", "Pospíšil"};
    private static final String[] CONS = { "m", "n", "k", "r", "v", "p" };
    private static final String[] VOWS = { "a", "e", "i", "o", "u", "y" };
    private static final String[] DOMAINS = { "gmail.com", "seznam.cz", "centrum.cz", "muni.cz", "cesnet.cz"};
    private static final String[] STREETS = { "Severní", "Jižní", "Pivoňková", "Lesní", "Járy Cimrmana", "Masarykova", "Okružní", "Šumavská", "Botanická"};
    private static final String[] TOWNS = { "Brno", "Praha", "Olomouc", "Okrouhlá Radouň", "Nedvězí", "Ejpovice", "Velké Meziříčí"};
    private String rnd(String[] strings) {
        return strings[random.nextInt(strings.length)];
    }




}
