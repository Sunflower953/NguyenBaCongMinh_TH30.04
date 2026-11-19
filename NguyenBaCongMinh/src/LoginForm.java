import javax.swing.*;
import java.awt.*;
import java.sql.*;

/** FORM 1: Đăng nhập (tài khoản mặc định: admin / 123456). Chạy chương trình từ file này. */
public class LoginForm extends JFrame {
    private final JTextField txtUser = Theme.field();
    private final JPasswordField txtPass = new JPasswordField();

    public LoginForm() {
        setTitle("Đăng nhập - Quản lý cửa hàng quần áo");
        setSize(820, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        // ----- Bên trái: banner gradient -----
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), getHeight(), Theme.BLUE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-80, getHeight() - 200, 300, 300);
                g2.fillOval(getWidth() - 120, -80, 240, 240);
                g2.dispose();
            }
        };
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel("👔");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel t1 = new JLabel("MEN'S FASHION");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        t1.setForeground(Theme.ACCENT);
        t1.setAlignmentX(CENTER_ALIGNMENT);
        JLabel t2 = new JLabel("Quản lý cửa hàng quần áo");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        t2.setForeground(Color.WHITE);
        t2.setAlignmentX(CENTER_ALIGNMENT);
        txt.add(icon); txt.add(Box.createVerticalStrut(10)); txt.add(t1); txt.add(Box.createVerticalStrut(6)); txt.add(t2);
        left.add(txt);

        // ----- Bên phải: form đăng nhập -----
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.PANEL);
        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(false);
        JLabel title = new JLabel("ĐĂNG NHẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.TEXT);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0, 0, 25, 0); g.anchor = GridBagConstraints.WEST;
        box.add(title, g);

        Theme.styleInput(txtPass);
        txtUser.setPreferredSize(new Dimension(280, 40));
        txtPass.setPreferredSize(new Dimension(280, 40));
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4, 0, 4, 0);
        g.gridy = 1; box.add(Theme.label("Tên đăng nhập"), g);
        g.gridy = 2; box.add(txtUser, g);
        g.gridy = 3; g.insets = new Insets(14, 0, 4, 0); box.add(Theme.label("Mật khẩu"), g);
        g.gridy = 4; g.insets = new Insets(4, 0, 4, 0); box.add(txtPass, g);

        JButton btn = Theme.button("ĐĂNG NHẬP", Theme.BLUE);
        btn.setPreferredSize(new Dimension(280, 44));
        g.gridy = 5; g.insets = new Insets(26, 0, 4, 0); box.add(btn, g);

        JLabel hint = new JLabel("Tài khoản mặc định: admin / 123456");
        hint.setForeground(Theme.MUTED);
        g.gridy = 6; g.insets = new Insets(10, 0, 0, 0); box.add(hint, g);
        right.add(box);

        add(left);
        add(right);

        btn.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());
        txtUser.addActionListener(e -> txtPass.requestFocus());
    }

    private void login() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            Theme.error(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }
        String sql = "SELECT hoTen FROM TaiKhoan WHERE username=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                new MainForm(rs.getString(1)).setVisible(true);
                dispose();
            } else {
                Theme.error(this, "Sai tên đăng nhập hoặc mật khẩu!");
            }
        } catch (SQLException ex) {
            Theme.error(this, "Không kết nối được CSDL!\nKiểm tra MySQL đã bật chưa, đã chạy database.sql chưa, và user/pass trong DBConnection.java.\n\nChi tiết: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
