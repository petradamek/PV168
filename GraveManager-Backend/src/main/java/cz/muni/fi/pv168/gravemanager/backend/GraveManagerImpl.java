package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * This class implements GraveManager service.
 * 
 * @author Petr Adamek
 */
public class GraveManagerImpl implements GraveManager {

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
    public List<Grave> findAllGraves() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, col, row, capacity, note FROM Grave");
            return executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all graves from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }          
    }
    
    @Override
    public void createGrave(Grave grave) {
        checkDataSource();
        validate(grave);
        if (grave.getId() != null) {
            throw new IllegalEntityException("grave id is already set");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Grave (row,col,capacity,note) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, grave.getRow());
            st.setInt(2, grave.getColumn());
            st.setInt(3, grave.getCapacity());
            st.setString(4, grave.getNote());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, grave, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            grave.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting grave into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Grave getGrave(Long id) {

        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, col, row, capacity, note FROM Grave WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleGrave(st);
        } catch (SQLException ex) {
            String msg = "Error when getting grave with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateGrave(Grave grave) {
        checkDataSource();
        validate(grave);
        if (grave.getId() == null) {
            throw new IllegalEntityException("grave id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Grave SET row = ?, col = ?, capacity = ?, note = ? WHERE id = ?");
            st.setInt(1, grave.getRow());
            st.setInt(2, grave.getColumn());
            st.setInt(3, grave.getCapacity());
            st.setString(4, grave.getNote());
            st.setLong(5, grave.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, grave, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating grave in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deleteGrave(Grave grave) {
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
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Grave WHERE id = ?");
            st.setLong(1, grave.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, grave, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting grave from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    static Grave executeQueryForSingleGrave(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Grave result = rowToGrave(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more graves with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    static List<Grave> executeQueryForMultipleGraves(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Grave> result = new ArrayList<Grave>();
        while (rs.next()) {
            result.add(rowToGrave(rs));
        }
        return result;
    }
    
    private static Grave rowToGrave(ResultSet rs) throws SQLException {
        Grave result = new Grave();
        result.setId(rs.getLong("id"));
        result.setColumn(rs.getInt("col"));
        result.setRow(rs.getInt("row"));
        result.setCapacity(rs.getInt("capacity"));
        result.setNote(rs.getString("note"));
        return result;
    }

    private static void validate(Grave grave) {
        if (grave == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (grave.getRow() < 0) {
            throw new ValidationException("row is negative number");
        }
        if (grave.getColumn() < 0) {
            throw new ValidationException("column is negative number");
        }
        if (grave.getCapacity() <= 0) {
            throw new ValidationException("capacity is not positive number");
        }       
    }
}
