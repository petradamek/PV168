package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private BodyBuilder sampleJoeBodyBuilder() {
        return new BodyBuilder()
                .name("Joe from depot")
                .gender(Gender.MALE)
                .born(1962,OCTOBER,21)
                .died(2011,NOVEMBER,8)
                .vampire(false);
    }

    private BodyBuilder sampleCatherineBodyBuilder() {
        return new BodyBuilder()
                .name("Catherine")
                .gender(Gender.FEMALE)
                .born(1921,FEBRUARY,6)
                .died(2008,DECEMBER,11)
                .vampire(true);
    }

    @Test
    public void createBody() {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);

        Long bodyId = body.getId();
        assertThat(bodyId).isNotNull();

        assertThat(manager.getBody(bodyId))
                .isNotSameAs(body)
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void findAllBodies() {

        assertThat(manager.findAllBodies()).isEmpty();

        Body joe = sampleJoeBodyBuilder().build();
        Body catherine = sampleCatherineBodyBuilder().build();

        manager.createBody(joe);
        manager.createBody(catherine);

        assertThat(manager.findAllBodies())
                .usingFieldByFieldElementComparator()
                .containsOnly(joe,catherine);
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
        Body body = sampleJoeBodyBuilder()
                .id(1L)
                .build();
        expectedException.expect(IllegalEntityException.class);
        manager.createBody(body);
    }

    // Test exception using AssertJ assertThatThrownBy() method
    // this requires Java 8 due to using lambda expression
    @Test
    public void createBodyWithNullName() {
        Body body = sampleJoeBodyBuilder()
                .name(null)
                .build();
        assertThatThrownBy(() -> manager.createBody(body))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBodyWithNullGender() {
        Body body = sampleJoeBodyBuilder()
                .gender(null)
                .build();
        assertThatThrownBy(() -> manager.createBody(body))
                .isInstanceOf(ValidationException.class);
    }

    // This and next test are testing special cases with border values
    // Body died one day before born is not allowed ...
    @Test
    public void createBodyDeadBeforeBorn() {
        Body body = sampleJoeBodyBuilder()
                .born(1962,OCTOBER,21)
                .died(1962,OCTOBER,20)
                .build();
        expectedException.expect(ValidationException.class);
        manager.createBody(body);
    }

    // ... while the body died and born at the same day are allowed
    @Test
    public void createBodyBornAndDiedSameDay() {
        Body body = sampleJoeBodyBuilder()
                .born(1962,OCTOBER,21)
                .died(1962,OCTOBER,21)
                .build();
        manager.createBody(body);

        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void createBodyNullBorn() {
        Body body = sampleJoeBodyBuilder()
                .born(null)
                .build();
        manager.createBody(body);
        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void createBodyNullDied() {
        Body body = sampleJoeBodyBuilder()
                .died(null)
                .build();
        manager.createBody(body);
        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    private void updateBody(Consumer<Body> updateOperation) {
        Body joe = sampleJoeBodyBuilder().build();
        Body catherine = sampleCatherineBodyBuilder().build();
        manager.createBody(joe);
        manager.createBody(catherine);

        updateOperation.accept(joe);

        manager.updateBody(joe);
        assertThat(manager.getBody(joe.getId()))
                .isEqualToComparingFieldByField(joe);
        // Check if updates didn't affected other records
        assertThat(manager.getBody(catherine.getId()))
                .isEqualToComparingFieldByField(catherine);
    }

    @Test
    public void updateBodyName() {
        updateBody((b) -> b.setName("Pepik"));
    }

    @Test
    public void updateGender() {
        updateBody((b) -> b.setGender(Gender.FEMALE));
    }

    @Test
    public void updateBodyBorn() {
        updateBody((b) -> b.setBorn(LocalDate.of(1999,DECEMBER,11)));
    }

    @Test
    public void updateBodyDied() {
        updateBody((b) -> b.setDied(LocalDate.of(1999,DECEMBER,12)));
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
        Body body = sampleJoeBodyBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateNonExistingBody() {
        Body body = sampleJoeBodyBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateBodyWithNullName() {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        body.setName(null);

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateBodyWithNullGender() {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        body.setGender(null);

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
    }

    @Test
    public void updateBodyWithBornAfterDied() {
        Body body = sampleJoeBodyBuilder().born(1962,OCTOBER,21).died(2011,NOVEMBER,8).build();
        manager.createBody(body);
        body.setBorn(LocalDate.of(2011,NOVEMBER,9));

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
    }

    @Test
    public void deleteBody() {

        Body joe = sampleJoeBodyBuilder().build();
        Body catherine = sampleCatherineBodyBuilder().build();
        manager.createBody(joe);
        manager.createBody(catherine);

        assertThat(manager.getBody(joe.getId())).isNotNull();
        assertThat(manager.getBody(catherine.getId())).isNotNull();

        manager.deleteBody(joe);

        assertThat(manager.getBody(joe.getId())).isNull();
        assertThat(manager.getBody(catherine.getId())).isNotNull();

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullBody() {
        manager.deleteBody(null);
    }

    @Test
    public void deleteBodyWithNullId() {
        Body body = sampleJoeBodyBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBody(body);
    }

    @Test
    public void deleteNonExistingBody() {
        Body body = sampleJoeBodyBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBody(body);
    }

    private void testExpectedServiceFailureException(Consumer<BodyManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.accept(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        testExpectedServiceFailureException((m) -> m.createBody(body));
    }

    @Test
    public void updateBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((m) -> m.updateBody(body));
    }

    @Test
    public void getBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((m) -> m.getBody(body.getId()));
    }

    @Test
    public void deleteBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((m) -> m.deleteBody(body));
    }

    @Test
    public void findAllBodiesWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findAllBodies());
    }

}
