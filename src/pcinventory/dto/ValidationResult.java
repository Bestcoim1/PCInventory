package pcinventory.dto;

import java.util.List;
import java.util.Set;
import pcinventory.model.ComponentType;

/**
 * Draft: compatible can be true with pending checks.
 * Final: compatible is true only if errors AND pendingChecks are empty.
 */
public final class ValidationResult {
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> pendingChecks;
    private final Set<ComponentType> affectedTypes;

    public ValidationResult(List<ValidationIssue> errors,
                            List<ValidationIssue> pendingChecks,
                            Set<ComponentType> affectedTypes) {
        this.errors = List.copyOf(errors);
        this.pendingChecks = List.copyOf(pendingChecks);
        this.affectedTypes = Set.copyOf(affectedTypes);
    }
    public List<ValidationIssue> getErrors() { return errors; }
    public List<ValidationIssue> getPendingChecks() { return pendingChecks; }
    public Set<ComponentType> getAffectedTypes() { return affectedTypes; }
    public boolean isCompatible() { return errors.isEmpty(); }
    public boolean isFinalValid() { return errors.isEmpty() && pendingChecks.isEmpty(); }
}
