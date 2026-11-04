package pcinventory.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable draft/snapshot for CompatibilityPort. Never mutates BuildContext. */
public final class PCConfiguration {
    private final List<ConfigurationItem> items;

    public PCConfiguration(List<ConfigurationItem> items) {
        Objects.requireNonNull(items, "items");
        Set<String> ids = new HashSet<>();
        for (ConfigurationItem item : items) {
            Objects.requireNonNull(item, "configuration item");
            if (!ids.add(item.lineId())) {
                throw new IllegalArgumentException("Duplicate lineId: " + item.lineId());
            }
        }
        this.items = List.copyOf(items);
    }

    public List<ConfigurationItem> getItems() { return items; }
    public List<ConfigurationItem> items() { return items; }
}
