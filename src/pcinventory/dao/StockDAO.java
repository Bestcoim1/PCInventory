package pcinventory.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pcinventory.service.StockBalanceReader;
import pcinventory.util.DBConnection;

/**
 * Read-only implementation for UC44. Never calls UPDATE or FOR UPDATE.
 * Ownership of stock deductions and assembly transactions remains with Hoang.
 */
public final class StockDAO implements StockBalanceReader {

    @Override
    public Map<Long, Integer> findBalancesByComponentIds(Set<Long> componentIds)
            throws SQLException, IOException {
        if (componentIds == null) throw new IllegalArgumentException("componentIds");
        if (componentIds.isEmpty()) return Map.of();
        String placeholders = componentIds.stream()
                .map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT component_id, quantity_on_hand "
                + "FROM stock_balance WHERE component_id IN (" + placeholders + ")";
        Map<Long, Integer> values = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parameter = 1;
            for (Long componentId : componentIds) {
                stmt.setLong(parameter++, componentId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.put(rs.getLong("component_id"),
                            rs.getInt("quantity_on_hand"));
                }
            }
        }
        return Map.copyOf(values);
    }
}
