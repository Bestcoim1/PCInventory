package pcinventory.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pcinventory.dto.StockCheckResult;
import pcinventory.dto.StockLine;
import pcinventory.dto.StockStatus;
import pcinventory.model.ConfigurationItem;
import pcinventory.model.PCConfiguration;

/**
 * UC44: checks a caller-verified immutable snapshot; no stock reservations.
 * Snapshot revision/identity and UC43 validity are verified by the caller.
 * UC42 must recheck stock inside its own atomic assembly transaction.
 */
public final class InventoryService {
    private final StockBalanceReader stockBalanceReader;

    public InventoryService(StockBalanceReader stockBalanceReader) {
        this.stockBalanceReader = Objects.requireNonNull(
                stockBalanceReader, "stockBalanceReader");
    }

    public StockCheckResult checkAvailability(PCConfiguration snapshot)
            throws IOException, SQLException {
        if (snapshot == null || snapshot.items().isEmpty()) {
            return new StockCheckResult(StockStatus.INVALID_REQUEST,
                    List.of(), "Cấu hình cần kiểm tra không có linh kiện.");
        }

        Map<Long, Long> demand = new LinkedHashMap<>();
        try {
            for (ConfigurationItem item : snapshot.items()) {
                if (item.quantity() <= 0 || item.component().getId() <= 0) {
                    return new StockCheckResult(StockStatus.INVALID_REQUEST,
                            List.of(), "Số lượng hoặc mã linh kiện không hợp lệ.");
                }
                demand.merge(item.component().getId(),
                        (long) item.quantity(), Math::addExact);
            }
        } catch (ArithmeticException e) {
            return new StockCheckResult(StockStatus.INVALID_REQUEST,
                    List.of(), "Tổng số lượng linh kiện vượt giới hạn.");
        }

        Map<Long, Integer> balances = stockBalanceReader.findBalancesByComponentIds(
                Set.copyOf(demand.keySet()));
        if (balances == null) {
            return new StockCheckResult(StockStatus.UNVERIFIABLE,
                    List.of(), "Không thể xác minh dữ liệu tồn kho.");
        }

        boolean missing = false;
        boolean insufficient = false;
        List<StockLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : demand.entrySet()) {
            Integer available = balances.get(entry.getKey());
            long required = entry.getValue();
            if (available == null || available < 0) {
                missing = true;
                lines.add(new StockLine(entry.getKey(), required, null, 0));
            } else {
                long shortage = Math.max(0L, required - available);
                if (shortage > 0) insufficient = true;
                lines.add(new StockLine(entry.getKey(), required, available, shortage));
            }
        }

        StockStatus status = missing ? StockStatus.UNVERIFIABLE
                : insufficient ? StockStatus.INSUFFICIENT : StockStatus.AVAILABLE;
        String message = switch (status) {
            case AVAILABLE -> "Đủ hàng tại thời điểm kiểm tra.";
            case INSUFFICIENT -> "Có linh kiện thiếu tồn kho.";
            case UNVERIFIABLE -> "Thiếu dữ liệu tồn kho cho ít nhất một linh kiện.";
            case INVALID_REQUEST -> "Yêu cầu không hợp lệ.";
        };
        return new StockCheckResult(status, lines, message);
    }
}
