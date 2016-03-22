package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.Date;
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
        assertThat(bodyId).isNotNull();

        assertThat(manager.getBody(bodyId))
                .isNotSameAs(body)
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void findAllBodies() {

        assertThat(manager.findAllBodies()).isEmpty();

        Body b1 = newBody("Body 1",date("1929-10-12"),date("2011-09-30"),false);
        Body b2 = newBody("Body 2",date("1962-01-21"),date("2001-12-01"),false);

        manager.createBody(b1);
        manager.createBody(b2);

        assertThat(manager.findAllBodies())
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b2);
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

    // Test exception using AssertJ assertThatThrownBy() method
    // this requires Java 8 due to using lambda expression
    @Test
    public void createBodyWithNullName() {
        Body body = newBody(null,date("1962-10-21"),date("2011-11-08"),false);
        assertThatThrownBy(() -> manager.createBody(body))
                .isInstanceOf(ValidationException.class);
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

        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void createBodyNullBorn() {
        Body body = newBody("Pepa z Depa",null,date("2011-11-08"),true);
        manager.createBody(body);
        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    @Test
    public void createBodyNullDied() {
        Body body = newBody("Pepa z Depa",date("1962-10-21"),null,true);
        manager.createBody(body);
        assertThat(manager.getBody(body.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(body);
    }

    private void updateBody(Consumer<Body> updateOperation) {
        Body sourceBody = newBody("Joe from depot",date("1962-10-21"),date("2011-11-08"),false);
        Body anotherBody = newBody("Billy Bob",date("1921-02-06"),date("2008-12-11"),false);
        manager.createBody(sourceBody);
        manager.createBody(anotherBody);

        updateOperation.accept(sourceBody);

        manager.updateBody(sourceBody);
        assertThat(manager.getBody(sourceBody.getId()))
                .isEqualToComparingFieldByField(sourceBody);
        // Check if updates didn't affected other records
        assertThat(manager.getBody(anotherBody.getId()))
                .isEqualToComparingFieldByField(anotherBody);
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

        assertThat(manager.getBody(b1.getId())).isNotNull();
        assertThat(manager.getBody(b2.getId())).isNotNull();

        manager.deleteBody(b1);

        assertThat(manager.getBody(b1.getId())).isNull();
        assertThat(manager.getBody(b2.getId())).isNotNull();

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

}
