package pcinventory.dto;
/** REFRESH_REQUIRED belongs to the caller/source controller, not InventoryService. */
public enum StockStatus {
    AVAILABLE, INSUFFICIENT, UNVERIFIABLE, INVALID_REQUEST
}
