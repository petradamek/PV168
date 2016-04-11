package cz.muni.fi.pv168.books;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerManagerImpl implements CustomerManager {

    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;

    public CustomerManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @Override
    public void deleteCustomer(long id) {
        jdbc.update("DELETE FROM customers WHERE id=?", id);
    }

    @Override
    public void updateCustomer(Customer c) {
        jdbc.update("UPDATE customers set fullname=?,address=?,phone=?,email=? where id=?",
                c.getFullname(), c.getAddress(), c.getPhone(), c.getEmail(), c.getId());
    }

    private RowMapper<Customer> customerMapper = new RowMapper<Customer>() {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Customer(rs.getLong("id"), rs.getString("fullname"), rs.getString("address"), rs.getString("phone"), rs.getString("email"));
        }
    };

    @Override
    public List<Customer> getAllCustomers() {
        //transakce, tady zbytečná, ale pro ukázku
        return transaction.execute(new TransactionCallback<List<Customer>>() {
            @Override
            public List<Customer> doInTransaction(TransactionStatus status) {
                return jdbc.query("SELECT * FROM customers", customerMapper);
            }
        });
    }

    @Override
    public Customer getCustomerById(long id) {
        return jdbc.queryForObject("SELECT * FROM customers WHERE id=?", customerMapper, id);
    }

    @Override
    public void createCustomer(Customer c) {
        SimpleJdbcInsert insertCustomer = new SimpleJdbcInsert(jdbc).withTableName("customers").usingGeneratedKeyColumns("id");
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("fullname", c.getFullname());
        parameters.put("address", c.getAddress());
        parameters.put("phone", c.getPhone());
        parameters.put("email", c.getEmail());
        Number id = insertCustomer.executeAndReturnKey(parameters);
        c.setId(id.longValue());
    }

}
