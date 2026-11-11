package pcinventory.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pcinventory.util.DBConnection;

/**
 * The first read-only DAO used for the UI shell smoke test.
 * Feature teams can add their own queries in separate DAO classes.
 */
public final class ComponentDAO {

    public int countAll() throws SQLException, IOException {
        String sql = "SELECT COUNT(*) FROM component";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }
}
