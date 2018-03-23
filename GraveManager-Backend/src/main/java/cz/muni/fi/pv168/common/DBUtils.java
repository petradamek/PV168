package cz.muni.fi.pv168.common;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Some DB tools.
 *
 * @author Petr Adamek
 * @author Martin Kuba
 */
public class DBUtils {

    /**
     * Extract key from ResultSet got from PreparedStatement.getGeneratedKeys().
     *
     * @param key resultSet with key
     * @return key from given result set
     * @throws SQLException when operation fails
     */
    public static Long getId(ResultSet key) throws SQLException {
        if (key.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        if (key.next()) {
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }

    /**
     * Executes SQL script.
     *
     * @param ds datasource
     * @param is InputStream obtained by getClass().getResourceAsStream()
     * @throws SQLException when operation fails
     */
    public static void executeSqlScript(DataSource ds, InputStream is) throws SQLException, IOException {
        try (Connection c = ds.getConnection()) {
            Scanner s = new Scanner(is).useDelimiter(";");
            while (s.hasNext()) {
                String sql = s.next().trim();
                if (sql.isEmpty()) continue;
                try (PreparedStatement st1 = c.prepareStatement(sql)) {
                    st1.execute();
                }
            }
        }
    }

}
