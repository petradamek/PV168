package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Petr Adámek
 */
public class BodyManagerImplTest {
    
    private BodyManagerImpl manager;
    private DataSource ds;

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //we will use in memory database
        ds.setUrl("jdbc:derby:memory:gravemgr-test;create=true");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,GraveManager.class.getResource("createTables.sql"));
        manager = new BodyManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,GraveManager.class.getResource("dropTables.sql"));
    }

    private Date date(String date) {
        return Date.valueOf(date);
    }    
    
    @Test
    public void createBody() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);

        Long bodyId = body.getId();
        assertNotNull(bodyId);
        Body result = manager.getBody(bodyId);
        assertEquals(body, result);
        assertNotSame(body, result);
        assertBodyDeepEquals(body, result);
    }
    
    @Test
    public void getBody() {
        
        assertNull(manager.getBody(1l));
        
        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);
        Long bodyId = body.getId();

        Body result = manager.getBody(bodyId);
        assertEquals(body, result);
        assertBodyDeepEquals(body, result);
    }

    @Test
    public void findAllBodies() {

        assertTrue(manager.findAllBodies().isEmpty());

        Body b1 = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        Body b2 = newBody("Body 2",date("1962-01-21"),date("2001-12-01"),false);

        manager.createBody(b1);
        manager.createBody(b2);

        List<Body> expected = Arrays.asList(b1,b2);
        List<Body> actual = manager.findAllBodies();
                
        assertBodyCollectionDeepEquals(expected, actual);
    }

    @Test
    public void createBodyWithWrongAttributes() {

        try {
            manager.createBody(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("2011-11-08"),false);
        body.setId(1l);
        try {
            manager.createBody(body);
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        body = newBody(null,date("1962-10-21"),date("2011-11-08"),false);
        try {
            manager.createBody(body);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        body = newBody("Pepa z Depa",date("1962-10-21"),date("1962-10-20"),false);
        try {
            manager.createBody(body);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        // these variants should be ok
        body = newBody("Pepa z Depa",null,date("2011-11-08"),true);
        manager.createBody(body);
        Body result = manager.getBody(body.getId()); 
        assertNotNull(result);
        assertBodyDeepEquals(body, result);

        body = newBody("Pepa z Depa",date("1962-10-21"),null,true);
        manager.createBody(body);
        result = manager.getBody(body.getId()); 
        assertNotNull(result);
        assertBodyDeepEquals(body, result);

        body = newBody("Pepa z Depa",date("1962-10-21"),date("1962-10-21"),true);
        manager.createBody(body);
        result = manager.getBody(body.getId()); 
        assertNotNull(result);
        assertBodyDeepEquals(body, result);
    }

    @Test
    public void updateBody() {
        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        Body b2 = newBody("Billy Bob",date("1921-02-06"),date("2008-12-11"),false);
        manager.createBody(body);
        manager.createBody(b2);
        Long bodyId = body.getId();
        Body result;
        
        body = manager.getBody(bodyId);
        body.setName("Pepik");
        manager.updateBody(body);        
        result = manager.getBody(bodyId);
        assertBodyDeepEquals(body, result);

        body = manager.getBody(bodyId);
        body.setBorn(date("1999-12-11"));
        manager.updateBody(body);
        result = manager.getBody(bodyId);
        assertBodyDeepEquals(body, result);

        body = manager.getBody(bodyId);
        body.setDied(date("1999-12-11"));
        manager.updateBody(body);
        result = manager.getBody(bodyId);
        assertBodyDeepEquals(body, result);

        body = manager.getBody(bodyId);
        body.setVampire(true);
        manager.updateBody(body);
        result = manager.getBody(bodyId);
        assertBodyDeepEquals(body, result);

        // Check if updates didn't affected other records
        assertBodyDeepEquals(b2, manager.getBody(b2.getId()));
    }

    @Test
    public void updateBodyWithWrongAttributes() {

        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);
        Long bodyId = body.getId();
        
        try {
            manager.updateBody(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        try {
            body = manager.getBody(bodyId);
            body.setId(null);
            manager.updateBody(body);        
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            body = manager.getBody(bodyId);
            body.setId(bodyId - 1);
            manager.updateBody(body);        
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            body = manager.getBody(bodyId);
            body.setName(null);
            manager.updateBody(body);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        try {
            body = manager.getBody(bodyId);
            body.setBorn(date("2011-11-09"));
            manager.updateBody(body);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }

    }

    @Test
    public void deleteBody() {

        Body b1 = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        Body b2 = newBody("Body 2",date("1962-01-21"),date("2001-12-01"),false);
        manager.createBody(b1);
        manager.createBody(b2);
        
        assertNotNull(manager.getBody(b1.getId()));
        assertNotNull(manager.getBody(b2.getId()));

        manager.deleteBody(b1);
        
        assertNull(manager.getBody(b1.getId()));
        assertNotNull(manager.getBody(b2.getId()));
                
    }

    @Test
    public void deleteBodyWithWrongAttributes() {

        Body body = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        
        try {
            manager.deleteBody(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            body.setId(null);
            manager.deleteBody(body);
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            body.setId(1l);
            manager.deleteBody(body);
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }        

    }
    
    static Body newBody(String name, Date born, Date died, boolean vampire) {
        Body body = new Body();
        body.setName(name);
        body.setBorn(born);
        body.setDied(died);
        body.setVampire(vampire);
        return body;
    }

    static void assertBodyDeepEquals(Body expected, Body actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getBorn(), actual.getBorn());
        assertEquals(expected.getDied(), actual.getDied());    
    } 
    
    private static Comparator<Body> bodyKeyComparator = new Comparator<Body>() {

        @Override
        public int compare(Body o1, Body o2) {
            Long k1 = o1.getId();
            Long k2 = o2.getId();
            if (k1 == null && k2 == null) {
                return 0;
            } else if (k1 == null && k2 != null) {
                return -1;
            } else if (k1 != null && k2 == null) {
                return 1;
            } else {
                return k1.compareTo(k2);
            }
        }
    };
    
    static void assertBodyCollectionDeepEquals(List<Body> expected, List<Body> actual) {
        
        assertEquals(expected.size(), actual.size());
        List<Body> expectedSortedList = new ArrayList<Body>(expected);
        List<Body> actualSortedList = new ArrayList<Body>(actual);
        Collections.sort(expectedSortedList,bodyKeyComparator);
        Collections.sort(actualSortedList,bodyKeyComparator);
        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertBodyDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
}
