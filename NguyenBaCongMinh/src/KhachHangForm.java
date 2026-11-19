import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** FORM 4: Quản lý khách hàng - Thêm / Sửa / Xóa / Tìm kiếm. */
public class KhachHangForm extends JFrame {
    private final JTextField txtMa = Theme.field(), txtTen = Theme.field(), txtSdt = Theme.field(),
                             txtDiaChi = Theme.field(), txtTim = Theme.field();
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Mã KH", "Tên khách hàng", "Số điện thoại", "Địa chỉ"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public KhachHangForm() {
        setTitle("Quản lý khách hàng");
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(Theme.header("👤  QUẢN LÝ KHÁCH HÀNG"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        Theme.addRow(form, 0, "Mã KH", txtMa);
        Theme.addRow(form, 1, "Họ tên", txtTen);
        Theme.addRow(form, 2, "SĐT", txtSdt);
        Theme.addRow(form, 3, "Địa chỉ", txtDiaChi);

        JButton btnThem = Theme.button("➕ Thêm", Theme.GREEN);
        JButton btnSua  = Theme.button("✏ Sửa", Theme.BLUE);
        JButton btnXoa  = Theme.button("🗑 Xóa", Theme.RED);
        JButton btnMoi  = Theme.button("🔄 Làm mới", Theme.ORANGE);
        JPanel btns = new JPanel(new GridLayout(2, 2, 10, 10));
        btns.setOpaque(false);
        btns.add(btnThem); btns.add(btnSua); btns.add(btnXoa); btns.add(btnMoi);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(18, 6, 6, 6);
        form.add(btns, g);

        JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Theme.BG);
        west.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 7));
        west.setPreferredSize(new Dimension(370, 0));
        west.add(form, BorderLayout.NORTH);
        add(west, BorderLayout.WEST);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        txtTim.setPreferredSize(new Dimension(300, 38));
        JButton btnTim = Theme.button("🔍 Tìm", Theme.ACCENT.darker());
        JButton btnAll = Theme.button("Tất cả", Theme.PURPLE);
        bar.add(Theme.label("Tìm theo mã / tên / SĐT / địa chỉ:"));
        bar.add(txtTim); bar.add(btnTim); bar.add(btnAll);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14, 7, 14, 14));
        center.add(bar, BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> load(txtTim.getText().trim()));
        txtTim.addActionListener(e -> load(txtTim.getText().trim()));
        btnAll.addActionListener(e -> { txtTim.setText(""); load(""); });
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) chonDong(); });

        load("");
    }

    private void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT * FROM KhachHang WHERE maKH LIKE ? OR tenKH LIKE ? OR sdt LIKE ? OR diaChi LIKE ? ORDER BY maKH";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        } catch (SQLException ex) { Theme.error(this, "Lỗi tải dữ liệu: " + ex.getMessage()); }
    }

    private void chonDong() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtMa.setText(String.valueOf(model.getValueAt(r, 0)));
        txtTen.setText(String.valueOf(model.getValueAt(r, 1)));
        txtSdt.setText(String.valueOf(model.getValueAt(r, 2)));
        txtDiaChi.setText(String.valueOf(model.getValueAt(r, 3)));
        txtMa.setEditable(false);
    }

    private boolean hopLe() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            Theme.error(this, "Mã KH và Họ tên không được để trống!"); return false;
        }
        String sdt = txtSdt.getText().trim();
        if (!sdt.isEmpty() && !sdt.matches("0\\d{9,10}")) {
            Theme.error(this, "Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!"); return false;
        }
        return true;
    }

    private void them() {
        if (!hopLe()) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO KhachHang VALUES (?,?,?,?)")) {
            ps.setString(1, txtMa.getText().trim());
            ps.setString(2, txtTen.getText().trim());
            ps.setString(3, txtSdt.getText().trim());
            ps.setString(4, txtDiaChi.getText().trim());
            ps.executeUpdate();
            Theme.info(this, "Thêm khách hàng thành công!");
            lamMoi();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Theme.error(this, "Mã khách hàng đã tồn tại!");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    private void sua() {
        if (table.getSelectedRow() < 0) { Theme.error(this, "Hãy chọn khách hàng cần sửa trong bảng!"); return; }
        if (!hopLe()) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE KhachHang SET tenKH=?, sdt=?, diaChi=? WHERE maKH=?")) {
            ps.setString(1, txtTen.getText().trim());
            ps.setString(2, txtSdt.getText().trim());
            ps.setString(3, txtDiaChi.getText().trim());
            ps.setString(4, txtMa.getText().trim());
            ps.executeUpdate();
            Theme.info(this, "Cập nhật thành công!");
            lamMoi();
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xoa() {
        if (table.getSelectedRow() < 0) { Theme.error(this, "Hãy chọn khách hàng cần xóa trong bảng!"); return; }
        if (!Theme.confirm(this, "Bạn có chắc muốn xóa khách hàng " + txtMa.getText() + "?")) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM KhachHang WHERE maKH=?")) {
            ps.setString(1, txtMa.getText().trim());
            ps.executeUpdate();
            Theme.info(this, "Đã xóa khách hàng!");
            lamMoi();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Theme.error(this, "Không thể xóa: khách hàng này đã có hóa đơn!");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    private void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtSdt.setText(""); txtDiaChi.setText(""); txtTim.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
        load("");
    }
}
