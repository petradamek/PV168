package cz.muni.fi.pv168.gravemanager.backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements {@link GraveManager}.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class GraveManagerImpl implements GraveManager {

    public GraveManagerImpl(Connection conn) {
        this.conn = conn;
    }

    // POZOR! Toto je jenom jednoducha ukazka, ktera neni vlaknove bezpecna a
    // neresi transakce. Spravne reseni s transakcemi a pouzitim DataSource
    // bude v prikladu k dalsi uloze
    private Connection conn;

    @Override
    public void createGrave(Grave grave) throws ServiceFailureException {

        if (grave == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (grave.getId() != null) {
            throw new IllegalArgumentException("grave id is already set");
        }
        if (grave.getColumn() < 0) {
            throw new IllegalArgumentException("grave column is negative number");
        }
        if (grave.getRow() < 0) {
            throw new IllegalArgumentException("grave row is negative number");
        }
        if (grave.getCapacity() <= 0) {
            throw new IllegalArgumentException("grave column is negative number");
        }

        try (PreparedStatement st = conn.prepareStatement(
                "INSERT INTO GRAVE (col,row,capacity,note) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {

            st.setInt(1, grave.getColumn());
            st.setInt(2, grave.getRow());
            st.setInt(3, grave.getCapacity());
            st.setString(4, grave.getNote());
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows "
                        + "inserted when trying to insert grave " + grave);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            grave.setId(getKey(keyRS, grave));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting grave " + grave, ex);
        }
    }

    private Long getKey(ResultSet keyRS, Grave grave) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + grave
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + grave
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + grave
                    + " - no key found");
        }
    }

    @Override
    public Grave getGrave(Long id) throws ServiceFailureException {
        try (PreparedStatement st = conn.prepareStatement(
                "SELECT id,col,row,capacity,note FROM grave WHERE id = ?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Grave grave = resultSetToGrave(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + grave + " and " + resultSetToGrave(rs));
                }

                return grave;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving grave with id " + id, ex);
        }
    }

    private Grave resultSetToGrave(ResultSet rs) throws SQLException {
        Grave grave = new Grave();
        grave.setId(rs.getLong("id"));
        grave.setColumn(rs.getInt("col"));
        grave.setRow(rs.getInt("row"));
        grave.setCapacity(rs.getInt("capacity"));
        grave.setNote(rs.getString("note"));
        return grave;
    }

    @Override
    public void updateGrave(Grave grave) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteGrave(Grave grave) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Grave> findAllGraves() throws ServiceFailureException {
        try (PreparedStatement st = conn.prepareStatement(
                "SELECT id,col,row,capacity,note FROM grave")) {
            ResultSet rs = st.executeQuery();

            List<Grave> result = new ArrayList<Grave>();
            while (rs.next()) {
                result.add(resultSetToGrave(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all graves", ex);
        }
    }

}
