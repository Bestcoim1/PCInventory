package pcinventory.ui.builder;

import java.util.List;
import javax.swing.SwingUtilities;
import pcinventory.ui.builder.BuilderDemoData.Kind;
import pcinventory.ui.builder.BuilderDemoData.Operation;
import pcinventory.ui.builder.BuilderDemoData.Part;
import pcinventory.ui.builder.BuilderDemoData.Scenario;

/**
 * No database / no external libraries. Verifies deterministic demo states and
 * enough headless Swing construction for screenshot preparation.
 */
public final class BuilderUiFirstSelfTest {
    private static int passed;
    private static void check(boolean condition,String name){
        if(!condition)throw new AssertionError(name);
        passed++;
        System.out.println("PASS "+name);
    }
    private static boolean has(List<BuilderDemoData.Issue> issues,String code){
        return issues.stream().anyMatch(i->i.code().equals(code));
    }
    public static void main(String[] args)throws Exception{
        BuilderDemoData data=new BuilderDemoData();
        check(data.revision()==0,"new demo revision zero");
        check(data.entries().size()==9,"base demo includes nine lines");
        check(data.finalIssues().isEmpty(),"base final config valid");
        check(data.stockReport().status().equals("AVAILABLE"),
              "base demo stock available");
        long original=data.totalPrice();
        var originalEntries=data.entries();

        Part mbDdr4=BuilderDemoData.byCode("MB-LGA-DDR4");
        check(has(data.preview(mbDdr4,Operation.REPLACE,null,1),
                 "RAM_TYPE_MISMATCH"),"UC28 DDR5 to DDR4 Mainboard RAM mismatch");
        check(data.revision()==0 && data.totalPrice()==original,
              "preview does not mutate current context");
        data.commitMainboardAdjustment(mbDdr4,true);
        check(data.revision()==1,"UC28 accepted adjustment increments once");
        check(!has(data.draftIssues(),"RAM_TYPE_MISMATCH"),
              "UC28 adjusted DDR4 RAM no mismatch");
        check(originalEntries.stream().anyMatch(e->e.part().code()
              .equals("RAM-DDR5-5600")),"original immutable snapshot remains intact");

        data.reset(Scenario.UC29);
        check(has(data.preview(BuilderDemoData.byCode("RAM-DDR5-6000"),
                Operation.ADD,null,1),"RAM_BUS_UNSUPPORTED"),
              "UC29 unsupported RAM bus");

        data.reset(Scenario.UC30);
        check(has(data.preview(BuilderDemoData.byCode("SSD-SATA"),
                Operation.ADD,null,2),"SATA_PORTS_EXCEEDED"),
              "UC30 count SATA ports on ADD quantity 2");
        check(data.entriesOf(Kind.STORAGE).size()==1,
              "UC30 refused preview preserves Storage");
        var oldStorage=data.entriesOf(Kind.STORAGE).get(0);
        check(!has(data.preview(BuilderDemoData.byCode("SSD-NVME"),
                Operation.REPLACE,oldStorage.lineId(),1),"SATA_PORTS_EXCEEDED"),
              "UC30 replace removes target Storage from copy");
        check(has(data.preview(BuilderDemoData.byCode("SSD-SATA"),
                Operation.SET_QUANTITY,oldStorage.lineId(),3),"SATA_PORTS_EXCEEDED"),
              "UC30 SET_QUANTITY uses requested absolute quantity");

        data.reset(Scenario.UC31);
        check(has(data.preview(BuilderDemoData.byCode("CASE-COMPACT"),
                Operation.REPLACE,null,1),"GPU_TOO_LONG"),
              "UC31 compact Case rejects long GPU");
        data.reset(Scenario.UC32);
        check(has(data.preview(BuilderDemoData.byCode("PSU-500"),
                Operation.REPLACE,null,1),"PSU_WATTAGE_INSUFFICIENT"),
              "UC32 rejects PSU below GPU wattage");
        check(has(data.preview(BuilderDemoData.byCode("PSU-SFX"),
                Operation.REPLACE,null,1),"CASE_PSU_FORM_MISMATCH"),
              "UC32 rejects wrong PSU form factor");

        data.reset(Scenario.UC43);
        check(has(data.finalIssues(),"REQUIRED_CASE"),"UC43 missing Case invalid");
        data.reset(Scenario.UC44_SHORTAGE);
        check(data.finalIssues().isEmpty(),"UC44 shortage source passed final validation");
        var shortage=data.stockReport();
        check(shortage.status().equals("INSUFFICIENT"),"UC44 insufficient stock");
        check(shortage.lines().stream().anyMatch(x->x.code()
                .equals("RAM-DDR5-5600")&&x.required()==3
                &&x.available()==2&&x.shortage()==1),
                "UC44 displays required available shortage separately");
        data.reset(Scenario.UC44_UNKNOWN);
        check(data.finalIssues().isEmpty(),"UC44 unknown stock source is technically valid");
        check(data.stockReport().status().equals("UNVERIFIABLE"),
              "UC44 missing stock is unverified, never available");

        data.reset(Scenario.VALID);
        data.commit(BuilderDemoData.byCode("SSD-SATA"),Operation.ADD,null,1);
        check(data.stockReport().lines().stream()
             .anyMatch(x->x.code().equals("SSD-SATA")&&x.required()==2),
              "UC44 aggregates duplicate Storage component codes");

        // Verifies UI can be instantiated without database or a visible JFrame.
        SwingUtilities.invokeAndWait(()->{
            BuilderPanel panel=new BuilderPanel();
            check(panel.getComponentCount()>=3,
                  "BuilderPanel mounts into existing MainFrame as JPanel");
            check(panel.getBackground()!=null,"UI has explicit background");
        });
        System.out.println("ALL UI-FIRST TESTS PASSED: "+passed);
    }
}
