package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for PSU. */
public final class Psu extends Component {
    private final int realWattage;
    private final String formFactor;

    public Psu(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            int realWattage,
            String formFactor) {
        super(id, code, name, ComponentType.PSU, currentUnitPrice, active);
        this.realWattage = positive(realWattage, "realWattage");
        this.formFactor = requireText(formFactor, "formFactor");
    }

    public int getRealWattage() { return realWattage; }
    public String getFormFactor() { return formFactor; }
}
