package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.TinChiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Màn hình sinh viên: năm học thứ mấy, tín chỉ tích lũy / bị rút / đang học.
 * UI Soft Azure – đồng bộ hệ thống.
 */
public class HocTapTinChiPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final TinChiService service = new TinChiService();

    private JLabel lblNamThu, lblNamNhap, lblTrangThai;
    private JLabel lblTichLuy, lblBiRut, lblDangKy, lblHieuLuc;
    private DefaultTableModel modelNam;

    public HocTapTinChiPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        add(buildBanner(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildTheTienDo());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(buildTheTinChi());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(buildBangTheoNam());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 88));

        JLabel title = new JLabel("Tiến độ học tập & Tín chỉ");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Năm học hiện tại · Tín chỉ tích lũy · Tín chỉ bị rút (khung 5 năm vận hành)");
        sub.setFont(UITheme.FONT_BASE);
        sub.setForeground(new Color(255, 255, 255, 210));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(sub);
        banner.add(text, BorderLayout.WEST);
        return banner;
    }

    private JPanel buildTheTienDo() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel c1 = UITheme.statCard("Năm học thứ", "—", UITheme.TINT_BLUE, UITheme.TEXT_BLUE);
        JPanel c2 = UITheme.statCard("Năm nhập học", "—", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET);
        JPanel c3 = UITheme.statCard("Trạng thái", "—", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        row.add(c1); row.add(c2); row.add(c3);

        lblNamThu = findGiaTri(c1);
        lblNamNhap = findGiaTri(c2);
        lblTrangThai = findGiaTri(c3);
        return row;
    }

    private JPanel buildTheTinChi() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel a = UITheme.statCard("Tín chỉ tích lũy", "0", new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69));
        JPanel b = UITheme.statCard("Tín chỉ bị rút", "0", new Color(0xFE, 0xF2, 0xF2), new Color(0xDC, 0x26, 0x26));
        JPanel c = UITheme.statCard("Đang đăng ký", "0", new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8));
        JPanel d = UITheme.statCard("TC có hiệu lực", "0", new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED));
        row.add(a); row.add(b); row.add(c); row.add(d);

        lblTichLuy = findGiaTri(a);
        lblBiRut = findGiaTri(b);
        lblDangKy = findGiaTri(c);
        lblHieuLuc = findGiaTri(d);
        return row;
    }

    private JLabel findGiaTri(JPanel the) {
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel && "giaTri".equals(comp.getName())) return (JLabel) comp;
        }
        // fallback: label lớn nhất font
        JLabel found = null;
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel lbl) {
                if (found == null || lbl.getFont().getSize() > found.getFont().getSize()) found = lbl;
            }
        }
        return found != null ? found : new JLabel("—");
    }

    private JPanel buildBangTheoNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Chi tiết tín chỉ theo từng năm học"), BorderLayout.NORTH);

        modelNam = new DefaultTableModel(new String[]{"Năm học", "Tín chỉ đạt", "Tín chỉ rút", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelNam);
        table.setRowHeight(36);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(10, 220));
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void taiDuLieu() {
        String maSV = taiKhoan.getMaSV() != null && !taiKhoan.getMaSV().isBlank() ? taiKhoan.getMaSV() : taiKhoan.getTenDangNhap();
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                TinChiSinhVien tc = service.layTienDo(maSV);
                List<TinChiTheoNam> ls = service.layLichSuNam(maSV);
                return new Object[]{tc, ls};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    TinChiSinhVien tc = (TinChiSinhVien) kq[0];
                    List<TinChiTheoNam> ls = (List<TinChiTheoNam>) kq[1];
                    if (tc == null) {
                        UIUtils.thongBaoLoi(HocTapTinChiPanel.this, "Không tìm thấy hồ sơ tín chỉ. Kiểm tra mã SV liên kết tài khoản.");
                        return;
                    }
                    if (lblNamThu != null) lblNamThu.setText(tc.getNamThu() != null ? ("Năm " + tc.getNamThu()) : "—");
                    if (lblNamNhap != null) lblNamNhap.setText(tc.getNamNhapHoc() != null ? String.valueOf(tc.getNamNhapHoc()) : "—");
                    if (lblTrangThai != null) lblTrangThai.setText(nhanTT(tc.getTrangThaiHoc()));
                    if (lblTichLuy != null) lblTichLuy.setText(String.valueOf(tc.getTinChiTichLuy()));
                    if (lblBiRut != null) lblBiRut.setText(String.valueOf(tc.getTinChiBiRut()));
                    if (lblDangKy != null) lblDangKy.setText(String.valueOf(tc.getTinChiDangKy()));
                    if (lblHieuLuc != null) lblHieuLuc.setText(String.valueOf(tc.getTinChiConHieuLuc()));

                    modelNam.setRowCount(0);
                    if (ls != null) {
                        for (TinChiTheoNam n : ls) {
                            modelNam.addRow(new Object[]{n.getNamHoc(), n.getTinChiDat(), n.getTinChiRut(),
                                    n.getGhiChu() != null ? n.getGhiChu() : ""});
                        }
                    }
                    if (modelNam.getRowCount() == 0) {
                        modelNam.addRow(new Object[]{"—", "—", "—", "Chưa có lịch sử theo năm"});
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HocTapTinChiPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private static String nhanTT(String tt) {
        if (tt == null) return "—";
        return switch (tt) {
            case "DANG_HOC" -> "Đang học";
            case "RA_TRUONG" -> "Ra trường";
            case "THOI_HOC" -> "Thôi học";
            default -> tt;
        };
    }
}