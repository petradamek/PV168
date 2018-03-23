package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

//------------------------------------------------------------------------------
// IMPORTANT NOTE:
// This test contains lots of comments to help you understand well all
// implementation details. You are not expected to use such kind of comments
// in your tests.
//------------------------------------------------------------------------------

/**
 * Example test class for {@link BodyManagerImpl}.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class BodyManagerImplTest {

    private BodyManagerImpl manager;
    private DataSource ds;

    // Our Clock mock object will be allways returning date and time
    // corresponding to February 29 2016, 14:00 in UTC.
    private final static ZonedDateTime NOW
            = LocalDateTime.of(2016, FEBRUARY, 29, 14, 00).atZone(ZoneId.of("UTC"));

    // ExpectedException is one possible mechanisms for testing if expected
    // exception is thrown. See createGraveWithExistingId() for usage example.
    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    //--------------------------------------------------------------------------
    // Test initialization
    //--------------------------------------------------------------------------

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        // we will use in memory database
        ds.setDatabaseName("memory:gravemgr-test");
        // database is created automatically if it does not exist yet
        ds.setCreateDatabase("create");
        return ds;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        // We don't need to use Mockito, because java already contais
        // implementation of Clock which returns fixed time.
        return Clock.fixed(now.toInstant(), now.getZone());
    }

    @Before
    public void setUp() throws SQLException, IOException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,GraveManager.class.getResourceAsStream("createTables.sql"));
        manager = new BodyManagerImpl(ds, prepareClockMock(NOW));
    }

    @After
    public void tearDown() throws SQLException, IOException {
        DBUtils.executeSqlScript(ds,GraveManager.class.getResourceAsStream("dropTables.sql"));
    }

    //--------------------------------------------------------------------------
    // Preparing test data
    //--------------------------------------------------------------------------

    // We will need to create some Body instances for testing purposes. We
    // could create constructor or helper method for initializing all fields,
    // but this is not well readable, especially for cases with multiple
    // parameters of the same type:
    //
    // Body body = new Body("Joe",Gender.Male,LocalDate.of(1962,Month.OCTOBER,21),
    //         LocalDate.of(2011,Month.NOVEMBER,8),false);   // constructor
    // Body body = newBody("Joe",Gender.Male,LocalDate.of(1962,Month.OCTOBER,21),
    //         LocalDate.of(2011,Month.NOVEMBER,8),false);   // helper method
    //
    // To understand this code, you need to know or look, what is the order and
    // meaning of parameters. And it will be difficult to maintain the code when
    // some new attributes are introduced. Another option is to use set methods:
    //
    // Body body = new Body();
    // body.setName("Joe");
    // body.setGender(Gender.MALE);
    // body.setBorn(LocalDate.of(1962,OCTOBER,21));
    // body.setDied(LocalDate.of(2011,NOVEMBER,8));
    // body.setVampire(false);
    //
    // This is better understandable, but it needs too much code to construct
    // the object. Alternative solution is to use Builder pattern:
    //
    // Body body = new BodyBuilder().name("Joe").gender(Gender.MALE)
    //         .born(1962,OCTOBER,21).died(2011,NOVEMBER,8);
    //
    // Advantage of builder pattern is compact syntax based on fluent API,
    // clear assigment of values to attribute names and flexibility allowing to
    // set only arbitrary subset of attributes (and keeping default values for
    // others). Disadvantage of builder pattern is the need to create and
    // maintain builder class. See Item 2 in Effective Java from Joshua Bloch
    // for more details.
    //
    // To make creation of test objects even easier, we can prepare some
    // pre-configured builders with some reasonable default attribute values
    // (see sampleJoeBodyBuilder() and sampleCatherineBodyBuilder() bellow).
    // These values can be changed by subsequent calls of appropriate builder
    // method if needed.
    //
    // Body bodyWithExistingId = sampleJoeBodyBuilder().id(12L).build();
    //
    // This mechanism allows us to focus only to attributes important for given
    // test and use some universal reasonable value for other attribute. For
    // example, we don't need to use some specific attribute values for
    // createBody() test, this test works well with any valid values.

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

    //--------------------------------------------------------------------------
    // Tests for operations for creating and fetching graves
    //--------------------------------------------------------------------------

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
    public void createBodyWithBornTomorrow() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Body body = sampleJoeBodyBuilder()
                .born(tomorrow)
                .died(null)
                .build();
        assertThatThrownBy(() -> manager.createBody(body))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBodyWithBornToday() {
        LocalDate today = NOW.toLocalDate();
        Body body = sampleJoeBodyBuilder()
                .born(today)
                .died(null)
                .build();
        manager.createBody(body);

        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void createBodyWithDiedTomorrow() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Body body = sampleJoeBodyBuilder()
                .died(tomorrow)
                .build();
        assertThatThrownBy(() -> manager.createBody(body))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBodyWithDiedToday() {
        LocalDate today = NOW.toLocalDate();
        Body body = sampleJoeBodyBuilder()
                .died(today)
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

    //--------------------------------------------------------------------------
    // Tests for BodyManager.updateBody(Body) operation
    //--------------------------------------------------------------------------

    @Test
    public void updateBodyName() {
        // Let us create two bodies, one will be used for testing the update
        // and another one will be used for verification that other objects are
        // not affected by update operation
        Body bodyForUpdate = sampleJoeBodyBuilder().build();
        Body anotherBody = sampleCatherineBodyBuilder().build();
        manager.createBody(bodyForUpdate);
        manager.createBody(anotherBody);

        // Performa the update operation ...
        bodyForUpdate.setName("New Name");

        // ... and save updated body to database
        manager.updateBody(bodyForUpdate);

        // Check if body was properly updated
        assertThat(manager.getBody(bodyForUpdate.getId()))
                .isEqualToComparingFieldByField(bodyForUpdate);
        // Check if updates didn't affected other records
        assertThat(manager.getBody(anotherBody.getId()))
                .isEqualToComparingFieldByField(anotherBody);
    }

    // Now we want to test also other update operations. We could do it the same
    // way as in updateBodyName(), but we would get couple of almost the same
    // test methods, which would differ from each other only in single line
    // with update operation. To avoid duplicit code and make the test better
    // maintainable, we need to separate the update operation from the test
    // method and to let us call test method multiple times with differen
    // update operation.

    // Let start with functional interface which will represent update operation
    // BTW, we could use standard Consumer<T> functional interface (and I would
    // probably do it in real test), but I decided to define my own interface to
    // make the test better understandable
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    // The next step is implementation of generic test method. This method will
    // perform update test with given update operation.
    // The method is almost the same as updateBodyName(), the only difference is
    // the line with calling given updateOperation.
    private void testUpdateBody(Operation<Body> updateOperation) {
        Body bodyForUpdate = sampleJoeBodyBuilder().build();
        Body anotherBody = sampleCatherineBodyBuilder().build();
        manager.createBody(bodyForUpdate);
        manager.createBody(anotherBody);

        updateOperation.callOn(bodyForUpdate);

        manager.updateBody(bodyForUpdate);
        assertThat(manager.getBody(bodyForUpdate.getId()))
                .isEqualToComparingFieldByField(bodyForUpdate);
        // Check if updates didn't affected other records
        assertThat(manager.getBody(anotherBody.getId()))
                .isEqualToComparingFieldByField(anotherBody);
    }

    // Now we will call testUpdateBody(...) method with different update
    // operations. Update operation is defined with Lambda expression.

    @Test
    public void updateGender() {
        testUpdateBody((body) -> body.setGender(Gender.FEMALE));
    }

    @Test
    public void updateBodyBorn() {
        testUpdateBody((body) -> body.setBorn(LocalDate.of(1999,DECEMBER,11)));
    }

    @Test
    public void updateBodyDied() {
        testUpdateBody((body) -> body.setDied(LocalDate.of(1999,DECEMBER,12)));
    }

    @Test
    public void updateBodyVampire() {
        testUpdateBody((body) -> body.setVampire(true));
    }

    // Test also if attemtpt to call update with invalid body throws
    // the correct exception.

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

    //--------------------------------------------------------------------------
    // Tests for BodyManager.deleteBody(Body) operation
    //--------------------------------------------------------------------------

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

    // Test also if attemtpt to call delete with invalid parameter throws
    // the correct exception.

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

    //--------------------------------------------------------------------------
    // Tests if BodyManager methods throws ServiceFailureException in case of
    // DB operation failure
    //--------------------------------------------------------------------------

    @Test
    public void createBodyWithSqlExceptionThrown() throws SQLException {
        // Create sqlException, which will be thrown by our DataSource mock
        // object to simulate DB operation failure
        SQLException sqlException = new SQLException();
        // Create DataSource mock object
        DataSource failingDataSource = mock(DataSource.class);
        // Instruct our DataSource mock object to throw our sqlException when
        // DataSource.getConnection() method is called.
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        // Configure our manager to use DataSource mock object
        manager = new BodyManagerImpl(failingDataSource, prepareClockMock(NOW));

        // Create Body instance for our test
        Body body = sampleJoeBodyBuilder().build();

        // Try to call Manager.createBody(Body) method and expect that exception
        // will be thrown
        assertThatThrownBy(() -> manager.createBody(body))
                // Check that thrown exception is ServiceFailureException
                .isInstanceOf(ServiceFailureException.class)
                // Check if cause is properly set
                .hasCause(sqlException);
    }

    // Now we want to test also other methods of BodyManager. To avoid having
    // couple of method with lots of duplicit code, we will use the similar
    // approach as with testUpdateBody(Operation) method.

    private void testExpectedServiceFailureException(Operation<BodyManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager = new BodyManagerImpl(failingDataSource, prepareClockMock(NOW));
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((bodyManager) -> bodyManager.updateBody(body));
    }

    @Test
    public void getBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((bodyManager) -> bodyManager.getBody(body.getId()));
    }

    @Test
    public void deleteBodyWithSqlExceptionThrown() throws SQLException {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        testExpectedServiceFailureException((bodyManager) -> bodyManager.deleteBody(body));
    }

    @Test
    public void findAllBodiesWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((bodyManager) -> bodyManager.findAllBodies());
    }

}
