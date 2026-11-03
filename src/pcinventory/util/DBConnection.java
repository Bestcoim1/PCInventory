
package pcinventory.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private DBConnection() {
        // Không khởi tạo đối tượng DBConnection
    }

    public static Connection getConnection()
            throws SQLException, IOException {

        Path configPath =
                Path.of("config", "db.properties");

        Properties properties = new Properties();

        try (InputStream input =
                Files.newInputStream(configPath)) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password =
                properties.getProperty("db.password");

        if (url == null || user == null ||
                password == null) {
            throw new IOException(
                "Thiếu thông tin trong db.properties");
        }

        return DriverManager.getConnection(
                url, user, password);
    }
}
