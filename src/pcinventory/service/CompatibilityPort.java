package pcinventory.service;

import java.util.List;
import pcinventory.model.Component;
import pcinventory.model.ComponentType;
import pcinventory.model.PCConfiguration;
import pcinventory.dto.ValidationResult;

/** Design Contract v1: keep these three methods unchanged. */
public interface CompatibilityPort {
    List<Component> filterCompatible(
            PCConfiguration config, ComponentType targetType,
            List<Component> candidates);

    ValidationResult revalidateDependencies(PCConfiguration config);

    ValidationResult validateFinal(PCConfiguration config);
}
