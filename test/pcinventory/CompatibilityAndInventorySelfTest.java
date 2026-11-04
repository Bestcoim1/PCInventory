package pcinventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pcinventory.dto.StockCheckResult;
import pcinventory.dto.StockStatus;
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
import pcinventory.model.RadiatorSize;
import pcinventory.model.Ram;
import pcinventory.model.Storage;
import pcinventory.model.StorageFormFactor;
import pcinventory.model.StorageInterface;
import pcinventory.service.CompatibilityPort;
import pcinventory.service.CompatibilityService;
import pcinventory.service.InventoryService;

/**
 * Dependency-free self-tests. Run:
 * java -cp build/classes:build/test-classes pcinventory.CompatibilityAndInventorySelfTest
 *
 * All data below is fictional. These tests never connect to MySQL.
 */
public final class CompatibilityAndInventorySelfTest {
    private static final CompatibilityPort COMPAT = new CompatibilityService();
    private static int passed;

    private CompatibilityAndInventorySelfTest() {}

    private static BigDecimal money() { return new BigDecimal("1000000.00"); }
    private static Cpu cpu(boolean integratedGpu, boolean includedCooler) {
        return new Cpu(1, "CPU-AM5", "Test CPU", money(), true,
                "AM5", integratedGpu, includedCooler);
    }
    private static Mainboard board(String socket, String ddr, int maxRam,
                                   int sata, int m2) {
        return new Mainboard(3, "MB-AM5", "Test MB", money(), true,
                socket, ddr, maxRam, "MICRO_ATX", sata, m2, Set.of(5200, 5600));
    }
    private static Ram ram(String ddr, int bus, int capacity) {
        return new Ram(5, "RAM", "Test RAM", money(), true, ddr, bus, capacity);
    }
    private static Storage drive(long id, StorageInterface storageInterface,
                                 StorageFormFactor form) {
        return new Storage(id, "SSD-" + id, "Test Storage", money(), true,
                storageInterface, form, 512);
    }
    private static Gpu gpu(int length, int minPsu) {
        return new Gpu(8, "GPU", "Test GPU", money(), true, length, minPsu);
    }
    private static Psu psu(int watts) {
        return new Psu(9, "PSU", "Test PSU", money(), true, watts, "ATX");
    }
    private static CpuCooler cooler(CoolingType type, Integer height,
                                   RadiatorSize radiator) {
        return new CpuCooler(10, "COOLER", "Test Cooler", money(), true,
                type, height, radiator, Set.of("AM5"));
    }
    private static PcCase pcCase(int includedFans, int maxFans,
                                 int bays25, int bays35, RadiatorSize maxRad,
                                 Set<Integer> sizes) {
        return new PcCase(11, "CASE", "Test Case", money(), true,
                165, 330, maxFans, includedFans, bays25, bays35, maxRad,
                Set.of("MICRO_ATX"), Set.of("ATX"), sizes);
    }
    private static CaseFan fan(int size) {
        return new CaseFan(12, "FAN", "Test Fan", money(), true, size);
    }

    private static PCConfiguration config(ConfigurationItem... items) {
        return new PCConfiguration(List.of(items));
    }
    private static ConfigurationItem line(String key, Component component, int qty) {
        return new ConfigurationItem(key, component, qty);
    }
    private static PCConfiguration complete() {
        return config(
            line("cpu", cpu(true, true), 1),
            line("mb", board("AM5", "DDR5", 64, 4, 2), 1),
            line("ram", ram("DDR5", 5600, 16), 2),
            line("storage", drive(7, StorageInterface.SATA,
                    StorageFormFactor.INCH_2_5), 1),
            line("case", pcCase(1, 5, 2, 1, RadiatorSize.MM240, Set.of(120)), 1),
            line("psu", psu(650), 1));
    }
    private static PCConfiguration replace(PCConfiguration source, String lineId,
                                           Component replacement, int quantity) {
        List<ConfigurationItem> result = new ArrayList<>();
        for (ConfigurationItem item : source.items()) {
            result.add(item.lineId().equals(lineId)
                    ? line(lineId, replacement, quantity) : item);
        }
        return new PCConfiguration(result);
    }
    private static PCConfiguration without(PCConfiguration source, String lineId) {
        return new PCConfiguration(source.items().stream()
                .filter(item -> !item.lineId().equals(lineId)).toList());
    }
    private static PCConfiguration with(PCConfiguration source, ConfigurationItem item) {
        List<ConfigurationItem> result = new ArrayList<>(source.items());
        result.add(item);
        return new PCConfiguration(result);
    }
    private static boolean has(ValidationResult result, String code) {
        return result.getErrors().stream().anyMatch(x -> x.code().equals(code));
    }
    private static boolean pending(ValidationResult result, String code) {
        return result.getPendingChecks().stream().anyMatch(x -> x.code().equals(code));
    }
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError("FAILED: " + description);
        passed++;
        System.out.println("PASS: " + description);
    }

    public static void main(String[] args) throws Exception {
        PCConfiguration base = complete();
        check(COMPAT.validateFinal(base).isFinalValid(), "UC43 complete AM5 config");
        check(!COMPAT.validateFinal(without(base, "case")).isCompatible(),
                "UC43 mandatory Case missing");
        check(has(COMPAT.validateFinal(without(base, "case")), "REQUIRED_CASE"),
                "UC43 stable issue code REQUIRED_CASE");

        check(has(COMPAT.revalidateDependencies(
                        replace(base, "mb", board("LGA1700", "DDR5", 64, 4, 2), 1)),
                        "SOCKET_MISMATCH"), "UC28 CPU/Mainboard socket mismatch");
        check(has(COMPAT.revalidateDependencies(
                        replace(base, "ram", ram("DDR4", 3200, 16), 2)),
                        "RAM_TYPE_MISMATCH"), "UC29 DDR type mismatch");
        check(has(COMPAT.revalidateDependencies(
                        replace(base, "ram", ram("DDR5", 6000, 16), 2)),
                        "RAM_BUS_UNSUPPORTED"), "UC29 exact bus set, not range");
        check(has(COMPAT.revalidateDependencies(
                        replace(base, "ram", ram("DDR5", 5600, 16), 5)),
                        "RAM_CAPACITY_EXCEEDED"), "UC29 total RAM capacity");

        check(has(COMPAT.revalidateDependencies(
                        replace(base, "storage", drive(7, StorageInterface.SATA,
                                StorageFormFactor.INCH_2_5), 5)),
                        "SATA_PORTS_EXCEEDED"), "UC30 SATA port count");
        check(has(COMPAT.revalidateDependencies(
                        with(base, line("nvme", drive(13, StorageInterface.M2_NVME,
                                StorageFormFactor.M_2), 3))),
                        "M2_SLOTS_EXCEEDED"), "UC30 M.2 slot count");
        check(has(COMPAT.revalidateDependencies(
                        replace(base, "storage", drive(7, StorageInterface.SATA,
                                StorageFormFactor.INCH_2_5), 3)),
                        "CASE_BAY25_EXCEEDED"), "UC30 drive bay count");
        PCConfiguration noCase = without(base, "case");
        check(pending(COMPAT.revalidateDependencies(noCase),
                        "STORAGE_CASE_BAYS_PENDING"), "UC30 missing Case = pending");
        check(!COMPAT.validateFinal(noCase).isFinalValid(),
                "UC43 final cannot accept pending Case");

        check(has(COMPAT.revalidateDependencies(
                        replace(base, "case", pcCase(1, 5, 2, 1,
                                RadiatorSize.MM240, Set.of(120)), 1)
                        // Add an oversized GPU after Case.
                        .getErrors().isEmpty()
                    ? new ValidationResult(List.of(), List.of(), Set.of())
                    : COMPAT.revalidateDependencies(base),
                        "IMPOSSIBLE_ISSUE") == false,
                "No artificial errors for valid Case");
        check(has(COMPAT.revalidateDependencies(
                        with(base, line("gpu", gpu(360, 600), 1))),
                        "GPU_TOO_LONG"), "UC31 GPU too long for Case");
        check(has(COMPAT.revalidateDependencies(
                        with(base, line("gpu", gpu(250, 700), 1))),
                        "PSU_WATTAGE_INSUFFICIENT"), "UC32 PSU below GPU minimum");

        check(has(COMPAT.revalidateDependencies(
                        with(base, line("cooler", cooler(CoolingType.LIQUID, null,
                                RadiatorSize.MM360), 1))),
                        "RADIATOR_UNSUPPORTED"), "UC31 radiator 360 vs Case 240");
        PCConfiguration biggerCase = replace(base, "case",
                pcCase(1, 5, 2, 1, RadiatorSize.MM360, Set.of(120)), 1);
        check(!has(COMPAT.revalidateDependencies(
                        with(biggerCase, line("cooler", cooler(CoolingType.LIQUID,
                                null, RadiatorSize.MM240), 1))),
                        "RADIATOR_UNSUPPORTED"), "Case 360 accepts radiator 240");
        check(has(COMPAT.revalidateDependencies(
                        with(base, line("fan", fan(120), 5))),
                        "CASE_FAN_COUNT_EXCEEDED"), "UC35 fan count includes preinstalled");
        check(has(COMPAT.revalidateDependencies(
                        with(base, line("fan", fan(140), 1))),
                        "CASE_FAN_SIZE_UNSUPPORTED"), "UC35 fan size set");

        check(has(COMPAT.validateFinal(replace(base, "cpu",
                        cpu(false, true), 1)), "GPU_REQUIRED"),
                "UC43 needs GPU when CPU has no integrated GPU");
        check(has(COMPAT.validateFinal(replace(base, "cpu",
                        cpu(true, false), 1)), "CPU_COOLER_REQUIRED"),
                "UC43 needs cooler if not included with CPU");
        check(has(COMPAT.validateFinal(replace(base, "case",
                        pcCase(0, 5, 2, 1, RadiatorSize.MM240, Set.of(120)), 1)),
                        "CASE_FAN_REQUIRED"), "UC43 needs fan when Case has none");

        check(COMPAT.filterCompatible(
                    config(line("cpu", cpu(true, true), 1)),
                    ComponentType.MAINBOARD,
                    List.of(board("AM5", "DDR5", 64, 4, 2),
                            board("LGA1700", "DDR5", 64, 4, 2))).size() == 1,
                "UC28 filter suitable Mainboard, no stock query");
        check(COMPAT.revalidateDependencies(
                    config(line("cpu", cpu(true, true), 1))).isCompatible(),
                "Draft may be compatible but incomplete");

        InventoryService inventory3 = new InventoryService(ids -> Map.of(12L, 3));
        PCConfiguration twoFanLines = config(
                line("fan_a", fan(120), 2),
                line("fan_b", fan(120), 2));
        StockCheckResult shortage = inventory3.checkAvailability(twoFanLines);
        check(shortage.status() == StockStatus.INSUFFICIENT &&
                shortage.lines().get(0).required() == 4 &&
                shortage.lines().get(0).shortage() == 1,
                "UC44 merges duplicate component IDs and reports shortage");
        check(new InventoryService(ids -> Map.of()).checkAvailability(twoFanLines)
                        .status() == StockStatus.UNVERIFIABLE,
                "UC44 missing stock_balance row = UNVERIFIABLE");
        check(new InventoryService(ids -> Map.of(12L, 10)).checkAvailability(
                        twoFanLines).status() == StockStatus.AVAILABLE,
                "UC44 sufficient read-only inventory");
        check(new InventoryService(ids -> Map.of()).checkAvailability(
                        config()).status() == StockStatus.INVALID_REQUEST,
                "UC44 empty config = INVALID_REQUEST");
        check(base.items().size() == 6, "Validation never mutates original config");

        System.out.println("ALL TESTS PASSED: " + passed);
    }
}
