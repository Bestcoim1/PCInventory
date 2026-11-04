package pcinventory.dto;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import pcinventory.model.ComponentType;

/** Draft may contain pending checks; final verification must not. */
public final class ValidationResult {
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> pendingChecks;
    private final Set<ComponentType> affectedTypes;
    private final boolean finalCheck;

    public ValidationResult(List<ValidationIssue> errors,
                            List<ValidationIssue> pendingChecks,
                            Set<ComponentType> affectedTypes) {
        this(errors, pendingChecks, affectedTypes, false);
    }

    public ValidationResult(List<ValidationIssue> errors,
                            List<ValidationIssue> pendingChecks,
                            Set<ComponentType> affectedTypes,
                            boolean finalCheck) {
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors"));
        this.pendingChecks = List.copyOf(
                Objects.requireNonNull(pendingChecks, "pendingChecks"));
        this.affectedTypes = Set.copyOf(
                Objects.requireNonNull(affectedTypes, "affectedTypes"));
        this.finalCheck = finalCheck;
    }

    public List<ValidationIssue> getErrors() { return errors; }
    public List<ValidationIssue> getPendingChecks() { return pendingChecks; }
    public Set<ComponentType> getAffectedTypes() { return affectedTypes; }
    public boolean isFinalCheck() { return finalCheck; }
    /** Compatible draft = no proven errors. Final also requires no pending. */
    public boolean isCompatible() {
        return errors.isEmpty() && (!finalCheck || pendingChecks.isEmpty());
    }
    public boolean isFinalValid() {
        return finalCheck && errors.isEmpty() && pendingChecks.isEmpty();
    }
}
