package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements GraveManager service.
 *
 * @author Petr Adamek
 */
public class GraveManagerImpl implements GraveManager {

    private DataSource dataSource;

    @SuppressWarnings("WeakerAccess")
    public GraveManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Grave> findAllGraves() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT id, col, row, capacity, note FROM Grave")) {
            return executeQueryForMultipleGraves(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when getting all graves from DB", ex);
        }
    }

    @Override
    public void createGrave(Grave grave) {
        validate(grave);
        if (grave.getId() != null) throw new IllegalEntityException("grave id is already set");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("INSERT INTO Grave (row,col,capacity,note) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, grave.getRow());
            st.setInt(2, grave.getColumn());
            st.setInt(3, grave.getCapacity());
            st.setString(4, grave.getNote());
            st.executeUpdate();
            grave.setId(DBUtils.getId(st.getGeneratedKeys()));
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting grave into db", ex);
        }
    }

    @Override
    public Grave getGrave(Long id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT id, col, row, capacity, note FROM Grave WHERE id = ?")) {
            st.setLong(1, id);
            return executeQueryForSingleGrave(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when getting grave with id = " + id + " from DB", ex);
        }
    }

    @Override
    public void updateGrave(Grave grave) {
        validate(grave);
        if (grave.getId() == null) throw new IllegalEntityException("grave id is null");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("UPDATE Grave SET row = ?, col = ?, capacity = ?, note = ? WHERE id = ?")) {
            st.setInt(1, grave.getRow());
            st.setInt(2, grave.getColumn());
            st.setInt(3, grave.getCapacity());
            st.setString(4, grave.getNote());
            st.setLong(5, grave.getId());
            int count = st.executeUpdate();
            if (count != 1) throw new IllegalEntityException("updated " + count + " instead of 1 grave");
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when updating grave in the db", ex);
        }
    }

    @Override
    public void deleteGrave(Grave grave) {
        if (grave == null) throw new IllegalArgumentException("grave is null");
        if (grave.getId() == null) throw new IllegalEntityException("grave id is null");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM Grave WHERE id = ?")) {
            st.setLong(1, grave.getId());
            int count = st.executeUpdate();
            if (count != 1) throw new IllegalEntityException("deleted " + count + " instead of 1 grave");
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting grave from the db", ex);
        }
    }

    static Grave executeQueryForSingleGrave(PreparedStatement st) throws SQLException, ServiceFailureException {
        try (ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                return rowToGrave(rs);
            } else {
                return null;
            }
        }
    }

    static List<Grave> executeQueryForMultipleGraves(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            List<Grave> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowToGrave(rs));
            }
            return result;
        }
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
