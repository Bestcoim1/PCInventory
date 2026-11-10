package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for STORAGE. */
public final class Storage extends Component {
    private final StorageInterface storageInterface;
    private final StorageFormFactor formFactor;
    private final int capacityGb;

    public Storage(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            StorageInterface storageInterface,
            StorageFormFactor formFactor,
            int capacityGb) {
        super(id, code, name, ComponentType.STORAGE, currentUnitPrice, active);
        if ((storageInterface == StorageInterface.SATA &&
                formFactor == StorageFormFactor.M_2) ||
            (storageInterface == StorageInterface.M2_NVME &&
                formFactor != StorageFormFactor.M_2)) {
            throw new IllegalArgumentException("Unsupported interface/formFactor combination");
        }
        this.storageInterface = java.util.Objects.requireNonNull(storageInterface, "storageInterface");
        this.formFactor = java.util.Objects.requireNonNull(formFactor, "formFactor");
        this.capacityGb = positive(capacityGb, "capacityGb");
    }

    public StorageInterface getStorageInterface() { return storageInterface; }
    public StorageFormFactor getFormFactor() { return formFactor; }
    public int getCapacityGb() { return capacityGb; }
}
