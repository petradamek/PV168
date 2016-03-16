package cz.muni.fi.pv168.gravemanager.backend;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests for {@link GraveManagerImpl} class.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class GraveManagerImplTest {

    private GraveManagerImpl manager;
    private DataSource dataSource;

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE GRAVE ("
                    + "id bigint primary key generated always as identity,"
                    + "col int,"
                    + "row int,"
                    + "capacity int not null,"
                    + "note varchar(255))").executeUpdate();
        }
        manager = new GraveManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE GRAVE").executeUpdate();
        }
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:gravemgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void createGrave() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);

        Long graveId = grave.getId();
        assertNotNull(graveId);
        Grave result = manager.getGrave(graveId);
        assertEquals(grave, result);
        assertNotSame(grave, result);
        assertDeepEquals(grave, result);
    }

    @Test
    public void getAllGraves() {
        assertTrue(manager.findAllGraves().isEmpty());

        Grave g1 = newGrave(23, 44, 5, "Grave 1");
        Grave g2 = newGrave(12, 4, 1, "Grave 2");

        manager.createGrave(g1);
        manager.createGrave(g2);

        List<Grave> expected = Arrays.asList(g1, g2);
        List<Grave> actual = manager.findAllGraves();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullGrave() {
        manager.createGrave(null);
    }

    @Test
    public void createGraveWithExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(1l);
        expectedException.expect(IllegalArgumentException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeColumn() {
        Grave grave = newGrave(-1, 13, 6, "Nice grave");
        expectedException.expect(IllegalArgumentException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeRow() {
        Grave grave = newGrave(1, -1, 6, "Nice grave");
        expectedException.expect(IllegalArgumentException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeCapacity() {
        Grave grave = newGrave(1, 1, -1, "Nice grave");
        expectedException.expect(IllegalArgumentException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroCapacity() {
        Grave grave = newGrave(1, 1, 0, "Nice grave");
        expectedException.expect(IllegalArgumentException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroColumn() {
        Grave grave = newGrave(0, 13, 6, "Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);
    }

    @Test
    public void createGraveWithZeroRow() {
        Grave grave = newGrave(12, 0, 6, "Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);
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
    public void updateGrave() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        Grave anotherGrave = newGrave(18, 19, 100, "Another grave");
        manager.createGrave(grave);
        manager.createGrave(anotherGrave);
        Long graveId = grave.getId();

        grave = manager.getGrave(graveId);
        grave.setColumn(0);
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(13, grave.getRow());
        assertEquals(6, grave.getCapacity());
        assertEquals("Nice grave", grave.getNote());

        grave = manager.getGrave(graveId);
        grave.setRow(0);
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(6, grave.getCapacity());
        assertEquals("Nice grave", grave.getNote());

        grave = manager.getGrave(graveId);
        grave.setCapacity(1);
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(1, grave.getCapacity());
        assertEquals("Nice grave", grave.getNote());

        grave = manager.getGrave(graveId);
        grave.setNote("Not so nice grave");
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(1, grave.getCapacity());
        assertEquals("Not so nice grave", grave.getNote());

        grave = manager.getGrave(graveId);
        grave.setNote(null);
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(1, grave.getCapacity());
        assertNull(grave.getNote());

        // Check if updates didn't affected other records
        assertDeepEquals(anotherGrave, manager.getGrave(anotherGrave.getId()));
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
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNonExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setId(grave.getId() + 1);
        expectedException.expect(EntityNotFoundException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeColumn() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setColumn(-1);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeRow() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setRow(-1);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithZeroCapacity() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setCapacity(0);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeCapacity() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        grave.setCapacity(-1);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void deleteGrave() {

        Grave g1 = newGrave(12, 13, 6, "Nice grave");
        Grave g2 = newGrave(18, 19, 100, "Another record");
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
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteGrave(grave);
    }

    @Test
    public void deleteGraveWithNonExistingId() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(1L);
        expectedException.expect(EntityNotFoundException.class);
        manager.deleteGrave(grave);
    }

    private static Grave newGrave(int column, int row, int capacity, String note) {
        Grave grave = new Grave();
        grave.setColumn(column);
        grave.setRow(row);
        grave.setCapacity(capacity);
        grave.setNote(note);
        return grave;
    }

    private void assertDeepEquals(List<Grave> expectedList, List<Grave> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Grave expected = expectedList.get(i);
            Grave actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Grave expected, Grave actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getColumn(), actual.getColumn());
        assertEquals(expected.getRow(), actual.getRow());
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.getNote(), actual.getNote());
    }

    private static final Comparator<Grave> idComparator = new Comparator<Grave>() {

        @Override
        public int compare(Grave o1, Grave o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };

}
