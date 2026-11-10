package pcinventory.dto;

/**
 * available == null when the stock_balance row is missing.
 * shortage is computed only for verified balances.
 */
public record StockLine(long componentId, long required, Integer available,
                        long shortage) {
    public boolean isVerified() { return available != null && available >= 0; }
}
