package pcinventory.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pcinventory.dto.ValidationIssue;
import pcinventory.dto.ValidationResult;
import pcinventory.model.CaseFan;
import pcinventory.model.Component;
import pcinventory.model.ComponentType;
import pcinventory.model.ConfigurationItem;
import pcinventory.model.CoolingType;
import pcinventory.model.Cpu;
import pcinventory.model.CpuCooler;
import pcinventory.model.Gpu;
import pcinventory.model.Mainboard;
import pcinventory.model.PCConfiguration;
import pcinventory.model.PcCase;
import pcinventory.model.Psu;
import pcinventory.model.Ram;
import pcinventory.model.RadiatorSize;
import pcinventory.model.Storage;
import pcinventory.model.StorageFormFactor;
import pcinventory.model.StorageInterface;

/**
 * Pure, read-only rules used by UC28-32 and UC43, also reusable by Hung's
 * UC27/33-35. Does not load from MySQL or change a BuildContext.
 *
 * In filterCompatible, the caller supplies a valid selection context:
 * replacing a single slot requires removing its old item from the COPY first.
 * Filtering tests one candidate unit; later quantities are revalidated.
 */
public final class CompatibilityService implements CompatibilityPort {
    private static final Set<ComponentType> SINGLETONS = EnumSet.of(
            ComponentType.CPU, ComponentType.MAINBOARD, ComponentType.GPU,
            ComponentType.PSU, ComponentType.CPU_COOLER, ComponentType.CASE);

    @Override
    public List<Component> filterCompatible(PCConfiguration config,
                                            ComponentType targetType,
                                            List<Component> candidates) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(candidates, "candidates");
        List<Component> accepted = new ArrayList<>();
        int index = 0;
        for (Component candidate : candidates) {
            if (candidate == null || !candidate.isActive() ||
                    candidate.getType() != targetType) {
                continue;
            }
            List<ConfigurationItem> proposed = new ArrayList<>(config.items());
            String lineId;
            do {
                lineId = "__filter_candidate_" + (index++);
            } while (containsLine(config.items(), lineId));
            proposed.add(new ConfigurationItem(lineId, candidate, 1));
            ValidationResult result = revalidateDependencies(
                    new PCConfiguration(proposed));
            if (result.getErrors().isEmpty()) accepted.add(candidate);
        }
        return List.copyOf(accepted);
    }

    private static boolean containsLine(List<ConfigurationItem> items, String key) {
        for (ConfigurationItem item : items) {
            if (item.lineId().equals(key)) return true;
        }
        return false;
    }

    @Override
    public ValidationResult revalidateDependencies(PCConfiguration config) {
        return inspect(Objects.requireNonNull(config, "config"), false);
    }

    @Override
    public ValidationResult validateFinal(PCConfiguration config) {
        return inspect(Objects.requireNonNull(config, "config"), true);
    }

    private ValidationResult inspect(PCConfiguration config, boolean finalCheck) {
        Findings out = new Findings();
        Map<ComponentType, List<ConfigurationItem>> byType =
                new EnumMap<>(ComponentType.class);
        for (ComponentType type : ComponentType.values()) {
            byType.put(type, new ArrayList<>());
        }
        for (ConfigurationItem item : config.items()) {
            byType.get(item.component().getType()).add(item);
            if (!item.component().isActive()) {
                out.error("INACTIVE_COMPONENT", item.component().getType(),
                        "Linh kiện đã ngừng hoạt động: " + item.component().getCode());
            }
        }

        for (ComponentType type : SINGLETONS) {
            long count = quantity(byType.get(type));
            if (count > 1) {
                out.error("MULTIPLE_SINGLETON", type,
                        "Chỉ cho phép một linh kiện thuộc nhóm " + type);
            }
        }

        Cpu cpu = first(byType, ComponentType.CPU, Cpu.class);
        Mainboard mb = first(byType, ComponentType.MAINBOARD, Mainboard.class);
        Gpu gpu = first(byType, ComponentType.GPU, Gpu.class);
        Psu psu = first(byType, ComponentType.PSU, Psu.class);
        PcCase pcCase = first(byType, ComponentType.CASE, PcCase.class);
        CpuCooler cooler = first(byType, ComponentType.CPU_COOLER, CpuCooler.class);
        List<ConfigurationItem> rams = byType.get(ComponentType.RAM);
        List<ConfigurationItem> drives = byType.get(ComponentType.STORAGE);
        List<ConfigurationItem> fans = byType.get(ComponentType.CASE_FAN);

        // UC28: CPU / Mainboard socket.
        if (cpu != null && mb != null && !cpu.getSocket().equals(mb.getSocket())) {
            out.error("SOCKET_MISMATCH", ComponentType.MAINBOARD,
                    "Socket CPU và Mainboard không tương thích.");
        } else if ((cpu == null) != (mb == null)) {
            out.pending("CPU_MAINBOARD_PENDING", ComponentType.MAINBOARD,
                    "Cần cả CPU và Mainboard để kiểm tra socket.");
        }

        // UC29: RAM type, exact supported bus set, and total capacity.
        if (!rams.isEmpty() && mb == null) {
            out.pending("RAM_MAINBOARD_PENDING", ComponentType.RAM,
                    "Chưa có Mainboard để kiểm tra RAM.");
        }
        long totalRamGb = 0L;
        for (ConfigurationItem item : rams) {
            Ram ram = (Ram) item.component();
            totalRamGb = safeAdd(totalRamGb,
                    safeMultiply(ram.getCapacityGb(), item.quantity(),
                            ComponentType.RAM, out), ComponentType.RAM, out);
            if (mb != null) {
                if (!mb.getRamType().equals(ram.getRamType())) {
                    out.error("RAM_TYPE_MISMATCH", ComponentType.RAM,
                            "RAM và Mainboard khác chuẩn DDR.");
                }
                if (!mb.getSupportedRamBuses().contains(ram.getBusMhz())) {
                    out.error("RAM_BUS_UNSUPPORTED", ComponentType.RAM,
                            "Bus RAM không nằm trong tập bus Mainboard hỗ trợ.");
                }
            }
        }
        if (mb != null && totalRamGb > mb.getMaxRamCapacityGb()) {
            out.error("RAM_CAPACITY_EXCEEDED", ComponentType.RAM,
                    "Tổng RAM vượt dung lượng Mainboard hỗ trợ.");
        }

        // UC30: SATA/M.2 slots and 2.5/3.5-inch drive bays.
        long sata = 0, m2 = 0, bay25 = 0, bay35 = 0;
        for (ConfigurationItem item : drives) {
            Storage drive = (Storage) item.component();
            long qty = item.quantity();
            if (drive.getStorageInterface() == StorageInterface.SATA) {
                sata = safeAdd(sata, qty, ComponentType.STORAGE, out);
            } else {
                m2 = safeAdd(m2, qty, ComponentType.STORAGE, out);
            }
            if (drive.getFormFactor() == StorageFormFactor.INCH_2_5) {
                bay25 = safeAdd(bay25, qty, ComponentType.STORAGE, out);
            } else if (drive.getFormFactor() == StorageFormFactor.INCH_3_5) {
                bay35 = safeAdd(bay35, qty, ComponentType.STORAGE, out);
            }
        }
        if (!drives.isEmpty() && mb == null) {
            out.pending("STORAGE_MAINBOARD_PENDING", ComponentType.STORAGE,
                    "Chưa có Mainboard để xác minh cổng lưu trữ.");
        }
        if (mb != null) {
            if (sata > mb.getSataPortCount()) {
                out.error("SATA_PORTS_EXCEEDED", ComponentType.STORAGE,
                        "Số ổ SATA vượt số cổng SATA.");
            }
            if (m2 > mb.getM2SlotCount()) {
                out.error("M2_SLOTS_EXCEEDED", ComponentType.STORAGE,
                        "Số ổ M.2 NVMe vượt số khe M.2.");
            }
        }
        if ((bay25 > 0 || bay35 > 0) && pcCase == null) {
            out.pending("STORAGE_CASE_BAYS_PENDING", ComponentType.STORAGE,
                    "Chưa có Case để kiểm tra khay ổ 2.5/3.5 inch.");
        }
        if (pcCase != null) {
            if (bay25 > pcCase.getDriveBay25Count()) {
                out.error("CASE_BAY25_EXCEEDED", ComponentType.STORAGE,
                        "Số ổ 2.5 inch vượt số khay Case.");
            }
            if (bay35 > pcCase.getDriveBay35Count()) {
                out.error("CASE_BAY35_EXCEEDED", ComponentType.STORAGE,
                        "Số ổ 3.5 inch vượt số khay Case.");
            }
        }

        // UC31: physical Case compatibility with motherboard, PSU, GPU, cooler.
        if (mb != null && pcCase == null) {
            out.pending("MAINBOARD_CASE_PENDING", ComponentType.CASE,
                    "Chưa có Case để kiểm tra chuẩn Mainboard.");
        }
        if (mb != null && pcCase != null &&
                !pcCase.getSupportedMainboardForms().contains(mb.getFormFactor())) {
            out.error("CASE_MAINBOARD_FORM_MISMATCH", ComponentType.CASE,
                    "Case không hỗ trợ form factor Mainboard.");
        }
        if (psu != null && pcCase == null) {
            out.pending("PSU_CASE_PENDING", ComponentType.CASE,
                    "Chưa có Case để kiểm tra chuẩn PSU.");
        }
        if (psu != null && pcCase != null &&
                !pcCase.getSupportedPsuForms().contains(psu.getFormFactor())) {
            out.error("CASE_PSU_FORM_MISMATCH", ComponentType.CASE,
                    "Case không hỗ trợ form factor PSU.");
        }
        if (gpu != null && pcCase == null) {
            out.pending("GPU_CASE_PENDING", ComponentType.GPU,
                    "Chưa có Case để kiểm tra chiều dài GPU.");
        }
        if (gpu != null && pcCase != null &&
                gpu.getLengthMm() > pcCase.getMaxGpuLengthMm()) {
            out.error("GPU_TOO_LONG", ComponentType.GPU,
                    "GPU dài hơn giới hạn của Case.");
        }

        // UC32 and optional GPU in Hung's UC33: GPU's minimum PSU wattage.
        if (gpu != null && psu == null) {
            out.pending("GPU_PSU_PENDING", ComponentType.PSU,
                    "Chưa có PSU để kiểm tra công suất tối thiểu cho GPU.");
        }
        if (gpu != null && psu != null &&
                psu.getRealWattage() < gpu.getMinimumPsuWattage()) {
            out.error("PSU_WATTAGE_INSUFFICIENT", ComponentType.PSU,
                    "PSU không đáp ứng công suất tối thiểu GPU yêu cầu.");
        }

        // UC34: cooler socket, air height or liquid radiator fitting.
        if (cooler != null && cpu == null) {
            out.pending("COOLER_CPU_PENDING", ComponentType.CPU_COOLER,
                    "Chưa có CPU để kiểm tra socket tản nhiệt.");
        }
        if (cooler != null && cpu != null &&
                !cooler.getSupportedSockets().contains(cpu.getSocket())) {
            out.error("COOLER_SOCKET_MISMATCH", ComponentType.CPU_COOLER,
                    "Tản nhiệt không hỗ trợ socket CPU.");
        }
        if (cooler != null && pcCase == null) {
            out.pending("COOLER_CASE_PENDING", ComponentType.CPU_COOLER,
                    "Chưa có Case để kiểm tra tản nhiệt.");
        }
        if (cooler != null && pcCase != null) {
            if (cooler.getCoolingType() == CoolingType.AIR &&
                    cooler.getHeightMm() > pcCase.getMaxCpuCoolerHeightMm()) {
                out.error("COOLER_TOO_TALL", ComponentType.CPU_COOLER,
                        "Tản khí cao hơn giới hạn của Case.");
            }
            if (cooler.getCoolingType() == CoolingType.LIQUID &&
                    cooler.getRadiatorSize().getMillimeters() >
                    pcCase.getMaxRadiatorSize().getMillimeters()) {
                out.error("RADIATOR_UNSUPPORTED", ComponentType.CPU_COOLER,
                        "Case không hỗ trợ kích thước radiator.");
            }
        }

        // UC35: total fan units, including preinstalled fans; supported sizes.
        long extraFans = quantity(fans);
        if (extraFans > 0 && pcCase == null) {
            out.pending("CASE_FAN_CASE_PENDING", ComponentType.CASE_FAN,
                    "Chưa có Case để kiểm tra quạt.");
        }
        if (pcCase != null) {
            long allFans = safeAdd(extraFans,
                    pcCase.getIncludedCaseFanCount(), ComponentType.CASE_FAN, out);
            if (allFans > pcCase.getMaxCaseFanCount()) {
                out.error("CASE_FAN_COUNT_EXCEEDED", ComponentType.CASE_FAN,
                        "Tổng quạt rời và quạt đi kèm vượt số vị trí.");
            }
            for (ConfigurationItem item : fans) {
                CaseFan fan = (CaseFan) item.component();
                if (!pcCase.getSupportedFanSizes().contains(fan.getSizeMm())) {
                    out.error("CASE_FAN_SIZE_UNSUPPORTED", ComponentType.CASE_FAN,
                            "Case không hỗ trợ kích thước quạt đã chọn.");
                }
            }
        }

        // UC43: final completeness. Do not add stock availability here.
        if (finalCheck) {
            required(out, byType, ComponentType.CPU);
            required(out, byType, ComponentType.MAINBOARD);
            required(out, byType, ComponentType.RAM);
            required(out, byType, ComponentType.STORAGE);
            required(out, byType, ComponentType.CASE);
            required(out, byType, ComponentType.PSU);
            if (cpu != null && !cpu.hasIntegratedGraphics() && gpu == null) {
                out.error("GPU_REQUIRED", ComponentType.GPU,
                        "CPU không có GPU tích hợp: cần VGA rời.");
            }
            if (cpu != null && !cpu.includesStockCooler() && cooler == null) {
                out.error("CPU_COOLER_REQUIRED", ComponentType.CPU_COOLER,
                        "CPU không có tản kèm: cần tản CPU rời.");
            }
            if (pcCase != null && pcCase.getIncludedCaseFanCount() == 0 &&
                    extraFans == 0) {
                out.error("CASE_FAN_REQUIRED", ComponentType.CASE_FAN,
                        "Case không có quạt đi kèm: cần quạt Case.");
            }
            if (!out.pendingChecks.isEmpty()) {
                out.error("UNRESOLVED_DEPENDENCIES", null,
                        "Cấu hình cuối vẫn có điều kiện tương thích chưa kiểm được.");
            }
        }
        return new ValidationResult(out.errors, out.pendingChecks,
                out.affected, finalCheck);
    }

    private static void required(Findings out,
                                 Map<ComponentType, List<ConfigurationItem>> parts,
                                 ComponentType type) {
        if (parts.get(type).isEmpty()) {
            out.error("REQUIRED_" + type, type,
                    "Cấu hình cuối thiếu nhóm linh kiện " + type);
        }
    }

    private static long quantity(List<ConfigurationItem> items) {
        long total = 0;
        for (ConfigurationItem item : items) total += item.quantity();
        return total;
    }

    private static long safeMultiply(long a, long b, ComponentType type, Findings out) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            out.error("QUANTITY_OVERFLOW", type, "Số lượng vượt giới hạn.");
            return Long.MAX_VALUE;
        }
    }

    private static long safeAdd(long a, long b, ComponentType type, Findings out) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            out.error("QUANTITY_OVERFLOW", type, "Số lượng vượt giới hạn.");
            return Long.MAX_VALUE;
        }
    }

    private static <T extends Component> T first(
            Map<ComponentType, List<ConfigurationItem>> parts,
            ComponentType type, Class<T> clazz) {
        List<ConfigurationItem> items = parts.get(type);
        return items.isEmpty() ? null : clazz.cast(items.get(0).component());
    }

    private static final class Findings {
        private final List<ValidationIssue> errors = new ArrayList<>();
        private final List<ValidationIssue> pendingChecks = new ArrayList<>();
        private final Set<ComponentType> affected = new HashSet<>();
        private final Set<String> seenErrorCodes = new HashSet<>();
        private final Set<String> seenPendingCodes = new HashSet<>();

        void error(String code, ComponentType type, String message) {
            if (seenErrorCodes.add(code)) errors.add(new ValidationIssue(code, type, message));
            if (type != null) affected.add(type);
        }
        void pending(String code, ComponentType type, String message) {
            if (seenPendingCodes.add(code)) pendingChecks.add(
                    new ValidationIssue(code, type, message));
            if (type != null) affected.add(type);
        }
    }
}
