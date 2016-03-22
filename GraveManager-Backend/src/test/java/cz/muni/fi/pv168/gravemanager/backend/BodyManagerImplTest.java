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
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 *
 * @author Petr Adámek
 */
public class BodyManagerImplTest {

    private BodyManagerImpl manager;
    private DataSource ds;

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:gravemgr-test");
        ds.setCreateDatabase("create");
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

    // Test exception with expected parameter of @Test annotation
    // it does not allow to specify exact place where the exception
    // is expected, therefor it is suitable only for simple single line tests
    @Test(expected = IllegalArgumentException.class)
    public void createNullBody() {
        manager.createBody(null);
    }

    // Test exception with ExpectedException @Rule
    @Test
    public void createBodyWithExistingId() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("2011-11-08"),false);
        body.setId(1l);
        expectedException.expect(IllegalEntityException.class);
        manager.createBody(body);
    }

    @Test
    public void createBodyWithNullName() {
        Body body = newBody(null,date("1962-10-21"),date("2011-11-08"),false);
        expectedException.expect(ValidationException.class);
        manager.createBody(body);
    }

    // This and next test are testing special cases with border values
    // Body died one day before born is not allowed ...
    @Test
    public void createBodyDeadBeforeBorn() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("1962-10-20"),false);
        expectedException.expect(ValidationException.class);
        manager.createBody(body);
    }

    // ... while the body died and born at the same day are allowed
    @Test
    public void createBodyBornAndDiedSameDay() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),date("1962-10-21"),false);
        manager.createBody(body);
        Body result = manager.getBody(body.getId());
        assertNotNull(result);
        assertBodyDeepEquals(body, result);
    }

    @Test
    public void createBodyNullBorn() {
        Body body = newBody("Pepa z Depa",null,date("2011-11-08"),true);
        manager.createBody(body);
        Body result = manager.getBody(body.getId());
        assertNotNull(result);
        assertBodyDeepEquals(body, result);
    }

    @Test
    public void createBodyNullDied() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),null,true);
        manager.createBody(body);
        Body result = manager.getBody(body.getId());
        assertNotNull(result);
        assertBodyDeepEquals(body, result);
    }

    private void updateBody(Consumer<Body> updateOperation) {
        Body sourceBody = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        Body anotherBody = newBody("Billy Bob",date("1921-02-06"),date("2008-12-11"),false);
        manager.createBody(sourceBody);
        manager.createBody(anotherBody);
        Long bodyId = sourceBody.getId();

        updateOperation.accept(sourceBody);

        manager.updateBody(sourceBody);
        assertBodyDeepEquals(sourceBody, manager.getBody(bodyId));

        // Check if updates didn't affected other records
        assertBodyDeepEquals(anotherBody, manager.getBody(anotherBody.getId()));
    }

    @Test
    public void updateBodyName() {
        updateBody((b) -> b.setName("Pepik"));
    }

    @Test
    public void updateBodyBorn() {
        updateBody((b) -> b.setBorn(date("1999-12-11")));
    }

    @Test
    public void updateBodyDied() {
        updateBody((b) -> b.setDied(date("1999-12-11")));
    }

    @Test
    public void updateBodyVampire() {
        updateBody((b) -> b.setVampire(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullBody() {
        manager.updateBody(null);
    }

    @Test
    public void updateBodyWithNullId() {
        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        expectedException.expect(IllegalEntityException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateNonExistingBody() {
        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);
        body.setId(body.getId() - 1);

        expectedException.expect(IllegalEntityException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateBodyWithNullName() {
        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);
        body.setName(null);

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateBodyWithBornAfterDied() {
        Body body = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        manager.createBody(body);
        body.setBorn(date("2011-11-09"));

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
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

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullBody() {
        manager.deleteBody(null);
    }

    @Test
    public void deleteBodyWithNullId() {
        Body body = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBody(body);
    }

    @Test
    public void deleteNonExistingBody() {
        Body body = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        body.setId(1L);
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBody(body);
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
