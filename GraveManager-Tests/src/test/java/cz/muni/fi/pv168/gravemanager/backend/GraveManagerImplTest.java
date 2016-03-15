package cz.muni.fi.pv168.gravemanager.backend;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link GraveManagerImpl} class.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class GraveManagerImplTest {

    private GraveManagerImpl manager;

    @Before
    public void setUp() throws SQLException {
        manager = new GraveManagerImpl();
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
    public void getContact() {

        assertNull(manager.getGrave(1l));

        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        Long graveId = grave.getId();

        Grave result = manager.getGrave(graveId);
        assertEquals(grave, result);
        assertDeepEquals(grave, result);
    }

    @Test
    public void getAllContacts() {

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

    @Test
    public void addContactWithWrongAttributes() {

        try {
            manager.createGrave(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Grave grave = newGrave(12, 13, 6, "Nice grave");
        grave.setId(1l);
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        grave = newGrave(-1, 13, 6, "Nice grave");
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        grave = newGrave(1, -1, 6, "Nice grave");
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        grave = newGrave(1, 1, -1, "Nice grave");
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        grave = newGrave(1, 1, 0, "Nice grave");
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        // these variants should be ok
        grave = newGrave(0, 13, 6, "Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId());
        assertNotNull(result);

        grave = newGrave(12, 0, 6, "Nice grave");
        manager.createGrave(grave);
        result = manager.getGrave(grave.getId());
        assertNotNull(result);

        grave = newGrave(12, 11, 6, null);
        manager.createGrave(grave);
        result = manager.getGrave(grave.getId());
        assertNotNull(result);
        assertNull(result.getNote());

    }

    @Test
    public void updateGrave() {
        Grave grave = newGrave(12, 13, 6, "Nice grave");
        Grave g2 = newGrave(18, 19, 100, "Another record");
        manager.createGrave(grave);
        manager.createGrave(g2);
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
        grave.setNote("Another grave");
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(1, grave.getCapacity());
        assertEquals("Another grave", grave.getNote());

        grave = manager.getGrave(graveId);
        grave.setNote(null);
        manager.updateGrave(grave);
        assertEquals(0, grave.getColumn());
        assertEquals(0, grave.getRow());
        assertEquals(1, grave.getCapacity());
        assertNull(grave.getNote());

        // Check if updates didn't affected other records
        assertDeepEquals(g2, manager.getGrave(g2.getId()));
    }

    @Test
    public void updateGraveWithWrongAttributes() {

        Grave grave = newGrave(12, 13, 6, "Nice grave");
        manager.createGrave(grave);
        Long graveId = grave.getId();

        try {
            manager.updateGrave(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setId(null);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setId(graveId - 1);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setColumn(-1);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setRow(-1);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setCapacity(0);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setCapacity(-1);
            manager.updateGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
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

    @Test
    public void deleteGraveWithWrongAttributes() {

        Grave grave = newGrave(12, 13, 6, "Nice grave");

        try {
            manager.deleteGrave(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave.setId(null);
            manager.deleteGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            grave.setId(1l);
            manager.deleteGrave(grave);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

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
