import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/** FORM 5: Hóa đơn - Lập hóa đơn (tự trừ kho) / Xóa hóa đơn (hoàn lại kho) / Tìm kiếm. */
public class HoaDonForm extends JFrame {
    private final JComboBox<String> cboKH = new JComboBox<>(), cboSP = new JComboBox<>();
    private final JSpinner spnSL = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JLabel lblGia = new JLabel("0 đ"), lblKho = new JLabel("0"), lblTong = new JLabel("0 đ");
    private final JTextField txtTim = Theme.field();
    private final Map<String, double[]> spInfo = new HashMap<>();   // maSP -> {giá, tồn kho}
    private boolean loading = false;

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Mã HĐ", "Khách hàng", "Sản phẩm", "Số lượng", "Tổng tiền", "Ngày lập"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public HoaDonForm() {
        setTitle("Quản lý hóa đơn");
        setSize(1180, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(Theme.header("🧾  QUẢN LÝ HÓA ĐƠN"), BorderLayout.NORTH);

        // ----- Khung lập hóa đơn -----
        Theme.styleCombo(cboKH); Theme.styleCombo(cboSP);
        spnSL.setFont(Theme.NORMAL);
        for (JLabel l : new JLabel[]{lblGia, lblKho}) { l.setForeground(Theme.ACCENT); l.setFont(Theme.BOLD); }
        lblTong.setForeground(Theme.ORANGE);
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        Theme.addRow(form, 0, "Khách hàng", cboKH);
        Theme.addRow(form, 1, "Sản phẩm", cboSP);
        Theme.addRow(form, 2, "Đơn giá", lblGia);
        Theme.addRow(form, 3, "Tồn kho", lblKho);
        Theme.addRow(form, 4, "Số lượng", spnSL);
        Theme.addRow(form, 5, "THÀNH TIỀN", lblTong);

        JButton btnLap = Theme.button("🧾 Lập hóa đơn", Theme.GREEN);
        JButton btnXoa = Theme.button("🗑 Xóa hóa đơn", Theme.RED);
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 10));
        btns.setOpaque(false);
        btns.add(btnLap); btns.add(btnXoa);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(18, 6, 6, 6);
        form.add(btns, g);

        JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Theme.BG);
        west.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 7));
        west.setPreferredSize(new Dimension(390, 0));
        west.add(form, BorderLayout.NORTH);
        add(west, BorderLayout.WEST);

        // ----- Tìm kiếm + bảng hóa đơn -----
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        txtTim.setPreferredSize(new Dimension(300, 38));
        JButton btnTim = Theme.button("🔍 Tìm", Theme.ACCENT.darker());
        JButton btnAll = Theme.button("Tất cả", Theme.PURPLE);
        bar.add(Theme.label("Tìm theo mã HĐ / khách / sản phẩm:"));
        bar.add(txtTim); bar.add(btnTim); bar.add(btnAll);

        Theme.styleTable(table);
        Theme.moneyColumn(table, 4);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14, 7, 14, 14));
        center.add(bar, BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ----- Sự kiện -----
        cboSP.addActionListener(e -> capNhatTien());
        spnSL.addChangeListener(e -> capNhatTien());
        btnLap.addActionListener(e -> lapHoaDon());
        btnXoa.addActionListener(e -> xoaHoaDon());
        btnTim.addActionListener(e -> load(txtTim.getText().trim()));
        txtTim.addActionListener(e -> load(txtTim.getText().trim()));
        btnAll.addActionListener(e -> { txtTim.setText(""); load(""); });

        loadCombo();
        load("");
    }

    private String ma(JComboBox<String> c) {
        Object o = c.getSelectedItem();
        return o == null ? null : o.toString().split(" - ")[0];
    }

    private void loadCombo() {
        loading = true;
        cboKH.removeAllItems(); cboSP.removeAllItems(); spInfo.clear();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT maKH, tenKH FROM KhachHang ORDER BY maKH");
            while (rs.next()) cboKH.addItem(rs.getString(1) + " - " + rs.getString(2));
            rs = st.executeQuery("SELECT maSP, tenSP, giaBan, soLuong FROM SanPham ORDER BY maSP");
            while (rs.next()) {
                cboSP.addItem(rs.getString(1) + " - " + rs.getString(2));
                spInfo.put(rs.getString(1), new double[]{rs.getDouble(3), rs.getInt(4)});
            }
        } catch (SQLException ex) { Theme.error(this, "Lỗi tải danh sách: " + ex.getMessage()); }
        loading = false;
        capNhatTien();
    }

    private void capNhatTien() {
        if (loading) return;
        double[] info = spInfo.get(ma(cboSP));
        if (info == null) { lblGia.setText("0 đ"); lblKho.setText("0"); lblTong.setText("0 đ"); return; }
        int sl = (Integer) spnSL.getValue();
        lblGia.setText(Theme.money(info[0]));
        lblKho.setText(String.valueOf((int) info[1]));
        lblTong.setText(Theme.money(info[0] * sl));
    }

    private void load(String kw) {
        model.setRowCount(0);
        String sql = "SELECT h.maHD, k.tenKH, s.tenSP, h.soLuong, h.tongTien, h.ngayLap " +
                     "FROM HoaDon h JOIN KhachHang k ON h.maKH=k.maKH JOIN SanPham s ON h.maSP=s.maSP " +
                     "WHERE CAST(h.maHD AS CHAR) LIKE ? OR k.tenKH LIKE ? OR s.tenSP LIKE ? ORDER BY h.maHD DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getInt(4), rs.getDouble(5), rs.getTimestamp(6)});
        } catch (SQLException ex) { Theme.error(this, "Lỗi tải hóa đơn: " + ex.getMessage()); }
    }

    // ---------- LẬP HÓA ĐƠN (dùng transaction: thêm HĐ + trừ kho) ----------
    private void lapHoaDon() {
        String maKH = ma(cboKH), maSP = ma(cboSP);
        if (maKH == null || maSP == null) { Theme.error(this, "Cần có khách hàng và sản phẩm trước khi lập hóa đơn!"); return; }
        int sl = (Integer) spnSL.getValue();
        double[] info = spInfo.get(maSP);
        if (sl > info[1]) { Theme.error(this, "Không đủ hàng! Trong kho chỉ còn " + (int) info[1] + " sản phẩm."); return; }

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement("INSERT INTO HoaDon(maKH, maSP, soLuong, tongTien) VALUES (?,?,?,?)");
                 PreparedStatement p2 = c.prepareStatement("UPDATE SanPham SET soLuong = soLuong - ? WHERE maSP=? AND soLuong >= ?")) {
                p1.setString(1, maKH); p1.setString(2, maSP); p1.setInt(3, sl); p1.setDouble(4, info[0] * sl);
                p1.executeUpdate();
                p2.setInt(1, sl); p2.setString(2, maSP); p2.setInt(3, sl);
                if (p2.executeUpdate() == 0) throw new SQLException("Không đủ hàng trong kho!");
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            Theme.info(this, "Lập hóa đơn thành công!\nThành tiền: " + Theme.money(info[0] * sl));
            spnSL.setValue(1);
            loadCombo();
            load("");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }

    // ---------- XÓA HÓA ĐƠN (hoàn lại hàng vào kho) ----------
    private void xoaHoaDon() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.error(this, "Hãy chọn hóa đơn cần xóa trong bảng!"); return; }
        int maHD = (Integer) model.getValueAt(r, 0);
        if (!Theme.confirm(this, "Xóa hóa đơn #" + maHD + " và hoàn lại hàng vào kho?")) return;

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement("SELECT maSP, soLuong FROM HoaDon WHERE maHD=?");
                 PreparedStatement p2 = c.prepareStatement("UPDATE SanPham SET soLuong = soLuong + ? WHERE maSP=?");
                 PreparedStatement p3 = c.prepareStatement("DELETE FROM HoaDon WHERE maHD=?")) {
                p1.setInt(1, maHD);
                ResultSet rs = p1.executeQuery();
                if (rs.next()) {
                    p2.setInt(1, rs.getInt(2)); p2.setString(2, rs.getString(1));
                    p2.executeUpdate();
                }
                p3.setInt(1, maHD);
                p3.executeUpdate();
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            Theme.info(this, "Đã xóa hóa đơn và hoàn lại hàng vào kho!");
            loadCombo();
            load("");
        } catch (SQLException ex) { Theme.error(this, "Lỗi: " + ex.getMessage()); }
    }
}
