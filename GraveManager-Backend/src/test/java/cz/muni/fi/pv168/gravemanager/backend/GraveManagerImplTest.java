package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private GraveBuilder sampleSmallGraveBuilder() {
        return new GraveBuilder()
                .id(null)
                .column(12)
                .row(13)
                .capacity(1)
                .note("Small Grave");
    }

    private GraveBuilder sampleBigGraveBuilder() {
        return new GraveBuilder()
                .id(null)
                .column(22)
                .row(27)
                .capacity(6)
                .note("Big Grave");
    }

    @Test
    public void createGrave() {
        Grave grave = sampleSmallGraveBuilder().build();
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

        Grave g1 = sampleSmallGraveBuilder().build();
        Grave g2 = sampleBigGraveBuilder().build();

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
        Grave grave = sampleSmallGraveBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.createGrave(grave);
    }

    // Test exception using AssertJ assertThatThrownBy() method
    // this requires Java 8 due to using lambda expression
    @Test
    public void createGraveWithNegativeColumn() {
        Grave grave = sampleSmallGraveBuilder().column(-1).build();
        assertThatThrownBy(() -> manager.createGrave(grave))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createGraveWithNegativeRow() {
        Grave grave = sampleSmallGraveBuilder().row(-1).build();
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithNegativeCapacity() {
        Grave grave = sampleSmallGraveBuilder().capacity(-1).build();
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroCapacity() {
        Grave grave = sampleSmallGraveBuilder().capacity(0).build();
        expectedException.expect(ValidationException.class);
        manager.createGrave(grave);
    }

    @Test
    public void createGraveWithZeroColumn() {
        Grave grave = sampleSmallGraveBuilder().column(0).build();
        manager.createGrave(grave);

        assertThat(manager.getGrave(grave.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(grave);
    }

    @Test
    public void createGraveWithZeroRow() {
        Grave grave = sampleSmallGraveBuilder().row(0).build();
        manager.createGrave(grave);

        assertThat(manager.getGrave(grave.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(grave);
    }

    @Test
    public void createGraveWithNullNote() {
        Grave grave = sampleSmallGraveBuilder().note(null).build();
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
        Grave sourceGrave = sampleSmallGraveBuilder().build();
        Grave anotherGrave = sampleBigGraveBuilder().build();
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
        Grave grave = sampleSmallGraveBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNonExistingId() {
        Grave grave = sampleSmallGraveBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeColumn() {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        grave.setColumn(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeRow() {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        grave.setRow(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithZeroCapacity() {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        grave.setCapacity(0);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void updateGraveWithNegativeCapacity() {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        grave.setCapacity(-1);
        expectedException.expect(ValidationException.class);
        manager.updateGrave(grave);
    }

    @Test
    public void deleteGrave() {

        Grave g1 = sampleSmallGraveBuilder().build();
        Grave g2 = sampleBigGraveBuilder().build();
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
        Grave grave = sampleSmallGraveBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteGrave(grave);
    }

    @Test
    public void deleteGraveWithNonExistingId() {
        Grave grave = sampleSmallGraveBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteGrave(grave);
    }

    private void testExpectedServiceFailureException(Consumer<GraveManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.accept(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createGraveWithSqlExceptionThrown() throws SQLException {
        Grave grave = sampleSmallGraveBuilder().build();
        testExpectedServiceFailureException((m) -> m.createGrave(grave));
    }

    @Test
    public void updateGraveWithSqlExceptionThrown() throws SQLException {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        testExpectedServiceFailureException((m) -> m.updateGrave(grave));
    }

    @Test
    public void getGraveWithSqlExceptionThrown() throws SQLException {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        testExpectedServiceFailureException((m) -> m.getGrave(grave.getId()));
    }

    @Test
    public void deleteGraveWithSqlExceptionThrown() throws SQLException {
        Grave grave = sampleSmallGraveBuilder().build();
        manager.createGrave(grave);
        testExpectedServiceFailureException((m) -> m.deleteGrave(grave));
    }

    @Test
    public void findAllGravesWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findAllGraves());
    }

}
