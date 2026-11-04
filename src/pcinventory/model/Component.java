package pcinventory.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable domain base class. Technical specs live in exactly one subclass.
 * No stock quantity or database connection is stored in Component.
 */
public abstract class Component {
    private final long id;
    private final String code;
    private final String name;
    private final ComponentType type;
    private final BigDecimal currentUnitPrice;
    private final boolean active;

    protected Component(long id, String code, String name, ComponentType type,
                        BigDecimal currentUnitPrice, boolean active) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (currentUnitPrice == null || currentUnitPrice.signum() < 0) {
            throw new IllegalArgumentException("Price must be nonnegative");
        }
        this.id = id;
        this.code = requireText(code, "code");
        this.name = requireText(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.currentUnitPrice = currentUnitPrice;
        this.active = active;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public ComponentType getType() { return type; }
    public BigDecimal getCurrentUnitPrice() { return currentUnitPrice; }
    public boolean isActive() { return active; }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
    protected static int positive(int value, String field) {
        if (value <= 0) throw new IllegalArgumentException(field + " must be > 0");
        return value;
    }
    protected static int nonnegative(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " must be >= 0");
        return value;
    }
    protected static Set<String> strings(Set<String> values, String field) {
        Objects.requireNonNull(values, field);
        Set<String> copy = new LinkedHashSet<>();
        for (String value : values) copy.add(requireText(value, field));
        return Set.copyOf(copy);
    }
    protected static Set<Integer> integers(Set<Integer> values, String field) {
        Objects.requireNonNull(values, field);
        Set<Integer> copy = new LinkedHashSet<>();
        for (Integer value : values) copy.add(positive(
                Objects.requireNonNull(value, field), field));
        return Set.copyOf(copy);
    }
}
