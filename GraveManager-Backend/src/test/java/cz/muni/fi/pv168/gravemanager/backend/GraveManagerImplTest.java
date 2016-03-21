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
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Petr Adámek
 */
public class GraveManagerImplTest {
    
    private GraveManagerImpl manager;
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
    public void getGrave() {
        
        assertNull(manager.getGrave(1l));
        
        Grave grave = newGrave(12,13,6,"Nice grave");
        manager.createGrave(grave);
        Long graveId = grave.getId();

        Grave result = manager.getGrave(graveId);
        assertEquals(grave, result);
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

    @Test
    public void addGraveWithWrongAttributes() {

        try {
            manager.createGrave(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Grave grave = newGrave(12,13,6,"Nice grave");
        grave.setId(1l);
        try {
            manager.createGrave(grave);
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        grave = newGrave(-1,13,6,"Nice grave"); 
        try {
            manager.createGrave(grave);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        grave = newGrave(1,-1,6,"Nice grave"); 
        try {
            manager.createGrave(grave);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        grave = newGrave(1,1,-1,"Nice grave"); 
        try {
            manager.createGrave(grave);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        grave = newGrave(1,1,0,"Nice grave"); 
        try {
            manager.createGrave(grave);
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        // these variants should be ok
        grave = newGrave(0,13,6,"Nice grave");
        manager.createGrave(grave);
        Grave result = manager.getGrave(grave.getId()); 
        assertNotNull(result);
        assertGraveDeepEquals(grave, result);

        grave = newGrave(12,0,6,"Nice grave");
        manager.createGrave(grave);
        result = manager.getGrave(grave.getId()); 
        assertNotNull(result);
        assertGraveDeepEquals(grave, result);

        grave = newGrave(12,11,6,null);
        manager.createGrave(grave);
        result = manager.getGrave(grave.getId()); 
        assertNotNull(result);
        assertNull(result.getNote());

    }

    @Test
    public void updateGrave() {
                
        Grave grave = newGrave(12,13,6,"Nice grave");
        Grave g2 = newGrave(18,19,100,"Another record");
        manager.createGrave(grave);
        manager.createGrave(g2);
        Long graveId = grave.getId();
        Grave result;
        
        grave = manager.getGrave(graveId);
        grave.setColumn(0);
        manager.updateGrave(grave);        
        result = manager.getGrave(graveId);
        assertGraveDeepEquals(grave, result);

        grave = manager.getGrave(graveId);
        grave.setRow(0);
        manager.updateGrave(grave);        
        result = manager.getGrave(graveId);
        assertGraveDeepEquals(grave, result);

        grave = manager.getGrave(graveId);
        grave.setCapacity(1);
        manager.updateGrave(grave);        
        result = manager.getGrave(graveId);
        assertGraveDeepEquals(grave, result);

        grave = manager.getGrave(graveId);
        grave.setNote("Another grave");
        manager.updateGrave(grave);        
        result = manager.getGrave(graveId);
        assertGraveDeepEquals(grave, result);

        grave = manager.getGrave(graveId);
        grave.setNote(null);
        manager.updateGrave(grave);        
        result = manager.getGrave(graveId);
        assertGraveDeepEquals(grave, result);

        // Check if updates didn't affected other records
        assertGraveDeepEquals(g2, manager.getGrave(g2.getId()));
    }

    @Test
    public void updateGraveWithWrongAttributes() {

        Grave grave = newGrave(12,13,6,"Nice grave");
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
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setId(graveId - 1);
            manager.updateGrave(grave);        
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setColumn(-1);
            manager.updateGrave(grave);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setRow(-1);
            manager.updateGrave(grave);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setCapacity(0);
            manager.updateGrave(grave);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }

        try {
            grave = manager.getGrave(graveId);
            grave.setCapacity(-1);
            manager.updateGrave(grave);        
            fail();
        } catch (ValidationException ex) {
            //OK
        }
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

    @Test
    public void deleteGraveWithWrongAttributes() {

        Grave grave = newGrave(12,13,6,"Nice grave");
        
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
        } catch (IllegalEntityException ex) {
            //OK
        }

        try {
            grave.setId(1l);
            manager.deleteGrave(grave);
            fail();
        } catch (IllegalEntityException ex) {
            //OK
        }        

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
