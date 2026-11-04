package pcinventory.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import pcinventory.dao.ComponentDAO;

/**
 * Read-only JDBC smoke test; it never blocks Swing's event dispatch thread.
 */
public final class DashboardPanel extends JPanel {

    private final JLabel connectionStatus = new JLabel("Chưa kiểm tra");
    private final JLabel componentCount = new JLabel("--");
    private final JLabel detail = new JLabel("Dữ liệu đọc từ pc_inventory.component");
    private final JButton refreshButton = new JButton("Kiểm tra JDBC");

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245, 247, 251));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel summary = new JPanel(new GridLayout(1, 2, 16, 0));
        summary.setOpaque(false);
        summary.add(makeMetricCard("Kết nối MySQL", connectionStatus));
        summary.add(makeMetricCard("Tổng linh kiện", componentCount));

        JPanel instructions = new JPanel(new BorderLayout(0, 14));
        instructions.setBackground(Color.WHITE);
        instructions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 239)),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)));

        JLabel intro = new JLabel("PCInventory – Project Skeleton v1");
        intro.setFont(new Font("SansSerif", Font.BOLD, 18));
        instructions.add(intro, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(3, 1, 0, 10));
        body.setOpaque(false);
        body.add(new JLabel(
                "Các trang nghiệp vụ đã có khu vực hiển thị riêng bằng CardLayout."));
        body.add(new JLabel(
                "Mỗi thành viên có thể thay JPanel mẫu bằng JPanel Form của mình."));
        body.add(detail);
        instructions.add(body, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refreshDatabaseStatus());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);
        instructions.add(actions, BorderLayout.SOUTH);

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(summary, BorderLayout.NORTH);
        north.add(instructions, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
    }

    private static JPanel makeMetricCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 239)),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)));
        JLabel heading = new JLabel(title);
        heading.setForeground(new Color(82, 100, 123));
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setForeground(new Color(34, 69, 114));
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    public void refreshDatabaseStatus() {
        refreshButton.setEnabled(false);
        connectionStatus.setText("Đang kiểm tra...");
        componentCount.setText("--");
        detail.setText("Đang truy vấn MySQL...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return new ComponentDAO().countAll();
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    int count = get();
                    connectionStatus.setText("Đã kết nối");
                    componentCount.setText(Integer.toString(count));
                    detail.setText("Đã truy vấn thành công bảng component.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showFailure("Thao tác bị gián đoạn.");
                } catch (ExecutionException ex) {
                    showFailure("Không kết nối được. Kiểm tra config/db.properties.");
                }
            }
        }.execute();
    }

    private void showFailure(String message) {
        connectionStatus.setText("Chưa kết nối");
        componentCount.setText("--");
        detail.setText(message);
    }
}
