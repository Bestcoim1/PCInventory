package pcinventory.model;

import java.math.BigDecimal;
import java.util.Set;

/** Immutable typed technical specs for MAINBOARD. */
public final class Mainboard extends Component {
    private final String socket;
    private final String ramType;
    private final int maxRamCapacityGb;
    private final String formFactor;
    private final int sataPortCount;
    private final int m2SlotCount;
    private final Set<Integer> supportedRamBuses;

    public Mainboard(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            String socket,
            String ramType,
            int maxRamCapacityGb,
            String formFactor,
            int sataPortCount,
            int m2SlotCount,
            Set<Integer> supportedRamBuses) {
        super(id, code, name, ComponentType.MAINBOARD, currentUnitPrice, active);
        this.socket = requireText(socket, "socket");
        this.ramType = requireText(ramType, "ramType");
        this.maxRamCapacityGb = positive(maxRamCapacityGb, "maxRamCapacityGb");
        this.formFactor = requireText(formFactor, "formFactor");
        this.sataPortCount = nonnegative(sataPortCount, "sataPortCount");
        this.m2SlotCount = nonnegative(m2SlotCount, "m2SlotCount");
        this.supportedRamBuses = integers(supportedRamBuses, "supportedRamBuses");
    }

    public String getSocket() { return socket; }
    public String getRamType() { return ramType; }
    public int getMaxRamCapacityGb() { return maxRamCapacityGb; }
    public String getFormFactor() { return formFactor; }
    public int getSataPortCount() { return sataPortCount; }
    public int getM2SlotCount() { return m2SlotCount; }
    public Set<Integer> getSupportedRamBuses() { return supportedRamBuses; }
}
