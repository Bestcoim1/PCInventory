package pcinventory.ui.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import pcinventory.ui.builder.BuilderDemoData.Entry;
import pcinventory.ui.builder.BuilderDemoData.Issue;
import pcinventory.ui.builder.BuilderDemoData.Kind;
import pcinventory.ui.builder.BuilderDemoData.Operation;
import pcinventory.ui.builder.BuilderDemoData.Part;
import pcinventory.ui.builder.BuilderDemoData.Scenario;

/**
 * Complete, self-contained Swing UI prototype for Truong's UC28–32/43/44.
 *
 * IMPORTANT: UI-FIRST / DOCUMENTATION DEMO. Neither MySQL nor BuildService
 * is called here. Replace BuilderDemoData with a Controller adapter after
 * Hung's BuildService and Truong's services have been integrated.
 *
 * Compatible with existing MainFrame.registerPage(AppPage.BUILDER, new BuilderPanel()).
 * Does not change the team's navigation shell or other member modules.
 */
public final class BuilderPanel extends JPanel {
    private final BuilderDemoData demo=new BuilderDemoData();
    private final Map<Kind,JButton> menuButtons=new EnumMap<>(Kind.class);
    private final List<Part> visibleCandidates=new ArrayList<>();

    private final DefaultTableModel candidateModel=new DefaultTableModel(
            new Object[]{"Mã","Linh kiện","Thông số","Đơn giá","Tồn","Đánh giá"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final DefaultTableModel selectedModel=new DefaultTableModel(
            new Object[]{"Nhóm","Linh kiện","SL","Thành tiền"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };

    private Kind currentKind=Kind.MAINBOARD;
    private final JTable candidateTable=new JTable(candidateModel);
    private final JTable selectedTable=new JTable(selectedModel);
    private final JComboBox<Scenario> scenarioBox=new JComboBox<>(Scenario.values());
    private final JTextField searchBox=new JTextField();
    private final JCheckBox inStockOnly=new JCheckBox("Chỉ hiện hàng còn tồn");
    private final JComboBox<Operation> operationBox=new JComboBox<>(Operation.values());
    private final JComboBox<Entry> targetBox=new JComboBox<>();
    private final JSpinner quantityBox=new JSpinner(new SpinnerNumberModel(1,1,20,1));
    private final JLabel categoryTitle=BuilderUiStyles.label("",17,true,BuilderUiStyles.INK);
    private final JLabel categoryNote=BuilderUiStyles.label("",11,false,BuilderUiStyles.MUTED);
    private final JTextArea partDetail=new JTextArea();
    private final JTextArea technicalStatus=new JTextArea();
    private final JLabel revisionLabel=BuilderUiStyles.label("",11,true,BuilderUiStyles.MUTED);
    private final JLabel totalLabel=BuilderUiStyles.label("",18,true,BuilderUiStyles.BLUE);
    private final JLabel bottomStatus=BuilderUiStyles.label(
            "UI FIRST · DỮ LIỆU MINH HỌA — chưa đồng bộ MySQL",11,false,
            BuilderUiStyles.MUTED);
    private final JButton stockButton=BuilderUiStyles.button("UC44 · Kiểm tra tồn",false);
    private final JButton confirmButton=BuilderUiStyles.button("Chọn / Thay linh kiện",true);
    private final JPanel categoryRail=BuilderUiStyles.panel();

    private long finalValidatedRevision=-1;

    public BuilderPanel(){
        setLayout(new BorderLayout(0,11));
        setBorder(BuilderUiStyles.space(14,13,10,13));
        setBackground(BuilderUiStyles.BG);
        add(createToolbar(),BorderLayout.NORTH);

        JPanel main=new JPanel(new BorderLayout(11,0));
        main.setOpaque(false);
        main.add(createCategoryRail(),BorderLayout.WEST);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCatalogPane(),createSummaryPane());
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setResizeWeight(0.64);
        split.setDividerSize(6);
        main.add(split,BorderLayout.CENTER);
        add(main,BorderLayout.CENTER);

        JPanel footer=new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BuilderUiStyles.space(2,6,0,0));
        footer.add(bottomStatus,BorderLayout.WEST);
        JLabel reminder=BuilderUiStyles.label(
                "Không trừ kho · không lưu cấu hình thật",10,false,
                BuilderUiStyles.MUTED);
        footer.add(reminder,BorderLayout.EAST);
        add(footer,BorderLayout.SOUTH);

        searchBox.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){refreshCandidates();}
            public void removeUpdate(DocumentEvent e){refreshCandidates();}
            public void changedUpdate(DocumentEvent e){refreshCandidates();}
        });
        inStockOnly.addActionListener(e->refreshCandidates());
        operationBox.addActionListener(e->{
            refreshTargetLines();
            refreshCandidates();
        });
        targetBox.addActionListener(e->{
            if(operationBox.getSelectedItem()==Operation.SET_QUANTITY &&
                    targetBox.getSelectedItem() instanceof Entry entry) {
                quantityBox.setValue(entry.quantity());
            }
            refreshCandidates();
        });
        quantityBox.addChangeListener(e->refreshCandidates());
        candidateTable.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting())updatePartDetail();
        });
        scenarioBox.addActionListener(e->{
            Scenario scenario=(Scenario)scenarioBox.getSelectedItem();
            if(scenario!=null)loadScenario(scenario);
        });
        loadScenario(Scenario.VALID);
    }

    private JPanel createToolbar(){
        JPanel top=BuilderUiStyles.panel();
        top.setBorder(BuilderUiStyles.cardBorder());
        top.setLayout(new BorderLayout(12,8));
        JPanel titles=new JPanel();
        titles.setLayout(new BoxLayout(titles,BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(BuilderUiStyles.label("PC BUILDER · USE CASE UI",18,true,BuilderUiStyles.INK));
        titles.add(Box.createVerticalStrut(5));
        titles.add(BuilderUiStyles.label(
                "UC28–32: lựa chọn phần cứng  •  UC43: kiểm tra cuối  •  UC44: tồn kho",
                11,false,BuilderUiStyles.MUTED));
        top.add(titles,BorderLayout.WEST);
        JPanel tools=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0));
        tools.setOpaque(false);
        JLabel demoTag=BuilderUiStyles.label("DEMO · KHÔNG KẾT NỐI CSDL",10,true,
                BuilderUiStyles.AMBER);
        tools.add(demoTag);
        scenarioBox.setFont(BuilderUiStyles.font(11,false));
        scenarioBox.setPreferredSize(new Dimension(215,35));
        tools.add(scenarioBox);
        JButton reset=BuilderUiStyles.button("Đặt lại",false);
        reset.addActionListener(e->loadScenario((Scenario)scenarioBox.getSelectedItem()));
        tools.add(reset);
        JButton capture=BuilderUiStyles.button("Xuất PNG",true);
        capture.addActionListener(e->BuilderImageExport.save(this,this,
                "PCInventory_"+demo.scenario().name()+"_Builder"));
        tools.add(capture);
        top.add(tools,BorderLayout.EAST);
        return top;
    }

    private JPanel createCategoryRail(){
        categoryRail.setLayout(new BoxLayout(categoryRail,BoxLayout.Y_AXIS));
        categoryRail.setBorder(BuilderUiStyles.cardBorder());
        categoryRail.setPreferredSize(new Dimension(127,460));
        JLabel label=BuilderUiStyles.label("NHÓM LINH KIỆN",10,true,BuilderUiStyles.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        categoryRail.add(label);
        categoryRail.add(Box.createVerticalStrut(12));
        for(Kind kind:Kind.values()){
            JButton button=BuilderUiStyles.button(kind.title,false);
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.addActionListener(e->selectKind(kind));
            menuButtons.put(kind,button);
            categoryRail.add(button);
            categoryRail.add(Box.createVerticalStrut(5));
        }
        categoryRail.add(Box.createVerticalGlue());
        JLabel note=BuilderUiStyles.label("Trường · UC28–32",10,false,
                BuilderUiStyles.MUTED);
        note.setAlignmentX(LEFT_ALIGNMENT);
        categoryRail.add(note);
        return categoryRail;
    }

    private JPanel createCatalogPane(){
        JPanel p=BuilderUiStyles.panel();
        p.setBorder(BuilderUiStyles.cardBorder());
        p.setLayout(new BorderLayout(0,11));
        JPanel header=new JPanel(new BorderLayout(0,8));
        header.setOpaque(false);
        JPanel heading=new JPanel(new GridLayout(2,1,0,3));
        heading.setOpaque(false);
        heading.add(categoryTitle);heading.add(categoryNote);
        header.add(heading,BorderLayout.NORTH);
        JPanel filters=new JPanel(new BorderLayout(7,0));
        filters.setOpaque(false);
        searchBox.setToolTipText("Tìm theo mã, tên, hãng hoặc thông số");
        searchBox.setFont(BuilderUiStyles.font(12,false));
        filters.add(searchBox,BorderLayout.CENTER);
        inStockOnly.setOpaque(false);
        inStockOnly.setForeground(BuilderUiStyles.MUTED);
        inStockOnly.setFont(BuilderUiStyles.font(11,false));
        filters.add(inStockOnly,BorderLayout.EAST);
        header.add(filters,BorderLayout.SOUTH);
        p.add(header,BorderLayout.NORTH);

        candidateTable.setRowHeight(42);
        candidateTable.setFont(BuilderUiStyles.font(11,false));
        candidateTable.getTableHeader().setFont(BuilderUiStyles.font(11,true));
        candidateTable.setSelectionBackground(BuilderUiStyles.SOFT_BLUE);
        candidateTable.setGridColor(BuilderUiStyles.LINE);
        candidateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths={110,204,270,110,54,182};
        for(int i=0;i<widths.length;i++)
            candidateTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        candidateTable.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer(){
                    @Override public java.awt.Component getTableCellRendererComponent(
                            JTable table,Object value,boolean selected,
                            boolean focus,int row,int col){
                        super.getTableCellRendererComponent(table,value,selected,focus,row,col);
                        String status=value==null?"":value.toString();
                        if(!selected)setForeground(
                                status.startsWith("Phù hợp")?BuilderUiStyles.GREEN:
                                status.startsWith("Cần")?BuilderUiStyles.AMBER:
                                BuilderUiStyles.RED);
                        setFont(BuilderUiStyles.font(11,true));
                        return this;
                    }
                });
        JScrollPane listScroll=new JScrollPane(candidateTable);
        listScroll.getViewport().setBackground(BuilderUiStyles.WHITE);
        p.add(listScroll,BorderLayout.CENTER);

        JPanel bottom=new JPanel(new BorderLayout(0,8));
        bottom.setOpaque(false);
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        controls.setOpaque(false);
        controls.add(BuilderUiStyles.label("Thao tác",11,false,BuilderUiStyles.MUTED));
        operationBox.setFont(BuilderUiStyles.font(11,false));
        operationBox.setPreferredSize(new Dimension(115,30));
        controls.add(operationBox);
        controls.add(BuilderUiStyles.label("Dòng đích",11,false,BuilderUiStyles.MUTED));
        targetBox.setFont(BuilderUiStyles.font(11,false));
        targetBox.setPreferredSize(new Dimension(175,30));
        controls.add(targetBox);
        controls.add(BuilderUiStyles.label("SL",11,false,BuilderUiStyles.MUTED));
        quantityBox.setPreferredSize(new Dimension(55,30));
        controls.add(quantityBox);
        bottom.add(controls,BorderLayout.NORTH);
        JPanel partCard=new JPanel(new BorderLayout(0,8));
        partCard.setOpaque(false);
        partDetail.setEditable(false);
        partDetail.setLineWrap(true);
        partDetail.setWrapStyleWord(true);
        partDetail.setBackground(BuilderUiStyles.BG);
        partDetail.setFont(BuilderUiStyles.font(11,false));
        partDetail.setForeground(BuilderUiStyles.MUTED);
        partDetail.setRows(2);
        partDetail.setBorder(BuilderUiStyles.space(6,8,5,8));
        partCard.add(partDetail,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0));
        buttons.setOpaque(false);
        JButton cancel=BuilderUiStyles.button("Hủy chọn",false);
        cancel.addActionListener(e->{
            candidateTable.clearSelection();
            bottomStatus.setText("Đã hủy thao tác chọn. Không thay đổi cấu hình.");
        });
        confirmButton.addActionListener(e->applySelected());
        buttons.add(cancel);buttons.add(confirmButton);
        partCard.add(buttons,BorderLayout.SOUTH);
        bottom.add(partCard,BorderLayout.CENTER);
        p.add(bottom,BorderLayout.SOUTH);
        return p;
    }

    private JPanel createSummaryPane(){
        JPanel p=BuilderUiStyles.panel();
        p.setBorder(BuilderUiStyles.cardBorder());
        p.setPreferredSize(new Dimension(268,430));
        p.setLayout(new BorderLayout(0,9));
        JPanel heading=new JPanel(new BorderLayout(0,5));
        heading.setOpaque(false);
        heading.add(BuilderUiStyles.label("Cấu hình hiện tại",16,true,
                BuilderUiStyles.INK),BorderLayout.NORTH);
        heading.add(revisionLabel,BorderLayout.SOUTH);
        p.add(heading,BorderLayout.NORTH);

        selectedTable.setRowHeight(32);
        selectedTable.setFont(BuilderUiStyles.font(11,false));
        selectedTable.getTableHeader().setFont(BuilderUiStyles.font(10,true));
        selectedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths={79,185,34,105};
        for(int i=0;i<widths.length;i++)
            selectedTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane selectedScroll=new JScrollPane(selectedTable);
        selectedScroll.setPreferredSize(new Dimension(290,255));
        selectedScroll.getViewport().setBackground(BuilderUiStyles.WHITE);
        p.add(selectedScroll,BorderLayout.CENTER);

        JPanel bottom=new JPanel(new BorderLayout(0,8));
        bottom.setOpaque(false);
        JPanel totalLine=new JPanel(new BorderLayout());
        totalLine.setOpaque(false);
        totalLine.add(BuilderUiStyles.label("Tổng tiền làm việc",11,false,
                BuilderUiStyles.MUTED),BorderLayout.WEST);
        totalLine.add(totalLabel,BorderLayout.EAST);
        bottom.add(totalLine,BorderLayout.NORTH);
        technicalStatus.setEditable(false);
        technicalStatus.setLineWrap(true);
        technicalStatus.setWrapStyleWord(true);
        technicalStatus.setBackground(BuilderUiStyles.BG);
        technicalStatus.setForeground(BuilderUiStyles.MUTED);
        technicalStatus.setFont(BuilderUiStyles.font(11,false));
        technicalStatus.setRows(4);
        technicalStatus.setBorder(BuilderUiStyles.space(7,8,7,8));
        JScrollPane issueScroll=new JScrollPane(technicalStatus);
        issueScroll.setPreferredSize(new Dimension(270,94));
        bottom.add(issueScroll,BorderLayout.CENTER);
        JPanel actions=new JPanel(new GridLayout(2,1,0,7));
        JButton finalButton=BuilderUiStyles.button("UC43 · Kiểm tra cuối cấu hình",true);
        finalButton.addActionListener(e->showFinal());
        stockButton.addActionListener(e->showStock());
        actions.add(finalButton);actions.add(stockButton);
        bottom.add(actions,BorderLayout.SOUTH);
        p.add(bottom,BorderLayout.SOUTH);
        return p;
    }

    private void loadScenario(Scenario scenario){
        if(scenario==null)return;
        demo.reset(scenario);
        // The two UC44 screenshots assume the mock UC43 was already valid.
        finalValidatedRevision=(scenario==Scenario.UC44_SHORTAGE ||
                scenario==Scenario.UC44_UNKNOWN) && demo.finalIssues().isEmpty()
                ?demo.revision():-1;
        searchBox.setText("");
        inStockOnly.setSelected(false);
        currentKind=scenario.focus;
        selectKind(scenario.focus);
        if(scenario==Scenario.UC30)quantityBox.setValue(2);
        refreshAll();
        bottomStatus.setText("Đã tải tình huống "+scenario.name()+
                " — DEMO; dữ liệu MySQL không thay đổi.");
    }

    private void selectKind(Kind kind){
        currentKind=kind;
        for(Map.Entry<Kind,JButton> e:menuButtons.entrySet()){
            e.getValue().setBackground(
                    e.getKey()==kind?BuilderUiStyles.BLUE:BuilderUiStyles.SOFT_BLUE);
            e.getValue().setForeground(
                    e.getKey()==kind?Color.WHITE:BuilderUiStyles.INK);
        }
        categoryTitle.setText(kind.title+" · Danh sách linh kiện");
        categoryNote.setText("Chọn linh kiện để xem trước và kiểm tra các ràng buộc kỹ thuật");
        boolean multi=kind==Kind.RAM||kind==Kind.STORAGE||kind==Kind.FAN;
        operationBox.setEnabled(multi);
        operationBox.setSelectedItem(multi?Operation.ADD:Operation.REPLACE);
        quantityBox.setEnabled(multi);
        quantityBox.setValue(1);
        refreshTargetLines();
        refreshCandidates();
    }

    private void refreshTargetLines(){
        if(currentKind==null)return;
        boolean multi=currentKind==Kind.RAM||currentKind==Kind.STORAGE||currentKind==Kind.FAN;
        DefaultComboBoxModel<Entry> model=new DefaultComboBoxModel<>();
        for(Entry item:demo.entriesOf(currentKind))model.addElement(item);
        targetBox.setModel(model);
        targetBox.setEnabled(multi&&operationBox.getSelectedItem()!=Operation.ADD);
    }

    private Part chooseCandidateForPreview(Part p){
        if(currentKind==Kind.STORAGE &&
                operationBox.getSelectedItem()==Operation.SET_QUANTITY &&
                targetBox.getSelectedItem() instanceof Entry entry){
            return entry.part();
        }
        return p;
    }

    private List<Issue> previewPart(Part p){
        Part actual=chooseCandidateForPreview(p);
        Operation op=(Operation)operationBox.getSelectedItem();
        Entry target=(Entry)targetBox.getSelectedItem();
        return demo.preview(actual,op,target==null?null:target.lineId(),
                ((Number)quantityBox.getValue()).intValue());
    }

    private void refreshCandidates(){
        if(currentKind==null||searchBox==null)return;
        String previousCode=null;
        int prev=candidateTable.getSelectedRow();
        if(prev>=0&&prev<visibleCandidates.size())
            previousCode=visibleCandidates.get(prev).code();
        visibleCandidates.clear();
        visibleCandidates.addAll(demo.candidates(currentKind,
                searchBox.getText(),inStockOnly.isSelected()));
        candidateModel.setRowCount(0);
        for(Part p:visibleCandidates){
            List<Issue> errs=previewPart(p).stream().filter(i->!i.pending()).toList();
            String status;
            if(p.stock()==-1) status="Chưa xác minh tồn";
            else if(p.stock()==0)status="Hết hàng";
            else if(errs.isEmpty())status="Phù hợp sơ bộ";
            else if(currentKind==Kind.MAINBOARD &&
                    errs.stream().allMatch(i->i.code().startsWith("RAM_")))
                status="Cần điều chỉnh RAM";
            else status="Xung đột: "+errs.get(0).code();
            candidateModel.addRow(new Object[]{p.code(),p.name(),p.info(),
                    BuilderUiStyles.money(p.price()),
                    p.stock()<0?"?":p.stock(),status});
        }
        String code=previousCode!=null?previousCode:demo.scenario().highlightedCode;
        for(int i=0;i<visibleCandidates.size();i++){
            if(visibleCandidates.get(i).code().equals(code)){
                candidateTable.setRowSelectionInterval(i,i);
                candidateTable.scrollRectToVisible(candidateTable.getCellRect(i,0,true));
                break;
            }
        }
        updatePartDetail();
        confirmButton.setEnabled(!visibleCandidates.isEmpty());
    }

    private void updatePartDetail(){
        int row=candidateTable.getSelectedRow();
        if(row<0||row>=visibleCandidates.size()){
            partDetail.setText("Chọn một dòng để xem thông số và trạng thái lựa chọn.");
            return;
        }
        Part p=visibleCandidates.get(row);
        partDetail.setText(p.name()+" · "+p.brand()+"\n"+
                p.info()+" · Giá demo "+BuilderUiStyles.money(p.price())+
                " · tồn "+(p.stock()<0?"không xác minh được":p.stock()));
    }

    private void applySelected(){
        int row=candidateTable.getSelectedRow();
        if(row<0||row>=visibleCandidates.size()){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn một linh kiện.");
            return;
        }
        Part part=visibleCandidates.get(row);
        Operation operation=(Operation)operationBox.getSelectedItem();
        Entry target=(Entry)targetBox.getSelectedItem();
        if(operation==Operation.SET_QUANTITY && target!=null)part=target.part();
        int quantity=((Number)quantityBox.getValue()).intValue();
        String targetId=target==null?null:target.lineId();
        // UC31 and UC32 input prerequisites in the corresponding diagrams.
        if(currentKind==Kind.CASE&&!demo.has(Kind.MAINBOARD)){
            showInputError("UC31","Cần Mainboard trước khi chọn Case.");return;
        }
        if(currentKind==Kind.PSU&&!demo.has(Kind.CASE)){
            showInputError("UC32","Cần Case trước khi chọn PSU.");return;
        }
        if(currentKind==Kind.STORAGE&&!demo.has(Kind.MAINBOARD)){
            showInputError("UC30","Cần Mainboard trước khi chọn Storage.");return;
        }
        if(part.stock()<0){
            showInputError(currentKind.name(),"Không có dữ liệu xác minh tồn kho cho "+part.code());
            return;
        }
        int used=demo.selectedQuantity(part.code());
        if(operation==Operation.REPLACE&&target!=null&&target.part().code().equals(part.code()))
            used-=target.quantity();
        if(operation==Operation.SET_QUANTITY&&target!=null&&target.part().code().equals(part.code()))
            used-=target.quantity();
        if(currentKind!=Kind.RAM&&currentKind!=Kind.STORAGE&&currentKind!=Kind.FAN){
            used=0; // one unit of a singleton slot replaces the existing one
        }
        if(used+quantity>part.stock()){
            showInputError(currentKind.name(),"Tồn mẫu không đủ: cần "+
                    (used+quantity)+", có "+part.stock()+".");
            return;
        }

        List<Issue> issues=demo.preview(part,operation,targetId,quantity);
        List<Issue> errors=issues.stream().filter(i->!i.pending()).toList();
        if(!errors.isEmpty()){
            if(currentKind==Kind.MAINBOARD &&
                    errors.stream().allMatch(i->i.code().startsWith("RAM_"))){
                handleMainboardAdjustment(part,errors);
            }else{
                BuilderDialogs.showIssues(this,"UC"+ucFor(currentKind),errors);
                bottomStatus.setText("REJECTED · "+errors.get(0).code()+
                        " — cấu hình và revision giữ nguyên.");
            }
            return;
        }
        int confirm=JOptionPane.showConfirmDialog(this,
                "Áp dụng lựa chọn trên bản sao?\n"
                +part.name()+"  ·  SL "+quantity+"\n"
                +"Tổng giá sẽ được tính lại sau khi xác nhận.\n"
                +"(Dữ liệu minh họa, không ghi MySQL)",
                "Xác nhận — UC"+ucFor(currentKind),JOptionPane.YES_NO_OPTION);
        if(confirm!=JOptionPane.YES_OPTION){
            bottomStatus.setText("Đã hủy xác nhận · cấu hình và giá không đổi.");return;
        }
        try{
            demo.commit(part,operation,targetId,quantity);
            finalValidatedRevision=-1;
            refreshAll();
            bottomStatus.setText("UPDATED · "+part.code()+" · revision "+demo.revision()+
                    " (chỉ cập nhật bản demo trên giao diện).");
        }catch(RuntimeException ex){
            showInputError(currentKind.name(),ex.getMessage());
        }
    }

    private void handleMainboardAdjustment(Part mainboard,List<Issue> errors){
        BuilderDialogs.AdjustmentChoice choice=
                BuilderDialogs.chooseMainboardAdjustment(this,
                        mainboard.name(),errors,"CPU LGA1700 / RAM DDR5");
        if(choice==BuilderDialogs.AdjustmentChoice.CANCEL){
            bottomStatus.setText("UC28 · Hủy phương án. Cấu hình hiện tại được giữ nguyên.");
            return;
        }
        String action=choice==BuilderDialogs.AdjustmentChoice.REPLACE_RAM
                ?"Thay RAM bằng dòng DDR tương thích":"Bỏ RAM khỏi cấu hình nháp";
        int confirm=JOptionPane.showConfirmDialog(this,
                "Xác nhận phương án đã xem trước:\n- "+mainboard.name()+
                "\n- "+action+"\n\nSau xác nhận sẽ tăng revision đúng một lần.",
                "UC28 · Xác nhận điều chỉnh",JOptionPane.YES_NO_OPTION);
        if(confirm!=JOptionPane.YES_OPTION){
            bottomStatus.setText("UC28 · Đã hủy bước xác nhận, không cập nhật.");return;
        }
        try{
            demo.commitMainboardAdjustment(mainboard,
                    choice==BuilderDialogs.AdjustmentChoice.REPLACE_RAM);
            finalValidatedRevision=-1;
            refreshAll();
            bottomStatus.setText("UC28 · Đã áp dụng phương án điều chỉnh trên dữ liệu demo.");
        }catch(RuntimeException ex){
            showInputError("UC28",ex.getMessage());
        }
    }

    private static int ucFor(Kind kind){
        return switch(kind){
            case MAINBOARD->28;
            case RAM->29;
            case STORAGE->30;
            case CASE->31;
            case PSU->32;
            default->26;
        };
    }
    private void showInputError(String uc,String message){
        BuilderDialogs.showIssues(this,uc,
                List.of(new Issue("INPUT_OR_DATA_ERROR",message,false)));
        bottomStatus.setText("REJECTED · "+message);
    }

    private void refreshAll(){
        selectedModel.setRowCount(0);
        for(Entry e:demo.entries()){
            selectedModel.addRow(new Object[]{e.part().kind().title,
                    e.part().name(),e.quantity(),
                    BuilderUiStyles.money(e.part().price()*e.quantity())});
        }
        totalLabel.setText(BuilderUiStyles.money(demo.totalPrice()));
        revisionLabel.setText("Phiên demo · revision "+demo.revision()+
                " · "+demo.entries().size()+" dòng");
        List<Issue> issues=demo.draftIssues();
        if(issues.isEmpty()) technicalStatus.setText(
                "Đã kiểm tra các phụ thuộc hiện có.\n"+
                "Muốn chốt để lắp, hãy chạy UC43.");
        else{
            StringBuilder text=new StringBuilder("Kiểm tra bản nháp:\n");
            for(int i=0;i<Math.min(3,issues.size());i++)
                text.append("• ").append(issues.get(i).code()).append("\n");
            if(issues.size()>3)text.append("… và ").append(issues.size()-3).append(" vấn đề khác");
            technicalStatus.setText(text.toString());
        }
        stockButton.setEnabled(finalValidatedRevision==demo.revision() &&
                demo.finalIssues().isEmpty());
        refreshTargetLines();
        refreshCandidates();
    }

    private void showFinal(){
        List<Issue> issues=demo.finalIssues();
        boolean valid=issues.isEmpty();
        finalValidatedRevision=valid?demo.revision():-1;
        boolean openStock=BuilderDialogs.showFinal(this,issues,
                demo.revision(),demo.entries().size());
        stockButton.setEnabled(valid);
        bottomStatus.setText(valid?"UC43 · VALID — snapshot demo đã được kiểm tra.":
                "UC43 · INVALID — còn lỗi hoặc điều kiện chưa xác minh.");
        if(openStock&&valid)showStock();
    }
    private void showStock(){
        if(finalValidatedRevision!=demo.revision() ||
                !demo.finalIssues().isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Hãy kiểm tra cuối (UC43) trên chính snapshot hiện tại trước khi UC44.",
                    "UC44 · Thiếu tiền điều kiện",JOptionPane.WARNING_MESSAGE);
            return;
        }
        var report=demo.stockReport();
        BuilderDialogs.showStock(this,report,demo.revision());
        bottomStatus.setText("UC44 · "+report.status()+
                " — chỉ đọc tồn giả lập, không giữ chỗ hoặc trừ kho.");
    }
}
