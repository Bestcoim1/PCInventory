package pcinventory.ui.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import pcinventory.ui.builder.BuilderDemoData.Issue;
import pcinventory.ui.builder.BuilderDemoData.StockLine;
import pcinventory.ui.builder.BuilderDemoData.StockReport;

/** Document-oriented Swing dialogs for UC28, UC29-32, UC43 and UC44. */
final class BuilderDialogs {
    enum AdjustmentChoice { REPLACE_RAM, REMOVE_RAM, CANCEL }
    private BuilderDialogs() {}

    private static JDialog createDialog(Component parent,String title,int width,int height) {
        Window owner=SwingUtilities.getWindowAncestor(parent);
        JDialog d=new JDialog(owner,title,Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setSize(width,height);
        d.setMinimumSize(new Dimension(540,340));
        d.setLocationRelativeTo(parent);
        return d;
    }
    private static JPanel root() {
        JPanel p=new JPanel(new BorderLayout(0,13));
        p.setBackground(BuilderUiStyles.BG);
        p.setBorder(BuilderUiStyles.space(17,19,17,19));
        return p;
    }
    private static JPanel heading(String title,String subtitle,Color accent) {
        JPanel panel=new JPanel(new GridLayout(2,1,0,5));
        panel.setBackground(BuilderUiStyles.BG);
        panel.add(BuilderUiStyles.label(title,19,true,accent));
        panel.add(BuilderUiStyles.label(subtitle,12,false,BuilderUiStyles.MUTED));
        return panel;
    }
    private static JPanel buttons() {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,9,0));
        p.setBackground(BuilderUiStyles.BG);
        return p;
    }
    private static JTable table(Object[][] rows,String...columns) {
        JTable t=new JTable(new DefaultTableModel(rows,columns){
            @Override public boolean isCellEditable(int row,int col){return false;}
        });
        t.setRowHeight(32);
        t.setFont(BuilderUiStyles.font(12,false));
        t.getTableHeader().setFont(BuilderUiStyles.font(12,true));
        t.setSelectionBackground(BuilderUiStyles.SOFT_BLUE);
        t.setShowVerticalLines(false);
        return t;
    }

    static void showIssues(Component parent,String ucTitle,List<Issue> issues) {
        JDialog dialog=createDialog(parent,ucTitle+" — Kết quả kiểm tra",750,460);
        JPanel body=root();
        body.add(heading("Không thể áp dụng phương án",
                "Các lỗi được phát hiện trên bản sao, cấu hình hiện tại không đổi.",
                BuilderUiStyles.RED),BorderLayout.NORTH);
        Object[][] data=new Object[issues.size()][3];
        for(int i=0;i<issues.size();i++){
            Issue issue=issues.get(i);
            data[i]=new Object[]{issue.code(),issue.pending()?"Chờ dữ liệu":"Xung đột",
                    issue.description()};
        }
        JTable t=table(data,"Mã kiểm tra","Trạng thái","Nội dung");
        t.getColumnModel().getColumn(0).setPreferredWidth(165);
        t.getColumnModel().getColumn(1).setPreferredWidth(100);
        t.getColumnModel().getColumn(2).setPreferredWidth(370);
        body.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel actions=buttons();
        JButton photo=BuilderUiStyles.button("Xuất PNG",false);
        photo.addActionListener(e->BuilderImageExport.save(body,dialog,"UC_issues"));
        JButton close=BuilderUiStyles.button("Đóng",true);
        close.addActionListener(e->dialog.dispose());
        actions.add(photo);actions.add(close);
        body.add(actions,BorderLayout.SOUTH);
        dialog.setContentPane(body);dialog.setVisible(true);
    }

    static AdjustmentChoice chooseMainboardAdjustment(
            Component parent,String proposedName,List<Issue> issues,
            String currentSummary) {
        JDialog dialog=createDialog(parent,"UC28 — Phương án điều chỉnh Mainboard",760,510);
        JPanel body=root();
        body.add(heading("Mainboard mới ảnh hưởng đến RAM",
                "UC28 · Chưa thay đổi cấu hình — cần nhân viên lựa chọn phương án.",
                BuilderUiStyles.AMBER),BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(0,10));
        center.setBackground(BuilderUiStyles.BG);
        JTextArea description=new JTextArea(
                "Mainboard đề xuất: "+proposedName+"\n"
              + "Trạng thái cũ: "+currentSummary+"\n\n"
              + "Chọn một trong hai phương án:\n"
              + "• Thay toàn bộ RAM không tương thích bằng RAM tương thích trong dữ liệu demo.\n"
              + "• Bỏ RAM không tương thích khỏi bản nháp và bổ sung sau.\n\n"
              + "Thao tác hủy giữ nguyên Mainboard, RAM, giá và revision.");
        description.setEditable(false);
        description.setOpaque(false);
        description.setWrapStyleWord(true);description.setLineWrap(true);
        description.setFont(BuilderUiStyles.font(13,false));
        center.add(description,BorderLayout.NORTH);
        Object[][] issueRows=new Object[issues.size()][2];
        for(int i=0;i<issues.size();i++)
            issueRows[i]=new Object[]{issues.get(i).code(),issues.get(i).description()};
        center.add(new JScrollPane(table(issueRows,"Mã lỗi","Mô tả")),BorderLayout.CENTER);
        body.add(center,BorderLayout.CENTER);
        JPanel actions=buttons();
        AtomicInteger choice=new AtomicInteger(2);
        JButton photo=BuilderUiStyles.button("Xuất PNG",false);
        photo.addActionListener(e->BuilderImageExport.save(body,dialog,"UC28_Adjustment"));
        JButton cancel=BuilderUiStyles.button("Hủy",false);
        cancel.addActionListener(e->{choice.set(2);dialog.dispose();});
        JButton remove=BuilderUiStyles.button("Bỏ RAM",false);
        remove.addActionListener(e->{choice.set(1);dialog.dispose();});
        JButton replace=BuilderUiStyles.button("Thay RAM phù hợp",true);
        replace.addActionListener(e->{choice.set(0);dialog.dispose();});
        actions.add(photo);actions.add(cancel);actions.add(remove);actions.add(replace);
        body.add(actions,BorderLayout.SOUTH);
        dialog.setContentPane(body);dialog.setVisible(true);
        return switch(choice.get()){
            case 0->AdjustmentChoice.REPLACE_RAM;
            case 1->AdjustmentChoice.REMOVE_RAM;
            default->AdjustmentChoice.CANCEL;
        };
    }

    /**
     * Returns true only if a valid final check was explicitly followed by
     * a request to open the inventory panel. This does not reserve stock.
     */
    static boolean showFinal(Component parent,List<Issue> issues,long revision,
                             int componentLines) {
        boolean valid=issues.isEmpty();
        JDialog dialog=createDialog(parent,"UC43 — Kiểm tra cuối cấu hình",770,510);
        JPanel body=root();
        body.add(heading(valid?"Cấu hình hoàn chỉnh và tương thích":
                         "Cấu hình chưa đủ điều kiện lắp",
                "Snapshot bản nháp · revision "+revision+" · "+componentLines+
                " dòng linh kiện · Không đọc kho/không ghi CSDL",
                valid?BuilderUiStyles.GREEN:BuilderUiStyles.RED),BorderLayout.NORTH);
        Object[][] rows;
        if(valid)rows=new Object[][]{{"VALID","Không có lỗi hoặc pending","Đã đạt kiểm tra kỹ thuật"}};
        else {
            rows=new Object[issues.size()][3];
            for(int i=0;i<issues.size();i++) {
                Issue issue=issues.get(i);
                rows[i]=new Object[]{issue.code(),issue.pending()?"Chờ xác minh":"Không đạt",
                        issue.description()};
            }
        }
        JTable table=table(rows,"Mã/Trạng thái","Phân loại","Nội dung");
        table.getColumnModel().getColumn(0).setPreferredWidth(175);
        table.getColumnModel().getColumn(2).setPreferredWidth(380);
        body.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel foot=buttons();
        AtomicInteger proceed=new AtomicInteger(0);
        JButton photo=BuilderUiStyles.button("Xuất PNG",false);
        photo.addActionListener(e->BuilderImageExport.save(body,dialog,"UC43_FinalValidation"));
        JButton close=BuilderUiStyles.button("Đóng",false);
        close.addActionListener(e->dialog.dispose());
        foot.add(photo);foot.add(close);
        if(valid) {
            JButton stock=BuilderUiStyles.button("Tiếp tục kiểm tra tồn (UC44)",true);
            stock.addActionListener(e->{proceed.set(1);dialog.dispose();});
            foot.add(stock);
        }
        body.add(foot,BorderLayout.SOUTH);
        dialog.setContentPane(body);dialog.setVisible(true);
        return proceed.get()==1;
    }

    static void showStock(Component parent,StockReport report,long revision) {
        JDialog dialog=createDialog(parent,"UC44 — Kiểm tra tồn kho trước lắp",790,540);
        JPanel body=root();
        Color color=switch(report.status()){
            case "AVAILABLE"->BuilderUiStyles.GREEN;
            case "INSUFFICIENT"->BuilderUiStyles.RED;
            default->BuilderUiStyles.AMBER;
        };
        String title=switch(report.status()){
            case "AVAILABLE"->"ĐỦ HÀNG tại thời điểm kiểm tra";
            case "INSUFFICIENT"->"THIẾU TỒN KHO — chưa thể lắp";
            default->"KHÔNG XÁC MINH ĐƯỢC tồn kho";
        };
        String time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        body.add(heading(title,
                "UC44 · Demo · "+time+" · snapshot revision "+revision+
                " · KHÔNG giữ chỗ/trừ tồn",color),BorderLayout.NORTH);
        Object[][] rows=new Object[report.lines().size()][5];
        for(int i=0;i<report.lines().size();i++){
            StockLine line=report.lines().get(i);
            rows[i]=new Object[]{
                line.code(),line.name(),line.required(),
                line.available()==null?"Không rõ":line.available(),
                line.shortage()==null?"?":line.shortage()
            };
        }
        JTable t=table(rows,"Mã linh kiện","Tên linh kiện","Cần","Còn","Thiếu");
        t.getColumnModel().getColumn(1).setPreferredWidth(250);
        body.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(4,8));
        footer.setBackground(BuilderUiStyles.BG);
        footer.add(BuilderUiStyles.label(report.note(),12,false,
                BuilderUiStyles.MUTED),BorderLayout.NORTH);
        JPanel actions=buttons();
        JButton photo=BuilderUiStyles.button("Xuất PNG",false);
        photo.addActionListener(e->BuilderImageExport.save(body,dialog,"UC44_Stock_"+report.status()));
        JButton close=BuilderUiStyles.button("Đóng",true);
        close.addActionListener(e->dialog.dispose());
        actions.add(photo);actions.add(close);
        footer.add(actions,BorderLayout.CENTER);
        body.add(footer,BorderLayout.SOUTH);
        dialog.setContentPane(body);dialog.setVisible(true);
    }
}
