
package pcinventory.app;

import pcinventory.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {

        System.out.println("Dang ket noi MySQL...");

        try (Connection conn =
                DBConnection.getConnection()) {

            System.out.println(
                "Ket noi MySQL thanh cong!");

            // Kiểm tra tên CSDL hiện tại
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT DATABASE()")) {

                if (rs.next()) {
                    System.out.println(
                        "Database: " + rs.getString(1));
                }
            }

            // Đếm số linh kiện
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT COUNT(*) FROM component")) {

                if (rs.next()) {
                    System.out.println(
                        "Tong so linh kien: " +
                        rs.getInt(1));
                }
            }

            // Đọc thử 5 linh kiện
            String sql = """
                SELECT component_code, component_name
                FROM component
                ORDER BY id
                LIMIT 5
                """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.println(
                    "--- Danh sach linh kien ---");

                while (rs.next()) {
                    System.out.println(
                        rs.getString("component_code")
                        + " | "
                        + rs.getString("component_name"));
                }
            }

        } catch (Exception e) {
            System.err.println(
                "Khong the ket noi hoac truy van MySQL.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
