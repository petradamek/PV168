package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ValidationException;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 *
 * @author Petr Adámek
 */
public class GraveManagerImplTest {

    private GraveManagerImpl manager;
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
        manager = new GraveManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,GraveManager.class.getResource("dropTables.sql"));
    }

    @Test
    @Ignore
    public void todo() {
        fail("Přidat ke třídě body atribut typu enum (např. pohlaví). "
                + "Příklady na webovku a jednoduchou implementaci upravit tak, "
                + "aby pracovali s třídou body (ukáže se tam víc typů: date, enum, boolean).");
    }

    @Test
    public void createGrave() {
        Grave grave = newGrave(12,13,6,"Nice grave");
        manager.createGrave(grave);

        Long graveId = grave.getId();
        assertNotNull(graveId);
        Grave result = manager.getGrave(graveId);
        assertEquals(grave, result);
        assertNotSame(grave, result);
        assertGraveDeepEquals(grave, result);
    }

    @Test
    public void findAllGraves() {

        assertTrue(manager.findAllGraves().isEmpty());

        Grave g1 = newGrave(23,44,5,"Grave 1");
        Grave g2 = newGrave(12,4,1,"Grave 2");

        manager.createGrave(g1);
        manager.createGrave(g2);

        List<Grave> expected = Arrays.asList(g1,g2);
        List<Grave> actual = manager.findAllGraves();

        assertGraveCollectionDeepEquals(expected, actual);
    }

    // Test exception with expected parameter of @Test annotation
    // it does not allow to specify exact place where the exception
    // is expected, therefor it is suitable only for simple single line tests
    @Test(expected = IllegalArgumentException.class)
    public void createNullGrave() {
        manager.createGrave(null);
    }

    // Test exception with ExpectedException @Rule
    @Test
    public void createGraveWithExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(1l);
        expectedException.expect(IllegalEntityException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeColumn() {
        Grave grave = newGrave(-1, 13, 6, "Nice grave");
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeRow() {
        Grave grave = newGrave(1, -1, 6, "Nice grave");
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeCapacity() {
        Grave grave = newGrave(1, 1, -1, "Nice grave");
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroCapacity() {
        Grave grave = newGrave(1, 1, 0, "Nice grave");
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroColumn() {
        Grave grave = newGrave(0, 13, 6, "Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);
        assertGraveDeepEquals(grave, result);
    }

    @Test
    public void createGraveWithZeroRow() {
        Grave grave = newGrave(12, 0, 6, "Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);
        assertGraveDeepEquals(grave, result);
    }

    @Test
    public void createGraveWithNullNote() {
        Grave grave = newGrave(12, 11, 6, null);
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);
        assertNull(result.getNote());
    }

    @Test
    public void updateGraveColumn() {
        testUpdate((g) -> g.setColumn(1));
    }

    @Test
    public void updateGraveRow() {
        testUpdate((g) -> g.setRow(3));
    }

    @Test
    public void updateGraveCapacity() {
        testUpdate((g) -> g.setCapacity(5));
    }

    @Test
    public void updateGraveNote() {
        testUpdate((g) -> g.setNote("Not so nice grave"));
    }

    @Test
    public void updateGraveNoteToNull() {
        testUpdate((g) -> g.setNote(null));
    }

    private void testUpdate(Consumer<Grave> updateOperation) {
        Grave sourceGrave = newGrave(12, 13, 6, "Nice grave");
        Grave anotherGrave = newGrave(18, 19, 100, "Another grave");
        manager.createGrave(sourceGrave);
        manager.createGrave(anotherGrave);

        updateOperation.accept(sourceGrave);
        manager.updateGrave(sourceGrave);

        Grave result = manager.getGrave(sourceGrave.getId());
        assertGraveDeepEquals(sourceGrave, result);
        // Check if updates didn't affected other records
        assertGraveDeepEquals(anotherGrave, manager.getGrave(anotherGrave.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullGrave() {
        manager.updateGrave(null);
    }

    @Test
    public void updateGraveWithNullId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setId(null);
        expectedException.expect(IllegalEntityException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNonExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setId(grave.getId() + 1);
        expectedException.expect(IllegalEntityException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeColumn() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setColumn(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeRow() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setRow(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithZeroCapacity() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setCapacity(0);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeCapacity() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setCapacity(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void deleteGrave() {

        Grave g1 = newGrave(12,13,6,"Nice grave");
        Grave g2 = newGrave(18,19,100,"Another record");
        manager.createGrave(g1);
        manager.createGrave(g2);

        assertNotNull(manager.getGrave(g1.getId()));
        assertNotNull(manager.getGrave(g2.getId()));

        manager.deleteGrave(g1);

        assertNull(manager.getGrave(g1.getId()));
        assertNotNull(manager.getGrave(g2.getId()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullGrave() {
        manager.deleteGrave(null);
    }

    @Test
    public void deleteGraveWithNullId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(null);
        expectedException.expect(IllegalEntityException.class);
        manager.deleteGrave(grave);
    }

    @Test
    public void deleteGraveWithNonExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(1L);
        expectedException.expect(IllegalEntityException.class);
        manager.deleteGrave(grave);
    }

    static Grave newGrave(int column, int row, int capacity, String note) {
        Grave grave = new Grave();
        grave.setColumn(column);
        grave.setRow(row);
        grave.setCapacity(capacity);
        grave.setNote(note);
        return grave;
    }

    static void assertGraveDeepEquals(Grave expected, Grave actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getColumn(), actual.getColumn());
        assertEquals(expected.getRow(), actual.getRow());
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.getNote(), actual.getNote());
    }

    private static Comparator<Grave> graveKeyComparator = new Comparator<Grave>() {

        @Override
        public int compare(Grave o1, Grave o2) {
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

    static void assertGraveCollectionDeepEquals(List<Grave> expected, List<Grave> actual) {

        assertEquals(expected.size(), actual.size());
        List<Grave> expectedSortedList = new ArrayList<Grave>(expected);
        List<Grave> actualSortedList = new ArrayList<Grave>(actual);
        Collections.sort(expectedSortedList,graveKeyComparator);
        Collections.sort(actualSortedList,graveKeyComparator);
        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertGraveDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
}
