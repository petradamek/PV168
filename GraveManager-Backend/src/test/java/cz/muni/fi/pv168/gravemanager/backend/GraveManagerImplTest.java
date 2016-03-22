package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(graveId).isNotNull();

        assertThat(manager.getGrave(graveId))
                .isNotSameAs(grave)
                .isEqualToComparingFieldByField(grave);
    }

    @Test
    public void findAllGraves() {

        assertThat(manager.findAllGraves()).isEmpty();

        Grave g1 = newGrave(23,44,5,"Grave 1");
        Grave g2 = newGrave(12,4,1,"Grave 2");

        manager.createGrave(g1);
        manager.createGrave(g2);

        assertThat(manager.findAllGraves())
                .usingFieldByFieldElementComparator()
                .containsOnly(g1,g2);
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

    // Test exception using AssertJ assertThatThrownBy() method
    // this requires Java 8 due to using lambda expression
    @Test
    public void createGraveWithNegativeColumn() {
        Grave grave = newGrave(-1, 13, 6, "Nice grave");
        assertThatThrownBy(() -> manager.createGrave(grave))
                .isInstanceOf(ValidationException.class);
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

        assertThat(manager.getGrave(grave.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(grave);
    }

    @Test
    public void createGraveWithZeroRow() {
        Grave grave = newGrave(12, 0, 6, "Nice grave");
        manager.createGrave(grave);

        assertThat(manager.getGrave(grave.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(grave);
    }

    @Test
    public void createGraveWithNullNote() {
        Grave grave = newGrave(12, 11, 6, null);
        manager.createGrave(grave);

        assertThat(manager.getGrave(grave.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(grave);
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

        assertThat(manager.getGrave(sourceGrave.getId()))
                .isEqualToComparingFieldByField(sourceGrave);
        // Check if updates didn't affected other records
        assertThat(manager.getGrave(anotherGrave.getId()))
                .isEqualToComparingFieldByField(anotherGrave);
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

        assertThat(manager.getGrave(g1.getId())).isNotNull();
        assertThat(manager.getGrave(g2.getId())).isNotNull();

        manager.deleteGrave(g1);

        assertThat(manager.getGrave(g1.getId())).isNull();
        assertThat(manager.getGrave(g2.getId())).isNotNull();

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

}
