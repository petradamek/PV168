package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.books.BookManagerImpl;
import cz.muni.fi.pv168.books.CustomerManagerImpl;
import cz.muni.fi.pv168.books.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.IOException;

@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("webová aplikace inicializována");
        ServletContext servletContext = ev.getServletContext();
        try {
            DataSource dataSource = Main.getDataSource();
            servletContext.setAttribute("customerManager", new CustomerManagerImpl(dataSource));
            servletContext.setAttribute("bookManager", new BookManagerImpl(dataSource));
            log.info("vytvořeny manažery a uloženy do atributů servletContextu");
        } catch (IOException e) {
            log.error("Nepovedlo se vytvořit databázi", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("aplikace končí");
    }
}
