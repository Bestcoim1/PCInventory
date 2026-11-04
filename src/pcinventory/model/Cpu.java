package pcinventory.model;

import java.math.BigDecimal;

/** Immutable typed technical specs for CPU. */
public final class Cpu extends Component {
    private final String socket;
    private final boolean hasIntegratedGraphics;
    private final boolean includesStockCooler;

    public Cpu(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            String socket,
            boolean hasIntegratedGraphics,
            boolean includesStockCooler) {
        super(id, code, name, ComponentType.CPU, currentUnitPrice, active);
        this.socket = requireText(socket, "socket");
        this.hasIntegratedGraphics = hasIntegratedGraphics;
        this.includesStockCooler = includesStockCooler;
    }

    public String getSocket() { return socket; }
    public boolean hasIntegratedGraphics() { return hasIntegratedGraphics; }
    public boolean includesStockCooler() { return includesStockCooler; }
}
