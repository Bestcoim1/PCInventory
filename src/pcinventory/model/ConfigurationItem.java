package pcinventory.model;

import java.util.Objects;

/** One line, not one distinct component. lineId survives quantity edits. */
public record ConfigurationItem(String lineId, Component component, int quantity) {
    public ConfigurationItem {
        if (lineId == null || lineId.isBlank()) {
            throw new IllegalArgumentException("lineId is required");
        }
        Objects.requireNonNull(component, "component");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
    }
    public String getLineId() { return lineId; }
    public Component getComponent() { return component; }
    public int getQuantity() { return quantity; }
}
