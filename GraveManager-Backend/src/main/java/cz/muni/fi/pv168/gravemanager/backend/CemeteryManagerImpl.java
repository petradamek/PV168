package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * This class implements CemeteryManager service.
 * 
 * @author Petr Adámek
 */
public class CemeteryManagerImpl implements CemeteryManager {
    
    private static final Logger logger = Logger.getLogger(
            GraveManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }    

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    @Override
    public Grave findGraveWithBody(Body body) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();        
        if (body == null) {
            throw new IllegalArgumentException("body is null");
        }        
        if (body.getId() == null) {
            throw new IllegalEntityException("body id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT Grave.id, col, row, capacity, note " +
                    "FROM Grave JOIN Body ON Grave.id = Body.graveId " +
                    "WHERE Body.id = ?");
            st.setLong(1, body.getId());
            return GraveManagerImpl.executeQueryForSingleGrave(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find grave with body " + body;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }        
    }

    @Override
    public List<Body> findBodiesInGrave(Grave grave) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();        
        if (grave == null) {
            throw new IllegalArgumentException("grave is null");
        }        
        if (grave.getId() == null) {
            throw new IllegalEntityException("grave id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT Body.id, name, gender, born, died, vampire " +
                    "FROM Body JOIN Grave ON Grave.id = Body.graveId " +
                    "WHERE Grave.id = ?");
            st.setLong(1, grave.getId());
            return BodyManagerImpl.executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find bodies in grave " + grave;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Body> findUnburiedBodies() throws ServiceFailureException {
        checkDataSource();        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, gender, born, died, vampire " +
                    "FROM Body WHERE graveId IS NULL");
            return BodyManagerImpl.executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find unburied bodies";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Grave> findEmptyGraves() throws ServiceFailureException {
        checkDataSource();        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT Grave.id, col, row, capacity, note " +
                    "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                    "GROUP BY Grave.id, col, row, capacity, note " +
                    "HAVING COUNT(Body.id) = 0");
            return GraveManagerImpl.executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find empty graves";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Grave> findGravesWithSomeFreeSpace() throws ServiceFailureException {
        checkDataSource();        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT Grave.id, col, row, capacity, note " +
                    "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                    "GROUP BY Grave.id, col, row, capacity, note " +
                    "HAVING COUNT(Body.id) < capacity");
            return GraveManagerImpl.executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find graves with some free space";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void putBodyIntoGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (grave == null) {
            throw new IllegalArgumentException("grave is null");
        }        
        if (grave.getId() == null) {
            throw new IllegalEntityException("grave id is null");
        }        
        if (body == null) {
            throw new IllegalArgumentException("body is null");
        }        
        if (body.getId() == null) {
            throw new IllegalEntityException("body id is null");
        }        
        Connection conn = null;
        PreparedStatement updateSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            checkIfGraveHasSpace(conn, grave);
            
            updateSt = conn.prepareStatement(
                    "UPDATE Body SET graveId = ? WHERE id = ? AND graveId IS NULL");
            updateSt.setLong(1, grave.getId());
            updateSt.setLong(2, body.getId());
            int count = updateSt.executeUpdate();
            if (count == 0) {
                throw new IllegalEntityException("Body " + body + " not found or it is already placed in some grave");
            }
            DBUtils.checkUpdatesCount(count, body, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting body into grave";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, updateSt);
        }
    }

    private static void checkIfGraveHasSpace(Connection conn, Grave grave) throws IllegalEntityException, SQLException {
        PreparedStatement checkSt = null;
        try {
            checkSt = conn.prepareStatement(
                    "SELECT capacity, COUNT(Body.id) as bodiesCount " +
                    "FROM Grave LEFT JOIN Body ON Grave.id = Body.graveId " +
                    "WHERE Grave.id = ? " +
                    "GROUP BY Grave.id, capacity");
            checkSt.setLong(1, grave.getId());
            ResultSet rs = checkSt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("capacity") <= rs.getInt("bodiesCount")) {
                    throw new IllegalEntityException("Grave " + grave + " is already full");
                }
            } else {
                throw new IllegalEntityException("Grave " + grave + " does not exist in the database");
            }
        } finally {
            DBUtils.closeQuietly(null, checkSt);
        }
    }

    @Override
    public void removeBodyFromGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (grave == null) {
            throw new IllegalArgumentException("grave is null");
        }        
        if (grave.getId() == null) {
            throw new IllegalEntityException("grave id is null");
        }        
        if (body == null) {
            throw new IllegalArgumentException("body is null");
        }        
        if (body.getId() == null) {
            throw new IllegalEntityException("body id is null");        
        }                             
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Body SET graveId = NULL WHERE id = ? AND graveId = ?");
            st.setLong(1, body.getId());
            st.setLong(2, grave.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, body, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting body into grave";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }    
}
