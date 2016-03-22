package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import java.sql.SQLException;
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

        g1 = new GraveBuilder().column(1).row(2).capacity(1).note("Grave 1").build();
        g2 = new GraveBuilder().column(8).row(9).capacity(2).note("Grave 2").build();
        g3 = new GraveBuilder().column(2).row(2).capacity(3).note("Grave 3").build();

        b1 = new BodyBuilder().name("Body 1").build();
        b2 = new BodyBuilder().name("Body 2").build();
        b3 = new BodyBuilder().name("Body 3").build();
        b4 = new BodyBuilder().name("Body 4").build();
        b5 = new BodyBuilder().name("Body 5").build();

        bodyManager.createBody(b1);
        bodyManager.createBody(b2);
        bodyManager.createBody(b3);
        bodyManager.createBody(b4);
        bodyManager.createBody(b5);

        graveManager.createGrave(g1);
        graveManager.createGrave(g2);
        graveManager.createGrave(g3);

        graveWithNullId = new GraveBuilder().id(null).build();
        graveNotInDB = new GraveBuilder().id(g3.getId() + 100).build();
        assertThat(graveManager.getGrave(graveNotInDB.getId())).isNull();

        bodyWithNullId = new BodyBuilder().name("Body with null id").id(null).build();
        bodyNotInDB = new BodyBuilder().name("Body not in DB").id(b5.getId() + 100).build();
        assertThat(bodyManager.getBody(bodyNotInDB.getId())).isNull();
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

        assertThat(manager.findGraveWithBody(b1)).isNull();
        assertThat(manager.findGraveWithBody(b2)).isNull();
        assertThat(manager.findGraveWithBody(b3)).isNull();
        assertThat(manager.findGraveWithBody(b4)).isNull();
        assertThat(manager.findGraveWithBody(b5)).isNull();

        manager.putBodyIntoGrave(b1, g3);

        assertThat(manager.findGraveWithBody(b1))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b2)).isNull();
        assertThat(manager.findGraveWithBody(b3)).isNull();
        assertThat(manager.findGraveWithBody(b4)).isNull();
        assertThat(manager.findGraveWithBody(b5)).isNull();
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

        assertThat(manager.findBodiesInGrave(g1)).isEmpty();
        assertThat(manager.findBodiesInGrave(g2)).isEmpty();
        assertThat(manager.findBodiesInGrave(g3)).isEmpty();

        manager.putBodyIntoGrave(b2, g3);
        manager.putBodyIntoGrave(b3, g2);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g2);

        assertThat(manager.findBodiesInGrave(g1))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g2))
                .usingFieldByFieldElementComparator()
                .containsOnly(b3,b5);
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b2,b4);
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

        assertThat(manager.findUnburiedBodies())
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b2,b3,b4,b5);

        manager.putBodyIntoGrave(b3, g1);

        assertThat(manager.findUnburiedBodies())
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b2,b4,b5);
    }

    @Test
    public void findEmptyGraves() {

        assertThat(manager.findEmptyGraves())
                .usingFieldByFieldElementComparator()
                .containsOnly(g1,g2,g3);

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b3, g3);
        manager.putBodyIntoGrave(b5, g1);

        assertThat(manager.findEmptyGraves())
                .usingFieldByFieldElementComparator()
                .containsOnly(g2);
    }

    @Test
    public void findGravesWithSomeFreeSpace() {

        assertThat(manager.findGravesWithSomeFreeSpace())
                .usingFieldByFieldElementComparator()
                .containsOnly(g1,g2,g3);

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b3, g3);
        manager.putBodyIntoGrave(b5, g1);

        assertThat(manager.findGravesWithSomeFreeSpace())
                .usingFieldByFieldElementComparator()
                .containsOnly(g2,g3);
    }

    @Test
    public void putBodyIntoGrave() {

        assertThat(manager.findGraveWithBody(b1)).isNull();
        assertThat(manager.findGraveWithBody(b2)).isNull();
        assertThat(manager.findGraveWithBody(b3)).isNull();
        assertThat(manager.findGraveWithBody(b4)).isNull();
        assertThat(manager.findGraveWithBody(b5)).isNull();

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b3);

        assertThat(manager.findGraveWithBody(b1))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b2))
                .isNull();
        assertThat(manager.findGraveWithBody(b3))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b4))
                .isNull();
        assertThat(manager.findGraveWithBody(b5))
                .isEqualToComparingFieldByField(g1);
    }

    @Test
    public void putBodyIntoGraveMultipleTime() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        assertThatThrownBy(() -> manager.putBodyIntoGrave(b1, g3))
                .isInstanceOf(IllegalEntityException.class);

        // verify that failure was atomic and no data was changed
        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b3);
    }

    @Test
    public void putBodyIntoMultipleGraves() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        assertThatThrownBy(() -> manager.putBodyIntoGrave(b1, g2))
                .isInstanceOf(IllegalEntityException.class);

        // verify that failure was atomic and no data was changed
        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b3);
    }

    @Test
    public void putBodyIntoFullGrave() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b5, g1);
        manager.putBodyIntoGrave(b3, g3);

        assertThatThrownBy(() -> manager.putBodyIntoGrave(b2, g1))
                .isInstanceOf(IllegalEntityException.class);

        // verify that failure was atomic and no data was changed
        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b3);
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

        assertThat(manager.findGraveWithBody(b1))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b2))
                .isNull();
        assertThat(manager.findGraveWithBody(b3))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b4))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b5))
                .isEqualToComparingFieldByField(g1);

        manager.removeBodyFromGrave(b3, g3);

        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b4);


        assertThat(manager.findGraveWithBody(b1))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b2))
                .isNull();
        assertThat(manager.findGraveWithBody(b3))
                .isNull();
        assertThat(manager.findGraveWithBody(b4))
                .isEqualToComparingFieldByField(g3);
        assertThat(manager.findGraveWithBody(b5))
                .isEqualToComparingFieldByField(g1);
    }

    @Test
    public void removeUnburiedBodyFromGrave() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g1);

        assertThatThrownBy(() -> manager.putBodyIntoGrave(b3, g1))
                .isInstanceOf(IllegalEntityException.class);

        // Check that previous tests didn't affect data in database
        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b4);
    }

    @Test
    public void removeBodyFromGraveWhereItIsNotBuried() {

        manager.putBodyIntoGrave(b1, g3);
        manager.putBodyIntoGrave(b4, g3);
        manager.putBodyIntoGrave(b5, g1);

        assertThatThrownBy(() -> manager.putBodyIntoGrave(b1, g1))
                .isInstanceOf(IllegalEntityException.class);

        // Check that previous tests didn't affect data in database
        assertThat(manager.findBodiesInGrave(g1))
                .usingFieldByFieldElementComparator()
                .containsOnly(b5);
        assertThat(manager.findBodiesInGrave(g2))
                .isEmpty();
        assertThat(manager.findBodiesInGrave(g3))
                .usingFieldByFieldElementComparator()
                .containsOnly(b1,b4);
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
