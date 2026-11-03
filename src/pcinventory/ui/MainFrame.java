package pcinventory.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import pcinventory.ui.assembled.AssembledPCPanel;
import pcinventory.ui.builder.BuilderPanel;
import pcinventory.ui.component.ComponentPanel;
import pcinventory.ui.configuration.SavedConfigurationPanel;
import pcinventory.ui.dashboard.DashboardPanel;
import pcinventory.ui.imports.ImportPanel;
import pcinventory.ui.inventory.InventoryPanel;

/**
 * Shared navigation shell. Feature teams should own their JPanel classes,
 * not edit this frame unless a shared contract changes.
 */
public final class MainFrame extends JFrame {

    private static final Color NAV_BACKGROUND = new Color(21, 32, 52);
    private static final Color NAV_SELECTED = new Color(38, 91, 170);
    private static final Color CONTENT_BACKGROUND = new Color(245, 247, 251);

    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);
    private final Map<AppPage, JPanel> registeredPanels =
            new EnumMap<>(AppPage.class);
    private final Map<AppPage, JButton> navigationButtons =
            new EnumMap<>(AppPage.class);
    private final JLabel currentTitle = new JLabel();
    private final JLabel currentDescription = new JLabel();
    private AppPage currentPage;

    public MainFrame() {
        super("PCInventory - Quản lý kho và cấu hình PC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setSize(1180, 730);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CONTENT_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        setContentPane(root);

        // Each module owns its own JPanel. Replace these registrations later.
        registerPage(AppPage.DASHBOARD, new DashboardPanel());
        registerPage(AppPage.COMPONENTS, new ComponentPanel());
        registerPage(AppPage.INVENTORY, new InventoryPanel());
        registerPage(AppPage.IMPORTS, new ImportPanel());
        registerPage(AppPage.BUILDER, new BuilderPanel());
        registerPage(AppPage.SAVED_CONFIGS, new SavedConfigurationPanel());
        registerPage(AppPage.ASSEMBLED_PCS, new AssembledPCPanel());

        showPage(AppPage.DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAV_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(242, 0));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(NAV_BACKGROUND);
        brand.setBorder(BorderFactory.createEmptyBorder(26, 22, 24, 15));

        JLabel name = new JLabel("PCInventory");
        name.setFont(new Font("SansSerif", Font.BOLD, 22));
        name.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("PTIT - Software Engineering");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(new Color(175, 190, 215));
        brand.add(name);
        brand.add(Box.createVerticalStrut(7));
        brand.add(subtitle);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(NAV_BACKGROUND);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (AppPage page : AppPage.values()) {
            JButton button = new JButton(page.getTitle());
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            button.setPreferredSize(new Dimension(210, 48));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFont(new Font("SansSerif", Font.PLAIN, 14));
            button.setForeground(Color.WHITE);
            button.setBackground(NAV_BACKGROUND);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 10));
            button.addActionListener(e -> showPage(page));
            navigationButtons.put(page, button);
            menu.add(button);
            menu.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(NAV_BACKGROUND);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(scroll, BorderLayout.CENTER);

        JLabel footer = new JLabel("Skeleton v1  |  Java Swing");
        footer.setBorder(BorderFactory.createEmptyBorder(16, 20, 18, 10));
        footer.setForeground(new Color(175, 190, 215));
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(CONTENT_BACKGROUND);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(21, 25, 20, 20));

        currentTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        currentTitle.setForeground(new Color(28, 43, 63));
        currentDescription.setFont(new Font("SansSerif", Font.PLAIN, 12));
        currentDescription.setForeground(new Color(100, 115, 135));
        header.add(currentTitle);
        header.add(Box.createVerticalStrut(6));
        header.add(currentDescription);

        workspace.add(header, BorderLayout.NORTH);
        cardHost.setBackground(CONTENT_BACKGROUND);
        workspace.add(cardHost, BorderLayout.CENTER);

        JLabel status = new JLabel(
                "  Bản khung giao diện: chưa có đăng nhập, phân quyền hay nghiệp vụ hoàn chỉnh.");
        status.setFont(new Font("SansSerif", Font.PLAIN, 11));
        status.setForeground(new Color(90, 105, 125));
        status.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));
        workspace.add(status, BorderLayout.SOUTH);
        return workspace;
    }

    /**
     * Integration point for feature teams (run on Swing EDT).
     * Supports replacing an existing placeholder without duplicating cards.
     */
    public void registerPage(AppPage page, JPanel panel) {
        if (page == null || panel == null) {
            throw new IllegalArgumentException("Page and panel must not be null.");
        }
        JPanel previous = registeredPanels.put(page, panel);
        if (previous != null) {
            cardHost.remove(previous);
        }
        cardHost.add(panel, page.name());
        cardHost.revalidate();
        cardHost.repaint();
        if (page == currentPage) {
            cards.show(cardHost, page.name());
        }
    }

    public void showPage(AppPage page) {
        if (!registeredPanels.containsKey(page)) {
            throw new IllegalArgumentException("Page not registered: " + page);
        }
        cards.show(cardHost, page.name());
        currentPage = page;
        currentTitle.setText(page.getTitle());
        currentDescription.setText(page.getDescription());

        for (Map.Entry<AppPage, JButton> entry : navigationButtons.entrySet()) {
            entry.getValue().setBackground(
                    entry.getKey() == page ? NAV_SELECTED : NAV_BACKGROUND);
        }
    }
}
