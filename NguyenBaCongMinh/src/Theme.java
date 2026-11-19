import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/** Màu sắc + các hàm tạo giao diện dùng chung cho mọi form. */
public class Theme {
    // ---- Bảng màu (tối - nam tính) ----
    public static final Color BG     = new Color(15, 23, 42);    // nền chính xanh navy đậm
    public static final Color PANEL  = new Color(30, 41, 59);    // nền khung
    public static final Color INPUT  = new Color(51, 65, 85);    // ô nhập
    public static final Color BORDER = new Color(71, 85, 105);
    public static final Color ACCENT = new Color(56, 189, 248);  // xanh cyan
    public static final Color BLUE   = new Color(37, 99, 235);
    public static final Color GREEN  = new Color(22, 163, 74);
    public static final Color ORANGE = new Color(245, 158, 11);
    public static final Color RED    = new Color(220, 38, 38);
    public static final Color PURPLE = new Color(124, 58, 237);
    public static final Color TEXT   = new Color(241, 245, 249);
    public static final Color MUTED  = new Color(148, 163, 184);

    // ---- Font ----
    public static final Font TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD   = new Font("Segoe UI", Font.BOLD, 14);

    /** Nút bo tròn, đổi màu khi rê chuột. */
    public static JButton button(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? color.brighter() : color;
                if (getModel().isPressed()) c = color.darker();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(BOLD);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 38));
        return b;
    }

    public static void styleInput(JTextField f) {
        f.setBackground(INPUT);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        styleInput(f);
        return f;
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setBackground(INPUT);
        c.setForeground(TEXT);
        c.setFont(NORMAL);
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(BOLD);
        return l;
    }

    /** Thêm 1 dòng "nhãn + ô nhập" vào panel dùng GridBagLayout. */
    public static void addRow(JPanel p, int row, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row; g.insets = new Insets(7, 6, 7, 6);
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; g.weightx = 0;
        p.add(label(label), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(c, g);
    }

    /** Thanh tiêu đề phía trên mỗi form. */
    public static JPanel header(String title) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BLUE, getWidth(), 0, new Color(8, 145, 178)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JLabel l = new JLabel(title);
        l.setFont(TITLE);
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        p.add(l, BorderLayout.WEST);
        return p;
    }

    /** Tô màu bảng dữ liệu. */
    public static void styleTable(JTable t) {
        t.setBackground(PANEL);
        t.setForeground(TEXT);
        t.setFont(NORMAL);
        t.setRowHeight(32);
        t.setGridColor(new Color(51, 65, 85));
        t.setSelectionBackground(BLUE);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v,
                    boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, f, r, c);
                l.setBackground(BLUE);
                l.setForeground(Color.WHITE);
                l.setFont(BOLD);
                l.setHorizontalAlignment(CENTER);
                return l;
            }
        });
    }

    /** Cột hiển thị tiền: 150,000 đ */
    public static void moneyColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                setText(v instanceof Number ? money(((Number) v).doubleValue()) : String.valueOf(v));
                setHorizontalAlignment(RIGHT);
            }
        });
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(PANEL);
        sp.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        return sp;
    }

    public static String money(double d) { return String.format("%,.0f đ", d); }

    public static void info(Component p, String m)  { JOptionPane.showMessageDialog(p, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE); }
    public static void error(Component p, String m) { JOptionPane.showMessageDialog(p, m, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    public static boolean confirm(Component p, String m) {
        return JOptionPane.showConfirmDialog(p, m, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
