package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import static cz.muni.fi.pv168.gravemanager.backend.BodyManagerImplTest.assertBodyCollectionDeepEquals;
import static cz.muni.fi.pv168.gravemanager.backend.BodyManagerImplTest.newBody;
import static cz.muni.fi.pv168.gravemanager.backend.GraveManagerImplTest.assertGraveCollectionDeepEquals;
import static cz.muni.fi.pv168.gravemanager.backend.GraveManagerImplTest.assertGraveDeepEquals;
import static cz.muni.fi.pv168.gravemanager.backend.GraveManagerImplTest.newGrave;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
 * @author petr
 */
public class CemeteryManagerImplTest {

    private CemeteryManagerImpl manager;
    private BodyManagerImpl bodyManager;
    private GraveManagerImpl graveManager;
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

    private Grave g1, g2, g3, graveWithNullId, graveNotInDB;
    private Body b1, b2, b3, b4, b5, bodyWithNullId, bodyNotInDB;

    private void prepareTestData() {

        g1 = newGrave(1, 2, 1, "Grave 1");
        g2 = newGrave(10, 11, 2, "Grave 2");
        g3 = newGrave(2, 2, 3, "Grave 3");

        b1 = newBody("Body 1", null, null, false);
        b2 = newBody("Body 2", null, null, true);
        b3 = newBody("Body 3", null, null, false);
        b4 = newBody("Body 4", null, null, false);
        b5 = newBody("Body 5", null, null, false);

        bodyManager.createBody(b1);
        bodyManager.createBody(b2);
        bodyManager.createBody(b3);
        bodyManager.createBody(b4);
        bodyManager.createBody(b5);

        graveManager.createGrave(g1);
        graveManager.createGrave(g2);
        graveManager.createGrave(g3);

        graveWithNullId = newGrave(1,1,1,"Grave with null id");
        graveNotInDB = newGrave(1,1,1,"Grave not in DB");
        graveNotInDB.setId(g3.getId() + 100);
        bodyWithNullId = newBody("Body with null id", null, null, true);
        bodyNotInDB = newBody("Body not in DB", null, null, true);
        bodyNotInDB.setId(b5.getId() + 100);

    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, GraveManager.class.getResource("createTables.sql"));
        manager = new CemeteryManagerImpl();
        manager.setDataSource(ds);
        bodyManager = new BodyManagerImpl();
        bodyManager.setDataSource(ds);
        graveManager = new GraveManagerImpl();
        graveManager.setDataSource(ds);
        prepareTestData();
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds, GraveManager.class.getResource("dropTables.sql"));
    }

    @Test
    public void findGraveWithBody() {

        assertNull(manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertNull(manager.findGraveWithBody(b3));
        assertNull(manager.findGraveWithBody(b4));
        assertNull(manager.findGraveWithBody(b5));

        manager.putBodyIntoGrave(b1, g3);

        assertEquals(g3, manager.findGraveWithBody(b1));
        assertGraveDeepEquals(g3, manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertNull(manager.findGraveWithBody(b3));
        assertNull(manager.findGraveWithBody(b4));
        assertNull(manager.findGraveWithBody(b5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findGraveWithNullBody() {
        manager.findGraveWithBody(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void findGraveWithBodyHavingNullId() {
        manager.findGraveWithBody(bodyWithNullId);
    }

    @Test
    public void findBodiesInGrave() {

        assertTrue(manager.findBodiesInGrave(g1).isEmpty());
        assertTrue(manager.findBodiesInGrave(g2).isEmpty());
        assertTrue(manager.findBodiesInGrave(g3).isEmpty());

        manager.putBodyIntoGrave(b2, g3);
        manager.putBodyIntoGrave(b3, g2);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g2);

        List<Body> bodiesInGrave2 = Arrays.asList(b3,b5);
        List<Body> bodiesInGrave3 = Arrays.asList(b2,b4);

        assertTrue(manager.findBodiesInGrave(g1).isEmpty());
        assertBodyCollectionDeepEquals(bodiesInGrave2, manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(bodiesInGrave3, manager.findBodiesInGrave(g3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findBodiesInNullGrave() {
        manager.findBodiesInGrave(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void findBodiesInGraveHavingNullId() {
        manager.findBodiesInGrave(graveWithNullId);
    }

    @Test
    public void findUnburiedBodies() {

        List<Body> unburiedBodies = Arrays.asList(b1,b2,b3,b4,b5);

        assertBodyCollectionDeepEquals(unburiedBodies, manager.findUnburiedBodies());

        manager.putBodyIntoGrave(b3, g1);
        unburiedBodies = Arrays.asList(b1,b2,b4,b5);

        assertBodyCollectionDeepEquals(unburiedBodies, manager.findUnburiedBodies());

    }

    @Test
    public void findEmptyGraves() {

        List<Grave> emptyGraves = Arrays.asList(g1,g2,g3);
        assertGraveCollectionDeepEquals(emptyGraves, manager.findEmptyGraves());

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b3, g3);
        manager.putBodyIntoGrave(b5, g1);

        emptyGraves = Arrays.asList(g2);
        assertGraveCollectionDeepEquals(emptyGraves, manager.findEmptyGraves());
    }

    @Test
    public void findGravesWithSomeFreeSpace() {

        List<Grave> notFullGraves = Arrays.asList(g1,g2,g3);
        assertGraveCollectionDeepEquals(notFullGraves, manager.findGravesWithSomeFreeSpace());

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b3, g3);
        manager.putBodyIntoGrave(b5, g1);

        notFullGraves = Arrays.asList(g2,g3);
        assertGraveCollectionDeepEquals(notFullGraves, manager.findGravesWithSomeFreeSpace());
    }

    @Test
    public void putBodyIntoGrave() {

        assertNull(manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertNull(manager.findGraveWithBody(b3));
        assertNull(manager.findGraveWithBody(b4));
        assertNull(manager.findGraveWithBody(b5));

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        List<Body> bodiesInGrave1 = Arrays.asList(b5);
        List<Body> bodiesInGrave2 = Collections.emptyList();
        List<Body> bodiesInGrave3 = Arrays.asList(b1,b3);

        assertBodyCollectionDeepEquals(bodiesInGrave1, manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(bodiesInGrave2, manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(bodiesInGrave3, manager.findBodiesInGrave(g3));

        assertEquals(g3, manager.findGraveWithBody(b1));
        assertGraveDeepEquals(g3, manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertEquals(g3, manager.findGraveWithBody(b3));
        assertGraveDeepEquals(g3, manager.findGraveWithBody(b3));
        assertNull(manager.findGraveWithBody(b4));
        assertEquals(g1, manager.findGraveWithBody(b5));
        assertGraveDeepEquals(g1, manager.findGraveWithBody(b5));

    }

    @Test
    public void putBodyIntoGraveMultipleTime() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        try {
            manager.putBodyIntoGrave(b1, g3);
            fail();
        } catch (IllegalEntityException ex) {}

        // verify that failure was atomic and no data was changed
        assertBodyCollectionDeepEquals(Arrays.asList(b5), manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(Collections.emptyList(), manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(Arrays.asList(b1,b3), manager.findBodiesInGrave(g3));
    }

    @Test
    public void putBodyIntoMultipleGraves() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        try {
            manager.putBodyIntoGrave(b1, g2);
            fail();
        } catch (IllegalEntityException ex) {}

        // verify that failure was atomic and no data was changed
        assertBodyCollectionDeepEquals(Arrays.asList(b5), manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(Collections.emptyList(), manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(Arrays.asList(b1,b3), manager.findBodiesInGrave(g3));
    }

    @Test
    public void putBodyIntoFullGrave() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        try {
            manager.putBodyIntoGrave(b2, g1);
            fail();
        } catch (IllegalEntityException ex) {}

        // verify that failure was atomic and no data was changed
        assertBodyCollectionDeepEquals(Arrays.asList(b5), manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(Collections.emptyList(), manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(Arrays.asList(b1,b3), manager.findBodiesInGrave(g3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void putNullBodyIntoGrave() {
        manager.putBodyIntoGrave(null, g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void putBodyWithNullIdIntoGrave() {
        manager.putBodyIntoGrave(bodyWithNullId, g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void putBodyNotInDBIntoGrave() {
        manager.putBodyIntoGrave(bodyNotInDB, g2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putBodyIntoNullGrave() {
        manager.putBodyIntoGrave(b2, null);
    }

    @Test(expected = IllegalEntityException.class)
    public void putBodyIntoGraveWithNullId() {
        manager.putBodyIntoGrave(b2, graveWithNullId);
    }

    @Test(expected = IllegalEntityException.class)
    public void putBodyIntoGraveNotInDB() {
        manager.putBodyIntoGrave(b2, graveNotInDB);
    }

    @Test
    public void removeBodyFromGrave() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b3, g3);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g1);

        assertEquals(g3, manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertEquals(g3, manager.findGraveWithBody(b3));
        assertEquals(g3, manager.findGraveWithBody(b4));
        assertEquals(g1, manager.findGraveWithBody(b5));

        manager.removeBodyFromGrave(b3, g3);

        List<Body> bodiesInGrave1 = Arrays.asList(b5);
        List<Body> bodiesInGrave2 = Collections.emptyList();
        List<Body> bodiesInGrave3 = Arrays.asList(b1,b4);

        assertBodyCollectionDeepEquals(bodiesInGrave1, manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(bodiesInGrave2, manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(bodiesInGrave3, manager.findBodiesInGrave(g3));

        assertEquals(g3, manager.findGraveWithBody(b1));
        assertNull(manager.findGraveWithBody(b2));
        assertNull(manager.findGraveWithBody(b3));
        assertEquals(g3, manager.findGraveWithBody(b4));
        assertEquals(g1, manager.findGraveWithBody(b5));
    }

    @Test
    public void removeUnburiedBodyFromGrave() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g1);

        try {
            manager.removeBodyFromGrave(b3, g1);
            fail();
        } catch (IllegalEntityException ex) {}

        // Check that previous tests didn't affect data in database
        assertBodyCollectionDeepEquals(Arrays.asList(b5), manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(Collections.emptyList(), manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(Arrays.asList(b1,b4), manager.findBodiesInGrave(g3));
    }

    @Test
    public void removeBodyFromGraveWhereItIsNotBuried() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g1);

        try {
            manager.removeBodyFromGrave(b1, g1);
            fail();
        } catch (IllegalEntityException ex) {}

        // Check that previous tests didn't affect data in database
        assertBodyCollectionDeepEquals(Arrays.asList(b5), manager.findBodiesInGrave(g1));
        assertBodyCollectionDeepEquals(Collections.emptyList(), manager.findBodiesInGrave(g2));
        assertBodyCollectionDeepEquals(Arrays.asList(b1,b4), manager.findBodiesInGrave(g3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullBodyFromGrave() {
        manager.removeBodyFromGrave(null, g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void removeBodyWithNullIdFromGrave() {
        manager.removeBodyFromGrave(bodyWithNullId, g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void removeBodyNotInDBFromGrave() {
        manager.removeBodyFromGrave(bodyNotInDB, g2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeBodyFromNullGrave() {
        manager.removeBodyFromGrave(b2, null);
    }

    @Test(expected = IllegalEntityException.class)
    public void removeBodyFromGraveWithNullId() {
        manager.removeBodyFromGrave(b2, graveWithNullId);
    }

    @Test(expected = IllegalEntityException.class)
    public void removeBodyFromGraveNotInDB() {
        manager.removeBodyFromGrave(b2, graveNotInDB);
    }
}
