package pcinventory.ui.builder;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/** Visual tokens only. Keeps the team MainFrame untouched. */
final class BuilderUiStyles {
    static final Color INK = new Color(24, 41, 65);
    static final Color MUTED = new Color(101, 117, 139);
    static final Color BLUE = new Color(37, 102, 194);
    static final Color SOFT_BLUE = new Color(231, 240, 253);
    static final Color BG = new Color(246, 248, 252);
    static final Color WHITE = Color.WHITE;
    static final Color LINE = new Color(222, 228, 237);
    static final Color GREEN = new Color(22, 132, 95);
    static final Color AMBER = new Color(166, 102, 17);
    static final Color RED = new Color(190, 56, 60);
    private BuilderUiStyles() {}

    static JPanel panel() {
        JPanel p=new JPanel();
        p.setBackground(WHITE);
        return p;
    }
    static Font font(int size, boolean bold) {
        return new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size);
    }
    static JLabel label(String value,int size,boolean bold,Color color) {
        JLabel label=new JLabel(value);
        label.setFont(font(size,bold));
        label.setForeground(color);
        return label;
    }
    static Border space(int top,int left,int bottom,int right) {
        return BorderFactory.createEmptyBorder(top,left,bottom,right);
    }
    static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),space(10,12,10,12));
    }
    static JButton button(String text,boolean primary) {
        JButton b=new JButton(text);
        b.setFont(font(12,primary));
        b.setForeground(primary?WHITE:INK);
        b.setBackground(primary?BLUE:SOFT_BLUE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary?BLUE:LINE),
                space(9,11,9,11)));
        return b;
    }
    static void pad(JComponent c,int top,int left,int bottom,int right) {
        c.setBorder(space(top,left,bottom,right));
    }
    static String money(long v) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
                .format(v)+" ₫";
    }
}
