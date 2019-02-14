package cz.muni.fi.pv168.books;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);

    public static DataSource getDataSource() throws IOException {
        HikariDataSource ds = new HikariDataSource();

        //load connection properties from a file
        Properties p = new Properties();
        p.load(Main.class.getResourceAsStream("/jdbc.properties"));

        //set connection
        ds.setDriverClassName(p.getProperty("jdbc.driver"));
        ds.setJdbcUrl(p.getProperty("jdbc.url"));
        ds.setUsername(p.getProperty("jdbc.user"));
        ds.setPassword(p.getProperty("jdbc.password"));

        //populate db with tables and data
        new ResourceDatabasePopulator(
                new ClassPathResource("schema-javadb.sql"),
                new ClassPathResource("test-data.sql"))
                .execute(ds);
        return ds;
    }

    public static void main(String[] args) throws BookException, IOException {

        log.info("zaciname");

        DataSource dataSource = getDataSource();
        BookManager bookManager = new BookManagerImpl(dataSource);

        List<Book> allBooks = bookManager.getAllBooks();
        System.out.println("allBooks = " + allBooks);

    }

}
