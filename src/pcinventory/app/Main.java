package pcinventory.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import pcinventory.ui.MainFrame;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use the default Swing look and feel.
            }
            new MainFrame().setVisible(true);
        });
    }
}
