package pcinventory.model;

import java.math.BigDecimal;
import java.util.Set;

/** Immutable typed technical specs for CPU_COOLER. */
public final class CpuCooler extends Component {
    private final CoolingType coolingType;
    private final Integer heightMm;
    private final RadiatorSize radiatorSize;
    private final Set<String> supportedSockets;

    public CpuCooler(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            CoolingType coolingType,
            Integer heightMm,
            RadiatorSize radiatorSize,
            Set<String> supportedSockets) {
        super(id, code, name, ComponentType.CPU_COOLER, currentUnitPrice, active);
        if ((coolingType == CoolingType.AIR &&
                (heightMm == null || heightMm <= 0 || radiatorSize != RadiatorSize.NONE)) ||
            (coolingType == CoolingType.LIQUID &&
                (heightMm != null || radiatorSize == RadiatorSize.NONE))) {
            throw new IllegalArgumentException("Invalid cooling type/size combination");
        }
        this.coolingType = java.util.Objects.requireNonNull(coolingType, "coolingType");
        this.heightMm = heightMm;
        this.radiatorSize = java.util.Objects.requireNonNull(radiatorSize, "radiatorSize");
        this.supportedSockets = strings(supportedSockets, "supportedSockets");
    }

    public CoolingType getCoolingType() { return coolingType; }
    public Integer getHeightMm() { return heightMm; }
    public RadiatorSize getRadiatorSize() { return radiatorSize; }
    public Set<String> getSupportedSockets() { return supportedSockets; }
}
