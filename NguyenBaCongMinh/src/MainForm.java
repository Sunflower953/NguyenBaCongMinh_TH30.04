import javax.swing.*;
import java.awt.*;
import java.sql.*;

/** FORM 2: Trang chủ - thống kê nhanh + menu mở các form chức năng. */
public class MainForm extends JFrame {
    private final JLabel lblSP = new JLabel("0"), lblKH = new JLabel("0"),
                         lblHD = new JLabel("0"), lblDT = new JLabel("0 đ");

    public MainForm(String hoTen) {
        setTitle("Quản lý cửa hàng quần áo");
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // ----- Thanh bên (menu) -----
        JPanel side = new JPanel();
        side.setBackground(Theme.PANEL);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(25, 18, 25, 18));
        side.setPreferredSize(new Dimension(260, 0));

        JLabel logo = new JLabel("👔 MEN'S SHOP");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Theme.ACCENT);
        logo.setAlignmentX(LEFT_ALIGNMENT);
        side.add(logo);
        side.add(Box.createVerticalStrut(30));

        side.add(menuButton("👕  Quản lý sản phẩm", Theme.BLUE, () -> new SanPhamForm().setVisible(true)));
        side.add(Box.createVerticalStrut(14));
        side.add(menuButton("👤  Quản lý khách hàng", Theme.PURPLE, () -> new KhachHangForm().setVisible(true)));
        side.add(Box.createVerticalStrut(14));
        side.add(menuButton("🧾  Quản lý hóa đơn", Theme.GREEN, () -> new HoaDonForm().setVisible(true)));
        side.add(Box.createVerticalStrut(14));
        side.add(menuButton("🔄  Làm mới thống kê", Theme.ORANGE, this::loadStats));
        side.add(Box.createVerticalGlue());
        side.add(menuButton("🚪  Đăng xuất", Theme.RED, () -> { new LoginForm().setVisible(true); dispose(); }));
        add(side, BorderLayout.WEST);

        // ----- Nội dung chính -----
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel hello = new JLabel("Xin chào, " + hoTen + " 👋");
        hello.setFont(new Font("Segoe UI", Font.BOLD, 28));
        hello.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Tổng quan hoạt động của cửa hàng");
        sub.setForeground(Theme.MUTED);
        sub.setFont(Theme.NORMAL);
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(hello); top.add(sub);
        main.add(top, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 22, 22));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        cards.add(card("👕 Sản phẩm", lblSP, Theme.BLUE));
        cards.add(card("👤 Khách hàng", lblKH, Theme.PURPLE));
        cards.add(card("🧾 Hóa đơn", lblHD, Theme.GREEN));
        cards.add(card("💰 Doanh thu", lblDT, Theme.ORANGE));
        main.add(cards, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        loadStats();
        // Tự cập nhật số liệu mỗi khi quay lại cửa sổ này
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) { loadStats(); }
        });
    }

    private JButton menuButton(String text, Color color, Runnable action) {
        JButton b = Theme.button(text, color);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JPanel card(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 9, getHeight(), 9, 9);   // vạch màu bên trái
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 20));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 17));
        t.setForeground(Theme.MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 40));
        value.setForeground(color.brighter());
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void loadStats() {
        lblSP.setText(scalar("SELECT COUNT(*) FROM SanPham"));
        lblKH.setText(scalar("SELECT COUNT(*) FROM KhachHang"));
        lblHD.setText(scalar("SELECT COUNT(*) FROM HoaDon"));
        try {
            lblDT.setText(Theme.money(Double.parseDouble(scalar("SELECT IFNULL(SUM(tongTien),0) FROM HoaDon"))));
        } catch (NumberFormatException e) { lblDT.setText("0 đ"); }
    }

    private String scalar(String sql) {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) { return "0"; }
    }
}
