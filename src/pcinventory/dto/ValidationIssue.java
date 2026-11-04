package pcinventory.dto;

import java.util.Objects;
import pcinventory.model.ComponentType;

/** Stable issue codes support tests and UI localization. */
public record ValidationIssue(String code, ComponentType componentType, String message) {
    public ValidationIssue {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code");
        Objects.requireNonNull(message, "message");
    }
}
