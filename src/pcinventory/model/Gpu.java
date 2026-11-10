package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for GPU. */
public final class Gpu extends Component {
    private final int lengthMm;
    private final int minimumPsuWattage;

    public Gpu(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            int lengthMm,
            int minimumPsuWattage) {
        super(id, code, name, ComponentType.GPU, currentUnitPrice, active);
        this.lengthMm = positive(lengthMm, "lengthMm");
        this.minimumPsuWattage = positive(minimumPsuWattage, "minimumPsuWattage");
    }

    public int getLengthMm() { return lengthMm; }
    public int getMinimumPsuWattage() { return minimumPsuWattage; }
}
