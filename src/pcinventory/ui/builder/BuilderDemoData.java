package pcinventory.ui.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * UI-FIRST DOCUMENTATION DEMO. This class is NOT a persistence or domain service.
 *
 * A fixed illustrative catalog lets the team take screenshots before the
 * full Builder / ComponentDAO integration is ready. Real UC implementation
 * must call BuildService, CompatibilityPort and StockDAO instead.
 */
public final class BuilderDemoData {
    public enum Kind {
        CPU("CPU"), MAINBOARD("Mainboard"), RAM("RAM"),
        STORAGE("Ổ lưu trữ"), GPU("Card đồ họa"), CASE("Vỏ máy"),
        PSU("Nguồn PSU"), COOLER("Tản CPU"), FAN("Quạt Case");

        public final String title;
        Kind(String title) { this.title = title; }
        @Override public String toString() { return title; }
    }

    public enum Scenario {
        UC28("UC28 · Thay Mainboard / xử lý RAM", Kind.MAINBOARD, "MB-LGA-DDR4"),
        UC29("UC29 · RAM sai bus hỗ trợ", Kind.RAM, "RAM-DDR5-6000"),
        UC30("UC30 · Storage / số lượng và khe cắm", Kind.STORAGE, "SSD-SATA"),
        UC31("UC31 · Case không vừa GPU", Kind.CASE, "CASE-COMPACT"),
        UC32("UC32 · PSU không đủ công suất", Kind.PSU, "PSU-500"),
        UC43("UC43 · Kiểm tra cuối: thiếu Case", Kind.CASE, "CASE-ATX"),
        UC44_SHORTAGE("UC44 · Tồn kho thiếu RAM", Kind.RAM, "RAM-DDR5-5600"),
        UC44_UNKNOWN("UC44 · Không xác minh được kho", Kind.STORAGE, "SSD-UNKNOWN"),
        VALID("Cấu hình mẫu hợp lệ", Kind.MAINBOARD, "MB-LGA-DDR5");

        public final String title;
        public final Kind focus;
        public final String highlightedCode;
        Scenario(String title, Kind focus, String highlightedCode) {
            this.title = title;
            this.focus = focus;
            this.highlightedCode = highlightedCode;
        }
        @Override public String toString() { return title; }
    }

    public enum Operation {
        ADD("Thêm"), REPLACE("Thay"), SET_QUANTITY("Sửa số lượng");
        public final String label;
        Operation(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public record Part(String code, Kind kind, String name, String brand,
                       long price, int stock, Map<String, String> spec) {
        public Part {
            Objects.requireNonNull(code);
            Objects.requireNonNull(kind);
            Objects.requireNonNull(name);
            Objects.requireNonNull(brand);
            if (price < 0) throw new IllegalArgumentException("Negative price");
            spec = Map.copyOf(spec);
        }
        public String spec(String key) { return spec.getOrDefault(key, ""); }
        public int number(String key) {
            try { return Integer.parseInt(spec(key)); }
            catch (NumberFormatException ex) { return 0; }
        }
        public boolean flag(String key) { return "true".equals(spec(key)); }
        public String info() { return spec("summary"); }
    }

    public record Entry(String lineId, Part part, int quantity) {
        public Entry {
            Objects.requireNonNull(lineId);
            Objects.requireNonNull(part);
            if (quantity <= 0) throw new IllegalArgumentException("quantity > 0");
        }
        @Override public String toString() {
            return part.name() + "  (SL " + quantity + ")";
        }
    }

    public record Issue(String code, String description, boolean pending) {
        @Override public String toString() { return description; }
    }

    public record StockLine(String code, String name, int required,
                            Integer available, Integer shortage) {}

    public record StockReport(String status, List<StockLine> lines, String note) {
        public StockReport { lines = List.copyOf(lines); }
    }

    private static final List<Part> CATALOG = createCatalog();
    private final LinkedHashMap<String, Entry> selected = new LinkedHashMap<>();
    private long revision;
    private int nextId = 1;
    private Scenario scenario = Scenario.VALID;

    public BuilderDemoData() { reset(Scenario.VALID); }

    private static Map<String,String> spec(String... values) {
        if (values.length % 2 != 0) throw new IllegalArgumentException();
        Map<String,String> m = new LinkedHashMap<>();
        for (int i=0;i<values.length;i+=2) m.put(values[i],values[i+1]);
        return m;
    }
    private static Part part(String code, Kind kind, String name, String brand,
                             long price, int stock, String... specs) {
        return new Part(code, kind, name, brand, price, stock, spec(specs));
    }

    private static List<Part> createCatalog() {
        return List.of(
            part("CPU-LGA",Kind.CPU,"Intel Core i5 (demo)","Intel",3400000,8,
                "socket","LGA1700","igpu","true","stockCooler","true",
                "summary","Socket LGA1700 · có iGPU và tản kèm"),
            part("CPU-AM5",Kind.CPU,"AMD Ryzen (demo)","AMD",3600000,7,
                "socket","AM5","igpu","true","stockCooler","false",
                "summary","Socket AM5 · có iGPU · không tản kèm"),
            part("MB-LGA-DDR5",Kind.MAINBOARD,"Mainboard LGA1700 DDR5","DemoTech",2600000,5,
                "socket","LGA1700","ramType","DDR5","ramBus","5200,5600",
                "ramMax","64","sata","2","m2","2","form","MICRO_ATX",
                "summary","LGA1700 · DDR5 5200/5600 · Micro-ATX · 2 SATA / 2 M.2"),
            part("MB-LGA-DDR4",Kind.MAINBOARD,"Mainboard LGA1700 DDR4","DemoTech",2250000,5,
                "socket","LGA1700","ramType","DDR4","ramBus","2933,3200",
                "ramMax","64","sata","2","m2","2","form","MICRO_ATX",
                "summary","LGA1700 · DDR4 2933/3200 · Micro-ATX"),
            part("MB-AM5",Kind.MAINBOARD,"Mainboard AM5 DDR5","DemoTech",2850000,4,
                "socket","AM5","ramType","DDR5","ramBus","5200,5600",
                "ramMax","128","sata","4","m2","2","form","ATX",
                "summary","AM5 · DDR5 · ATX · 4 SATA / 2 M.2"),
            part("RAM-DDR5-5600",Kind.RAM,"RAM DDR5 16GB 5600","DemoRAM",1090000,2,
                "ramType","DDR5","bus","5600","capacity","16",
                "summary","DDR5 · 5600 MHz · 16 GB / thanh"),
            part("RAM-DDR5-6000",Kind.RAM,"RAM DDR5 16GB 6000","DemoRAM",1290000,8,
                "ramType","DDR5","bus","6000","capacity","16",
                "summary","DDR5 · 6000 MHz · 16 GB / thanh"),
            part("RAM-DDR4-3200",Kind.RAM,"RAM DDR4 16GB 3200","DemoRAM",790000,8,
                "ramType","DDR4","bus","3200","capacity","16",
                "summary","DDR4 · 3200 MHz · 16 GB / thanh"),
            part("SSD-SATA",Kind.STORAGE,"SSD SATA 2.5 inch 512 GB","DemoSSD",950000,8,
                "interface","SATA","form","2_5",
                "summary","SSD 2.5 inch · SATA · 512 GB"),
            part("HDD-SATA",Kind.STORAGE,"HDD SATA 3.5 inch 1 TB","DemoHDD",1100000,6,
                "interface","SATA","form","3_5",
                "summary","HDD 3.5 inch · SATA · 1 TB"),
            part("SSD-NVME",Kind.STORAGE,"SSD M.2 NVMe 1 TB","DemoSSD",1600000,5,
                "interface","M2_NVME","form","M_2",
                "summary","SSD NVMe · M.2 · 1 TB"),
            part("SSD-UNKNOWN",Kind.STORAGE,"SSD SATA chưa xác minh tồn","DemoSSD",850000,-1,
                "interface","SATA","form","2_5",
                "summary","SSD SATA 2.5 inch · bản ghi tồn chưa xác minh"),
            part("GPU-280",Kind.GPU,"GPU rời 280 mm (demo)","DemoGPU",5500000,4,
                "length","280","minPsu","650","summary","Dài 280 mm · PSU tối thiểu 650 W"),
            part("GPU-215",Kind.GPU,"GPU rời 215 mm (demo)","DemoGPU",3400000,5,
                "length","215","minPsu","500","summary","Dài 215 mm · PSU tối thiểu 500 W"),
            part("CASE-ATX",Kind.CASE,"Case Mid Tower ATX","DemoCase",930000,6,
                "mainboards","ATX,MICRO_ATX","psus","ATX","maxGpu","340",
                "maxCooler","170","maxRad","360","bay25","3","bay35","2",
                "fanCount","6","fanIncluded","1","fanSizes","120,140",
                "summary","ATX/Micro-ATX · GPU ≤ 340 mm · radiator ≤ 360"),
            part("CASE-COMPACT",Kind.CASE,"Case Mini Compact","DemoCase",820000,7,
                "mainboards","MICRO_ATX","psus","ATX","maxGpu","240",
                "maxCooler","150","maxRad","240","bay25","1","bay35","0",
                "fanCount","3","fanIncluded","1","fanSizes","120",
                "summary","Micro-ATX · GPU ≤ 240 mm · radiator ≤ 240"),
            part("PSU-750",Kind.PSU,"PSU ATX 750 W","DemoPower",1450000,8,
                "watts","750","form","ATX","summary","750 W thực · chuẩn ATX"),
            part("PSU-500",Kind.PSU,"PSU ATX 500 W","DemoPower",850000,8,
                "watts","500","form","ATX","summary","500 W thực · chuẩn ATX"),
            part("PSU-SFX",Kind.PSU,"PSU SFX 650 W","DemoPower",1550000,6,
                "watts","650","form","SFX","summary","650 W thực · chuẩn SFX"),
            part("COOLER-AIR",Kind.COOLER,"Tản khí 155 mm","DemoCool",560000,5,
                "type","AIR","height","155","socket","LGA1700,AM5",
                "summary","Tản khí 155 mm · socket LGA1700/AM5"),
            part("COOLER-360",Kind.COOLER,"Tản nước 360 mm","DemoCool",1800000,4,
                "type","LIQUID","radiator","360","socket","LGA1700,AM5",
                "summary","Tản nước · radiator 360 mm"),
            part("FAN-120",Kind.FAN,"Quạt Case 120 mm","DemoFan",130000,10,
                "size","120","summary","Quạt 120 mm"),
            part("FAN-140",Kind.FAN,"Quạt Case 140 mm","DemoFan",160000,10,
                "size","140","summary","Quạt 140 mm")
        );
    }

    public static List<Part> catalog() { return CATALOG; }
    public static Part byCode(String code) {
        for (Part p : CATALOG) if (p.code().equals(code)) return p;
        throw new IllegalArgumentException("Unknown demo item " + code);
    }
    public long revision() { return revision; }
    public Scenario scenario() { return scenario; }
    public List<Entry> entries() { return List.copyOf(selected.values()); }
    public boolean has(Kind kind) {
        for (Entry e: selected.values()) if (e.part().kind()==kind) return true;
        return false;
    }
    public List<Entry> entriesOf(Kind kind) {
        List<Entry> r = new ArrayList<>();
        for (Entry e: selected.values()) if(e.part().kind()==kind) r.add(e);
        return r;
    }
    public long totalPrice() {
        long total=0;
        for (Entry e: selected.values()) total=Math.addExact(total,
                Math.multiplyExact(e.part().price(),(long)e.quantity()));
        return total;
    }
    public void reset(Scenario nextScenario) {
        scenario = Objects.requireNonNull(nextScenario);
        selected.clear();
        nextId=1;
        revision=0L;
        addBase("CPU-LGA",1);
        addBase("MB-LGA-DDR5",1);
        addBase("RAM-DDR5-5600",2);
        addBase("SSD-SATA",1);
        addBase("GPU-280",1);
        addBase("CASE-ATX",1);
        addBase("PSU-750",1);
        addBase("COOLER-AIR",1);
        addBase("FAN-120",1);
        if (scenario==Scenario.UC43) {
            selected.values().removeIf(e -> e.part().kind()==Kind.CASE);
        } else if (scenario==Scenario.UC44_SHORTAGE) {
            changeQuantity("RAM-DDR5-5600",3);
        } else if (scenario==Scenario.UC44_UNKNOWN) {
            addBase("SSD-UNKNOWN",1);
        }
    }

    private void addBase(String code, int quantity) {
        Part p=byCode(code);
        String id="L"+(nextId++);
        selected.put(id,new Entry(id,p,quantity));
    }
    private void changeQuantity(String code, int qty) {
        for (Entry e: new ArrayList<>(selected.values())) {
            if (e.part().code().equals(code)) {
                selected.put(e.lineId(),new Entry(e.lineId(),e.part(),qty));
                return;
            }
        }
    }

    /** Pure simulation: returns issues for a copied selection, never commits. */
    public List<Issue> preview(Part part, Operation operation,
                               String targetLineId, int quantity) {
        try {
            return validate(project(part,operation,targetLineId,quantity),false);
        } catch (IllegalArgumentException ex) {
            return List.of(new Issue("INPUT_ERROR",ex.getMessage(),false));
        }
    }

    /** Returns a copied map for a prospective operation. */
    private LinkedHashMap<String,Entry> project(Part p,Operation op,
                                                String lineId,int quantity) {
        Objects.requireNonNull(p,"part");
        Objects.requireNonNull(op,"operation");
        if(quantity<1)throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
        LinkedHashMap<String,Entry> draft=new LinkedHashMap<>(selected);
        if (p.kind()!=Kind.RAM && p.kind()!=Kind.STORAGE && p.kind()!=Kind.FAN &&
                quantity!=1)throw new IllegalArgumentException("Nhóm này chỉ được chọn số lượng 1.");

        if(op==Operation.SET_QUANTITY) {
            Entry target=draft.get(lineId);
            if(target==null || target.part().kind()!=p.kind() ||
                    !target.part().code().equals(p.code())) {
                throw new IllegalArgumentException("Dòng cần sửa số lượng không hợp lệ.");
            }
            draft.put(lineId,new Entry(lineId,p,quantity));
        } else if(op==Operation.REPLACE) {
            if(p.kind()==Kind.STORAGE || p.kind()==Kind.RAM || p.kind()==Kind.FAN) {
                Entry target=draft.get(lineId);
                if(target==null || target.part().kind()!=p.kind()) {
                    throw new IllegalArgumentException("Cần chọn đúng dòng linh kiện muốn thay.");
                }
                draft.put(lineId,new Entry(lineId,p,quantity));
            } else {
                draft.values().removeIf(e -> e.part().kind()==p.kind());
                draft.put("PREVIEW-"+p.kind(),new Entry("PREVIEW-"+p.kind(),p,1));
            }
        } else {
            if(p.kind()!=Kind.RAM && p.kind()!=Kind.STORAGE && p.kind()!=Kind.FAN) {
                draft.values().removeIf(e -> e.part().kind()==p.kind());
            }
            draft.put("PREVIEW-"+nextId,new Entry("PREVIEW-"+nextId,p,quantity));
        }
        return draft;
    }

    /** Demo commit after explicit confirmation; rejects any hard technical issue. */
    public void commit(Part part,Operation operation,String lineId,int quantity) {
        LinkedHashMap<String,Entry> draft=project(part,operation,lineId,quantity);
        if(validate(draft,false).stream().anyMatch(i->!i.pending())) {
            throw new IllegalStateException("Có xung đột kỹ thuật chưa giải quyết.");
        }
        selected.clear();
        for(Entry entry:draft.values()) {
            String id=entry.lineId().startsWith("PREVIEW-")?"L"+(nextId++):entry.lineId();
            selected.put(id,new Entry(id,entry.part(),entry.quantity()));
        }
        revision++;
    }

    /** Explicit user-approved Mainboard adjustment; no silent RAM deletion. */
    public void commitMainboardAdjustment(Part mainboard, boolean replaceRam) {
        if(mainboard.kind()!=Kind.MAINBOARD)throw new IllegalArgumentException();
        LinkedHashMap<String,Entry> draft=project(mainboard,Operation.REPLACE,null,1);
        draft.values().removeIf(e->e.part().kind()==Kind.RAM);
        if (replaceRam) {
            Part match=null;
            for(Part candidate:CATALOG) {
                if(candidate.kind()==Kind.RAM &&
                    candidate.spec("ramType").equals(mainboard.spec("ramType")) &&
                    csv(mainboard.spec("ramBus")).contains(candidate.spec("bus"))) {
                    match=candidate; break;
                }
            }
            if(match==null) throw new IllegalStateException("Không tìm được RAM thay thế trong bộ demo.");
            draft.put("PREVIEW-RAM",new Entry("PREVIEW-RAM",match,2));
        }
        if(validate(draft,false).stream().anyMatch(i->!i.pending())) {
            throw new IllegalStateException("Phương án còn xung đột.");
        }
        selected.clear();
        for(Entry e:draft.values()){
            String id=e.lineId().startsWith("PREVIEW-")?"L"+(nextId++):e.lineId();
            selected.put(id,new Entry(id,e.part(),e.quantity()));
        }
        revision++;
    }

    public List<Issue> draftIssues() { return validate(selected,false); }
    public List<Issue> finalIssues() { return validate(selected,true); }

    private static Set<String> csv(String raw) {
        if(raw==null||raw.isBlank())return Set.of();
        Set<String> result=new LinkedHashSet<>();
        for(String x:raw.split(","))result.add(x.trim());
        return result;
    }
    private static Entry one(Map<String,Entry> entries, Kind kind) {
        for (Entry e: entries.values()) if(e.part().kind()==kind)return e;
        return null;
    }
    private static int sumQuantity(Map<String,Entry> entries, Kind kind) {
        int total=0;
        for(Entry e:entries.values()) if(e.part().kind()==kind) total=Math.addExact(total,e.quantity());
        return total;
    }
    private static List<Issue> validate(Map<String,Entry> entries,boolean finalCheck) {
        List<Issue> result=new ArrayList<>();
        Set<String> codes=new LinkedHashSet<>();
        Entry cpu=one(entries,Kind.CPU),mb=one(entries,Kind.MAINBOARD),
                ram=one(entries,Kind.RAM),storage=one(entries,Kind.STORAGE),
                gpu=one(entries,Kind.GPU),pcCase=one(entries,Kind.CASE),
                psu=one(entries,Kind.PSU),cooler=one(entries,Kind.COOLER);
        if(cpu!=null&&mb!=null&&!cpu.part().spec("socket").equals(mb.part().spec("socket"))) {
            issue(result,codes,"SOCKET_MISMATCH","Socket CPU và Mainboard không trùng.",false);
        } else if((cpu==null)!=(mb==null)) {
            issue(result,codes,"CPU_MAINBOARD_PENDING","Chưa đủ CPU/Mainboard để kiểm tra socket.",true);
        }
        int ramGb=0,sata=0,m2=0,bay25=0,bay35=0;
        for(Entry e:entries.values()) {
            Part p=e.part();
            if(p.kind()==Kind.RAM) {
                ramGb=Math.addExact(ramGb,Math.multiplyExact(p.number("capacity"),e.quantity()));
                if(mb!=null) {
                    if(!p.spec("ramType").equals(mb.part().spec("ramType")))
                        issue(result,codes,"RAM_TYPE_MISMATCH","RAM khác chuẩn DDR của Mainboard.",false);
                    if(!csv(mb.part().spec("ramBus")).contains(p.spec("bus")))
                        issue(result,codes,"RAM_BUS_UNSUPPORTED","Bus RAM không thuộc tập hỗ trợ.",false);
                }
            }
            if(p.kind()==Kind.STORAGE) {
                if("SATA".equals(p.spec("interface")))sata+=e.quantity();
                else if("M2_NVME".equals(p.spec("interface")))m2+=e.quantity();
                if("2_5".equals(p.spec("form")))bay25+=e.quantity();
                if("3_5".equals(p.spec("form")))bay35+=e.quantity();
            }
        }
        if(ram!=null&&mb==null)
            issue(result,codes,"RAM_MAINBOARD_PENDING","Chưa có Mainboard để kiểm tra RAM.",true);
        if(mb!=null&&ramGb>mb.part().number("ramMax"))
            issue(result,codes,"RAM_CAPACITY_EXCEEDED","Tổng RAM vượt dung lượng tối đa.",false);
        if(storage!=null&&mb==null)
            issue(result,codes,"STORAGE_MAINBOARD_PENDING","Chưa có Mainboard để kiểm tra cổng lưu trữ.",true);
        if(mb!=null) {
            if(sata>mb.part().number("sata"))issue(result,codes,"SATA_PORTS_EXCEEDED","Số ổ SATA vượt số cổng Mainboard.",false);
            if(m2>mb.part().number("m2"))issue(result,codes,"M2_SLOTS_EXCEEDED","Số ổ M.2 vượt số khe Mainboard.",false);
        }
        if(storage!=null&&pcCase==null&&(bay25>0||bay35>0))
            issue(result,codes,"STORAGE_CASE_PENDING","Chưa có Case để kiểm tra khay ổ.",true);
        if(pcCase!=null) {
            Part c=pcCase.part();
            if(bay25>c.number("bay25"))issue(result,codes,"CASE_BAY25_EXCEEDED","Số ổ 2.5 inch vượt khay Case.",false);
            if(bay35>c.number("bay35"))issue(result,codes,"CASE_BAY35_EXCEEDED","Số ổ 3.5 inch vượt khay Case.",false);
            if(mb!=null&&!csv(c.spec("mainboards")).contains(mb.part().spec("form")))
                issue(result,codes,"CASE_MAINBOARD_FORM_MISMATCH","Case không hỗ trợ form factor Mainboard.",false);
            if(psu!=null&&!csv(c.spec("psus")).contains(psu.part().spec("form")))
                issue(result,codes,"CASE_PSU_FORM_MISMATCH","Case không hỗ trợ chuẩn PSU.",false);
            if(gpu!=null&&gpu.part().number("length")>c.number("maxGpu"))
                issue(result,codes,"GPU_TOO_LONG","GPU dài hơn khoảng trống của Case.",false);
            if(cooler!=null) {
                if("AIR".equals(cooler.part().spec("type"))&&
                    cooler.part().number("height")>c.number("maxCooler"))
                    issue(result,codes,"COOLER_TOO_TALL","Tản khí cao hơn giới hạn Case.",false);
                if("LIQUID".equals(cooler.part().spec("type"))&&
                    cooler.part().number("radiator")>c.number("maxRad"))
                    issue(result,codes,"RADIATOR_UNSUPPORTED","Case không hỗ trợ radiator.",false);
            }
            int fans=c.number("fanIncluded")+sumQuantity(entries,Kind.FAN);
            if(fans>c.number("fanCount"))
                issue(result,codes,"CASE_FAN_COUNT_EXCEEDED","Số quạt vượt vị trí lắp của Case.",false);
            for(Entry e:entries.values())if(e.part().kind()==Kind.FAN &&
                !csv(c.spec("fanSizes")).contains(e.part().spec("size")))
                issue(result,codes,"CASE_FAN_SIZE_UNSUPPORTED","Kích thước quạt không được Case hỗ trợ.",false);
        } else {
            if(mb!=null||psu!=null||gpu!=null||cooler!=null||storage!=null)
                issue(result,codes,"CASE_PENDING","Chưa có Case để kiểm tra kích thước và bố trí.",true);
        }
        if(gpu!=null&&psu!=null&&psu.part().number("watts")<gpu.part().number("minPsu"))
            issue(result,codes,"PSU_WATTAGE_INSUFFICIENT","Công suất PSU thấp hơn yêu cầu GPU.",false);
        else if(gpu!=null&&psu==null)
            issue(result,codes,"GPU_PSU_PENDING","Chưa có PSU để đối chiếu công suất.",true);
        if(cooler!=null&&cpu!=null&&!csv(cooler.part().spec("socket")).contains(cpu.part().spec("socket")))
            issue(result,codes,"COOLER_SOCKET_MISMATCH","Tản CPU không hỗ trợ socket.",false);
        if(finalCheck) {
            for(Kind kind:List.of(Kind.CPU,Kind.MAINBOARD,Kind.RAM,Kind.STORAGE,Kind.CASE,Kind.PSU)) {
                if(one(entries,kind)==null)
                    issue(result,codes,"REQUIRED_"+kind.name(),"Thiếu linh kiện bắt buộc: "+kind.title,false);
            }
            if(cpu!=null&&!cpu.part().flag("igpu")&&gpu==null)
                issue(result,codes,"GPU_REQUIRED","CPU không có GPU tích hợp: cần VGA rời.",false);
            if(cpu!=null&&!cpu.part().flag("stockCooler")&&cooler==null)
                issue(result,codes,"CPU_COOLER_REQUIRED","CPU không có tản kèm: cần tản rời.",false);
            if(pcCase!=null&&pcCase.part().number("fanIncluded")==0&&sumQuantity(entries,Kind.FAN)==0)
                issue(result,codes,"CASE_FAN_REQUIRED","Case cần ít nhất một quạt.",false);
            if(result.stream().anyMatch(Issue::pending))
                issue(result,codes,"UNRESOLVED_DEPENDENCIES","Chưa đủ dữ liệu xác minh để hoàn tất.",false);
        }
        return List.copyOf(result);
    }
    private static void issue(List<Issue> result,Set<String> codes,
                              String code,String message,boolean pending) {
        if(codes.add(code))result.add(new Issue(code,message,pending));
    }

    /** Only a simulated stock report. No DB reads and no reservations. */
    public StockReport stockReport() {
        Map<String,Integer> demand=new LinkedHashMap<>();
        Map<String,Part> parts=new LinkedHashMap<>();
        for(Entry e:selected.values()) {
            demand.merge(e.part().code(),e.quantity(),Math::addExact);
            parts.put(e.part().code(),e.part());
        }
        List<StockLine> lines=new ArrayList<>();
        boolean unknown=false,shortage=false;
        for(Map.Entry<String,Integer> e:demand.entrySet()) {
            Part p=parts.get(e.getKey());
            if(p.stock()<0){
                unknown=true;
                lines.add(new StockLine(p.code(),p.name(),e.getValue(),null,null));
            } else {
                int missing=Math.max(0,e.getValue()-p.stock());
                shortage |= missing>0;
                lines.add(new StockLine(p.code(),p.name(),e.getValue(),p.stock(),missing));
            }
        }
        String status=unknown?"UNVERIFIABLE":shortage?"INSUFFICIENT":"AVAILABLE";
        return new StockReport(status,lines,
                "Chỉ đối chiếu tồn kho mô phỏng; không giữ chỗ, không trừ kho.");
    }

    public List<Part> candidates(Kind kind,String query,boolean inStockOnly) {
        String q=query==null?"":query.toLowerCase(Locale.ROOT).trim();
        List<Part> result=new ArrayList<>();
        for(Part p:CATALOG) {
            if(p.kind()!=kind)continue;
            if(inStockOnly && p.stock()<=0)continue;
            String haystack=(p.code()+" "+p.name()+" "+p.brand()+" "+p.info())
                    .toLowerCase(Locale.ROOT);
            if(!haystack.contains(q))continue;
            result.add(p);
        }
        return result;
    }
    public int selectedQuantity(String code) {
        int total=0;
        for(Entry e:selected.values())if(e.part().code().equals(code))total+=e.quantity();
        return total;
    }
}
