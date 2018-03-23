package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements BodyManager service.
 *
 * @author Petr Adámek
 */
public class BodyManagerImpl implements BodyManager {

    private final DataSource dataSource;
    private final Clock clock;

    @SuppressWarnings("WeakerAccess")
    public BodyManagerImpl(DataSource dataSource, Clock clock) {
        this.dataSource = dataSource;
        this.clock = clock;
    }

    @Override
    public List<Body> findAllBodies() throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT id, name, gender, born, died, vampire FROM Body")) {
            return executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when getting all bodies from DB", ex);
        }
    }

    @Override
    public void createBody(Body body) throws ServiceFailureException {
        validate(body);
        if (body.getId() != null) throw new IllegalEntityException("body id is already set");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "INSERT INTO Body (name,gender,born,died,vampire) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, body.getName());
            st.setString(2, toString(body.getGender()));

            // This is the proper way, how to handle LocalDate, however it is not
            // supported by Derby yet - see https://issues.apache.org/jira/browse/DERBY-6445
            //st.setObject(3, body.getBorn());
            //st.setObject(4, body.getDied());

            st.setDate(3, toSqlDate(body.getBorn()));
            st.setDate(4, toSqlDate(body.getDied()));
            st.setBoolean(5, body.isVampire());

            st.executeUpdate();
            body.setId(DBUtils.getId(st.getGeneratedKeys()));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting grave into db", ex);
        }
    }

    @Override
    public Body getBody(Long id) throws ServiceFailureException {
        if (id == null) throw new IllegalArgumentException("id is null");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT id, name, gender, born, died, vampire FROM Body WHERE id = ?")) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rowToBody(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when getting body with id = " + id + " from DB", ex);
        }
    }

    @Override
    public void updateBody(Body body) throws ServiceFailureException {
        validate(body);

        if (body.getId() == null) throw new IllegalEntityException("body id is null");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("UPDATE Body SET name = ?, gender = ?, born = ?, died = ?, vampire = ? WHERE id = ?")) {
            st.setString(1, body.getName());
            st.setString(2, toString(body.getGender()));
            st.setDate(3, toSqlDate(body.getBorn()));
            st.setDate(4, toSqlDate(body.getDied()));
            st.setBoolean(5, body.isVampire());
            st.setLong(6, body.getId());

            int count = st.executeUpdate();
            if (count != 1) throw new IllegalEntityException("updated " + count + " body records instead of 1");
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when updating body in the db", ex);
        }
    }


    @Override
    public void deleteBody(Body body) throws ServiceFailureException {
        if (body == null) throw new IllegalArgumentException("body is null");
        if (body.getId() == null) throw new IllegalEntityException("body id is null");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM Body WHERE id = ?")) {
            st.setLong(1, body.getId());
            int count = st.executeUpdate();
            if (count != 1) throw new IllegalEntityException("deleted " + count + " instead of 1 body");
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting body from the db", ex);
        }
    }

    static List<Body> executeQueryForMultipleBodies(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            List<Body> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowToBody(rs));
            }
            return result;
        }
    }

    static private Body rowToBody(ResultSet rs) throws SQLException {
        Body body = new Body();
        body.setId(rs.getLong("id"));
        body.setName(rs.getString("name"));
        body.setGender(toGender(rs.getString("gender")));
        body.setBorn(toLocalDate(rs.getDate("born")));
        body.setDied(toLocalDate(rs.getDate("died")));
        body.setVampire(rs.getBoolean("vampire"));
        return body;
    }

    private void validate(Body body) {
        if (body == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (body.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (body.getGender() == null) {
            throw new ValidationException("gender is null");
        }
        if (body.getBorn() != null && body.getDied() != null && body.getDied().isBefore(body.getBorn())) {
            throw new ValidationException("died is before born");
        }
        LocalDate today = LocalDate.now(clock);
        if (body.getBorn() != null && body.getBorn().isAfter(today)) {
            throw new ValidationException("born is in future");
        }
        if (body.getDied() != null && body.getDied().isAfter(today)) {
            throw new ValidationException("died is in future");
        }
    }

    private static Gender toGender(String gender) {
        return gender == null ? null : Gender.valueOf(gender);
    }

    private static String toString(Gender gender) {
        return gender == null ? null : gender.name();
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

}
