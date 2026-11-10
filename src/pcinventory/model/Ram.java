package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for RAM. */
public final class Ram extends Component {
    private final String ramType;
    private final int busMhz;
    private final int capacityGb;

    public Ram(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            String ramType,
            int busMhz,
            int capacityGb) {
        super(id, code, name, ComponentType.RAM, currentUnitPrice, active);
        this.ramType = requireText(ramType, "ramType");
        this.busMhz = positive(busMhz, "busMhz");
        this.capacityGb = positive(capacityGb, "capacityGb");
    }

    public String getRamType() { return ramType; }
    public int getBusMhz() { return busMhz; }
    public int getCapacityGb() { return capacityGb; }
}
