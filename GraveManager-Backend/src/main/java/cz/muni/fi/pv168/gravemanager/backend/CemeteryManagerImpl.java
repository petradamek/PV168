package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class implements CemeteryManager service.
 *
 * @author Petr Adámek
 */
public class CemeteryManagerImpl implements CemeteryManager {

    private DataSource dataSource;

    @SuppressWarnings("WeakerAccess")
    public CemeteryManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Grave findGraveWithBody(Body body) throws ServiceFailureException, IllegalEntityException {
        if (body == null) throw new IllegalArgumentException("body is null");
        if (body.getId() == null) throw new IllegalEntityException("body id is null");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT Grave.id, col, row, capacity, note " +
                             "FROM Grave JOIN Body ON Grave.id = Body.graveId " +
                             "WHERE Body.id = ?")) {
            st.setLong(1, body.getId());
            return GraveManagerImpl.executeQueryForSingleGrave(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when trying to find grave with body " + body, ex);
        }
    }

    @Override
    public List<Body> findBodiesInGrave(Grave grave) throws ServiceFailureException, IllegalEntityException {
        if (grave == null) throw new IllegalArgumentException("grave is null");
        if (grave.getId() == null) throw new IllegalEntityException("grave id is null");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT Body.id, name, gender, born, died, vampire " +
                             "FROM Body JOIN Grave ON Grave.id = Body.graveId " +
                             "WHERE Grave.id = ?")) {
            st.setLong(1, grave.getId());
            return BodyManagerImpl.executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when trying to find bodies in grave " + grave, ex);
        }
    }

    @Override
    public List<Body> findUnburiedBodies() throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT id, name, gender, born, died, vampire FROM Body WHERE graveId IS NULL")) {
            return BodyManagerImpl.executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when trying to find unburied bodies", ex);
        }
    }

    @Override
    public List<Grave> findEmptyGraves() throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT Grave.id, col, row, capacity, note " +
                             "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                             "GROUP BY Grave.id, col, row, capacity, note " +
                             "HAVING COUNT(Body.id) = 0")) {
            return GraveManagerImpl.executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when trying to find empty graves", ex);
        }
    }

    @Override
    public List<Grave> findGravesWithSomeFreeSpace() throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT Grave.id, col, row, capacity, note " +
                             "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                             "GROUP BY Grave.id, col, row, capacity, note " +
                             "HAVING COUNT(Body.id) < capacity")) {

            return GraveManagerImpl.executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when trying to find graves with some free space", ex);
        }
    }

    @Override
    public void putBodyIntoGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException {
        if (grave == null) throw new IllegalArgumentException("grave is null");
        if (grave.getId() == null) throw new IllegalEntityException("grave id is null");
        if (body == null) throw new IllegalArgumentException("body is null");
        if (body.getId() == null) throw new IllegalEntityException("body id is null");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE Body SET graveId = ? WHERE id = ? AND graveId IS NULL")) {
                // Start transaction so that the check for enough space in grave and putting body into the grave are atomic
                conn.setAutoCommit(false);
                //check if the grave has some free capacity left
                checkIfGraveHasSpace(conn, grave);
                //put the body into the grave
                st.setLong(1, grave.getId());
                st.setLong(2, body.getId());
                int count = st.executeUpdate();
                if (count == 0)
                    throw new IllegalEntityException("Body " + body + " not found or it is already placed in some grave");
                if (count != 1)
                    throw new IllegalEntityException("updated " + count + " instead of 1 body");
                conn.commit();
            } catch (Exception ex) {
                //something failed, let's rollback
                conn.rollback();
                throw ex;
            } finally {
                //re-enable autocommit mode
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when putting body into grave", ex);
        }
    }

    private static void checkIfGraveHasSpace(Connection conn, Grave grave) throws IllegalEntityException, SQLException {
        try (PreparedStatement checkSt = conn.prepareStatement(
                "SELECT capacity, COUNT(Body.id) AS bodiesCount " +
                        "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                        "WHERE Grave.id = ? " +
                        "GROUP BY Grave.id, capacity")) {
            checkSt.setLong(1, grave.getId());
            try (ResultSet rs = checkSt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("capacity") <= rs.getInt("bodiesCount")) {
                        throw new IllegalEntityException("Grave " + grave + " is already full");
                    }
                } else {
                    throw new IllegalEntityException("Grave " + grave + " does not exist in the database");
                }
            }
        }
    }

    @Override
    public void removeBodyFromGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException {
        if (grave == null) throw new IllegalArgumentException("grave is null");
        if (grave.getId() == null) throw new IllegalEntityException("grave id is null");
        if (body == null) throw new IllegalArgumentException("body is null");
        if (body.getId() == null) throw new IllegalEntityException("body id is null");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE Body SET graveId = NULL WHERE id = ? AND graveId = ?")) {
            st.setLong(1, body.getId());
            st.setLong(2, grave.getId());
            int count = st.executeUpdate();
            if (count != 1) throw new IllegalEntityException("updated " + count + " instead of 1 body");
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when putting body into grave", ex);
        }
    }
}
