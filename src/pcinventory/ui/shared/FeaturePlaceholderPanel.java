package pcinventory.ui.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import pcinventory.ui.AppPage;

/**
 * Temporary content. Replace each module's panel with a NetBeans JPanel Form
 * once its assigned team member begins implementing that feature.
 */
public class FeaturePlaceholderPanel extends JPanel {

    public FeaturePlaceholderPanel(AppPage page) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 251));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new GridLayout(4, 1, 0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 239)),
                BorderFactory.createEmptyBorder(36, 32, 36, 32)));

        JLabel title = new JLabel(page.getTitle(), SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(28, 43, 63));

        JLabel description = new JLabel(page.getDescription());
        description.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JLabel owner = new JLabel("Phụ trách: " + page.getOwner());
        owner.setForeground(new Color(84, 102, 124));

        JLabel note = new JLabel(
                "Trang mẫu. Chưa triển khai nghiệp vụ hoặc quyền truy cập.");
        note.setForeground(new Color(96, 110, 130));

        card.add(title);
        card.add(description);
        card.add(owner);
        card.add(note);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);
    }
}
