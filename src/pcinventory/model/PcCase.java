package pcinventory.model;

import java.math.BigDecimal;
import java.util.Set;

/** Immutable typed technical specs for CASE. */
public final class PcCase extends Component {
    private final int maxCpuCoolerHeightMm;
    private final int maxGpuLengthMm;
    private final int maxCaseFanCount;
    private final int includedCaseFanCount;
    private final int driveBay25Count;
    private final int driveBay35Count;
    private final RadiatorSize maxRadiatorSize;
    private final Set<String> supportedMainboardForms;
    private final Set<String> supportedPsuForms;
    private final Set<Integer> supportedFanSizes;

    public PcCase(long id, String code, String name, BigDecimal currentUnitPrice, boolean active,
            int maxCpuCoolerHeightMm,
            int maxGpuLengthMm,
            int maxCaseFanCount,
            int includedCaseFanCount,
            int driveBay25Count,
            int driveBay35Count,
            RadiatorSize maxRadiatorSize,
            Set<String> supportedMainboardForms,
            Set<String> supportedPsuForms,
            Set<Integer> supportedFanSizes) {
        super(id, code, name, ComponentType.CASE, currentUnitPrice, active);
        if (includedCaseFanCount > maxCaseFanCount) {
            throw new IllegalArgumentException("includedCaseFanCount exceeds maximum");
        }
        this.maxCpuCoolerHeightMm = positive(maxCpuCoolerHeightMm, "maxCpuCoolerHeightMm");
        this.maxGpuLengthMm = positive(maxGpuLengthMm, "maxGpuLengthMm");
        this.maxCaseFanCount = nonnegative(maxCaseFanCount, "maxCaseFanCount");
        this.includedCaseFanCount = nonnegative(includedCaseFanCount, "includedCaseFanCount");
        this.driveBay25Count = nonnegative(driveBay25Count, "driveBay25Count");
        this.driveBay35Count = nonnegative(driveBay35Count, "driveBay35Count");
        this.maxRadiatorSize = java.util.Objects.requireNonNull(maxRadiatorSize, "maxRadiatorSize");
        this.supportedMainboardForms = strings(supportedMainboardForms, "supportedMainboardForms");
        this.supportedPsuForms = strings(supportedPsuForms, "supportedPsuForms");
        this.supportedFanSizes = integers(supportedFanSizes, "supportedFanSizes");
    }

    public int getMaxCpuCoolerHeightMm() { return maxCpuCoolerHeightMm; }
    public int getMaxGpuLengthMm() { return maxGpuLengthMm; }
    public int getMaxCaseFanCount() { return maxCaseFanCount; }
    public int getIncludedCaseFanCount() { return includedCaseFanCount; }
    public int getDriveBay25Count() { return driveBay25Count; }
    public int getDriveBay35Count() { return driveBay35Count; }
    public RadiatorSize getMaxRadiatorSize() { return maxRadiatorSize; }
    public Set<String> getSupportedMainboardForms() { return supportedMainboardForms; }
    public Set<String> getSupportedPsuForms() { return supportedPsuForms; }
    public Set<Integer> getSupportedFanSizes() { return supportedFanSizes; }
}
