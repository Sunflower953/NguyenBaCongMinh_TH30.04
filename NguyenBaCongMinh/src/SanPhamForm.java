import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** FORM 3: Quản lý sản phẩm - Thêm / Sửa / Xóa / Tìm kiếm. */
public class SanPhamForm extends JFrame {
    private final JTextField txtMa = Theme.field(), txtTen = Theme.field(), txtMau = Theme.field(),
                             txtGia = Theme.field(), txtSL = Theme.field(), txtTim = Theme.field();
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{
        "Áo thun", "Áo sơ mi", "Áo khoác", "Quần jean", "Quần short", "Quần tây", "Phụ kiện"});
    private final JComboBox<String> cboSize = new JComboBox<>(new String[]{"S", "M", "L", "XL", "XXL"});
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Mã SP", "Tên sản phẩm", "Loại", "Size", "Màu", "Giá bán", "Số lượng"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public SanPhamForm() {
        setTitle("Quản lý sản phẩm");
        setSize(1180, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(Theme.header("👕  QUẢN LÝ SẢN PHẨM"), BorderLayout.NORTH);

        // ----- Khung nhập liệu bên trái -----
        Theme.styleCombo(cboLoai); Theme.styleCombo(cboSize);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        Theme.addRow(form, 0, "Mã SP", txtMa);
        Theme.addRow(form, 1, "Tên SP", txtTen);
        Theme.addRow(form, 2, "Loại", cboLoai);
        Theme.addRow(form, 3, "Size", cboSize);
        Theme.addRow(form, 4, "Màu sắc", txtMau);
        Theme.addRow(form, 5, "Giá bán (đ)", txtGia);
        Theme.addRow(form, 6, "Số lượng", txtSL);

        JButton btnThem = Theme.button("➕ Thêm", Theme.GREEN);
        JButton btnSua  = Theme.button("✏ Sửa", Theme.BLUE);
        JButton btnXoa  = Theme.button("🗑 Xóa", Theme.RED);
        JButton btnMoi  = Theme.button("🔄 Làm mới", Theme.ORANGE);
        JPanel btns = new JPanel(new GridLayout(2, 2, 10, 10));
        btns.setOpaque(false);
        btns.add(btnThem); btns.add(btnSua); btns.add(btnXoa); btns.add(btnMoi);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(18, 6, 6, 6);
        form.add(btns, g);

        JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Theme.BG);
        west.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 7));
        west.setPreferredSize(new Dimension(370, 0));
        west.add(form, BorderLayout.NORTH);
        add(west, BorderLayout.WEST);

        // ----- Thanh tìm kiếm + bảng -----
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        txtTim.setPreferredSize(new Dimension(300, 38));
        JButton btnTim = Theme.button("🔍 Tìm", Theme.ACCENT.darker());
        JButton btnAll = Theme.button("Tất cả", Theme.PURPLE);
        bar.add(Theme.label("Tìm theo mã / tên / loại / màu:"));
        bar.add(txtTim); bar.add(btnTim); bar.add(btnAll);

        Theme.styleTable(table);
        Theme.moneyColumn(table, 5);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14, 7, 14, 14));
        center.add(bar, BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ----- Sự kiện -----
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

    // ---------- Đọc dữ liệu (có tìm kiếm) ----------
    private void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT * FROM SanPham WHERE maSP LIKE ? OR tenSP LIKE ? OR loai LIKE ? OR mau LIKE ? ORDER BY maSP";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getString("maSP"), rs.getString("tenSP"), rs.getString("loai"),
                    rs.getString("size"), rs.getString("mau"), rs.getDouble("giaBan"), rs.getInt("soLuong")});
        } catch (SQLException ex) { Theme.error(this, "Lỗi tải dữ liệu: " + ex.getMessage()); }
    }

    private void chonDong() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtMa.setText(model.getValueAt(r, 0).toString());
        txtTen.setText(model.getValueAt(r, 1).toString());
        cboLoai.setSelectedItem(model.getValueAt(r, 2));
        cboSize.setSelectedItem(model.getValueAt(r, 3));
        txtMau.setText(String.valueOf(model.getValueAt(r, 4)));
        txtGia.setText(String.format("%.0f", ((Number) model.getValueAt(r, 5)).doubleValue()));
        txtSL.setText(model.getValueAt(r, 6).toString());
        txtMa.setEditable(false);   // không cho sửa khóa chính
    }

    // ---------- Kiểm tra dữ liệu nhập ----------
    private boolean hopLe() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            Theme.error(this, "Mã SP và Tên SP không được để trống!"); return false;
        }
        try {
            if (Double.parseDouble(txtGia.getText().trim()) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { Theme.error(this, "Giá bán phải là số lớn hơn 0!"); return false; }
        try {
            if (Integer.parseInt(txtSL.getText().trim()) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { Theme.error(this, "Số lượng phải là số nguyên không âm!"); return false; }
        return true;
    }

    // ---------- THÊM ----------
    private void them() {
        if (!hopLe()) return;
        String sql = "INSERT INTO SanPham VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtMa.getText().trim());
            ps.setString(2, txtTen.getText().trim());
            ps.setString(3, (String) cboLoai.getSelectedItem());
            ps.setString(4, (String) cboSize.getSelectedItem());
            ps.setString(5, txtMau.getText().trim());
            ps.setDouble(6, Double.parseDouble(txtGia.getText().trim()));
            ps.setInt(7, Integer.parseInt(txtSL.getText().trim()));
            ps.executeUpdate();
            Theme.info(this, "Thêm sản phẩm thành công!");
            lamMoi();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Theme.error(this, "Mã sản phẩm đã tồn tại!");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    // ---------- SỬA ----------
    private void sua() {
        if (table.getSelectedRow() < 0) { Theme.error(this, "Hãy chọn sản phẩm cần sửa trong bảng!"); return; }
        if (!hopLe()) return;
        String sql = "UPDATE SanPham SET tenSP=?, loai=?, size=?, mau=?, giaBan=?, soLuong=? WHERE maSP=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.setString(2, (String) cboLoai.getSelectedItem());
            ps.setString(3, (String) cboSize.getSelectedItem());
            ps.setString(4, txtMau.getText().trim());
            ps.setDouble(5, Double.parseDouble(txtGia.getText().trim()));
            ps.setInt(6, Integer.parseInt(txtSL.getText().trim()));
            ps.setString(7, txtMa.getText().trim());
            ps.executeUpdate();
            Theme.info(this, "Cập nhật thành công!");
            lamMoi();
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    // ---------- XÓA ----------
    private void xoa() {
        if (table.getSelectedRow() < 0) { Theme.error(this, "Hãy chọn sản phẩm cần xóa trong bảng!"); return; }
        if (!Theme.confirm(this, "Bạn có chắc muốn xóa sản phẩm " + txtMa.getText() + "?")) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM SanPham WHERE maSP=?")) {
            ps.setString(1, txtMa.getText().trim());
            ps.executeUpdate();
            Theme.info(this, "Đã xóa sản phẩm!");
            lamMoi();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Theme.error(this, "Không thể xóa: sản phẩm này đã có trong hóa đơn!");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    private void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtMau.setText(""); txtGia.setText(""); txtSL.setText("");
        cboLoai.setSelectedIndex(0); cboSize.setSelectedIndex(0);
        txtMa.setEditable(true);
        table.clearSelection();
        txtTim.setText("");
        load("");
    }
}
