package pcinventory.dto;

import java.util.List;
import java.util.Objects;

/** Read-only UC44 result. It does not reserve or deduct stock. */
public record StockCheckResult(StockStatus status, List<StockLine> lines,
                               String message) {
    public StockCheckResult {
        Objects.requireNonNull(status, "status");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        Objects.requireNonNull(message, "message");
    }
}
