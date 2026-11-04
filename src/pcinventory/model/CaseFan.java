package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for CASE_FAN. */
public final class CaseFan extends Component {
    private final int sizeMm;

    public CaseFan(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            int sizeMm) {
        super(id, code, name, ComponentType.CASE_FAN, currentUnitPrice, active);
        this.sizeMm = positive(sizeMm, "sizeMm");
    }

    public int getSizeMm() { return sizeMm; }
}
