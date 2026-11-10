package pcinventory.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/** Read-only adapter for UC44. Missing key means missing stock_balance row. */
@FunctionalInterface
public interface StockBalanceReader {
    Map<Long, Integer> findBalancesByComponentIds(Set<Long> componentIds)
            throws SQLException, IOException;
}
